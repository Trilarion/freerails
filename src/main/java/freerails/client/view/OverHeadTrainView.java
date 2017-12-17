package freerails.client.view;

import freerails.client.common.Painter;
import freerails.client.common.SoundManager;
import freerails.client.renderer.RenderersRoot;
import freerails.client.renderer.TrainRenderer;
import freerails.config.ClientConfig;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.controller.TrainAccessor;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainPositionOnMap;

import java.awt.*;

/**
 * Draws the trains on the main map.
 *
 * @author Luke
 */
public class OverHeadTrainView implements Painter {
    private final TrainRenderer trainPainter;

    private final ReadOnlyWorld w;

    private final SoundManager soundManager = SoundManager.getSoundManager();

    private final ModelRoot mr;

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
                TrainPositionOnMap pos = ta.findPosition(time,
                        newVisibleRectectangle);
                if (pos == null)
                    continue;
                if (pos.isCrashSite()
                        && (pos.getFrameCt() <= TrainPositionOnMap.CRASH_FRAMES_COUNT)) {
                    trainPainter.paintTrainCrash(g, pos);
                    if (pos.getFrameCt() == 1) {
                        try {
                            soundManager.playSound(
                                    ClientConfig.SOUND_TRAIN_CRASH, 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    trainPainter.paintTrain(g, train, pos);
                }
            }
        }
    }
}