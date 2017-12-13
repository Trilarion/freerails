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
 * Created on 30-Apr-2003
 *
 */
package org.railz.client.common;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.imageio.ImageIO;

import org.railz.util.*;

/**
 * @author Luke
 *
 */
public class ImageManagerImpl implements ImageManager {
    private ModdableResourceFinder mrf;

    /**
     * HashMap of BufferedImage
     */
    private HashMap imageHashMap = new HashMap();
    
    /**
     * HashMap of BufferedImage
     */
    private HashMap scaledImagesHashMap = new HashMap();
    private GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                            .getDefaultScreenDevice()
                                                                            .getDefaultConfiguration();

    /**
     * @param readpath UNIX-style path
     */
    public ImageManagerImpl(Component c, String readpath) {
	mrf = new ModdableResourceFinder(readpath);
	defaultConfiguration = c.getGraphicsConfiguration();
    }

    public void setPathToReadFrom(String s) {
	mrf = new ModdableResourceFinder(s);
    }

    /**
     * @param relativeFilename UNIX-style relative path
     */
    private BufferedImage loadImage(String relativeFilename) throws IOException {
        String read = relativeFilename;

        URL url = mrf.getURLForReading(read);

        if (null == url) {
            throw new IOException("Couldn't find: " + read);
        }
	// XXX This should improve performance, however uncommenting this line
	// causes exceptions (due to a bug in the JDK?)
	// ImageIO.setUseCache(false);
        BufferedImage tempImage = ImageIO.read(url);
	BufferedImage compatibleImage =
	    defaultConfiguration.createCompatibleImage(tempImage.getWidth(
                    null), tempImage.getHeight(null), Transparency.TRANSLUCENT);
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(tempImage, 0, 0, null);
	imageHashMap.put(relativeFilename, compatibleImage);
	g.dispose();
	compatibleImage.flush();

        return compatibleImage;
    }

    /**
     * @param relativeFilename UNIX-style relative path
     */
    public BufferedImage getImage(String relativeFilename) throws IOException {
        relativeFilename = relativeFilename.replace(' ', '_');

	if (imageHashMap.containsKey(relativeFilename)) {
	    return (BufferedImage)imageHashMap.get(relativeFilename);
	}

	return loadImage(relativeFilename);
    }

    public boolean contains(String relativeFilename) {
        relativeFilename = relativeFilename.replace(' ', '_');

        if (imageHashMap.containsKey(relativeFilename)) {
            return true;
        } 
	return false;
    }

    public void setImage(String relativeFilename, BufferedImage i) {
        relativeFilename = relativeFilename.replace(' ', '_');

        if (i == null) {
            throw new NullPointerException(relativeFilename);
        }

        imageHashMap.put(relativeFilename, i);
    }

    /**
     *  Returns the specified image scaled so that its height is equal to the
     * specified height.
     * @param relativeFilename UNIX-style relative path
     */
    public BufferedImage getScaledImage(String relativeFilename, int height)
        throws IOException {
        BufferedImage i = getImage(relativeFilename);
        String hashKey = relativeFilename + height;

        if (this.scaledImagesHashMap.containsKey(hashKey)) {
            return (BufferedImage)scaledImagesHashMap.get(hashKey);
        } else {
            if (i.getHeight(null) == height) {
                return i;
            } else {
                int width = (i.getWidth() * height) / i.getHeight();
		BufferedImage compatibleImage =
		    defaultConfiguration.createCompatibleImage(width,
                        height, Transparency.BITMASK);
                Graphics g = compatibleImage.getGraphics();
                g.drawImage(i, 0, 0, width, height, null);
                scaledImagesHashMap.put(hashKey, compatibleImage);

                return compatibleImage;
            }
        }
    }
}
