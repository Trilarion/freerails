package jfreerails.client.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.common.Painter;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.train.WagonType;


/**
 * Renders box showing the cargo waiting at a station.
 * @author Luke
 */
public class StationBoxRenderer implements Painter {
    private static final int WAGON_IMAGE_HEIGHT = 10;
    private static final int SPACING = 3;
    private static final int MAX_WIDTH = 80;
    private final ReadOnlyWorld w;
    private final Color bgColor;
    private final ViewLists vl;
    private final int wagonImageWidth;
    private final ModelRoot modelRoot;

    public StationBoxRenderer(ReadOnlyWorld world, ViewLists vl,
        ModelRoot modelRoot) {
        this.w = world;
        this.vl = vl;
        this.bgColor = new Color(0, 0, 200, 60);
        this.modelRoot = modelRoot;

        Image wagonImage = vl.getTrainImages().getSideOnWagonImage(0,
                WAGON_IMAGE_HEIGHT);
        wagonImageWidth = wagonImage.getWidth(null);
    }

    public void paint(Graphics2D g) {
    	Boolean showCargoWaiting = (Boolean)modelRoot.getProperty(ModelRoot.Property.SHOW_CARGO_AT_STATIONS);
    	
    	if (showCargoWaiting.booleanValue()) {
    		/* We only show the station boxes for the current player.*/
    		FreerailsPrincipal principal = modelRoot.getPrincipal();
    		WorldIterator wi = new NonNullElements(KEY.STATIONS, w,
    				principal);
    		
    		while (wi.next()) { //loop over non null stations
    			
    			StationModel station = (StationModel)wi.getElement();
    			int positionX = (station.getStationX() * 30) + 15;
    			int positionY = (station.getStationY() * 30) + 60;
    			g.setColor(bgColor);
    			g.fillRect(positionX, positionY, MAX_WIDTH,
    					5 * (WAGON_IMAGE_HEIGHT + SPACING));
    			g.setColor(Color.WHITE);
    			g.setStroke(new BasicStroke(1f));
    			g.drawRect(positionX, positionY, MAX_WIDTH,
    					5 * (WAGON_IMAGE_HEIGHT + SPACING));
    			
    			ImmutableCargoBundle cb = (ImmutableCargoBundle)w.get(KEY.CARGO_BUNDLES,
    					station.getCargoBundleID(), principal);
    			
    			for (int category = 0;
    			category < CargoType.getNumberOfCategories();
    			category++) {
    				int[] carsLoads = calculateCarLoads(cb, category);
    				int alternateWidth = (MAX_WIDTH - 2 * SPACING) / (carsLoads.length +
    						1);
    				int xOffsetPerWagon = Math.min(wagonImageWidth,
    						alternateWidth);
    				
    				for (int car = 0; car < carsLoads.length; car++) {
    					int x = positionX + (car * xOffsetPerWagon) +
						SPACING;
    					int y = positionY +
						(category * (WAGON_IMAGE_HEIGHT + SPACING));
    					int cargoType = carsLoads[car];
    					Image wagonImage = vl.getTrainImages()
						.getSideOnWagonImage(cargoType,
								WAGON_IMAGE_HEIGHT);
    					g.drawImage(wagonImage, x, y, null);
    				}
    			}
    		}
    	}
    }


    /**
        * The length of the returned array is the number of complete carloads of
        * the specified cargo category in the specified bundle.  The values
        * in the array are the type of the cargo.  E.g. if the bundle contained
        * 2 carloads of cargo type 3 and 1 of type 7, {3, 3, 7} would be returned.
        */
    private int[] calculateCarLoads(ImmutableCargoBundle cb, int category) {
        int numCargoTypes = w.size(SKEY.CARGO_TYPES);
        int numberOfCarLoads = 0;

        for (int i = 0; i < numCargoTypes; i++) {
            CargoType ct = (CargoType)w.get(SKEY.CARGO_TYPES, i);

            if (ct.getCategoryNumber() == category) {
                numberOfCarLoads += cb.getAmount(i) / WagonType.UNITS_OF_CARGO_PER_WAGON;
            }
        }

        int[] returnValue = new int[numberOfCarLoads];
        int arrayIndex = 0;

        for (int cargoType = 0; cargoType < numCargoTypes; cargoType++) {
            CargoType ct = (CargoType)w.get(SKEY.CARGO_TYPES, cargoType);

            if (ct.getCategoryNumber() == category) {
                int carsOfThisCargo = cb.getAmount(cargoType) / WagonType.UNITS_OF_CARGO_PER_WAGON;

                for (int j = 0; j < carsOfThisCargo; j++) {
                    returnValue[arrayIndex] = cargoType;
                    arrayIndex++;
                }
            }
        }

        assert returnValue.length == arrayIndex; //We should have filled up the array.

        return returnValue;
    }
}