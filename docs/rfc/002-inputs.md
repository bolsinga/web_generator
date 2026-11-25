# RFC 002: Inputs: raw files and JSON

Status: Describes existing behavior · Updated: 2026-11-24 · Code: `src/com/bolsinga/music/data/raw`,
`src/com/bolsinga/music/data/json`, `src/com/bolsinga/diary/data/{raw,json}`,
`src/com/bolsinga/itunes`, `src/com/bolsinga/site/Main.java`, `src/scripts/site`

## Summary

There are three ways to build the `Music` and `Diary` of [RFC 001](001-data-model.md):

- **`site`** reads the raw text files plus an iTunes JSON export. This is how the site is made today.
- **`json-site`** reads a `music.json` and a `diary.json`, as this program exports them.
- **`shows-site`** reads `bolsinga/site`'s shows file plus an iTunes JSON export and the raw diary
  files ([RFC 005](005-shows-site.md)). It replaces `site` once the app edits the shows file.

Every run of any command also writes `json/music.json`, `json/shows.json` and `json/diary.json`
into the site, so the JSON is always current with the raw files. The names are the
`musicJsonFile`, `showsJsonFile` and `diaryJsonFile` settings plus `.json`; this RFC calls them by
their usual names. `bolsinga/site` reads
`json/shows.json` from the website, which makes that format a public contract.

## Commands

`src/scripts/site` (installed as `output/dst/site`) wraps `com.bolsinga.site.Main`:

```
site site       <output_dir> <data_dir> <itunes.json>
site json-site  <output_dir> <settings.properties> <json_dir>
site shows-site <output_dir> <data_dir> <itunes.json>
```

| Command | Reads | Builds with |
|---|---|---|
| `site` | `data_dir/` `settings.properties`, `shows.txt`, `venuemap.txt`, `bandsort.txt`, `relations.txt`, `comments.txt`, `statics.txt`, `artistids.txt`, and the iTunes file | `raw.Music.create`, `raw.Diary.create` |
| `json-site` | `settings.properties`, and in `json_dir` the diary and music files named by its `diaryJsonFile` and `musicJsonFile`, as the export writes them | `json.Music.create`, `json.Diary.create` |
| `shows-site` | `data_dir/` `settings.properties`, the shows file named by its `showsJsonFile` (`shows.json`), `comments.txt`, `statics.txt`, and the iTunes file | `json.Music.createFromShows`, `raw.Diary.create` |

The script runs Java with `-ea:com.bolsinga...`, so asserts are on. Set `DUMP=1` to print the
command instead of running it. `Main` checks the command and its number of arguments before
reading anything; when they're wrong, `Main.usage` prints the problem and every form of the
command to stderr and exits 1.

The iTunes file is required by `site` and `shows-site`; there is no way to build without it.
`json-site` reads one music file, so iTunes data and shows must be in the same file.

## Raw files

All raw files are UTF-8, one record per line, split with `StringTokenizer`. `StringTokenizer`
treats repeated delimiters as one, so **a field can't be empty**, and a delimiter inside a value
splits it. Records refer to each other by **name**, not ID.

### shows.txt

```
shN^M-D-YYYY^Headliner|Opener|Opener^Venue Name[^comment]
```

- Reading stops at the first empty line.
- In the date, a month or day of `0`, or a year of `1900`, means unknown.
- Each artist name creates the artist the first time it is seen.
- The venue name must match a venue in `venuemap.txt`, or the run stops.
- Duplicate show IDs stop the run.

### venuemap.txt

```
vN*Name*City*State[*street][*url][*sortname][*mapkit=ID]
```

- street, url and sortname are positional, so a url needs a street, and a sortname needs both.
- `mapkit=ID` is tagged, may come anywhere after the state, and becomes `mapKitID`.
- Two venues with the same name stop the run, naming both IDs. Shows and relations name venues,
  so only one of them could be reached.
- Duplicate venue IDs stop the run.
- `bolsinga/site`'s Venue Places tool writes this file.

### bandsort.txt

```
Artist Name*Sort Name
```

Applied after iTunes, so it overrides iTunes sort names. An unknown artist stops the run.

### artistids.txt

