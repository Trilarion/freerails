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

package freerails.client.renderer.map.detail;

import freerails.client.ClientConstants;
import freerails.client.ModelRootProperty;
import freerails.client.renderer.RendererRoot;
import freerails.model.cargo.*;
import freerails.model.world.*;
import freerails.util.ui.Painter;
import freerails.client.ModelRoot;
import freerails.model.*;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders box showing the cargo waiting at a station.
 */
public class StationBoxRenderer implements Painter {

    private final UnmodifiableWorld world;
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 200, 60);
    private final int wagonImageWidth;
    private final ModelRoot modelRoot;
    private final Image[] cargoImages;

    /**
     * @param world
     * @param rendererRoot
     * @param modelRoot
     */
    public StationBoxRenderer(UnmodifiableWorld world, RendererRoot rendererRoot, ModelRoot modelRoot) {
        this.world = world;
        this.modelRoot = modelRoot;

        // How wide will the wagon images be if we scale them so their height is WAGON_IMAGE_HEIGHT?
        Image wagonImage = rendererRoot.getWagonImages(0).getSideOnImage();
        wagonImageWidth = wagonImage.getWidth(null) * ClientConstants.WAGON_IMAGE_HEIGHT / wagonImage.getHeight(null);

        int nrOfCargoTypes = world.getCargos().size();
        cargoImages = new Image[nrOfCargoTypes];
        for (int i = 0; i < nrOfCargoTypes; i++) {
            String wagonFilename = rendererRoot.getWagonImages(i).sideOnFileName;
            try {
                wagonImage = rendererRoot.getScaledImage(wagonFilename, ClientConstants.WAGON_IMAGE_HEIGHT);
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
        Boolean showCargoWaiting = (Boolean) modelRoot.getProperty(ModelRootProperty.SHOW_CARGO_AT_STATIONS);

        if (showCargoWaiting) {
            // We only show the station boxes for the current player.
            FreerailsPrincipal principal = modelRoot.getPrincipal();
            WorldIterator worldIterator = new NonNullElementWorldIterator(PlayerKey.Stations, world, principal);

            // TODO can there be null stations?
            while (worldIterator.next()) { // loop over non null stations
                Station station = (Station) worldIterator.getElement();
                // TODO which position is meant here?
                int positionX = (station.getLocation().x * ModelConstants.TILE_SIZE) + ModelConstants.TILE_SIZE / 2;
                int positionY = (station.getLocation().y * ModelConstants.TILE_SIZE) + ModelConstants.TILE_SIZE * 2;
                Rectangle r = new Rectangle(positionX, positionY, ClientConstants.MAX_WIDTH, ClientConstants.MAX_HEIGHT);
                if (newVisibleRectangle.intersects(r)) {
                    g.setColor(StationBoxRenderer.BACKGROUND_COLOR);
                    g.fillRect(r.x, r.y, r.width, r.height);
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(1.0f));
                    g.drawRect(r.x, r.y, r.width, r.height);

                    CargoBatchBundle cargoBatchBundle = (ImmutableCargoBatchBundle) world.get(principal, PlayerKey.CargoBundles, station.getCargoBundleID());
                    Map<CargoCategory, List<Integer>> carsLoads = calculateCarLoads(cargoBatchBundle);
                    int i = 0;
                    for (CargoCategory cargoCategory: CargoCategory.values()) {
                        int alternateWidth = (ClientConstants.MAX_WIDTH - 2 * ClientConstants.SPACING) / (carsLoads.get(cargoCategory).size() + 1);
                        int xOffsetPerWagon = Math.min(wagonImageWidth, alternateWidth);

                        for (int car = 0; car < carsLoads.get(cargoCategory).size(); car++) {
                            int x = positionX + (car * xOffsetPerWagon) + ClientConstants.SPACING;
                            int y = positionY + (i * (ClientConstants.WAGON_IMAGE_HEIGHT + ClientConstants.SPACING));
                            int cargoType = carsLoads.get(cargoCategory).get(car);
                            g.drawImage(cargoImages[cargoType], x, y, null);
                        }
                        i++;
                    }
                }
            }
        }
    }

    // TODO move this to cargo batch bundle maybe? however, depends on the world
    /**
     * The length of the returned array is the number of complete carloads of
     * the specified cargo category in the specified bundle. The values in the
     * array are the type of the cargo. E.g. if the bundle contained 2 carloads
     * of cargo type 3 and 1 of type 7, {3, 3, 7} would be returned.
     */
    private Map<CargoCategory, List<Integer>> calculateCarLoads(CargoBatchBundle cargoBatchBundle) {
        // TODO overly complicated, easier way possible?
        int numCargoTypes = world.getCargos().size();
        Map<CargoCategory, Integer> numberOfCarLoads = new HashMap<>();
        for (CargoCategory cargoCategory: CargoCategory.values()) {
            numberOfCarLoads.put(cargoCategory, 0);
        }
        Map<CargoCategory, Map<Integer, Integer>> cars = new HashMap<>();
        for (CargoCategory cargoCategory: CargoCategory.values()) {
            Map<Integer, Integer> map = new HashMap<>();
            // TODO int i is not an ID, this will break if ids are not going from 0 to number of types - 1
            for (int i = 0; i < numCargoTypes; i++) {
                map.put(i, 0);
            }
            cars.put(cargoCategory, map);
        }
        for (int i = 0; i < numCargoTypes; i++) {
            Cargo ct = world.getCargoType(i);
            int carsOfThisCargo = cargoBatchBundle.getAmountOfType(i) / ModelConstants.UNITS_OF_CARGO_PER_WAGON;
            numberOfCarLoads.put(ct.getCategory(), numberOfCarLoads.get(ct.getCategory()) + carsOfThisCargo);
            cars.get(ct.getCategory()).put(i, cars.get(ct.getCategory()).get(i) + carsOfThisCargo);
        }

        Map<CargoCategory, List<Integer>> returnMatrix = new HashMap<>();
        for (CargoCategory cargoCategory: CargoCategory.values()) {
            List<Integer> returnValue = new ArrayList<>();

            for (int cargoType = 0; cargoType < numCargoTypes; cargoType++) {
                for (int j = 0; j < cars.get(cargoCategory).get(cargoType); j++) {
                    returnValue.add(cargoType);
                }
            }
            returnMatrix.put(cargoCategory, returnValue);
        }
        return returnMatrix;
    }
}