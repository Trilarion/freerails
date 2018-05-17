package freerails.io;

import com.google.gson.FieldAttributes;

/**
 *
 */
public class ExclusionStrategy implements com.google.gson.ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(Conceal.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
