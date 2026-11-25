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

import freerails.model.building.EngineShop;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.operations.train.TrainTemplate;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.player.Player;
import freerails.model.simulation.Speed;
import freerails.model.simulation.Time;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.move.generator.AddStationMoveGenerator;
import freerails.move.generator.BuildIndustryMoveGenerator;
import freerails.move.generator.BuildTrackMoveGenerator;
import freerails.move.generator.ClientMoveGenerator;
import freerails.move.generator.IssueBondMoveGenerator;
import freerails.move.generator.RemoveTrackMoveGenerator;
import freerails.move.generator.RepayBondMoveGenerator;
import freerails.move.generator.TradeSharesMoveGenerator;
import freerails.move.generator.UpgradeTrackMoveGenerator;
import freerails.move.mapupdatemove.ChangeTileMove;
import freerails.util.IntPoint2D;
import junit.framework.TestCase;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Tests what clients may send and that it is rebound to the sending player.
 */
public class ClientMoveTest extends TestCase {

    private static final Player CLAIMED = new Player(1, "claimed");
    private static final Player SENDER = new Player(0, "sender");
    private static final IntPoint2D LOCATION = new IntPoint2D(2, 2);

    /**
     * The complete set of types a client may send. Clients send what the player
     * wants to do; the server works out the effects and costs. Extending this set
     * is a deliberate decision: each type must check its own content.
     */
    private static final Set<Class<?>> CLIENT_SENDABLE = Set.of(
            BuildEngineShopMove.class,
            ChangeProductionAtEngineShopMove.class,
            ChangeTrainScheduleMove.class,
            ChangeGameSpeedMove.class,
            AddStationMoveGenerator.class,
            BuildTrackMoveGenerator.class,
            RemoveTrackMoveGenerator.class,
            UpgradeTrackMoveGenerator.class,
            BuildIndustryMoveGenerator.class,
            TradeSharesMoveGenerator.class,
            IssueBondMoveGenerator.class,
            RepayBondMoveGenerator.class);

    /**
     * Tests that exactly the intended types may be sent by clients.
     * @throws Exception if the compiled classes cannot be scanned
     */
    public void testClientSendableSetIsExactlyTheIntendedOne() throws Exception {
        assertEquals(CLIENT_SENDABLE, new HashSet<>(clientSendableClasses()));
    }

    /**
     * Tests that every client-sendable move is rebound to the sending player.
     */
    public void testMovesAreReboundToTheSender() {
        List<Function<Player, ClientMove>> moves = List.of(
                player -> new BuildEngineShopMove(player, new EngineShop(1, LOCATION)),
                player -> new ChangeProductionAtEngineShopMove(List.of(new TrainTemplate(0, List.of(0))), 1, player),
                player -> new ChangeTrainScheduleMove(player, 3, new ScheduleState()),
                player -> new ChangeGameSpeedMove(player, Speed.FAST));

        for (Function<Player, ClientMove> move : moves) {
            assertEquals(move.apply(SENDER), move.apply(CLAIMED).withPlayer(SENDER));
        }
    }

    /**
     * Tests that every client-sendable move generator is rebound to the sending player.
     */
    public void testGeneratorsAreReboundToTheSender() {
        List<Function<Player, ClientMoveGenerator>> generators = List.of(
                player -> new AddStationMoveGenerator(LOCATION, 1, player),
                player -> new BuildTrackMoveGenerator(LOCATION, TileTransition.EAST, 0, 0, player),
                player -> new RemoveTrackMoveGenerator(LOCATION, TileTransition.EAST, player),
                player -> new UpgradeTrackMoveGenerator(LOCATION, 1, player),
                player -> new BuildIndustryMoveGenerator(LOCATION, 3, player),
                player -> new TradeSharesMoveGenerator(1, 10_000, player),
                IssueBondMoveGenerator::new,
                RepayBondMoveGenerator::new);

        for (Function<Player, ClientMoveGenerator> generator : generators) {
            assertEquals(generator.apply(SENDER), generator.apply(CLAIMED).withPlayer(SENDER));
        }
    }

    /**
     * Tests that moves only the server makes are rejected, including the results
     * and costs clients used to send themselves.
     */
    public void testServerOnlyMovesAreRejected() {
        List<Move> serverOnly = List.of(
                new AddPlayerMove(new Player(2, "intruder")),
                new AddTransactionMove(SENDER, new Transaction(TransactionCategory.MISC_INCOME, new Money(5), Time.ZERO)),
                new ChangeTileMove(new TerrainTile(4), LOCATION),
                new UpdateTrainMove(SENDER, 3, null, List.of(1), null),
                new CompositeMove(List.of(new ChangeGameSpeedMove(SENDER, Speed.FAST))));

        for (Move move : serverOnly) {
            try {
                ClientMove.withPlayer(move, SENDER);
                fail(move.getClass().getSimpleName() + " must not be accepted from a client.");
            } catch (IllegalArgumentException expected) {
                // expected
            }
        }
    }

    /**
     * Tests that every client-sendable class rebinds its own player references:
     * each concrete class must declare {@code withPlayer} itself.
     * @throws Exception if the compiled classes cannot be scanned
     */
    public void testEveryClientSendableClassDeclaresWithPlayer() throws Exception {
        for (Class<?> type : clientSendableClasses()) {
            try {
                type.getDeclaredMethod("withPlayer", Player.class);
            } catch (NoSuchMethodException e) {
                fail(type.getName() + " may be sent by a client but does not declare withPlayer(Player).");
            }
        }
    }

    /**
     * Returns the concrete main classes that a client may send.
     */
    private static List<Class<?>> clientSendableClasses() throws Exception {
        Path classesDirectory = Path.of(Move.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<Class<?>> classes = new java.util.ArrayList<>();
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
