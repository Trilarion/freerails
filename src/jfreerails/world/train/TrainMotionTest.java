/*
 * Created on 02-Apr-2005
 *
 */
package jfreerails.world.train;

import java.awt.Point;

import jfreerails.world.common.GameTime;
import jfreerails.world.common.OneTileMoveVector;
import static jfreerails.world.common.OneTileMoveVector.EAST;
import static jfreerails.world.common.OneTileMoveVector.TILE_DIAMETER;
import junit.framework.TestCase;

/**
 * @author Luke
 *
 */
public class TrainMotionTest extends TestCase {
	
	public void testConstructor(){
		OneTileMoveVector[] vectors = {EAST, EAST, EAST};
		PathOnTiles path = new PathOnTiles(new Point(0,0), vectors);
		int trainLength = TrainModel.WAGON_LENGTH;
		assertFalse(throwsException(path, 1, trainLength, SpeedAgainstTime.STOPPED));
		assertTrue(throwsException(path, 0, trainLength, SpeedAgainstTime.STOPPED));
		assertTrue(throwsException(path, 4, trainLength, SpeedAgainstTime.STOPPED));
		
	}
	
	public void testGetPosition(){
		OneTileMoveVector[] vectors = {EAST, EAST, EAST};
		PathOnTiles path = new PathOnTiles(new Point(0,0), vectors);
		int trainLength = TrainModel.WAGON_LENGTH;
		TrainMotion motion = new TrainMotion(path, 1, trainLength, SpeedAgainstTime.STOPPED);
		TrainPositionOnMap tp = motion.getPosition(new GameTime(0));
		assertNotNull(tp);
		//The 1st point should be the centre of tile 0,0.
		int expectedX = TILE_DIAMETER/2 + TILE_DIAMETER;
		int expectedY = TILE_DIAMETER/2;
		assertEquals(expectedX, tp.getX(0));
		assertEquals(expectedY, tp.getY(0));
		
		expectedX -= trainLength;
		assertEquals(expectedX, tp.getX(1));
		assertEquals(expectedY, tp.getY(1));
		
		
	}
	
	public void testGetTiles(){
		OneTileMoveVector[] vectors = {EAST, EAST, EAST};
		PathOnTiles path = new PathOnTiles(new Point(0,0), vectors);
		int trainLength = TrainModel.WAGON_LENGTH;
		TrainMotion motion = new TrainMotion(path, 1, trainLength, SpeedAgainstTime.STOPPED);
		Point[] tiles = motion.getTiles(new GameTime(0));
		assertNotNull(tiles);
		assertEquals(2, tiles.length);		
		assertEquals(new Point(0,0), tiles[0]);
		assertEquals(new Point(1,0), tiles[1]);
		
	}
	
	boolean throwsException(PathOnTiles path, int enginePosition, int trainLength,
			SpeedAgainstTime speeds){
		try{
			new TrainMotion(path, enginePosition, trainLength, speeds);
			return false;
		}catch(Exception e){
			return true;
		}
	}

}
