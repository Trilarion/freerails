package jfreerails.client.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import jfreerails.client.common.Painter;
import jfreerails.client.common.SoundManager;
import jfreerails.client.renderer.TrainRenderer;
import jfreerails.client.renderer.RenderersRoot;
import jfreerails.controller.ModelRoot;
import jfreerails.controller.TrainAccessor;
import jfreerails.controller.ModelRoot.Property;
import jfreerails.world.common.ImInts;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;

/**
 * Draws the trains on the main map.
 * 
 * @author Luke
 */
public class OverHeadTrainView implements Painter {
    private final TrainRenderer trainPainter;

    private final ReadOnlyWorld w;

    private SoundManager soundManager = SoundManager.getSoundManager();

    private ModelRoot mr;

    public OverHeadTrainView(ReadOnlyWorld world, RenderersRoot rr, ModelRoot mr) {
        this.w = world;
        trainPainter = new TrainRenderer(rr);
        this.mr = mr;
    }

    public void paint(Graphics2D g, Rectangle newVisibleRectectangle) {
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(10));

        Double time = (Double) mr.getProperty(Property.TIME);

        for (int k = 0; k < w.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = w.getPlayer(k).getPrincipal();

            for (int i = 0; i < w.size(principal, KEY.TRAINS); i++) {
                TrainModel train = (TrainModel) w.get(principal, KEY.TRAINS, i);

                // TrainPositionOnMap pos = (TrainPositionOnMap) w.get(
                // principal, KEY.TRAIN_POSITIONS, i);
                TrainAccessor ta = new TrainAccessor(w, principal, i);
                /** 666 10% */
                TrainPositionOnMap pos = ta.findPosition(time);
                if (pos.isCrashSite()
                        && (pos.getFrameCt() <= TrainPositionOnMap.CRASH_FRAMES_COUNT)) {
                    trainPainter.paintTrainCrash(g, pos);
                    if (pos.getFrameCt() == 1) {
                        try {
                            soundManager.playSound(
                                    "/jfreerails/client/sounds/traincrash.wav",
                                    1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (newVisibleRectectangle != null) {
                        // test if its visible
                        ImInts xpoints = pos.getXPoints();
                        ImInts ypoints = pos.getYPoints();
                        int minx = Integer.MAX_VALUE;
                        int miny = Integer.MAX_VALUE;
                        int maxx = 0;
                        int maxy = 0;
                        for (int c = 0; c < pos.getLength(); c++) {
                            minx = Math.min(minx, xpoints.get(c));
                            miny = Math.min(miny, ypoints.get(c));
                            maxx = Math.max(maxx, xpoints.get(c));
                            maxy = Math.max(maxy, ypoints.get(c));
                        }
                        Rectangle r = new Rectangle(minx - 15, miny - 15, maxx
                                - minx + 30, maxy - miny + 35);
                        if (newVisibleRectectangle.intersects(r)) {
                            /** 666 10% */
                            trainPainter.paintTrain(g, train, pos);
                        }
                    } else {
                        trainPainter.paintTrain(g, train, pos);
                    }
                }
            }
        }
    }
}