
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.File;

import jfreerails.client.common.BinaryNumberFormatter;
import jfreerails.client.common.ImageManager;
import jfreerails.world.track.TrackConfiguration;

/**
*  This class renders a track piece.
*
*@author     Luke Lindsay
*     09 October 2001
*/

final public  class TrackPieceRendererImpl implements TrackPieceRenderer {

	Image[] trackPieceIcons = new Image[512];
	
	private final String typeName;

	

	public void drawTrackPieceIcon(
		int trackTemplate,
		java.awt.Graphics g,
		int x,
		int y,
		java.awt.Dimension tileSize) {
		if ((trackTemplate > 511) || (trackTemplate < 0)) {
			throw new java.lang.IllegalArgumentException(
				"trackTemplate = "
					+ trackTemplate
					+ ", it should be in the range 0-511");
		}
		if (trackPieceIcons[trackTemplate] != null) {
			int drawX = x * tileSize.width - tileSize.width / 2;
			int drawY = y * tileSize.height - tileSize.height / 2;
			g.drawImage(trackPieceIcons[trackTemplate], drawX, drawY, null);
		}
	}

	/**
	*  Creates new TrackPieceView
	*
	*@param  trackTemplatesPrototypes  int's representing the legal track pieces.
	*@param  trackImageSplitter        Source of track icons
	*@exception  FreerailsException    Description of Exception
	*/

	public TrackPieceRendererImpl(
		int[] trackTemplatesPrototypes,
		jfreerails.client.common.ImageSplitter trackImageSplitter, String name) {
		trackImageSplitter.setTransparencyToTRANSLUCENT();
		typeName = name;

		//Since track tiles have transparent regions.
		for (int i = 0; i < trackTemplatesPrototypes.length; i++) {

			/*
			*  Check for invalid parameters.
			*/
			if ((trackTemplatesPrototypes[i] > 511)
				|| (trackTemplatesPrototypes[i] < 0)) {
				throw new java.lang.IllegalArgumentException(
					"trackTemplate = "
						+ trackTemplatesPrototypes[i]
						+ ", it should be in the range 0-511");
			}

			/*
			*  Grab the images for those track pieces that are legal.
			*/
			for (int j = 0; j < trackTemplatesPrototypes.length; j++) {
				int[] rotationsOfTrackTemplate =
					jfreerails
						.world
						.track
						.EightRotationsOfTrackPieceProducer
						.getRotations(
						trackTemplatesPrototypes[j]);
				for (int k = 0; k < rotationsOfTrackTemplate.length; k++) {
					if (trackPieceIcons[rotationsOfTrackTemplate[k]] == null) {
						trackPieceIcons[rotationsOfTrackTemplate[k]] =
							trackImageSplitter.getTileFromSubGrid(k, j);
					}
				}
			}
		}
	}

	
	public Image getTrackPieceIcon(int trackTemplate) {
		if ((trackTemplate > 511) || (trackTemplate < 0)) {
			throw new java.lang.IllegalArgumentException(
				"trackTemplate = "
					+ trackTemplate
					+ ", it should be in the range 0-511");
		}
		return trackPieceIcons[trackTemplate];
	}

	public void dumpImages(ImageManager imageManager) {
		String relativeFileNameBase = "track" + File.separator + this.getTrackTypeName();
		
		for (int i = 0 ; i < 512 ; i++){
			if(trackPieceIcons[i] != null){
				int newTemplate = TrackConfiguration.getFlatInstance(i).getNewTemplateNumber();
				String fileName = relativeFileNameBase+"_"+BinaryNumberFormatter.formatWithLowBitOnLeft(newTemplate, 8)+".png";	
				imageManager.setImage(fileName,trackPieceIcons[i]);							
			}			
		}			
	}

	public String getTrackTypeName() {		
		return typeName;
	}
}
