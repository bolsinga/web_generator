# RFC 001: The data model

Status: Describes existing behavior · Updated: 2026-11-24 · Code: `src/com/bolsinga/music/data`,
`src/com/bolsinga/diary/data`

## Summary

Everything web_generator makes comes from two objects: a `Music` and a `Diary`. Each is a set of
Java interfaces with two implementations, `raw` (the original text files, [RFC 002](002-inputs.md))
and `json` (the JSON this program exports, also [RFC 002](002-inputs.md)). The HTML code only
sees the interfaces, so either input produces the same site ([RFC 003](003-generation.md)). The
interfaces are the contract; the file formats are two ways to fill them.

## Music

```
Music ─┬─ venues:    Venue ── Location
       ├─ artists:   Artist ── albums (from iTunes)
       ├─ relations: Relation ── members (all Artists, or all Venues)
       ├─ songs:     Song   ┐ iTunes only
       ├─ albums:    Album  ┘
       ├─ shows:     Show ── Date, Venue, Artists (headliner first)
       └─ timestamp
```

Each list has an unmodifiable getter (`getShows()`) and a copy (`getShowsCopy()`) that callers sort.

| Type | Fields | Notes |
|---|---|---|
| `Music` | `timestamp` (`ZonedDateTime`), `venues`, `artists`, `relations`, `songs`, `albums`, `shows` | Raw input sets `timestamp` to now. The HTML does not use it. |
| `Show` | `id`, `date`, `venue`, `artists`, `comment?` | Read-only. `artists` is ordered headliner first, then openers, and must not be empty. |
| `Venue` | `id`, `name`, `location`, `comment?`, `sortname?`, `mapKitID?` | `location` is effectively required: city and state group the cities pages. The venue's website is `location.web`. |
| `Artist` | `id`, `name`, `sortname?`, `location?`, `comment?`, `albums` | `albums` comes from iTunes. Nothing sets `location` or `comment` today. |
| `Location` | `street?`, `city`, `state`, `zip`, `web?` | `zip` is an `int`, 0 when unknown. Nothing sets it today. |
| `Date` | `year`, `month`, `day` | See below. |
| `Relation` | `id`, `reason?`, `members` | `members` is `List<Object>` holding `Artist` or `Venue` objects. |
| `Album`, `Song` | see `Album.java`, `Song.java` | Only from iTunes. Titles are plain text, not HTML. Out of scope here except where pages depend on them ([RFC 003](003-generation.md)). |

`?` means the value may be null. `comment` on `Venue` exists in the interface and the JSON, but
raw input never sets it.

### Dates

`Date.UNKNOWN` is 0. Any of year, month (1-based) or day may be unknown. `isUnknown()` is true if
*any* part is unknown. Shows sort by the number `year*10000 + month*100 + day`
(`Compare.DATE_COMPARATOR`), so unknown parts sort before known ones. Shows with an unknown year
go on `dates/other.html`.

### Relations

A relation says that artists, or venues, belong together, with an optional reason. `Lookup`
casts every member to the type of the first member, so **a relation must hold only artists or only
venues**. The raw files can't mix them, and the JSON reader stops on a relation that does
([RFC 002](002-inputs.md)).

### Sort names

`sortname` is optional on artists and venues. Where it is null, `name` is used. Sorting and the
letter a page is filed under both use `Compare.simplify`, which lowercases, drops one leading
"the ", "a " or "an ", and drops leading non-word characters. "The" is never stored in the
sort name.

Raw input also derives artist sort names: when a name contains accented letters, Unicode canonical
decomposition splits each one into its base letter followed by a separate accent mark, and that
decomposed name becomes the sortname, even without a bandsort entry (`raw/Artist.getSortName`).
The accent marks are kept, not removed. JSON input takes `sortname` exactly as written.

## Identity

IDs are opaque strings, but they become file names and anchors in the site, so **an ID that has
been published is a permanent URL**.

| Thing | ID form | Assigned by | Stable? | URL |
|---|---|---|---|---|
| Show | `shN` | hand, first field of `shows.txt` | yes | `dates/<id>.html` |
| Venue | `vN` | hand, first field of `venuemap.txt` | yes | `venues/<id>.html` |
| Artist | `arN` | hand, `artistids.txt` | yes | `bands/<id>.html` |
| Relation | `rN` | line number in `relations.txt` | no | none |
| Album, Song | `aN`, `sN` | position while parsing iTunes | no | anchors on `tracks/` pages |
| Diary entry | `eN` | position in date order | no, inserting an older entry renumbers later ones | `archives/<id>.html` |

When an artist has no ID, `Main.dumpMissingArtistIDs` prints suggested `arN^Name` lines, numbering
from the count of artists that have IDs, and stops the run. No ID format is checked anywhere.

In JSON, relation members are looked up as artists first and then venues, so artist and venue IDs
must never be equal. The prefixes keep them apart.

## Diary

| Type | Fields |
|---|---|
| `Diary` | `timestamp`, `title`, `static`, `header`, `friends`, `colophon` (each a `List<String>` of HTML fragments), `entries` |
| `Entry` | `id`, `timestamp` (`ZonedDateTime`), `title?`, `comment` |

Text may contain `@@ROOT_URL@@`, which is replaced with a relative path to the site root when
pages are written.

## Gaps

- The `json` classes have copy constructors (`json.Music(Music)`, `Venue.createOrGet`, and
  others) that would convert any `Music` to the JSON classes. Nothing calls them. The `Venue` one
  does not copy `sortname`.
- Every `json` class keeps a static map from ID to object, so a JVM can load only one `Music`.
- `Lookup`, `Compare`, `Links` and the settings are static singletons too ([RFC 003](003-generation.md)).
