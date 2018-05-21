package freerails.nove;

import freerails.model.world.World;

public interface Nove {

    public Status applicable();

    public void apply(World world);
}
