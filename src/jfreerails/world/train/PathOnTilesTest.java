/*
 * Created on 09-Feb-2005
 *
 */
package jfreerails.world.train;

import java.awt.Point;

import jfreerails.world.common.OneTileMoveVector;
import junit.framework.TestCase;
import static jfreerails.world.common.OneTileMoveVector.*;
/**
 * JUnit test for PathOnTiles.
 * @author Luke
 * 
 */
public class PathOnTilesTest extends TestCase {

	public void testPathOnTiles(){
		Point start = null;
		OneTileMoveVector[] vectors = null;
		assertTrue(throwsException(start, vectors));
		start = new Point();
		assertTrue(throwsException(start, vectors));
		vectors = new OneTileMoveVector[]{null, null}; 
		assertTrue(throwsException(start, vectors));
		vectors = new OneTileMoveVector[]{NORTH, SOUTH}; 
		assertFalse(throwsException(start, vectors));
		
	}
	
	boolean throwsException(Point start, OneTileMoveVector[] vectors){
		try{
			new PathOnTiles(start, vectors);
			return false;			
		}catch( Exception e){
			return true;
		}
	}
	
}
