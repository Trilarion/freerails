/*
 * Created on 15-Apr-2003
 *
 */
package jfreerails.world.top;

import jfreerails.world.station.StationModel;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import java.util.NoSuchElementException;
/**
 * This junit TestCase tests NonNullElements.
 * @author Luke
 *
 */
public class NonNullElementsTest extends TestCase {
    
    World w;
    StationModel station1, station2, station3;
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite testSuite = new TestSuite(NonNullElementsTest.class);
        return testSuite;
    }
    
    
    protected void setUp(){
        w = new WorldImpl();
        station1 = new StationModel(10, 20, "Station1", 4, 0);
        station2 = new StationModel(15, 16, "Station2", 4, 1);
        station3 = new StationModel(30, 50, "Station3", 4, 2);
        w.add(KEY.STATIONS, station1);
        w.add(KEY.STATIONS, null);
        w.add(KEY.STATIONS, station2);
        w.add(KEY.STATIONS, null);
        w.add(KEY.STATIONS, null);
        w.add(KEY.STATIONS, station3);
        
    }
    
    public void testNext(){
        WorldIterator wi = new NonNullElements(KEY.STATIONS, w);
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getRowNumber());
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getIndex());
        
        //Look at first station
        boolean b = wi.next();
        assertTrue(b);
        int index = wi.getIndex();
        assertEquals(0, index);
        assertEquals(0, wi.getRowNumber());
        assertEquals(station1, wi.getElement());
        
        //Look at seond station
        assertTrue(wi.next());
        assertEquals(2, wi.getIndex());
        assertEquals(1, wi.getRowNumber());
        assertEquals(station2, wi.getElement());
        
        WorldIterator wi2 = new NonNullElements(KEY.TRACK_RULES, w);
        assertTrue(!wi2.next());
    }
    
    public void testGotoIndex(){
        WorldIterator wi = new NonNullElements(KEY.STATIONS, w);
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getRowNumber());
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getIndex());
        
        wi.gotoIndex(2);
        assertEquals(2, wi.getIndex());
        assertEquals(1, wi.getRowNumber());
        
        try{
            wi.gotoIndex(100);
            assertTrue(false);
        }catch (NoSuchElementException e){
            
        }
        
    }
}
