package jfreerails.client.top;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import jfreerails.client.common.ImageManager;
import jfreerails.client.common.ImageManagerImpl;
import jfreerails.client.renderer.ChequeredTileRenderer;
import jfreerails.client.renderer.ForestStyleTileRenderer;
import jfreerails.client.renderer.RiverStyleTileRenderer;
import jfreerails.client.renderer.SideOnTrainTrainViewImages;
import jfreerails.client.renderer.StandardTileRenderer;
import jfreerails.client.renderer.TileRenderer;
import jfreerails.client.renderer.TileRendererList;
import jfreerails.client.renderer.TileRendererListImpl;
import jfreerails.client.renderer.TrackPieceRendererList;
import jfreerails.client.renderer.TrainImages;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;

public class ViewListsImpl implements ViewLists {

	private final TileRendererList tiles;
	private final TrackPieceRendererList trackPieceViewList;

	private final SideOnTrainTrainViewImages sideOnTrainTrainView;

	private final TrainImages trainImages;

	private final ImageManager imageManager;

	public ViewListsImpl(ReadOnlyWorld w) throws IOException {

		URL out = ViewListsImpl.class.getResource("/experimental");
		URL in = ViewListsImpl.class.getResource("/jfreerails/client/graphics");

		imageManager = new ImageManagerImpl("/jfreerails/client/graphics/", out.getPath());

		//tiles = new QuickRGBTileRendererList(w);
		tiles = loadNewTileViewList(w);

		trackPieceViewList = loadTrackViews(w);

		//engine views

		sideOnTrainTrainView = addTrainViews();

		trainImages = new TrainImages(w, imageManager);
	}
	
	public TrackPieceRendererList loadTrackViews(ReadOnlyWorld w) throws IOException {		
		return new TrackPieceRendererList(w, imageManager);
	}

	private static SideOnTrainTrainViewImages addTrainViews() {
		//wagon views
		Image tempImage = null;

		SideOnTrainTrainViewImages sideOnTrainTrainView = new SideOnTrainTrainViewImages(5, 3);
		URL wagon = ViewListsImpl.class.getResource("/jfreerails/data/wagon_151x100.png");
		System.out.println(wagon);
		tempImage = (new javax.swing.ImageIcon(wagon)).getImage();
		sideOnTrainTrainView.setWagonImage(0, tempImage);
		sideOnTrainTrainView.setWagonImage(1, tempImage);
		sideOnTrainTrainView.setWagonImage(2, tempImage);
		sideOnTrainTrainView.setWagonImage(3, tempImage);
		sideOnTrainTrainView.setWagonImage(4, tempImage);
		URL engine = ViewListsImpl.class.getResource("/jfreerails/data/engine_350x100.png");
		System.out.println(engine);
		tempImage = (new javax.swing.ImageIcon(engine)).getImage();
		sideOnTrainTrainView.setEngineImage(0, tempImage);
		sideOnTrainTrainView.setEngineImage(1, tempImage);
		sideOnTrainTrainView.setEngineImage(2, tempImage);
		return sideOnTrainTrainView;
	}

	public TileRendererList loadNewTileViewList(ReadOnlyWorld w) throws IOException {
		ArrayList tileRenderers = new ArrayList();
		
		for (int i = 0; i < w.size(KEY.TERRAIN_TYPES); i++) {

			TerrainType t = (TerrainType) w.get(KEY.TERRAIN_TYPES, i);
			int[] typesTreatedAsTheSame = new int[] { i };

			TileRenderer tr = null;
			Integer rgb = new Integer(t.getRGB());
			try {
				tr = new RiverStyleTileRenderer(imageManager, typesTreatedAsTheSame, t);
				tileRenderers.add(tr);
				continue;
			} catch (IOException io) {
			}
			try {
				tr = new ForestStyleTileRenderer(imageManager, typesTreatedAsTheSame, t);
				tileRenderers.add(tr);
				continue;
			} catch (IOException io) {
			}
			try {
				tr = new ChequeredTileRenderer(imageManager, typesTreatedAsTheSame, t);
				tileRenderers.add(tr);
				continue;
			} catch (IOException io) {
			}
			try {
				tr = new StandardTileRenderer(imageManager, typesTreatedAsTheSame, t);
				tileRenderers.add(tr);
				continue;
			} catch (IOException io) {
				// If the image is missing, we generate it.

				System.out.println("No tile renderer for " + t.getTerrainTypeName());
				String filename = StandardTileRenderer.generateFilename(t.getTerrainTypeName());
				Image image = QuickRGBTileRendererList.createImageFor(t);
				imageManager.setImage(filename, image);
				//generatedImages.setImage(filename, image);
				try {

					tr = new StandardTileRenderer(imageManager, typesTreatedAsTheSame, t);
					tileRenderers.add(tr);
					continue;
				} catch (IOException io2) {
					io2.printStackTrace();
					throw new IllegalStateException();
				}
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

	public SideOnTrainTrainViewImages getSideOnTrainTrainViewImages() {
		return sideOnTrainTrainView;
	}

	public TrainImages getTrainImages() {		
		return trainImages;
	}

}
