/*
 * Created by IntelliJ IDEA.
 * User: lindsal
 * Date: Jan 14, 2002
 * Time: 4:15:15 PM
 * To change template for new interface use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package jfreerails.world.train;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


public class WagonType {
	
	public static final int NUMBER_OF_CATEGORIES=6;
   
	public static final int MAIL=0;
	

	public static final int PASSENGER=1;
	

	public static final int FAST_FREIGHT=2;
	

	public static final int SLOW_FREIGHT=3;
	

	public static final int BULK_FREIGHT=4;
	

	public static final int ENGINE=5;
	
	private final String typeName;
	
	private final int typeCategory;

	public WagonType(String name, int category){
		typeName=name;
		typeCategory=category;				
	}
	
	public String getName(){
		return typeName;
	}
	
	public int getCategory(){
		return typeCategory;
	}
		
	
}
