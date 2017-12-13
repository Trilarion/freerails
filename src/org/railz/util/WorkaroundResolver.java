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
import org.xml.sax.*;

/**
 * Works around a bug in the Crimson parser which causes relative DTD
 * references in XML files to not be parsed correctly when in .jar files 
 */
public class WorkaroundResolver implements EntityResolver {
    private EntityResolver defaultResolver;

    public WorkaroundResolver(EntityResolver defaultResolver) {
	this.defaultResolver = defaultResolver;
    }

    public InputSource resolveEntity(String publicId, String systemId)
	throws SAXException, IOException {
	    if (systemId != null) {
		String prefix = null;
		String suffix = null;
	       if (systemId.startsWith("jar:")) {
		// keep everything up to the !
		   int i = systemId.indexOf('!');
		   if (i >= 0 && i < systemId.length() - 1) {
		       prefix = systemId.substring(0, i + 1);
		       suffix = systemId.substring(i + 1);
		   }
	       } else if (systemId.startsWith("file:")) {
		   prefix = "";
		   suffix = systemId;
	       } else {
		   return new InputSource(systemId);
	       }
	       String[] elements = suffix.split("/");
	       String newPath = "";
	       int j = 0;
	       for (int i = 0; i < elements.length; i++) {
		   if (elements[i].equals("..")) {
		       j = j - 2;
		   } else if (elements[i].equals(".")) {
		       j--;
		   } else {
		       elements[j] = elements[i];
		   }
		   j++;
	       }
	       for (int i = 0; i < j; i++) {
		   if (i > 0) {
		       newPath = newPath + "/";
		   }
		   newPath = newPath + elements[i];
	       }
	       systemId = prefix + newPath;
	    }
	    return new InputSource(systemId);
	}
}

