package freerails.client.launcher;

/**
 * Shares synchronization state between a JavaFX client and its gameplay window.
 */
final class JavaFxGameSession {

    private final Object updateLock = new Object();

    /**
     * Returns the monitor used to synchronize game, render, and input access.
     *
     * @return the shared update monitor
     */
    Object updateLock() {
        return updateLock;
    }
}
