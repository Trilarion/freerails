/*
 * Created on Apr 17, 2004
 */
package jfreerails.controller.net;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.util.GameModel;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;


/**
 *  A client for FreerailsGameServer.
 *  @author Luke
 *
 */
public class FreerailsClient implements ClientControlInterface, GameModel {
    private static final Logger logger = Logger.getLogger(FreerailsClient.class.getName());
    private Connection2Server connection2Server;
    private final HashMap properties = new HashMap();
    private World world;

    /**
     * Connects this client to a remote server.
     */
    public LogOnResponse connect(String address, int port, String username,
        String password) {
        try {
            connection2Server = new InetConnection2Server(address, port);

            LogOnRequest request = new LogOnRequest(username, password);
            connection2Server.writeToServer(request);
            connection2Server.flush();

            LogOnResponse response = (LogOnResponse)connection2Server.waitForObjectFromServer();

            return response;
        } catch (Exception e) {
            try {
                connection2Server.disconnect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return LogOnResponse.rejected(e.getMessage());
        }
    }

    /**
     * Connects this client to a local server.
     */
    public LogOnResponse connect(NewGameServer server, String username,
        String password) {
        try {
            LogOnRequest request = new LogOnRequest(username, password);
            connection2Server = new NewLocalConnection();
            connection2Server.writeToServer(request);
            server.addConnection((NewLocalConnection)connection2Server);

            LogOnResponse response = (LogOnResponse)connection2Server.waitForObjectFromServer();

            return response;
        } catch (Exception e) {
            try {
                connection2Server.disconnect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return LogOnResponse.rejected(e.getMessage());
        }
    }

    /**
     * Disconnect the client from the server.
     */
    public void disconnect() {
        try {
            connection2Server.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setGameModel(FreerailsSerializable world) {
        this.world = (World)world;
    }

    public void setProperty(String propertyName, Serializable value) {
        properties.put(propertyName, value);
    }

    public Serializable getProperty(String propertyName) {
        return (Serializable)properties.get(propertyName);
    }

    public void resetProperties(HashMap newProperties) {
        // TODO Auto-generated method stub
    }

    public void showMenu() {
        // TODO Auto-generated method stub
    }

    FreerailsSerializable read() {
        try {
            return this.connection2Server.waitForObjectFromServer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        throw new IllegalStateException();
    }

    void write(FreerailsSerializable fs) {
        try {
            connection2Server.writeToServer(fs);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    /** Reads and deals with all outstanding messages from the server.*/
    public void update() {
        try {
            FreerailsSerializable[] messages = connection2Server.readFromServer();

            for (int i = 0; i < messages.length; i++) {
                FreerailsSerializable message = messages[i];
                processMessage(message);
            }

            connection2Server.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void processMessage(FreerailsSerializable message)
        throws IOException {
        if (message instanceof ClientCommand) {
            ClientCommand command = (ClientCommand)message;
            CommandStatus status = command.execute(this);
            logger.fine(command.toString());
            connection2Server.writeToServer(status);
        } else if (message instanceof Move) {
            Move m = (Move)message;
            MoveStatus status = m.doMove(world, Player.AUTHORITATIVE);

            if (!status.isOk()) {
                throw new IllegalStateException(status.message);
            }
        } else {
            logger.fine(message.toString());
        }
    }

    World getWorld() {
        return world;
    }
}