package jfreerails.client.common;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Implements a portable MouseAdapter which can be used generically across many
 * platforms. This is useful e.g. on systems such as Mac which handle context
 * menus differently.
 */
public abstract class PortablePopupAdapter extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
	if (e.isPopupTrigger()) {
	    triggerPopup(e);
	}
    }

    public void mousePressed(MouseEvent e) {
	if (e.isPopupTrigger()) {
	    triggerPopup(e);
	}
    }

    public void mouseReleased(MouseEvent e) {
	if (e.isPopupTrigger()) {
	    triggerPopup(e);
	}
    }

    /**
     * Subclasses implement this to listen for popup trigger
     */
    public abstract void triggerPopup(MouseEvent e);

}
