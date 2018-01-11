/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.client.view;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * Returns the Java System Properties as an HTML table.
 */
public class ShowJavaProperties {

    /**
     *
     */
    public static final int TABLE_WIDTH = 500;

    private static final Logger logger = Logger.getLogger(ShowJavaProperties.class.getName());

    private ShowJavaProperties() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        logger.info(getPropertiesHtmlString());
    }

    /**
     * @return
     */
    public static String getPropertiesHtmlString() {
        Properties p = System.getProperties();
        StringBuilder sb = new StringBuilder();
        /* We set the width of the table so that its text word-wraps. */
        sb.append("<html><h3>Java System Properties</h3><table width =\"" + TABLE_WIDTH + "\" align = \"left\" valign = \"top\">\n");

        Enumeration keys = p.keys();

        // We use an ArrayList so that the keys can be sorted into alphabetical
        // order
        List<String> list = new ArrayList<>();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            list.add(key);
        }
        Collections.sort(list);
        for (String key : list) {
            String value = p.getProperty(key);
            /*
             * Insert a line break after each ";". This makes reading classpath
             * elements easier.
             */
            value = value.replaceAll(";", ";<br>");
            sb.append("<tr><td>").append(key).append(" </td><td> ").append(value).append("</td></tr>\n");
        }

        sb.append("</table></html>\n");

        return sb.toString();
    }
}