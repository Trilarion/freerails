package freerails.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import freerails.client.ClientConstants;
import freerails.gson.adapter.MoneyAdapter;
import freerails.model.finances.Money;
import freerails.model.train.Engine;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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

    public static SortedSet<Engine> loadEngines(URL url) throws IOException{
        // load json
        String json = IOUtils.toString(url, ClientConstants.defaultCharset);

        // deserialize json
        SortedSet<Engine> engines = gson.fromJson(json, new TypeToken<SortedSet<Engine>>(){}.getType());

        return engines;
    }
}
