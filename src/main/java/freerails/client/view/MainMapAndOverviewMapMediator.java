/*
 * MainMapAndOverviewMapMediator.java
 *
 * Created on 24 June 2002, 21:04
 */
package freerails.client.view;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * This class mediates between the main map view and the overview map view. It
 * does the following:<br>
 * (1) Updates the rectangle on the overview map when the visible rectangle of
 * the main map changes.<br>
 * (2) Updates the main map visible rectangle when the user clicks on the
 * overview map.<br>
 * (3) Updates the main map visible rectangle when the user drags the rectangle
 * on the overview map.<br>
 * (4) Changes the mouse cursor to indicate that the rectangle on the overview
 * map is draggable when the mouse moves into the rectangle.
 *
 * @author Luke Lindsay
 * @version 1.0
 */
public class MainMapAndOverviewMapMediator extends MouseInputAdapter {
    private JComponent overviewMapJPanel;

    private JComponent mainMap;

    private Rectangle currentVisRect;

    private Point lastMouseLocation = new Point();

    private boolean inside = false;

    private boolean draggingAndStartedInside = false;

    public MainMapAndOverviewMapMediator() {
    }

    public MainMapAndOverviewMapMediator(JComponent omv, JViewport v,
                                         JComponent mm, Rectangle rect) {
        setup(omv, v, mm, rect);
    }

    public void setup(JComponent omv, JViewport v, JComponent mm, Rectangle rect) {
        currentVisRect = rect;

        overviewMapJPanel = omv;
        JViewport viewport = v;
        mainMap = mm;

        overviewMapJPanel.addMouseMotionListener(this);
        overviewMapJPanel.addMouseListener(this);
        viewport.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateObservedRect();
            }
        });

        overviewMapJPanel
                .addComponentListener(new java.awt.event.ComponentAdapter() {
                    @Override
                    public void componentResized(
                            java.awt.event.ComponentEvent evt) {
                        updateObservedRect();
                    }

                    @Override
                    public void componentShown(java.awt.event.ComponentEvent evt) {
                        updateObservedRect();
                    }
                });
    }

    @Override
    public void mouseMoved(MouseEvent evt) {
        lastMouseLocation.x = evt.getX();
        lastMouseLocation.y = evt.getY();
        updateInside(evt);
    }

    @Override
    public void mousePressed(MouseEvent evt) {
        if (inside) {
            draggingAndStartedInside = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        draggingAndStartedInside = false;
    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        if (draggingAndStartedInside) {
            /*
             * Rectangle r= overviewMapJPanel.mainMapVisibleRect;
             * r.x+=evt.getX()-lastMouseLocation.x;
             * r.y+=evt.getY()-lastMouseLocation.y;
             * lastMouseLocation.x=evt.getX(); lastMouseLocation.y=evt.getY();
             *
             * updateInside(evt); overviewMapJPanel.repaint();
             */
            int deltaX = evt.getX() - lastMouseLocation.x;
            int deltaY = evt.getY() - lastMouseLocation.y;
            lastMouseLocation.x = evt.getX();
            lastMouseLocation.y = evt.getY();

            // float overviewScale=overviewMapJPanel.getScale();
            // float mainMapScale=mainMap.getScale();
            int overviewScale = overviewMapJPanel.getPreferredSize().width;
            int mainMapScale = mainMap.getWidth();

            int scaledDeltaX = (deltaX * mainMapScale / overviewScale);
            int scaledDeltaY = (deltaY * mainMapScale / overviewScale);

            Rectangle r = mainMap.getVisibleRect();
            r.x += scaledDeltaX;
            r.y += scaledDeltaY;

            mainMap.scrollRectToVisible(r);
            updateInside(evt);
        }
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        /*
         * Rectangle r= overviewMapJPanel.mainMapVisibleRect;
         * r.x=evt.getX()-r.width/2; r.y=evt.getY()-r.width/2;
         */

        // float overviewScale=overviewMapJPanel.getScale();
        // float mainMapScale=mainMap.getScale();
        int overviewScale = overviewMapJPanel.getPreferredSize().width;
        int mainMapScale = mainMap.getWidth();

        int x = (evt.getX() * mainMapScale / overviewScale);
        int y = (evt.getY() * mainMapScale / overviewScale);

        Rectangle r = mainMap.getVisibleRect();
        r.x = x - r.width / 2;
        r.y = y - r.height / 2;
        mainMap.scrollRectToVisible(r);
        updateInside(evt);
    }

    private void updateInside(MouseEvent evt) {
        // Rectangle r= overviewMapJPanel.mainMapVisibleRect;
        boolean b = currentVisRect.contains(evt.getX(), evt.getY());

        if (b != inside) {
            inside = b;

            if (inside) {
                overviewMapJPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            } else {
                overviewMapJPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    private void updateObservedRect() {
        Rectangle r = mainMap.getVisibleRect();

        // if (!r.equals(this.currentVisRect)) {
        // float overviewScale=overviewMapJPanel.getScale();
        // float mainMapScale=mainMap.getScale();
        int overviewScale = overviewMapJPanel.getPreferredSize().width;
        int mainMapScale = mainMap.getWidth();

        if (0 != (overviewScale * mainMapScale)) {
            // avoid division by zero.
            currentVisRect.x = (r.x * overviewScale / mainMapScale);
            currentVisRect.y = (r.y * overviewScale / mainMapScale);
            currentVisRect.width = (r.width * overviewScale / mainMapScale);
            currentVisRect.height = (r.height * overviewScale / mainMapScale);
            overviewMapJPanel.repaint();
        }

        // }
    }
}