package jfreerails.client.top;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import jfreerails.client.common.ImageManager;
import jfreerails.client.common.ImageManagerImpl;
import jfreerails.client.common.SoundManager;
import jfreerails.client.renderer.ChequeredTileRenderer;
import jfreerails.client.renderer.ForestStyleTileRenderer;
import jfreerails.client.renderer.RiverStyleTileRenderer;
import jfreerails.client.renderer.SpecialTileRenderer;
import jfreerails.client.renderer.StandardTileRenderer;
import jfreerails.client.renderer.TileRenderer;
import jfreerails.client.renderer.TileRendererList;
import jfreerails.client.renderer.TileRendererListImpl;
import jfreerails.client.renderer.TrackPieceRendererList;
import jfreerails.client.renderer.TrainImages;
import jfreerails.client.renderer.ViewLists;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;


/** Implementation of ViewLists whose constructor loads graphics and provides feed back
 * using a FreerailsProgressMonitor.
 * @author Luke
 */
public class ViewListsImpl implements ViewLists {
    private static final Logger logger = Logger.getLogger(ViewListsImpl.class.getName());
    private final TileRendererList tiles;
    private final TrackPieceRendererList trackPieceViewList;
    private final TrainImages trainImages;
    private final ImageManager imageManager;

    public ViewListsImpl(ReadOnlyWorld w, FreerailsProgressMonitor pm)
        throws IOException {
        URL out = ViewListsImpl.class.getResource("/experimental");
        imageManager = new ImageManagerImpl("/jfreerails/client/graphics/",
                out.getPath());
        tiles = loadNewTileViewList(w, pm);

        trackPieceViewList = loadTrackViews(w, pm);
        
        trainImages = new TrainImages(w, imageManager, pm);
        
        preloadSounds(pm);
        
    }

