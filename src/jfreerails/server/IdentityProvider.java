package jfreerails.server;

import java.util.HashMap;
import jfreerails.controller.ConnectionToServer;
import jfreerails.move.AddPlayerMove;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;


/**
 * Provides a method by which a Principal may be obtained
 */
class IdentityProvider {
    private final AuthoritativeMoveExecuter moveExecuter;

    /**
     * A HashMap in which the keys are instances of ConnectionToServer and the
     * values are FreerailsPrincipals.
     */
    private HashMap principals = new HashMap();
    private ServerGameEngine serverGameEngine;

    public IdentityProvider(ServerGameEngine s, AuthoritativeMoveExecuter me) {
        serverGameEngine = s;
        moveExecuter = me;
    }

    /**
     * TODO deprecate strings in favour of certificates
     * XXX currently we only support a single player per connection.
     * @param player the player trying to connect
     * @return true if the connection was successfully identified with the
     * specified player
     */
    public synchronized boolean addConnection(ConnectionToServer c,
        Player player, byte[] signature) {
        System.err.println("Authenticating player " + player.getName());

        World w = serverGameEngine.getWorld();

        /* determine whether this identity already exists */
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            Player p = w.getPlayer(i);

            if (p.equals(player)) {
                /* this player already exists */
                /* is this identity already connected ? */
                if (principals.containsValue(p)) {
                    System.err.println("Player " + p.getName() + " is already" +
                        " connected");

                    return false;
                }

                /* is this player the same as the one which previously
                 * connected under the same name? */
                System.out.println("Verifying player " + p + " with " + player);

                if (!p.verify(player, signature)) {
                    System.err.println("Couldn't verify signature of player " +
                        p.getName());

                    return false;
                }

                principals.put(c, p);

                return true;
            }
        }

        /* this player does not already exist */
        System.err.println("Adding player " + player.getName() + " to " +
            serverGameEngine.getWorld());

        AddPlayerMove m = AddPlayerMove.generateMove(serverGameEngine.getWorld(),
                player);

        /* TODO
        moveExecuter.processMove(m, m.getPrincipal());
        */
        moveExecuter.processMove(m);

        /*
         * get the newly created player-with-principal
         */
        System.err.println("checking " + w);
        player = w.getPlayer(w.getNumberOfPlayers() - 1);
        assert (w != null);

        principals.put(c, player);

        return true;
    }

    /**
     * Dissociate all players with this connection
     */
    public synchronized void removeConnection(ConnectionToServer c) {
        principals.remove(c);
    }

    public synchronized FreerailsPrincipal getPrincipal(ConnectionToServer c) {
        Player p;

        if (c == null) {
            /* No connection implies the principal is the server itself */
            return Player.AUTHORITATIVE;
        }

        if ((p = (Player)principals.get(c)) == null) {
            return Player.NOBODY;
        }

        return p.getPrincipal();
    }

    public synchronized Player getPlayer(FreerailsPrincipal p) {
        World w = serverGameEngine.getWorld();

        /* determine whether this identity already exists */
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            Player player = w.getPlayer(i);

            if (player.getPrincipal().equals(p)) {
                return player;
            }
        }

        return null;
    }
}