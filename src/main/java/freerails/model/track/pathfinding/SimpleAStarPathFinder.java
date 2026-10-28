/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.model.track.pathfinding;

import freerails.model.track.explorer.GraphExplorer;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * A* pathfinder over integer-encoded vertices, with optional time-limited
 * incremental searches.
 */
public class SimpleAStarPathFinder implements Serializable, IncrementalPathFinder {

    private static final long serialVersionUID = 3257565105200576310L;
    private static final Logger logger = Logger.getLogger(SimpleAStarPathFinder.class.getName());
    private final OpenList openList = new OpenList();
    private final Collection<Integer> startingPositions = new HashSet<>();
    private final HashMap<Integer, Integer> closedList = new HashMap<>();
    private final HashMap<Integer, Integer> shortestPath = new HashMap<>();
    private PathFinderStatus status = PathFinderStatus.SEARCH_NOT_STARTED;
    private List<Integer> path = new ArrayList<>();
    private int bestPath;
    private int bestPathF;
    private GraphExplorer explorer;
    private long searchStartTime = 0;

    /**
     * Returns the current status of the search.
     *
     * @return current pathfinder status
     */
    @Override
    public PathFinderStatus getStatus() {
        return status;
    }

    /**
     * Returns the path produced by the most recent successful search.
     *
     * @return encoded path vertices in traversal order
     */
    public List<Integer> retrievePath() {
        return path;
    }

    /**
     * Finds the first step from a starting vertex toward any target.
     *
     * @param currentPosition starting encoded vertex
     * @param targets target vertices
     * @param tempExplorer graph explorer used to enumerate edges
     * @return first encoded vertex on the path, or {@link PathFinderStatus#PATH_NOT_FOUND}
     *         when no path can be found
     */
    public int findstep(int currentPosition, int[] targets, GraphExplorer tempExplorer) {
        try {
            return findpath(new int[]{currentPosition}, targets, tempExplorer).get(0);
        } catch (PathNotFoundException e) {
            return PathFinderStatus.PATH_NOT_FOUND.id;
        }
    }

    /**
     * Configures and completes an unbounded search, returning the resulting path.
     *
     * @param currentPosition one or more starting vertices
     * @param targets destination vertices
     * @param e graph explorer used to enumerate edges
     * @return path vertices in traversal order
     * @throws PathNotFoundException if the search cannot find a route
     */
    private List<Integer> findpath(int[] currentPosition, int[] targets, GraphExplorer e) throws PathNotFoundException {
        logger.debug(currentPosition.length + " starting points; " + targets.length + " targets.");

        setupSearch(currentPosition, targets, e);
        search(-1);
        return path;
    }

    /**
     * Continues the current search until it finishes or the positive time limit
     * is exceeded. A timeout leaves the search available to resume.
     *
     * @param maxDuration maximum duration of this call in milliseconds, or a
     *                    non-positive value for no time limit
     * @throws PathNotFoundException if the search has no route yet or cannot find one
     */
    @Override
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

                if (openList.contains(successor) && openList.getF(successor) < successorF) {
                    // if a node with the same position as successor is in the
                    // OPEN list \
                    // which has a lower f than successor, skip this successor
                } else if (closedList.containsKey(successor) && closedList.get(successor) < successorF) {
                    // if a node with the same position as successor is in the
                    // CLOSED list \
                    // which has a lower f than successor, skip this successor
                } else {
                    // otherwise, add the node to the open list
                    openList.add(successor, successorF);
                    shortestPath.put(successor, q);
                }
            }

            if (PathFinderStatus.PATH_NOT_FOUND.id != bestPath) {
                int SmallestFOnOpenList = openList.smallestF();

                if (bestPathF <= SmallestFOnOpenList) {
                    // if the F value for the best path we have found so far is
                    // less than that of the node with the smallest F value on
                    // the open list, then the best path so far is the shortest
                    // path.
                    logger.debug("Path successfully found after " + loopCounter + " iterations.");
                    path.add(bestPath);

                    int step = bestPath;

                    while (shortestPath.containsKey(step)) {
                        step = shortestPath.get(step);
                        path.add(step);
                    }

                    logger.debug("Path found!");
                    status = PathFinderStatus.PATH_FOUND;

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
                    status = PathFinderStatus.SEARCH_PAUSED;

                    long totalSearchTime = currentTime - searchStartTime;
                    throw new PathNotFoundException("No path found yet. " + totalSearchTime + "moveStatus.");
                }
            }
        }

        status = PathFinderStatus.PATH_NOT_FOUND;
        logger.debug("No path found and open list empty after " + loopCounter + " iterations.");

        throw new PathNotFoundException("Path not found.");
    }

    /**
     * Initializes a search from one or more vertices to any of the targets.
     *
     * @param currentPosition starting vertex IDs
     * @param targets target vertex IDs
     * @param e graph explorer used to enumerate neighbors
     * @throws PathNotFoundException if a start is already a target
     */
    public void setupSearch(int[] currentPosition, int[] targets, GraphExplorer e) throws PathNotFoundException {
        abandonSearch();

        explorer = e;

        // put the starting nodes on the open list (you can leave its f at zero)
        for (int target : targets) {
            openList.add(target, 0);

            for (int aCurrentPosition : currentPosition) {
                if (target == aCurrentPosition) {
                    status = PathFinderStatus.PATH_NOT_FOUND;
                    throw new PathNotFoundException("Already at target!");
                }
            }
        }

        for (int aCurrentPosition : currentPosition) {
            startingPositions.add(aCurrentPosition);
        }
    }

    /**
     * Resets all search state and discards any partial or completed path.
     */
    @Override
    public void abandonSearch() {
        path.clear();
        searchStartTime = 0;

        bestPath = PathFinderStatus.PATH_NOT_FOUND.id;
        bestPathF = Integer.MAX_VALUE;
        // initialize the open list
        openList.clear();

        // initialize the closed list
        closedList.clear();

        shortestPath.clear();

        startingPositions.clear();

        status = PathFinderStatus.SEARCH_NOT_STARTED;
    }
}