
/*
 * ChangeTrackPieceCompositeMoveTest.java
 * JUnit based test
 *
 * Created on 26 January 2002, 00:33
 */
 

package jfreerails.move;

import java.awt.Dimension;
import java.awt.Point;

import jfreerails.world.track.TrackRule;
import jfreerails.world.track.TrackRuleList;
import jfreerails.world.misc.OneTileMoveVector;
import jfreerails.world.track.NullTrackPiece;
import jfreerails.world.track.TrackTileMap;
import jfreerails.world.track.TrackTileMapImpl;
import jfreerails.world.track.MapFixtureFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author lindsal
 */
public class ChangeTrackPieceCompositeMoveTest extends TestCase {
    
    TrackTileMap trackTileMap;
    TrackRuleList trackRuleList;
    
    public ChangeTrackPieceCompositeMoveTest(java.lang.String testName) {
        super(testName);
        
    }
    
    public static void main(java.lang.String[] args) {
    	        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite testSuite = new TestSuite(ChangeTrackPieceCompositeMoveTest.class);
        return testSuite;
    }
     protected void setUp(){
         trackRuleList=MapFixtureFactory.generateTrackRuleList();       
         trackTileMap=new TrackTileMapImpl(new Dimension(10,10));
    }
    
    public void testRemoveTrack() {
        OneTileMoveVector east = OneTileMoveVector.EAST;
        OneTileMoveVector west = OneTileMoveVector.WEST;
        
        TrackRule trackRule = trackRuleList.getTrackRule(0);
        
        assertBuildTrackSuceeds(new Point(0, 5), east, trackRule);
        
        assertBuildTrackSuceeds(new Point(0, 6), east, trackRule);
        assertBuildTrackSuceeds(new Point(1, 6), east, trackRule);
        
        assertBuildTrackSuceeds(new Point(0, 7), east, trackRule);
        assertBuildTrackSuceeds(new Point(1, 7), east, trackRule);
        assertBuildTrackSuceeds(new Point(2, 7), east, trackRule);
        
        //Remove only track piece built.
        
        assertRemoveTrackSuceeds(new Point(0, 5), east);
        assertTrue(NullTrackPiece.getInstance() == trackTileMap.getTrackPiece(new Point(0, 5)));
        assertTrue(NullTrackPiece.getInstance() == trackTileMap.getTrackPiece(new Point(1, 5)));
        
        
        
        //Try to remove non existent track piece
        
        assertTrue(NullTrackPiece.getInstance() == trackTileMap.getTrackPiece(new Point(0, 5)));
        assertRemoveTrackFails(new Point(0, 5), east);
        
        
    }
    
    public void testBuildTrack() {
        Point pointA = new Point(0, 0);
        Point pointB = new Point(1, 1);
        Point pointC = new Point(1, 0);
        OneTileMoveVector southeast = OneTileMoveVector.SOUTH_EAST;
        OneTileMoveVector east = OneTileMoveVector.EAST;
        OneTileMoveVector northeast = OneTileMoveVector.NORTH_EAST;
        OneTileMoveVector south = OneTileMoveVector.SOUTH;
        OneTileMoveVector west = OneTileMoveVector.WEST;
        
        TrackRule trackRule = trackRuleList.getTrackRule(0);
        
        //First track piece built
        assertBuildTrackSuceeds(pointA, southeast, trackRule);
        
        //Track connected from one existing track piece
        assertBuildTrackSuceeds(pointB, northeast, trackRule);
        
        //Track connected to one existing track piece
        assertBuildTrackSuceeds(pointC, east, trackRule);
        
        //Track connecting two existing track pieces.
        assertBuildTrackSuceeds(pointA, east, trackRule);
        
        //Track off map.. should fail.
        assertBuildTrackFails(pointA, northeast, trackRule);
        
        //Track already there.
        assertBuildTrackFails(pointA, southeast, trackRule);
        
        //Illegal config. connecting from one existing track piece
        assertBuildTrackFails(pointA, south, trackRule);
        
        //Illegal config. connecting to one existing track piece
        assertBuildTrackFails(new Point(0, 1), northeast, trackRule);
        
        //Illegal config. connecting between two existing track pieces
        assertBuildTrackFails(pointC, south, trackRule);
        
        //Not allowed on this terrain type, from existing track.
        assertBuildTrackFails(
        new Point(2, 0),
        northeast,
        trackRuleList.getTrackRule(1));
        
        //Implement these tests later.
        //Not allowed on this terrain type, to existing track.
        //assertBuildTrackFails(new Point(3, 0), west, trackRuleList.getTrackRule(1));
        
        //Not allowed on this terrain type, first track piece built.
        //assertBuildTrackFails(new Point(3, 1), east, trackRuleList.getTrackRule(1));
    }
    
    private void assertBuildTrackFails(Point p, OneTileMoveVector v, TrackRule rule){
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove( p,  v,  rule, trackTileMap);
        MoveStatus status=move.doMove(trackTileMap);
        assertEquals(false, status.isOk());
      
    }
    
    private void assertBuildTrackSuceeds(Point p, OneTileMoveVector v, TrackRule rule){
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove( p,  v,  rule, trackTileMap);
        MoveStatus status=move.doMove(trackTileMap);
        assertEquals(true, status.isOk());
    }
    private void assertRemoveTrackFails(Point p, OneTileMoveVector v){
         ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove( p,  v, trackTileMap);
        MoveStatus status=move.doMove(trackTileMap);
        assertEquals(false, status.isOk());
    }
    
    private void assertRemoveTrackSuceeds(Point p, OneTileMoveVector v){
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove( p,  v, trackTileMap);
        MoveStatus status=move.doMove(trackTileMap);
        assertEquals(true, status.isOk());
    }
    
}
