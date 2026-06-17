# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this is

**Markalon** — a fast, local-first markdown editor for Android. Native app built with
**Kotlin + Jetpack Compose + Material 3**. It was implemented from a high-fidelity design
handoff (markdown spec + screenshots); the original handoff is **not** in the repo.

Three screens: **Library** (browse/search/create notes), **Editor** (Raw / Mixed /
Rendered views + a 16-tool insert toolbar), and **Settings** (modal bottom sheet:
theme, accent, font, default view, toolbar config). Everything persists locally; no
account, no network, no cloud.

## Build & run

Requires the **Android SDK** + JDK 17. The Gradle wrapper is committed.

There are two product flavors — **`prod`** (`com.markalon.app`, "Markalon") and **`qa`**
(`com.markalon.app.qa`, "Markalon QA", versionName `…-qa`) — so QA and prod install side by
side with isolated local data. Variant tasks are `{prod,qa}{Debug,Release}`; `BuildConfig.IS_QA`
distinguishes them at runtime.

```bash
./gradlew assembleDebug          # build BOTH flavors' debug APKs (the compile check)
./gradlew assembleProdRelease    # prod release -> app/build/outputs/apk/prod/release/
./gradlew assembleQaRelease      # qa release   -> app/build/outputs/apk/qa/release/
./gradlew installProdDebug       # build + install prod on a connected device/emulator
./gradlew installQaDebug         # build + install qa (alongside prod)
./gradlew lint                   # Android lint
./gradlew test                   # unit tests (none of substance yet)
```

There are no unit/instrumentation tests of substance yet — **CI is the compile check**
(`.github/workflows/android.yml`). It builds both flavors' debug APKs on PRs to `main` and,
on push to `main`, publishes the `prod` **and** `qa` release APKs as a GitHub Release
(tag `build-<run#>`).

### Environment caveat (important)
This project is often edited from **Claude Code on the web**, where the container has
**no Android SDK and no network access for Gradle** — so you cannot compile locally there.
Do a careful static review, then **rely on the CI build (open/push to a PR) as the real
compiler check.** Do not assume a change compiles just because it looks right.

## Architecture

State-driven navigation (no nav library). Single `MarkalonViewModel` holds all UI state as
a `StateFlow<MarkalonUiState>`; `MarkalonApp` renders the current screen and routes events.

```
com.markalon.app
├─ MainActivity            // enableEdgeToEdge + setContent { MarkalonApp() }
├─ MarkalonApp             // theme, Library<->Editor switch, Settings sheet,
│                          //   file import/export launchers, toast
├─ MarkalonViewModel       // UiState, per-note undo/redo, debounced save (550ms), search
├─ data/
│  ├─ Note                 // { id, title, body, created, updated }  @Serializable
│  ├─ Settings             // theme/font/defaultView/accent/toolbar  @Serializable
│  ├─ Tool / ToolItem      // the 16 toolbar tools + defaultToolbar
│  └─ MarkalonRepository   // DataStore(Preferences) + kotlinx.serialization JSON; seeds notes
├─ markdown/MarkdownView   // commonmark-java AST -> native Compose (THE custom renderer)
├─ editor/
│  ├─ EditorScreen         // top bar, view switcher, Raw/Mixed/Rendered, overflow menu
│  ├─ Toolbar              // ToolIcon (glyphs + Material icons) + InsertToolbar
│  └─ MarkdownInsert       // pure fn: applyInsert(tool, text, sel) -> new text + selection
├─ library/LibraryScreen
├─ settings/SettingsSheet  // ModalBottomSheet with all settings controls
├─ ui/
│  ├─ theme/Tokens         // MarkalonColors (light/dark) + LocalMarkalonColors/LocalAccent
│  ├─ theme/Theme          // MarkalonTheme(settings) — applies tokens + M3 ColorScheme
│  ├─ theme/Type           // fonts (generic family substitutes — see below)
│  └─ components/Components // SegmentedControl, MarkalonLogo
└─ util/NoteFormat         // wordCount, snippet, relativeTime
```

