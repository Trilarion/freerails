/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.client.view;

import freerails.client.ClientConstants;
import freerails.model.economy.reporting.BalanceSheetGenerator;
import freerails.model.economy.reporting.IncomeStatementGenerator;
import freerails.model.player.Player;
import freerails.model.world.World;
import freerails.move.AddPlayerMove;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Tests the populateTokens method on HtmlPanel.
 */
public class HtmlPanelTest extends TestCase {

    /**
     * Tests populate tokens behavior.
     */
    public void testPopulateTokens() {
        String template = "test";
        HashMap<String, String> context = new HashMap<>();
        String output = HtmlPanel.populateTokens(template, context);
        assertEquals(template, output);

        template = "Hello $name$, $question$";
        context.put("name", "Luke");
        context.put("question", "how are you?");

        String expectedOutput = "Hello Luke, how are you?";
        output = HtmlPanel.populateTokens(template, context);
        assertEquals(expectedOutput, output);

        output = HtmlPanel.populateTokens(template, Map.of(
                "name", "Luke",
                "question", "how are you?"));
        assertEquals(expectedOutput, output);
    }

    /**
     * Tests populate tokens2 behavior.
     */
    public void testPopulateTokens2() {
        String template = "Hey $a.name$ I would like you to meet $b.name$";
        String expectedOutput = "Hey Tom I would like you to meet Claire";

        Map<String, String> context = Map.of("a.name", "Tom", "b.name", "Claire");
        String output = HtmlPanel.populateTokens(template, context);
        assertEquals(expectedOutput, output);
    }

    /**
     * Tests that a missing token is reported instead of rendered as a null value.
     */
    public void testMissingToken() {
        try {
            HtmlPanel.populateTokens("$missing$", Map.of());
            fail("Expected a missing token to be reported.");
        } catch (NoSuchElementException expected) {
            assertEquals("missing", expected.getMessage());
        }
    }

    /**
     * Tests that both financial report templates render with complete token maps.
     */
    public void testFinancialReportsRender() {
        World world = WorldGenerator.minimalWorld();
        Player player = WorldGenerator.TEST_PLAYER;
        new AddPlayerMove(player).apply(world);

        BalanceSheetGenerator balanceSheetGenerator = new BalanceSheetGenerator(world, player);
        String balanceSheet = renderTemplate(
                BalanceSheetHtmlPanel.class.getResource(ClientConstants.VIEW_BALANCE_SHEET),
                balanceSheetGenerator.getTemplateValues());
        assertTrue(balanceSheet.startsWith("<html>"));
        assertTrue(balanceSheet.contains("Balance Sheet: " + world.getClock().getCurrentYear()));
        assertFalse(balanceSheet.contains("$"));

        IncomeStatementGenerator incomeStatementGenerator = new IncomeStatementGenerator(world, player);
        incomeStatementGenerator.calculateAll();
        String incomeStatement = renderTemplate(
                IncomeStatementHtmlPanel.class.getResource(ClientConstants.VIEW_INCOME_STATEMENT),
                incomeStatementGenerator.getTemplateValues());
        assertTrue(incomeStatement.startsWith("<html>"));
        assertTrue(incomeStatement.contains("Income Statement: " + world.getClock().getCurrentYear()));
        assertFalse(incomeStatement.contains("$"));
    }

    /**
     * Loads and populates an HTML template using the supplied token values.
     *
     * @param url template resource URL
     * @param values token values
     * @return rendered HTML
     */
    private static String renderTemplate(URL url, Map<String, ?> values) {
        return HtmlPanel.populateTokens(HtmlPanel.loadText(url), values);
    }

}
