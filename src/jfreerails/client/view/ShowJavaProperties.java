/*
 * Created on Mar 14, 2004
 */
package jfreerails.client.view;

import java.util.Enumeration;
import java.util.Properties;

/**
 *	This class returns the Java System Properties as an HTML table.
 *  @author Luke  
 *
 */
public class ShowJavaProperties {

	public static void main(String[] args) {
		System.out.println(getPropertiesHtmlString());		
	}

	public static String getPropertiesHtmlString() {
		Properties p = System.getProperties();
		String s = p.toString();
		StringBuffer sb = new StringBuffer();
		sb.append("<html><h3>Java System Properties</h3><table>\n");
		
		Enumeration keys = p.keys();                
		while(keys.hasMoreElements()){
			String key =(String)keys.nextElement();
			String value = p.getProperty(key);
			sb.append("<tr><td>"+key+" </td><td> "+value+"</td></tr>\n");                                                                   
		}
		sb.append("</table></html>\n");
		return sb.toString();
	}
}
