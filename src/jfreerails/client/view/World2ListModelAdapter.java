/*
 * Created on 23-Mar-2003
 * 
 */
package jfreerails.client.view;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import jfreerails.world.top.KEY;
import jfreerails.world.top.World;

/**
 * This class implements the GoF Adapter pattern.  It converts the
 * interface of a list on the World to a ListModel interface that can
 * be used by JLists.  Currently, change notification is <b>not</b> implemented.
 * @author Luke
 * 
 */
public class World2ListModelAdapter implements ListModel {
	
	private final KEY k;
	
	private final World w;
	
	public World2ListModelAdapter(World world, KEY key){
		this.k=key;
		this.w=world;
	}

	public int getSize() {
		return w.size(k);
	}

	public Object getElementAt(int i) {
		return w.get(k, i);
	}

	public void addListDataListener(ListDataListener arg0) {
		// TODO Auto-generated method stub

	}

	public void removeListDataListener(ListDataListener arg0) {
		// TODO Auto-generated method stub

	}

}
