package freerails.model.world;

import freerails.model.train.Engine;
import freerails.util.Utils;

import java.util.SortedSet;

/**
 *
 */
public class WorldBuilder {

    private SortedSet<Engine> engines;

    public WorldBuilder setEngines(SortedSet<Engine> engines) {
        this.engines = engines;
        return this;
    }

    public World create() {
        // create new instance
        // return new World(engines);
        return new World();
    }
}
