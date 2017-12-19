/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package experimental;

import freerails.client.common.ImageManager;
import freerails.client.common.ImageManagerImpl;
import freerails.world.common.Step;
import freerails.world.track.TrackConfiguration;

import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.CubicCurve2D.Double;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods that render track pieces.
 *
 * @see experimental.TrackTilesGenerator
 */
public class TrackRenderer {

    final Color sleepersColor = new Color(118, 54, 36);
    final Color railsColor = new Color(118, 118, 118);
    final double sleeperLength = 6;
    final float sleeperWidth = 2f;
    final float targetSleeperGap = 2.5f;
    final float tileWidth = 30f;
    final float gauge = 3f;
    final BasicStroke rail = new BasicStroke(1f);
    final float doubleTrackGap = 4f;
    private final ImageManager imageManager = new ImageManagerImpl(
            "/freerails/client/graphics/");
    boolean doubleTrack = false;
    Image icon = null;

    boolean tunnel = false;

    /**
     *
     * @param line
     * @param shift
     * @return
     */
    public static Line2D.Double createParallelLine(Line2D.Double line,
                                                   double shift) {
        Line2D.Double returnValue = new Line2D.Double(line.getP1(), line
                .getP2());
        double distance = line.getP1().distance(line.getP2());
        double dRatio = shift / distance;
        double dx = (line.x1 - line.x2) * dRatio;
        double dy = (line.y1 - line.y2) * dRatio;
        returnValue.x1 -= dy;
        returnValue.y1 += dx;
        returnValue.x2 -= dy;
        returnValue.y2 += dx;
        return returnValue;
    }

    /**
     *
     * @param c
     * @param shift1
     * @param shift2
     * @return
     */
    public static CubicCurve2D.Double createAdjacentCurve(
            CubicCurve2D.Double c, double shift1, double shift2) {
        Line2D.Double line1 = new Line2D.Double(c.getX1(), c.getY1(), c
                .getCtrlX1(), c.getCtrlY1());
        Line2D.Double line2 = new Line2D.Double(c.getX2(), c.getY2(), c
                .getCtrlX2(), c.getCtrlY2());
        line1 = createParallelLine(line1, shift1);
        line2 = createParallelLine(line2, -shift2);
        return new CubicCurve2D.Double(line1.x1, line1.y1, line1.x2, line1.y2,
                line2.x2, line2.y2, line2.x1, line2.y1);
    }

    void paintTrackConf(Graphics2D g2, TrackConfiguration conf) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw title
        // String title = BinaryNumberFormatter.formatWithLowBitOnLeft(conf
        // .get9bitTemplate(), 9);
        // g.setColor(Color.BLACK);
        // g.setFont(font);
        //
        // g.drawString(title, 10, 10);

