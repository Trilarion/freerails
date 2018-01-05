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
import freerails.world.World;
import freerails.world.cargo.CargoCategory;
import freerails.world.cargo.CargoType;
import freerails.world.terrain.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Processes CargoAndTerrainHandler events and adds terrain and cargo types to
 * the world object.
 *
 * @see CargoAndTerrainHandler
 * @see CargoAndTerrainParser
 */
// TODO difference between interface and implementation?
public class CargoAndTerrainHandlerImpl implements CargoAndTerrainHandler {

    final HashMap<String, Integer> cargoNameTocargoTypeNumber = new HashMap<>();
    final HashSet<Integer> rgbValuesAlreadyUsed = new HashSet<>();
    final ArrayList<Consumption> typeConsumes = new ArrayList<>();
    final ArrayList<Production> typeProduces = new ArrayList<>();
    final ArrayList<Conversion> typeConverts = new ArrayList<>();
    private final World world;

    // Parsing variables for Tile
    String tileID;
    TerrainCategory tileCategory;
    int tileRGB;
    int tileROW;
    int tileBuildCost;

    /**
     * @param world
     */
    public CargoAndTerrainHandlerImpl(World world) {
        this.world = world;
    }

    public void handle_Converts(final Attributes meta) throws SAXException {
        String inputCargo = meta.getValue("input");
        String outputCargo = meta.getValue("output");

        int input = string2CargoID(inputCargo);
        int output = string2CargoID(outputCargo);
        Conversion conversion = new Conversion(input, output);
        typeConverts.add(conversion);
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
            throw new SAXException(tileID + " can't using rgb value "
                    + rgbString
                    + " because it is being used by another tile type!");
        }
        rgbValuesAlreadyUsed.add(rgbInteger);

        tileROW = Integer.parseInt(meta.getValue("right-of-way"));
    }

    public void end_Tile() {
        Consumption[] consumes = new Consumption[typeConsumes.size()];

        for (int i = 0; i < typeConsumes.size(); i++) {
            consumes[i] = typeConsumes.get(i);
        }

        Production[] produces = new Production[typeProduces.size()];

        for (int i = 0; i < typeProduces.size(); i++) {
            produces[i] = typeProduces.get(i);
        }

        Conversion[] converts = new Conversion[typeConverts.size()];

        for (int i = 0; i < typeConverts.size(); i++) {
            converts[i] = typeConverts.get(i);
        }

        TileTypeImpl tileType = new TileTypeImpl(tileRGB, tileCategory, tileID,
                tileROW, produces, consumes, converts, tileBuildCost);

        world.add(SKEY.TERRAIN_TYPES, tileType);
    }

    public void handle_Cargo(final Attributes meta) {
        String cargoID = meta.getValue("id");
        String cargoCategory = meta.getValue("Category");
        int unitWeight = Integer.parseInt(meta.getValue("unitWeight"));
        CargoType cargoType = new CargoType(unitWeight, cargoID, CargoCategory.valueOf(cargoCategory));

        int cargoNumber = world.size(SKEY.CARGO_TYPES);
        cargoNameTocargoTypeNumber.put(cargoID, cargoNumber);
        world.add(SKEY.CARGO_TYPES, cargoType);
    }

    public void handle_Consumes(final Attributes meta) throws SAXException {
        int cargoConsumed = string2CargoID(meta.getValue("Cargo"));
        String prerequisiteString = meta.getValue("Prerequisite");

        // "Prerequisite" is an optional attribute, so may be null.
        int prerequisiteForConsumption = (null == prerequisiteString ? 1
                : Integer.parseInt(prerequisiteString));
        Consumption consumption = new Consumption(cargoConsumed,
                prerequisiteForConsumption);
        typeConsumes.add(consumption);
    }

    public void handle_Produces(final Attributes meta) throws SAXException {
        int cargoProduced = string2CargoID(meta.getValue("Cargo"));
        int rateOfProduction = Integer.parseInt(meta.getValue("Rate"));
        Production production = new Production(cargoProduced, rateOfProduction);
        typeProduces.add(production);
    }

    private int string2RGBValue(String temp_number) {
        int rgb = Integer.parseInt(temp_number, 16);

        /*
         * We need to change the format of the rgb value to the same one as used
         * by the the BufferedImage that stores the map. See
         * freerails.common.Map
         */
        rgb = new java.awt.Color(rgb).getRGB();

        return rgb;
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