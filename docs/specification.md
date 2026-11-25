# Specification

FreeRails is a railway strategy and management game. The player develops a railroad across
a changing landscape, connects places that need transport, and turns a small network into
a thriving business. The game aims for engaging strategic choices rather than a complete
or realistic simulation.

This document describes the intended player experience and game rules. It focuses on what
players can do and how the world responds, not on how the game is implemented.

## Design goals

- Make building and operating a railroad the heart of play: players choose
where to invest, which places to serve, and how to grow their network.
- Offer a rich set of connected systems. Geography shapes construction;
stations connect local supply and demand to the network; trains turn routes into services;
and company finances reward or constrain expansion.
- Give players room to pursue different strategies, from careful service
planning to rapid expansion and competition with other railroads.
- Keep the rules understandable and the world legible, while leaving
interesting trade-offs for players to discover.

## The world and the passage of time

The world is a map of square tiles representing a broad region. Terrain, settlements,
resources, and industries vary across maps and scenarios, giving each network different
opportunities and construction challenges. Maps may range from compact tutorial settings
to large regional landscapes.

Game time is abstracted so that a relatively short play session can represent the
development of a railroad over many years. The economy and the world change as time
passes, creating new opportunities and changing the value of existing services.

## Building a railroad

Players extend their network by acquiring rights of way and building track. Terrain
affects the expense and difficulty of construction. Track can be improved, removed, and
extended from the existing network; bridges and tunnels allow routes through otherwise
difficult terrain. Building and upgrading require investment, while track and trains carry
ongoing costs.

Track construction is charged at the selected track type's price plus the terrain's
right-of-way cost per mile. Industry construction has a separate terrain-specific build
cost.

Track is shared infrastructure with limited capacity. Trains must contend for available
routes, but a train that has stopped should not permanently prevent other trains from
moving. Route planning therefore balances direct connections, construction cost, terrain,
and the traffic the network can carry.

## Stations, cargo, and train services

Stations gather the goods and passengers produced or needed in the surrounding area. Their
service area and local development determine the traffic available to a railroad. Stations
must be placed so their service areas do not overlap. Separate engine shops can be built at
stations to construct trains; other station improvements can improve operations,
maintenance, storage, and the return on selected cargo.

The world produces and consumes a variety of cargo, including passengers, mail, and
freight. Cargo types differ in their delivery needs: some benefit more from speed, while
others are suited to bulk transport. Cargo waits at stations for service and may lose
value or be wasted over time, so reliable routes and suitable station facilities matter.

Players build trains from engines and cargo-carrying wagons, then schedule services
between stations. A train loads and unloads where it stops, and its consist can be changed
at scheduled stations. Train performance depends on its engine, consist, cargo, and the
terrain along its route. Trains find routes automatically, with priority services
receiving precedence when routes are contested.

## Running a railroad

Delivering cargo earns revenue and connects the quality of a railroad's services to its
financial success. Players must balance expansion and improvements against construction,
operating, and maintenance costs. Changing economic conditions affect the cost of
investment and the terms of borrowing.

Companies can raise and repay bonds, issue and trade shares, and compete for control of
rival railroads. Gaining control can provide influence over a rival; complete acquisition
can combine its assets with the player's railroad. Financing and competition provide
alternatives to growth through construction alone, but they also create financial risk.

## A changing economy and landscape

Cities, industries, and resource-producing places respond over time to their economic
surroundings and to the traffic delivered by railroads. Settlements can grow or decline,
and the location and mix of economic activity can change. These changes affect where cargo
is available and where new connections may become worthwhile.

City growth is an explicit part of scenario design, not a universal rule. Scenarios can
enable yearly city expansion when it creates meaningful gameplay, or use static city
footprints after initial setup when cities serve only as destinations and cargo sources.

## Play against other railroads

Players may develop their railroad alone or compete with other human or
computer-controlled railroads. Every railroad should operate within the same game rules,
and competition should emerge from decisions about service, investment, and control of
companies rather than from separate rule sets.

## Visual and interaction style

The map is the main way players understand and shape the world. It should make terrain,
settlements, stations, track, and moving trains easy to distinguish, with enough regional
context to support planning and enough local detail to make construction and operations
clear.

The presentation should be readable and informative rather than ornamental. Important
changes in the railroad and its finances should be easy to recognize, and reports should
help players understand the state of their company and services without obscuring the
world.

Interaction should make planning feel direct. Players should be able to inspect the map,
choose an action, see clear feedback about its likely result, and confirm or cancel it
without losing their place. Invalid or unaffordable actions should be apparent before they
cause confusion. Mouse and keyboard input should both support common map and management
tasks, with consistent controls and locally available, localized help.

## User interface design

This section records the intended screen design. It is distilled from earlier design
notes and screenshots. Not every screen or detail is implemented in the current JavaFX
client yet; where they differ, treat this section as the target design.

### Main window

The main window has a menu bar, the world view, a mini map, and a tab pane. The world
view fills most of the window. The mini map, the current cash, the date (year and month),
and the tab pane are arranged in a narrow column beside it. Pressing Tab moves keyboard
focus between the world view and the tab pane's content.

