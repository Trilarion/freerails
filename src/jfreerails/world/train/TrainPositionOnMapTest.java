package jfreerails.world.train;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import junit.framework.TestCase;

/**
 * @author Luke Lindsay 26-Oct-2002
 *
 */
public class TrainPositionOnMapTest extends TestCase {

	/**
	 * Constructor for TrainPositionTest.
	 * @param arg0
	 */
	public TrainPositionOnMapTest(String arg0) {
		super(arg0);
	}

	public void testGetLength() {
		TrainPositionOnMap a;
		a=TrainPositionOnMap.createInstance(new int[] {10,20,30,40}, new int[]{11,22,33,44});
		assertEquals(4, a.getLength());
	}

	public void testGetPoint() {
		TrainPositionOnMap a;
		a=TrainPositionOnMap.createInstance(new int[] {10,20}, new int[]{11,22});
		
		assertEquals(a.getX(0), 10);
		assertEquals(a.getY(0), 11);		
		
		assertEquals(a.getX(1), 20);
		assertEquals(a.getY(1), 22);
	}

	public void testPath() {
		TrainPositionOnMap a;
		a=TrainPositionOnMap.createInstance(new int[] {10,20,30,40}, new int[]{11,22,33,44});
		FreerailsPathIterator path=a.path();
		IntLine line=new IntLine();
		assertTrue(path.hasNext());
		path.nextSegment(line);
		assertEquals(line, new IntLine(10, 11, 20, 22));
		assertTrue(path.hasNext());
		path.nextSegment(line);
		assertEquals(line, new IntLine(20, 22, 30 ,33));
		assertTrue(path.hasNext());
		path.nextSegment(line);
		assertEquals(line, new IntLine(30 ,33, 40 ,44));
		assertTrue(!path.hasNext());
		
	}

	public void testReversePath() {
		TrainPositionOnMap a;
		a=TrainPositionOnMap.createInstance(new int[] {40,30,20,10}, new int[]{44, 33, 22, 11});
		FreerailsPathIterator path=a.reversePath();
		IntLine line=new IntLine();
		assertTrue(path.hasNext());
		path.nextSegment(line);
		assertEquals(line, new IntLine(10, 11, 20, 22));
		assertTrue(path.hasNext());
		path.nextSegment(line);
		assertEquals(line, new IntLine(20, 22, 30 ,33));
		assertTrue(path.hasNext());
		path.nextSegment(line);
		assertEquals(line, new IntLine(30 ,33, 40 ,44));
		assertTrue(!path.hasNext());
	}

	/*
	 * Test for TrainPosition createInstance(int[], int[])
	 */
	public void testCreateInstanceIArrayIArray() {
		TrainPositionOnMap a;
		try{
			a=TrainPositionOnMap.createInstance(new int[] {40,30,20,10}, new int[]{44, 33, 22, 11});
		}catch (Exception e){
			assertTrue(false);
		}
		try{
			a=TrainPositionOnMap.createInstance(new int[] {40,30,20}, new int[]{44, 33, 22, 11});
			assertTrue(false);
		}catch (Exception e){
			
		}
		
	}
	/*
	public void testAdd() {
		TrainPosition a, b, c, d, e, f, g, h , i, j;
		a=TrainPosition.createInstance(new int[] {10,20}, new int[]{11,22});
		b=TrainPosition.createInstance(new int[] {20, 30}, new int[]{22,33});
		c=TrainPosition.createInstance(new int[] {10, 30}, new int[]{11, 33});
		
		d=TrainPosition.add(a, b);
		assertEquals(d, c);
		e=TrainPosition.add(b, a);
		assertEquals(e, c);
		
		
		f = TrainPosition.createInstance(
				new int[] { 40, 50 },
				new int[] { 44, 55 });
		g = TrainPosition.createInstance(
			new int[] { 10,  30, 40 },
			new int[] { 11,  33, 44 });
			
		i = TrainPosition.createInstance(
			new int[] { 10,  30, 50 },
			new int[] { 11,  33, 55 });
		j = TrainPosition.add(f, g);
		assertEquals(i, j);
			
			
				
		
	}
	*/


