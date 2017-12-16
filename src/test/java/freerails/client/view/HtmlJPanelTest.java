/*
 * Created on Mar 29, 2004
 */
package freerails.client.view;

import junit.framework.TestCase;

import java.util.HashMap;

/**
 * Tests the populateTokens method on HtmlJPanel.
 *
 * @author Luke
 */
public class HtmlJPanelTest extends TestCase {
    public void testPopulateTokens() {
        String template = "test";
        HashMap<String, String> context = new HashMap<String, String>();
        String output = HtmlJPanel.populateTokens(template, context);
        assertEquals(template, output);

        template = "Hello $name$, $question$";
        context.put("name", "Luke");
        context.put("question", "how are you?");

        String expectedOutput = "Hello Luke, how are you?";
        output = HtmlJPanel.populateTokens(template, context);
        assertEquals(expectedOutput, output);

        Object objectContext = new Object() {
            @SuppressWarnings("unused")
            public String name = "Luke";

            @SuppressWarnings("unused")
            public String question = "how are you?";
        };

        output = HtmlJPanel.populateTokens(template, objectContext);
        assertEquals(expectedOutput, output);
    }

    public void testPopulateTokens2() {
        String template = "Hey $a.name$ I would like you to meet $b.name$";
        String expectedOutput = "Hey Tom I would like you to meet Claire";

        Object objectContext = new Object() {
            @SuppressWarnings("unused")
            public Person a = new Person("Tom");

            @SuppressWarnings("unused")
            public Person b = new Person("Claire");
        };

        String output = HtmlJPanel.populateTokens(template, objectContext);
        assertEquals(expectedOutput, output);
    }

    public static class Person {
        public String name;

        public Person(String name) {
            this.name = name;
        }
    }

}
