/*
 * Created on Sep 4, 2004
 *
 */
package jfreerails.client.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jfreerails.client.renderer.RenderersRoot;
import jfreerails.controller.ModelRoot;
import jfreerails.controller.NetWorthCalculator;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.TransactionAggregator;

import org.apache.log4j.Logger;

/**
 * A JPanel that displays a graph of the net worth of each of the players
 * against time.
 * 
 * @author Luke
 * 
 */
public class NetWorthGraphJPanel extends JPanel implements View {

    private static final long serialVersionUID = 3618703010813980982L;

    private static final Logger logger = Logger
            .getLogger(NetWorthGraphJPanel.class.getName());

    private JLabel title = null;

    private JLabel yAxisLabel1 = null;

    private JLabel yAxisLabel3 = null;

    private JLabel yAxisLabel4 = null;

    private JLabel yAxisLabel2 = null;

    private JLabel xAxisLabel3 = null;

    private JLabel xAxisLabel2 = null;

    private JLabel xAxisLabel1 = null;

    private final Font FONT;

    private ArrayList<CompanyDetails> companies = new ArrayList<CompanyDetails>();

    private long scaleMax;

    private Rectangle graphRect = new Rectangle(44, 50, 380, 245);

    ActionListener submitButtonCallBack = null;

    /**
     * Stores the company details that are used to draw a line and title on the
     * graph.
     * 
     * @author Luke
     * 
     */
    static class CompanyDetails {

        /** The company's net worth at the end of each year. */
        long[] value = new long[100];

        /** The colour for the line on the graph. */
        final Color color;

        /** The company's name. */
        final String name;

        CompanyDetails(String n, Color c) {

            color = c;
            name = n;
            for (int i = 0; i < 100; i++) {
                value[i] = Integer.MIN_VALUE;
            }

        }
    }

