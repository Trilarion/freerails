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

package freerails.client.renderer;

import freerails.client.ClientConstants;
import freerails.client.common.Painter;
import freerails.controller.ModelRoot;
import freerails.world.*;
import freerails.world.cargo.CargoBatchBundle;
import freerails.world.cargo.CargoCategory;
import freerails.world.cargo.CargoType;
import freerails.world.cargo.ImmutableCargoBatchBundle;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.Station;
import freerails.world.train.WagonType;

import java.awt.*;
import java.io.IOException;

/**
 * Renders box showing the cargo waiting at a station.
 */
public class StationBoxRenderer implements Painter {

    private static final int WAGON_IMAGE_HEIGHT = 10;

    private static final int SPACING = 3;

    private static final int MAX_WIDTH = 80;
    private static final int MAX_HEIGHT = 5 * (WAGON_IMAGE_HEIGHT + SPACING);
    private final ReadOnlyWorld w;
    private final Color bgColor;
    private final int wagonImageWidth;
    private final ModelRoot modelRoot;
    private final Image[] cargoImages;

    /**
     * @param world
     * @param vl
     * @param modelRoot
     */
    public StationBoxRenderer(ReadOnlyWorld world, RendererRoot vl, ModelRoot modelRoot) {
        w = world;
        bgColor = new Color(0, 0, 200, 60);
        this.modelRoot = modelRoot;

        // How wide will the wagon images be if we scale them so their height is
        // WAGON_IMAGE_HEIGHT?
        Image wagonImage = vl.getWagonImages(0).getSideOnImage();
        wagonImageWidth = wagonImage.getWidth(null) * WAGON_IMAGE_HEIGHT / wagonImage.getHeight(null);

        int nrOfCargoTypes = w.size(SKEY.CARGO_TYPES);
        cargoImages = new Image[nrOfCargoTypes];
        for (int i = 0; i < nrOfCargoTypes; i++) {
            String wagonFilename = vl.getWagonImages(i).sideOnFileName;
            try {
                wagonImage = vl.getScaledImage(wagonFilename, WAGON_IMAGE_HEIGHT);
            } catch (IOException e) {
                throw new IllegalArgumentException(wagonFilename);
            }
            cargoImages[i] = wagonImage;
        }
    }

    /**
     * @param g
     * @param newVisibleRectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {
        Boolean showCargoWaiting = (Boolean) modelRoot.getProperty(ModelRoot.Property.SHOW_CARGO_AT_STATIONS);

        if (showCargoWaiting) {
            /* We only show the station boxes for the current player. */
            FreerailsPrincipal principal = modelRoot.getPrincipal();
            WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, w, principal);

            while (wi.next()) { // loop over non null stations
                Station station = (Station) wi.getElement();
                int positionX = (station.getStationX() * ClientConstants.TILE_SIZE) + ClientConstants.TILE_SIZE / 2;
                int positionY = (station.getStationY() * ClientConstants.TILE_SIZE) + ClientConstants.TILE_SIZE * 2;
                Rectangle r = new Rectangle(positionX, positionY, MAX_WIDTH, MAX_HEIGHT);
                if (newVisibleRectangle.intersects(r)) {
                    g.setColor(bgColor);
                    g.fillRect(positionX, positionY, MAX_WIDTH, MAX_HEIGHT);
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(1.0f));
                    g.drawRect(positionX, positionY, MAX_WIDTH, MAX_HEIGHT);

                    CargoBatchBundle cb = (ImmutableCargoBatchBundle) w.get(principal, KEY.CARGO_BUNDLES, station.getCargoBundleID());
                    int[][] carsLoads = calculateCarLoads(cb);
                    for (int category = 0; category < CargoCategory.getNumberOfCategories(); category++) {
                        int alternateWidth = (MAX_WIDTH - 2 * SPACING) / (carsLoads[category].length + 1);
                        int xOffsetPerWagon = Math.min(wagonImageWidth, alternateWidth);

                        for (int car = 0; car < carsLoads[category].length; car++) {
                            int x = positionX + (car * xOffsetPerWagon) + SPACING;
                            int y = positionY + (category * (WAGON_IMAGE_HEIGHT + SPACING));
                            int cargoType = carsLoads[category][car];
                            g.drawImage(cargoImages[cargoType], x, y, null);
                        }
                    }
                }
            }
        }
    }

    /**
     * The length of the returned array is the number of complete carloads of
     * the specified cargo category in the specified bundle. The values in the
     * array are the type of the cargo. E.g. if the bundle contained 2 carloads
     * of cargo type 3 and 1 of type 7, {3, 3, 7} would be returned.
     */
    private int[][] calculateCarLoads(CargoBatchBundle cb) {
        int categories = CargoCategory.getNumberOfCategories();
        int numCargoTypes = w.size(SKEY.CARGO_TYPES);
        int[] numberOfCarLoads = new int[categories];
        int[][] cars = new int[categories][numCargoTypes];
        for (int i = 0; i < numCargoTypes; i++) {
            CargoType ct = (CargoType) w.get(SKEY.CARGO_TYPES, i);
            int carsOfThisCargo = cb.getAmountOfType(i) / WagonType.UNITS_OF_CARGO_PER_WAGON;
            numberOfCarLoads[ct.getCategory().getID()] += carsOfThisCargo;
            cars[ct.getCategory().getID()][i] += carsOfThisCargo;
        }

        int[][] returnMatrix = new int[categories][];
        for (int category = 0; category < categories; category++) {
            int[] returnValue = new int[numberOfCarLoads[category]];
            int arrayIndex = 0;

            for (int cargoType = 0; cargoType < numCargoTypes; cargoType++) {
                for (int j = 0; j < cars[category][cargoType]; j++) {
                    returnValue[arrayIndex] = cargoType;
                    arrayIndex++;
                }
            }
            returnMatrix[category] = returnValue;
        }
        return returnMatrix;
    }
}