

package freerails.controller;

/**
 * simple class which clears caches, if something in the world happens, which
 * changes the result of the path finding algorithm
 * 
 * @author cymric
 * @version $Revision 1.1$
 */
public class PathCacheController {
    public static void clearTrackCache() {
        MoveTrainPreMove.clearCache();
    }
}
