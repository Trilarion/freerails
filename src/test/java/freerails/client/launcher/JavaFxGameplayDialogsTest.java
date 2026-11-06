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

package freerails.client.launcher;

import freerails.client.ClientConstants;
import freerails.model.economy.reporting.BalanceSheetGenerator;
import freerails.model.economy.reporting.IncomeStatementGenerator;
import freerails.model.player.Player;
import freerails.model.world.World;
import freerails.move.AddPlayerMove;
import freerails.util.WorldGenerator;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests HTML token substitution used by JavaFX report dialogs. */
public class JavaFxGameplayDialogsTest {

    /** Keeps replacement values containing regex syntax literal. */
    @Test
    public void replacesTokensLiterally() {
        assertEquals("<p>Cash $100: \\1</p>",
                JavaFxGameplayDialogs.replaceTemplateTokens(
                        "<p>$heading$: $value$</p>",
                        Map.of("heading", "Cash $100", "value", "\\1")));
    }

    /** Rejects a template whose token has no corresponding report value. */
    @Test(expected = IllegalStateException.class)
    public void rejectsMissingTemplateValues() {
        JavaFxGameplayDialogs.replaceTemplateTokens("<p>$missing$</p>", Map.of());
    }

    /** Renders the financial report templates with values produced by their generators. */
    @Test
    public void rendersFinancialReports() {
        World world = WorldGenerator.minimalWorld();
        Player player = WorldGenerator.TEST_PLAYER;
        new AddPlayerMove(player).apply(world);

        String balanceSheet = JavaFxGameplayDialogs.populateTemplate(
                ClientConstants.VIEW_BALANCE_SHEET,
                new BalanceSheetGenerator(world, player).getTemplateValues());
        assertTrue(balanceSheet.startsWith("<html>"));
        assertTrue(balanceSheet.contains("Balance Sheet: " + world.getClock().getCurrentYear()));
        assertFalse(balanceSheet.contains("$"));

        IncomeStatementGenerator incomeStatementGenerator = new IncomeStatementGenerator(world, player);
        incomeStatementGenerator.calculateAll();
        String incomeStatement = JavaFxGameplayDialogs.populateTemplate(
                ClientConstants.VIEW_INCOME_STATEMENT,
                incomeStatementGenerator.getTemplateValues());
        assertTrue(incomeStatement.startsWith("<html>"));
        assertTrue(incomeStatement.contains("Income Statement: " + world.getClock().getCurrentYear()));
        assertFalse(incomeStatement.contains("$"));
    }

    /** Renders the broker template with values produced by the broker report generator. */
    @Test
    public void rendersBrokerReport() {
        World world = WorldGenerator.minimalWorld();
        Player player = WorldGenerator.TEST_PLAYER;
        new AddPlayerMove(player).apply(world);

        String brokerReport = JavaFxGameplayDialogs.populateTemplate(
                ClientConstants.VIEW_BROKER,
                new freerails.client.view.BrokerScreenGenerator(world, player).getTemplateValues());
        assertTrue(brokerReport.contains(player.getName()));
        assertFalse(brokerReport.contains("$"));
    }
}
