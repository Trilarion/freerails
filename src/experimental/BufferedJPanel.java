/*
 * Created on Mar 9, 2004
 */
package experimental;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import javax.swing.JPanel;
import jfreerails.client.common.RepaintManagerForActiveRendering;


/**
 * I thought this class might improve the frame rate but it seems to make things worse on my machine.
 *  @author Luke
 *
 */
public class BufferedJPanel extends JPanel {
    protected GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                              .getDefaultScreenDevice()
                                                                              .getDefaultConfiguration();
    private Image buffer;
    private Dimension bufferSize;
    boolean bufferNeedsPainting = true;
    private long lastEventsDispatched = 0;
    long bufferRepaints;
    long bufferUsed;
    long paints;

    protected void paintComponent(Graphics g) {
        if (null == buffer) {
            resetBuffer();
        }

        Dimension jPanelSize = this.getSize();
        assert (null != jPanelSize);
        assert (null != bufferSize);
        assert (null != buffer);

        if (bufferSize.width != jPanelSize.width ||
                bufferSize.height != bufferSize.height) {
            resetBuffer();
        }

        long repaintsRequested = RepaintManagerForActiveRendering.getNumRepaintRequests();

        if (lastEventsDispatched != repaintsRequested) {
            bufferNeedsPainting = true;
        }

        if (bufferNeedsPainting) {
            //Paint buffer.
            Graphics bufferG = buffer.getGraphics();
            super.paintComponent(bufferG);
            lastEventsDispatched = repaintsRequested;
            bufferNeedsPainting = false;
            bufferRepaints++;
        } else {
            bufferUsed++;
        }

        g.drawImage(buffer, 0, 0, null);
        paints++;

        if (paints == 200) {
            long percentageUsage = (bufferUsed * 100) / (bufferRepaints +
                bufferUsed);
            bufferRepaints = 0;
            bufferRepaints = 0;
            paints = 0;
            System.out.println(percentageUsage + "% buffer usage");
        }
    }

    private void resetBuffer() {
        bufferNeedsPainting = true;
        bufferSize = this.getSize();
        buffer = defaultConfiguration.createCompatibleVolatileImage(bufferSize.width,
                bufferSize.height);
    }
}