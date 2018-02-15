/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.savegames;

import freerails.model.world.WorldSharedKey;
import freerails.model.world.World;
import freerails.model.terrain.TerrainCategory;
import freerails.model.track.*;
import org.xml.sax.Attributes;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Processes Track_TilesHandle events, generates track rules, and provides a
 * methods to add the track rules to the world object.
 *
 * @see TrackTilesXmlParser
 */
// TODO difference between interface and implementation
public class TrackTilesXmlHandlerImpl implements TrackTilesXmlHandler {

    private List<TrackRule> ruleList;
    private TrackRuleProperties trackRuleProperties;
    private ValidTrackConfigurations validTrackConfigurations;
    private ArrayList<String> legalTemplates;
    private HashSet<TerrainCategory> terrainTypes;
    private ValidTrackPlacement validTrackPlacement;
    private int maxConsequ;

    /**
     * @param trackXmlUrl
     */
    public TrackTilesXmlHandlerImpl(URL trackXmlUrl) {
        try {
            TrackTilesXmlParser.parse(trackXmlUrl, this);
        } catch (Exception ignored) {}
    }

    public void startCanOnlyBuildOnTheseTerrainTypes(final Attributes attributes) {
        terrainTypes = new HashSet<>();
    }

    public void endCanOnlyBuildOnTheseTerrainTypes() {
        validTrackPlacement = new ValidTrackPlacement(terrainTypes, PlacementRule.ONLY_ON_THESE);
        terrainTypes = null;
    }

    public void startListOfTrackPieceTemplates(final Attributes attributes) {
        legalTemplates = new ArrayList<>();
    }

    public void endListOfTrackPieceTemplates() {
        validTrackConfigurations = new ValidTrackConfigurations(maxConsequ, legalTemplates);
        legalTemplates = null;
    }

    public void startCannotBuildOnTheseTerrainTypes(final Attributes attributes) {
        terrainTypes = new java.util.HashSet<>();
    }

    public void endCannotBuildOnTheseTerrainTypes() {
        validTrackPlacement = new ValidTrackPlacement(terrainTypes, PlacementRule.ANYWHERE_EXCEPT_ON_THESE);
        terrainTypes = null;
    }

    public void startTrackType(final Attributes attributes) {
        int rGBvalue;
        String rgbString = attributes.getValue("RGBvalue");
        rGBvalue = Integer.parseInt(rgbString, 16);

        /*
         * We need to change the format of the rgb value to the same one as used
         * by the the BufferedImage that stores the map. See
         * freerails.common.Map
         */
        rGBvalue = new Color(rGBvalue).getRGB();

        TrackCategories category = TrackCategories.valueOf(attributes.getValue("category"));

        boolean enableDoubleTrack = Boolean.valueOf(attributes.getValue("doubleTrack"));
        String typeName = attributes.getValue("type");
        maxConsequ = Integer.parseInt(attributes.getValue("maxConsecutivePieces"));

        String stationRadiusString = attributes.getValue("stationRadius");
        int stationRadius;

        if (null != stationRadiusString) {
            stationRadius = Integer.parseInt(stationRadiusString);
        } else {
            stationRadius = 0;
        }

        String priceString = attributes.getValue("price");
        int price = Integer.parseInt(priceString);

        String fixedCostString = attributes.getValue("fixedCost");
        int fixedCost;

        if (null != fixedCostString) {
            fixedCost = Integer.parseInt(fixedCostString);
        } else {
            fixedCost = 0;
        }

        String maintenanceString = attributes.getValue("maintenance");
        int maintenance = Integer.parseInt(maintenanceString);

        trackRuleProperties = new TrackRuleProperties(rGBvalue, enableDoubleTrack, typeName, category, stationRadius, price, maintenance, fixedCost);
    }

    public void endTrackType() {
        TrackRule trackRuleImpl = new freerails.model.track.TrackRuleImpl(trackRuleProperties, validTrackConfigurations, validTrackPlacement);
        ruleList.add(trackRuleImpl);

        validTrackConfigurations = null;
        trackRuleProperties = null;
        validTrackPlacement = null;
    }

    public void handleTerrainType(final Attributes attributes) {
        TerrainCategory cat = TerrainCategory.valueOf(attributes.getValue("name"));
        terrainTypes.add(cat);
    }

    public void endTiles() {
        // Sort the track tiles by category then price.
        Collections.sort(ruleList);
    }

    public void startTrackPieceTemplate(final Attributes attributes) {
        legalTemplates.add(attributes.getValue("trackTemplate"));
    }

    public void startTrackSet(final Attributes attributes) {
        ruleList = new ArrayList<>();
    }

    /**
     * @param w
     */
    public void addTrackRules(World w) {
        for (TrackRule r : ruleList) {
            w.add(WorldSharedKey.TrackRules, r);
        }
    }

    /**
     * @return
     */
    public List<TrackRule> getRuleList() {
        return ruleList;
    }
}