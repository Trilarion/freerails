/*
 * TypeID.java
 *
 * Created on 22 August 2003, 20:20
 */
package jfreerails.world.top;


/**
 *  This class stores an SKEY and an item index.
 * @author  Luke Lindsay
 */
public class TypeID {
    private final int m_id;
    private final SKEY m_key;

    public TypeID(int id, SKEY key) {
        m_id = id;
        m_key = key;
    }

    public SKEY getKey() {
        return m_key;
    }

    public int getID() {
        return m_id;
    }
}