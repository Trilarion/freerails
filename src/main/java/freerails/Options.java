package freerails;

import com.google.gson.*;
import freerails.client.ClientConstants;
import freerails.util.Utils;
import freerails.util.Vec2D;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Options that can be changed and are loaded on startup and stored on exit.
 */
public final class Options {

    // client options

    // name
    public static String NAME;

    // main window
    public static Boolean MAINWINDOW_FULLSCREEN;

    // window resolution
    public static Vec2D DISPLAY_MODE;

    // sound
    public static Boolean SOUNDTRACK_MUTE;

    // server options

    // public IP
    public static String IP;

    // public Port
    public static Integer PORT;

    {
        // set to default values once in the beginning
        reset();
    }

    /**
     * Sets to default values
     */
    public static void reset() {
        NAME = System.getProperty("user.name");
        MAINWINDOW_FULLSCREEN = false;
        DISPLAY_MODE = Vec2D.ZERO;
        SOUNDTRACK_MUTE = false;
        IP = "127.0.0.1";
        PORT = 55000;
    }

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
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        // deserialize, created instance not needed
        gson.fromJson(json, Options.class);

    }

    /**
     *
     * @param file
     */
    public static void save(final File file) {
        Utils.verifyNotNull(file);

        // create Gson instance
        GsonBuilder builder = new GsonBuilder().serializeNulls().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.TRANSIENT);
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
