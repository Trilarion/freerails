/*
 * Created on Feb 20, 2004
 */
package experimental;

import junit.framework.TestCase;


/**
 *
 *  @author Luke
 *
 */
public class GenerateDependenciesXmlAndHtmlTest extends TestCase {
    public void testIsPackageNameOk() {
        assertTrue(GenerateDependenciesXmlAndHtml.isPackageNameOk(
                "jfreerails/*"));
        assertFalse(GenerateDependenciesXmlAndHtml.isPackageNameOk(
                "jfreerails.*"));
        assertTrue(GenerateDependenciesXmlAndHtml.isPackageNameOk(
                "jfreerails/trees/*"));
        assertFalse(GenerateDependenciesXmlAndHtml.isPackageNameOk(
                "jfreerails/trees/branches*"));
        assertTrue(GenerateDependenciesXmlAndHtml.isPackageNameOk(
                "jfreerails/trees/branches/*"));
        assertFalse(GenerateDependenciesXmlAndHtml.isPackageNameOk(
                "jfreerails/trees/branches/**/"));
        assertTrue(GenerateDependenciesXmlAndHtml.isPackageNameOk(
                "jfreerails/trees/branches/**/*"));
        assertTrue(GenerateDependenciesXmlAndHtml.isPackageNameOk(
                "it/unimi/dsi/fastUtil/*")); //note upper case in package name.
    }
}