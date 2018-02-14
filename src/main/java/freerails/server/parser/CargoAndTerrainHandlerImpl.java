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

package freerails.server.parser;

import freerails.world.SKEY;
import freerails.world.world.World;
import freerails.world.cargo.CargoCategory;
import freerails.world.cargo.CargoType;
import freerails.world.terrain.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.Serializable;
import java.util.*;

/**
 * Processes CargoAndTerrainHandler events and adds terrain and cargo types to
 * the world object.
 *
 * @see CargoAndTerrainHandler
 * @see CargoAndTerrainParser
 */
// TODO difference between interface and implementation?
public class CargoAndTerrainHandlerImpl implements CargoAndTerrainHandler {

    private final HashMap<String, Integer> cargoNameTocargoTypeNumber = new HashMap<>();
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
    public CargoAndTerrainHandlerImpl(World world) {
        this.world = world;
    }

    private static int string2RGBValue(String temp_number) {
        int rgb = Integer.parseInt(temp_number, 16);

        /*
         * We need to change the format of the rgb value to the same one as used
         * by the the BufferedImage that stores the map. See
         * freerails.common.Map
         */
        rgb = new java.awt.Color(rgb).getRGB();

        return rgb;
    }

    public void handle_Converts(final Attributes meta) throws SAXException {
        String inputCargo = meta.getValue("input");
        String outputCargo = meta.getValue("output");

        int input = string2CargoID(inputCargo);
        int output = string2CargoID(outputCargo);
        TileConversion tileConversion = new TileConversion(input, output);
        typeConverts.add(tileConversion);
    }

    public void start_Tile(final Attributes meta) throws SAXException {
        typeConsumes.clear();
        typeProduces.clear();
        typeConverts.clear();

        tileID = meta.getValue("id");
        tileCategory = TerrainCategory.valueOf(meta.getValue("Category"));

        String rgbString = meta.getValue("rgb");
        tileRGB = string2RGBValue(rgbString);

        String buildCostString = meta.getValue("build_cost");

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

        tileROW = Integer.parseInt(meta.getValue("right-of-way"));
    }

    public void end_Tile() {
        TileConsumption[] consumes = new TileConsumption[typeConsumes.size()];

        for (int i = 0; i < typeConsumes.size(); i++) {
            consumes[i] = typeConsumes.get(i);
        }

        TileProduction[] produces = new TileProduction[typeProduces.size()];

        for (int i = 0; i < typeProduces.size(); i++) {
            produces[i] = typeProduces.get(i);
        }

        TileConversion[] converts = new TileConversion[typeConverts.size()];

        for (int i = 0; i < typeConverts.size(); i++) {
            converts[i] = typeConverts.get(i);
        }

        Serializable tileType = new TerrainTypeImpl(tileRGB, tileCategory, tileID, tileROW, produces, consumes, converts, tileBuildCost);

        world.add(SKEY.TERRAIN_TYPES, tileType);
    }

    public void handle_Cargo(final Attributes meta) {
        String cargoID = meta.getValue("id");
        String cargoCategory = meta.getValue("Category");
        int unitWeight = Integer.parseInt(meta.getValue("unitWeight"));
        Serializable cargoType = new CargoType(unitWeight, cargoID, CargoCategory.valueOf(cargoCategory));

        int cargoNumber = world.size(SKEY.CARGO_TYPES);
        cargoNameTocargoTypeNumber.put(cargoID, cargoNumber);
        world.add(SKEY.CARGO_TYPES, cargoType);
    }

    public void handle_Consumes(final Attributes meta) throws SAXException {
        int cargoConsumed = string2CargoID(meta.getValue("Cargo"));
        String prerequisiteString = meta.getValue("Prerequisite");

        // "Prerequisite" is an optional attribute, so may be null.
        int prerequisiteForConsumption = (null == prerequisiteString ? 1 : Integer.parseInt(prerequisiteString));
        TileConsumption tileConsumption = new TileConsumption(cargoConsumed, prerequisiteForConsumption);
        typeConsumes.add(tileConsumption);
    }

    public void handle_Produces(final Attributes meta) throws SAXException {
        int cargoProduced = string2CargoID(meta.getValue("Cargo"));
        int rateOfProduction = Integer.parseInt(meta.getValue("Rate"));
        TileProduction tileProduction = new TileProduction(cargoProduced, rateOfProduction);
        typeProduces.add(tileProduction);
    }

    /**
     * Returns the index number of the cargo with the specified name.
     */
    private int string2CargoID(String cargoName) throws SAXException {
        if (cargoNameTocargoTypeNumber.containsKey(cargoName)) {
            return cargoNameTocargoTypeNumber.get(cargoName);
        }
        throw new SAXException("Unknown cargo type: " + cargoName);
    }
}