	/*
	public void testRemove() {
		TrainPosition a, b, c, d, e, f, g, h , i, j , k;
		a=TrainPosition.createInstance(new int[] {10,20 ,40 , 50, 60}, new int[]{11,22, 44, 55 , 66});
		b=TrainPosition.createInstance(new int[] {10, 20, 30}, new int[]{11,22,33});
		c=TrainPosition.createInstance(new int[] {48, 50, 60}, new int[]{49,55, 66});
		
		d=TrainPosition.createInstance(new int[] {30, 40 , 50,  60}, new int[]{33, 44, 55, 66});
		e=TrainPosition.createInstance(new int[] {10, 20, 40, 48}, new int[]{11, 22, 44, 49});	
		
		f=TrainPosition.remove(a, b);
		assertEquals(f, d);
		 
		g=TrainPosition.remove(a, c);
		assertEquals(g, e);			
		
		h = TrainPosition.createInstance(
			new int[] { 10,  30, 50 },
			new int[] { 11,  33, 55 });
			
		i = TrainPosition.createInstance(
				new int[] { 10, 20 },
				new int[] { 11, 22 });
		
		j = TrainPosition.createInstance(
				new int[] { 20,  30, 50 },
				new int[] { 22,  33, 55 });
				
		k = TrainPosition.remove(h, i);
		
		assertEquals(k, j);	
				
	}
	
	*/
	/*
	public void testCanBeAdded() {
		TrainPosition a, b, c, d;
		a=TrainPosition.createInstance(new int[] {10,20}, new int[]{11,22});
		b=TrainPosition.createInstance(new int[] {20, 30}, new int[]{22,33});
		c=TrainPosition.createInstance(new int[] {30, 40}, new int[]{33,44});
		
		assertTrue(TrainPosition.canBeAdded(a, b));
		assertTrue(TrainPosition.canBeAdded(b, a));
		assertTrue(TrainPosition.canBeAdded(b, c));
		assertTrue(!TrainPosition.canBeAdded(c, b));
		
		assertTrue(!TrainPosition.canBeAdded(a, c));
		assertTrue(!TrainPosition.canBeAdded(c, a));
		
		//Test that we cannot add a position to itself
		assertTrue(!TrainPosition.canBeAdded(a, a));
		assertTrue(!TrainPosition.canBeAdded(b, b));
		assertTrue(!TrainPosition.canBeAdded(c, c));
	}
	*/

	/*
	public void testCanBeRemoved() {
		TrainPosition a, b, c, d;
		a=TrainPosition.createInstance(new int[] {10,20 ,40 , 50}, new int[]{11,22, 44, 55});
		b=TrainPosition.createInstance(new int[] {10, 20, 30}, new int[]{11,22,33});
		c=TrainPosition.createInstance(new int[] {30, 40, 50}, new int[]{33,44,55});
		
		assertTrue(TrainPosition.canBeRemoved(a, b));
		
		assertTrue(!TrainPosition.canBeRemoved(b, a));
		
		assertTrue(TrainPosition.canBeRemoved(a, c));
		
		assertTrue(!TrainPosition.canBeRemoved(c, a));
		
		//Test that we cannot remove a position from itself
		assertTrue(!TrainPosition.canBeRemoved(a, a));
		assertTrue(!TrainPosition.canBeRemoved(b, b));
		assertTrue(!TrainPosition.canBeRemoved(c, c));				
	}
	*/
	
	public void testAddToHead(){
		TrainPositionOnMap a, b, c, d, e, f, g, h , i, j;
		a=TrainPositionOnMap.createInstance(new int[] {10,20}, new int[]{11,22});
		b=TrainPositionOnMap.createInstance(new int[] {20, 30}, new int[]{22,33});
		c=TrainPositionOnMap.createInstance(new int[] {10, 30}, new int[]{11, 33});
		
		d=b.addToHead(a);
		assertEquals(d, c);
				
		
		f = TrainPositionOnMap.createInstance(
				new int[] { 40, 50 },
				new int[] { 44, 55 });
		g = TrainPositionOnMap.createInstance(
			new int[] { 10,  30, 40 },
			new int[] { 11,  33, 44 });
			
		i = TrainPositionOnMap.createInstance(
			new int[] { 10,  30, 50 },
			new int[] { 11,  33, 55 });
		j = f.addToHead(g);
		assertEquals(i, j);
		
	}
	
