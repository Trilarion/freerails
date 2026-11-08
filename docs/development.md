# Development

This guide describes the architecture and working conventions for developing FreeRails.
The implementation and tests are the source of truth when older design notes differ from
the current behavior.

This document is the authoritative source for FreeRails architecture, design
principles, development and code style, repository layout, and build guidance.
`AGENTS.md` points here rather than duplicating that guidance.

## Repository layout

- `src/main/java/freerails/` — Java application code, organized into packages for the
  client UI, game model, controllers, moves, networking, scenarios, server, and shared
  utilities.
- `src/main/resources/` — game graphics, sounds, interface content, and scenario data.
- `src/test/java/` and `src/test/resources/` — automated tests and their supporting data.
- `docs/` — Sphinx documentation, including the player manual, development notes, and specifications.
- `build.gradle`, `settings.gradle`, and `gradle/` — Gradle build configuration and the Gradle wrapper.
- `docs/history.md` — project history.

## Architecture and design principles

FreeRails separates game state and rules from interaction, presentation, and
infrastructure:

- `freerails.model` contains the game domain and world state. It should remain
independent of the client, controller, server, and move layers. The shared
`freerails.model.GameModel` update contract is used by clients and servers.
- `freerails.move` represents player actions as moves that can be checked and
applied to the world.
- `freerails.controller` translates user intent into game actions.
- `freerails.server` processes game actions, maintains the authoritative game
state, and owns game-start orchestration through injected scenario and save-game
interfaces.
- `freerails.client` coordinates the desktop application, user interaction,
JavaFX views, and rendering. It does not depend on `freerails.server`; the
launcher requests host operations through `LauncherServices`.
- **JavaFX is the production desktop UI framework.** Swing UI code has been
removed; Java2D remains for shared map rendering and image processing.
- `freerails.Start` composes the JavaFX desktop client and optional local server.
  Run with no arguments or `--both` for the local server and interactive client
  setup (the default local-game option connects the client to that server, while
  the client can also join another server), `--client` to launch remote-client
  setup, or `--server` to launch server-only setup. The server-only setup lets
  the user choose a scenario or saved game and listening port. The standalone
  server listens on all network interfaces for remote clients; a locally hosted
  game remains bound to loopback. The JavaFX gameplay shell includes the map,
  overview, terrain and station details, track and station building, train
  production and schedule editing, save/load and exit dialogs, broker
  transactions, and HTML-backed financial reports. The
  map view adapts the existing Java2D renderers to JavaFX images. Build the
  executable JAR separately on each supported platform so it includes that
  platform's JavaFX runtime, including the WebView module used for HTML reports.
- `freerails.io` contains serialization and file/resource adapters, including
scenario and save-game persistence. `freerails.network` contains
communication-level types; shared utilities live in `freerails.util`.

Keep dependencies moving toward these responsibilities. In particular, avoid introducing
dependencies from the game model to application layers or creating new package cycles.
Some existing code does not yet satisfy every boundary; do not treat those exceptions as a
pattern for new code.

Scenario configuration is represented by `freerails.model.scenario.ScenarioData`. It
aggregates the definition data and terrain map, and converts explicitly to and from
`World.Builder` and a world's terrain-only configuration.
`freerails.model.scenario.ScenarioWorldInitializer` builds the initial world from that
data. Scenario JSON layout and classpath resource access belong to
`freerails.io.scenario`; the server requests scenarios through its `ScenarioSource`
interface, implemented by the I/O layer and wired by the launcher. Save-game persistence
implementations belong to `freerails.io.savegame`, while the server-facing `SaveGamesManager`
contract remains in `freerails.server`. Player-specific live state remains outside
scenario configuration and is handled by game setup or save-game persistence.

Economic value types, account and transaction state, and transaction aggregation belong
under `freerails.model.economy`. Financial report generators belong in its
`reporting` subpackage.
The stock valuation formula and its share-count weighting are documented in the
`StockPrice` class Javadoc; keep that as the reference when changing stock-pricing behavior.

Simulation-wide time primitives (`Clock`, `Time`, and `Speed`) belong to
`freerails.model.simulation`, rather than the gameplay-specific
`freerails.model.game` package. Train activities and motion remain under
`freerails.model.operations.train`; their time offsets and durations use simulation
ticks and may be fractional for smooth interpolation. `Time` represents whole tick
boundaries.

