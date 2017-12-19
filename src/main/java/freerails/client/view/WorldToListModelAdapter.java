/*
 * Created on 23-Mar-2003
 *
 */
package freerails.client.view;

import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.top.NonNullElements;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;

import javax.swing.*;
import javax.swing.event.ListDataListener;

/**
 * Converts the interface of a list on the world object to a ListModel interface
 * that can be used by JLists. Currently, change notification is <b>not</b>
 * implemented (null elements are skipped).
 *
 */
public class WorldToListModelAdapter implements ListModel {

    private final ReadOnlyWorld w;

    private final NonNullElements elements;

    /**
     *
     * @param world
     * @param key
     */
    public WorldToListModelAdapter(ReadOnlyWorld world, SKEY key) {

        this.w = world;

        if (null == key)
            throw new NullPointerException();

        if (null == w)
            throw new NullPointerException();

        elements = new NonNullElements(key, world);
    }

    /**
     *
     * @param world
     * @param key
     * @param p
     */
    public WorldToListModelAdapter(ReadOnlyWorld world, KEY key,
                                   FreerailsPrincipal p) {

        this.w = world;

        if (null == key)
            throw new NullPointerException();

        if (null == p)
            throw new NullPointerException();

        if (null == w)
            throw new NullPointerException();

        // Check that the principal exists.
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