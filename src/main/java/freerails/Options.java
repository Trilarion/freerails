package freerails;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import freerails.client.ClientConstants;
import freerails.util.Utils;
import freerails.util.Vec2D;
import freerails.util.value.ValueWithDefault;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;

/**
 * Options that can be changed and are loaded on startup and stored on exit.
 */
public final class Options {

    // version
    public static ValueWithDefault<String> OPTION_VERSION = new ValueWithDefault<>(Version.VERSION);

    // client options
    public static final class Client {

        // name
        public static ValueWithDefault<String> NAME = new ValueWithDefault<>(System.getProperty("user.name"));

        // main window
        public static ValueWithDefault<Boolean> MAINWINDOW_FULLSCREEN = new ValueWithDefault<>(false);

        // window resolution
        public static ValueWithDefault<Vec2D> DISPLAY_MODE = new ValueWithDefault<>(Vec2D.ZERO);

        // sound
        public static ValueWithDefault<Boolean> SOUNDTRACK_MUTE = new ValueWithDefault<>(false);

    }

    // server options
    public static final class Server {

        // public IP
        public static ValueWithDefault<String> IP = new ValueWithDefault<>("127.0.0.1");

        // public Port
        public static ValueWithDefault<Integer> PORT = new ValueWithDefault<>(55000);
    }






    // TODO only serialize values, not default values
    /**
     *
     * @param file
     */
    public static void load(final File file) {
        Utils.verifyNotNull(file);

        // read from file
        String json;
        try {
            json = FileUtils.readFileToString(file, ClientConstants.defaultCharset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // create Gson instance
        GsonBuilder builder = (new GsonBuilder()).serializeNulls().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.TRANSIENT);
        Gson gson = builder.create();

        // deserialize from json
        gson.fromJson(json, Options.class);
    }

    // TODO deserialize only serialized values, no default values
    /**
     *
     * @param file
     */
    public static void save(final File file) {
        Utils.verifyNotNull(file);

        // create Gson instance
        GsonBuilder builder = (new GsonBuilder()).serializeNulls().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.TRANSIENT);
        Gson gson = builder.create();

        // serialize to json
        String json = gson.toJson(new Options());

        // write to file
        try {
            FileUtils.writeStringToFile(file, json, ClientConstants.defaultCharset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
