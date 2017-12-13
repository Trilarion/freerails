package jfreerails.client.renderer;

import javax.swing.ImageIcon;

import jfreerails.world.top.ReadOnlyWorld;


public interface ViewLists {
    TileRendererList getTileViewList();

    TrackPieceRendererList getTrackPieceViewList();

    TrainImages getTrainImages();

    boolean validate(ReadOnlyWorld world);

    /**
     * @return the ImageIcon corresponding to the specified name
     */
    ImageIcon getImageIcon(String iconName);
}