	public void testCanAddToHead(){
		TrainPositionOnMap a, b, c, d;
		a=TrainPositionOnMap.createInstance(new int[] {10,20}, new int[]{11,22});
		b=TrainPositionOnMap.createInstance(new int[] {20, 30}, new int[]{22,33});
		c=TrainPositionOnMap.createInstance(new int[] {30, 40}, new int[]{33,44});
		
		assertTrue(b.canAddToHead(a));
		assertTrue(!a.canAddToHead(b));
		
		assertTrue(c.canAddToHead(b));
		assertTrue(!b.canAddToHead(c));
		
		assertTrue(!c.canAddToHead(a));
		assertTrue(!a.canAddToHead(c));
		
		
	}
	
	public void testAddToTail(){
		TrainPositionOnMap a, b, c, d, e, f, g, h , i, j;
		a=TrainPositionOnMap.createInstance(new int[] {10,20}, new int[]{11,22});
		b=TrainPositionOnMap.createInstance(new int[] {20, 30}, new int[]{22,33});
		c=TrainPositionOnMap.createInstance(new int[] {10, 30}, new int[]{11, 33});
		
		d=a.addToTail(b);
		assertEquals(d, c);
	
		
		
		f = TrainPositionOnMap.createInstance(
				new int[] { 40, 50 },
				new int[] { 44, 55 });
		g = TrainPositionOnMap.createInstance(
			new int[] { 10,  30, 40 },
			new int[] { 11,  33, 44 });
			
		i = TrainPositionOnMap.createInstance(
			new int[] { 10,  30, 50 },
			new int[] { 11,  33, 55 });
		j = g.addToTail(f);
		assertEquals(i, j);
		
	}
	
	public void testCanAddToTail(){
		TrainPositionOnMap a, b, c, d;
		a=TrainPositionOnMap.createInstance(new int[] {10,20}, new int[]{11,22});
		b=TrainPositionOnMap.createInstance(new int[] {20, 30}, new int[]{22,33});
		c=TrainPositionOnMap.createInstance(new int[] {30, 40}, new int[]{33,44});
		
			
		assertTrue(!b.canAddToTail(a));
		assertTrue(a.canAddToTail(b));
		
		assertTrue(!c.canAddToTail(b));
		assertTrue(b.canAddToTail(c));
		
		assertTrue(!c.canAddToTail(a));
		assertTrue(!a.canAddToTail(c));				
	
	}
	
	public void testRemoveFromHead(){
		TrainPositionOnMap a, b, c, d, e, f, g, h , i, j , k;
		a=TrainPositionOnMap.createInstance(new int[] {10,20 ,40 , 50, 60}, new int[]{11,22, 44, 55 , 66});
		b=TrainPositionOnMap.createInstance(new int[] {10, 20, 30}, new int[]{11,22,33});
		c=TrainPositionOnMap.createInstance(new int[] {48, 50, 60}, new int[]{49,55, 66});
		
		d=TrainPositionOnMap.createInstance(new int[] {30, 40 , 50,  60}, new int[]{33, 44, 55, 66});
		e=TrainPositionOnMap.createInstance(new int[] {10, 20, 40, 48}, new int[]{11, 22, 44, 49});	
		
	}
	
	public void testCanRemoveFromHead(){
		TrainPositionOnMap a, b, c, d;
		a=TrainPositionOnMap.createInstance(new int[] {10,20 ,40 , 50}, new int[]{11,22, 44, 55});
		b=TrainPositionOnMap.createInstance(new int[] {10, 20, 30}, new int[]{11,22,33});
		c=TrainPositionOnMap.createInstance(new int[] {30, 40, 50}, new int[]{33,44,55});
					
		assertTrue(!b.canRemoveFromHead(a));
		assertTrue(a.canRemoveFromHead(b));
		
		assertTrue(!c.canRemoveFromHead(b));
		assertTrue(!b.canRemoveFromHead(c));
		
		assertTrue(!c.canRemoveFromHead(a));
		assertTrue(!a.canRemoveFromHead(c));		
					
	}
	
