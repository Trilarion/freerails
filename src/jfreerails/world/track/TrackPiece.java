package jfreerails.world.track;
import jfreerails.world.tilemap.Tile;

public interface TrackPiece extends Tile {
    int getTrackGraphicNumber();    
    
    int getRGB();
    
    TrackRule getTrackRule();
    
    TrackConfiguration getTrackConfiguration();
    
}
