# web_generator

A Java program that generates [bolsinga.com](https://www.bolsinga.com), a fully static site of the
shows the owner has seen, a diary, and iTunes music. The site is copied to the server with rsync,
which is not in this repo. It has worked for decades; change it carefully and keep its output the
same unless a change is meant to alter it.

[bolsinga/site](https://github.com/bolsinga/site), a Swift app, reads `json/shows.json` from the
website. The JSON this program writes is a public contract.

## How it works

```
raw text files + iTunes JSON ─► raw.Music / raw.Diary ─┐
                                                       ├─► Music / Diary interfaces ─► HTML, CSS, RSS, iCal, JSON
music.json + diary.json ──────► json.Music / json.Diary ┘
```

The Java types in `com.bolsinga.music.data` and `com.bolsinga.diary.data` are what matter. The raw
and JSON formats are interchangeable ways to fill them, and the page code only sees the interfaces.

## Layout

- `src/com/bolsinga/music/data`, `diary/data`: the interfaces; `raw/` and `json/` implement them.
- `src/com/bolsinga/itunes`: reads the iTunes library export.
- `src/com/bolsinga/web`: shared page code: `Links` (paths), `Util`, `RecordDocumentCreator`,
  `Backgrounder` (thread pool), settings.
- `src/com/bolsinga/music`, `diary`, `rss`, `ical`: the page generators, plus `Lookup` and
  `Compare`.
- `src/com/bolsinga/site/Main.java`: the entry point. `src/scripts/site` is the script that runs it.
- `src/properties/config.properties`: the site's text and formats.
- `lib/`: the ECS and org.json jars.

## Building and running

- Build with Ant on Java 26: `ant -Dbuild.id=<id>`. Ant may not be on `PATH`; the owner's copy is
  `~/Documents/code/java_libs/apache-ant-1.9.3/bin/ant`.
- The runnable program is `output/dst/site`. `output/` is a build product; never commit it.
- `output/dst/site site <out> <data_dir> <itunes.json>` builds from the raw files.
  `output/dst/site json-site <out> <settings.properties> <json_dir>` builds from JSON.
- The data and `settings.properties` live outside this repo. Don't go looking for them; ask.
- There are no automated tests of the output. To check a change, generate the site before and after
  with `JAVA_OPTS=-Dweb.debug_output=true` and `diff -r` the trees
  ([RFC 004](docs/rfc/004-verifying-output.md)).

## Rules

- Show, venue and artist IDs are URLs (`dates/shN.html`, `venues/vN.html`, `bands/arN.html`).
  Never renumber or reuse them.
- Keep the raw and JSON readers able to produce the same `Music` and `Diary`. A field added to an
  interface is read by both and written to the JSON.
- A change to the JSON is a change to `bolsinga/site`'s input; keep old readers working.
- Match the style of the file being edited: 2-space indent, `StringBuilder` for messages,
  `WebException` for data errors.

## RFCs

Describe how things work, in `docs/rfc/`, named `NNN-name.md`, as in `bolsinga/site`. Read the one
for the area you're changing. A change that alters what an RFC describes updates it in the same PR.

- [`docs/rfc/001-data-model.md`](docs/rfc/001-data-model.md): the `Music` and `Diary` types, which
  fields are optional, dates, relations, sort names, and who assigns each ID.
- [`docs/rfc/002-inputs.md`](docs/rfc/002-inputs.md): the `site` and `json-site` commands, every raw
  file format, the JSON schema, the checks each reader makes, and what another JSON writer must get
  right.
- [`docs/rfc/003-generation.md`](docs/rfc/003-generation.md): the generation stages, `Lookup` and
  `Compare`, the site's layout and URLs, which fields each page uses, settings, and threading.
- [`docs/rfc/004-verifying-output.md`](docs/rfc/004-verifying-output.md): building, running, comparing
  generated sites, and known problems.
- [`docs/rfc/005-shows-site.md`](docs/rfc/005-shows-site.md) (proposed): the `shows-site` command,
  which makes the site from `bolsinga/site`'s shows file, the iTunes export and the raw diary files.

`docs/notes.txt` is an old sketch of the model and the iCalendar format; the RFCs supersede it.
