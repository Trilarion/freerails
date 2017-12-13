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
/*
 * StatsJPanel.java
 *
 * Created on 03 August 2004, 21:33
 */

package org.railz.client.view;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.railz.client.model.*;
import org.railz.util.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.player.Statistic.DataPoint;
import org.railz.world.top.*;
/**
 *
 * @author  rtuck99@users.berlios.de
 */
final class StatsJPanel extends javax.swing.JPanel {
    private ModelRoot modelRoot;
    private ObjectKey statisticKey;
    
    private class LegendTableModel extends AbstractTableModel {
	public int getRowCount() {
	    NonNullElements i = new NonNullElements(KEY.PLAYERS,
		    modelRoot.getWorld(), Player.AUTHORITATIVE);
	    int j = 0;
	    while (i.next())
		j++;
	    return j;
	}

	public Class getColumnClass(int columnIndex) {
	    switch (columnIndex) {
		case 0:
		    return String.class;
		case 1:
		    return Player.class;
		default:
		    throw new IllegalArgumentException();
	    }
	}

	public int getColumnCount() {
	    return 2;
	}

	private final String columnNames[] = new String[] {
	    Resources.get("Player"),
	    Resources.get("Key")
	};

	public String getColumnName(int column) {
	    return columnNames[column];
	} 

	public Object getValueAt(int row, int col) {
	    NonNullElements i = new NonNullElements(KEY.PLAYERS,
		    modelRoot.getWorld(), Player.AUTHORITATIVE);
	    int j = 0;
	    while (i.next() && j < row)
		j++;
	    if (j != row)
		return null;

	    switch(col) {
		case 0:
		    return ((Player) i.getElement()).getName();
		case 1:
		    return i.getElement();
		default:
		    throw new IllegalArgumentException();
	    }
	}
    }

    private class ColorBlock extends JPanel {
	public Color color;
	public void paintComponent(Graphics g) {
	    Graphics gg = g.create();
	    gg.setColor(color);
	    gg.fillRect(0, 0, getWidth(), getHeight());
	    gg.dispose();
	}

	private ColorBlock() {
	    setPreferredSize(new Dimension(35, 12));
	}
    }

    private class LegendCellRenderer implements TableCellRenderer {
	private ColorBlock colorBlock = new ColorBlock();
	private DefaultTableCellRenderer dtcr = new
	    DefaultTableCellRenderer();

	public Component getTableCellRendererComponent(JTable table, Object
		value, boolean isSelected, boolean hasFocus, int row, int
		column) {
	    if (column == 0) {
		return dtcr.getTableCellRendererComponent(table, (String)
			    value, isSelected, hasFocus, row, column);
	    } else {
		NonNullElements i = new NonNullElements(KEY.PLAYERS,
			modelRoot.getWorld(), Player.AUTHORITATIVE);
		int j = 0;
		while (i.next() && ! ((Player)
			    i.getElement()).equals((Player) value))
		    j++;
		colorBlock.color = colors[j % colors.length];
		return colorBlock;
	    }	
	}
    }
    
    private final Color[] colors = new Color[] {
	Color.BLACK,
	Color.BLUE,
	Color.CYAN,
	Color.GREEN,
	Color.MAGENTA,
	Color.ORANGE,
	Color.PINK,
	Color.RED,
	Color.YELLOW,
	Color.WHITE,
	Color.DARK_GRAY
    };

    private class StatChart extends JComponent {
	public StatChart() {
	    setPreferredSize(new Dimension(250, 200));
	}

	public void drawPoint(Graphics g, int x, int y) {
	    final int X_RADIUS = 2;
	    g.drawLine(x - X_RADIUS, y - X_RADIUS, x + X_RADIUS, y + X_RADIUS);
	    g.drawLine(x - X_RADIUS, y + X_RADIUS, x + X_RADIUS, y -
		    X_RADIUS);
	}

	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Graphics2D graphics = (Graphics2D) g.create();
	    int width = getWidth();
	    int height = getHeight();
	    /* size of current font */
	    int fontHeight = graphics.getFontMetrics().getHeight();
	    /* calculate x and y axis lengths */
	    int xmin = 3 * fontHeight;
	    int ymax = height - 3 * fontHeight;
	    int xmax = width - fontHeight;
	    int ymin = fontHeight;
	    /* draw axes */
	    graphics.drawLine(xmin, ymin, xmin, ymax);
	    graphics.drawLine(xmin, ymax, xmax, ymax);
	    /* draw small ticks */
	    int tickLength = 4;
	    graphics.drawLine(xmin, ymin, xmin - tickLength, ymin);
	    graphics.drawLine(xmin, ymax, xmin - tickLength, ymax);
	    graphics.drawLine(xmin, ymax, xmin, ymax + tickLength);
	    graphics.drawLine(xmax, ymax, xmax, ymax + tickLength);

	    int dataXMin, dataXMax, dataYMin, dataYMax;
	    dataXMin = dataYMin = Integer.MAX_VALUE;
	    dataXMax = dataYMax = Integer.MIN_VALUE;

