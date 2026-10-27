# FreeRails

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE.txt)

FreeRails is an open-source railway strategy and management game inspired by *Railroad Tycoon* and *Railroad Tycoon II*.
Players build railway networks, run trains, transport cargo, and grow a railroad empire. The game is written in Java and
includes a desktop client and a game server; its current codebase is an ongoing work in progress.

The project continues a long-running series of FreeRails and Railz projects. Read [HISTORY.md](HISTORY.md) for the
project's background and the [manual](https://freerails.readthedocs.io/en/latest/) for gameplay information.

## Repository layout

- `src/main/java/freerails/` — Java application code, organized into packages for the client UI, game model, controllers,
  moves, networking, scenarios, server, and shared utilities.
- `src/main/resources/` — game graphics, sounds, interface content, and scenario data.
- `src/test/java/` and `src/test/resources/` — automated tests and their supporting data.
- `docs/` — Sphinx documentation, including the player manual, development notes, and specifications.
- `build.gradle`, `settings.gradle`, and `gradle/` — Gradle build configuration and the Gradle wrapper.
- `HISTORY.md` — project history.

## Build and run

Install a JDK 21 or newer, then use the included Gradle wrapper from the repository root:

```shell
# Run the desktop application
./gradlew run

# Run the test suite
./gradlew check
```

On Windows, use `gradlew.bat` instead of `./gradlew`. The project sets Java source compatibility to Java 21.

## Documentation

- [Player manual](https://freerails.readthedocs.io/en/latest/)
- [Project history](HISTORY.md)
- [Release downloads](https://github.com/Trilarion/freerails/releases)

## Contributing

Bug reports and feature requests can be opened in the [issue tracker](https://github.com/Trilarion/freerails/issues).
Contributions are welcome via [pull requests](https://github.com/Trilarion/freerails/pulls).

## License

FreeRails is distributed under the [GNU General Public License, version 3 or later](LICENSE.txt).
