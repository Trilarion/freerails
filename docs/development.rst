***********
Development
***********

Brief overview of the architecture
----------------------------------

The model and the view are separated. The classes that make up the model, referred to as the game world, are in
the freerails.world.* packages. The view classes are in the freerails.client.* packages.
The state of game world is stored by an instance of a class implementing the interface World.
This class is mutable - one can think of it as a specialised hashmap. All the objects that represent entities in the
world - values in the hashmap - are immutable. It follows that changes to the gameworld involve adding, removing or
replacing objects representing entities rather than changing their properties.

The client and server are separate. They communicate by sending objects to each other. This is done either by placing
objects on a queue or by sending serialized objects over a network connection. All objects passed between the client
and server are immutable and are instances of one of the following:

- Message2Server
- Message2Client
- MessageStatus
- PreMove
- PreMoveStatus
- Move
- MoveStatus

When a new game starts or a game is loaded, the server sends the client a copy of the World object (using an instance
of SetWorldMessage2Client) All changes to the game world that occur after the game has started, referred to as moves,
are done using the classes in the package jfreerails.move. Moves are either obtained from a PreMove object or
constructed directly.
