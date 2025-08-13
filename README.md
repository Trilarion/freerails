# FreeRails

**[Blog](https://trilarion.blogspot.com/search/label/freerails) - [History](http://freerails.readthedocs.io/en/latest/history.html) - [Manual](http://freerails.readthedocs.io/en/latest/)**

FreeRails is a real-time, multiplayer railway strategy & management game where players compete to build the most powerful
railroad empire. It is based on the RailRoad Tycoon I and II games.

It is written in Java and the code is released under the open source [GPL-3.0 license](LICENSE.txt). The project is currently in an intermediate state. It has a long development [history](http://freerails.readthedocs.io/en/latest/history.html).

The goal is to finish the game with a modern 2D graphics style using JavaFx and a stable, efficient game engine with similar features to RailRoad I&II
and worthy AI opponents, so it can be played single-player or multi-player.


## Download & Manual

The latest version is **0.4.1** released on January 2nd, 2018. It's a minor release.

Downloads can be found at the [GitHub Releases page](https://github.com/Trilarion/freerails/releases)

For information how to play FreeRails see the [FreeRails manual](http://freerails.readthedocs.io/en/latest/).

## Bugs, Feature requests, Feedback

- Report a bug/feature request on the [GitHub issue tracker](https://github.com/Trilarion/freerails/issues)

## Contributing

- Report a bug/feature request (see above)
- Fork this repository and create a [GitHub pull request](https://github.com/Trilarion/freerails/pulls)

### Getting started

The source code uses [Gradle](https://gradle.org/guides/#getting-started) as build system which makes it independent of the choice of the IDE.
Most IDEs can import Gradle based project easily, but you could also [install Gradle](https://docs.gradle.org/4.6/userguide/installation.html)
and use it from the command line together with your favorite editor.

#### Eclipse

*Eclipse IDE for Java Developers*

Select Menu File/Import, Select Gradle/Existing Gradle Project, Select Next, Set Project root directory to the FreeRails
local working directory, Select Finish

In the Gradle Tasks window execute task verification/check for running the tests and application/run for running the application.

#### NetBeans

*NetBeans IDE*

Make sure the Gradle support plugin is installed (Menu Tools/Plugins)

Select Menu File/Open Project and select the FreeRails local working directory (should show the Gradle logo) and select
Open Project.

Run the project with F6 and test with Alt+F6.

#### IntelliJ IDEA

*IntelliJ IDEA Community*

Select Import Project, select file build.gradle from the FreeRails local working directory (should show the Gradle logo),
if not set, select "Use default gradle wrapper" and select a suitable Gradle JVM.

Select Menu View/Tool Windows/Gradle and execute task verification/check for running the tests and application/run for running the application.

Use the custom dictionary with Menu File/Settings and Editor/Spelling/Dictionaries and add file /docs/dictionary/freerails.dic
from the FreeRails local working directory.
