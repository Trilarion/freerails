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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of RendererRoot whose constructor loads graphics and provides
 * feed back using a ProgressMonitorModel.
 */
public class RendererRootImpl implements RendererRoot {

    private static final TaggedLogger logger = Logger.tag("RendererRootImpl");
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

    // TODO this tile renderer list must have a certain length and structure otherwise the scenario start will hang, fix this behavior and simplify
    /**
     * Loads and returns the new tile view list.
     * @param world the world
     * @return the result of this operation
     * @throws IOException if the operation cannot be completed
     */
    private TileRendererList loadNewTileViewList(UnmodifiableWorld world) throws IOException {
        Map<Integer, TileRenderer> tileRenderers = new HashMap<>();

        // Setup progress monitor..

        // for all terrain types
        for (Terrain terrainType: world.getTerrains()) {
            TileRenderer tileRenderer;

            // TODO not a nice hack, unhack, provide information about neighboring tiles differently or better inside the Renderer
            List<Integer> typesTreatedAsTheSame = new ArrayList<>();
            try {
                // TODO hack to make rivers flow into ocean and harbours & ocean
                // treat harbours as the same type.
                TerrainCategory thisTerrainCategory = terrainType.getCategory();

                if (thisTerrainCategory == TerrainCategory.RIVER || thisTerrainCategory == TerrainCategory.OCEAN) {

                    for (Terrain terrain : world.getTerrains()) {
                        TerrainCategory terrainCategory = terrain.getCategory();

                        if (terrainCategory == TerrainCategory.OCEAN || terrainCategory == thisTerrainCategory) {
                            typesTreatedAsTheSame.add(terrain.getId());
                        }
                    }
                }

                tileRenderer = new RiverStyleTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
                tileRenderers.put(terrainType.getId(), tileRenderer);

                continue;
            } catch (IOException ignored) {
            }

            try {
                tileRenderer = new ForestStyleTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
                tileRenderers.put(terrainType.getId(), tileRenderer);

                continue;
            } catch (IOException ignored) {
            }

            // TODO this was the chequered tile renderer which was removed because it was not needed
            try {
                tileRenderer = new StandardTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
                tileRenderers.put(terrainType.getId(), tileRenderer);

                continue;
            } catch (IOException ignored) {
            }

            try {
                tileRenderer = new StandardTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
                tileRenderers.put(terrainType.getId(), tileRenderer);
            } catch (IOException ignored) {
            }
        }

        // add special tile renderer for harbours
        TileRenderer oceanTileRenderer = null;

        for (Terrain terrainType: world.getTerrains()) {

            if (terrainType.getCategory().equals(TerrainCategory.OCEAN)) {
                oceanTileRenderer = tileRenderers.get(terrainType.getId());
            }
        }

        for (Terrain terrainType: world.getTerrains()) {
            if (terrainType.getName().equals("Harbour")) {
                TileRenderer tr = new SpecialTileRenderer(imageManager, new ArrayList<>(), terrainType , oceanTileRenderer);
                tileRenderers.put(terrainType.getId(), tr);
            }
        }

        return new StandardTileRendererList(tileRenderers);
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
     * Returns the tile renderer by index.
     *
     * @param index the index of the item
     * @return the tile renderer by index produced by this method
     */
    @Override
    public TileRenderer getTileRendererByIndex(int index) {
        return tileRendererList.getTileRendererByIndex(index);
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