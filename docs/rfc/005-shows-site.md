# RFC 005: Making the site from the shows file

Status: Proposed · Updated: 2026-11-24 · Code: not written yet. Planned: `src/com/bolsinga/site/Main.java`,
`src/scripts/site`, `src/com/bolsinga/music/data/raw/Media.java`,
`src/com/bolsinga/music/data/json/{Music,Check}.java`

## Summary

`bolsinga/site` will take over editing shows, venues, artists and relations, and save them to a
shows file ([site RFC 018](https://github.com/bolsinga/site/blob/main/docs/rfc/018-shows-file.md)).
This RFC adds a third command, **`shows-site`**, that makes the site from that file, the iTunes
export and the raw diary files. It is site RFC 018's rule 13.

Neither command today can do this. `site` reads the raw text files, and `json-site` reads one music
file that holds shows and iTunes data together. A shows-only file given to `json-site` makes artist
pages with no albums, empty `tracks/` pages, and a home page with no song counts or last played
song.

## The shows file

The shows file is the format of `json/shows.json` ([RFC 002](002-inputs.md#showsjson)): a
`music.json` whose `songs` and `albums` are `[]`. It holds every venue, every artist (including
artists known only from iTunes), every relation and every show. It lives in the data folder, named
as the export names it: the `showsJsonFile` setting plus `.json`, which is `shows.json`.

The website's `json/shows.json` is already such a file. Every run of any of the three commands
goes on writing it.

## The command

```
site shows-site <output_dir> <data_dir> <itunes.json>
```

It reads, from `data_dir`, `settings.properties`, the shows file, `comments.txt` and `statics.txt`,
plus the iTunes export. `src/scripts/site` checks each file exists and passes them to `Main`, as it
does for `site`. `Main.usage` gains the third form.

The diary is read by `raw.Diary.create`, as `site` reads it. `site` and `json-site` are unchanged.

## Reading

1. Read the shows file into a `JSONObject`. Its `songs` and `albums` must be empty; if not, the run
   stops, because they'd collide with the IDs iTunes is given in step 2.
2. Parse the iTunes export as `raw.Media.createMedia` does today. Albums and songs get IDs `aN` and
   `sN` in the order the export is read, so the same export gives the same IDs as `site`.
3. Join each album and song artist to a shows file artist by **name**, exactly as the raw files join
   today (no trimming or Unicode normalizing). An iTunes artist name that no shows file artist has
   stops the run, listing every such name at once:

   ```
   shows.json is missing 2 iTunes artist(s); add them, then run again:
     An Artist Only In iTunes
     Another New Artist
   ```

   The person adds each with site's **New Artist**, which gives it the next `arN`.
4. Give each joined artist the shows file's ID, and write the albums and songs into the
   `JSONObject`'s `albums` and `songs` with the existing `json.Album` and `json.Song` writers.
5. **Sort names.** An artist's `sortname` in the shows file wins. When it has none and iTunes gives
   one, the iTunes sort name is written in, made diacritic-free as `raw.Artist.setSortname` does.
   Otherwise the artist has no sort name. Nothing is derived from the name; site derives one when it
   creates an artist (its rule 12).
6. Hand the `JSONObject` to the JSON reader, through a new `json.Music.create` that takes a
   `JSONObject` and the file name to report. The checks below run, then the same reading as
   `json-site`. Albums attach themselves to their performers and song artists, as they do now.

The `Music` the reader builds then goes to `Main.generateSite`, as from either other command, which
runs the venue, markup and name checks and writes the site and the JSON exports.

Using the JSON reader means `shows-site` and `json-site` share one reader and one set of checks, and
`shows-site` adds only the join. `raw.Media` is changed to give back what it parsed without keeping
it in `raw.Artist`'s name map, or a copy of its loop is made for the join; the code PR picks one.

## Checks

site's `ArchiveCheck` (its rule 10) checks the file before the app writes it. web_generator makes
the same checks, so a file written by hand or by another program is caught too. `json.Check`
already makes most of them ([RFC 002](002-inputs.md#checks)). This RFC adds:

| Check | `json-site` | `shows-site` |
|---|---|---|
| Each ID starts with its kind's prefix: `v`, `ar`, `r`, `sh`, and `a` and `s` for albums and songs | stops | stops |
| A date is possible: month 1–12, and a day that exists in its month, in a leap year when the year is unknown | stops | stops |
| Two venues have the same name | stops | stops |
| `location.zip`, `venue.comment`, `artist.location` or `artist.comment` is present | not checked | stops |
| `songs` or `albums` is not empty | n/a | stops |
| An iTunes artist name isn't a shows file artist | n/a | stops |

- The first three are cheap and true of every `music.json` web_generator writes, so they apply to
  `json-site` as well. RFC 002's Checks table is updated with them.
- The fields of the fourth row are never set by the raw files, and site doesn't model them (its rule
  6). A shows file that has them was not written from the raw files or by the app, and the app would
  drop them on its next save. `json-site` keeps reading them, because `music.json` may carry them.
- As now, every problem is listed at once, each naming the array, the index and the ID.

## Verifying

Site RFC 018 rule 14.1: making the site from the raw files and from the `json/shows.json` they export
must give the same HTML.

1. Run `site` on the raw files and the iTunes export, with `JAVA_OPTS=-Dweb.debug_output=true`.
2. Copy its `json/shows.json` into a copy of the data folder.
3. Run `shows-site` on that folder and the same iTunes export.
4. `diff -r` the two trees, leaving out what [RFC 004](004-verifying-output.md) lists as changing
   from run to run.

Only the HTML must match. The other files, including the JSON exports in `json/`, may differ in key
order, `timestamp`, and the order of artists and venues. In practice they are expected to match too,
as `json-site`'s do today.

Two more checks before merging:

- Removing an artist that only iTunes knows from the shows file stops the run with its name.
- A shows file with one of each new problem in the Checks table stops the run listing all of them.

## After this

Once site writes the shows file, `shows.txt`, `venuemap.txt`, `bandsort.txt`, `artistids.txt` and
`relations.txt` are no longer edited, and web_generator is run with `shows-site` (site RFC 018 rule
14.3). `site` stays, to rebuild old data. When the code lands, RFCs 002 and 003 describe the new
command and this RFC's status changes to describe existing behavior.

## Out of scope

- The diary stays in `comments.txt` and `statics.txt`. Moving it to JSON, where entries keep their
  IDs, is separate (#117, #188).
- Played music stays with the iTunes export (site RFC 018).
