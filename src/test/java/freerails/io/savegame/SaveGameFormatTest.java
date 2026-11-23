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

package freerails.io.savegame;

import freerails.model.economy.Money;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.operations.cargo.CargoBatch;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.train.PathOnTiles;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.TrainState;
import freerails.model.operations.train.motion.TrainMotion;
import freerails.model.player.Player;
import freerails.model.simulation.Time;
import freerails.model.terrain.TileTransition;
import freerails.model.world.World;
import freerails.network.LogOnCredentials;
import freerails.server.SavedGame;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.*;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Guards {@link FullSaveGameManager#FORMAT_VERSION}: describes the serialized structure of every
 * FreeRails class that can end up in a save game and compares it with a golden file. When the
 * structure changes, the test fails until the format version is bumped and the golden file is
 * regenerated with {@code ./gradlew test --tests freerails.io.savegame.SaveGameFormatTest
 * -Dfreerails.updateSaveGameFormat=true}.
 *
 * <p>Classes are found three ways: classes written when serializing a sample saved game, the
 * declared (including generic) types of serialized fields reachable from {@link SavedGame}, and all
 * serializable FreeRails subtypes of reachable abstract or non-final types. Classes that are only
 * reachable through fields of type {@link Object} or {@link Serializable} are not found that way
 * and must be listed in {@link #EXTRA_ROOTS}.
 */
public class SaveGameFormatTest extends TestCase {

    private static final String UPDATE_PROPERTY = "freerails.updateSaveGameFormat";
    private static final Path GOLDEN_FILE = Path.of("src", "test", "resources", "freerails", "io", "savegame",
            "savegame-format.txt");
    /** Additional classes stored in save games that the class discovery cannot find. */
    private static final List<Class<?>> EXTRA_ROOTS = List.of();
    /** Types too general to expand into their subtypes. */
    private static final Set<Class<?>> NON_EXPANDING_TYPES = Set.of(Object.class, Serializable.class,
            Comparable.class, Cloneable.class);
    private static final List<String> SERIALIZATION_HOOKS = List.of("writeObject", "readObject",
            "readObjectNoData", "writeReplace", "readResolve");

    /**
     * Tests that the save-game structure matches the golden file for the current format version.
     *
     * @throws Exception if the structure cannot be described or the golden file cannot be read
     */
    public void testSaveGameStructureMatchesFormatVersion() throws Exception {
        String actual = describeFormat();

        if (Boolean.getBoolean(UPDATE_PROPERTY)) {
            Files.createDirectories(GOLDEN_FILE.getParent());
            Files.writeString(GOLDEN_FILE, actual, StandardCharsets.UTF_8);
            return;
        }

        assertTrue("Missing " + GOLDEN_FILE + "; generate it with -D" + UPDATE_PROPERTY + "=true",
                Files.isRegularFile(GOLDEN_FILE));
        String expected = Files.readString(GOLDEN_FILE, StandardCharsets.UTF_8).replace("\r\n", "\n");
        String expectedHeader = expected.lines().findFirst().orElse("");
        String message = formatHeader().equals(expectedHeader)
                ? "Save-game structure changed. Bump FullSaveGameManager.FORMAT_VERSION and regenerate "
                        + GOLDEN_FILE + " with -D" + UPDATE_PROPERTY + "=true."
                : "FullSaveGameManager.FORMAT_VERSION changed. Regenerate " + GOLDEN_FILE
                        + " with -D" + UPDATE_PROPERTY + "=true.";
        assertEquals(message, expected, actual);
    }

    /**
     * Tests that the class discovery finds the central save-game classes.
     *
     * @throws Exception if the classes cannot be discovered
     */
    public void testDiscoversSaveGameClasses() throws Exception {
        Set<Class<?>> classes = saveGameClasses();
        assertTrue(classes.contains(SavedGame.class));
        assertTrue(classes.contains(World.class));
        assertTrue(classes.contains(LogOnCredentials.class));
        assertTrue(classes.contains(Train.class));
        assertTrue(classes.contains(Transaction.class));
    }

    private static String formatHeader() {
        return "formatVersion=" + FullSaveGameManager.FORMAT_VERSION;
    }

    private static String describeFormat() throws Exception {
        StringBuilder description = new StringBuilder(formatHeader()).append('\n');
        for (Class<?> type : saveGameClasses()) {
            description.append('\n');
            describeClass(type, description);
        }
        return description.toString();
    }

    /**
     * Returns all serializable FreeRails classes that can be part of a save game, sorted by name.
     */
    private static Set<Class<?>> saveGameClasses() throws Exception {
        List<Class<?>> serializableClasses = serializableMainClasses();
        Set<Class<?>> found = new TreeSet<>(Comparator.comparing(Class::getName));
        Deque<Class<?>> pending = new ArrayDeque<>();
        pending.add(SavedGame.class);
        pending.addAll(EXTRA_ROOTS);
        pending.addAll(classesWrittenBySampleGame());

        Set<Class<?>> visited = new HashSet<>();
        while (!pending.isEmpty()) {
            Class<?> type = pending.remove();
            if (type.isArray() || type.isPrimitive() || !visited.add(type)) {
                continue;
            }
            if (!type.getName().startsWith("freerails.")) {
                continue;
            }
            if (Serializable.class.isAssignableFrom(type) && !type.isInterface()) {
                found.add(normalizeEnum(type));
            }
            if (type.getSuperclass() != null) {
                pending.add(type.getSuperclass());
            }
            if (!NON_EXPANDING_TYPES.contains(type)
                    && (type.isInterface() || Modifier.isAbstract(type.getModifiers())
                    || !Modifier.isFinal(type.getModifiers()))) {
                for (Class<?> candidate : serializableClasses) {
                    if (type.isAssignableFrom(candidate)) {
                        pending.add(candidate);
                    }
                }
            }
            if (type.isRecord()) {
                for (RecordComponent component : type.getRecordComponents()) {
                    addTypes(component.getGenericType(), pending);
                }
            } else {
                for (Field field : type.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                        addTypes(field.getGenericType(), pending);
                    }
                }
            }
        }
        return found;
    }

    /**
     * Adds a type and all of its type arguments, bounds and array components to the pending queue.
     */
    private static void addTypes(Type type, Deque<Class<?>> pending) {
        switch (type) {
            case Class<?> clazz -> {
                if (clazz.isArray()) {
                    addTypes(clazz.getComponentType(), pending);
                } else if (!NON_EXPANDING_TYPES.contains(clazz)) {
                    pending.add(clazz);
                }
            }
            case ParameterizedType parameterized -> {
                addTypes(parameterized.getRawType(), pending);
                for (Type argument : parameterized.getActualTypeArguments()) {
                    addTypes(argument, pending);
                }
            }
            case GenericArrayType array -> addTypes(array.getGenericComponentType(), pending);
            case WildcardType wildcard -> {
                for (Type bound : wildcard.getUpperBounds()) {
                    addTypes(bound, pending);
                }
                for (Type bound : wildcard.getLowerBounds()) {
                    addTypes(bound, pending);
                }
            }
            case TypeVariable<?> variable -> {
                for (Type bound : variable.getBounds()) {
                    addTypes(bound, pending);
                }
            }
            default -> throw new IllegalStateException("Unexpected type " + type);
        }
    }

    /**
     * Returns the classes whose descriptors are written when serializing a sample saved game.
     */
    private static Set<Class<?>> classesWrittenBySampleGame() throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        try (ObjectOutputStream out = new ObjectOutputStream(OutputStream.nullOutputStream()) {
            @Override
            protected void annotateClass(Class<?> cl) {
                classes.add(cl);
            }
        }) {
            out.writeObject(sampleSavedGame());
        }
        return classes;
    }

    private static SavedGame sampleSavedGame() {
        Player player = WorldGenerator.TEST_PLAYER;
        World world = WorldGenerator.defaultWorld();
        world.addPlayer(player);
        int engineId = world.getEngines().iterator().next().getId();
        int cargoId = world.getCargos().iterator().next().getId();
        Train train = new Train(2, engineId);
        train.setConsist(List.of(cargoId));
        CargoBatchBundle cargo = new CargoBatchBundle();
        cargo.addCargo(new CargoBatch(cargoId, IntPoint2D.ZERO, new Time(0), 0), 4);
        train.setCargoBatchBundle(cargo);
        world.addTrain(player, train);
        world.addActivity(player, train.getId(), new TrainMotion(
                new PathOnTiles(IntPoint2D.ZERO, List.of(TileTransition.EAST, TileTransition.EAST)),
                24, 20, TrainState.READY));
        world.addTransaction(player, new Transaction(TransactionCategory.MISC_INCOME, new Money(10), Time.ZERO));
        return new SavedGame(world, List.of(new LogOnCredentials(player.getName(), "secret")));
    }

    /**
     * Returns all serializable classes compiled from the main source set.
     */
    private static List<Class<?>> serializableMainClasses() throws IOException, URISyntaxException {
        Path classesDirectory = Path.of(SavedGame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
                    type = Class.forName(className, false, SavedGame.class.getClassLoader());
                } catch (ClassNotFoundException | LinkageError e) {
                    continue;
                }
                if (Serializable.class.isAssignableFrom(type) && !type.isInterface()) {
                    classes.add(type);
                }
            }
        }
        return classes;
    }

    /**
     * Maps enum constant bodies to their enum type, which is what serialization stores.
     */
    private static Class<?> normalizeEnum(Class<?> type) {
        Class<?> superclass = type.getSuperclass();
        return superclass != null && superclass.isEnum() ? superclass : type;
    }

    private static void describeClass(Class<?> type, StringBuilder out) {
        if (type.isEnum()) {
            out.append("enum ").append(type.getName()).append('\n');
            for (Object constant : type.getEnumConstants()) {
                out.append("  ").append(((Enum<?>) constant).name()).append('\n');
            }
            return;
        }

        out.append(type.isRecord() ? "record " : "class ").append(type.getName());
        if (!type.isRecord() && type.getSuperclass() != null && type.getSuperclass() != Object.class) {
            out.append(" extends ").append(type.getSuperclass().getName());
        }
        if (Externalizable.class.isAssignableFrom(type)) {
            out.append(" externalizable");
        }
        List<String> hooks = new ArrayList<>();
        for (Method method : type.getDeclaredMethods()) {
            if (SERIALIZATION_HOOKS.contains(method.getName()) && method.getParameterCount() <= 1
                    && !method.isSynthetic()) {
                hooks.add(method.getName());
            }
        }
        if (!hooks.isEmpty()) {
            Collections.sort(hooks);
            out.append(" hooks ").append(String.join(",", hooks));
        }
        out.append('\n');

        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                out.append("  ").append(component.getName()).append(": ")
                        .append(component.getGenericType().getTypeName()).append('\n');
            }
            return;
        }
        ObjectStreamClass streamClass = ObjectStreamClass.lookup(type);
        if (streamClass == null) {
            return;
        }
        for (ObjectStreamField field : streamClass.getFields()) {
            out.append("  ").append(field.getName()).append(": ")
                    .append(fieldTypeName(type, field)).append('\n');
        }
    }

    private static String fieldTypeName(Class<?> type, ObjectStreamField field) {
        try {
            return type.getDeclaredField(field.getName()).getGenericType().getTypeName();
        } catch (NoSuchFieldException e) {
            return field.getType().getName();
        }
    }
}
