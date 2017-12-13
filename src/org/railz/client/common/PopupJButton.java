/*
 * Copyright (C) 2004 Robert Tuck
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.railz.client.common;

import java.awt.*;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.Component;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.border.BevelBorder;

/**
 * Implements a single-shot popup menu with scrollbars.
 * This class exists because the Apple JDK refuses to render JPopupMenus with
 * items taller than 1 text line.
 */
public class PopupJButton extends JButton {
    private ListModel listModel;
    private ListCellRenderer listCellRenderer;
    private ButtonPanel buttonPanel;
    private JList list;
    private JScrollPane scrollPane;
    private Popup popup;

    private FocusListener buttonFocusListener = new FocusListener() {
	public void focusGained(FocusEvent e) {
	    // do nothing
	}
	public void focusLost(FocusEvent e) {
	    Component c = e.getOppositeComponent();
	    if (! PopupJButton.this.isAncestorOf(c) && 
		    c != PopupJButton.this) {
		popup.hide();
		popup = null;
	    }
	}
    };

    private ActionListener buttonListener = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    System.out.println("action performed on popupjbutton");
	    if (listModel.getSize() > 0 && popup == null) {
		System.out.println("popped up button, listModel size is " +
			listModel.getSize());
		list.setSelectedIndices(new int[] {});
		list.setMinimumSize(new Dimension(getWidth(), 0));
		scrollPane.revalidate();
		Point p = PopupJButton.this.getLocationOnScreen();
		popup =
		    PopupFactory.getSharedInstance().getPopup(PopupJButton.this,
			    scrollPane, p.x,
			    p.y + PopupJButton.this.getHeight());
		popup.show();
	    }
	}
    };

    private ListSelectionListener selectionListener = new
	ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (list.getSelectedIndex() == -1)
		    return;

		PopupJButton.this.repaint();
		ActionEvent ae = new ActionEvent(PopupJButton.this,
			ActionEvent.ACTION_PERFORMED, null);
		if (popup != null) {
		    popup.hide();
		    popup = null;
		}
		synchronized(actionListeners) {
		    for (int i = 0; i < actionListeners.size(); i++) {
			((ActionListener)
			 actionListeners.get(i)).actionPerformed(ae);
		    }
		}
	    }
	};

    private class ButtonPanel extends JPanel implements Icon {
	public int getIconHeight() {
	    return getPreferredSize().height;
	}

	public int getIconWidth() {
	    return getPreferredSize().width;
	}

	public Dimension getPreferredSize() {
	    return listCellRenderer.getListCellRendererComponent(list, null,
		    -1, false, false).getPreferredSize();
	}

	public void paint(Graphics g) {
	    System.out.println("painting button");
	    listCellRenderer.getListCellRendererComponent(list, null, -1,
		    false, false).paint(g);
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
	    System.out.println("x = " + x + ", y = " + y);
	    g.translate(x, y);
	    paint(g);
	    g.setColor(java.awt.Color.BLACK);
	    g.drawRect(0, 0, getIconWidth(), getIconHeight());
	    g.translate(-x, -y);
	}
    }

    /**
     * @see JComboBox#getSelectedIndex()
     */
    public int getSelectedIndex() {
	return list.getSelectedIndex();
    }
    
    /**
     * @see JComboBox#getSelectedItem()
     */
    public Object getSelectedItem() {
	return list.getSelectedValue();
    }

    public PopupJButton(ListModel lm, ListCellRenderer lcr) {
	listCellRenderer = lcr;
	listModel = lm;
	list = new JList(lm);
	list.setCellRenderer(listCellRenderer);
	list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	if (listModel.getSize() > 0)
	    list.setSelectedIndex(0);
	list.addListSelectionListener(selectionListener);
	scrollPane = new JScrollPane(list,
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scrollPane.addFocusListener(buttonFocusListener);

	/*
	buttonPanel = new ButtonPanel();
	setIcon(buttonPanel);
	*/
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	setLayout(new GridBagLayout());

	add(listCellRenderer.getListCellRendererComponent(list, null, -1,
		    false, false), gbc);
	gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.VERTICAL;
	gbc.weighty = 1.0;
	gbc.gridx = 1;
	add(new JSeparator(JSeparator.VERTICAL), gbc);

	gbc = new GridBagConstraints();
	gbc.gridx = 2;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weighty = 1.0;
	add(new JLabel("V"));
	super.addActionListener(buttonListener);
    }

    private Vector actionListeners = new Vector();

    public void addActionListener(ActionListener l) {
	actionListeners.add(l);
    }

    public void removeActionListener(ActionListener l) {
	actionListeners.remove(l);
    }
}


