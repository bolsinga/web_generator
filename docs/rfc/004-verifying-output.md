# RFC 004: Building, and checking the site is unchanged

Status: Describes existing behavior · Updated: 2026-11-24 · Code: `build.xml`, `build_tools/`,
`.github/workflows/ant.yml`, `src/scripts/site`, `src/com/bolsinga/test`

## Summary

The build is Ant on Java 26. There are no automated tests of the output. The way to know a change
is safe is to generate the site before and after it and compare the two trees.

## Building

```
ant -Dbuild.id=<id>
```

- The default target is `package`. It compiles everything into `output/obj/classes`, makes
  `output/obj/sym/lib/web_generator.jar`, and installs the runnable program in `output/dst/`:
  the `site` script and `lib/` with `web_generator.jar`, `ecs-1.4.2.jar` and `json-20200518.jar`.
- `build.id`, the build date and the copyright year are filled into `web.properties` from
  `src/properties/config.properties`; they show up in the generated pages.
- `ant clean` removes `output/`. `output/` is not ignored by git; don't commit it.
- CI (`.github/workflows/ant.yml`) builds every push and pull request to `master` with Zulu
  JDK 26 and uploads `output/dst`.
- `build_tools/` holds older wrapper scripts. `build_ant.sh` expects Ant at
  `~/Documents/code/java_libs/apache-ant-1.9.3`.

## Running

See [RFC 002](002-inputs.md) for the commands. For example:

```
output/dst/site site      /tmp/site <data_dir> <itunes.json>
output/dst/site json-site /tmp/site-json <data_dir>/settings.properties /tmp/site/json
```

`json-site` reads the diary and music files in `json_dir` by the names in the settings, the same
names the export writes, so a site's `json/` folder can be passed as it is.

## Comparing output

Set `JAVA_OPTS="-Dweb.debug_output=true"` in the environment when running the `site` script, so
pages leave out when, and by which build, they were made.

**A code change** to generation:

1. Build `master`, run `site` into `A`.
2. Build the change, run `site` with the same data into `B`.
3. `diff -r A B`.

**Raw and JSON give the same site**, which is the check for any change to the readers or to
another program writing the JSON:

1. Run `site` into `A`.
2. Run `json-site` on `A/json/` into `B`.
3. `diff -r -x json A B`.

Differences that come from the environment, not the change:

- the `timestamp` in `music.json`, `shows.json` and `diary.json` is when the raw files were read,
  so two `site` runs differ in that one line of each export; `json-site` carries the timestamp
  through unchanged
- the `Author` meta tag is the `user.name` of whoever ran it
- dates are formatted in the default time zone and locale

### Results on the real data

Both checks were run on 2026-11-24 with the real data, `master` at 188dc85, a fresh iTunes export
and `web.debug_output` on (#184). Each run writes 3,562 files in a few seconds.

- **`site` twice from the same data:** all HTML, CSS, RSS and `.ics` files are identical. The only
  differences are the three JSON `timestamp` lines.
- **Raw and JSON:** `diff -r -x json A B` finds no differences. The JSON in `B/json/` is also
  byte-identical to `A/json/`, timestamps included.

Raw and JSON input give the same site. Run these again after changing a reader or the JSON, and
expect only the differences listed above.

## Tests

`src/com/bolsinga/test` has `CSSTest`, `ICalTest` and `RSSTest`. They are `main` programs, not
JUnit, and Ant only compiles them. `CSSTest` checks for 2 arguments and then reads the third.

## Known problems

Recorded here so they are not mistaken for intended behavior:

- `Util.getRecentItems` fails if there are fewer shows or entries than `recentCount` or
  `diaryCount`.
- `Util.toHTMLSafe` escapes `&` only when it is not followed by `a`, `g` or `l`.
- The search form's site is fixed to `bolsinga.com`.
- The json `Venue` copy constructor does not copy `sortname`. It is never called.
