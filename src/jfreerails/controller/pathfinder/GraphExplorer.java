package jfreerails.controller.pathfinder;

/** This interface lets the caller explorer a graph while hiding the
 * way the graph is stored.  Vertices are packed into single ints
 * to avoid the cost of object creation and garbage collection.
 *
 * 24-Nov-2002
 * @author Luke Lindsay
 */
public interface GraphExplorer {
	
	void setPosition(int vertex);
	
	/** Return the current edge.	*/
	int getPosition();
	
	/** Sets the current edge to the current vertex's next edge.  Throws a NoSuchElementException if
	 * the vertex foes not have another edge.*/
	void nextEdge();
	
	/** Returns the vertex that is connected to the current vertex by the current edge. */
	int getVertexConnectedByEdge();
	
	/** Returns the length of the current edge	*/
	int getEdgeLength();
	
	boolean hasNextEdge();

	/** Moves this GraphExplorer from the current vertex to the vertex
	 * that is connected to the current vertex by the current edge.	 
	 */
	void moveForward();

	/***********************************/
	//scott bennett 15/03/03
	boolean isAtStation();
	/***********************************/
}
