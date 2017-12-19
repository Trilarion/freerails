/*
 * TypeID.java
 *
 * Created on 22 August 2003, 20:20
 */
package freerails.world.top;

/**
 * This class stores an SKEY and an item index.
 *
 */
public class TypeID {
    private final int id;

    private final SKEY key;

    /**
     *
     * @param id
     * @param key
     */
    public TypeID(int id, SKEY key) {
        this.id = id;
        this.key = key;
    }

    /**
     *
     * @return
     */
    public SKEY getKey() {
        return key;
    }

    /**
     *
     * @return
     */
    public int getID() {
        return id;
    }
}