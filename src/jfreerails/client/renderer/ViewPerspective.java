package jfreerails.client.renderer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ViewPerspective {
	
	private static final Set set=new HashSet();			
	public static final ViewPerspective SIDE_ON=new ViewPerspective();	
	public static final ViewPerspective OVERHEAD=new ViewPerspective();	
	
	private ViewPerspective(){		
		set.add(this);
	}
	
	public static Iterator iterator(){
		return set.iterator();
	}


}
