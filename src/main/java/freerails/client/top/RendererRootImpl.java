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

package freerails.client.top;

import freerails.client.ClientConfig;
import freerails.client.common.ImageManager;
import freerails.client.common.ImageManagerImpl;
import freerails.client.common.SoundManager;
import freerails.client.renderer.*;
import freerails.util.ProgressMonitor;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.cargo.CargoType;
import freerails.world.terrain.TerrainCategory;
import freerails.world.terrain.TerrainType;
import freerails.world.train.EngineType;
import org.apache.log4j.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Implementation of RendererRoot whose constructor loads graphics and provides
 * feed back using a ProgressMonitor.
 */
public class RendererRootImpl implements RendererRoot {
    private static final Logger logger = Logger
            .getLogger(RendererRootImpl.class.getName());

    private final TileRendererList tiles;

    private final TrackPieceRendererList trackPieceViewList;

    private final ImageManager imageManager;

    private final ArrayList<TrainImages> wagonImages = new ArrayList<>();

    private final ArrayList<TrainImages> engineImages = new ArrayList<>();

    /**
     * @param w
     * @param pm
     * @throws IOException
     */
    public RendererRootImpl(ReadOnlyWorld w, ProgressMonitor pm)
            throws IOException {
        imageManager = new ImageManagerImpl(ClientConfig.GRAPHICS_PATH);
        tiles = loadNewTileViewList(w, pm);

        trackPieceViewList = loadTrackViews(w, pm);

        // rr = new OldTrainImages(w, imageManager, pm);
        loadTrainImages(w, pm);
        preloadSounds(pm);
    }

    private void loadTrainImages(ReadOnlyWorld w, ProgressMonitor pm)
            throws IOException {
        // Setup progress monitor..
        final int numberOfWagonTypes = w.size(SKEY.CARGO_TYPES);
        final int numberOfEngineTypes = w.size(SKEY.ENGINE_TYPES);
        pm.nextStep(numberOfWagonTypes + numberOfEngineTypes);
        int progress = 0;
        pm.setValue(progress);

        // Load wagon images.
        for (int i = 0; i < numberOfWagonTypes; i++) {
            CargoType cargoType = (CargoType) w.get(SKEY.CARGO_TYPES, i);
            String name = cargoType.getName();
            TrainImages ti = new TrainImages(imageManager, name);
            wagonImages.add(ti);
            pm.setValue(++progress);
        }

        // Load engine images
        for (int i = 0; i < numberOfEngineTypes; i++) {
            EngineType engineType = (EngineType) w.get(SKEY.ENGINE_TYPES, i);
            String engineTypeName = engineType.getEngineTypeName();
            TrainImages ti = new TrainImages(imageManager, engineTypeName);
            engineImages.add(ti);
            pm.setValue(++progress);
        }

    }

    private void preloadSounds(ProgressMonitor pm) {
        // Pre-load sounds..
        String[] soundsFiles = {ClientConfig.SOUND_BUILD_TRACK,
                ClientConfig.SOUND_CASH,
                ClientConfig.SOUND_REMOVE_TRACK,
                ClientConfig.SOUND_WHISTLE};
        pm.nextStep(soundsFiles.length);
        SoundManager sm = SoundManager.getSoundManager();
        for (int i = 0; i < soundsFiles.length; i++) {
            try {
                sm.addClip(soundsFiles[i]);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                // TODO Auto-generated catch block
            }
            pm.setValue(i + 1);
        }
    }

    private TrackPieceRendererList loadTrackViews(ReadOnlyWorld w,
                                                  ProgressMonitor pm) throws IOException {
        return new TrackPieceRendererList(w, imageManager, pm);
    }

