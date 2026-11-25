# RFC 003: Generating the site

Status: Describes existing behavior · Updated: 2026-11-24 · Code: `src/com/bolsinga/site/Main.java`,
`src/com/bolsinga/web`, `src/com/bolsinga/music`, `src/com/bolsinga/diary`, `src/com/bolsinga/rss`,
`src/com/bolsinga/ical`

## Summary

`Main.generateSite` takes a `Music` and a `Diary` ([RFC 001](001-data-model.md)), however they
were read ([RFC 002](002-inputs.md)), and writes a complete static site: HTML 4.01 pages built with
ECS, CSS, an RSS feed, an iCalendar file and the JSON exports. The site is then copied to
bolsinga.com with rsync, which is outside this repo.

## Stages

```
Music + Diary
   │
   ├─ CSS.install                         styles/
   ├─ checks: similar artist names (printed), missing artist IDs (stop)
   │
   ├─ singletons, made on first use: Settings, Lookup, Compare, Links
   │
   ├─ document creators queue one task per page group on the Backgrounder pool
   │    Diary: MainDocumentCreator, EntryRecordDocumentCreator, AltDocumentCreator
   │    Music: Artist-, Venue-, Show-, City-, TracksRecordDocumentCreator
   │
   ├─ on the main thread, while the pool works:
   │    ICal.generate        alt/<icalName>.ics
   │    RSS.generate         alt/<rssFile>
   │    JSON export          json/<musicJsonFile>.json, <showsJsonFile>.json, <diaryJsonFile>.json
   │
   └─ Main.complete: the pool shuts down, Main waits for every task, and the run fails
                     if any task failed
```

## Derived data

`Lookup` is built once from `Music`:

- HTML-escaped names for artists and venues.
- Shows per artist, per venue, and per city. The city key is the `cityformat` resource
  (`{0}, {1}`, city and state), so it needs every show's venue to have a location.
- Shows per state.
- For each artist or venue in a relation, the set of related ones, sorted.
- Counts used by stats and the home page.

`Compare` holds the orderings:

| Comparator | Order |
|---|---|
| `LIBRARY_COMPARATOR` | `simplify(sortname ?: name)`, ignoring case ([RFC 001](001-data-model.md)) |
| `ARTIST_COMPARATOR`, `VENUE_COMPARATOR` | library order |
| `DATE_COMPARATOR` | `year*10000 + month*100 + day`, unknown first |
| `SHOW_COMPARATOR` | date, then venue, then headliner |
| `*_STATS_COMPARATOR` | count descending, then library order |

## Site layout (`Links`)

| Path | Content |
|---|---|
| `index.html` | home: recent shows and diary entries, counts, last played song |
| `archives/<yyyy>.html` | diary entries by year |
| `archives/<eN>.html` | redirect to an entry |
| `archives/overview.html` | diary stats |
| `bands/<L>.html` | artists filed under letter `L` |
| `bands/<arN>.html` | redirect to an artist |
| `bands/stats.html` | shows per artist |
| `venues/<L>.html`, `venues/<vN>.html`, `venues/stats.html` | the same for venues |
| `dates/<yyyy>.html` | shows in a year, with month anchors (`#January`, `#Unknown`) |
| `dates/other.html` | shows with an unknown year |
| `dates/<shN>.html`, `dates/stats.html` | redirect to a show; shows per year |
| `cities/stats.html` | shows per city |
| `tracks/<L>.html`, `tracks/stats.html`, `tracks/albumstats.html` | albums and songs, from iTunes |
| `alt/index.html` | colophon and links |
| `alt/<icalName>.ics`, `alt/<rssFile>` | calendar and feed |
| `styles/<cssFile>` | CSS |
| `json/*.json` | exports ([RFC 002](002-inputs.md)) |

- **Letter pages.** The letter is the first character of `Compare.simplify(sortname ?: name)`,
  uppercased. A non-word character files under `other`.
- **Redirect pages.** Every artist, venue, show and diary entry has its own
  `<dir>/<id>.html` with `og:` metadata and a `meta refresh` to `<page>#<id>`. Every internal link
  points at the redirect page, so the page an item is filed on can change without breaking links.
- **Anchors** inside a page are the item's ID.
- `@@ROOT_URL@@` in text becomes a relative path to the site root (`Encode`).

Every page has the same header: icon, CSS, RSS link, `og:` tags, a navigator of nine sections,
and a DuckDuckGo search form fixed to `bolsinga.com` (`Util`). The page title is built from the
`sitepagetitle` resource and the domain in the `root` setting.

## What each page uses

This is the field list `json-site` must supply for the site to be unchanged.

| Page | Uses |
|---|---|
| show (`dates/`) | show `id`, `date`, `venue`, `artists` in order, `comment` (its first sentences become `og:description`) |
| venue (`venues/`) | venue `id`, `name`, `sortname`, `location.{street, city, state, web}`, `mapKitID` (Apple Maps link), related venues, the venue's shows |
| artist (`bands/`) | artist `id`, `name`, `sortname`, related artists, the artist's shows, and **albums** |
| cities | venue `location.city` and `location.state` for every show |
| tracks | albums and songs only |
| home | recent shows and entries, counts of shows, artists, venues, songs and albums, and the **last played song** |
| RSS | recent shows and entries |
| iCal | every show with a full date, as an all-day event on that date that repeats yearly, so the calendar shows each show's anniversaries; shows with any unknown date part are left out |
| diary | `Diary` title, statics, header, friends, colophon; entry `id`, `timestamp`, `title`, `comment` |

Pages that change without iTunes data: artist pages (no album list, and a different redirect
title), all of `tracks/`, and the home page's counts and last played song.

Artist pages list shows in `Music.getShows()` order. Both readers sort shows with
`SHOW_COMPARATOR`, and the pages depend on that. Venue pages sort for themselves.

## Settings

`settings.properties` is not in this repo; it lives with the data. Its keys are read by
`SettingsProperties`: `contact`, `ico`, `webClipIcon`, `largeImage`,
`logoImage.{location,width,height,alt}`, `cssFile`, `rssFile`, `diaryCount`, `recentCount`, `root`,
`rssDescription`, `icalName`, `linksTitle`, `friendsTitle`, `showTime`, `copyrightStartYear`,
`diaryEntryTime`, `diaryJsonFile`, `musicJsonFile`, `showsJsonFile`.

The site's own words (titles, formats, labels) are in `src/properties/config.properties`, which the
build copies to `web.properties` with the build ID filled in.

## Threading

`Backgrounder` is a fixed pool with one thread per processor, or `-Dweb.poolthreadcount=N`. Each
task writes one page group, and its redirect pages as a side effect.

- Anything a task throws, `Error`s included, is printed and counted, and the other tasks go on.
  When they have all finished, `Main.complete` fails the run if any failed. A page that can't be
  written throws from `RecordDocumentCreator`, so it counts as a failure.
- Anything thrown on the main thread, `Error`s included, ends the run with `System.exit(1)`, which
  also stops the pool's threads.
- **A run that exits 0 wrote every page.** Exit 1 means something failed; don't copy that site up.
- `Settings`, `Lookup`, `Compare` and `Links` are static, so a JVM can build one site.
- `json.Artist` builds its album list lazily without a lock, from several threads.

## System properties

| Property | Effect |
|---|---|
| `web.debug_output=true` | leaves out the date, generator and copyright year details, so output can be compared ([RFC 004](004-verifying-output.md)) |
| `web.poolthreadcount=N` | size of the thread pool |
