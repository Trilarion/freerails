/*
 * Created on 23-Mar-2003
 *
 */
package jfreerails.client.view;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;


/**
 * Converts the interface of a list on the world object to a ListModel interface that can be used by JLists.
 * Currently, change notification is <b>not</b> implemented (null elements are skipped).
 *
 * @author Luke
 *
 */
public class World2ListModelAdapter implements ListModel {
 
    private final ReadOnlyWorld w;
    private final NonNullElements elements;

    public World2ListModelAdapter(ReadOnlyWorld world, SKEY key) {
       
        this.w = world;
       

        if (null == key)
            throw new NullPointerException();

        if (null == w)
            throw new NullPointerException();
        
        elements = new NonNullElements(key, world);
    }

    public World2ListModelAdapter(ReadOnlyWorld world, KEY key,
        FreerailsPrincipal p) {
       
        this.w = world;
       
        if (null == key)
            throw new NullPointerException();

        if (null == p)
            throw new NullPointerException();

        if (null == w)
            throw new NullPointerException();

        //Check that the principal exists.
        if (!world.isPlayer(p))
            throw new IllegalArgumentException(p.getName());
        
        elements = new NonNullElements(key, world, p);
    }

    public int getSize() {
    	return elements.size();
    }

    public Object getElementAt(int i) {
    	elements.gotoRow(i);        
		return elements.getElement();
    }

    public void addListDataListener(ListDataListener arg0) {
        // TODO Auto-generated method stub
    }

    public void removeListDataListener(ListDataListener arg0) {
        // TODO Auto-generated method stub
    }
}