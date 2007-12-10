package jfreerails.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import jfreerails.util.IntArray;

import org.apache.log4j.Logger;

/**
 * A simple A* pathfinder implementation. It uses int's to avoid the cost of
 * object creation and garbage collection. 26-Nov-2002
 * 
 * @author Luke Lindsay
 * 
 */
public class SimpleAStarPathFinder implements Serializable,
        IncrementalPathFinder {
    private static final long serialVersionUID = 3257565105200576310L;

    private static final Logger logger = Logger
            .getLogger(SimpleAStarPathFinder.class.getName());

    private OpenList openList = new OpenList();

    private final HashSet<Integer> startingPositions = new HashSet<Integer>();

    private final HashMap<Integer, Integer> closedList = new HashMap<Integer, Integer>();

    private final HashMap<Integer, Integer> shortestPath = new HashMap<Integer, Integer>();

    private int status = SEARCH_NOT_STARTED;

    /** Note, IntArray is not Serializable. */
    private transient IntArray path = null;

    private int bestPath;

    private int bestPathF;

    private GraphExplorer explorer;

    private long searchStartTime = 0;

    public int getStatus() {
        return status;
    }

    public IntArray retrievePath() {
        return path;
    }

    public int findstep(int currentPosition, int[] targets,
            GraphExplorer tempExplorer) {
        try {
            return findpath(new int[] { currentPosition }, targets,
                    tempExplorer).get(0);
        } catch (PathNotFoundException e) {
            return PATH_NOT_FOUND;
        }
    }

    public IntArray findpath(int[] currentPosition, int[] targets,
            GraphExplorer e) throws PathNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug(currentPosition.length + " starting points; "
                    + targets.length + " targets.");
        }

        setupSearch(currentPosition, targets, e);

        search(-1);

        return path;
    }

    public void search(long maxDuration) throws PathNotFoundException {
        long iterationStartTime = 0;
        boolean check4timeout = false;

        if (maxDuration > 0) {
            check4timeout = true;
            iterationStartTime = System.currentTimeMillis();

            if (searchStartTime == 0) {
                searchStartTime = iterationStartTime;
            }
        }

        int loopCounter = 0;

        // while the open list is not empty
        while (openList.size() > 0) {
            // find the node with the least f on the open list, call it "q"
            // pop q off the open list
            int f = openList.smallestF();
            int q = openList.popNodeWithSmallestF();

            // generate q's successors.
            explorer.setPosition(q);

            // for each successor
            while (explorer.hasNextEdge()) {
                explorer.nextEdge();

                int successor = explorer.getVertexConnectedByEdge();

                int successorF = f + explorer.getEdgeCost();

                // for now, let successor.h=0
                // successor.g = q.g + distance between successor and q
                // successor.h = distance from goal to successor
                // successor.f = successor.g + successor.h
                if (startingPositions.contains(successor)) {
                    // if successor is the goal, we have found a path, but not
                    // necessarily the shortest.
                    if (bestPathF > successorF) {
                        bestPath = q;
                        bestPathF = successorF;
                    }
                }

                if (openList.contains(successor)
                        && openList.getF(successor) < successorF) {
                    // if a node with the same position as successor is in the
                    // OPEN list \
                    // which has a lower f than successor, skip this successor
                    continue;
                } else if (closedList.containsKey(successor)
                        && closedList.get(successor) < successorF) {
                    // if a node with the same position as successor is in the
                    // CLOSED list \
                    // which has a lower f than successor, skip this successor
                    continue;
                } else {
                    // otherwise, add the node to the open list
                    openList.add(successor, successorF);
                    shortestPath.put(successor, q);
                }
            }

            if (PATH_NOT_FOUND != bestPath) {
                int SmallestFOnOpenList = openList.smallestF();

                if (bestPathF <= SmallestFOnOpenList) {
                    // if the F value for the best path we have found so far is
                    // less than that of the node with the smallest F value on
                    // the open list, then the best path so far is the shortest
                    // path.
                    if (logger.isDebugEnabled()) {
                        logger.debug("Path successfully found after "
                                + loopCounter + " iterations.");
                    }
                    path.add(bestPath);

                    int step = bestPath;

                    while (shortestPath.containsKey(step)) {
                        step = shortestPath.get(step);
                        path.add(step);
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Path found!");
                    }
                    status = PATH_FOUND;

                    return;
                }
            }

            // push q on the closed list
            // PositionOnTrack p = new PositionOnTrack(q);
            closedList.put(q, f);
            loopCounter++;

            // Check whether we have been searching too long.
            if (check4timeout && loopCounter % 50 == 0) {
                long currentTime = System.currentTimeMillis();
                long deltatime = currentTime - iterationStartTime;

                if (deltatime > maxDuration) {
                    status = SEARCH_PAUSED;

                    long totalSearchTime = currentTime - searchStartTime;
                    throw new PathNotFoundException("No path found yet. "
                            + totalSearchTime + "ms.");
                }
            }
        }

        status = PATH_NOT_FOUND;
        if (logger.isDebugEnabled()) {
            logger.debug("No path found and open list empty after "
                    + loopCounter + " iterations.");
        }
        throw new PathNotFoundException("Path not found.");
    }

    public void setupSearch(int[] currentPosition, int[] targets,
            GraphExplorer e) throws PathNotFoundException {
        abandonSearch();

        explorer = e;

        // put the starting nodes on the open list (you can leave its f at zero)
        for (int i = 0; i < targets.length; i++) {
            openList.add(targets[i], 0);

            for (int j = 0; j < currentPosition.length; j++) {
                if (targets[i] == currentPosition[j]) {
                    status = PATH_NOT_FOUND;
                    throw new PathNotFoundException("Already at target!");
                }
            }
        }

        for (int j = 0; j < currentPosition.length; j++) {
            startingPositions.add(currentPosition[j]);
        }
    }

    public void abandonSearch() {
        path = new IntArray();
        searchStartTime = 0;

        bestPath = PATH_NOT_FOUND;
        bestPathF = Integer.MAX_VALUE;
        // initialize the open list
        openList.clear();

        // initialize the closed list
        closedList.clear();

        shortestPath.clear();

        startingPositions.clear();

        status = SEARCH_NOT_STARTED;
    }
}