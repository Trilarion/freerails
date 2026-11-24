# Overview of Development

This guide describes the architecture and working conventions for developing FreeRails.
The implementation and tests are the source of truth when older design notes differ from
the current behavior.

This document is the authoritative source for FreeRails architecture, design
principles, development and code style, repository layout, and build guidance.
`AGENTS.md` points here rather than duplicating that guidance.

## Repository layout

Paths in this guide are relative to the repository root.

- `src/main/java/freerails` — Java application code, organized into packages for the
  client UI, game model, controllers, moves, networking, scenarios, server, and shared
  utilities.
- `src/main/resources` — game graphics, sounds, interface content, and scenario data.
- `src/test/java` and `src/test/resources` — automated tests and their supporting data.
- `docs` — development notes and specifications; the Sphinx player manual is in `docs/user`.
- `build.gradle`, `settings.gradle`, and `gradle` — Gradle build configuration and the Gradle wrapper.
- `docs/user/history.md` — project history.

## Architecture and design principles

FreeRails separates game state and rules from interaction, presentation, and
infrastructure:

- `freerails.model` contains the game domain and world state. It should remain
independent of the client, controller, server, and move layers.
- `freerails.move` represents player actions as moves that can be checked and
applied to the world.
- `freerails.controller` translates user intent into game actions.
- `freerails.server` processes game actions, maintains the authoritative game
state, and owns game-start orchestration through injected scenario and save-game
interfaces.
- `freerails.client` coordinates the desktop application, user interaction,
JavaFX views, and rendering. It does not depend on `freerails.server`; the
launcher requests host operations through `LauncherServices`.
- `freerails.io` contains serialization and file/resource adapters, including
  scenario and save-game persistence.
- `freerails.network` contains communication-level types.
- Shared utilities live in `freerails.util`.

Keep dependencies moving toward these responsibilities. In particular, avoid introducing
dependencies from the game model to application layers or creating new package cycles.
Some existing code does not yet satisfy every boundary; do not treat those exceptions as a
pattern for new code.

Further notes:

- JavaFX is the production desktop UI framework. Java2D remains for shared map rendering and image processing.
- `freerails.Start` composes the JavaFX desktop client and optional local server.
  Run with no arguments or `--both` for the local server and interactive client
  setup (the default local-game option connects the client to that server, while
  the client can also join another server). Use `--client` to launch remote-client
  setup, or `--server` to launch server-only setup. The server-only setup lets
  the user choose a scenario or saved game and listening port. The standalone
  server listens on all network interfaces for remote clients; a locally hosted
  game remains bound to loopback.
- The JavaFX gameplay shell includes the map,
  overview, terrain and station details, track and station building, train
  production and schedule editing, save/load and exit dialogs, broker
  transactions, and HTML-backed financial reports.
- Build the
  executable JAR separately on each supported platform so it includes that
  platform's JavaFX runtime, including the WebView module used for HTML display.

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

### Client/server design

The server keeps the authoritative full game model. Each client is a thick
client built on the model-view-presenter pattern:

- The passive view, made of JavaFX views and renderers, shows the game state
  and forwards user input.
- The presenter (the controllers and client coordination) turns the input into
  proposed changes, expressed as moves, and updates the view when the model
  changes.
- The client keeps its own full copy of the game model. The model signals
  changes to the presenter.

Proposed moves go from the client's model to the server. The client can
pre-commit them locally (see `MovePrecommitter`), so the player sees the effect
at once. The server checks each move with `applicable`, applies the accepted
ones, and broadcasts them to all clients. Each client then confirms its
pre-committed moves or undoes them. If a client's copy becomes inconsistent,
the server can send it the whole world (`SetWorldCommandToClient`).

The server never trusts player identity supplied by a client. It accepts from
clients only moves that implement `ClientMove` and move generators that
implement `ClientMoveGenerator`, and rejects everything else, such as moves
only the server makes. Before it checks or applies one, it replaces every
player reference in it, including in contained moves, with the player of the
connection the message arrived on (`withPlayer`). The moves it forwards to
other clients are the rebound ones, so a player stored in an accepted move can
be trusted. A new type that clients may send must implement one of these
interfaces and declare its own `withPlayer`; `ClientMoveTest` checks this.
Owner IDs inside track pieces describe the world, not the sender, and are not
rebound; the track rules check them against the acting player.

This makes sure a client can only act as itself. It does not yet limit what a
client may do with its own state: clients still create their own transactions
(and so choose their own prices), can send a tile change without its cost or a
train update that changes more than the schedule, and any player can change
the game speed. Closing this gap means calculating costs on the server and
narrowing what these moves may change.

Besides moves, client and server exchange commands (`CommandToServer`,
`CommandToClient`). Moves are checked changes to an existing game world.
Commands control the session: starting, loading, and saving games, updating
the lists of connected clients, scenarios, and saved games, and replacing a
client's whole world. Commands are not moves, because they may act on state
outside the world or replace the world itself, which a move cannot do and
which must bypass move pre-commitment. The receiver of a command replies with
a `CommandStatus`. The server should know whether a command failed on a
client; it does not act on these replies yet.

## Persistence and game-state contracts

