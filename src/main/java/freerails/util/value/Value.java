package freerails.util.value;

/**
 *
 * @param <T>
 */
public class Value<T> {

    private T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
