*************
Specification
*************

*Originally written by Luke Lindsay.*

.. note::
   This spec is incomplete and what is here will be revised several times before it is finalized.
   This spec may not discuss in detail how features will be implemented.

*Scenarios*

In designing software, it helps to imagine a few real life stories of how actual (stereotypical) people would use it.
We'll look at three scenarios.

Scenario 1: Jeff.
Aged 38, splits his time between contract web development work and (real) surfing (i.e. He's an aging hippy).
Played RT1, disliked RT2 and RT3. Sceptical about the quality of open source software.

Scenario 2: Andy.
Young programmer, 19, rides a moped. First language not English. Never played RT1 but has played RT2 and RT3.
When software doesn't work or works in a way he doesn't expect, he is quick to blame the software.
Wants to get involved in the development of jfreerails and to write is own AI.

Scenario 3: Claudia.
Recently left university and started working for the government (as an economist). Never played any of the RT games before.
Wants something to play on her computer to distract her from her research.

*Non Goals*

Realism - it's a game not a simulation.

*Not yet included*

- Customisablity
- Difficulty levels
- Advanced graphics (including animations other than movement).
- Performance
- Cheat proof: don't play it with your house-mate who keeps stealing your milk
- Prority Shipments
- Scripting events and AI using Jython
- Internet high score table
- Custom Look-and-Feel
- Undo building track
- Rate wars
- Save game compatibility between releases.
- Signals

Game Model
----------

Time
++++

Each year is a representative day. So, for instance, a train travelling at an average speed of 10 mph will make two
(and a bit) 100 mile trips in a year.

Map
+++

Two maps will be available. A map of south America - used for proper games -and a small map - used for a tutorial.
The maps are divided into square tiles. Tiles are 10 miles across. Rather than let the scale vary between maps of
different sized regions, different sized maps should be used.

.. note::
   The exact shape of mountain ranges and the distribution of terrain types will vary between games.
