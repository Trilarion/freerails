/*
 * Created on Mar 14, 2004
 */
package jfreerails.client.view;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class returns the Java System Properties as an HTML table.
 *
 * @author Luke
 *
 */
public class ShowJavaProperties {
    
    public static final int TABLE_WIDTH = 500;
    
    private static final Logger logger = Logger
            .getLogger(ShowJavaProperties.class.getName());
    
    public static void main(String[] args) {
        logger.info(getPropertiesHtmlString());
    }
    
    public static String getPropertiesHtmlString() {
        Properties p = System.getProperties();
        StringBuffer sb = new StringBuffer();
        /* We set the width of the table so that its text word-wraps. */
        sb.append("<html><h3>Java System Properties</h3><table width =\""
                + TABLE_WIDTH + "\" align = \"left\" valign = \"top\">\n");
        
        Enumeration keys = p.keys();
        
        //We use an ArrayList so that the keys can be sorted into alphabetical order
        ArrayList<String> list = new ArrayList<String>();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            list.add(key);
        }
        Collections.sort(list);
        for(String key : list){
            String value = p.getProperty(key);
                        /*
                         * Insert a line break after each ";". This makes reading classpath
                         * elements easier.
                         */
            value = value.replaceAll(";", ";<br>");
            sb
                    .append("<tr><td>" + key + " </td><td> " + value
                    + "</td></tr>\n");
        }
        
        
        sb.append("</table></html>\n");
        
        return sb.toString();
    }
}