Transport model state is grouped under `freerails.model.operations`: train consists,
schedules, activities and motion, station cargo queues, and cargo handling. Its `train`,
`station`, and `cargo` subpackages expose those independently useful APIs. Moves that
change this state stay in `freerails.move`; cargo delivery transactions remain in
`freerails.model.economy.transaction` as accounting records.

## Persistence and game-state contracts

The default on-disk save-game manager lists, loads, and writes `.sav` files in
the user's `FreeRails User Data` folder.

### Savegame compatibility

FreeRails is currently in active development. Changes to persisted game state do not need
to support pre-existing savegames; development should always assume that no pre-existing
savegames need to be loaded.

### Moves and world state

Treat a `Move` as one coherent game action. Moves cross the client/server boundary, so
they should be serializable, immutable, logically comparable, and refer to world objects
through stable identifiers or positions. `applicable` checks a move against an
`UnmodifiableWorld` without changing the state; `apply` performs the change on `World`.

Moves combined in a `CompostMove` must be pairwise orthogonal: each is
applicable to the same original world, and applying them in either order
produces equivalent game state. This is a semantic contract, not a property
that can be reliably inferred from move classes. Unless documented otherwise,
assume a move can be combined with moves affecting independent state. Avoid
overlapping effects, such as track changes on the same or neighboring tiles,
terrain and track changes on the same tile, or updates to the same field of
the same train. Use `CompositeMove` when dependent component moves need a
defined order. The station-removal bundle coordinates track, station-list,
and train-schedule changes as one action; its current use of `CompostMove`
does not make those components generally independent or reusable.

`UnmodifiableWorld` collection getters return live, unmodifiable views: changes made
through the world are visible through previously obtained views, but the views do not
allow adding or removing elements. This is a shallow read-only boundary, not a deep
immutable snapshot. Returned entities and state objects (including trains and stations)
may be mutable and may expose the underlying world state.

Track configurations use an 8-bit directional mask: bit zero is north, and bits
increase clockwise. Scenario templates use standard binary string order, with
the least-significant bit at the right. A tile without a track piece has no
track; a station replaces existing track and requires at least one directional
connection.

Preserve game-state invariants when changing rules. For example, a track connection is
represented on both neighboring tiles, so track changes must use the appropriate paired
track operations rather than changing one side alone. Consider scenario loading when
changing persisted data, and serialization compatibility for data transmitted between
clients and servers.

## Development and code style

### Code and tests

- Follow the existing package structure and nearby code conventions, preserving the
  architecture and package boundaries described above.
- Prefer `Math.clamp()` over nested `Math.max()`/`Math.min()` when constraining a value
  to a range.
- Add or update tests for behavior changes, especially game rules, moves,
  serialization, and scenario loading.
- Prefer applicable assertion helpers from `freerails.util.TestUtils` over
  duplicating their logic in tests.

### Diagnostics and errors

- Production source under `src/main/java` must not write directly to the
  console. Use the project logger for diagnostics, which are written to
  `freerails.log` in the user's `FreeRails User Data` folder. Avoid swallowed
  errors. Logged exception traces omit frames from configured Java runtime and
  dependency package prefixes while keeping application frames, exception
  messages, causes, and suppressed exceptions. The configured prefixes are
  explicit; frames from other third-party packages remain in the trace.

### Documentation

- Add concise Javadoc to every class and every method, including private
  classes and methods, in newly written or generated Java code. Add a brief
  one-line comment before longer logical code sections when their purpose is
  not obvious. Update relevant pages under `docs/` when behavior or
  architecture changes.
- Keep Markdown prose wrapped to roughly 80-100 characters so the source stays
  readable in editors, reviews, and git diffs.

### Scope

- Keep changes focused and preserve existing behavior unless a change is
  intentional.

## Build and run

The project targets JDK 21. Install a JDK 21 or newer and use the included Gradle wrapper
from the repository root:

```shell
.\gradlew.bat test
.\gradlew.bat check
.\gradlew.bat run
.\gradlew.bat run --args="--client"
```

On Unix-like systems, use `./gradlew` instead of `.\gradlew.bat`.
