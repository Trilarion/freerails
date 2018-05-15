package freerails.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import freerails.model.finances.Money;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Money has only a single member variable. Read and write it directly with Gson.
 */
public class MoneyAdapter extends TypeAdapter<Money> {

    @Override
    public void write(JsonWriter out, @Nullable Money value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.amount);
        }
    }

    @Override
    public @Nullable Money read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            long amount = in.nextLong();
            return new Money(amount);
        }
    }
}
