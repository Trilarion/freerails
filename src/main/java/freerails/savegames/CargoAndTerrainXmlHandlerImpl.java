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

package freerails.savegames;

import freerails.util.ui.UiUtils;
import freerails.model.world.SharedKey;
import freerails.model.world.World;
import freerails.model.terrain.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.Serializable;
import java.util.*;

/**
 * Processes CargoAndTerrainHandler events and adds terrain and cargo types to
 * the world object.
 *
 * @see CargoAndTerrainXmlHandler
 * @see CargoAndTerrainXmlParser
 */
// TODO difference between interface and implementation?
public class CargoAndTerrainXmlHandlerImpl implements CargoAndTerrainXmlHandler {

    private final HashMap<String, Integer> cargoNameToCargoTypeNumber = new HashMap<>();
    {
        cargoNameToCargoTypeNumber.put("Mail", 0);
        cargoNameToCargoTypeNumber.put("Passengers", 1);
        cargoNameToCargoTypeNumber.put("Food", 2);
        cargoNameToCargoTypeNumber.put("Livestock", 3);
        cargoNameToCargoTypeNumber.put("Mfg._Goods", 4);
        cargoNameToCargoTypeNumber.put("Grain", 5);
        cargoNameToCargoTypeNumber.put("Coffee", 6);
        cargoNameToCargoTypeNumber.put("Export_Goods", 7);
        cargoNameToCargoTypeNumber.put("Petroleum", 8);
        cargoNameToCargoTypeNumber.put("Oil", 9);
        cargoNameToCargoTypeNumber.put("Wood", 10);
        cargoNameToCargoTypeNumber.put("Sugar", 11);
    }
    private final Collection<Integer> rgbValuesAlreadyUsed = new HashSet<>();
    private final List<TileConsumption> typeConsumes = new ArrayList<>();
    private final List<TileProduction> typeProduces = new ArrayList<>();
    private final List<TileConversion> typeConverts = new ArrayList<>();
    private final World world;

    // Parsing variables for Tile
    private String tileID;
    private TerrainCategory tileCategory;
    private int tileRGB;
    private int tileROW;
    private int tileBuildCost;

    /**
     * @param world
     */
    public CargoAndTerrainXmlHandlerImpl(World world) {
        this.world = world;
    }

    public void handleConversions(final Attributes attributes) throws SAXException {
        String inputCargo = attributes.getValue("input");
        String outputCargo = attributes.getValue("output");

        int input = stringToCargoID(inputCargo);
        int output = stringToCargoID(outputCargo);
        TileConversion tileConversion = new TileConversion(input, output);
        typeConverts.add(tileConversion);
    }

    public void startTile(final Attributes attributes) throws SAXException {
        typeConsumes.clear();
        typeProduces.clear();
        typeConverts.clear();

        tileID = attributes.getValue("id");
        tileCategory = TerrainCategory.valueOf(attributes.getValue("Category"));

        String rgbString = attributes.getValue("rgb");
        tileRGB = UiUtils.stringToRGBValue(rgbString);

        String buildCostString = attributes.getValue("build_cost");

        if (null != buildCostString) {
            tileBuildCost = Integer.parseInt(buildCostString);
        } else {
            tileBuildCost = -1;
        }

        // Check if another type is already using this rgb value..
        Integer rgbInteger = tileRGB;

        if (rgbValuesAlreadyUsed.contains(rgbInteger)) {
            throw new SAXException(tileID + " can't using rgb value " + rgbString + " because it is being used by another tile type!");
        }
        rgbValuesAlreadyUsed.add(rgbInteger);

        tileROW = Integer.parseInt(attributes.getValue("right-of-way"));
    }

    public void endTile() {
        Serializable tileType = new TerrainType(tileRGB, tileCategory, tileID, tileROW, Collections.unmodifiableList(typeProduces), Collections.unmodifiableList(typeConsumes), Collections.unmodifiableList(typeConverts), tileBuildCost);
        world.add(SharedKey.TerrainTypes, tileType);
    }

    public void handleConsumptions(final Attributes attributes) throws SAXException {
        int cargoConsumed = stringToCargoID(attributes.getValue("Cargo"));
        String prerequisiteString = attributes.getValue("Prerequisite");

        // "Prerequisite" is an optional attribute, so may be null.
        int prerequisiteForConsumption = (null == prerequisiteString ? 1 : Integer.parseInt(prerequisiteString));
        TileConsumption tileConsumption = new TileConsumption(cargoConsumed, prerequisiteForConsumption);
        typeConsumes.add(tileConsumption);
    }

    public void handleProductions(final Attributes attributes) throws SAXException {
        int cargoProduced = stringToCargoID(attributes.getValue("Cargo"));
        int rateOfProduction = Integer.parseInt(attributes.getValue("Rate"));
        TileProduction tileProduction = new TileProduction(cargoProduced, rateOfProduction);
        typeProduces.add(tileProduction);
    }

    /**
     * Returns the index number of the cargo with the specified name.
     */
    private int stringToCargoID(String cargoName) throws SAXException {
        if (cargoNameToCargoTypeNumber.containsKey(cargoName)) {
            return cargoNameToCargoTypeNumber.get(cargoName);
        }
        throw new SAXException("Unknown cargo type: " + cargoName);
    }
}