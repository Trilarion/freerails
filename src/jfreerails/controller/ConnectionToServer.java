package jfreerails.controller;

import jfreerails.world.top.World;

/**
 * This interface represents a connection between a server and a client. This class
 * should be subclassed to provide connections over different transport media.
 *
 * The connection is responsible for guaranteeing the delivery of moves across
 * the transport medium.
 *
 * TODO eventually this class will be simplified so that it's function is only
 * to send FreerailsSerializable objects across the connection. The ulitmate
 * goal is for this class not to implement MoveReceiver and to have a sendMove()
 * method or similar.
 *
 * @author lindsal
 */
public interface ConnectionToServer extends UncommittedMoveReceiver {
    public void addMoveReceiver(MoveReceiver moveReceiver);

    public void removeMoveReceiver(MoveReceiver moveReceiver);

    public World loadWorldFromServer();

    /**
     * close the connection to the remote peer
     */
    public void close();

    /**
     * connect to the remote peer
     */
    public void open();
    
    /*
     * TODO
     * proposed interface:
     *
     * public void send(FreerailsSerializable());
     */
}
