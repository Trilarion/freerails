/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.client.renderer.map.overview;

import freerails.client.renderer.map.MapRenderer;
import freerails.io.ARGBColor;
import freerails.model.economy.Money;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.World;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Tests the overview map renderer, which needs no display.
 */
public class OverviewMapRendererTest extends TestCase {

    private static final int CLEAR = 0;
    private static final int OCEAN = 1;
    private static final int CLEAR_COLOR = 0xff00b400;
    private static final int OCEAN_COLOR = 0xff0000b4;

    private World world;
    private MapRenderer renderer;

    /**
     * Creates a 20 x 10 world of clear terrain with one ocean tile, drawn at one pixel per tile.
     */
    @Override
    protected void setUp() {
        World.Builder builder = WorldGenerator.minimalWorldBuilder(new IntPoint2D(20, 10));
        SortedSet<Terrain> terrainTypes = new TreeSet<>();
        terrainTypes.add(new Terrain(CLEAR, "Clear", TerrainCategory.COUNTRY,
                Money.ZERO, Money.ZERO, List.of(), List.of(), List.of()));
        terrainTypes.add(new Terrain(OCEAN, "Ocean", TerrainCategory.OCEAN,
                Money.ZERO, Money.ZERO, List.of(), List.of(), List.of()));
        builder.setTerrainTypes(terrainTypes);
        world = builder.build();
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 10; y++) {
                world.setTile(new IntPoint2D(x, y), new TerrainTile(CLEAR));
            }
        }
        world.setTile(new IntPoint2D(3, 4), new TerrainTile(OCEAN));

        renderer = OverviewMapRenderer.getInstance(world, new Dimension(20, 10),
                Map.of(CLEAR, new ARGBColor(CLEAR_COLOR), OCEAN, new ARGBColor(OCEAN_COLOR)));
    }

    /**
     * Tests that each tile is drawn in its terrain color.
     */
    public void testPaintsTerrainColors() {
        assertEquals(new IntPoint2D(20, 10), renderer.getMapSizeInPixels());

        BufferedImage image = paint();

        assertEquals(CLEAR_COLOR, image.getRGB(0, 0));
        assertEquals(OCEAN_COLOR, image.getRGB(3, 4));
        assertEquals(CLEAR_COLOR, image.getRGB(19, 9));
    }

    /**
     * Tests that a refreshed tile is drawn in its new terrain color.
     */
    public void testRefreshTileRepaintsChangedTile() {
        paint();
        world.setTile(new IntPoint2D(5, 6), new TerrainTile(OCEAN));
        renderer.refreshTile(new IntPoint2D(5, 6));

        BufferedImage image = paint();

        assertEquals(OCEAN_COLOR, image.getRGB(5, 6));
        assertEquals(OCEAN_COLOR, image.getRGB(3, 4));
    }

    /**
     * Paints the overview into a new image the way the map view does.
     *
     * @return the painted image
     */
    private BufferedImage paint() {
        IntPoint2D size = renderer.getMapSizeInPixels();
        BufferedImage image = new BufferedImage(size.x(), size.y(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            renderer.paintBackgroundRect(g, new Rectangle(0, 0, size.x(), size.y()));
        } finally {
            g.dispose();
        }
        return image;
    }
}