	private void preloadSounds(FreerailsProgressMonitor pm) {
		//Pre-load sounds..
        String[] soundsFiles = {"/jfreerails/client/sounds/buildtrack.wav",
        		"/jfreerails/client/sounds/cash.wav",
        		"/jfreerails/client/sounds/removetrack.wav",
        		"/jfreerails/client/sounds/whistle.wav"};
		pm.setMessage("Loading sounds");
		pm.setMax(soundsFiles.length);
		SoundManager sm = SoundManager.getSoundManager();
		for(int i = 0; i < soundsFiles.length; i++){
        	try {
				sm.addClip(soundsFiles[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedAudioFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pm.setValue(i+1);
        }
	}

    private TrackPieceRendererList loadTrackViews(ReadOnlyWorld w,
        FreerailsProgressMonitor pm) throws IOException {
        return new TrackPieceRendererList(w, imageManager, pm);
    }

    private TileRendererList loadNewTileViewList(ReadOnlyWorld w,
        FreerailsProgressMonitor pm) throws IOException {
        ArrayList<TileRenderer> tileRenderers = new ArrayList<TileRenderer>();

        //Setup progress monitor..
        pm.setMessage("Loading terrain graphics.");

        int numberOfTypes = w.size(SKEY.TERRAIN_TYPES);
        pm.setMax(numberOfTypes);

        int progress = 0;
        pm.setValue(progress);

        for (int i = 0; i < numberOfTypes; i++) {
            TerrainType t = (TerrainType)w.get(SKEY.TERRAIN_TYPES, i);
            int[] typesTreatedAsTheSame = new int[] {i};

            TileRenderer tr = null;
            pm.setValue(++progress);

            try {
                //XXX hack to make rivers flow into ocean and habours & occean
                // treate habours as the same type.
            	TerrainType.Category thisTerrainCategory = t.getCategory();

                if (thisTerrainCategory.equals(TerrainType.Category.River) ||
                        thisTerrainCategory.equals(TerrainType.Category.Ocean)) {
                    //Count number of types with category "water"
                    int count = 0;

                    for (int j = 0; j < numberOfTypes; j++) {
                        TerrainType t2 = (TerrainType)w.get(SKEY.TERRAIN_TYPES,
                                j);
                        TerrainType.Category  terrainCategory = t2.getCategory();

                        if (terrainCategory.equals(TerrainType.Category.Ocean) ||
                                terrainCategory.equals(
                                    thisTerrainCategory)) {
                            count++;
                        }
                    }

                    typesTreatedAsTheSame = new int[count];
                    count = 0;

                    for (int j = 0; j < numberOfTypes; j++) {
                        TerrainType t2 = (TerrainType)w.get(SKEY.TERRAIN_TYPES,
                                j);
                        TerrainType.Category terrainCategory = t2.getCategory();

                        if (terrainCategory.equals(TerrainType.Category.Ocean) ||
                                terrainCategory.equals(
                                    thisTerrainCategory)) {
                            typesTreatedAsTheSame[count] = j;
                            count++;
                        }
                    }
                }

                tr = new RiverStyleTileRenderer(imageManager,
                        typesTreatedAsTheSame, t);
                tileRenderers.add(tr);

                continue;
            } catch (IOException io) {
            }

            try {
                tr = new ForestStyleTileRenderer(imageManager,
                        typesTreatedAsTheSame, t);
                tileRenderers.add(tr);

                continue;
            } catch (IOException io) {
            }

            try {
                tr = new ChequeredTileRenderer(imageManager,
                        typesTreatedAsTheSame, t);
                tileRenderers.add(tr);

                continue;
            } catch (IOException io) {
            }

            try {
                tr = new StandardTileRenderer(imageManager,
                        typesTreatedAsTheSame, t);
                tileRenderers.add(tr);

                continue;
            } catch (IOException io) {
                // If the image is missing, we generate it.
                logger.warning("No tile renderer for " +
                    t.getTerrainTypeName());

                String filename = StandardTileRenderer.generateFilename(t.getTerrainTypeName());
                Image image = QuickRGBTileRendererList.createImageFor(t);
                imageManager.setImage(filename, image);

                //generatedImages.setImage(filename, image);
                try {
                    tr = new StandardTileRenderer(imageManager,
                            typesTreatedAsTheSame, t);
                    tileRenderers.add(tr);

                    continue;
                } catch (IOException io2) {
                    io2.printStackTrace();
                    throw new IllegalStateException();
                }
            }
        }

        //XXXX add special tile renderer for habours
        TileRenderer occeanTileRenderer = null;

        for (int j = 0; j < numberOfTypes; j++) {
            TerrainType t2 = (TerrainType)w.get(SKEY.TERRAIN_TYPES, j);
            String terrainName = t2.getTerrainTypeName();

            if (terrainName.equalsIgnoreCase("Ocean")) {
                occeanTileRenderer = tileRenderers.get(j);

                break;
            }
        }

        for (int j = 0; j < numberOfTypes; j++) {
            TerrainType t2 = (TerrainType)w.get(SKEY.TERRAIN_TYPES, j);
            String terrainName = t2.getTerrainTypeName();

            if (terrainName.equalsIgnoreCase("Harbour")) {
                TerrainType t = (TerrainType)w.get(SKEY.TERRAIN_TYPES, j);
                TileRenderer tr = new SpecialTileRenderer(imageManager,
                        new int[] {j}, t, occeanTileRenderer);
                tileRenderers.set(j, tr);

                occeanTileRenderer = tileRenderers.get(j);

                break;
            }
        }

        return new TileRendererListImpl(tileRenderers);
    }

    public TileRendererList getTileViewList() {
        return this.tiles;
    }

    public TrackPieceRendererList getTrackPieceViewList() {
        return this.trackPieceViewList;
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

    public TrainImages getTrainImages() {
        return trainImages;
    }

	public ImageManager getImageManager() {		
		return imageManager;
	}
}