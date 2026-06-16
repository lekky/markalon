# Markalon

A fast, **local-first** markdown editor for Android. Write notes in markdown, view them
in three modes (Raw / Mixed / Rendered), and have everything auto-saved to local storage —
no account, no cloud.

Native Android implementation of the Markalon design handoff, built with **Kotlin +
Jetpack Compose + Material 3**.

## Features

- **Library** — browse, search, and open notes; create new ones; import & settings access.
- **Editor** — three view modes (Raw, Mixed, Rendered), a customizable quick-insert
  toolbar (16 tools), inline title editing, live word count, and a debounced auto-save
  indicator (Saving… → Saved).
- **Markdown** — headings, bold/italic/strikethrough, inline & fenced code, links, images,
  bullet/ordered/**task** lists, blockquotes, GFM tables, and horizontal rules.
- **Settings** — light/dark/system theme, 8 accent colors, 4 editor fonts, configurable
  default view, and a reorderable/toggleable insert toolbar. All persisted.
- **Import / Export** — open `.md`/`.markdown`/`.txt` from device storage; export the
  current note as a `.md` file via the system file picker.

## Tech

| Concern | Choice |
| --- | --- |
| UI | Jetpack Compose, Material 3 |
| Markdown parsing | [commonmark-java](https://github.com/commonmark/commonmark-java) + GFM tables, strikethrough, task-list extensions |
| Markdown rendering | Native Compose AST renderer (`markdown/MarkdownView.kt`) — exact control over the design's type scale |
| Persistence | Jetpack DataStore (Preferences) + kotlinx.serialization (JSON) |
| State | `MarkalonViewModel` (`StateFlow`), state-driven navigation |
| Min / target SDK | 24 / 35 |

## Architecture

```
com.markalon.app
├─ MainActivity            // hosts MarkalonApp
├─ MarkalonApp             // theme + state-driven nav + file import/export + toast
├─ MarkalonViewModel       // UI state, undo/redo history, debounced save, search
├─ data/                   // Note, Settings, Tool, Accents, MarkalonRepository (DataStore)
├─ markdown/MarkdownView   // commonmark AST → Compose
├─ editor/                 // EditorScreen, insert toolbar, insert logic
├─ library/LibraryScreen
├─ settings/SettingsSheet  // modal bottom sheet
└─ ui/                     // theme tokens, fonts, shared components
```

## Building

Requires the Android SDK and JDK 11+.

```bash
./gradlew assembleDebug      # build the debug APK
./gradlew installDebug       # build & install on a connected device/emulator
```

Open the project in Android Studio (Ladybug or newer) and run the `app` configuration.

## Notes / fonts

The design specifies brand fonts (Spline Sans, JetBrains Mono, IBM Plex Sans, Newsreader).
This implementation uses the platform's generic font families (sans / monospace / serif)
as native substitutes — the handoff permits this. To match the brand exactly, drop the
font files into `app/src/main/res/font/` (or wire up Google downloadable fonts) and point
`ui/theme/Type.kt` at them.
