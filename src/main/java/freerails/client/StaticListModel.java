package freerails.client;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @param <E>
 */
public class StaticListModel<E> implements ListModel<E> {

    private List<E> list;

    public StaticListModel(@NotNull List<E> list) {
        this.list = list;
    }

    public StaticListModel(@NotNull Collection<? extends E> c) {
        list = new ArrayList<>(c);
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
