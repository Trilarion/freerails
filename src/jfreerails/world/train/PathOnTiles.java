/*
 * Created on 03-Feb-2005
 *
 */
package jfreerails.world.train;

import java.awt.Dimension;
import java.awt.Point;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.OneTileMoveVector;

/**
 * An immutable class that stores a path made up of OneTileMoveVectors.
 * 
 * @author Luke
 * 
 */
public class PathOnTiles implements FreerailsSerializable{
	
	private static final long serialVersionUID = 3544386994122536753L;
	private final Point start;
	private final OneTileMoveVector[] vectors;	
	
	/** 
	 * @throws NullPointerException if null == start
	 * @throws NullPointerException if null == vectors
	 * @throws NullPointerException if null == vectors[i] for any i;
	 */
	public PathOnTiles(Point start, OneTileMoveVector[] vectors){
		if(null == start) throw new NullPointerException();
		for (int i = 0; i < vectors.length; i++) {
			if(null == vectors[i]) throw new NullPointerException();
		}
		this.start = start;
		this.vectors = vectors;		
	}
	
	/** Returns the coordinates of the point you would be standing at if you
	 * walked the specified distance along the path from the start point.
	 * 
	 */
	public Point getPoint(int distance){
		return null;
	}
	/** Returns the distance you would travel if you walked the all the way along the path.*/
	public int getLength(){
		return 0;
	}
	
	public FreerailsPathIterator path(){
		return null;
	}

}
