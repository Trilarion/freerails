package jfreerails.client.renderer;

import jfreerails.world.top.World;


public interface ViewLists {

	TileRendererList getTileViewList();
	TrackPieceRendererList getTrackPieceViewList();
	SideOnTrainTrainViewImages getSideOnTrainTrainViewImages();
	TrainImages getTrainImages();
	boolean validate(World world);

}
