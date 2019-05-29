***********
Development
***********

.. figure:: /images/design_client_server_overview.png
   :width: 14 cm
   :align: center

   Client/Server design overview.

Brief overview of the architecture
----------------------------------

The model and the view are separated. The classes that make up the model, referred to as the game world, are in
the freerails.model.* packages. The view classes are in the freerails.view.* packages.
The state of game world is stored by an instance of a class implementing the interface World.
This class is mutable - one can think of it as a specialised hashmap. Changes to the game world involve adding, removing or
changing their properties.

The client and server are separate. They communicate by sending objects to each other. This is done by sending serialized
objects over a network connection.

When a new game starts or a game is loaded, the server sends the client a copy of the World object.  All changes to
the game world that occur after the game has started, referred to as moves, are done using the classes in the package
freerails.move.*.

Server - client communication
-----------------------------

The server is the entirety of computational processes that are concerned with evaluating the game mechanics and
distributing the game updates, which can be physically separated from the players.

The server knows logged in users and communicates with them via connections which can read and write serialized
Java objects over the internet. However, connections can get lost and reestablished, so the server needs to have
the ability to  re-associate a connection to a player and the player needs the ability to re-identify itself
within a new connection.

Solution: Identity is an uuid, given by the server to the client, together with a display name, that the client chooses.
Identification is by uuid. If there is a player on the server side without connection and a new connection presents the
UUID of this player, instead of creating a new player, the connection is re-associated.

Before that connections have to send their game version id and the version id has to be equal to the version id of the
server or the connection will be closed immediately.

Furthermore: Connections not presenting a UUID within certain a timeout or presenting something else are automatically closed.

For this we need handler, that actually process messages and allow to act on the various things as well as do something
in the future. (see also Java Executors https://stackoverflow.com/a/2258082/1536976)

Proposed structure of identify: Identity (UUID id, String name)

Identified players can send commands/message to the server and may receive responses.

Examples are: chat message (are echoed to all) , chat log request (returns with the last X chat messages), display
available inbuilt scenarios, display saved games ready for loading on the server, create random scenario with options,
start hosting a game (preparation phase), start game, stop game.

- ChatMessage: String content, String author, String date (certain format)
- SimpleMessage (Request): Enum type of request (chat log request, display available scenarios, )
- ChatLog: List<ChatMessage>
- Scenario/SaveGameInformation: String title, String description, Image preview
- RandomScenarioCreationOptions:
- ...

The server is represented with a list of <id, message> and processes them. The first player on the server to decide to
host a game becomes the game master. He decides when to start the game, when to stop it running on the server.
However, the players can safe the game locally as well (see later)

Stopping the server is done externally by the local client, not through messages. The local server starts at start of
the local client and stops when the local client stops.

When a server stops, the current running game is stopped, all connections are closed.

A game consists of a certain number of players and additional data, all contained in a model object. In a game the
players are either humans or AI. Access to a player may be restricted by password. Users can decide to use
heir uuid as password.

If no passwords are stored, the scenario is said to be "pure", otherwise it is said to be "restricted".
Pure scenarios can always become restricted. Restricted scenarios can only become pure if all participating human
players agree. Passwords are part of the game model.

Client options:

- Internal (program version acting as option version)
- Server limited to local connections (default: on, changes are reflected on next start)
- UUID (chosen at first start, may be set)
- Default password (chosen randomly at first start, may be set)
- Use default password (otherwise the player is prompted before sending)
- Start in windowed/fullscreen mode (changes are reflected on next start)
- Music mute (immediate, default: off)
- Music volume (immediate)

Implementation (stored as JSON as key-value pairs, or as properties maybe?)

During the game, only Chat Messages (not part of the game, chat messages not persistent when server stops) and stop
game messages (by game master).

The game has a status (running, paused). It pauses if the majority of players hit the pause button (message) or if a
player lost a connection. The game master determines the game speed (stored in the game model).

Everyone can send a Move, Moves are collected in a list in the order of arrival at the server. Every move is processed
sequentially and first tested for applicability. If so, it gets a running number (applied moves since start of game)
and is applied and sent to the client. The client then checks if the current running number is the successor of the
last received such move and applies it. If not, a Message (Invalid Move or so) is returned.

A Move should always encapsulates a single atomic game action for each player.

Move: void apply(World), Status applicable(Read-Only-World)

In case of inconsistencies every player can always send a request to obtain the whole world).

Track implementation
--------------------

The map consists of an rectangular grid consisting of square-sized tiles. Each tile (except those at the border) have
exactly eight neighbors which can be uniquely identified by compass points (north, north-east, ...) given the current
center tile or by grid positions (row, column).

The total track on the map consists of many track pieces, where each piece connects two neighboring tiles (diagonal
connections have ~1.41 (square-root of 2) times the length of horizontal or vertical connections).

Path finding of the trains works on a graph where the tiles are nodes and the track pieces connecting neighboring tiles
are the edges.

Building and removing track works by adding and removing track pieces. In particular, the planning of a longer piece
of newly built track is done by path finding again.

The track itself is visualized by rendering each tile according to its track configuration. The track configuration is
an 8 bit value indicating if there is a connecting to one of the 8 neighbors from the current tile. See the attached
image for some examples.

.. figure:: /images/design_track_configurations.png
   :width: 14 cm
   :align: center

   Track configurations.

The track configurations are not independent from each other. For every connection on a tile towards a neighboring tile,
this neighboring tile must also have a connection to this tile. This invariant must be obeyed by not allowing to change
track configurations directly, but only by allowing adding or removing of track pieces at a time.

Track pieces have no direction, any train can go on them both ways. However, a train can change direction at every tile
at most by 90 degree, effectively inducing some kind of directionality.

Track pieces can be single tracked or double tracked. There can only be one running train on each track piece
(and trains have a certain extent, also measured in track pieces). However, stopped trains do not count as obstacles.

The track configuration of a tile is sufficient to draw it uniquely on the tile. Not all possible 8 bit values are valid.

Bridges and stations are a special case. Stations have a orientation and only allow track parallel to their orientation.
Bridges span a water tile (other track cannot be put on water) and consist of two track pieces resulting in a parallel
configuration.

Computer controlled (AI) players
--------------------------------

Is built exactly like a client (communicates by Moves) but without UI or presenter. Lives locally on the server.
Shutdown automatically if the game ends.

Coding Guidelines
-----------------

- Follow package dependency rules (utils does only depend on java libraries, model only on utils, move only on model and utils, ..).
- Avoid circular dependencies between packages. I.e. if any classes in package A import classes from package B, then
  classes in package B should not import classes from package A.
- Run all unit tests after making changes to see whether anything has broken. You can do this using the check gradle target.
- Javadoc comments. Add a couple of sentences saying what the class does and the reason for its addition.
- Use the Code Formatter with care. Avoid reformatting whole files with different formatting schemes when you have only changed a few lines.
- Consider writing junit tests.
- Consider using assertions.
- Add //TODO comments if you spot possible problems in existing code.
- Use logging instead of System.out or System.err for debug messages. Each class should have its own logger, e.g.
  private static final Logger logger = Logger.getLogger(SomeClass.class.getName());

Reading

- Effective Java (http://java.sun.com/docs/books/effective/) (sample chapters online)
- User Interface Design for Programmers (http://www.joelonsoftware.com/uibook/chapters/fog0000000057.html) (available online)



