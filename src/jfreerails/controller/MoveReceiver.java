package jfreerails.controller;

import jfreerails.move.Move;


public interface MoveReceiver {
    public void processMove(Move Move);
}