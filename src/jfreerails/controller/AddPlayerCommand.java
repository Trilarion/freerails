package jfreerails.controller;

import jfreerails.world.player.Player;


/**
 * Sent by the client to request that a player be added to the world.
 * @author rob
 * TODO server specific password
 */
public class AddPlayerCommand extends ServerCommand {
    private final Player player;
    private final byte[] signature;

    /**
     * @param signature signature of the Player object with the client's
     * private key
     */
    public AddPlayerCommand(Player p, byte[] signature) {
        player = p;
        this.signature = signature;
    }

    public Player getPlayer() {
        return player;
    }

    public byte[] getSignature() {
        return signature;
    }
}