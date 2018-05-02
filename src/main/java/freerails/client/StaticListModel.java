package freerails.client;

import freerails.util.ImmutableList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.Collection;

/**
 *
 * @param <E>
 */
public class StaticListModel<E> implements ListModel<E> {

    private ImmutableList<E> list;

    public StaticListModel(@NotNull ImmutableList<E> list) {
        this.list = list;
    }

    public StaticListModel(@NotNull Collection<? extends E> c) {
        list = new ImmutableList<E>(c);
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public E getElementAt(int index) {
        return list.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }
}
