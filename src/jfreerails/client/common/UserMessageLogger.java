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
    public void println (String s);
}
