/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import freerails.model.operations.cargo.Cargo;
import freerails.model.terrain.Terrain;
import freerails.model.operations.train.Engine;
import freerails.util.ui.SoundManager;
import freerails.util.ui.ImageManager;
import freerails.util.ui.ImageManagerImpl;
import freerails.client.renderer.tile.*;
import freerails.client.renderer.track.TrackPieceRenderer;
import freerails.client.renderer.track.TrackPieceRendererList;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.terrain.TerrainCategory;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of RendererRoot whose constructor loads graphics and provides
 * feed back using a ProgressMonitorModel.
 */
public class RendererRootImpl implements RendererRoot {

    private static final TaggedLogger logger = Logger.tag("RendererRootImpl");
    private static final String HARBOUR_TERRAIN_NAME = "Harbour";
    private final TileRendererList tileRendererList;
    private final TrackPieceRendererList trackPieceViewList;
    private final ImageManager imageManager;
    private final Map<Integer, TrainImages> wagonImages = new HashMap<>();
    private final Map<Integer, TrainImages> engineImages = new HashMap<>();

    /**
     * Creates a renderer root impl instance.
     *
     * @param world the game world being queried
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    public RendererRootImpl(UnmodifiableWorld world) throws IOException {
        imageManager = new ImageManagerImpl(ClientConstants.GRAPHICS_PATH);
        tileRendererList = loadNewTileViewList(world);

        trackPieceViewList = loadTrackViews(world);

        // rr = new OldTrainImages(world, imageManager, pm);
        loadTrainImages(world);

        // Pre-load sounds..
        String[] soundsFiles = {ClientConstants.SOUND_BUILD_TRACK, ClientConstants.SOUND_CASH, ClientConstants.SOUND_REMOVE_TRACK, ClientConstants.SOUND_WHISTLE};
        SoundManager sm = SoundManager.getInstance();
        for (String soundsFile : soundsFiles) {
            try {
                sm.addClip(soundsFile);
            } catch (IOException | UnsupportedAudioFileException e) {
                logger.warn(e, "Could not load sound " + soundsFile);
            }
        }
    }

    /**
     * Loads the train images.
     * @param world the world
     * @throws IOException if the operation cannot be completed
     */
    private void loadTrainImages(UnmodifiableWorld world) throws IOException {
        // Load wagon images.
        for (Cargo cargo : world.getCargos()) {
            String name = cargo.getName();
            TrainImages ti = new TrainImages(imageManager, name);
            wagonImages.put(cargo.getId(), ti);
        }

        // Load engine images
        for (Engine engine: world.getEngines()) {
            String name = engine.getName();
            TrainImages trainImages = new TrainImages(imageManager, name);
            engineImages.put(engine.getId(), trainImages);
        }
    }

    /**
     * Loads and returns the track views.
     * @param world the world
     * @return the result of this operation
     * @throws IOException if the operation cannot be completed
     */
    private TrackPieceRendererList loadTrackViews(UnmodifiableWorld world) throws IOException {
        return new TrackPieceRendererList(world, imageManager);
    }

    /**
     * Loads one tile renderer for every terrain type of the world. Harbours are
     * drawn on top of the ocean renderer.
     *
     * @param world the world
     * @return the tile renderers keyed by terrain ID
     * @throws IOException if the graphics for a terrain type cannot be loaded
     */
    private TileRendererList loadNewTileViewList(UnmodifiableWorld world) throws IOException {
        Map<Integer, TileRenderer> tileRenderers = new HashMap<>();

        for (Terrain terrainType: world.getTerrains()) {
            if (!isHarbour(terrainType)) {
                tileRenderers.put(terrainType.getId(), loadTileRenderer(world, terrainType));
            }
        }

        TileRenderer oceanTileRenderer = world.getTerrains().stream()
                .filter(terrainType -> terrainType.getCategory() == TerrainCategory.OCEAN && !isHarbour(terrainType))
                .map(terrainType -> tileRenderers.get(terrainType.getId()))
                .findFirst()
                .orElse(null);

        for (Terrain terrainType: world.getTerrains()) {
            if (isHarbour(terrainType)) {
                TileRenderer tr = new SpecialTileRenderer(imageManager, Set.of(terrainType.getId()), terrainType, oceanTileRenderer);
                tileRenderers.put(terrainType.getId(), tr);
            }
        }

        return new TileRendererList(tileRenderers);
    }

