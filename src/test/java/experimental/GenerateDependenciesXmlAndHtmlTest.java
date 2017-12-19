/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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