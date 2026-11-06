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

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

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
}