    /**
     * Returns whether the terrain type is drawn as a harbour on top of the ocean.
     *
     * @param terrainType the terrain type
     * @return whether the terrain type is the harbour
     */
    private static boolean isHarbour(Terrain terrainType) {
        return HARBOUR_TERRAIN_NAME.equals(terrainType.getName());
    }

    /**
     * Returns the IDs of the terrain types that a tile of the specified terrain
     * type visually connects to when choosing its image from its neighbours.
     * <p>
     * Every terrain type connects to itself. Water connects as well: river
     * and ocean tiles also connect to all ocean terrain types (including the
     * harbour), and river tiles to all river terrain types, so that rivers flow
     * into the sea and the coast does not appear next to a harbour.
     *
     * @param world the world
     * @param terrainType the terrain type
     * @return the IDs of the connecting terrain types
     */
    private static Set<Integer> connectingTerrainIds(UnmodifiableWorld world, Terrain terrainType) {
        Set<Integer> ids = new HashSet<>();
        ids.add(terrainType.getId());

        TerrainCategory category = terrainType.getCategory();
        if (category == TerrainCategory.RIVER || category == TerrainCategory.OCEAN) {
            for (Terrain terrain : world.getTerrains()) {
                TerrainCategory otherCategory = terrain.getCategory();
                if (otherCategory == TerrainCategory.OCEAN || otherCategory == category) {
                    ids.add(terrain.getId());
                }
            }
        }
        return ids;
    }

    /**
     * Loads the tile renderer for one terrain type, choosing the first style
     * whose graphics exist: river style, forest style, then a single image.
     *
     * @param world the world
     * @param terrainType the terrain type
     * @return the tile renderer
     * @throws IOException if no style's graphics exist for the terrain type
     */
    private TileRenderer loadTileRenderer(UnmodifiableWorld world, Terrain terrainType) throws IOException {
        Set<Integer> typesTreatedAsTheSame = connectingTerrainIds(world, terrainType);

        try {
            return new RiverStyleTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
        } catch (IOException ignored) {
        }
        try {
            return new ForestStyleTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
        } catch (IOException ignored) {
        }
        try {
            return new StandardTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
        } catch (IOException e) {
            throw new IOException("No tile graphics found for terrain type " + terrainType.getName()
                    + " (ID " + terrainType.getId() + ")", e);
        }
    }

    /**
     * Returns whether validate.
     * @param world the world
     * @return whether validate
     */
    @Override
    public boolean validate(UnmodifiableWorld world) {
        return tileRendererList.validate(world) && trackPieceViewList.validate(world);
    }

    /**
     * Returns the image.
     *
     * @param relativeFilename the relative filename parameter
     * @return the image produced by this method
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    @Override
    public Image getImage(String relativeFilename) throws IOException {
        return imageManager.getImage(relativeFilename);
    }

    /**
     * Returns the tile renderers for the terrain types.
     *
     * @return the tile renderer list
     */
    @Override
    public TileRendererList getTileRendererList() {
        return tileRendererList;
    }

    /**
     * Returns the track piece view.
     *
     * @param i the index of the item
     * @return the track piece view produced by this method
     */
    @Override
    public TrackPieceRenderer getTrackPieceView(int i) {
        return trackPieceViewList.getTrackPieceView(i);
    }

    /**
     * Returns the wagon images.
     *
     * @param cargoTypeId the cargo type id parameter
     * @return the wagon images produced by this method
     */
    @Override
    public TrainImages getWagonImages(int cargoTypeId) {
        TrainImages trainImages = wagonImages.get(cargoTypeId);
        if (trainImages == null) {
            throw new IllegalArgumentException("Unknown cargo type ID: " + cargoTypeId);
        }
        return trainImages;
    }

    /**
     * Returns the engine images.
     *
     * @param type the type parameter
     * @return the engine images produced by this method
     */
    @Override
    public TrainImages getEngineImages(int type) {
        return engineImages.get(type);
    }

    /**
     * Returns the scaled image.
     *
     * @param relativeFilename the relative filename parameter
     * @param height the height parameter
     * @return the scaled image produced by this method
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    @Override
    public Image getScaledImage(String relativeFilename, int height) throws IOException {
        return imageManager.getScaledImage(relativeFilename, height);
    }
}