	public void testRemoveFromTail(){
		TrainPositionOnMap a, b, c, d, e, f, g, h , i, j , k;
		a=TrainPositionOnMap.createInstance(new int[] {10,20 ,40 , 50, 60}, new int[]{11,22, 44, 55 , 66});
		b=TrainPositionOnMap.createInstance(new int[] {10, 20, 30}, new int[]{11,22,33});
		c=TrainPositionOnMap.createInstance(new int[] {48, 50, 60}, new int[]{49,55, 66});
		
		d=TrainPositionOnMap.createInstance(new int[] {30, 40 , 50,  60}, new int[]{33, 44, 55, 66});
		e=TrainPositionOnMap.createInstance(new int[] {10, 20, 40, 48}, new int[]{11, 22, 44, 49});
		
		f = a.removeFromTail(c);
		assertEquals(e, f);	
		
	}
	
	public void testCanRemoveFromTail(){
		TrainPositionOnMap a, b, c, d;
		a=TrainPositionOnMap.createInstance(new int[] {10,20 ,40 , 50}, new int[]{11,22, 44, 55});
		b=TrainPositionOnMap.createInstance(new int[] {10, 20, 30}, new int[]{11,22,33});
		c=TrainPositionOnMap.createInstance(new int[] {30, 40, 50}, new int[]{33,44,55});	
		
		assertTrue(!b.canRemoveFromTail(a));
		assertTrue(!a.canRemoveFromTail(b));
		
		assertTrue(!c.canRemoveFromTail(b));
		assertTrue(!b.canRemoveFromTail(c));
		
		assertTrue(!c.canRemoveFromTail(a));
		assertTrue(a.canRemoveFromTail(c));		
	}
	
	public void testEquals(){
		
		TrainPositionOnMap a, b, c;
		a=TrainPositionOnMap.createInstance(new int[] {10,20}, new int[]{11,22});
		b=TrainPositionOnMap.createInstance(new int[] {10,20}, new int[]{11,22});
		c=TrainPositionOnMap.createInstance(new int[] {30,40}, new int[]{33,44});	
		
		assertTrue(!a.equals(null));
		assertTrue(!a.equals(new Object()));
		assertTrue(a.equals(a));
		assertTrue(a.equals(b));
		assertTrue(!a.equals(c));
		
	}

	/*
	 * Test for TrainPosition createInstance(FreerailsPathIterator)
	 */
	public void testCreateInstanceFreerailsPathIterator() {
		FreerailsPathIterator path = new SimplePathIteratorImpl(new int[] {40,30,20,10}, new int[]{44, 33, 22, 11});
		TrainPositionOnMap a = TrainPositionOnMap.createInSameDirectionAsPath(path);
	
		assertEquals(a.getLength(), 4);
		
		assertEquals(a.getX(0), 40);
		assertEquals(a.getY(0), 44);	
						
		assertEquals(a.getX(1), 30);
		assertEquals(a.getY(1), 33);	
				
		assertEquals(a.getX(2), 20);
		assertEquals(a.getY(2), 22);	
						
		assertEquals(a.getX(3), 10);
		assertEquals(a.getY(3), 11);			
	}
	
	public void testCreateInOppositeDirectionToPath(){
		
		FreerailsPathIterator path = new SimplePathIteratorImpl(new int[] {40,30,20,10}, new int[]{44, 33, 22, 11});
		TrainPositionOnMap a = TrainPositionOnMap.createInOppositeDirectionToPath(path);
	
		assertEquals(a.getLength(), 4);
		
		assertEquals(a.getX(3), 40);
		assertEquals(a.getY(3), 44);	
						
		assertEquals(a.getX(2), 30);
		assertEquals(a.getY(2), 33);	
				
		assertEquals(a.getX(1), 20);
		assertEquals(a.getY(1), 22);	
						
		assertEquals(a.getX(0), 10);
		assertEquals(a.getY(0), 11);							
	}		
}
