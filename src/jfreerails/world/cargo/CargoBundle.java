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
    /**
     * @param cargoType index into the CARGO_TYPES table
     * @return amount of cargo of the specified type in tonnes
     */
    int getAmount(int cargoType);

    int getAmount(CargoBatch cb);

    /**
     * @param amount Amount of cargo in tonnes
     */
    void setAmount(CargoBatch cb, int amount);

    /**
     * Adds the specified amount of the specified CargoBatch to the
     * amount already present in the Bundle.
     * @param amount Amount of cargo in tonnes.
     */
    void addCargo(CargoBatch cb, int amount);

    boolean contains(CargoBatch cb);

    /**
     * @return an iterator over a set of Map.Entry. The keys are CargoBundle
     * instances, the values are Integer amounts
     */
    Iterator cargoBatchIterator();

    CargoBundle getCopy();
}
