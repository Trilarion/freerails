package jfreerails.client.common;


/**
 * This interface allows messages to be sent to the user (not debug messages).
 * TODO implement a better message logger than the default (i.e. one with a nice
 * GUI front-end).
 * TODO future functionality could involve in-game chat, different colours,
 * smilies etc. For now just have simple text messages.
 */
public interface UserMessageLogger {
    /**
     * prints a message and terminates it with a newline
     */
    public void println(String s);

    /**
            * prints an 'important message' that will stay in the middle of the screen
            * until hide function is called.
            * In this version it is used to display message when the game is paused,
            * but later there should be also 'Game is loading' or 'Connection is lost' messages
            * since that should be there longer then only predefined value <code>displayMessageUntil</code>.
            */
    public void showMessage(String msg);

    /**
         * Hides currently displayed message.
         */
    public void hideMessage();
}