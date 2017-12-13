/*
 * Copyright (C) 2005 Robert Tuck
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

package org.railz.util;

import java.io.*;
import java.net.*;
import java.util.logging.*;

/**
 * Provides a resource-finding facility for resources which may be bundled
 * with the application, or provided in user-customisations.
 */
public class ModdableResourceFinder {
    private String prefix;
    private File prefixedHomeDir;
    private static final Logger logger = Logger.getLogger("global");

    /**
     * @param prefix a UNIX-style search path which is prefixed to all
     * requests for resources.
     */
    public ModdableResourceFinder(String prefix) {
	// chop off trailing '/' if any
	if (prefix.length() > 0 &&
		prefix.charAt(prefix.length() - 1) == '/')
	    prefix = prefix.substring(0, prefix.length() - 1);
	String homeDir = System.getProperty("user.home");
	URI uri;
	try {
	    uri = (new File(homeDir)).toURI().resolve(new URI(prefix)); 
	} catch (URISyntaxException e) {
	    throw new IllegalArgumentException
		("Illegal argument " + prefix + ": " + e.getMessage());
	}
	// add initial '/'  if not already present
	if (prefix.length() == 0 ||
		prefix.charAt(0) != '/')
	    prefix = "/" + prefix;
	// append trailing '/'
	if (prefix.charAt(prefix.length() - 1) != '/')
	    prefix = prefix + "/";
	this.prefix = prefix;
	prefixedHomeDir = new File(uri);
    }

    /**
     * Searches for the resource first in the customisable resource area, then
     * using the ClassLoader for this class.
     * @return a URL pointing to the desired resource.
     * @param relPath relative path ('/' separated) to the resource
     */
    public URL getURLForReading(String relPath) {
	/* first search user.home */
	File f = new File(prefixedHomeDir, relPath);
	URL u;
	if (f.exists()) {
	    try {
		u = f.toURL();
	    } catch (MalformedURLException e) {
		throw new IllegalArgumentException
		    ("Illegal argument " + relPath + ": " + e.getMessage());
	    }
	    logger.log(Level.FINE, "Getting URL " + u);
	    return u;
	}
	
	// file did not exist, open default URL
	u = getClass().getResource(prefix + relPath);
	logger.log(Level.FINE, "Getting URL " + u + " for " + relPath);
	return u;
    }

    /**
     * Searches for the resource first in the customisable resource area, then
     * using the ClassLoader for this class.
     * @return a File pointing to the desired resource.
     * @param relPath relative path ('/' separated) to the resource
     */
    public File getFileForWriting(String relPath) throws IOException {
	File f = new File(prefixedHomeDir, relPath);
	File parent = f.getParentFile();
	if (!parent.exists()) {
	    try {
		parent.mkdirs();
	    } catch (SecurityException e) {
		throw new IOException("Couldn't create parent directories " +
			"for " + f.toString());
	    }
	}
	return f;
    }

}
