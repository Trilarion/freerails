package freerails.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import freerails.client.ARGBColor;

import java.io.IOException;

public class ColorAdapter extends TypeAdapter<ARGBColor> {

    @Override
    public void write(JsonWriter out, ARGBColor value) throws IOException {
        out.value(ARGBColor.toHexString(value));
    }

    @Override
    public ARGBColor read(JsonReader in) throws IOException {
        return ARGBColor.fromHexString(in.nextString());
    }
}