	    Statistic s = null;
	    NonNullElements players = new NonNullElements(KEY.PLAYERS,
		    modelRoot.getWorld(), Player.AUTHORITATIVE);
	    while (players.next()) {
		/* calculate data point min and max values */
		s = (Statistic) modelRoot.getWorld().get(statisticKey.key,
		       	statisticKey.index, (FreerailsPrincipal) ((Player)
			    players.getElement()).getPrincipal());
		ArrayList data = s.getData();
		if (data.isEmpty()) {
		    /* TODO put something sensible in the middle and exit */
		    return;
		}
		for (int i = 0; i < data.size(); i++) {
		    DataPoint dp = (DataPoint) data.get(i);
		    int x = dp.time.getTime();
		    if (x < dataXMin)
			dataXMin = x;
		    if (x > dataXMax)
			dataXMax = x;
		    if (dp.y < dataYMin)
			dataYMin = dp.y;
		    if (dp.y > dataYMax)
			dataYMax = dp.y;
		}
	    }
	    dataYMin -= dataYMin % 10;
	    dataYMax += 10 - (dataYMax % 10);
	    
	    /* convert min and max from time-ticks to years */
	    GameCalendar gameCal = (GameCalendar) 
		modelRoot.getWorld().get(ITEM.CALENDAR,
		       	modelRoot.getPlayerPrincipal());
	    GregorianCalendar calMin = gameCal.getCalendar(new
		    GameTime(dataXMin));
	    GregorianCalendar calMax = gameCal.getCalendar(new
		    GameTime(dataXMax));
	    calMin = new GregorianCalendar(calMin.get(Calendar.YEAR), 0, 1);
	    calMax = new GregorianCalendar(calMax.get(Calendar.YEAR) + 1, 0,
		    1);

	    dataXMin = gameCal.getTimeFromCalendar(calMin).getTime();
	    dataXMax = gameCal.getTimeFromCalendar(calMax).getTime();

	    String minXString = String.valueOf(calMin.get(Calendar.YEAR));
	    String maxXString = String.valueOf(calMax.get(Calendar.YEAR));

	    /* paint min and max values */
	    TextLayout tl = new TextLayout(minXString,
		    graphics.getFont(), ((Graphics2D) graphics)
		    .getFontRenderContext());
	    tl.draw(graphics, xmin, ymax +
		    graphics.getFontMetrics().getHeight());
	    tl = new TextLayout(maxXString,
		    graphics.getFont(), graphics.getFontRenderContext());
	    Rectangle2D r = tl.getBounds();
	    tl.draw(graphics, (float) (xmax - r.getWidth()), ymax +
		    graphics.getFontMetrics().getHeight());

	    tl = new TextLayout("Year", graphics.getFont(), 
			graphics.getFontRenderContext());
	    r = tl.getBounds();
	    tl.draw(graphics, (int) (xmax + xmin - r.getWidth()) / 2,
		    (int) (ymax + graphics.getFontMetrics().getHeight()));
	    /* clockwise 90 degree rotation  */
	    AffineTransform at = AffineTransform.getRotateInstance(Math.PI /
		    2);
	    tl = new TextLayout(String.valueOf(dataYMax),
		    graphics.getFont(), graphics.getFontRenderContext());
	    r = tl.getBounds();
	    Image image = StatsJPanel.this.createImage
		((int) r.getWidth(), (int) r.getHeight() + 2);
	    Graphics2D imgGraphics = (Graphics2D) image.getGraphics();
	    tl.draw(imgGraphics, 0, tl.getAscent());
	    graphics.translate(xmin - (int) graphics.getFontMetrics().getHeight(),
		    ymin);
	    graphics.drawImage(image, at, null);
	    graphics.translate((int) graphics.getFontMetrics().getHeight()
		    - xmin, -ymin);
	    image.flush();
	    imgGraphics.dispose();

	    tl = new TextLayout(String.valueOf(dataYMin), graphics.getFont(),
		    graphics.getFontRenderContext());
	    r = tl.getBounds();
	    
	    image = StatsJPanel.this.createImage((int) r.getWidth(),
		   (int) r.getHeight() + 2);
	    imgGraphics = (Graphics2D) image.getGraphics();
	    tl.draw(imgGraphics, 0, tl.getAscent());
	    graphics.translate(xmin - graphics.getFontMetrics().getHeight(),
		    ymax - (int) r.getWidth());
	    graphics.drawImage(image, at, null);
	    graphics.translate(graphics.getFontMetrics().getHeight() - xmin,
		    (int) r.getWidth() - ymax);
	    image.flush();
	    imgGraphics.dispose();

