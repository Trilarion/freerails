






















































/*		 
 * fastUtil 1.3: Fast & compact specialized hash-based utility classes for Java
 *
 * Copyright (C) 2002 Sebastiano Vigna 
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package it.unimi.dsi.fastUtil;

import java.util.Map;

/** An object that maps fixed type keys to values; provides some additional methods
 * that use polymorphism to reduce type juggling.
 *
 * @see Map
 */

public interface Int2IntMap  extends Map {



    /**
     * @see Map#containsKey(Object)
     */

    boolean containsKey(int  key);






    /**
     * @see Map#containsValue(Object)
	  */

    boolean containsValue(int  value);






    /** Returns the value to which the given key is mapped.
	  *
	  * @param key the key.
	  * @return the corresponding value, or the default return value if no value was present for the given key.
     * @see Map#get(Object)
     */

    int  get(int  key);






	 /** Adds a pair to the map.
	  *
	  * @param key the key.
	  * @param value the value.
	  * @return the old value, or the default return value if no value was present for the given key.
     * @see Map#put(Object,Object)
     */

    int  put(int  key, int  value);







    /** Removes the mapping with the given key.
	  * @param key
	  * @return the old value, or the default return value if no value was present for the given key.
     * @see Map#remove(Object)
     */
    int  remove(int  key);






    /**
     * Sets the default return value. This value is returned 
	  * by <code>get()</code>, <code>put()</code> and <code>remove()</code> to denote
	  * that the map does not contain the specified key.
     *
     * @param rv the new default return value.
	  * @see #getDefRetValue()
     */

	 void setDefRetValue( int  rv );

	 
    /**
     * Gets the default return value.
     *
     * @return the current default return value.
     */

	 int  getDefRetValue();



	 
    /** An object containing a key and a value; provides some additional methods
	  * to access its content reducing type juggling.
	  *
     * @see java.util.Map.Entry
     */

    interface Entry extends java.util.Map.Entry {
		  

		  /**
			* @see java.util.Map.Entry#setValue(Object)
			*/
		  int  setValue(int  value);

		  /**
			* @see java.util.Map.Entry#getValue()
			*/
		  int  getIntValue ();



		  /**
			* @see java.util.Map.Entry#getKey()
			*/
		  int  getIntKey ();


    }

}

// Local Variables:
// mode: java
// End:


