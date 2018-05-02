package freerails.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import freerails.client.ClientConstants;
import freerails.gson.adapter.MoneyAdapter;
import freerails.model.Identifiable;
import freerails.model.finances.Money;
import freerails.model.train.Engine;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public class GsonManager {

    private static Gson gson;
    static
    {
        GsonBuilder builder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        builder.registerTypeAdapter(Money.class, new MoneyAdapter());
        gson = builder.create();
    }

    private GsonManager() {}

    public static Map<Integer, Engine> loadEngines(URL url) throws IOException{
        // load json
        String json = IOUtils.toString(url, ClientConstants.defaultCharset);

        // deserialize json
        List<Engine> engineList = gson.fromJson(json, new TypeToken<List<Engine>>(){}.getType());

        return toMap(engineList);
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
