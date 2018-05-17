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

import freerails.io.GsonManager;
import freerails.model.finances.Money;
import freerails.model.world.SharedKey;
import freerails.model.world.World;
import freerails.model.terrain.TerrainCategory;
import freerails.model.track.*;
import org.xml.sax.Attributes;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Processes Track_TilesHandle events, generates track rules, and provides a
 * methods to add the track rules to the world object.
 *
 * @see TrackTilesXmlParser
 */
// TODO difference between interface and implementation
public class TrackTilesXmlHandlerImpl implements TrackTilesXmlHandler {

    private List<TrackRule> ruleList;
    private ValidTrackConfigurations validTrackConfigurations;
    private ArrayList<String> legalTemplates;
    private HashSet<TerrainCategory> terrainTypes;
    private ValidTrackPlacement validTrackPlacement;
    private TrackCategory category;
    private String name;
    private boolean doubleTracked;
    private int stationRadius;
    private Money price, maintenance, fixedCost;

    /**
     * @param trackXmlUrl
     */
    public TrackTilesXmlHandlerImpl(URL trackXmlUrl) {
        try {
            TrackTilesXmlParser.parse(trackXmlUrl, this);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public void startCanOnlyBuildOnTheseTerrainTypes(final Attributes attributes) {
        terrainTypes = new HashSet<>();
    }

    public void endCanOnlyBuildOnTheseTerrainTypes() {
        validTrackPlacement = new ValidTrackPlacement(terrainTypes, true);
        terrainTypes = null;
    }

    public void startListOfTrackPieceTemplates(final Attributes attributes) {
        legalTemplates = new ArrayList<>();
    }

    public void endListOfTrackPieceTemplates() {
        validTrackConfigurations = new ValidTrackConfigurations(legalTemplates);
        legalTemplates = null;
    }

    public void startCannotBuildOnTheseTerrainTypes(final Attributes attributes) {
        terrainTypes = new java.util.HashSet<>();
    }

    public void endCannotBuildOnTheseTerrainTypes() {
        validTrackPlacement = new ValidTrackPlacement(terrainTypes, false);
        terrainTypes = null;
    }

    public void startTrackType(final Attributes attributes) {
        category = TrackCategory.valueOf(attributes.getValue("category"));

        doubleTracked = Boolean.valueOf(attributes.getValue("doubleTrack"));
        name = attributes.getValue("type");

        String stationRadiusString = attributes.getValue("stationRadius");

        if (null != stationRadiusString) {
            stationRadius = Integer.parseInt(stationRadiusString);
        } else {
            stationRadius = 0;
        }

        String priceString = attributes.getValue("price");
        price = new Money(Integer.parseInt(priceString));

        String fixedCostString = attributes.getValue("fixedCost");

        if (null != fixedCostString) {
            fixedCost = new Money(Integer.parseInt(fixedCostString));
        } else {
            fixedCost = Money.ZERO;
        }

        String maintenanceString = attributes.getValue("maintenance");
        maintenance = new Money(Integer.parseInt(maintenanceString));
    }

    public void endTrackType() {
        TrackRule trackRuleImpl = new TrackRule(validTrackConfigurations, validTrackPlacement, category, name, doubleTracked, stationRadius, maintenance, price, fixedCost);
        ruleList.add(trackRuleImpl);

        validTrackConfigurations = null;
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
     * @param world
     */
    public void addTrackRules(World world) {
        
        for (TrackRule trackRule : ruleList) {
            world.add(SharedKey.TrackRules, trackRule);
        }
        /*
        // with storage
        SortedSet<TrackType> trackTypes = new TreeSet<>();
        for (int i = 0; i < ruleList.size(); i++) {
            TrackRule t = ruleList.get(i);
            world.add(SharedKey.TrackRules, t);
            Set<TrackProperty> propertyList = new TreeSet<>();
            switch (t.getCategory()) {
                case BRIDGE:
                    propertyList.add(TrackProperty.BRIDGE);
                    break;
                case TUNNEL:
                    propertyList.add(TrackProperty.TUNNEL);
                    break;
            }
            propertyList.add(t.isDouble() ? TrackProperty.DOUBLE : TrackProperty.SINGLE);
            TrackType trackType = new TrackType(i, t.getName(), t.getCategory(), propertyList, t.getMaintenanceCost(), t.getPrice(), t.getValidTrackPlacement().getTerrainTypes(), t.getValidTrackConfigurations().getLegalTrackStrings());
            trackType.prepare();
            trackTypes.add(trackType);
        }
        File file = new File("track_types.json");
        try {
            GsonManager.save(file, trackTypes);
        } catch (IOException e) {
            e.printStackTrace();
        }/**/
    }

    /**
     * @return
     */
    public List<TrackRule> getRuleList() {
        return ruleList;
    }

    public ValidTrackPlacement getValidTrackPlacement() {
        return validTrackPlacement;
    }
}