package jfreerails.controller.pathfinder;

import it.unimi.dsi.fastUtil.Int2IntHashMap;
import it.unimi.dsi.fastUtil.IntHashSet;
import it.unimi.dsi.fastUtil.IntIterator;
import jfreerails.world.common.FreerailsSerializable;


/**	A simple A* pathfinder implementation.  It uses int's to avoid the cost of
 * object creation and garbage collection. 
 * 26-Nov-2002
 * @author Luke Lindsay
 *
 */
public class SimpleAStarPathFinder implements FreerailsSerializable {

	public static final int PATH_NOT_FOUND = Integer.MIN_VALUE;

	Int2IntHashMap openList = new Int2IntHashMap();

	IntHashSet openKeys = new IntHashSet();

	Int2IntHashMap closedList = new Int2IntHashMap();

	public int findpath(
		int currentPosition,
		int[] targets,
		GraphExplorer explorer) {
			
		int bestPath = PATH_NOT_FOUND; //the best path so far
		int bestPathF = Integer.MAX_VALUE;	

		//initialize the open list
		openList.clear();
		openKeys.clear();

		//initialize the closed list
		closedList.clear();

		
		//put the starting node on the open list (you can leave its f at zero)
		for (int i = 0; i < targets.length; i++) {
			//PositionOnTrack p = new PositionOnTrack(targets[i]);
			
			openList.put(targets[i], 0);
			openKeys.add(targets[i]);
			if(targets[i]==currentPosition){					
				return PATH_NOT_FOUND;
			}
		}

		//while the open list is not empty
		while (openList.size() > 0) {

			

			//find the node with the least f on the open list, call it "q"
			int q = findNodeWithSmallestFOnOpenList();
			int f = openList.get(q);

			//pop q off the open list
			openList.remove(q);
			openKeys.remove(q);

			//generate q's successors.
			explorer.setPosition(q);

			//for each successor
			while (explorer.hasNextEdge()) {
				explorer.nextEdge();
				int successor = explorer.getVertexConnectedByEdge();
				
				int successorF = f + explorer.getEdgeLength();
				//for now, let successor.h=0

				//successor.g = q.g + distance between successor and q
				//successor.h = distance from goal to successor
				//successor.f = successor.g + successor.h

				if (successor == currentPosition) {
					//if successor is the goal, we have found a path, but not necessarily the shorest.
					bestPath = q;
					bestPathF = successorF;
					
				}
				
				if (openList.containsKey(successor)
					&& openList.get(successor) < successorF) {
					//if a node with the same position as successor is in the OPEN list \
					//which has a lower f than successor, skip this successor
					continue;

				} else if (
					closedList.containsKey(successor)
						&& closedList.get(successor) < successorF) {
					//if a node with the same position as successor is in the CLOSED list \
					//which has a lower f than successor, skip this successor					
					continue;
				} else {
					//otherwise, add the node to the open list
					
					openList.put(successor, successorF);
					openKeys.add(successor);
				}
			}
			
			if (PATH_NOT_FOUND != bestPath) {
				int node = findNodeWithSmallestFOnOpenList();
				int SmallestFOnOpenList = openList.get(node);				
				if (bestPathF <= SmallestFOnOpenList) {
					//if the F value for the best path we have found so far is
					//less than that of the node with the smallest F value on 
					//the open list, then the best path so far is the shortest path.
										
					return bestPath;
				}
			}

			//push q on the closed list
			//PositionOnTrack p = new PositionOnTrack(q);
				closedList.put(q, f);

		}
		
		return PATH_NOT_FOUND;
	}

	int findNodeWithSmallestFOnOpenList() {

		IntIterator it = (IntIterator) openKeys.iterator();
		int nodeWithSmallestF = 0;
		int smallestF = Integer.MAX_VALUE;

		while (it.hasNext()) {
			int node = it.nextInt();
			int f = openList.get(node);
			if (f < smallestF) {
				smallestF = f;
				nodeWithSmallestF = node;
			}
		}

		return nodeWithSmallestF;
	}

}
