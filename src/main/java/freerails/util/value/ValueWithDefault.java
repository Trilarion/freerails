package freerails.util.value;

/**
 * Value with default ability.
 *
 * @param <T>
 */
public class ValueWithDefault<T> extends Value<T> {

    private final T defaultValue;

    public ValueWithDefault(T defaultValue) {
        this.defaultValue = defaultValue;
        set(defaultValue);
    }

    public void setToDefaultValue() {
        set(defaultValue);
    }

    public T getDefaultValue() {
        return defaultValue;
    }
}