### Key design decisions
- **Theming is custom, not Material defaults.** Colors come from `LocalMarkalonColors`
  (a `MarkalonColors` data class with `bg/surface/surface2/border/fg1/fg2/fg3/codeBg`) and
  `LocalAccent`. A matching M3 `ColorScheme` is provided so standard components inherit it,
  but **read colors from the composition locals**, not `MaterialTheme.colorScheme`. All hex
  values are authoritative from the handoff — don't "round" them.
- **Markdown is parsed with `commonmark-java`** (+ GFM tables, strikethrough, task-list
  extensions) and rendered **natively in Compose** by walking the AST in
  `markdown/MarkdownView.kt`. This was chosen over Markwon so we control the exact type
  scale (h1 25sp, body 15.5sp/lh 1.65, etc.). Do **not** hand-roll a parser. When adding
  markdown features, extend the AST visitor; avoid regex.
- **Editor text uses local `TextFieldValue`** state keyed on `note.id`; edits call
  `vm.updateBody`, undo/redo replace the field explicitly. This avoids the controlled-
  String cursor-jump bug — keep title and body as `TextFieldValue`, not `String`.
- **Persistence:** DataStore single instance, JSON-encoded. The VM owns state after the
  initial `repo.notes.first()` load and writes back; don't continuously collect the repo
  flow into the VM (it would fight its own writes).

### Gotchas / things that bit us
- `IntrinsicSize` lives in **`androidx.compose.foundation.layout`**, not
  `androidx.compose.ui.layout`. (This was the one CI compile failure.)
- `enableEdgeToEdge()` sets `decorFitsSystemWindows=false`, which makes the manifest's
  `adjustResize` a **no-op**. The keyboard is handled with **`Modifier.imePadding()`** on
  the screen container in `MarkalonApp`. Keep it.
- Don't wrap a height-bounded `BasicTextField` in `verticalScroll` — it scrolls internally
  and the wrapper passes an infinite height constraint that fights the cursor.
- Material icons: prefer the plain `Icons.Filled.*` we already use. Some are deprecated in
  favor of `Icons.AutoMirrored.Filled.*`, but not every icon exists in that namespace —
  switching can break the build. Verify via CI if you change them.

## Conventions
- Read tokens from `LocalMarkalonColors.current` / `LocalAccent.current`; UI text uses
  `UiFontFamily`, editor/preview uses `settings.editorFont.toFontFamily()`, code is always
  `CodeFontFamily` (monospace).
- New persisted fields go on the `@Serializable` data classes with defaults (the JSON reader
  uses `ignoreUnknownKeys`, so additive changes are safe).
- The 16 toolbar tools and their inserted markdown are defined in `data/Tool.kt` +
  `editor/MarkdownInsert.kt`; the insert strings are ported verbatim from the prototype —
  match that behavior if you touch them.
- Fonts: the design calls for brand fonts (Spline Sans, JetBrains Mono, IBM Plex Sans,
  Newsreader); we currently substitute the platform's sans/monospace/serif families (the
  handoff permits this). To use the real fonts, add them under `app/src/main/res/font/`
  (or wire Google downloadable fonts) and point `ui/theme/Type.kt` at them — call sites
  won't change.

## Git / CI
- Default branch is `main`. CI builds both flavors on PRs and releases the `prod` + `qa`
  release APKs on merge to `main`.
- **Signing:** release builds use a real keystore when `MARKALON_KEYSTORE` (path) +
  `MARKALON_KEYSTORE_PASSWORD` / `MARKALON_KEY_ALIAS` / `MARKALON_KEY_PASSWORD` are present
  in the environment; otherwise they fall back to the **debug key** (installable, not
  Play-Store-ready). To ship a real prod build, generate a keystore and provide those four
  values to the workflow (e.g. decode a base64 repo secret to a file and export the path).