	    tl = new TextLayout(Resources.get(s.getYUnit()),
		    graphics.getFont(), graphics.getFontRenderContext());
	    r = tl.getBounds();
	    image = StatsJPanel.this.createImage((int) r.getWidth(), (int)
		    r.getHeight() + 2);
	    imgGraphics = (Graphics2D) image.getGraphics();
	    tl.draw(imgGraphics, 0, tl.getAscent());
	    graphics.translate(xmin - graphics.getFontMetrics().getHeight(),
		    ymin + (ymax - ymin - (int) r.getWidth()) / 2);
	    graphics.drawImage(image, at, null);
	    graphics.translate(graphics.getFontMetrics().getHeight() - xmin,
		    ((int) r.getWidth() - ymax + ymin) / 2 - ymin);
	    image.flush();
	    imgGraphics.dispose();

	    /* draw points */
	    int oldDataX = 0, oldDataY = 0;
	    players = new NonNullElements(KEY.PLAYERS,
		    modelRoot.getWorld(), Player.AUTHORITATIVE);
	    while (players.next()) {
		graphics.setColor(colors[players.getIndex() % colors.length]);
		s = (Statistic) modelRoot.getWorld().get(statisticKey.key,
		       	statisticKey.index, (FreerailsPrincipal) ((Player)
			    players.getElement()).getPrincipal());
		ArrayList data = s.getData();
		for (int i = 0; i < data.size(); i++) {
		    DataPoint dp = (DataPoint) data.get(i);
		    int dataX = xmin + (xmax - xmin) * (dp.time.getTime() -
			    dataXMin) / (dataXMax - dataXMin);
		    int dataY = ymax - (ymax - ymin) * (dp.y - dataYMin) /
			(dataYMax - dataYMin);
		    drawPoint(graphics, dataX, dataY);
		    if (i > 0) {
			graphics.drawLine(dataX, dataY, oldDataX, oldDataY);
		    }
		    oldDataX = dataX;
		    oldDataY = dataY;
		}
	    }
	    graphics.dispose();
	}
    }

    /** Creates new form StatsJPanel */
    public StatsJPanel(ModelRoot mr) {
	modelRoot = mr;
        initComponents();
	add(new StatChart(), BorderLayout.CENTER);
	NonNullElements i = new NonNullElements(KEY.STATISTICS,
		modelRoot.getWorld(), modelRoot.getPlayerPrincipal());
	if (!i.next())
	    throw new IllegalStateException();

	statisticKey = new ObjectKey(KEY.STATISTICS,
		modelRoot.getPlayerPrincipal(), i.getIndex());

	statisticsJComboBox.setModel(statisticsCBModel);
	jTable1.setModel(new LegendTableModel());
	jTable1.setDefaultRenderer(String.class, new LegendCellRenderer());
	jTable1.setDefaultRenderer(Player.class, new LegendCellRenderer());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        statisticsJComboBox = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTable1 = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        jLabel1.setText(org.railz.util.Resources.get("Show statistic:"));
        jPanel1.add(jLabel1);

        jPanel1.add(statisticsJComboBox);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(org.railz.util.Resources.get("Key"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jLabel2, gridBagConstraints);

        jTable1.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jTable1, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.EAST);

    }//GEN-END:initComponents
    
    private StatisticsCBModel statisticsCBModel = new StatisticsCBModel();
    
    private class StatisticsCBModel implements ComboBoxModel {
	private Statistic getStatistic() {
	    return (Statistic) modelRoot.getWorld().get(statisticKey.key,
		    statisticKey.index, statisticKey.principal);
	}

	public Object getSelectedItem() {
	    return Resources.get(getStatistic().getName());
	}

	public void setSelectedItem(Object anItem) {
	    String itemName = (String) anItem;
	    NonNullElements i = new NonNullElements(KEY.STATISTICS,
		    modelRoot.getWorld(), modelRoot.getPlayerPrincipal());
	    while (i.next()) {
		Statistic s = (Statistic) i.getElement();
		if (itemName.equals(Resources.get(s.getName()))) {
		    statisticKey = new ObjectKey(KEY.STATISTICS,
			    modelRoot.getPlayerPrincipal(), i.getIndex());
		    StatsJPanel.this.repaint();
		    return;
		}
	    }
	}

	public void addListDataListener(ListDataListener l) {
	}

	public Object getElementAt(int index) {
	    NonNullElements i = new NonNullElements(KEY.STATISTICS,
		    modelRoot.getWorld(), modelRoot.getPlayerPrincipal());
	    int j = 0;
	    while (i.next() && j < index) {
		j++;
	    }
	    if (j != index)
		return null;
	    Statistic s = (Statistic) i.getElement();
	    return Resources.get(s.getName());
	}

	public void removeListDataListener(ListDataListener l) {
	}

	public int getSize() {
	    NonNullElements i = new NonNullElements(KEY.STATISTICS,
		    modelRoot.getWorld(), modelRoot.getPlayerPrincipal());
	    int j = 0;
	    while (i.next())
		j++;
	    return j;
	}
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTable jTable1;
    private javax.swing.JComboBox statisticsJComboBox;
    // End of variables declaration//GEN-END:variables
    
}
