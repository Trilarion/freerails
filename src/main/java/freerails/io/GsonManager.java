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

package freerails.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import freerails.io.adapter.ColorAdapter;
import freerails.io.adapter.MoneyAdapter;
import freerails.util.Identifiable;
import freerails.model.ModelConstants;
import freerails.model.operations.cargo.Cargo;
import freerails.model.economy.Money;
import freerails.model.game.Sentiment;
import freerails.model.terrain.city.City;
import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.model.operations.train.Engine;
import freerails.util.Array2D;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Loads and saves game data using the application's Gson configuration.
 */
public final class GsonManager {

    private static Gson gson;
    static
    {
        GsonBuilder builder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        builder.registerTypeAdapter(Money.class, new MoneyAdapter());
        builder.registerTypeAdapter(ARGBColor.class, new ColorAdapter());
        builder.setExclusionStrategies(new ExclusionStrategy());
        gson = builder.create();
    }
    private static Gson compact_gson;
    static
    {
        GsonBuilder builder = new GsonBuilder();
        compact_gson = builder.create();
    }

    private static final Type enginesListType = new TypeToken<List<Engine>>(){}.getType();
    private static final Type citiesListType = new TypeToken<List<City>>(){}.getType();
    private static final Type cargoTypesListType = new TypeToken<List<Cargo>>(){}.getType();
    private static final Type terrainTypesListType = new TypeToken<List<Terrain>>(){}.getType();
    private static final Type terrainColorsMapType = new TypeToken<Map<Integer, ARGBColor>>(){}.getType();
    private static final Type trackTypeListType = new TypeToken<List<TrackType>>(){}.getType();
    private static final Type sentimentListType = new TypeToken<List<Sentiment>>(){}.getType();

    private static final TypeToken<Array2D> typeTokenArray2D = new TypeToken<>() {
    };

    /**
     * Prevents instantiation of this utility class.
     */
    private GsonManager() {}

    /**
     * Loads engine definitions from a JSON file.
     *
     * @param file source file
     * @return engines ordered by identifier
     * @throws IOException if the file cannot be read
     */
    public static SortedSet<Engine> loadEngines(File file) throws IOException {
        return loadAsSortedSet(file, enginesListType);
    }

    /**
     * Loads city definitions from a JSON file.
     *
     * @param file source file
     * @return cities ordered by identifier
     * @throws IOException if the file cannot be read
     */
    public static SortedSet<City> loadCities(File file) throws IOException {
        return loadAsSortedSet(file, citiesListType);
    }

    /**
     * Loads cargo definitions from a JSON file.
     *
     * @param file source file
     * @return cargo types ordered by identifier
     * @throws IOException if the file cannot be read
     */
    public static SortedSet<Cargo> loadCargoTypes(File file) throws IOException {
        return loadAsSortedSet(file, cargoTypesListType);
    }

    /**
     * Loads terrain definitions from a JSON file.
     *
     * @param file source file
     * @return terrain types ordered by identifier
     * @throws IOException if the file cannot be read
     */
    public static SortedSet<Terrain> loadTerrainTypes(File file) throws IOException {
        return loadAsSortedSet(file, terrainTypesListType);
    }

    /**
     * Loads terrain colors keyed by terrain identifier.
     *
     * @param file source file
     * @return map of terrain identifiers to colors
     * @throws IOException if the file cannot be read
     */
    public static Map<Integer, ARGBColor> loadTerrainColors(File file) throws  IOException {
        return loadAsMap(file, terrainColorsMapType);
    }

    /**
     * Loads a two-dimensional integer array from JSON.
     *
     * @param file source file
     * @return loaded array
     * @throws IOException if the file cannot be read
     */
    public static Array2D loadArray2D(File file) throws  IOException {
        return load(file, typeTokenArray2D);
    }

    /**
     * Loads track definitions and prepares each definition for use.
     *
     * @param file source file
     * @return track types ordered by identifier
     * @throws IOException if the file cannot be read
     */
    public static SortedSet<TrackType> loadTrackTypes(File file) throws IOException {
        SortedSet<TrackType> trackTypes = loadAsSortedSet(file, trackTypeListType);
        // prepare
        for (TrackType trackType: trackTypes) {
            trackType.prepare();
        }
        return trackTypes;
    }

