/*
 * TypeID.java
 *
 * Created on 22 August 2003, 20:20
 */
package jfreerails.world.top;

/**
 * This class stores an SKEY and an item index.
 * 
 * @author Luke Lindsay
 */
public class TypeID {
    private final int id;

    private final SKEY key;

    public TypeID(int id, SKEY key) {
        this.id = id;
        this.key = key;
    }

    public SKEY getKey() {
        return key;
    }

    public int getID() {
        return id;
    }
}