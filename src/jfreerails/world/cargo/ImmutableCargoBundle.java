package jfreerails.world.cargo;

import java.util.Iterator;
import jfreerails.world.common.FreerailsSerializable;


/** This class represents a bundle of cargo made up of
 * quantities of cargo from different {@link CargoBatch}s.
 * <p>For example:</p>
 * <table width="75%" border="0">
  <tr>
    <td><strong>Cargo Batch</strong></td>
    <td><strong>Quantity</strong></td>
  </tr>
  <tr>
    <td>passengers from (1, 5) created at 01:00</td>
    <td>2</td>
  </tr>
  <tr>
    <td>passengers from (1, 5) created at 01:25</td>
    <td>1</td>
  </tr>
  <tr>
    <td>coal from (4,10) created at 02:50</td>
    <td>8</td>
  </tr>
  <tr>
    <td>mail from (6, 10) created at 04:45</td>
    <td>10</td>
  </tr>
</table>

 * @author Luke
 *
 */
public class ImmutableCargoBundle implements FreerailsSerializable {
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CargoBundle {\n");

        for (int i = 0; i < m_batches.length; i++) {
            sb.append(m_amounts[i]);
            sb.append(" units of cargo type ");
            sb.append(m_batches[i]);
            sb.append("\n");
        }

        sb.append("}");

        return sb.toString();
    }

    public boolean equals(Object arg0) {
        if (null == arg0) {
            return false;
        }

        if (!(arg0 instanceof ImmutableCargoBundle)) {
            return false;
        }

        ImmutableCargoBundle test = (ImmutableCargoBundle)arg0;

        /* Note, the two bundles are equal if they contain the same cargo but ordered differently.*/
        Iterator it = cargoBatchIterator();

        while (it.hasNext()) {
            CargoBatch batch = (CargoBatch)it.next();

            if (getAmount(batch) != test.getAmount(batch)) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        return m_amounts.length;
    }

    public static final ImmutableCargoBundle EMPTY_BUNDLE = new ImmutableCargoBundle(new CargoBatch[0],
            new int[0]);
    private final CargoBatch[] m_batches;
    private final int[] m_amounts;

    public ImmutableCargoBundle(CargoBatch[] batches, int[] amounts) {
        if (batches.length != amounts.length) {
            throw new IllegalArgumentException();
        }

        m_batches = batches;
        m_amounts = amounts;
    }

    public int getAmount(int cargoType) {
        int amount = 0;

        for (int i = 0; i < m_batches.length; i++) {
            if (m_batches[i].getCargoType() == cargoType) {
                amount += m_amounts[i];
            }
        }

        return amount;
    }

    public int getAmount(CargoBatch cb) {
        int amount = 0;

        for (int i = 0; i < m_batches.length; i++) {
            if (m_batches[i].equals(cb)) {
                amount += m_amounts[i];
            }
        }

        return amount;
    }

    public boolean contains(CargoBatch cb) {
        for (int i = 0; i < m_batches.length; i++) {
            if (m_batches[i].equals(cb)) {
                return true;
            }
        }

        return false;
    }

    public Iterator cargoBatchIterator() {
        return new Iterator() {
                int index = 0;

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public boolean hasNext() {
                    return index < m_batches.length;
                }

                public Object next() {
                    Object o = m_batches[index];
                    index++;

                    return o;
                }
            };
    }
}