package jfreerails.controller;

import jfreerails.world.player.FreerailsPrincipal;


/**
 * Sent by the server to indicate that a request to add a player to the world
 * was rejected or accepted.
 */
public class AddPlayerResponseCommand extends ServerCommand {
    private AddPlayerCommand rejectedCommand;
    private boolean rejected;
    private String reason;
    private FreerailsPrincipal principal;

    /**
     * The request was accepted
     * @param p the principal that represents the added player
     */
    public AddPlayerResponseCommand(FreerailsPrincipal p) {
        rejected = false;
        principal = p;
    }

    /**
     * The request was rejected
     */
    public AddPlayerResponseCommand(AddPlayerCommand c, String reason) {
        rejectedCommand = c;
        this.reason = reason;
        rejected = true;
    }

    public FreerailsPrincipal getPrincipal() {
        return principal;
    }

    public AddPlayerCommand getRejectedCommand() {
        return rejectedCommand;
    }

    public String getReason() {
        return reason;
    }

    public boolean isRejected() {
        return rejected;
    }
}