        Step[] directions = Step.getList();
        List<CubicCurve2D.Double> sections = new ArrayList<>();
        int matches = 0;
        for (int i = 0; i < directions.length - 2; i++) {

            if (conf.contains(directions[i])) {
                // System.out.println("\n"+directions[i]+" to ..");
                int maxJ = Math.min(i + 7, directions.length);

                for (int j = i + 2; j < maxJ; j++) {
                    // System.out.println(directions[j]);
                    if (conf.contains(directions[j])) {
                        Double toCurve = toCurve(directions[i], directions[j]);
                        if (doubleTrack) {
                            sections.add(createAdjacentCurve(toCurve,
                                    doubleTrackGap, doubleTrackGap));
                            sections.add(createAdjacentCurve(toCurve,
                                    -doubleTrackGap, -doubleTrackGap));
                        } else {
                            sections.add(toCurve);
                        }

                        matches++;

                    }

                }
            }
        }
        if (matches == 0) {
            for (Step direction : directions) {

                if (conf.contains(direction)) {
                    Double toCurve = toCurve(direction);
                    if (doubleTrack) {
                        sections.add(createAdjacentCurve(toCurve,
                                doubleTrackGap, doubleTrackGap));
                        sections.add(createAdjacentCurve(toCurve,
                                -doubleTrackGap, -doubleTrackGap));
                    } else {
                        sections.add(toCurve);
                    }
                }
            }
        }
        paintTrack(g2, sections);

    }

    CubicCurve2D.Double toCurve(Step a) {
        float halfTile = tileWidth / 2;
        Point2D.Double start, end, one;
        start = new Point2D.Double();
        start.x = tileWidth + (halfTile * a.deltaX);
        start.y = tileWidth + (halfTile * a.deltaY);
        one = controlPoint(start);
        end = new Point2D.Double(tileWidth, tileWidth);
        CubicCurve2D.Double returnValue = new CubicCurve2D.Double();
        returnValue.setCurve(start, one, one, end);
        return returnValue;
    }

    CubicCurve2D.Double toCurve(Step a, Step b) {
        float halfTile = tileWidth / 2;
        Point2D.Double start, end, one, two;
        start = new Point2D.Double();
        start.x = tileWidth + (halfTile * a.deltaX);
        start.y = tileWidth + (halfTile * a.deltaY);
        one = controlPoint(start);
        end = new Point2D.Double();
        end.x = tileWidth + (halfTile * b.deltaX);
        end.y = tileWidth + (halfTile * b.deltaY);
        two = controlPoint(end);
        CubicCurve2D.Double returnValue = new CubicCurve2D.Double();
        returnValue.setCurve(start, one, two, end);
        return returnValue;
    }

    void paintTrack(Graphics2D g, List<CubicCurve2D.Double> sections) {

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (!tunnel) {
            // Draw sleepers
            g.setColor(sleepersColor);
            for (CubicCurve2D.Double section : sections) {
                BasicStroke dashed = getStroke4Curve(section);
                g.setStroke(dashed);
                g.draw(section);
            }

            g.setColor(railsColor);
        } else {
            g.setColor(Color.BLACK);
        }
        // Draw rails
        g.setStroke(rail);
        for (CubicCurve2D.Double section : sections) {
            float halfGauge = gauge / 2;
            CubicCurve2D.Double rail1 = createAdjacentCurve(section, halfGauge,
                    halfGauge);
            CubicCurve2D.Double rail2 = createAdjacentCurve(section,
                    -halfGauge, -halfGauge);
            g.draw(rail1);
            g.draw(rail2);
        }

    }

    /**
     * Generates the Stroke used to draw the sleepers for track section
     * represented by the specified curve.
     * @param curve
     * @return 
     */
    public BasicStroke getStroke4Curve(CubicCurve2D.Double curve) {
        PathIterator fpt = curve.getPathIterator(new AffineTransform(), 0.01);
        double length = 0;
        double[] coords = new double[6];
        double x, y;
        fpt.currentSegment(coords);
        double lastX = coords[0];
        double lastY = coords[1];
        for (; !fpt.isDone(); fpt.next()) {
            fpt.currentSegment(coords);
            x = coords[0];
            y = coords[1];
            double dx = x - lastX;
            double dy = y - lastY;
            length += Math.sqrt(dx * dx + dy * dy);
            lastX = x;
            lastY = y;
        }

        float sleepers = (float) length / (targetSleeperGap + sleeperWidth);
        float sleeperCount = (int) sleepers;
        float sleeperGap = (float) length / sleeperCount - sleeperWidth;
        float dash1[] = {sleeperWidth, sleeperGap};
        float phase = sleeperWidth + (sleeperGap / 2);
        return new BasicStroke((float) sleeperLength, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, dash1, phase);
    }

    private Point2D.Double controlPoint(Point2D.Double from) {
        double weight = 0.3;
        double x = from.getX() * weight + tileWidth * (1 - weight);
        double y = from.getY() * weight + tileWidth * (1 - weight);
        return new Point2D.Double(x, y);
    }

    void setIcon(String typeName) {
        try {

            String relativeFileName = "icons" + File.separator + typeName
                    + ".png";
            relativeFileName = relativeFileName.replace(' ', '_');

            icon = imageManager.getImage(relativeFileName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

}
