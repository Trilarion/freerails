package freerails.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import freerails.gson.adapter.MoneyAdapter;
import freerails.model.Identifiable;
import freerails.model.ModelConstants;
import freerails.model.finances.Money;
import freerails.model.terrain.City2;
import freerails.model.train.Engine;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class GsonManager {

    private static Gson gson;
    static
    {
        GsonBuilder builder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        builder.registerTypeAdapter(Money.class, new MoneyAdapter());
        gson = builder.create();
    }

    private static final Type enginesListType = new TypeToken<List<Engine>>(){}.getType();
    private static final Type citiesListType = new TypeToken<List<City2>>(){}.getType();

    private GsonManager() {}

    public static SortedSet<Engine> loadEngines(File file) throws IOException {
        return load(file, enginesListType);
    }

    public static SortedSet<City2> loadCities(File file) throws IOException {
        return load(file, citiesListType);
    }

    private static <T extends Identifiable> SortedSet<T> load(File file, Type type) throws IOException {
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
