/*
 * TypeID.java
 *
 * Created on 22 August 2003, 20:20
 */
package jfreerails.world.top;


/**
 *  This class stores a list KEY and an item index.
 * @author  Luke Lindsay
 */
public class TypeID {
    private final int id;
    private final KEY key;

    /** Creates a new instance of TypeID */
    public TypeID(int id, KEY key) {
        this.id = id;
        this.key = key;
    }

    public KEY getKey() {
        return key;
    }

    public int getID() {
        return id;
    }
}