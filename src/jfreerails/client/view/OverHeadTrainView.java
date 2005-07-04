package jfreerails.client.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import jfreerails.client.common.Painter;
import jfreerails.client.common.SoundManager;
import jfreerails.client.renderer.TrainRenderer;
import jfreerails.client.renderer.ViewLists;
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

	public OverHeadTrainView(ReadOnlyWorld world, ViewLists vl) {
		this.w = world;
		trainPainter = new TrainRenderer(vl.getTrainImages());
	}

	public void paint(Graphics2D g) {
		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke(10));

		for (int k = 0; k < w.getNumberOfPlayers(); k++) {
			FreerailsPrincipal principal = w.getPlayer(k).getPrincipal();

			for (int i = 0; i < w.size(KEY.TRAINS, principal); i++) {
				TrainModel train = (TrainModel) w.get(KEY.TRAINS, i, principal);
				TrainPositionOnMap pos = (TrainPositionOnMap) w.get(
						KEY.TRAIN_POSITIONS, i, principal);
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
					trainPainter.paintTrain(g, train, pos);
				}
			}
		}
	}
}