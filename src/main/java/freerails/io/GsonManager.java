/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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
import freerails.client.ARGBColor;
import freerails.io.adapter.ColorAdapter;
import freerails.io.adapter.MoneyAdapter;
import freerails.model.Identifiable;
import freerails.model.ModelConstants;
import freerails.model.cargo.Cargo;
import freerails.model.finances.Money;
import freerails.model.terrain.city.City;
import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.model.train.Engine;
import freerails.util.Array2D;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 *
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

    private static final TypeToken<Array2D> typeTokenArray2D = new TypeToken<Array2D>() {};

    private GsonManager() {}

    public static SortedSet<Engine> loadEngines(File file) throws IOException {
        return loadAsSortedSet(file, enginesListType);
    }

    public static SortedSet<City> loadCities(File file) throws IOException {
        return loadAsSortedSet(file, citiesListType);
    }

    public static SortedSet<Cargo> loadCargoTypes(File file) throws IOException {
        return loadAsSortedSet(file, cargoTypesListType);
    }

    public static SortedSet<Terrain> loadTerrainTypes(File file) throws IOException {
        return loadAsSortedSet(file, terrainTypesListType);
    }

    public static Map<Integer, ARGBColor> loadTerrainColors(File file) throws  IOException {
        return loadAsMap(file, terrainColorsMapType);
    }

    public static Array2D loadArray2D(File file) throws  IOException {
        return load(file, typeTokenArray2D);
    }

    public static SortedSet<TrackType> loadTrackTypes(File file) throws IOException {
        SortedSet<TrackType> trackTypes = loadAsSortedSet(file, trackTypeListType);
        // prepare
        for (TrackType trackType: trackTypes) {
            trackType.prepare();
        }
        return trackTypes;
    }

    private static <E> E load(File file, TypeToken<E> typeToken) throws IOException {
        // load json
        String json = FileUtils.readFileToString(file, ModelConstants.defaultCharset);

        // deserialize
        return gson.fromJson(json, typeToken.getType());
    }

    /**
     *
     * @param file
     * @param type
     * @return
     * @throws IOException
     */
    public static <E> E load(File file, Type type) throws IOException {
        // load json
        String json = FileUtils.readFileToString(file, ModelConstants.defaultCharset);

        // deserialize json
        return gson.fromJson(json, type);
    }

    /**
     *
     * @param file
     * @param type
     * @param <K>
     * @param <V>
     * @return
     * @throws IOException
     */
    private static <K, V> Map<K, V> loadAsMap(File file, Type type) throws IOException {
        // load json
        String json = FileUtils.readFileToString(file, ModelConstants.defaultCharset);

        // deserialize json
        return gson.fromJson(json, type);
    }

    /**
     * Additionally checks that no id is used twice.
     *
     * @param file
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    private static <T extends Identifiable> SortedSet<T> loadAsSortedSet(File file, Type type) throws IOException {
        // load json
        String json = FileUtils.readFileToString(file, ModelConstants.defaultCharset);

        // deserialize json
        List<T> list = gson.fromJson(json, type);

        // check for uniqueness
        verifyUniqueIds(list);

        return new TreeSet<>(list);
    }

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

    public static void save(File file, Object object) throws IOException {
        // serialize to json
        String json = gson.toJson(object);

        // write json
        FileUtils.writeStringToFile(file, json, ModelConstants.defaultCharset);
    }

    /**
     * Used for saving Array2D (maps, etc. ...).
     *
     * @param file
     * @param object
     * @throws IOException
     */
    public static void saveCompact(File file, Object object) throws IOException {
        // serialize to json
        String json = compact_gson.toJson(object);

        // write json
        FileUtils.writeStringToFile(file, json, ModelConstants.defaultCharset);
    }

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
