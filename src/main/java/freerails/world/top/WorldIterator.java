/*
 * Created on 29-Mar-2003
 *
 */
package freerails.world.top;

import freerails.world.common.FreerailsSerializable;

import java.util.NoSuchElementException;

/**
 * This interface lets the caller access the results of a search in the
 * gameworld. It is similar in concept to <code>java.sql.ResultSet</code>.
 *
 * @author Luke
 */
public interface WorldIterator {

    /**
     *
     */
    int BEFORE_FIRST = -1;

    /**
     * Moves the cursor down one row from its current position.
     * @return 
     */
    boolean next();

    /**
     * Moves the cursor up one row from its current position.
     * @return 
     */
    boolean previous();

    /**
     * Moves the cursor to before the first element and updates any cached
     * values.
     */
    void reset();

    /**
     * Returns the element the curor is pointing to.
     * @return 
     */
    FreerailsSerializable getElement();

    /**
     * Returns the index of the element the cursor is pointing to. The value
     * returned is index you would need to use in
     * <code>World.get(KEY key, int index)</code> to retrieve the same element
     * as is returned by <code>getElement()</code>
     * @return 
     */
    int getIndex();

    /**
     * Returns the number of the row where the cursor is (the first row is 0).
     * @return 
     */
    int getRowID();

    /**
     * Returns the number of rows.
     * @return 
     */
    int size();

    /**
     * Moves the cursor to the specified index.
     *
     * @param i
     * @throws NoSuchElementException if index out of range
     */
    void gotoIndex(int i);

    /**
     * Moves the cursor to the specified index.
     * @param row
     */
    void gotoRow(int row);

    /**
     * Returns the number of the row where the cursor is (the first row is 1).
     * @return 
     */
    int getNaturalNumber();
}