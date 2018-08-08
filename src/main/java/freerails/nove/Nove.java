package freerails.nove;

import freerails.model.world.World;

public interface Nove {

    Status applicable();

    void apply(World world);
}
