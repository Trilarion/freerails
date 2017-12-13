/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 17-May-2003
 *
 */
package org.railz.client.renderer;

import java.awt.Image;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.railz.client.common.ImageManager;
import org.railz.util.*;
import org.railz.client.view.*;
import org.railz.world.cargo.CargoType;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.train.*;


/**
 * This class stores the overhead and side on wagon and engine images.
 * @author Luke
 *
 */
public final class TrainImages {
    private final Image[] sideOnWagonImages;
    private final Image[] sideOnEmptyWagonImages;
    private final Image[][] overheadWagonImages;
    private final Image[] sideOnEngineImages;
    private final Image[][] overheadEngineImages;
    private final ImageManager imageManager;
    private final ReadOnlyWorld w;
    private final int[] engineLengths;
    private final int[] wagonLengths;

    public TrainImages(GUIRoot gr, ReadOnlyWorld w, ImageManager imageManager,
        FreerailsProgressMonitor pm) throws IOException {
	Logger logger = Logger.getLogger("global");
        this.w = w;
        this.imageManager = imageManager;

        final int numberOfWagonTypes = w.size(KEY.CARGO_TYPES,
		Player.AUTHORITATIVE);
        final int numberOfEngineTypes = w.size(KEY.ENGINE_TYPES,
		Player.AUTHORITATIVE);
	engineLengths = new int[numberOfEngineTypes];
	wagonLengths = new int[numberOfWagonTypes];
	for (int i = 0; i < numberOfEngineTypes; i++)
	    engineLengths[i] = TileRenderer.TILE_SIZE.width;
	for (int i = 0; i < numberOfWagonTypes; i++)
	    wagonLengths[i] = TileRenderer.TILE_SIZE.width;

        //Setup progress monitor..
        pm.setMax(numberOfWagonTypes + numberOfEngineTypes + 1);

        int progress = 0;
        pm.setValue(progress);

        sideOnWagonImages = new Image[numberOfWagonTypes];
	sideOnEmptyWagonImages = new Image[numberOfWagonTypes];
        overheadWagonImages = new Image[numberOfWagonTypes][8];
        sideOnEngineImages = new Image[numberOfEngineTypes];
        overheadEngineImages = new Image[numberOfEngineTypes][8];

        pm.setMessage(Resources.get("Loading wagon images."));
        for (int i = 0; i < numberOfWagonTypes; i++) {
            CargoType cargoType = (CargoType)w.get(KEY.CARGO_TYPES, i,
		    Player.AUTHORITATIVE);
            String sideOnFileName = generateSideOnFilename(cargoType.getName());
            sideOnWagonImages[i] = imageManager.getImage(sideOnFileName);
	    sideOnEmptyWagonImages[i] = imageManager.getImage
		(generateSideOnEmptyFileName(cargoType.getName()));

            for (int direction = 0; direction < 8; direction++) {
                String overheadOnFileName = generateOverheadFilename(cargoType.getName(),
                        direction);
                overheadWagonImages[i][direction] = imageManager.getImage(overheadOnFileName);
            }

            pm.setValue(++progress);
        }

        pm.setMessage(Resources.get("Loading engine images."));
        for (int i = 0; i < numberOfEngineTypes; i++) {
            EngineType engineType = (EngineType)w.get(KEY.ENGINE_TYPES, i,
		    Player.AUTHORITATIVE);
            String sideOnFileName = generateSideOnFilename(engineType.getEngineTypeName());
            sideOnEngineImages[i] = imageManager.getImage(sideOnFileName);

            for (int direction = 0; direction < 8; direction++) {
                String overheadOnFileName = generateOverheadFilename(engineType.getEngineTypeName(),
                        direction);
                overheadEngineImages[i][direction] = imageManager.getImage(overheadOnFileName);
            }

            pm.setValue(++progress);
        }

	pm.setMessage(Resources.get("Loading train metadata"));
	try {
	    SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
	    DefaultHandler dh = new WagonDataHandler();
	    sp.parse(gr.getGraphicsResourceFinder().getURLForReading
		    ("trains/overhead/wagondata.xml").toString(),
		    dh);
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Couldn't read train graphics " +
		    "metadata.", e);
	} catch (SAXException e) {
	    logger.log(Level.WARNING, "Couldn't parse the train graphics " +
		    "metadata.", e);
	} catch (Exception e) {
	    // shouldn't get these!
	    logger.log(Level.SEVERE, "Unexpected exception caught parsing " +
		    "train graphics metadata.", e);
	    assert false;
	}
	pm.setValue(++progress);
    }

    public Image getSideOnWagonImage(int cargoTypeNumber) {
        return sideOnWagonImages[cargoTypeNumber];
    }

    public Image getSideOnWagonImage(int cargoTypeNumber, int height, int
	    percentFull) {
	CargoType cargoType = (CargoType)w.get(KEY.CARGO_TYPES,
		cargoTypeNumber, Player.AUTHORITATIVE);
        String sideOnFileName;
       if (percentFull >= 50) {
	   sideOnFileName = generateSideOnFilename(cargoType.getName());
       } else {
	   sideOnFileName = generateSideOnEmptyFileName(cargoType.getName());
       }

        try {
            return imageManager.getScaledImage(sideOnFileName, height);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(sideOnFileName);
        }
    }

    public Image getSideOnWagonImage(int cargoTypeNumber, int height) {
	return getSideOnWagonImage(cargoTypeNumber, height, 100);
    }

    /**
     * @param direction a 3-bit CompassPoint 
     */
    public Image getOverheadWagonImage(int cargoTypeNumber, int direction) {
        return overheadWagonImages[cargoTypeNumber][direction];
    }

    public Image getSideOnEngineImage(int engineTypeNumber) {
        return sideOnEngineImages[engineTypeNumber];
    }

    public Image getSideOnEngineImage(int engineTypeNumber, int height) {
        EngineType engineType = (EngineType)w.get(KEY.ENGINE_TYPES,
                engineTypeNumber, Player.AUTHORITATIVE);
        String sideOnFileName = generateSideOnFilename(engineType.getEngineTypeName());

        try {
            return imageManager.getScaledImage(sideOnFileName, height);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(sideOnFileName);
        }
    }

    /**
     * @param direction a 3-bits CompassPoints vector
     */
    public Image getOverheadEngineImage(int engineTypeNumber, int direction) {
        return overheadEngineImages[engineTypeNumber][direction];
    }

    /**
     * @param i a 3-bit CompassPoints vector
     */
    private static String generateOverheadFilename(String name, int i) {
        return "trains/overhead/" + name +
        "_" + CompassPoints.toAbrvString(i) + ".png";
    }

    private static String generateSideOnFilename(String name) {
        return "trains/sideon/" + name +
        ".png";
    }

    private static String generateSideOnEmptyFileName(String name) {
	return "trains/sideon/empty/" + name + ".png";
    }
    
    public ImageIcon getWagonImage(int cargoType, int height) {
	for (int i = 0; i < w.size(KEY.WAGON_TYPES,
		    Player.AUTHORITATIVE); i++) {
	    if (((WagonType) w.get(KEY.WAGON_TYPES, i,
			    Player.AUTHORITATIVE)).getCargoType()
		    == cargoType) {
		Image icon = getSideOnWagonImage(i, height);
		return new ImageIcon(icon);
	    }
	}
	throw new IllegalArgumentException();
    }

    /**
     * Defines an XML ContentHandler for parsing the wagondata.dtd
     * which provides information about the graphics, for example
     * the lengths of the wagons
     */
    private class WagonDataHandler extends DefaultHandler {
	public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
	    if ("WagonType".equals(qName)) {
		NonNullElements i = new NonNullElements(KEY.WAGON_TYPES, w,
			Player.AUTHORITATIVE);
		while (i.next()) {
		    if (((WagonType) i.getElement()).getName().equals
			    (attributes.getValue("name"))) {
			wagonLengths[i.getIndex()] = 
			    Integer.parseInt(attributes.getValue("length"));
			return;
		    }
		}
		Logger.getLogger("global").log(Level.WARNING,
			"Unrecognized wagon type " + attributes.getValue
			("name"));
	    } else if ("EngineType".equals(qName)) {
		NonNullElements i = new NonNullElements(KEY.ENGINE_TYPES, w,
			Player.AUTHORITATIVE);
		while (i.next()) {
		    if (((EngineType) i.getElement()).getEngineTypeName()
			    .equals(attributes.getValue("name"))) {
			engineLengths[i.getIndex()] =
			    Integer.parseInt(attributes.getValue("length"));
			return;
		    }
		}
		Logger.getLogger("global").log(Level.WARNING,
			"Unrecognized engine type " + attributes.getValue
			("name"));
	    }
	}
    }

    /** @return the length of the specified engine in pixels */
    public int getEngineLength(int engineType) {
	return engineLengths[engineType];
    }

    /** @return the length of the specified wagon in pixels */
    public int getWagonLength(int wagonType) {
	return wagonLengths[wagonType];
    }
}
