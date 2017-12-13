package jfreerails.controller;

import jfreerails.move.Move;


/**
 * Accepts moves with associated source information
 */
public interface SourcedMoveReceiver extends UncommittedMoveReceiver {
    public void processMove(Move move, ConnectionToServer source);
}