```
arN^Artist Name
```

An unknown artist stops the run. An artist with no line stops the run after printing suggested
lines ([RFC 001](001-data-model.md)).

### relations.txt

```
band|reason|Artist A|Artist B...
venue|reason|Venue A|Venue B...
```

- A reason of `^` means none.
- Relation IDs are `r` plus the line index.
- An unknown artist or venue stops the run. Every unknown member is listed at once.
- Any other type stops the run.

### comments.txt and statics.txt (diary)

Pseudo-XML, matched with regular expressions, not parsed:

```
<comment><date>M-D-YYYY</date><title>optional</title><data>text</data></comment>
<static><location>left|header|links|title|colophon</location><data>text</data></static>
```

Entries are written newest first. An entry dated after the one above it stops the run, naming both
dates; entries on the same day are allowed. Entries are sorted by date and numbered `e0`, `e1`, … in
that order, oldest first, so the file's order is the numbering's order reversed. Entries on the same
day keep their order in the file.

### iTunes

`itunes.Parser` reads an iTunes library export in JSON (`itunes.JSONParser`). It becomes albums
and songs, and creates any artist not already seen at a show. iTunes sort names are applied to
artists. Album and song titles are kept as iTunes gives them, as plain text, so `music.json` has
`"UFO>UFO>UFO"`. Exports written before #218 have `&lt;` and `&gt;` in a few song titles instead;
they give the same pages.

### Order of assembly

`raw.Music.create`:

1. venues (`venuemap.txt`)
2. shows (`shows.txt`), creating artists
3. iTunes, creating more artists and setting sort names
4. `bandsort.txt` sort names
5. `artistids.txt` IDs
6. relations (`relations.txt`), which need every artist

Venues are sorted by `VENUE_COMPARATOR`, artists by `ARTIST_COMPARATOR`, shows by `SHOW_COMPARATOR`.

## JSON

Written by `json.Music.export` / `json.Diary.export` with `org.json`, indented 2 spaces, keys in no
particular order. A key marked `?` is left out when the value is null, 0 or empty.

### music.json

```
{
  "timestamp": "2026-11-24T10:00:00-08:00",
  "venues": [
    { "id": "v1", "name": "…",
      "location": { "street"?: "…", "city": "…", "state": "…", "zip"?: 0, "web"?: "https://…" },
      "comment"?: "…", "sortname"?: "…", "mapkit"?: "I…" }
  ],
  "artists": [
    { "id": "ar1", "name": "…", "sortname"?: "…", "location"?: {…}, "comment"?: "…",
      "albums"?: ["a1", …] }
  ],
  "relations": [ { "id": "r0", "reason"?: "…", "members": ["ar1", "ar2"] } ],
  "songs": [ … ],
  "albums": [ … ],
  "shows": [
    { "id": "sh1", "date": { "year"?: 1998, "month"?: 9, "day"?: 6 },
      "venue": "v1", "artists": ["ar1", "ar2"], "comment"?: "…" }
  ]
}
```

- **References** are ID strings: `show.venue`, `show.artists`, `relation.members`,
  `artist.albums`, and the album and song references.
- **Unknown date parts** are left out. A wholly unknown date is `{}`. Never write `0` or `1900`.
- **Order:** shows are written sorted by `SHOW_COMPARATOR` and sorted again on read. Venues and
  artists are written in input order and not sorted on read; the pages sort copies.
- **Required on read:** `timestamp` (parsed by `ZonedDateTime.parse`), and all six arrays, though
  they may be empty. Each venue, artist and show needs `id` and `name` (or `date` and `venue`).
- **Read order:** venues, then artists, relations, songs, albums, shows, each resolving references
  to what came before.
- **Ignored on read:** `artist.albums`. Albums attach themselves to their performers instead.
- **Album keys:** `id`, `title`, `performer`, `compilation`, `release`, `purchase`, `comment`,
  `songs`. **Song keys:** `id`, `artist`, `title`, `release`, `lastPlayed`, `track`, `genre`,
  `playCount`, `digitized`, `live`.

### shows.json

The same as `music.json`, except `songs` and `albums` are `[]`. Every artist is still written,
including artists known only from iTunes, along with their `albums` IDs, which now point nowhere.
`bolsinga/site` drops artists with no shows after it reads the file.

