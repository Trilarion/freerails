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

Terrain
+++++++

Terrain Type
............

Each tile on the map has a 'terrain type' e.g. Farm, desert etc.

Category
  All terrain types fall into one of the following categories: Urban, River, Ocean, Hill, Country, Special, Industry, or Resource.

Cargo production
  Some types of terrain produce cargo (of one or more types), e.g. A Cattle Ranch produces livestock.

Cargo consumption
  Some types of terrain consume cargo, e.g. 'City' tiles consume 'Food'.

Right-of-way cost
  Before you build track on a square, you need to purchase the right of way. Different terrain types have different ROW costs.

Terrain Heights
...............

Different terrain types will have different heights, I'll call this the type-height.
The height of a tile will be the weighted average of the type-heights of the surrounding tiles.

Cargo
+++++

There are a number of cargo types, e.g. Mail, Passengers, Livestock etc. Cargo types fall into one of the following categories:
Mail, Passengers, Fast Freight, Slow Freight, Bulk Freight. Mail is the most sensitive to speed of delivery; Bulk Freight is the least.

Cities
++++++

Cities of random size are added at predefined locations on the map when the game starts.
As time passes, cities grow and shrink depending the amount of cargo picked up and delivered.

Track
+++++

There are a number of track types, e.g. standard track, double track, tunnel etc. A tile can only have track of one type.
You can only built track if you have sufficient cash. The cost of building track depends on the track type,
the track configuration, and the right of way cost of the terrain. Some track types can only be built on certain terrain,
e.g. an Iron Girder bridge can only be built on river. If some track has already been built, all new track must
connect to the existing track.

Track can be upgraded. The cost of upgrading track from type X to type Y is less than the cost of building
track of type Y but more than the cost of Y minus the cost of X. Track can be removed. When you remove track you
get a little amount of money back.

Different track types have different maintenance costs. The maintenance cost must be paid once per year.

Bridges
.......

Name | Price | Train Speed | Track Type

- Wooden Trestle | $50,000 | slow | single
- Iron Girder | $200,000 | fast | single
- Stone Masonry | $400,000 | fast | double

Track Contention
................

On single (double) track, only one (two) train(s) can move at a time. Gridlock shouldn't occur because when trains stop
moving they do not block other trains.

Trains
++++++

Once you have built some track and a station you can build a train. You get a choice of engines and the option to add up
to 6 wagons. You can build trains even if you have cash < 0.
(This is to stop people getting stuck without any trains since you need trains to make money.)
Trains can be scheduled to stop at 2 or more stations.
The pseudo code below describes the behaviour of trains.::

   if(train is moving){
      if(train has  reached a new tile){
         if (there is a station here){
            unload cargo demanded by station
            if(this is a scheduled stop){
               if(consist needs changing){
                  unload cargo that won't fit after changing consist
                  change consist
               }
            }
            load cargo
            setStatus(stoppedAtStation)
            departTime = currentTime + stopTime
         }else{
            setStatus(readyToMove)
         }
      }else{
         keep moving
      }
   } else if(train is at station){
      if(waiting for full load){
         load any cargo
         if(full){
            update next station on schedule
            if(currentTime > departTime){
               setStatus(readyToMove)
            }
         }
      }else{
         if(currentTime > departTime){
            if(this was a scheduled stop){
               update next station on schedule
            }
            setStatus(readyToMove)
         }
      }
   }
   if(train status is readyToMove){
       find next track section
       if(number of trains on next track section < number of tracks){
           setStatus(moving)
           move train onto next track section
       }
   }

Engines
.......

Two types of engine are available when you start the game. Three other types become available later.
A train's engine can be upgraded.

Wagons
......

There is one wagon type for each cargo type. A wagon can carry 40 units of cargo. Wagons are free and can be added at
any station (since moving empty wagons about would be boring).

Train Schedules
...............

The stations a train stops at and whether it changes its wagons when it stops are governed by a train's schedule.

Train Movement
..............