    /**
     * Loads sentiment definitions from a JSON file.
     *
     * @param file source file
     * @return sentiments ordered by identifier
     * @throws IOException if the file cannot be read
     */
    public static SortedSet<Sentiment> loadSentiments(File file) throws IOException {
        return loadAsSortedSet(file, sentimentListType);
    }

    /**
     * Deserializes a JSON file using a generic type token.
     *
     * @param file source file
     * @param typeToken token describing the result type
     * @param <E> result type
     * @return deserialized value
     * @throws IOException if the file cannot be read
     */
    private static <E> E load(File file, TypeToken<E> typeToken) throws IOException {
        // load JSON
        String json = FileUtils.readFileToString(file, ModelConstants.defaultCharset);

        // deserialize
        return gson.fromJson(json, typeToken.getType());
    }

    /**
     * Deserializes a JSON file into the specified reflective type.
     *
     * @param file source file
     * @param type type represented by the JSON
     * @param <E> result type
     * @return deserialized value
     * @throws IOException if the file cannot be read
     */
    public static <E> E load(File file, Type type) throws IOException {
        // load JSON
        String json = FileUtils.readFileToString(file, ModelConstants.defaultCharset);

        // deserialize JSON
        return gson.fromJson(json, type);
    }

    /**
     * Loads a JSON object as a map.
     *
     * @param file source file
     * @param type reflective map type
     * @param <K> key type
     * @param <V> value type
     * @return deserialized map
     * @throws IOException if the file cannot be read
     */
    private static <K, V> Map<K, V> loadAsMap(File file, Type type) throws IOException {
        // load JSON
        String json = FileUtils.readFileToString(file, ModelConstants.defaultCharset);

        // deserialize JSON
        return gson.fromJson(json, type);
    }

    /**
     * Loads identifiable values into a sorted set and checks identifier uniqueness.
     *
     * @param file source file
     * @param type reflective collection type
     * @param <T> identifiable element type
     * @return values ordered by identifier
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if identifiers are duplicated
     */
    private static <T extends Identifiable> SortedSet<T> loadAsSortedSet(File file, Type type) throws IOException {
        // load JSON
        String json = FileUtils.readFileToString(file, ModelConstants.defaultCharset);

        // deserialize JSON
        List<T> list = gson.fromJson(json, type);

        // check for uniqueness
        verifyUniqueIds(list);

        return new TreeSet<>(list);
    }

    /**
     * Verifies that a collection contains no duplicate identifiers.
     *
     * @param c identifiable values to inspect
     * @param <E> element type
     * @throws IllegalArgumentException if two values share an identifier
     * @throws NullPointerException if {@code c} or an element is null
     */
    public static <E extends Identifiable> void verifyUniqueIds(Collection<E> c) {
        Map<Integer, Object> map = new HashMap<>(c.size());
        for (E e: c) {
            Integer id = e.getId();
            if (map.containsKey(id)) {
                throw new IllegalArgumentException(String.format("Contains non-unique Ids (id = %d).", id));
            } else {
                map.put(id, null);
            }
        }
    }

    /**
     * Serializes an object as formatted JSON and writes it to a file.
     *
     * @param file destination file
     * @param object object to serialize
     * @throws IOException if the file cannot be written
     */
    public static void save(File file, Object object) throws IOException {
        // serialize to JSON
        String json = gson.toJson(object);

        // write JSON
        FileUtils.writeStringToFile(file, json, ModelConstants.defaultCharset);
    }

    /**
     * Serializes an object as compact JSON and writes it to a file.
     *
     * @param file destination file
     * @param object object to serialize
     * @throws IOException if the file cannot be written
     */
    public static void saveCompact(File file, Object object) throws IOException {
        // serialize to JSON
        String json = compact_gson.toJson(object);

        // write JSON
        FileUtils.writeStringToFile(file, json, ModelConstants.defaultCharset);
    }

    /**
     * Creates a map keyed by identifier from a list of identifiable values.
     *
     * @param list values to index
     * @param <E> identifiable element type
     * @return map from identifier to value
     * @throws IllegalArgumentException if identifiers are duplicated
     */
    public static <E extends Identifiable> Map<Integer, E> toMap(List<E> list) {
        Map<Integer, E> map = new HashMap<>();
        for (E e: list) {
            if (map.containsKey(e.getId())) {
                throw new IllegalArgumentException("List contains non-unique ids.");
            }
            map.put(e.getId(), e);
        }
        return map;
    }
}
