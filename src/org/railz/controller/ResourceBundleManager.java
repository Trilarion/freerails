/*
 * Copyright (C) 2004 Robert Tuck
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
package org.railz.controller;

import java.util.*;
import java.io.*;

import org.railz.util.*;
/**
 * Converts a property list to an array of bytes suitable for network
 * transmission.
 */
public class ResourceBundleManager { 
    /**
     * A request to the server to obtain some resources.
     */
    public static class GetResourceCommand extends ServerCommand {
	public Locale locale;
	public String baseName;

	public GetResourceCommand(String baseName, Locale locale) {
	    this.locale = locale;
	    this.baseName = baseName;
	}
    }

    public static class GetResourceResponseCommand extends ServerCommand {
	private boolean successful;
	private byte[] properties;

	public GetResourceResponseCommand (byte []properties) {
	    if (properties.length == 0) {
		successful = false;
	    } else {
		successful = true;
		this.properties = properties;
	    }
	}

	public boolean isSuccessful() {
	    return successful;
	}

	public ResourceBundle getResourceBundle() {
	    try {
		ByteArrayInputStream bis = new
		    ByteArrayInputStream(properties);
		return new PropertyResourceBundle(bis);
	    } catch (Exception e) {
		return null;
	    }
	}
    }

    public static byte[] getResourceByteArray(Locale l, String baseName) {
	try {
	    baseName = baseName.replace('.', '/');
	    InputStream is = ResourceBundleManager.class
		.getResourceAsStream(baseName + "_" + l.toString());
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    int max;
	    byte buf[] = new byte[1024];
	    while ((max = is.read(buf)) != -1) {
		bos.write(buf, 0, max);
	    }
	    return bos.toByteArray();
	} catch (Exception e) {
	    return new byte[0];
	}
    }
}
