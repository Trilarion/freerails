package freerails.world.cargo;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImInts;
import freerails.world.common.ImList;

import java.util.Iterator;
import java.util.SortedMap;

/**
 * This class represents a bundle of cargo made up of quantities of cargo from
 * different {@link CargoBatch}s.
 * <p>
 * For example:
 * </p>
 * <table width="75%" border="0">
 * <tr>
 * <td><strong>Cargo Batch</strong></td>
 * <td><strong>Quantity</strong></td>
 * </tr>
 * <tr>
 * <td>passengers from (1, 5) created at 01:00</td>
 * <td>2</td>
 * </tr>
 * <tr>
 * <td>passengers from (1, 5) created at 01:25</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>coal from (4,10) created at 02:50</td>
 * <td>8</td>
 * </tr>
 * <tr>
 * <td>mail from (6, 10) created at 04:45</td>
 * <td>10</td>
 * </tr>
 * </table>
 *
 * @author Luke
 */
public class ImmutableCargoBundle implements CargoBundle, FreerailsSerializable {
    public static final ImmutableCargoBundle EMPTY_BUNDLE = new ImmutableCargoBundle();

    private static final long serialVersionUID = 3257566187666814009L;

    public static boolean equals(CargoBundle a, CargoBundle b) {
        Iterator<CargoBatch> it = a.cargoBatchIterator();
        if (a.size() != b.size())
            return false;
        while (it.hasNext()) {
            CargoBatch batch = it.next();

            if (a.getAmount(batch) != b.getAmount(batch)) {
                return false;
            }
        }
        return true;

    }

    private final ImInts amounts;

    private final ImList<CargoBatch> batches;

    private ImmutableCargoBundle() {
        batches = new ImList<CargoBatch>();
        amounts = new ImInts();
    }

    public ImmutableCargoBundle(SortedMap<CargoBatch, Integer> sortedMap) {
        int size = sortedMap.size();
        int[] amountsArray = new int[size];
        CargoBatch[] batchesArray = new CargoBatch[size];
        int i = 0;
        for (CargoBatch batch : sortedMap.keySet()) {
            batchesArray[i] = batch;
            amountsArray[i] = sortedMap.get(batch);
            i++;
        }

        batches = new ImList<CargoBatch>(batchesArray);
        amounts = new ImInts(amountsArray);
    }

    public Iterator<CargoBatch> cargoBatchIterator() {
        return new Iterator<CargoBatch>() {
            int index = 0;

            public boolean hasNext() {
                return index < batches.size();
            }

            public CargoBatch next() {
                CargoBatch o = batches.get(index);
                index++;

                return o;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean contains(CargoBatch cb) {
        for (int i = 0; i < batches.size(); i++) {
            if (batches.get(i).equals(cb)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object arg0) {
        if (null == arg0) {
            return false;
        }

        if (!(arg0 instanceof CargoBundle)) {
            return false;
        }

        return equals(this, (CargoBundle) arg0);
    }

    public int getAmount(CargoBatch cb) {
        int amount = 0;

        for (int i = 0; i < batches.size(); i++) {
            if (batches.get(i).equals(cb)) {
                amount += amounts.get(i);
            }
        }

        return amount;
    }

    // 666 use dynamic cache (growing arraylist)->breaks save games
    public int getAmount(int cargoType) {
        int amount = 0;
        for (int i = 0; i < batches.size(); i++) {
            if (batches.get(i).getCargoType() == cargoType) {
                amount += amounts.get(i);
            }
        }

        return amount;
    }

    @Override
    public int hashCode() {
        return amounts.size();
    }

    public int size() {
        return batches.size();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CargoBundle {\n");

        for (int i = 0; i < batches.size(); i++) {
            sb.append(amounts.get(i));
            sb.append(" units of cargo type ");
            sb.append(batches.get(i));
            sb.append("\n");
        }

        sb.append("}");

        return sb.toString();
    }
}