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

import java.util.*;


/** An abstract class providing basic methods for collections implementing a type-specific interface.
 */

public abstract class IntAbstractCollection  implements IntCollection  {



	 public int [] toArray(int  a[]) {
		  return toIntArray (a);
	 }


    public int [] toIntArray () {
		  return toIntArray (null);
	 }
	 

    public int [] toIntArray ( int  a[] ) {
		  final int  result[];
		  final IntIterator  i;
		  int j, n;

		  if (a == null || a.length < size()) result = new int [size()];
		  else result = a;

		  j = 0;
		  i = (IntIterator )iterator();
		  n = size();

		  while(n-- != 0) result[j++] = i. nextInt ();

		  return result;
    }



    public Object[] toArray() {
		  return toArray((Object[])null);
	 }

    public Object[] toArray( Object a[] ) {
		  final Object result[];
		  final Iterator i;
		  int j, n;

		  if (a == null || a.length < size()) result = new Object[size()];
		  else result = a;

		  j = 0;
		  i = iterator();
		  n = size();

		  while(n-- != 0) result[j++] = i.next();

		  return result;
    }

	 /** Adds all elements of the given collection to this collection.
	  * If the collection is an instance of this class, it uses the faster iterators.
	  *
	  * @param c a collection.
     * @return <code>true</code> if this collection changed as a result of the call.
	  */

	 public boolean addAll(Collection c) {
		  boolean retVal = false;
		  final Iterator i = c.iterator();
		  int n = c.size();

		  if (i instanceof IntIterator ) {
				final IntIterator  j = (IntIterator )i;
				while(n-- != 0) if (add(j. nextInt ())) retVal = true;
		  }
		  else {
				while(n-- != 0) if (add(i.next())) retVal = true;
		  }
		  return retVal;
	 }



	 public boolean add(Object o) {
		  throw new UnsupportedOperationException();
	 }

	 public boolean remove(Object o) {
		  throw new UnsupportedOperationException();
	 }


	 public boolean add(int  k) {
		  throw new UnsupportedOperationException();
	 }
	 
	 public boolean remove(int  k) {
		  throw new UnsupportedOperationException();
	 }


	 /** Checks whether this collection contains all elements from the given collection.
	  * If the collection is an instance of this class, it uses the faster iterators.
	  *
	  * @param c a collection.
     * @return <code>true</code> if this collection contains all elements of the argument.
	  */

	 public boolean containsAll(Collection c) {
		  final Iterator i = c.iterator();
		  int n = c.size();

		  if (i instanceof IntIterator ) {
				final IntIterator  j = (IntIterator )i;
				while(n-- != 0) if (!contains(j. nextInt ())) return false;
		  }
		  else {
				while(n-- != 0) if (!contains(i.next())) return false;
		  }

		  return true;
	 }


	 /** Retains in this collection only elements from the given collection.
	  * If the collection is an instance of this class, it uses the faster iterators.
	  *
	  * @param c a collection.
     * @return <code>true</code> if this collection changed as a result of the call.
	  */

	 public boolean retainAll(Collection c) {
		  boolean retVal = false;
		  int n = size();

		  if (c instanceof IntCollection ) {
				final IntCollection  d = (IntCollection )c;
				final IntIterator  i = (IntIterator )iterator();

				while(n-- != 0) {
					 if (!d.contains(i. nextInt ())) {
						  i.remove();
						  retVal = true;
					 }
				}
		  }
		  else {
				final Iterator i = iterator();
				while(n-- != 0) {
					 if (!c.contains(i.next())) {
						  i.remove();
						  retVal = true;
					 }
				}
		  }
		  return retVal;
	 }

	 /** Remove from this collection all elements in the given collection.
	  * If the collection is an instance of this class, it uses the faster iterators.
	  *
	  * @param c a collection.
     * @return <code>true</code> if this collection changed as a result of the call.
	  */

	 public boolean removeAll(Collection c) {
		  boolean retVal = false;
		  int n = size();

		  if (c instanceof IntCollection ) {
				final IntCollection  d = (IntCollection )c;
				final IntIterator  i = (IntIterator )iterator();

				while(n-- != 0) {
					 if (d.contains(i. nextInt ())) {
						  i.remove();
						  retVal = true;
					 }
				}
		  }
		  else {
				final Iterator i = iterator();
				while(n-- != 0) {
					 if (c.contains(i.next())) {
						  i.remove();
						  retVal = true;
					 }
				}
		  }
		  return retVal;
	 }

	 public boolean isEmpty() {
		  return size() != 0;
	 }

	 public String toString() {
		  final StringBuffer s = new StringBuffer();
		  final IntIterator  i = (IntIterator )iterator();
		  int n = size();
		  int  k;
		  boolean first = true;

		  s.append("{");

		  while(n-- != 0) {
				if (first) first = false;
				else s.append(", ");
				k = i. nextInt ();

				s.append(String.valueOf(k));
		  }

		  s.append("}");
		  return s.toString();
	 }
	 
}

// Local Variables:
// mode: java
// End:


