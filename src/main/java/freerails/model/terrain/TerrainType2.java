package freerails.model.terrain;

import freerails.model.Identifiable;

/**
 *
 */
public class TerrainType2 extends Identifiable {

    private final String name;
    private final TerrainCategory category;

    public TerrainType2(int id, String name, TerrainCategory category) {
        super(id);
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public TerrainCategory getCategory() {
        return category;
    }
}