The more wagons a train is pulling, the slower it moves. The greater the amount of cargo, the slower the train.
The gradient of the track also affects speed. When trains arrive at a station it stops for a few moments to load
and unload cargo. Trains stop instantly (this is a simplification so we don't need to look ahead) but speed up slowly.

Stations
++++++++

Supply and demand at a station is determined by the tile types within the station's sphere-of-influence. Different
station types have different sized spheres-of-influence. The spheres-of-influence of two stations cannot overlap.

Station Improvements
....................

Improvement Type | Effect | Price

- Engine Shop | Trains can be built | $100,000
- Switching Yard | Cuts time taken the change wagons by 75% | $50,000
- Maintenance Shop | Cuts yearly maintenance of trains that stop at station by 75% | $25,000
- Cargo Storage | Prevents a certain cargo from wasting away | variable
- Revenue booster | Increases revenue from a cargo X by Y% | variable

Economy
+++++++

The economy alternates between 5 states, with an order and states can only change to adjacent states.

Economic Climate | Base Interest rate | Effect on track price

- Boom | 2% | +33%
- Prosperity | 3% | +17%
- Moderation | 4% | -
- Recession | 5% | -17%
- Panic | 6% | -33%

Stocks and Bonds
................

The value of a bonds is $500k.

The interest rate for new bonds = (base interest rate) + (number of outstanding bonds). Bonds can only be issued if this figure is <= 7.

New railroads issue 1,000,000 shares at $5 per share.

Shares are traded in 10,000 share bundles.

If the share price is >$100 at the end of the year, stocks are split 2 for 1.

Stock price = [Net worth at start of year + 5 * profit last year] / [ shares owned by public + 0.5 shares owned by other players]

Let profit last year = 100,000 in the first year.

When a player buys or sells shares, the price used is the price calculate after the shares have changed hands.

A transaction fee $25,000 applies each time a bond is issued or repaid and each time a bundle of shares is bought or sold.

Competition between Railroads
.............................

You can take over a rival by buying over 50% of its stock. When you have done this, you have indirect control over the
other railroad. You can transfer money between the two railroads, tell the other railroad where to build track to next,
and tell the other railroad to repay its bonds.

If you go on to buy 100% of the stock, you have the option to merge the with the other railroad. If you do this,
you gain complete control over the other railroad. I.e. the other railroad's track, trains, and stations are added
to your railroad. Once a merger has taken place, there is no way to undo it.

Non player effects on the game model
++++++++++++++++++++++++++++++++++++

City Growth and Decay
.....................

As time passes, Urban (e.g. Village, City), Industry (e.g. Factory, Steel Mill), and Resource (e.g. Coal Mine, Sugar
Plantation) tiles are added and removed from cities. Example, a factory tile will relocate from New York to Boston if
Boston's utility gain exceeds New york's utility loss. Utilities are calculated as follows. The routine that updates
cities should run once per month.

Category | Marginal utility | Motivation

- Industry | Number of Urban Tiles / (1+Number of Industry Tiles)^2 | Urban tiles supply labour to industries. There are decreasing returns to scale.
- Resource | (Units of Resource Picked Up + c) / (1 + Number of Resource Tiles) | Resources grow when they are exploited. There are decreasing returns to scale.
- Urban | Units of demanded cargo delivered - k * Number of Urban Tiles/(1+Number of Industry Tiles) | Urban tiles value employment and delivery of cargo but are adverse to overcrowding.

Industries owned by Railroads do not enter the utility calculations, so when you build an industry, it stays put!

Payments for delivering Cargo
.............................

Addition and removal of cargo at stations
.........................................

User Interface
++++++++++++++

Main Window
...........

The main window has a menu bar, the world view, the mini map, and a tabpane.

.. figure:: /images/ui_main_window.png
   :width: 10 cm
   :align: center

   Scheme of main window.

The GUI components should display properly when the main window is 640 * 480 pixels or bigger. The table below shows the
dimensions of the components in terms of the width (W) and height of the main window. The figures do not include space
taken up by borders, scroll bars, tabs, menus etc.

Component | Width | Height

- Minimap | 200 | 200
- Tab's Content | 195 | H - 300
- World View | W - 230 | H - 70

Pressing the tab key toggles keyboard focus between the world view window and the tabpane's content.

Menu bar
........

Game - New Game | Game Speed (Paused, Slow, Moderate, Fast) | Save Game | Load Game | Exit Game

Build - New train | Industry | Improve Station

Display - Regional Display | Area | Detailed Area | Options

Reports - Balance Sheet | Income Statement | Networth Graph | Stocks | Leaderboard | Accomplishments

Broker - Call Broker

Help - Controls | Quick Start | Manual | Report Bug | About

World View Window
.................

The world can be displayed at 4 zoom levels:

- Local detailed: 30x30 px
- Local: 15x15 px
- Network: Scaled so that all the player's stations are visible, Shows trains, stations, and track but not geography
- Regional: Scaled so the whole map is visible, Minimap hidden

Cursor
......

The cursor can be in one of the following modes. The cursor should only be visible if the world view has keyboard focus.
The cursor's appearance should indicate which mode it is in.
The initial cursor position is 0,0. However, if a game is loaded or a new game is started and the map size is the same
as the last map size, then the cursor should take the position it had on the last map.

Place station mode
..................

The cursor gets put into place-station-mode when the player selects a station type from the build tab.
Shows the radius of the selected station type.
Red when station cannot be built on selected square, white otherwise. This should be determined by whether building the
station is possible, not merely whether there is track on the selected tile.
Pressing the LMB attempts to place the station. If the station is built, the cursor is returned to its previous mode;
if the station is not built, the cursor remains in place-station-mode.
Pressing the RMB or pressing Esc cancels placing the station and returns the cursor to its previous mode.

Build track mode
................

Track can be built by dragging the mouse (moving the mouse with the LMB down). As the mouse is dragged, the proposed
track is shown. Releasing the LMB builds the track. Pressing the RMB or Esc cancels any proposed track.
Track can be built by pressing the number pad keys.

Remove track mode
.................

Track can be removed by moving the cursor with the number pad keys.

Info mode
.........

Components on right hand side
+++++++++++++++++++++++++++++

Minimap, Current Cash, Date (Shows the current year and month.)

Train Roster Tab
................

Shows wagons in each train and whether they are full or empty, the trains relative speed and destination.
Double clicking a train on the roster (or pressing enter when the train roster has focus) or on the map
opens the train report for the train.

Build Tab
.........

There are 5 build modes (see the table and screenshot below).

.. list-table:: Build modes
   :header-rows: 1

   * - Build mode
     - Options visible when mode is selected
     - Action
   * - build track
     - Track type, bridges, and tunnels.
     - When the cursor is moved, track is built. On clear terrain, the selected track type is built. On rivers, the selected bridge type is built (if a bridge type is selected.) On hills and mountains, tunnel is built if build tunnls is selected.
   * - upgrade track
     - Track type and bridges
     - Track and bridges are upgraded to the selected type when the cursor enters a tile.
   * - build station
     - Stations
     -
   * - bulldoze
     - None
     - When the cursor moves from a tile to a neigbouring tile, any track connecting the two tiles is removed.
   * - off
     - None
     - Nothing is built or removed when the cursor moves.

.. figure:: /images/ui_build_tab.png
   :width: 8 cm
   :align: center

   Scheme of build tab.

The Build tab should not accept keyboard focus when the mouse is click on it. This is because doing so would cause
the world view window to lose focus which is annoying when you are building track using the keyboard.
When a new game is started or a game is loaded, the build mode should default to 'build track' with single track,
wooden trestle bridges, and tunnels selected.

Reports and dialog boxes
++++++++++++++++++++++++

Broker Dialog
.............

.. figure:: /images/ui_broker_dialog.png
   :width: 10 cm
   :align: center

   Example of the broker dialog.

.. figure:: /images/ui_broker_dialog2.png
   :width: 10 cm
   :align: center

   Another example of the broker dialog.

Station report
..............

Shows information on a station.  There will be 3 tabs: 'supply and demand', 'trains', and 'improvements'

.. figure:: /images/ui_station_report_supply_demand_tab.png
   :width: 10 cm
   :align: center

   Supply and Demand Tab

The trains tab will list all trains that are scheduled to stop at this station.  Note, if a train is scheduled to
stop at the stations several times, there will be a row in the table for each scheduled stop.

.. figure:: /images/ui_station_report_trains_tab.png
   :width: 10 cm
   :align: center

   Trains Tab

Improvements tab shows the station improvements that have been built at this station and lets you buy additional ones.

Station list
............

Shows summary details for each of the stations: name, type, cargo waiting, revenue this year.

Train report
............

.. figure:: /images/ui_train_report.png
   :width: 10 cm
   :align: center

   Trains report

Train list
..........

Shows summary details for each of the trains

.. figure:: /images/ui_train_list.png
   :width: 10 cm
   :align: center

   Trains list

Select station
..............

.. figure:: /images/ui_select_station.png
   :width: 10 cm
   :align: center

   Select station

Newspaper
.........

.. figure:: /images/ui_newspaper.png
   :width: 10 cm
   :align: center

   Newspaper

Leaderboard
...........

.. figure:: /images/ui_leaderboard.png
   :width: 6 cm
   :align: center

   Leaderboard

Balance sheet
.............

.. figure:: /images/ui_balance_sheet_dialog.png
   :width: 10 cm
   :align: center

   Balance sheet

Income statement
................

.. figure:: /images/ui_income_statement.png
   :width: 10 cm
   :align: center

   Income statement

Networth graph
..............

.. figure:: /images/ui_networth_graph.png
   :width: 10 cm
   :align: center

   Networth graph

Report bug dialog
.................

The report bug dialog box is accessible from the help menu. It is also shown when there is an unexpected exception.

It should list the following information and it should be possible to copy and paste the details to the clipboard.
Property | Value

- tracker.url	http://sourceforge.net/tracker/?group_id=9495&atid=109495
- java.version	Java System Property
- java.vm.name	Java System Property
- os.name	Java System Property
- os.version	Java System Property
- jfreerails.build	The timestamp generated by the ant script.
- jfreerails.compiled.by	The username of the crazy person who ran the ant compile target

The how to report bug dialog should appear as follows...

::

    How to report a bug

    Use the sourceforge.net bug tracker at the following url:
    {tracker.url}

    Please include:
      1. Steps to reproduce the bug (attach a  save game if  appropriate).
      2. What you expected to see.
      3. What you saw instead (attach a screenshot if appropriate).
      4. The details below (copy and past them into the bug report).
        {os.name} {os.version}
        {java.vm.name} {java.version}
        Freerails build {jfreerails.build}  compiled by {jfreerails.compiled.by}

And the “Unexpected Exception” version should read ...

::

    Unexpected Exception

    Consider submitting a bug report using the sourceforge.net bug tracker at the following url:
    {tracker.url}

    Please:
    1.	Use the following as the title of the bug report:
        {Exception.type} at {fileaname} line {line.number}
    2.	Include steps to reproduce the bug (attach a  save game if  appropriate).
    3.	Copy and paste the details below into the bug report:

    {os.name} {os.version}
    {java.vm.name} {java.version}
    Freerails build {jfreerails.build}  compiled by {jfreerails.compiled.by}

    {stacktrace}

Cargo chart
...........

The cargo chart will show the sources of supply and demand for each of the cargo types. The information will be
presented in a table as below.  There should be a 'print' button which should... well, its pretty obvious what it should do.

.. figure:: /images/ui_cargo_chart.png
   :width: 10 cm
   :align: center

   Cargo chart

Load games
..........

Displays a list of saved games.  The list comprises all files ending in '.sav' in the directory from which the game
was run.  If the current game is a network game, the relevant directory is the directory from which the server was
run.  All players, not just the host, can access the dialogue.

.. figure:: /images/ui_load_game.png
   :width: 10 cm
   :align: center

   Load game

The 'OK' button is only enabled when a game is selected.
Pressing the 'OK' button loads the selected game.
Pressing the 'Cancel' button closes the dialogue box.
Pressing the 'Refresh' button updates the list of saved games, taking into account any changes to the filesystem (e.g. any files that have been added, removed, or renamed.)

Launcher
........

Panel 1: Select Game Type

.. figure:: /images/ui_launcher1.png
   :width: 10 cm
   :align: center

   Launcher1

Selection | Next Screen

- Single Player	Select Map (without server port input box)
- Start a network game	Select Map (with server port input box)
- Join a network game	Client details (with remote server details showing)
- Server only	Select Map (with server port input box)

Panel 2: Select Map (and server details)

.. figure:: /images/ui_launcher2.png
   :width: 10 cm
   :align: center

   Launcher2

The value of the field “Server port” should be the value entered last time the launcher was run.  On the first run it defaults to 55000.

Selection | Next Screen

- Single Player	Client details (without remote server details showing)
- Start a network game	Client details (without remote server details showing)
- Server only	Connected players

Condition | Message or result | When checked

- No saved game available. | The item "Load a saved game" should be disabled | When the panel is created.
- Port field does not contain a number between, inclusive 0 and 65535 | "A valid port value is between between 0 and 65535." and disable "next" button. | As text is entered.
- " Start a new map" is selected but no map is selected." | "Select a map". The "next" button should be disabled. | When the radio button selection changes and when the selected map in the map list changes.
- Can't start server on specified port | Use the message from the exception. The next button should still be enabled. | When the next button is pressed.

Panel 3: Client details (and remote server details)

The following fields should be recalled from the last time the launcher was run.
Field | Default | Notes

- Player name | The value of system property "user.name" | If a game is being loaded, the text box should not be appear.  Instead there should be a dropdown list with the names of the players from the saved game.
- IP Address | 127.0.0.1 |
- port | 5500 |

Selection | Next Screen

- Single Player | Progress bar
- Start a network game | Connected players
- Join a network game | Progress bar

.. figure:: /images/ui_launcher3.png
   :width: 10 cm
   :align: center

   Launcher3

Condition | Message or result | When checked

- The " Player name" field is empty. | " Enter a player name" and disable " next" button. | As text is entered.
- Port field does not contain a number between, inclusive 0 and 65535 | " A valid port value is between between 0 and 65535." and disable " next" button. | As text is entered.
- " Full screen" is selected but no map is selected." | " Select a display mode" . The next button should be disabled. | When the radio button selection changes and when the selected display-mode in the display-mode list changes.
- The " IP address" field is empty. | " Enter the address of the server" and disable " next" button. | As text is entered.
- Can't resolve host. | "Can't resolve host." | When next button is pressed.
- Can't connect to server. | " Can't connect to server." | When next button is pressed.
- Load game was selected. | The player name textbox should be replaced with a dropdown list of players in the saved game. | When the form is displayed.
- We are connecting to a remote server which has loaded, but not started a game, and the player name we entered is not a player in the saved game. | "New players can't join a saved game." | When next button is pressed.
- We are connecting to a remote server but the game has already started. | "New players can't join a game in progress." | When the next button is pressed.

Panel 4: Connected players

.. figure:: /images/ui_launcher4.png
   :width: 10 cm
   :align: center

   Launcher4

Panel 5: Progress bar

.. figure:: /images/ui_launcher5.png
   :width: 10 cm
   :align: center

   Launcher5

AI
++

Disclaimer - the notes below are very incomplete. It might make sense to do something simpler for the first version of the AI.

Deciding which cities should be connected to each other
Create a table of the distances between cities. E.g.

.. list-table:: City distances example
   :widths: 10 10 10 10 10
   :header-rows: 0

   * -
     - City A
     - City B
     - City C
     - City D
   * - City A
     - x
     - 20 km
     - 30 km
     - 25 km
   * - City B
     - x
     - x
     - 35 km
     - 15 km
   * - City C
     - x
     - x
     - x
     - 40 km
   * - City D
     - x
     - x
     - x
     - x

::

   For every pair of cities i, j {
       For every city k where i != k and j != k {
           Let A = the distance between i and j.
           Let B = the distance between i and k.
           Let C = the distance between k and j.
           If (A < B and A < C) then remove the value at i, j from the table.
       }
   }

We can now construct a graph from the values remaining in the table. It will have the following properties. First, every
city is connected to its nearest neighbour. Second, we can get from any city to any other city. Third, not too much track will be wasted.

Deciding the order in which to connect cities
Lets assume the profitability of a line between 2 cities, A and B is given by the following condition.
Profitability = (Cargo supplied by A and demanded by B + Cargo supplied by B and demanded by A) / Distance between A and B.

Implementation Note: a natural way to analyze supply and demand and cargo conversions would be using matrix algebra.
E.g. supply and demand at a station could be represented by n * 1 matrices and cargo converted by an n * n matrix
(where n is the number of cargo types). There is a public domain java matrix package available at: http://math.nist.gov/javanumerics/jama/.

The simplest strategy for building track would be starting with most profitable connection. Note, that on the first
move, we can pick any connection, but on subsequent moves, we are restricted to connections involving at least one
city we have already connected to. Call this restricted set of connections S. A reasonable strategy for subsequent
moves would be repeatedly picking the most profitable connection from S.

A more sophisticated strategy would take into account the restriction that new track must connect to existing track
when picking the first pair of cities to connect. We could approach the problem as follows. Assume we build one
connection per year and the game continues until we have built all possible connections. Suppose our payoff for
building a connection is the profitability of the connection times the number of years remaining. For simplicity,
assume that once we have built the first connection, we revert to just picking the most profitable connection from
S as before. Now, we can solve the problem of which connection to start with by comparing the payoff over the
complete game for each of the possible starts.

Obviously, to formally solve the problem above, we would need to consider strategies other than picking the most
profitable connection from S for moves after the first one. However, unless the number of cities is relatively
small this would likely take a long time to solve. What is more, we have not even considered what other players
may be doing, so even if we could formally solve the problem above, we would still have a lot of work to do.