    /**
     * This method initializes
     * 
     */
    public NetWorthGraphJPanel() {

        super();
        FONT = new java.awt.Font("Bookman Old Style", java.awt.Font.BOLD, 10);
        initialize();
        // companies.add(new CompanyDetails("Player 1", Color.BLUE));
        // companies.add(new CompanyDetails("Player 2", Color.GREEN));
        // companies.add(new CompanyDetails("Player 3", Color.CYAN));
        // for(int i = 0; i < companies.size(); i++){
        // CompanyDetails cd = (CompanyDetails)companies.get(i);
        // cd.fillWithRnadomData();
        // }
        //        
        // setAppropriateScale();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        yAxisLabel4 = new JLabel();
        yAxisLabel3 = new JLabel();
        yAxisLabel2 = new JLabel();
        xAxisLabel1 = new JLabel();
        xAxisLabel2 = new JLabel();
        xAxisLabel3 = new JLabel();
        title = new JLabel();
        yAxisLabel1 = new JLabel();

        this.setLayout(null);
        this.setBackground(java.awt.Color.white);
        this.setSize(444, 315);
        title.setText("Net Worth");
        title.setFont(new java.awt.Font("Bookman Old Style",
                java.awt.Font.PLAIN, 24));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setLocation(0, 0);
        title.setSize(444, 43);
        yAxisLabel1.setText("$25");
        yAxisLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        yAxisLabel3.setText("$999m");
        yAxisLabel4.setText("$999M");
        yAxisLabel4.setFont(new java.awt.Font("Bookman Old Style",
                java.awt.Font.BOLD, 10));
        yAxisLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        yAxisLabel3.setFont(new java.awt.Font("Bookman Old Style",
                java.awt.Font.BOLD, 10));
        yAxisLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        yAxisLabel2.setText("$50");
        yAxisLabel2.setLocation(0, 167);
        yAxisLabel2.setSize(40, 16);
        yAxisLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        yAxisLabel2.setFont(new java.awt.Font("Bookman Old Style",
                java.awt.Font.BOLD, 10));
        yAxisLabel1.setLocation(0, 227);
        yAxisLabel1.setSize(40, 16);
        yAxisLabel3.setLocation(0, 107);
        yAxisLabel3.setSize(40, 16);
        yAxisLabel4.setLocation(0, 47);
        yAxisLabel4.setSize(40, 16);
        xAxisLabel3.setText("2000");
        xAxisLabel3.setLocation(400, 300);
        xAxisLabel3.setSize(40, 17);
        xAxisLabel3.setFont(new java.awt.Font("Bookman Old Style",
                java.awt.Font.BOLD, 10));
        xAxisLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        xAxisLabel2.setText("1950");
        xAxisLabel2.setLocation(210, 300);
        xAxisLabel2.setSize(40, 17);
        xAxisLabel2.setFont(new java.awt.Font("Bookman Old Style",
                java.awt.Font.BOLD, 10));
        xAxisLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        xAxisLabel1.setText("1900");
        xAxisLabel1.setLocation(20, 300);
        xAxisLabel1.setSize(40, 17);
        xAxisLabel1.setFont(new java.awt.Font("Bookman Old Style",
                java.awt.Font.BOLD, 10));
        xAxisLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        this.add(xAxisLabel3, null);
        this.add(xAxisLabel2, null);
        this.add(xAxisLabel1, null);
        yAxisLabel1.setFont(new java.awt.Font("Bookman Old Style",
                java.awt.Font.BOLD, 10));
        this.add(title, null);
        this.add(yAxisLabel1, null);
        this.add(yAxisLabel3, null);
        this.add(yAxisLabel2, null);
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (null == submitButtonCallBack) {
                    System.err.println("mouseClicked");
                } else {
                    submitButtonCallBack.actionPerformed(new ActionEvent(this,
                            0, null));
                }
            }
        });
        this.add(yAxisLabel4, null);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_BEVEL));

        // Draw guide lines.
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_BEVEL));
        g.setColor(Color.GRAY);
        for (int y = 295; y > 50; y -= 60) {
            g2.drawLine(graphRect.x, y, 420, y);
        }
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_BEVEL));
        // Draw key
        for (int i = 0; i < companies.size(); i++) {
            int yOffset = i * 20;
            CompanyDetails company = companies.get(i);
            g2.setColor(company.color);
            g2.drawLine(50, 70 + yOffset, 60, 70 + yOffset);
            g2.setColor(Color.BLACK);
            g2.setFont(FONT);
            g.drawString(company.name, 65, 72 + yOffset);
        }

        // Draw graphs lines
        for (int i = 0; i < companies.size(); i++) {

            CompanyDetails company = companies.get(i);
            g2.setColor(company.color);
            for (int year = 1; year < 100; year++) {
                if (company.value[year] != Integer.MIN_VALUE
                        && company.value[year - 1] != Integer.MIN_VALUE) {
                    long x1 = year * graphRect.width / 100 + graphRect.x;
                    long y1 = company.value[year] * graphRect.height / scaleMax;
                    y1 = Math.max(1, y1);
                    y1 = graphRect.y + graphRect.height - y1;

                    long x2 = (year - 1) * graphRect.width / 100 + graphRect.x;
                    long y2 = company.value[year - 1] * graphRect.height
                            / scaleMax;
                    y2 = Math.max(1, y2);
                    y2 = graphRect.y + graphRect.height - y2;
                    g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
                }
            }
        }

        // Draw axis
        g2.setColor(Color.BLACK);

        g2.drawLine(graphRect.x, graphRect.y, graphRect.x, graphRect.y
                + graphRect.height);
        g2.drawLine(graphRect.x, graphRect.y + graphRect.height, graphRect.x
                + graphRect.width, graphRect.y + graphRect.height);

    }

    /**
     * <p>
     * Sets the value of scaleMax subject to the following constraints.
     * </P>
     * <p>
     * (1) scaleMax >= max, where max is the max net worth value.
     * </p>
     * <p>
     * (2) (scaleMax % 4) == 0
     * </p>
     * <p>
     * (3) if max >= 1,000, then (scaleMax % 4,000) == 0
     * </p>
     * <p>
     * (4) if max >= 1,000,000, then (scaleMax % 4,000,000) == 0
     * </p>
     * <p>
     * (5) if max >= 1,000,000,000, then (scaleMax % 4,000,000,000) == 0
     * </p>
     */
    private void setAppropriateScale() {

        long max = 0;
        for (int i = 0; i < companies.size(); i++) {
            CompanyDetails company = companies.get(i);
            for (int year = 0; year < 100; year++) {
                long value = company.value[year];
                if (value > max) {
                    max = value;
                }
            }
        }

        long increment = 1;
        while (scaleMax < max) {
            scaleMax = increment;
            if (scaleMax < max) {
                scaleMax += increment;
                int loopCount = 0;
                while (scaleMax < max && loopCount < 3) {
                    scaleMax += increment * 2;
                    loopCount++;
                }
            }
            increment = increment * 10;
        }

        /*
         * Make sure that if the scale is in k/m/b that each of the quarter
         * scales will be divisible by k/m/b.
         */
        increment = 1;
        for (int i = 0; i < 3; i++) {
            increment = increment * 1000;

            if (scaleMax >= 8 * increment && scaleMax < 12 * increment) {
                scaleMax = 12 * increment;
            } else if (scaleMax >= 4 * increment && scaleMax < 8 * increment) {
                scaleMax = 8 * increment;
            } else if (scaleMax >= 1 * increment && scaleMax < 4 * increment) {
                scaleMax = 4 * increment;
            }
        }
        if (scaleMax < 100) {
            scaleMax = 100;
        }

        long quarterScale = scaleMax / 4;
        yAxisLabel1.setText(getYScaleString(quarterScale));
        yAxisLabel2.setText(getYScaleString(quarterScale * 2));
        yAxisLabel3.setText(getYScaleString(quarterScale * 3));
        yAxisLabel4.setText(getYScaleString(quarterScale * 4));

    }

    private String getYScaleString(long value) {
        String abv;
        if (value >= 1000000000) {
            value = value / 1000000000;
            abv = "b";
        } else if (value >= 1000000) {
            value = value / 1000000;
            abv = "m";
        } else if (value >= 1000) {
            value = value / 1000;
            abv = "k";
        } else {
            abv = "";
        }

        return "$" + String.valueOf(value) + abv;
    }

    public void setup(ModelRoot modelRoot, RenderersRoot vl, Action closeAction) {
        this.submitButtonCallBack = closeAction;
        ReadOnlyWorld world = modelRoot.getWorld();
        companies = new ArrayList<CompanyDetails>();
        GameCalendar calender = (GameCalendar) world.get(ITEM.CALENDAR);
        int startYear = calender.getYear(0);
        int endYear = startYear + 100;
        GameTime currentTime = world.currentTime();
        int currentYear = calender.getYear(currentTime.getTicks());
        xAxisLabel1.setText(String.valueOf(startYear));
        xAxisLabel2.setText(String.valueOf(startYear + 50));
        xAxisLabel3.setText(String.valueOf(endYear));

        for (int i = 0; i < world.getNumberOfPlayers(); i++) {

            Color c = PlayerColors.getColor(i);
            Player player = world.getPlayer(i);
            String name = player.getName();

            if (logger.isDebugEnabled()) {
                logger.debug("Adding player " + name + " to net worth graph.");
            }
            CompanyDetails cd = new CompanyDetails(name, c);
            GameTime[] times = new GameTime[101];
            for (int year = 0; year < 101; year++) {
                int ticks = calender.getTicks(startYear + year - 1);
                times[year] = new GameTime(ticks);
            }
            TransactionAggregator aggregator = new NetWorthCalculator(world,
                    player.getPrincipal());
            aggregator.setTimes(times);
            Money[] values = aggregator.calculateValues();
            int stopYear = currentYear - startYear + 1;
            for (int year = 0; year < stopYear; year++) {
                cd.value[year] = values[year].getAmount();
            }
            companies.add(cd);

        }

        setAppropriateScale();

    }

} // @jve:decl-index=0:visual-constraint="10,10"
