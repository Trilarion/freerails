package freerails.util.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import org.tinylog.configuration.ServiceLoader;
import org.tinylog.throwable.ThrowableFilter;
import org.tinylog.throwable.ThrowableData;
import org.tinylog.throwable.ThrowableWrapper;

/** Tests the stack-trace filtering used by the configured logger. */
public class ThrowableFilterTest {

    /** Removes configured runtime and dependency frames while retaining application frames. */
    @Test
    public void removesConfiguredPrefixesAndRetainsOtherFrames() throws IOException {
        RuntimeException exception = new RuntimeException("failure");
        exception.setStackTrace(new StackTraceElement[] {
                frame("freerails.Start", "main"),
                frame("java.lang.Thread", "run"),
                frame("com.google.gson.JsonParser", "parse"),
                frame("org.apache.commons.io.IOUtils", "read"),
                frame("com.example.Library", "execute")
        });

        ThrowableData filtered = filter(exception);

        assertEquals("failure", filtered.getMessage());
        assertEquals(List.of(
                frame("freerails.Start", "main"),
                frame("com.example.Library", "execute")), filtered.getStackTrace());
    }

    /** Applies the configured prefix filtering to causes and suppressed exceptions. */
    @Test
    public void filtersCauseAndSuppressedTracesRecursively() throws IOException {
        RuntimeException cause = new RuntimeException("cause");
        cause.setStackTrace(new StackTraceElement[] {
                frame("java.lang.Thread", "run"),
                frame("freerails.Server", "start")
        });
        RuntimeException exception = new RuntimeException("failure", cause);
        exception.setStackTrace(new StackTraceElement[] {
                frame("freerails.Client", "connect"),
                frame("jdk.internal.reflect.NativeMethodAccessorImpl", "invoke")
        });
        IOException suppressed = new IOException("suppressed");
        suppressed.setStackTrace(new StackTraceElement[] {
                frame("org.tinylog.Logger", "error"),
                frame("freerails.Client", "close")
        });
        exception.addSuppressed(suppressed);

        ThrowableData filtered = filter(exception);

        assertEquals("failure", filtered.getMessage());
        assertEquals(List.of(frame("freerails.Client", "connect")), filtered.getStackTrace());
        assertEquals("cause", filtered.getCause().getMessage());
        assertEquals(List.of(frame("freerails.Server", "start")),
                filtered.getCause().getStackTrace());
        assertEquals(1, filtered.getSuppressed().size());
        assertEquals("suppressed", filtered.getSuppressed().get(0).getMessage());
        assertEquals(List.of(frame("freerails.Client", "close")),
                filtered.getSuppressed().get(0).getStackTrace());
        assertEquals(RuntimeException.class.getName(), filtered.getClassName());
    }

    /** Applies tinylog's configured built-in filter to a throwable. */
    private static ThrowableData filter(Throwable throwable) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = ThrowableFilterTest.class.getResourceAsStream("/tinylog.properties")) {
            if (input == null) {
                throw new IOException("Could not load tinylog.properties.");
            }
            properties.load(input);
        }
        String configuration = properties.getProperty("writerFile.exception");
        assertNotNull(configuration);
        List<ThrowableFilter> filters =
                new ServiceLoader<>(ThrowableFilter.class, String.class).createList(configuration);
        assertEquals(1, filters.size());
        return filters.get(0).filter(new ThrowableWrapper(throwable));
    }

    /** Creates a stack-trace frame for a test class and method. */
    private static StackTraceElement frame(String className, String methodName) {
        return new StackTraceElement(className, methodName, className + ".java", 1);
    }
}
