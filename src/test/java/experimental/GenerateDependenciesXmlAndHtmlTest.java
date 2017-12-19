package experimental;

import junit.framework.TestCase;

/**
 * JUnit test for GenerateDependenciesXmlAndHtml.
 *
 */
public class GenerateDependenciesXmlAndHtmlTest extends TestCase {

    /**
     *
     */
    public void testIsPackageNameOk() {
        assertTrue(GenerateDependenciesXmlAndHtml
                .isPackageNameOk("freerails/*"));
        assertFalse(GenerateDependenciesXmlAndHtml
                .isPackageNameOk("freerails.*"));
        assertTrue(GenerateDependenciesXmlAndHtml
                .isPackageNameOk("freerails/trees/*"));
        assertFalse(GenerateDependenciesXmlAndHtml
                .isPackageNameOk("freerails/trees/branches*"));
        assertTrue(GenerateDependenciesXmlAndHtml
                .isPackageNameOk("freerails/trees/branches/*"));
        assertFalse(GenerateDependenciesXmlAndHtml
                .isPackageNameOk("freerails/trees/branches/**/"));
        assertTrue(GenerateDependenciesXmlAndHtml
                .isPackageNameOk("freerails/trees/branches/**/*"));
        assertTrue(GenerateDependenciesXmlAndHtml
                .isPackageNameOk("it/unimi/dsi/fastUtil/*")); // note upper
        // case in
        // package name.
    }
}