package jfreerails.client.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import jfreerails.client.common.Painter;
import jfreerails.client.renderer.TrainRenderer;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.TrainModel;


public class TestOverHeadTrainView implements Painter {
    final TrainRenderer trainPainter;
    ReadOnlyWorld w;

    public TestOverHeadTrainView(ReadOnlyWorld world, ViewLists vl) {
        this.w = world;
        trainPainter = new TrainRenderer(vl.getTrainImages());
    }

    public void paint(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(10));

        for (int k = 0; k < w.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = w.getPlayer(k).getPrincipal();

            for (int i = 0; i < w.size(KEY.TRAINS, principal); i++) {
                TrainModel train = (TrainModel)w.get(KEY.TRAINS, i, principal);
                trainPainter.paintTrain(g, train);
            }
        }
    }
}