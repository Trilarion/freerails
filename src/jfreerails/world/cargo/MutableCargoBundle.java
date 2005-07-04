/*
 * Created on 24-May-2003
 *
 */
package jfreerails.world.cargo;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This CargoBundle implementation uses a <code>java.util.HashMap</code> to
 * map quantities to cargo batches.
 * 
 * @author Luke
 * 
 */
public class MutableCargoBundle {
	private final HashMap<CargoBatch, Integer> hashMap;

	private int updateID = 0;

	public int hashCode() {
		return hashMap.size();
	}

	public String toString() {
		return toImmutableCargoBundle().toString();
	}

	public MutableCargoBundle() {
		hashMap = new HashMap<CargoBatch, Integer>();
	}

	public MutableCargoBundle(ImmutableCargoBundle imcb) {
		this();

		Iterator<CargoBatch> it = imcb.cargoBatchIterator();

		while (it.hasNext()) {
			CargoBatch cb = it.next();
			addCargo(cb, imcb.getAmount(cb));
		}
	}

	private HashMap getHashMap() {
		return hashMap;
	}

	public int getAmount(int cargoType) {
		Iterator<CargoBatch> it = cargoBatchIterator();
		int amount = 0;

		while (it.hasNext()) {
			CargoBatch cb = it.next();

			if (cb.getCargoType() == cargoType) {
				amount += getAmount(cb);
			}
		}

		return amount;
	}

	public int getAmount(CargoBatch cb) {
		if (contains(cb)) {
			Integer i = hashMap.get(cb);

			return i.intValue();
		}
		return 0;
	}

	public void setAmount(CargoBatch cb, int amount) {
		if (0 == amount) {
			hashMap.remove(cb);
		} else {
			hashMap.put(cb, new Integer(amount));
		}

		updateID++;
	}

	public boolean contains(CargoBatch cb) {
		return hashMap.containsKey(cb);
	}

	/**
	 * Note, calling hasNext() or next() on the returned iterator throws a
	 * ConcurrentModificationException if this CargoBundle has changed since the
	 * iterator was aquired.
	 */
	public Iterator<CargoBatch> cargoBatchIterator() {
		final Iterator<CargoBatch> it = hashMap.keySet().iterator();

		/*
		 * A ConcurrentModificationException used to get thrown when the amount
		 * of cargo was set to 0, since this resulted in the key being removed
		 * from the hashmap. The iterator below throws a
		 * ConcurrentModificationException whenever this CargoBundle has been
		 * changed since the iterator was aquired. This should mean that if the
		 * cargo bundle gets changed while the iterator is in use, you will know
		 * about it straight away.
		 */
		return new Iterator<CargoBatch>() {
			final int updateIDAtCreation = updateID;

			public void remove() {
				throw new UnsupportedOperationException(
						"Use CargoBundle.setAmount(CargoBatch cb, 0)");
			}

			public boolean hasNext() {
				if (updateIDAtCreation != updateID) {
					throw new ConcurrentModificationException();
				}

				return it.hasNext();
			}

			public CargoBatch next() {
				if (updateIDAtCreation != updateID) {
					throw new ConcurrentModificationException();
				}

				return it.next();
			}
		};
	}

	public boolean equals(Object o) {
		if (o instanceof MutableCargoBundle) {
			MutableCargoBundle test = (MutableCargoBundle) o;

			return hashMap.equals(test.getHashMap());
		}
		return false;
	}

	public void addCargo(CargoBatch cb, int amount) {
		int amountAlready = this.getAmount(cb);
		this.setAmount(cb, amount + amountAlready);
		updateID++;
	}

	public ImmutableCargoBundle toImmutableCargoBundle() {
		int size = hashMap.keySet().size();
		CargoBatch[] batches = new CargoBatch[size];
		int[] amounts = new int[size];
		Iterator<CargoBatch> it = cargoBatchIterator();
		int i = 0;

		while (it.hasNext()) {
			CargoBatch batch = it.next();
			int amount = getAmount(batch);
			batches[i] = batch;
			amounts[i] = amount;
			i++;
		}

		return new ImmutableCargoBundle(batches, amounts);
	}
}