/*
 * Created on 28-Mar-2003
 * 
 */
package jfreerails.move;

import jfreerails.WagonAndEngineTypesFactory;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;

/**
 * Junit TestCase for ChangeProductionAtEngineShopMove.
 * @author Luke
 * 
 */
public class ChangeProductionAtEngineShopMoveTest extends AbstractMoveTestCase {
	

	private ProductionAtEngineShop before;

	private ProductionAtEngineShop after;

	private int engineType;

	private int wagonType;

	private int[] wagons;

	protected void setUp() {
		super.setUp();
		w.add(KEY.STATIONS, new StationModel());
		w.add(KEY.STATIONS, new StationModel());
		w.add(KEY.STATIONS, new StationModel());
		WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
		wetf.addTypesToWorld(w);	
		engineType = 0;
		wagonType = 0;
		wagons = new int[] {wagonType, wagonType};
		after = new ProductionAtEngineShop(engineType, wagons);		
	}

	public void testMove() {
		before = null;
		ChangeProductionAtEngineShopMove m;							
				
		//Should fail because current production at station 0 is null;
		m = new ChangeProductionAtEngineShopMove(after, before, 0);
		assertTryMoveFails(m);
		assertDoMoveFails(m);
		
		//Should fail because station 6 does not exist.
		m = new ChangeProductionAtEngineShopMove(before, after, 6);
		assertTryMoveFails(m);
		assertDoMoveFails(m);
				
		//Should go through
		m = new ChangeProductionAtEngineShopMove(before, after, 0);
		assertTryMoveIsOk(m);
		assertDoMoveIsOk(m);
		assertTryUndoMoveIsOk(m);
		assertUndoMoveIsOk(m);
						
		//It should not be repeatable.
		assertOkButNotRepeatable(m);
				
	}	
	
	public void testProductionAtEngineShopEquals(){
		ProductionAtEngineShop a,b,c,d;
		a = null;
		b = new ProductionAtEngineShop(engineType, wagons);
		c = new ProductionAtEngineShop(engineType, wagons);
		assertEquals(c,b);
		assertEquals(b,c);			
	}	
}