The menu bar contains:

- **Game**: New Game, Game Speed (Paused, Slow, Moderate, Fast), Save Game, Load Game,
  Exit Game
- **Build**: New Train, Industry, Improve Station
- **Display**: Regional Display, Area, Detailed Area, Options
- **Reports**: Balance Sheet, Income Statement, Net Worth Graph, Stocks, Leaderboard,
  Accomplishments
- **Broker**: Call Broker
- **Help**: Controls, Quick Start, Manual, Report Bug, About

### World view

The world view can show the map at four zoom levels:

- Local detailed: 30 × 30 pixels per tile.
- Local: 15 × 15 pixels per tile.
- Network: scaled so that all of the player's stations are visible. It shows trains,
  stations, and track, but not geography.
- Regional: scaled so that the whole map is visible. The mini map is hidden.

### Cursor

The map cursor is visible only while the world view has keyboard focus, and its
appearance shows its current mode. It starts at the top-left tile. When a game is loaded
or a new game is started on a map of the same size as the previous one, the cursor keeps
its previous position.

- **Place station mode** starts when the player selects a station type on the build tab.
  The cursor shows the radius of the selected station type. It is red when the station
  cannot be built on the tile and white otherwise; this reflects whether building is
  actually possible, not merely whether the tile has track. Clicking the left mouse
  button tries to place the station. If the station is built, the cursor returns to its
  previous mode; otherwise it stays in place station mode. The right mouse button or
  Esc cancels and returns to the previous mode.
- **Build track mode**: dragging with the left mouse button shows the proposed track,
  and releasing the button builds it. The right mouse button or Esc cancels the proposed
  track. Track can also be built by moving the cursor with the number pad keys; control
  and the arrow keys only move the cursor.
- **Upgrade track mode** and **remove track mode**: dragging with the left mouse button
  shows the proposed change, and releasing the button applies it. In these modes the
  keyboard only moves the cursor: only building track is done with the keyboard, so a
  cursor move never upgrades or removes track by accident.
- **Inspect mode** shows information about the tile under the cursor. The cursor moves
  without building, upgrading, or removing anything.

### Train roster tab

The train roster shows each train's wagons and whether they are full or empty, together
with the train's relative speed and destination. Double-clicking a train in the roster
or on the map, or pressing Enter while the roster has focus, opens its train report.

### Build tab

The build tab is a panel of icon buttons arranged in rows: build mode, track type,
bridges, tunnels, and stations. The track types are standard and double track; the
bridges are wooden trestle, steel girder, and stone masonry; the stations are depot,
station, and terminal. The bridge and tunnel rows each have a "none" button. There are
five build modes:

- **Build track** (shows track type, bridges, and tunnels): dragging the mouse, or moving
  the cursor with the number pad, builds track: the selected track type on clear terrain,
  the selected bridge type over rivers (if one is selected), and a tunnel through hills and
  mountains (if tunnels are selected).
- **Upgrade track** (shows track type and bridges): dragging the mouse upgrades track and
  bridges along the path to the selected types. The keyboard only moves the cursor.
- **Build station** (shows stations): selecting a station type enters place station mode.
- **Bulldoze** (shows no options): dragging the mouse removes the track along the path. The
  keyboard only moves the cursor.
- **Inspect** (shows no options): nothing is built, upgraded, or removed when the cursor
  moves.

Clicking the build tab must not take keyboard focus away from the world view, because
that would interrupt building track with the keyboard. When a game is started or loaded,
the build mode defaults to build track, with standard track, wooden trestle bridges, and
tunnels selected.

### Broker

The broker screen has a bonds part and a stocks part. For the player's company it shows
the company name, a portrait of its president, net worth, cash, and outstanding bonds,
with actions to issue or repay a bond. It also shows the stock price per share and how
the shares are held: by the public, in the company's treasury, and by each other player.
Actions next to the holdings buy or sell shares. The same overview (net worth, cash,
loans, share price, and holdings) is shown for every other railroad.

### Station report

The station report has three tabs:

- **Supply and demand** lists each cargo type with whether the station will pay for it
  (that is, whether it is demanded), how much the station supplies in cars per year,
  and how many car loads are waiting for pickup. Supply and waiting cargo are shown as
  rows of wagon icons.
- **Trains** lists every train scheduled to stop at the station, with one row per
  scheduled stop, so a train that stops several times has several rows. The columns
  are the train, the station it comes from, the wagons it delivers, the wagons it picks
  up (or "automatic" or "no change"), and the station it goes to next.
- **Improvements** shows the improvements built at the station and lets the player buy
  more.

### Station list

The station list shows a summary of each station: name, type, cargo waiting, and revenue
this year.

### Train report

The train report shows the train's current consist as a picture of the engine and wagons,
followed by a summary of its cargo, for example "Mail (27); Passengers (72)". Below it is
the schedule: each stop names the station and its consist instruction (for example
"select wagons automatically"), and an arrow marks the next stop. Buttons add a station
or add priority orders. "Last" and "next" buttons step through the player's trains, and
further buttons open the train list or close the report.

### Train list

The train list shows each of the player's trains as a picture of its engine and wagons.
Selecting a train and choosing "show details" opens its train report.

