/*
 * Created on Mar 29, 2004
 */
package jfreerails.client.view;

import java.util.HashMap;

import junit.framework.TestCase;

/**
 *
 *  @author Luke  
 *
 */
public class HtmlJPanelTest extends TestCase {
	public void testPopulateTokens() {
		String template = "test";
		HashMap context = new HashMap();
		String output = HtmlJPanel.populateTokens(template, context);
		assertEquals(template, output);
		
		template = "Hello $name$, $question$";
		context.put("name", "Luke");
		context.put("question", "how are you?");
		String expectedOutput = "Hello Luke, how are you?";
		output= HtmlJPanel.populateTokens(template, context);
		assertEquals(expectedOutput, output);
		Object objectContext = new Object(){
			public String name = "Luke";
			public String question = "how are you?";
		};
		output= HtmlJPanel.populateTokens(template, objectContext);
		assertEquals(expectedOutput, output);
		
	}
}