    private TileRendererList loadNewTileViewList(ReadOnlyWorld w,
                                                 ProgressMonitor pm) throws IOException {
        ArrayList<TileRenderer> tileRenderers = new ArrayList<>();

        // Setup progress monitor..

        int numberOfTypes = w.size(SKEY.TERRAIN_TYPES);
        pm.nextStep(numberOfTypes);

        int progress = 0;
        pm.setValue(progress);

        for (int i = 0; i < numberOfTypes; i++) {
            TerrainType t = (TerrainType) w.get(SKEY.TERRAIN_TYPES, i);
            int[] typesTreatedAsTheSame = new int[]{i};

            TileRenderer tr = null;
            pm.setValue(++progress);

            try {
                // XXX hack to make rivers flow into ocean and harbours & ocean
                // treat harbours as the same type.
                TerrainCategory thisTerrainCategory = t.getCategory();

                if (thisTerrainCategory.equals(TerrainCategory.River)
                        || thisTerrainCategory
                        .equals(TerrainCategory.Ocean)) {
                    // Count number of types with category "water"
                    int count = 0;

                    for (int j = 0; j < numberOfTypes; j++) {
                        TerrainType t2 = (TerrainType) w.get(
                                SKEY.TERRAIN_TYPES, j);
                        TerrainCategory terrainCategory = t2.getCategory();

                        if (terrainCategory.equals(TerrainCategory.Ocean)
                                || terrainCategory.equals(thisTerrainCategory)) {
                            count++;
                        }
                    }

                    typesTreatedAsTheSame = new int[count];
                    count = 0;

                    for (int j = 0; j < numberOfTypes; j++) {
                        TerrainType t2 = (TerrainType) w.get(
                                SKEY.TERRAIN_TYPES, j);
                        TerrainCategory terrainCategory = t2.getCategory();

                        if (terrainCategory.equals(TerrainCategory.Ocean)
                                || terrainCategory.equals(thisTerrainCategory)) {
                            typesTreatedAsTheSame[count] = j;
                            count++;
                        }
                    }
                }

                tr = new RiverStyleTileRenderer(imageManager,
                        typesTreatedAsTheSame, t, w);
                tileRenderers.add(tr);

                continue;
            } catch (IOException io) {
            }

            try {
                tr = new ForestStyleTileRenderer(imageManager,
                        typesTreatedAsTheSame, t, w);
                tileRenderers.add(tr);

                continue;
            } catch (IOException io) {
            }

            try {
                tr = new ChequeredTileRenderer(imageManager,
                        typesTreatedAsTheSame, t, w);
                tileRenderers.add(tr);

                continue;
            } catch (IOException io) {
            }

            try {
                tr = new StandardTileRenderer(imageManager,
                        typesTreatedAsTheSame, t, w);
                tileRenderers.add(tr);

            } catch (IOException io) {
                // If the image is missing, we generate it.
                logger.warn("No tile renderer for " + t.getTerrainTypeName());

                String filename = StandardTileRenderer.generateFilename(t
                        .getTerrainTypeName());
                Image image = QuickRGBTileRendererList.createImageFor(t);
                imageManager.setImage(filename, image);

                // generatedImages.setImage(filename, image);
                try {
                    tr = new StandardTileRenderer(imageManager,
                            typesTreatedAsTheSame, t, w);
                    tileRenderers.add(tr);

                } catch (IOException io2) {
                    io2.printStackTrace();
                    throw new IllegalStateException();
                }
            }
        }

        // add special tile renderer for harbours
        TileRenderer occeanTileRenderer = null;

        for (int j = 0; j < numberOfTypes; j++) {
            TerrainType t2 = (TerrainType) w.get(SKEY.TERRAIN_TYPES, j);
            String terrainName = t2.getTerrainTypeName();

            if (terrainName.equalsIgnoreCase("Ocean")) {
                occeanTileRenderer = tileRenderers.get(j);

                break;
            }
        }

        for (int j = 0; j < numberOfTypes; j++) {
            TerrainType t2 = (TerrainType) w.get(SKEY.TERRAIN_TYPES, j);
            String terrainName = t2.getTerrainTypeName();

            if (terrainName.equalsIgnoreCase("Harbour")) {
                TerrainType t = (TerrainType) w.get(SKEY.TERRAIN_TYPES, j);
                TileRenderer tr = new SpecialTileRenderer(imageManager,
                        new int[]{j}, t, occeanTileRenderer, w);
                tileRenderers.set(j, tr);
                break;
            }
        }

        return new TileRendererListImpl(tileRenderers);
    }

    public boolean validate(ReadOnlyWorld w) {
        boolean okSoFar = true;

        if (!this.tiles.validate(w)) {
            okSoFar = false;
        }

        if (!this.trackPieceViewList.validate(w)) {
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
    public Image getScaledImage(String relativeFilename, int height)
            throws IOException {
        return imageManager.getScaledImage(relativeFilename, height);
    }
}