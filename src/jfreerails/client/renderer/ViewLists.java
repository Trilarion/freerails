package jfreerails.client.renderer;

import jfreerails.world.top.ReadOnlyWorld;


public interface ViewLists {
    TileRendererList getTileViewList();

    TrackPieceRendererList getTrackPieceViewList();

    SideOnTrainTrainViewImages getSideOnTrainTrainViewImages();

    TrainImages getTrainImages();

    boolean validate(ReadOnlyWorld world);
}