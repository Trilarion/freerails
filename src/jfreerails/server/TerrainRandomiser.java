package jfreerails.server;

import java.util.Vector;


/**
 * Class to randomly select a terrain type for a terrain tile.
 *
 * TerrainRandomiser.java
 *
 * @author Scott Bennett <me@scottbennett.net>
 * Created on 23rd Jan 2004
 The Terrain Types are:
      0) City (Urban)
      1) Refinery (Industry)
      2) Village (Urban)
      3) Factory (Industry)
      4) Clear (Country)
      5) Farm (Country)
      6) Desert (Country)
      7) Ocean (Ocean)
      8) Harbour (Ocean)
      9) Stock-Yard (Industry)
     10) Food_Proc._Plant (Industry)
     11) Cattle_Ranch (Resource)
     12) Grain_Elevator (Resource)
     13) Oil_Well (Resource)
     14) Lumber_Mill (Resource)
     15) Sugar_Plant. (Resource)
     16) River (River)
     17) Landing (River)
     18) Terminal (Special)
     19) Jungle (Country)
     20) Hills (Hill)
     21) Foothills (Hill)
     22) Mountain (Hill)
 */
public class TerrainRandomiser {
    private final Vector terrainTypes;
    private final Vector non_terrainTypes;
    private final double CLEAR_PERCENTAGE = 0.98; //ie. % of map that is clear (on avg.)

    public TerrainRandomiser(Vector num, Vector num2) {
        terrainTypes = num;
        non_terrainTypes = num2;
    }

    public int getNewType(int type) {
        int newType = type;
        double value;
        double divide = 1.0 / terrainTypes.size();

        //allow any terrain type to be drawn over except those listed in non_terrainTypes			 
        if (!non_terrainTypes.contains(new Integer(newType))) {
            if (Math.random() < CLEAR_PERCENTAGE) {
                //make the tile Clear
                return 4;
            }
			value = Math.random();

			/*
			 * at the moment, this logic produces a balanced and even distribution of the
			 * different country tiles (currently 3). somehow it would be better to have
			 * the actual proportions of Farms, Jungle and Desert etc vary. dunno how.
			 */
			for (int i = 0; i < terrainTypes.size(); i++) {
			    if ((value > (i * divide)) &&
			            (value <= ((i + 1) * divide))) {
			        return ((Integer)terrainTypes.elementAt(i)).intValue();
			    }
			}
        }

        return newType;
    }
}