package freerails.model.track.pathfinding;

/**
 *
 */
public enum PathFinderStatus {

    PATH_NOT_FOUND(Integer.MIN_VALUE), PATH_FOUND(Integer.MIN_VALUE + 1), SEARCH_PAUSED(Integer.MIN_VALUE + 2), SEARCH_NOT_STARTED(Integer.MIN_VALUE + 3);

    public final int id;

    PathFinderStatus(int id) {
        this.id = id;
    }


}
