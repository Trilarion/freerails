package jfreerails.server.common;

import jfreerails.world.top.World;


/**
*  Defines a standard method to add track types to the world objects.
*
*@author     Luke Lindsay
*     09 October 2001
*/
public interface TrackSetFactory {
    void addTrackRules(World w);
}