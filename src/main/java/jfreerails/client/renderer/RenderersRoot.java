package jfreerails.client.renderer;

import java.awt.Image;
import java.io.IOException;

import jfreerails.world.top.ReadOnlyWorld;

/**
 * Provides access to the objects that render terrain, track, and trains.
 * 
 * @author Luke
 */
public interface RenderersRoot extends TileRendererList {

    TrackPieceRenderer getTrackPieceView(int i);

    TrainImages getWagonImages(int type);

    TrainImages getEngineImages(int type);

    // OldTrainImages getTrainImages();

    boolean validate(ReadOnlyWorld world);

    Image getImage(String relativeFilename) throws IOException;

    Image getScaledImage(String relativeFilename, int height)
            throws IOException;
}