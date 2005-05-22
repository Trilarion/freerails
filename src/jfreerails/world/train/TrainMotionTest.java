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
	
	public void testNext(){
		OneTileMoveVector[] vectors = {EAST, EAST, EAST};
		PathOnTiles path = new PathOnTiles(new Point(0,0), vectors);
		int trainLength = TrainModel.WAGON_LENGTH;
		TrainMotion motion = new TrainMotion(path, 1, trainLength, SpeedAgainstTime.STOPPED);
		PathOnTiles tiles = motion.getTiles(new GameTime(0));
		GameTime[] times = {new GameTime(0), new GameTime(3)};
		int[] speeds = {10, 10};
		SpeedAgainstTime newSpeeds = new SpeedAgainstTime(times, speeds);
		
		TrainMotion motion2 = motion.next(newSpeeds, OneTileMoveVector.EAST);
		tiles = motion2.getTiles(new GameTime(0));
		assertNotNull(tiles);
		assertEquals(1, tiles.steps());		
		assertEquals(new Point(0,0), tiles.getStart());
		assertEquals(EAST, tiles.getStep(0));
		
		tiles = motion2.getTiles(new GameTime(1));
		assertNotNull(tiles);
		assertEquals(2, tiles.steps());		
		assertEquals(new Point(0,0), tiles.getStart());
		assertEquals(EAST, tiles.getStep(0));
		assertEquals(EAST, tiles.getStep(1));
	}
	
	public void testGetTiles(){
		OneTileMoveVector[] vectors = {EAST, EAST, EAST};
		PathOnTiles path = new PathOnTiles(new Point(0,0), vectors);
		int trainLength = TrainModel.WAGON_LENGTH;
		TrainMotion motion = new TrainMotion(path, 1, trainLength, SpeedAgainstTime.STOPPED);
		PathOnTiles tiles = motion.getTiles(new GameTime(0));
		assertNotNull(tiles);
		assertEquals(1, tiles.steps());		
		assertEquals(new Point(0,0), tiles.getStart());
		assertEquals(EAST, tiles.getStep(0));
		
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