### diary.json

```
{ "timestamp": "…", "title": "…",
  "statics": ["…"], "header": ["…"], "friends": ["…"], "colophon": ["…"],
  "entries": [ { "id": "e0", "timestamp": "…", "title"?: "…", "comment": "…" } ] }
```

Every key is required except an entry's `title`.

## Checks

| Problem | Raw | JSON |
|---|---|---|
| Duplicate show, venue or artist ID | show or venue: stops | stops |
| An ID that isn't its kind's prefix and a number (`v`, `ar`, `r`, `sh`, `a`, `s`) | can't happen | stops |
| A show date that can't be: month not 1–12, or a day its month doesn't have | not checked | stops |
| Show names an unknown venue | stops | stops |
| Show names an unknown artist | n/a, created | stops |
| Show with no artists, or no `artists` key | can't happen | stops |
| Unknown relation member | stops | stops |
| Two venues with the same name | stops | stops |
| Mixed artist and venue relation | can't happen | stops |
| Venue without a location, city or state | can't happen | stops |
| Venue that no show names | stops | stops |
| Artist with no ID | stops, prints suggestions | stops |
| Two artists with the same name | can't happen, artists are joined by name | stops |
| Diary entry dated after the entry above it | stops | n/a, entries have IDs |

The JSON reader checks `music.json` (`json.Check`) before it builds anything, and stops with a list
of every problem it found, each naming the array, the index and the ID:

```
music.json has 2 problem(s):
  shows[12] sh12 names an unknown venue: v999
  relations[3] r3 mixes artists and venues.
```

`shows-site` makes the same checks on the shows file, and those of
[RFC 005](005-shows-site.md#checks) that only a shows file needs.

A venue that no show names is checked after any reader, by `Main.generateSite`, because its page
would list its shows. The run stops before writing anything and names every such venue. Its `vN`
ID stays retired; don't give it to another venue.

### Markup warnings

Show comments, diary entries and their titles, and the diary's statics, header, friends and
colophon are HTML. After any reader, `MarkupCheck` prints warnings under `--Markup Warnings--`
for markup that pages would show wrongly. Each warning names the show or entry ID. They don't stop
the run.

- A tag that isn't in HTML 4.01 Strict. Text like `Set<String>` is read as a tag and disappears;
  write `Set&lt;String&gt;`. A Transitional-only tag such as `<strike>` gets the Strict
  replacement, such as `<del>`.
- An `href` or `src` that starts or ends with whitespace, such as `href=" https://…"`.

### Name warnings

Artists are joined by name, so a name that looks right but is spelled differently underneath won't
match the same name typed elsewhere. After any reader, `NameCheck` prints warnings under
`--Name Warnings--` for every artist name and album and song title that has spaces at either end or
two in a row, or accents stored decomposed rather than precomposed (not NFC). Album and song
warnings name the artist, to find the track in the Music app. Sort names aren't checked, because
the raw reader makes them decomposed on purpose ([RFC 001](001-data-model.md)). The fix belongs in
the source: the Music app, or the raw files. They don't stop the run.

## Gaps for another writer of the JSON

These are what any program other than web_generator must get right to write `music.json` that
`json-site` turns into the same site:

1. Keep the iTunes `songs` and `albums`, and the artists that only iTunes knows. Or write a
   shows file, with `songs` and `albums` empty, and run `shows-site` with the iTunes export
   ([RFC 005](005-shows-site.md)).
2. Never reuse or renumber show, venue or artist IDs. They are URLs.
3. Resolve every reference, give every show at least one artist, and give every venue a location
   with city and state and at least one show.
4. Write `sortname` where it's wanted. JSON input does not derive one.
5. Keep relations to one kind of member.
6. Give every artist and every venue its own name. The raw files and iTunes join them by name.
7. Give IDs their kind's prefix and a number, and give shows dates that can be.

`json-site` stops on a file that breaks 2's duplicate IDs, 3, 5, 6 or 7 ([Checks](#checks)). It can't
tell whether an ID was renumbered, a sortname is missing, or iTunes data was dropped.
