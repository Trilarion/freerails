package jfreerails.world.track;
import jfreerails.world.common.Tile;

/** Defines the interface of a the track on a tile. */

public interface TrackPiece extends Tile {
    int getTrackGraphicNumber();    
    
    int getRGB();
    
    TrackRule getTrackRule();
    
    TrackConfiguration getTrackConfiguration();
    
}
