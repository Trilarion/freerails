/*
 * Created on 23-Mar-2003
 *
 */
package jfreerails.client.view;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;


/**
 * This class implements the GoF Adapter pattern. It converts the interface of
 * a list on the World to a ListModel interface that can be used by JLists.
 * Currently, change notification is <b>not</b> implemented.
 *
 * @author Luke
 *
 */
public class World2ListModelAdapter implements ListModel {
    private final KEY k;
    private final SKEY skey;
    private final FreerailsPrincipal principal;
    private final ReadOnlyWorld w;

    public World2ListModelAdapter(ReadOnlyWorld world, SKEY key) {
        this.k = null;
        this.w = world;
        skey = key;
        principal = null;

        if (null == key)
            throw new NullPointerException();

        if (null == w)
            throw new NullPointerException();
    }

    public World2ListModelAdapter(ReadOnlyWorld world, KEY key,
        FreerailsPrincipal p) {
        this.k = key;
        this.w = world;
        skey = null;
        principal = p;

        if (null == key)
            throw new NullPointerException();

        if (null == p)
            throw new NullPointerException();

        if (null == w)
            throw new NullPointerException();
    }

    public int getSize() {
        if (null == skey) {
            return w.size(k, principal);
        } else {
            return w.size(skey);
        }
    }

    public Object getElementAt(int i) {
        if (null == skey) {
            return w.get(k, i, principal);
        } else {
            return w.get(skey, i);
        }
    }

    public void addListDataListener(ListDataListener arg0) {
        // TODO Auto-generated method stub
    }

    public void removeListDataListener(ListDataListener arg0) {
        // TODO Auto-generated method stub
    }
}