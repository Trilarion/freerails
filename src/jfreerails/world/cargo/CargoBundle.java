package jfreerails.world.cargo;

import java.util.Iterator;
import jfreerails.world.common.FreerailsSerializable;


/** This interface defines a bundle of cargo made up of
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
public interface CargoBundle extends FreerailsSerializable {
    int getAmount(int cargoType);

    int getAmount(CargoBatch cb);

    void setAmount(CargoBatch cb, int amount);

    /** Adds the specified amount of the specified CargoBatch to the
     * amount already present in the Bundle.
     */
    void addCargo(CargoBatch cb, int amount);

    boolean contains(CargoBatch cb);

    Iterator cargoBatchIterator();

    CargoBundle getCopy();
}