The default on-disk save-game manager lists, loads, and writes `.sav` files in
the user's `FreeRails User Data` folder. A save-game file contains a `SavedGame`: the
world and the players' log-on credentials. The server game model itself is not saved.

### Savegame compatibility

FreeRails is currently in active development. Changes to persisted game state do not need
to support pre-existing savegames; development should always assume that no pre-existing
savegames need to be loaded.

Instead, incompatible savegames must be rejected cleanly. `FullSaveGameManager` writes a
gzipped Java object stream containing a magic number, the `FORMAT_VERSION`, the build
version that wrote the file (informational only), and the `SavedGame`. Loading rejects
files with a different magic number or format version with an `IOException` that names
both builds. Saves are written to a temporary file and then moved into place, so a failed
save never damages an existing savegame.

`SaveGameFormatTest` describes the serialized structure of every FreeRails class that can
be stored in a savegame and compares it with
`src/test/resources/freerails/io/savegame/savegame-format.txt`. When it fails, the
savegame structure changed: bump `FullSaveGameManager.FORMAT_VERSION` and regenerate the
golden file with
`./gradlew test --tests freerails.io.savegame.SaveGameFormatTest -Dfreerails.updateSaveGameFormat=true`.
Do not edit the golden file by hand or regenerate it without bumping the version. A class
that is only reachable through a field of type `Object` or `Serializable` is not found
automatically; add it to `SaveGameFormatTest.EXTRA_ROOTS`.

Because this test decides compatibility, all `serialVersionUID`s are `1L`; do not use
generated values. Savegames and network connections are deserialized through the shared
`SerializationFilter` allowlist (FreeRails classes plus standard value and container
classes). A new serializable type outside `freerails.*` must be added there.

A future option is to replace Java serialization for savegames with an explicit,
non-Java-serialization format such as JSON with a versioned schema. It would make savegames
readable and diffable, decouple them from class internals, remove the deserialization attack
surface, and allow migrations should savegame compatibility become a goal. It is not
pursued while compatibility with older savegames is not required.

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
defined order. For example, `RemoveStationMoveGenerator` builds the
station-removal bundle as a `CompositeMove` that applies the track change, the
station removal, and the owner's train-schedule updates in that order as one
action.

`UnmodifiableWorld` collection getters return live, unmodifiable views: changes made
through the world are visible through previously obtained views, but the views do not
allow adding or removing elements. This is a shallow read-only boundary, not a deep
immutable snapshot. Returned entities and state objects (including trains and stations)
may be mutable and may expose the underlying world state.

Preserve game-state invariants when applying moves. For example, a track connection is
represented on both neighboring tiles, so track changes must use the appropriate paired
track operations rather than changing one side alone. Likewise, station track and
its station are removed together: a `ChangeTrackPieceMove` on its own refuses to
remove station track, and `RemoveStationCompositeMove` checks that the track change,
station removal, and schedule updates belong together. Consider scenario loading and saving when
changing persisted data, and serialization compatibility for data transmitted between
clients and servers.

### Track model

The map is a rectangular grid of square tiles. Every tile that is not on the
map border has exactly eight neighbors. A neighbor can be named either by its
compass direction from the center tile or by its grid offset. With a tile at
column `x` and row `y`, north is `(x, y - 1)`, east is `(x + 1, y)`, south is
`(x, y + 1)`, and west is `(x - 1, y)`; the diagonal neighbors combine these
offsets. `TileTransition` defines the eight directions, their offsets, and their
configuration bits.

The track network consists of track pieces. Each piece connects two
neighboring tiles. A diagonal connection is about 1.41 (the square root of 2)
times as long as a horizontal or vertical one. Track pieces have no direction:
any train can use them both ways.

Each tile stores its track as a track configuration, an 8-bit mask with one bit
per neighbor that the tile connects to. Bit zero is north and the bits increase
clockwise:

| Direction | Bit | Value |
|-----------|-----|-------|
| North | 0 | 1 |
| North-east | 1 | 2 |
| East | 2 | 4 |
| South-east | 3 | 8 |
| South | 4 | 16 |
| South-west | 5 | 32 |
| West | 6 | 64 |
| North-west | 7 | 128 |

Scenario templates use standard binary string order, with the least-significant
bit at the right. For example:

- `00010001`: straight north-south track
- `00011001`: north-south track with a branch to the south-east
- `10000010`: a V shape opening upward, connecting north-west and north-east

The renderer draws each tile from its configuration. A tile without a track
piece has no track. A station replaces existing track and requires at least one
directional connection.

Track configurations on neighboring tiles depend on each other. When a tile
connects toward a neighbor, that neighbor must also connect back to the tile.
Code must therefore never change a configuration directly. It adds or removes
whole track pieces, which update both tiles together.

Train path finding works on a graph whose nodes are tiles and whose edges are
track pieces. When the player builds a longer stretch of new track, the route
is also planned by path finding over candidate track pieces.

## Development and code style

### Code and tests

- Follow the existing package structure and nearby code conventions, preserving the
  architecture and package boundaries described above.
- Prefer `Math.clamp()` over nested `Math.max()`/`Math.min()` when constraining a value
  to a range.
- Add or update tests for behavior changes, especially game rules, moves,
  and serialization.
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
  not obvious. Update relevant pages under `docs` when behavior or
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
