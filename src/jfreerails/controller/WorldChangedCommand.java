package jfreerails.controller;


/**
 * This Event signifies that the world model in the server has been completely
 * changed (for example by loading a new copy) and that the client needs to
 * download a new copy.
 */
public class WorldChangedCommand extends ServerCommand {
}