### Select station

When choosing a station for a schedule stop, the screen shows the player's stations on
a simplified map as small squares labeled with their stop numbers; a station used by
several stops lists all of them, such as "2, 3". The stop being edited is named in the
title (for example "Stop 3"), and the route between stops is drawn as a dotted line. A
side panel shows the station under the cursor or selection: its name, the cargo waiting
there with quantities, and the cargo it demands. The station can be changed with the
mouse or number pad and accepted with a click or Enter.

### Newspaper

Important events are announced on a newspaper page laid over the game: a "News Weekly"
masthead, a large headline, and a "click to continue" button.

### Leaderboard

The leaderboard lists every player, ordered by net worth, with the player's name, net
worth, and number of stations.

### Balance sheet

The balance sheet for the current year has a total column and a year-to-date change
column. It lists the assets (operating funds, track, stations, rolling stock, and
industries), the liabilities (outstanding loans and stockholders' equity), and profit.

### Income statement

The income statement for the current year has a year-to-date column and a total column.
It lists revenues by category (mail, passengers, fast freight, slow freight, bulk freight,
and other income), expenses (interest and fees, train maintenance, track maintenance, and
station maintenance), and profit.

### Net worth graph

The net worth graph plots each player's net worth over time, one line per player with a
legend. The time axis spans the whole period of the game, for example 1840 to 1940.

### Cargo chart

The cargo chart shows where each cargo type is supplied and demanded. Each row has:

- the raw materials needed, if any,
- the source of supply,
- the supply rate in cars per year,
- the cargo type,
- the sources of demand, with the number of tiles needed to create demand,
- the cargo that is produced from it, if any.

For example, a refinery turns oil into petroleum, which a factory demands to produce
export goods. Manufactured goods are supplied by harbors and demanded by cities (two
tiles needed) or villages (four tiles needed). The chart can be printed.

### Report bug dialog

The report bug dialog is available from the Help menu and is also shown after an
unexpected error. It explains how to report a bug in the project's issue tracker:
include the steps to reproduce it (with a saved game if helpful), what was expected,
and what happened instead (with a screenshot if helpful). It shows diagnostic details
that can be copied to the clipboard: operating system and version, Java VM and version,
and FreeRails version and build. After an unexpected error it also suggests a bug title
of the form "<exception type> at <file> line <line number>" and includes the stack trace.

### Load game

The load game dialog lists the saved games available to the server, so in a network game
the server's saved games are shown. All players, not just the host, can open it. "OK" is
enabled only when a game is selected and loads that game, "Cancel" closes the dialog, and
"Refresh" updates the list after files were added, removed, or renamed.

### Launcher

The launcher guides the player through a series of panels. Fields are remembered from the
last time the launcher was run.

**Panel 1: game type.** The choice determines the next panel:

- **Single player**: select map, without a server port field.
- **Start a network game**: select map, with a server port field.
- **Join a network game**: client details, with remote server fields.
- **Server only**: select map, with a server port field.

**Panel 2: select map and server details.** The player starts a new map or loads a saved
game. The server port defaults to 55000. The game type determines the next panel:

- **Single player**: client details, without remote server fields.
- **Start a network game**: client details, without remote server fields.
- **Server only**: connected players.

The panel checks its input as follows:

- No saved game is available: "Load a saved game" is disabled. Checked when the panel is
  created.
- The port is not a number from 0 to 65535: "A valid port value is between 0 and
  65535."; "Next" is disabled. Checked as text is entered.
- "Start a new map" is selected but no map is: "Select a map"; "Next" is disabled. Checked
  when the selection changes.
- The server cannot start on the port: the error message is shown; "Next" stays enabled.
  Checked when "Next" is pressed.

**Panel 3: client details and remote server details.** The player name defaults to the
operating-system user name. When a saved game is loaded, the name field is replaced by a
list of the players in that game. For a remote server, the address defaults to 127.0.0.1
and the port to 55000. The game type determines the next panel:

- **Single player**: progress.
- **Start a network game**: connected players.
- **Join a network game**: progress.

The panel checks its input as follows:

- The player name is empty: "Enter a player name"; "Next" is disabled. Checked as text is
  entered.
- The port is not a number from 0 to 65535: "A valid port value is between 0 and
  65535."; "Next" is disabled. Checked as text is entered.
- Full screen is selected but no display mode is: "Select a display mode"; "Next" is
  disabled. Checked when the selection changes.
- The server address is empty: "Enter the address of the server"; "Next" is disabled.
  Checked as text is entered.
- The host cannot be resolved: "Can't resolve host." Checked when "Next" is pressed.
- The server cannot be reached: "Can't connect to server." Checked when "Next" is pressed.
- The remote server has loaded a saved game, and the player name is not in it: "New
  players can't join a saved game." Checked when "Next" is pressed.
- The remote server's game has already started: "New players can't join a game in
  progress." Checked when "Next" is pressed.

**Panel 4: connected players.** The host sees the players who have connected before
starting the game.

**Panel 5: progress.** A picture and a progress bar are shown while the game is
prepared, with "Back" and "Start" buttons.
