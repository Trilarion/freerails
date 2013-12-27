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

package org.railz.client.top;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.railz.client.common.ImageManager;
import org.railz.client.common.ImageManagerImpl;
import org.railz.client.config.ClientConfigurationConstants;
import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.ChequeredTileRenderer;
import org.railz.client.renderer.ForestStyleTileRenderer;
import org.railz.client.renderer.RiverStyleTileRenderer;
import org.railz.client.renderer.SpecialTileRenderer;
import org.railz.client.renderer.StandardTileRenderer;
import org.railz.client.renderer.StationRenderer;
import org.railz.client.renderer.TileRenderer;
import org.railz.client.renderer.TileRendererList;
import org.railz.client.renderer.TileRendererListImpl;
import org.railz.client.renderer.TrackPieceRendererList;
import org.railz.client.renderer.TrainImages;
import org.railz.client.renderer.ViewLists;
import org.railz.client.view.GUIRoot;
import org.railz.config.LogManager;
import org.railz.util.FreerailsProgressMonitor;
import org.railz.util.ModdableResourceFinder;
import org.railz.util.Resources;
import org.railz.world.building.BuildingType;
import org.railz.world.player.Player;
import org.railz.world.station.StationImprovement;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.KEY;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.ObjectKey;
import org.railz.world.top.ReadOnlyWorld;

public class ViewListsImpl implements ViewLists {
	private final ModelRoot modelRoot;
	private final TileRendererList tiles;
	private final TileRendererList buildingRenderers;
	// TODO
	// private final TileRendererList buildModeRenderers;
	private final TrackPieceRendererList trackPieceViewList;
	private final TrainImages trainImages;
	private final ImageManager imageManager;
	private final HashMap icons = new HashMap();
	private final GUIRoot guiRoot;
	private final ModdableResourceFinder mrf;
	private static final String CLASS_NAME = ViewListsImpl.class.getName();
	private static final Logger logger = LogManager.getLogger(CLASS_NAME);

	public ViewListsImpl(ModelRoot mr, GUIRoot gr, FreerailsProgressMonitor pm)
			throws IOException {
		guiRoot = gr;
		mrf = guiRoot.getGraphicsResourceFinder();
		modelRoot = mr;
		ReadOnlyWorld w = mr.getWorld();

		imageManager = new ImageManagerImpl(guiRoot.getClientJFrame(),
				ClientConfigurationConstants.IMAGE_PACKAGE_SLASH_PATH);

		tiles = loadTerrainRenderers(w, pm);
		buildingRenderers = loadBuildingRenderers(w, pm);

		// TODO initiate
		// buildModeRenderers = loadBuildViewRenderers(w, pm);

		trackPieceViewList = loadTrackViews(w, pm);

		trainImages = new TrainImages(guiRoot, w, imageManager, pm);
	}

	private TrackPieceRendererList loadTrackViews(ReadOnlyWorld w,
			FreerailsProgressMonitor pm) throws IOException {
		return new TrackPieceRendererList(w, imageManager, pm);
	}

	private TileRendererList loadBuildingRenderers(ReadOnlyWorld w,
			FreerailsProgressMonitor pm) throws IOException {
		// TODO generic
		pm.setMessage(Resources.get("Loading building graphics..."));

		ArrayList renderers = new ArrayList();

		KEY rendererType = KEY.BUILDING_TYPES;
		// TODO building types
		int nBuildingTypes = w.size(rendererType, Player.AUTHORITATIVE);
		pm.setMax(nBuildingTypes);
		NonNullElements i = new NonNullElements(rendererType, w,
				Player.AUTHORITATIVE);

		while (i.next()) {
			BuildingType buildingType = (BuildingType) i.getElement();
			int tileTypes[] = new int[] { i.getIndex() };
			pm.setValue(i.getIndex());
			/* try loading as a standard tile */
			try {
				StandardTileRenderer tr = new StandardTileRenderer(
						imageManager, tileTypes, buildingType);
				renderers.add(tr);
				continue;

			} catch (IOException e) {
				// ignore
			}
			/* try loading as a chequered tile */
			try {
				ChequeredTileRenderer tr = new ChequeredTileRenderer(
						imageManager, buildingType, tileTypes);
				renderers.add(tr);
				continue;
			} catch (IOException e) {
				// ignore
			}
			/* Try loading as a station tile */
			try {
				StationRenderer sr = new StationRenderer(imageManager,
						buildingType);
				renderers.add(sr);
				continue;
			} catch (IOException e) {
				// no more renderers to try
				pm.setMessage(Resources.get("Problem loading building "
						+ "graphics"));
				throw e;
			}
		}
		return new TileRendererListImpl(renderers);
	}

	private TileRendererList loadBuildViewRenderers(ReadOnlyWorld w,
			FreerailsProgressMonitor pm) {
		// TODO
		throw new UnsupportedOperationException("Not done yet.");
	}

