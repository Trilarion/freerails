package jfreerails.controller;

public interface UncommittedMoveReceiver extends MoveReceiver {
    public void undoLastMove();
}