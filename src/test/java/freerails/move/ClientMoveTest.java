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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.move;

import freerails.model.MapFixtureFactory2;
import freerails.model.building.EngineShop;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.operations.train.TrainTemplate;
import freerails.model.player.Player;
import freerails.model.simulation.Speed;
import freerails.model.simulation.Time;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;
import freerails.model.world.World;
import freerails.move.generator.AddStationMoveGenerator;
import freerails.move.generator.ClientMoveGenerator;
import freerails.move.mapupdatemove.ChangeTileMove;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.move.mapupdatemove.RemoveStationCompositeMove;
import freerails.util.IntPoint2D;
import junit.framework.TestCase;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Tests that client-sendable moves are rebound to the sending player.
 */
public class ClientMoveTest extends TestCase {

    private static final Player CLAIMED = new Player(1, "claimed");
    private static final Player SENDER = new Player(0, "sender");
    private static final int PIECE_OWNER = 1;

    private TrackType stationType;
    private TrackType trackType;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        World world = MapFixtureFactory2.getCopy();
        trackType = world.getTrackType(0);
        stationType = world.getTrackTypes().stream().filter(TrackType::isStation).findFirst().orElseThrow();
    }

    /**
     * Tests that every player reference of each client-sendable move, including
     * those of contained moves, is replaced by the sending player.
     */
    public void testWithPlayerReplacesEveryPlayerReference() {
        List<Function<Player, ClientMove>> moves = List.of(
                player -> new AddTransactionMove(player,
                        new Transaction(TransactionCategory.MISC_INCOME, new Money(5), Time.ZERO), true),
                player -> new BuildEngineShopMove(player, new EngineShop(1, new IntPoint2D(1, 1))),
                player -> new ChangeProductionAtEngineShopMove(List.of(new TrainTemplate(0, List.of(0))), 1, player),
                player -> new UpdateTrainMove(player, 3, null, List.of(1), null),
                player -> new RemoveStationMove(player, 2),
                this::stationRemoval,
                this::trackBuild,
                player -> new ChangeTrackPieceMove(player, null,
                        TrackPiece.create(TileTransition.EAST, trackType, PIECE_OWNER), new IntPoint2D(5, 5)),
                player -> new CompositeMove(List.of(trackBuild(player),
                        new AddTransactionMove(player, new Transaction(TransactionCategory.TRACK, new Money(-5), Time.ZERO)))),
                player -> new CompostMove(List.of(new RemoveStationMove(player, 2), new UpdateTrainMove(player, 3, null, List.of(1), null))));

        for (Function<Player, ClientMove> move : moves) {
            assertEquals(move.apply(SENDER), move.apply(CLAIMED).withPlayer(SENDER));
        }
    }

    /**
     * Tests that moves without a player reference are returned unchanged.
     */
    public void testMovesWithoutPlayerAreUnchanged() {
        List<ClientMove> moves = List.of(
                new ChangeGameSpeedMove(Speed.FAST),
                new ChangeTileMove(new TerrainTile(4), new IntPoint2D(2, 3)));

        for (ClientMove move : moves) {
            assertEquals(move, move.withPlayer(SENDER));
        }
    }

    /**
     * Tests that a client move generator is rebound to the sending player.
     */
    public void testGeneratorWithPlayer() {
        IntPoint2D location = new IntPoint2D(2, 2);
        assertEquals(new AddStationMoveGenerator(location, 1, SENDER),
                new AddStationMoveGenerator(location, 1, CLAIMED).withPlayer(SENDER));
    }

    /**
     * Tests that moves a client may not send are rejected, also inside a composite.
     */
    public void testMovesOnlyTheServerMayMakeAreRejected() {
        Move serverOnly = new AddPlayerMove(new Player(2, "intruder"));

        assertThrows(() -> ClientMove.withPlayer(serverOnly, SENDER));
        assertThrows(() -> new CompositeMove(List.of(new RemoveStationMove(CLAIMED, 2), serverOnly)).withPlayer(SENDER));
        assertThrows(() -> new CompostMove(List.of(new RemoveStationMove(CLAIMED, 2), serverOnly)).withPlayer(SENDER));
    }

    /**
     * Tests that every client-sendable class rebinds its own player references:
     * each concrete class must declare {@code withPlayer} itself, so a new or
     * derived class cannot silently inherit an implementation that misses its
     * player references.
     * @throws Exception if the compiled classes cannot be scanned
     */
    public void testEveryClientSendableClassDeclaresWithPlayer() throws Exception {
        List<Class<?>> clientClasses = clientSendableClasses();
        assertTrue(clientClasses.contains(AddTransactionMove.class));
        assertTrue(clientClasses.contains(AddStationMoveGenerator.class));

        for (Class<?> type : clientClasses) {
            try {
                type.getDeclaredMethod("withPlayer", Player.class);
            } catch (NoSuchMethodException e) {
                fail(type.getName() + " may be sent by a client but does not declare withPlayer(Player).");
            }
        }
    }

    private ChangeTrackPieceCompositeMove trackBuild(Player player) {
        IntPoint2D from = new IntPoint2D(5, 5);
        TileTransition direction = TileTransition.EAST;
        // Track piece owners describe the world, not the acting player, so they are not rebound.
        ChangeTrackPieceMove a = new ChangeTrackPieceMove(player, null,
                TrackPiece.create(direction, trackType, PIECE_OWNER), from);
        ChangeTrackPieceMove b = new ChangeTrackPieceMove(player, null,
                TrackPiece.create(direction.getOpposite(), trackType, PIECE_OWNER), direction.createRelocatedPoint(from));
        return new ChangeTrackPieceCompositeMove(a, b, player);
    }

    private RemoveStationCompositeMove stationRemoval(Player player) {
        ChangeTrackPieceMove trackChange = new ChangeTrackPieceMove(player,
                TrackPiece.create(TileTransition.EAST, stationType, PIECE_OWNER), null, new IntPoint2D(5, 5));
        return new RemoveStationCompositeMove(trackChange, new RemoveStationMove(player, 2),
                List.of(new UpdateTrainMove(player, 3, null, List.of(1), null)));
    }

    private static void assertThrows(Runnable runnable) {
        try {
            runnable.run();
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * Returns the concrete main classes that a client may send.
     */
    private static List<Class<?>> clientSendableClasses() throws Exception {
        Path classesDirectory = Path.of(Move.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<Class<?>> classes = new ArrayList<>();
        try (Stream<Path> files = Files.walk(classesDirectory)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                String relative = classesDirectory.relativize(file).toString();
                String className = relative.substring(0, relative.length() - ".class".length())
                        .replace(File.separatorChar, '.');
                if (className.endsWith("package-info") || className.endsWith("module-info")) {
                    continue;
                }
                Class<?> type;
                try {
                    type = Class.forName(className, false, Move.class.getClassLoader());
                } catch (ClassNotFoundException | LinkageError e) {
                    continue;
                }
                boolean clientSendable = ClientMove.class.isAssignableFrom(type)
                        || ClientMoveGenerator.class.isAssignableFrom(type);
                if (clientSendable && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
                    classes.add(type);
                }
            }
        }
        return classes;
    }
}
