package freerails.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import freerails.model.finances.Money;

import java.io.IOException;

/**
 * Money has only a single member variable. Read and write it directly with Gson.
 */
public class MoneyAdapter extends TypeAdapter<Money> {

    @Override
    public void write(JsonWriter out, Money value) throws IOException {
        out.value(value.amount);
    }

    @Override
    public Money read(JsonReader in) throws IOException {
        long amount = in.nextLong();
        return new Money(amount);
    }
}
