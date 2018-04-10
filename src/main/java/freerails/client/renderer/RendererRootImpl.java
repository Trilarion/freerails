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
import freerails.util.ui.SoundManager;
import freerails.util.ui.ImageManager;
import freerails.util.ui.ImageManagerImpl;
import freerails.client.renderer.tile.*;
import freerails.client.renderer.track.TrackPieceRenderer;
import freerails.client.renderer.track.TrackPieceRendererList;
import freerails.util.ui.ProgressMonitorModel;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
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
    private final TileRendererList tileRendererList;
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
        tileRendererList = loadNewTileViewList(world, progressMonitorModel);

        trackPieceViewList = loadTrackViews(world, progressMonitorModel);

        // rr = new OldTrainImages(world, imageManager, pm);
        loadTrainImages(world, progressMonitorModel);

        // Pre-load sounds..
        String[] soundsFiles = {ClientConfig.SOUND_BUILD_TRACK, ClientConfig.SOUND_CASH, ClientConfig.SOUND_REMOVE_TRACK, ClientConfig.SOUND_WHISTLE};
        progressMonitorModel.nextStep(soundsFiles.length);
        SoundManager sm = SoundManager.getInstance();
        for (int i = 0; i < soundsFiles.length; i++) {
            try {
                sm.addClip(soundsFiles[i]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
            progressMonitorModel.setValue(i + 1);
        }
    }

    private void loadTrainImages(ReadOnlyWorld world, ProgressMonitorModel progressMonitorModel) throws IOException {
        // Setup progress monitor..
        final int numberOfWagonTypes = world.size(SharedKey.CargoTypes);
        final int numberOfEngineTypes = world.size(SharedKey.EngineTypes);
        progressMonitorModel.nextStep(numberOfWagonTypes + numberOfEngineTypes);
        int progress = 0;
        progressMonitorModel.setValue(progress);

        // Load wagon images.
        for (int i = 0; i < numberOfWagonTypes; i++) {
            CargoType cargoType = (CargoType) world.get(SharedKey.CargoTypes, i);
            String name = cargoType.getName();
            TrainImages ti = new TrainImages(imageManager, name);
            wagonImages.add(ti);
            progressMonitorModel.setValue(++progress);
        }

        // Load engine images
        for (int i = 0; i < numberOfEngineTypes; i++) {
            EngineType engineType = (EngineType) world.get(SharedKey.EngineTypes, i);
            String engineTypeName = engineType.getEngineTypeName();
            TrainImages ti = new TrainImages(imageManager, engineTypeName);
            engineImages.add(ti);
            progressMonitorModel.setValue(++progress);
        }
    }

    private TrackPieceRendererList loadTrackViews(ReadOnlyWorld world, ProgressMonitorModel progressMonitorModel) throws IOException {
        return new TrackPieceRendererList(world, imageManager, progressMonitorModel);
    }

    // TODO this tile renderer list must have a certain length and structure otherwise the scenario start will hang, fix this behavior and simplify
    private TileRendererList loadNewTileViewList(ReadOnlyWorld world, ProgressMonitorModel progressMonitorModel) throws IOException {
        ArrayList<TileRenderer> tileRenderers = new ArrayList<>();

        // Setup progress monitor..

        int numberOfTypes = world.size(SharedKey.TerrainTypes);
        progressMonitorModel.nextStep(numberOfTypes);

        int progress = 0;
        progressMonitorModel.setValue(progress);

        // for all terrain types
        for (int i = 0; i < numberOfTypes; i++) {
            TerrainType terrainType = (TerrainType) world.get(SharedKey.TerrainTypes, i);
            int[] typesTreatedAsTheSame = new int[]{i};

            TileRenderer tileRenderer;
            progressMonitorModel.setValue(++progress);

            // TODO not a nice hack, unhack
            try {
                // XXX hack to make rivers flow into ocean and harbours & ocean
                // treat harbours as the same type.
                TerrainCategory thisTerrainCategory = terrainType.getCategory();

                if (thisTerrainCategory == TerrainCategory.River || thisTerrainCategory == TerrainCategory.Ocean) {
                    // Count number of types with category "water"
                    int count = 0;

                    for (int j = 0; j < numberOfTypes; j++) {
                        TerrainType t2 = (TerrainType) world.get(SharedKey.TerrainTypes, j);
                        TerrainCategory terrainCategory = t2.getCategory();

                        if (terrainCategory == TerrainCategory.Ocean || terrainCategory == thisTerrainCategory) {
                            count++;
                        }
                    }

                    typesTreatedAsTheSame = new int[count];
                    count = 0;

                    for (int j = 0; j < numberOfTypes; j++) {
                        TerrainType t2 = (TerrainType) world.get(SharedKey.TerrainTypes, j);
                        TerrainCategory terrainCategory = t2.getCategory();

                        if (terrainCategory == TerrainCategory.Ocean || terrainCategory == thisTerrainCategory) {
                            typesTreatedAsTheSame[count] = j;
                            count++;
                        }
                    }
                }

                tileRenderer = new RiverStyleTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
                tileRenderers.add(tileRenderer);

                continue;
            } catch (IOException ignored) {
            }

            try {
                tileRenderer = new ForestStyleTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
                tileRenderers.add(tileRenderer);

                continue;
            } catch (IOException ignored) {
            }

            // TODO this was the chequered tile renderer which was removed because it was not needede
            try {
                tileRenderer = new StandardTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
                tileRenderers.add(tileRenderer);

                continue;
            } catch (IOException ignored) {
            }

            try {
                tileRenderer = new StandardTileRenderer(imageManager, typesTreatedAsTheSame, terrainType);
                tileRenderers.add(tileRenderer);
            } catch (IOException ignored) {
            }
        }

        // add special tile renderer for harbours
        TileRenderer oceanTileRenderer = null;

        for (int j = 0; j < numberOfTypes; j++) {
            TerrainType t2 = (TerrainType) world.get(SharedKey.TerrainTypes, j);
            String terrainName = t2.getTerrainTypeName();

            if (terrainName.equalsIgnoreCase("Ocean")) {
                oceanTileRenderer = tileRenderers.get(j);

                break;
            }
        }

        for (int j = 0; j < numberOfTypes; j++) {
            TerrainType t2 = (TerrainType) world.get(SharedKey.TerrainTypes, j);
            String terrainName = t2.getTerrainTypeName();

            if (terrainName.equalsIgnoreCase("Harbour")) {
                TerrainType t = (TerrainType) world.get(SharedKey.TerrainTypes, j);
                TileRenderer tr = new SpecialTileRenderer(imageManager, new int[]{j}, t, oceanTileRenderer);
                tileRenderers.set(j, tr);
                break;
            }
        }

        return new StandardTileRendererList(tileRenderers);
    }

    public boolean validate(ReadOnlyWorld world) {
        return tileRendererList.validate(world) && trackPieceViewList.validate(world);
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
     * @param index
     * @return
     */
    public TileRenderer getTileRendererByIndex(int index) {
        return tileRendererList.getTileRendererByIndex(index);
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