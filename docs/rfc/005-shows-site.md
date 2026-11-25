# RFC 005: Making the site from the shows file

Status: Describes existing behavior · Updated: 2026-11-24 · Code: `src/com/bolsinga/site/Main.java`,
`src/scripts/site`, `src/com/bolsinga/music/data/raw/Media.java`,
`src/com/bolsinga/music/data/json/{Music,Check}.java`

## Summary

`bolsinga/site` will take over editing shows, venues, artists and relations, and save them to a
shows file ([site RFC 018](https://github.com/bolsinga/site/blob/main/docs/rfc/018-shows-file.md)).
This RFC describes the third command, **`shows-site`**, that makes the site from that file, the
iTunes export and the raw diary files. It is site RFC 018's rule 13.

Neither of the other commands can do this. `site` reads the raw text files, and `json-site` reads
one music file that holds shows and iTunes data together. A shows-only file given to `json-site`
makes artist pages with no albums, empty `tracks/` pages, and a home page with no song counts or
last played song.

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
plus the iTunes export. `src/scripts/site` checks the settings and diary files exist and passes
them to `Main`, with `data_dir` itself:

```
Main shows-site <output_dir> <settings.properties> <shows_dir> <comments.txt> <statics.txt> <itunes.json>
```

`Main` finds the shows file in `shows_dir` by the `showsJsonFile` setting, as `json-site` finds its
files in `json_dir`.

The diary is read by `raw.Diary.create`, as `site` reads it. `site` and `json-site` are unchanged.

## Reading

1. Read the shows file into a `JSONObject` and check it ([Checks](#checks)). Its `songs` and
   `albums` must be empty; if not, the run stops, because they'd collide with the IDs iTunes is
   given in step 2.
2. Parse the iTunes export as `raw.Media.createMedia` does today. Albums and songs get IDs `aN` and
   `sN` in the parser's sorted album order ([RFC 002](002-inputs.md#itunes)), so the same export
   gives the same IDs as `site`.
3. Join each album and song artist to a shows file artist by **name**, exactly as the raw files join
   today (no trimming or Unicode normalizing). An iTunes artist name that no shows file artist has
   stops the run, listing every such name at once, album artists before song artists, each in
   iTunes' order:

   ```
   shows.json is missing 2 iTunes artist(s); add them, then run again:
     An Artist Only In iTunes
     Another New Artist
   ```

   The person adds each with site's **New Artist**, which gives it the next `arN`.
4. Give each joined artist the shows file's ID, and write the albums and songs into the
   `JSONObject`'s `albums` and `songs` with the existing `json.Album` and `json.Song` writers.
5. **Sort names.** An artist's `sortname` in the shows file wins. When it has none and iTunes gives
   one, the iTunes sort name is written in, decomposed (NFD) as `raw.Artist.setSortname` does.
   `raw.Media` keeps these in `fSortnames`, apart from sort names derived from names.
   Otherwise the artist has no sort name. Nothing is derived from the name; site derives one when it
   creates an artist (its rule 12).
6. Hand the `JSONObject` to the same reading as `json-site`. Albums attach themselves to their
   performers and song artists.

The `Music` the reader builds then goes to `Main.generateSite`, as from either other command, which
runs the venue, markup and name checks and writes the site and the JSON exports.

All of this is `json.Music.createFromShows`. Using the JSON reader means `shows-site` and
`json-site` share one reader and one set of checks, and `shows-site` adds only the join. The iTunes
parse is `raw.Media.createMedia`, unchanged but for `fSortnames`. It still fills `raw.Artist`'s name
map, which nothing reads in a `shows-site` run.

## Checks

site's `ArchiveCheck` (its rule 10) checks the file before the app writes it. web_generator makes
the same checks, so a file written by hand or by another program is caught too. `json.Check`
already made most of them ([RFC 002](002-inputs.md#checks)). This RFC added:

| Check | `json-site` | `shows-site` |
|---|---|---|
| Each ID starts with its kind's prefix: `v`, `ar`, `r`, `sh`, and `a` and `s` for albums and songs | stops | stops |
| A show's date is possible: month 1–12, and a day that exists in its month, in a leap year when the year is unknown | stops | stops |
| Two venues have the same name | stops | stops |
| `songs` or `albums` is not empty | n/a | stops |
| An iTunes artist name isn't a shows file artist | n/a | stops |

- The first three are cheap and true of every `music.json` web_generator writes, so they apply to
  `json-site` as well, and are in RFC 002's Checks table. Album and song IDs are checked when the
  arrays are present.
- Site's rule 6 names `Location.zip`, `Venue.comment`, `Artist.location` and `Artist.comment`,
  which the raw files never set and the app doesn't model. A check for them was here until
  web_generator removed them too; now neither program has them, and the keys are ignored.
- As for `json-site`, every problem is listed at once, each naming the array, the index and the ID:

  ```
  shows.json has 3 problem(s):
    relations[0] rel0 isn't r and a number.
    shows[1] sh3 has an impossible day: 29
    songs isn't empty; iTunes gives them.
  ```

## Verifying

Site RFC 018 rule 14.1: making the site from the raw files and from the `json/shows.json` they export
must give the same HTML.

1. Run `site` on the raw files and the iTunes export, with `JAVA_OPTS=-Dweb.debug_output=true`.
2. Copy its `json/shows.json` into a copy of the data folder.
3. Run `shows-site` on that folder and the same iTunes export.
4. `diff -r` the two trees, leaving out what [RFC 004](004-verifying-output.md) lists as changing
   from run to run.

Only the HTML must match. The other files, including the JSON exports in `json/`, may differ in key
order, `timestamp`, and the order of artists and venues.

On the real data (915 shows, 1,794 artists, 15,885 songs) the whole tree matches, JSON exports and
the run's warnings included. Only `diary.json`'s `timestamp` differs: the raw diary is read anew,
while music's timestamp comes from the shows file. Also checked when this landed:

- Removing two artists that only iTunes knows from the shows file stopped the run naming both.
- A shows file with one of each new problem in the Checks table stopped the run listing all nine.
- With every `sortname` removed from the shows file, iTunes gave back 382 of the 520.

## After this

Once site writes the shows file, `shows.txt`, `venuemap.txt`, `bandsort.txt`, `artistids.txt` and
`relations.txt` are no longer edited, and web_generator is run with `shows-site` (site RFC 018 rule
14.3). `site` stays, to rebuild old data.

## Out of scope

- The diary stays in `comments.txt` and `statics.txt`. Moving it to JSON, where entries keep their
  IDs, is separate (#117, #188).
- Played music stays with the iTunes export (site RFC 018).
