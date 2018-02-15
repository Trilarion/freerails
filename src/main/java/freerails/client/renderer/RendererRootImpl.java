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

import freerails.client.ClientConfig;
import freerails.client.SoundManager;
import freerails.util.ui.ImageManager;
import freerails.util.ui.ImageManagerImpl;
import freerails.client.renderer.tile.*;
import freerails.client.renderer.track.TrackPieceRenderer;
import freerails.client.renderer.track.TrackPieceRendererList;
import freerails.util.ui.ProgressMonitorModel;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.WorldSharedKey;
import freerails.model.cargo.CargoType;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TerrainType;
import freerails.model.train.EngineType;
import org.apache.log4j.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of RendererRoot whose constructor loads graphics and provides
 * feed back using a ProgressMonitorModel.
 */
public class RendererRootImpl implements RendererRoot {

    private static final Logger logger = Logger.getLogger(RendererRootImpl.class.getName());
    private final TileRendererList tiles;
    private final TrackPieceRendererList trackPieceViewList;
    private final ImageManager imageManager;
    private final List<TrainImages> wagonImages = new ArrayList<>();
    private final List<TrainImages> engineImages = new ArrayList<>();

    /**
     * @param world
     * @param progressMonitorModel
     * @throws IOException
     */
    public RendererRootImpl(ReadOnlyWorld world, ProgressMonitorModel progressMonitorModel) throws IOException {
        imageManager = new ImageManagerImpl(ClientConfig.GRAPHICS_PATH);
        tiles = loadNewTileViewList(world, progressMonitorModel);

        trackPieceViewList = loadTrackViews(world, progressMonitorModel);

        // rr = new OldTrainImages(world, imageManager, pm);
        loadTrainImages(world, progressMonitorModel);
        preloadSounds(progressMonitorModel);
    }

