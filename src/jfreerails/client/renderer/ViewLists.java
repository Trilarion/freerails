package jfreerails.client.renderer;

import jfreerails.client.common.ImageManager;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * Provides access to the objects that render terrain, track, and trains.
 * 
 * @author Luke
 */
public interface ViewLists {
	TileRendererList getTileViewList();

	TrackPieceRendererList getTrackPieceViewList();

	TrainImages getTrainImages();

	boolean validate(ReadOnlyWorld world);

	ImageManager getImageManager();
}