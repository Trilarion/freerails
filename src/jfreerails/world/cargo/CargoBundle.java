package jfreerails.world.cargo;

import java.util.Vector;
import java.util.Iterator;
import jfreerails.type.CargoType;
public interface CargoBundle {
    Vector getCargoBatch();

    Iterator getCargoBatchIterator();

    CargoType getCargoType();

    int getTotalAmount();

    int getTotalWeight();
}