    private static void preloadSounds(ProgressMonitorModel pm) {
        // Pre-load sounds..
        String[] soundsFiles = {ClientConfig.SOUND_BUILD_TRACK, ClientConfig.SOUND_CASH, ClientConfig.SOUND_REMOVE_TRACK, ClientConfig.SOUND_WHISTLE};
        pm.nextStep(soundsFiles.length);
        SoundManager sm = SoundManager.getSoundManager();
        for (int i = 0; i < soundsFiles.length; i++) {
            try {
                sm.addClip(soundsFiles[i]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
            pm.setValue(i + 1);
        }
    }

    private void loadTrainImages(ReadOnlyWorld world, ProgressMonitorModel progressMonitorModel) throws IOException {
        // Setup progress monitor..
        final int numberOfWagonTypes = world.size(WorldSharedKey.CargoTypes);
        final int numberOfEngineTypes = world.size(WorldSharedKey.EngineTypes);
        progressMonitorModel.nextStep(numberOfWagonTypes + numberOfEngineTypes);
        int progress = 0;
        progressMonitorModel.setValue(progress);

        // Load wagon images.
        for (int i = 0; i < numberOfWagonTypes; i++) {
            CargoType cargoType = (CargoType) world.get(WorldSharedKey.CargoTypes, i);
            String name = cargoType.getName();
            TrainImages ti = new TrainImages(imageManager, name);
            wagonImages.add(ti);
            progressMonitorModel.setValue(++progress);
        }

        // Load engine images
        for (int i = 0; i < numberOfEngineTypes; i++) {
            EngineType engineType = (EngineType) world.get(WorldSharedKey.EngineTypes, i);
            String engineTypeName = engineType.getEngineTypeName();
            TrainImages ti = new TrainImages(imageManager, engineTypeName);
            engineImages.add(ti);
            progressMonitorModel.setValue(++progress);
        }
    }

    private TrackPieceRendererList loadTrackViews(ReadOnlyWorld world, ProgressMonitorModel progressMonitorModel) throws IOException {
        return new TrackPieceRendererList(world, imageManager, progressMonitorModel);
    }

    private TileRendererList loadNewTileViewList(ReadOnlyWorld world, ProgressMonitorModel progressMonitorModel) throws IOException {
        ArrayList<TileRenderer> tileRenderers = new ArrayList<>();

        // Setup progress monitor..

        int numberOfTypes = world.size(WorldSharedKey.TerrainTypes);
        progressMonitorModel.nextStep(numberOfTypes);

        int progress = 0;
        progressMonitorModel.setValue(progress);

        for (int i = 0; i < numberOfTypes; i++) {
            TerrainType t = (TerrainType) world.get(WorldSharedKey.TerrainTypes, i);
            int[] typesTreatedAsTheSame = new int[]{i};

            TileRenderer tileRenderer;
            progressMonitorModel.setValue(++progress);

            // TODO not a nice hack, unhack
            try {
                // XXX hack to make rivers flow into ocean and harbours & ocean
                // treat harbours as the same type.
                TerrainCategory thisTerrainCategory = t.getCategory();

                if (thisTerrainCategory == TerrainCategory.River || thisTerrainCategory == TerrainCategory.Ocean) {
                    // Count number of types with category "water"
                    int count = 0;

                    for (int j = 0; j < numberOfTypes; j++) {
                        TerrainType t2 = (TerrainType) world.get(WorldSharedKey.TerrainTypes, j);
                        TerrainCategory terrainCategory = t2.getCategory();

                        if (terrainCategory == TerrainCategory.Ocean || terrainCategory == thisTerrainCategory) {
                            count++;
                        }
                    }

                    typesTreatedAsTheSame = new int[count];
                    count = 0;

                    for (int j = 0; j < numberOfTypes; j++) {
                        TerrainType t2 = (TerrainType) world.get(WorldSharedKey.TerrainTypes, j);
                        TerrainCategory terrainCategory = t2.getCategory();

                        if (terrainCategory == TerrainCategory.Ocean || terrainCategory == thisTerrainCategory) {
                            typesTreatedAsTheSame[count] = j;
                            count++;
                        }
                    }
                }

                tileRenderer = new RiverStyleTileRenderer(imageManager, typesTreatedAsTheSame, t, world);
                tileRenderers.add(tileRenderer);

                continue;
            } catch (IOException ignored) {
            }

            try {
                tileRenderer = new ForestStyleTileRenderer(imageManager, typesTreatedAsTheSame, t, world);
                tileRenderers.add(tileRenderer);

                continue;
            } catch (IOException ignored) {
            }

            try {
                tileRenderer = new ChequeredTileRenderer(imageManager, typesTreatedAsTheSame, t, world);
                tileRenderers.add(tileRenderer);

                continue;
            } catch (IOException ignored) {
            }

            try {
                tileRenderer = new StandardTileRenderer(imageManager, typesTreatedAsTheSame, t, world);
                tileRenderers.add(tileRenderer);
            } catch (IOException io) {
                // If the image is missing, we generate it.
                logger.warn("No tile renderer for " + t.getTerrainTypeName());

                String filename = StandardTileRenderer.generateFilename(t.getTerrainTypeName());
                Image image = QuickRGBTileRendererList.createImageFor(t);
                imageManager.setImage(filename, image);

                // generatedImages.setImage(filename, image);
                try {
                    tileRenderer = new StandardTileRenderer(imageManager, typesTreatedAsTheSame, t, world);
                    tileRenderers.add(tileRenderer);
                } catch (IOException io2) {
                    io2.printStackTrace();
                    throw new IllegalStateException();
                }
            }
        }

        // add special tile renderer for harbours
        TileRenderer occeanTileRenderer = null;

        for (int j = 0; j < numberOfTypes; j++) {
            TerrainType t2 = (TerrainType) world.get(WorldSharedKey.TerrainTypes, j);
            String terrainName = t2.getTerrainTypeName();

            if (terrainName.equalsIgnoreCase("Ocean")) {
                occeanTileRenderer = tileRenderers.get(j);

                break;
            }
        }

        for (int j = 0; j < numberOfTypes; j++) {
            TerrainType t2 = (TerrainType) world.get(WorldSharedKey.TerrainTypes, j);
            String terrainName = t2.getTerrainTypeName();

            if (terrainName.equalsIgnoreCase("Harbour")) {
                TerrainType t = (TerrainType) world.get(WorldSharedKey.TerrainTypes, j);
                TileRenderer tr = new SpecialTileRenderer(imageManager, new int[]{j}, t, occeanTileRenderer, world);
                tileRenderers.set(j, tr);
                break;
            }
        }

        return new TileRendererListImpl(tileRenderers);
    }

    public boolean validate(ReadOnlyWorld world) {
        boolean okSoFar = true;

        if (!tiles.validate(world)) {
            okSoFar = false;
        }

        if (!trackPieceViewList.validate(world)) {
            okSoFar = false;
        }

        return okSoFar;
    }

    /**
     * @param relativeFilename
     * @return
     * @throws IOException
     */
    public Image getImage(String relativeFilename) throws IOException {
        return imageManager.getImage(relativeFilename);
    }

    /**
     * @param i
     * @return
     */
    public TileRenderer getTileViewWithNumber(int i) {
        return tiles.getTileViewWithNumber(i);
    }

    /**
     * @param i
     * @return
     */
    public TrackPieceRenderer getTrackPieceView(int i) {
        return trackPieceViewList.getTrackPieceView(i);
    }

    /**
     * @param type
     * @return
     */
    public TrainImages getWagonImages(int type) {
        return wagonImages.get(type);
    }

    /**
     * @param type
     * @return
     */
    public TrainImages getEngineImages(int type) {
        return engineImages.get(type);
    }

    /**
     * @param relativeFilename
     * @param height
     * @return
     * @throws IOException
     */
    public Image getScaledImage(String relativeFilename, int height) throws IOException {
        return imageManager.getScaledImage(relativeFilename, height);
    }
}