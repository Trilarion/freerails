package jfreerails.client.model;

import java.awt.Point;

public interface MapCursor {

    /**
     *  A MapCursor that does nothing.
     */
    public static final MapCursor NULL_MAP_CURSOR = new MapCursor() {

	public void tryMoveCursor(Point tryThisPoint) {
	}

	public void addCursorEventListener(CursorEventListener l) {
	}

	public void setMessage(String message) {
	}
    };

    /**
     *  Moves the cursor provided the destination is a legal position.
     * @param tryThisPoint The cursor's destination.
     */
    public void tryMoveCursor(Point tryThisPoint);

    /**
     *  Adds a listener.  Listeners could include: the trackbuild system, the
     * view the cursor moves across, etc.
     * @param l The listener.
     */
    public void addCursorEventListener(CursorEventListener l);

    public void setMessage(String message);
}
