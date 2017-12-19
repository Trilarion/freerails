package freerails.client.renderer;

import freerails.world.top.ReadOnlyWorld;

import java.awt.*;
import java.io.IOException;

/**
 * Provides access to the objects that render terrain, track, and trains.
 *
 */
public interface RenderersRoot extends TileRendererList {

    /**
     *
     * @param i
     * @return
     */
    TrackPieceRenderer getTrackPieceView(int i);

    /**
     *
     * @param type
     * @return
     */
    TrainImages getWagonImages(int type);

    /**
     *
     * @param type
     * @return
     */
    TrainImages getEngineImages(int type);

    // OldTrainImages getTrainImages();

    boolean validate(ReadOnlyWorld world);

    /**
     *
     * @param relativeFilename
     * @return
     * @throws IOException
     */
    Image getImage(String relativeFilename) throws IOException;

    /**
     *
     * @param relativeFilename
     * @param height
     * @return
     * @throws IOException
     */
    Image getScaledImage(String relativeFilename, int height)
            throws IOException;
}