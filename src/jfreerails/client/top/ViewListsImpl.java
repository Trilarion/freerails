/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.client.top;

import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import jfreerails.client.model.ModelRoot;
import jfreerails.client.view.GUIRoot;
import jfreerails.client.common.ImageManager;
import jfreerails.client.common.ImageManagerImpl;
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
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;


public class ViewListsImpl implements ViewLists {
    private final TileRendererList tiles;
    private final TrackPieceRendererList trackPieceViewList;
    private final TrainImages trainImages;
    private final ImageManager imageManager;
    private final HashMap icons = new HashMap();
    private final GUIRoot guiRoot;

    public ViewListsImpl(ModelRoot mr, GUIRoot gr,
	    FreerailsProgressMonitor pm)
        throws IOException {
	    guiRoot = gr;
	    ReadOnlyWorld w = mr.getWorld();
        URL in = ViewListsImpl.class.getResource("/jfreerails/client/graphics");

	imageManager = new
	    ImageManagerImpl(guiRoot.getClientJFrame(),
		    "/jfreerails/client/graphics/");
        tiles = loadNewTileViewList(w, pm);

        trackPieceViewList = loadTrackViews(w, pm);

        trainImages = new TrainImages(w, imageManager, pm);
    }

    public TrackPieceRendererList loadTrackViews(ReadOnlyWorld w,
        FreerailsProgressMonitor pm) throws IOException {
        return new TrackPieceRendererList(w, imageManager, pm);
    }

    public TileRendererList loadNewTileViewList(ReadOnlyWorld w,
        FreerailsProgressMonitor pm) throws IOException {
        ArrayList tileRenderers = new ArrayList();

        //Setup progress monitor..
        pm.setMessage("Loading terrain graphics.");

        int numberOfTypes = w.size(KEY.TERRAIN_TYPES);
        pm.setMax(numberOfTypes);

        int progress = 0;
        pm.setValue(progress);

        for (int i = 0; i < numberOfTypes; i++) {
            TerrainType t = (TerrainType)w.get(KEY.TERRAIN_TYPES, i);
            int[] typesTreatedAsTheSame = new int[] {i};

            TileRenderer tr = null;
            Integer rgb = new Integer(t.getRGB());
            pm.setValue(++progress);

            try {
                //XXX hack to make rivers flow into ocean and habours & occean
                // treate habours as the same type.
                String thisTerrainCategory = t.getTerrainCategory();

                if (thisTerrainCategory.equalsIgnoreCase("River") ||
                        thisTerrainCategory.equalsIgnoreCase("Ocean")) {
                    //Count number of types with category "water"
                    int count = 0;

                    for (int j = 0; j < numberOfTypes; j++) {
                        TerrainType t2 = (TerrainType)w.get(KEY.TERRAIN_TYPES, j);
                        String terrainCategory = t2.getTerrainCategory();

                        if (terrainCategory.equalsIgnoreCase("Ocean") ||
                                terrainCategory.equalsIgnoreCase(
                                    thisTerrainCategory)) {
                            count++;
                        }
                    }

                    typesTreatedAsTheSame = new int[count];
                    count = 0;

                    for (int j = 0; j < numberOfTypes; j++) {
                        TerrainType t2 = (TerrainType)w.get(KEY.TERRAIN_TYPES, j);
                        String terrainCategory = t2.getTerrainCategory();

                        if (terrainCategory.equalsIgnoreCase("Ocean") ||
                                terrainCategory.equalsIgnoreCase(
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
                System.err.println("No tile renderer for " +
                    t.getTerrainTypeName());

                String filename = StandardTileRenderer.generateFilename(t.getTerrainTypeName());
                BufferedImage image = QuickRGBTileRendererList.createImageFor(t);
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
            TerrainType t2 = (TerrainType)w.get(KEY.TERRAIN_TYPES, j);
            String terrainName = t2.getTerrainTypeName();

            if (terrainName.equalsIgnoreCase("Ocean")) {
                occeanTileRenderer = (TileRenderer)tileRenderers.get(j);

                break;
            }
        }

        for (int j = 0; j < numberOfTypes; j++) {
            TerrainType t2 = (TerrainType)w.get(KEY.TERRAIN_TYPES, j);
            String terrainName = t2.getTerrainTypeName();

            if (terrainName.equalsIgnoreCase("Harbour")) {
                TerrainType t = (TerrainType)w.get(KEY.TERRAIN_TYPES, j);
                TileRenderer tr = new SpecialTileRenderer(imageManager,
                        new int[] {j}, t, occeanTileRenderer);
                tileRenderers.set(j, tr);

                occeanTileRenderer = (TileRenderer)tileRenderers.get(j);

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

    public ImageIcon getImageIcon(String iconName) {
	ImageIcon icon = (ImageIcon) icons.get(iconName);
 	if (icon == null) {
 	    URL iconURL;
 	    iconURL = this.getClass().getClass().getResource
 		("/jfreerails/client/graphics/toolbar/" + iconName +
 		 ".png");
 	    if (iconURL == null) {
 		System.err.println("Couldn't find icon for " + iconName);
 		return null;
 	    }
 	    icons.put(iconName, new ImageIcon(iconURL));
 	}
 	return (ImageIcon) icons.get(iconName);
    }
}
