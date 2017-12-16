package freerails.server;

import freerails.network.MoveReceiver;

import java.io.Serializable;

/**
 * This interface is implemented by objects which are responsible for updating
 * the game world. They are serialized when the game is saved. They are internal
 * clients of the ServerGameEngine and need to be initialised with a connection
 * to the game when deserialized.
 *
 * @author rob
 */
public interface ServerAutomaton extends Serializable {
    /**
     * Initializes the automaton with a connection to the MoveExecuter.
     */
    public void initAutomaton(MoveReceiver mr);
}