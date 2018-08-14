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
import freerails.model.player.Player;
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
            Player player = modelRoot.getPlayer();
            for (Station station: world.getStations(player)) {
                // position of station box (below station)
                int positionX = (station.getLocation().x * ModelConstants.TILE_SIZE) + ModelConstants.TILE_SIZE / 2;
                int positionY = (station.getLocation().y * ModelConstants.TILE_SIZE) + ModelConstants.TILE_SIZE * 2;
                Rectangle r = new Rectangle(positionX, positionY, ClientConstants.MAX_WIDTH, ClientConstants.MAX_HEIGHT);
                if (newVisibleRectangle.intersects(r)) {
                    g.setColor(StationBoxRenderer.BACKGROUND_COLOR);
                    g.fillRect(r.x, r.y, r.width, r.height);
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(1.0f));
                    g.drawRect(r.x, r.y, r.width, r.height);

                    UnmodifiableCargoBatchBundle cargoBatchBundle = station.getCargoBatchBundle();
                    Map<CargoCategory, List<Integer>> carsLoads = CargoUtils.calculateCarLoads(world, cargoBatchBundle);
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
}