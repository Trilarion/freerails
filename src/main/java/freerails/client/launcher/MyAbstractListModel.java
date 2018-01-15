package freerails.client.launcher;

import javax.swing.*;

class MyAbstractListModel extends AbstractListModel {

    private static final long serialVersionUID = -7077093078891444168L;
    private final String[] strings = {"No players are logged on!"};

    public int getSize() {
        return strings.length;
    }

    public Object getElementAt(int index) {
        return strings[index];
    }
}
