package jfreerails.world.track;

/** Defines the interface of a the track on a tile. */

public interface TrackPiece {
    int getTrackGraphicNumber();    
      
    TrackRule getTrackRule();
    
    TrackConfiguration getTrackConfiguration();
    
}
