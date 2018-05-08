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
import freerails.model.terrain.City;
import freerails.model.terrain.TerrainType2;
import freerails.model.train.Engine;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 *
 */
public class GsonManager {

    private static Gson gson;
    static
    {
        GsonBuilder builder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        builder.registerTypeAdapter(Money.class, new MoneyAdapter());
        builder.registerTypeAdapter(ARGBColor.class, new ColorAdapter());
        gson = builder.create();
    }

    private static final Type enginesListType = new TypeToken<List<Engine>>(){}.getType();
    private static final Type citiesListType = new TypeToken<List<City>>(){}.getType();
    private static final Type cargoTypesListType = new TypeToken<List<Cargo>>(){}.getType();
    private static final Type terrainTypesListType = new TypeToken<List<TerrainType2>>(){}.getType();
    private static final Type terrainColorsMapType = new TypeToken<Map<Integer, ARGBColor>>(){}.getType();

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

    public static SortedSet<TerrainType2> loadTerrainTypes(File file) throws IOException {
        return loadAsSortedSet(file, terrainTypesListType);
    }

    public static Map<Integer, ARGBColor> loadTerrainColors(File file) throws  IOException {
        return loadAsMap(file, terrainColorsMapType);
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
        Map<K, V> map = gson.fromJson(json, type);

        return map;
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
