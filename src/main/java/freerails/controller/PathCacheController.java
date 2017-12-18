package freerails.controller;

/**
 * simple class which clears caches, if something in the world happens, which
 * changes the result of the path finding algorithm
 *
 * @author cymric
 */
public class PathCacheController {

    /**
     *
     */
    public static void clearTrackCache() {
        MoveTrainPreMove.clearCache();
    }
}
