# Development

This guide describes the architecture and working conventions for developing FreeRails.
The implementation and tests are the source of truth when older design notes differ from
the current behavior.

## Repository layout

- `src/main/java/freerails/` — Java application code, organized into packages for the
  client UI, game model, controllers, moves, networking, scenarios, server, and shared
  utilities.
- `src/main/resources/` — game graphics, sounds, interface content, and scenario data.
- `src/test/java/` and `src/test/resources/` — automated tests and their supporting data.
- `docs/` — Sphinx documentation, including the player manual, development notes, and specifications.
- `build.gradle`, `settings.gradle`, and `gradle/` — Gradle build configuration and the Gradle wrapper.
- `docs/history.md` — project history.

## Architecture

FreeRails separates game state and rules from interaction, presentation, and
infrastructure:

- `freerails.model` contains the game domain and world state. It should remain
independent of the client, controller, server, and move layers.
- `freerails.move` represents player actions as moves that can be checked and
applied to the world.
- `freerails.controller` translates user intent into game actions.
- `freerails.server` processes game actions and maintains the authoritative
game state.
- `freerails.client` coordinates the desktop application, user interaction,
Swing views, and rendering.
- `freerails.scenario` and `freerails.io` handle scenarios, saved games, and
serialization. `freerails.network` contains communication-level types; shared utilities
live in `freerails.util`.

Keep dependencies moving toward these responsibilities. In particular, avoid introducing
dependencies from the game model to application layers or creating new package cycles.
Some existing code does not yet satisfy every boundary; do not treat those exceptions as a
pattern for new code.

## Game-state and move contracts

Treat a `Move` as one coherent game action. Moves cross the client/server boundary, so
they should be serializable, immutable, logically comparable, and refer to world objects
through stable identifiers or positions. `applicable` checks a move against an
`UnmodifiableWorld` without changing the state; `apply` performs the change on `World`.

Preserve game-state invariants when changing rules. For example, a track connection is
represented on both neighboring tiles, so track changes must use the appropriate paired
track operations rather than changing one side alone. Consider scenario loading, save
games, and serialization compatibility when changing persisted or transmitted data.

## Development practices

- Use the existing package structure and nearby code conventions. Keep the
model independent of Swing and put game rules in model- or server-facing code, not in
views or renderers.
- Add or update tests for behavior changes, especially game rules, moves,
serialization, and scenario loading.
- Use the project logger for diagnostics. Avoid `System.out`, `System.err`,
and swallowed errors.
- Document public types and important behavior with concise Javadoc, and
update relevant pages under `docs/` when behavior or architecture changes.
- Keep Markdown prose wrapped to roughly 80-100 characters so the source stays
readable in editors, reviews, and git diffs.
- Keep changes focused and preserve existing behavior unless a change is
intentional.

## Build and run

The project targets JDK 21. Install a JDK 21 or newer and use the included Gradle wrapper
from the repository root:

```shell
.\gradlew.bat test
.\gradlew.bat check
.\gradlew.bat run
```

On Unix-like systems, use `./gradlew` instead of `.\gradlew.bat`.
