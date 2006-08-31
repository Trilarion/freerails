/*
 * Created on 12-Jul-2005
 *
 */
package jfreerails.world.common;

import jfreerails.util.Immutable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Immutable
public class ImHashSet<E extends FreerailsSerializable> implements
		FreerailsSerializable {

	private static final long serialVersionUID = -4098862905501171517L;

	private final HashSet<E> hashSet;

	public ImHashSet(HashSet<E> hashSet) {
		this.hashSet = new HashSet<E>(hashSet);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ImHashSet))
			return false;

		final ImHashSet imHashSet = (ImHashSet) o;

		if (!hashSet.equals(imHashSet.hashSet))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return hashSet.hashCode();
	}

	public ImHashSet(E... values) {
		this.hashSet = new HashSet<E>();
		for (E e : values) {
			hashSet.add(e);
		}
	}

	public ImHashSet(List<E> values) {
		this.hashSet = new HashSet<E>();
		for (E e : values) {
			hashSet.add(e);
		}
	}

	public boolean contains(E e) {
		return hashSet.contains(e);
	}

	public Iterator<E> iterator() {
		return new Iterator<E>() {
			Iterator<E> it = hashSet.iterator();

			public boolean hasNext() {
				return it.hasNext();
			}

			public E next() {
				return it.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();

			}

		};
	}

}