	private TileRendererList loadTerrainRenderers(ReadOnlyWorld w,
			FreerailsProgressMonitor pm) throws IOException {
		ArrayList tileRenderers = new ArrayList();

		// Setup progress monitor..
		pm.setMessage(Resources.get("Loading terrain graphics."));

		int numberOfTypes = w.size(KEY.TERRAIN_TYPES, Player.AUTHORITATIVE);
		pm.setMax(numberOfTypes);

		int progress = 0;
		pm.setValue(progress);

		for (int i = 0; i < numberOfTypes; i++) {
			TerrainType t = (TerrainType) w.get(KEY.TERRAIN_TYPES, i,
					Player.AUTHORITATIVE);
			int[] typesTreatedAsTheSame = new int[] { i };

			TileRenderer tr = null;
			pm.setValue(++progress);

			try {
				// XXX hack to make rivers flow into ocean
				int thisTerrainCategory = t.getTerrainCategory();

				if (thisTerrainCategory == TerrainType.CATEGORY_RIVER
						|| thisTerrainCategory == TerrainType.CATEGORY_OCEAN) {
					// Count number of types with category "water"
					int count = 0;

					for (int j = 0; j < numberOfTypes; j++) {
						TerrainType t2 = (TerrainType) w.get(KEY.TERRAIN_TYPES,
								j, Player.AUTHORITATIVE);
						int terrainCategory = t2.getTerrainCategory();

						if (terrainCategory == TerrainType.CATEGORY_OCEAN
								|| terrainCategory == thisTerrainCategory) {
							count++;
						}
					}

					typesTreatedAsTheSame = new int[count];
					count = 0;

					for (int j = 0; j < numberOfTypes; j++) {
						TerrainType t2 = (TerrainType) w.get(KEY.TERRAIN_TYPES,
								j, Player.AUTHORITATIVE);
						int terrainCategory = t2.getTerrainCategory();

						if (terrainCategory == TerrainType.CATEGORY_OCEAN
								|| terrainCategory == thisTerrainCategory) {
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

			// TODO renderer stuff
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
				logger.log(Level.WARNING,
						"No tile renderer for " + t.getTerrainTypeName());
			}
		}

		// XXX add special tile renderer for habours
		TileRenderer occeanTileRenderer = null;

		for (int j = 0; j < numberOfTypes; j++) {
			TerrainType t2 = (TerrainType) w.get(KEY.TERRAIN_TYPES, j,
					Player.AUTHORITATIVE);
			String terrainName = t2.getTerrainTypeName();

			if (terrainName.equalsIgnoreCase("Ocean")) {
				occeanTileRenderer = (TileRenderer) tileRenderers.get(j);

				break;
			}
		}

		for (int j = 0; j < numberOfTypes; j++) {
			TerrainType t2 = (TerrainType) w.get(KEY.TERRAIN_TYPES, j,
					Player.AUTHORITATIVE);
			String terrainName = t2.getTerrainTypeName();

			if (terrainName.equalsIgnoreCase("Harbour")) {
				TerrainType t = (TerrainType) w.get(KEY.TERRAIN_TYPES, j,
						Player.AUTHORITATIVE);
				TileRenderer tr = new SpecialTileRenderer(imageManager,
						new int[] { j }, t, occeanTileRenderer);
				tileRenderers.set(j, tr);

				occeanTileRenderer = (TileRenderer) tileRenderers.get(j);

				break;
			}
		}

		return new TileRendererListImpl(tileRenderers);
	}

	@Override
	public TileRendererList getTileViewList() {
		return this.tiles;
	}

	@Override
	public TileRendererList getBuildingViewList() {
		return buildingRenderers;
	}

	@Override
	public TileRendererList getBuildModeViewList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrackPieceRendererList getTrackPieceViewList() {
		return trackPieceViewList;
	}

	@Override
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

	@Override
	public TrainImages getTrainImages() {
		return trainImages;
	}

	public ImageIcon getImageIconImpl(String iconName) {
		ImageIcon icon = (ImageIcon) icons.get(iconName);
		if (icon == null) {
			URL iconURL;
			iconURL = mrf.getURLForReading(iconName + ".png");
			if (iconURL == null) {
				logger.log(Level.WARNING, "Couldn't find icon for " + iconName);
				return null;
			}
			icons.put(iconName, new ImageIcon(iconURL));
		}
		return (ImageIcon) icons.get(iconName);
	}

	@Override
	public ImageIcon getImageIcon(String iconName) {
		return getImageIconImpl("toolbar/" + iconName);
	}

	@Override
	public ImageIcon getImageIcon(ObjectKey key, int type) {
		String path = null;
		String prefix = null;
		switch (type) {
		case LARGE_ICON:
			prefix = "48x48_";
			break;
		default:
			throw new IllegalArgumentException();
		}
		if (key.key == KEY.STATION_IMPROVEMENTS) {
			StationImprovement si = (StationImprovement) modelRoot.getWorld()
					.get(key.key, key.index, key.principal);
			// FIXME stationImprovements path in ClientConfigurationConstants
			path = "stationImprovements/" + prefix + si.getName();
		} else {
			throw new IllegalArgumentException();
		}
		return getImageIconImpl(path);
	}

}
