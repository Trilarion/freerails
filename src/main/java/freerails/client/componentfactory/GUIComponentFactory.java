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

package freerails.client.componentfactory;

import javax.swing.*;

/**
 * Defines methods that create the GUI components used by the game.
 */
public interface GUIComponentFactory {

    /**
     * Creates the overview map.
     *
     * @return the overview map produced by this method
     */
    JPanel createOverviewMap();

    /**
     * Creates the trains j tab pane.
     *
     * @return the trains jtab pane produced by this method
     */
    JTabbedPane createTrainsJTabPane();

    /**
     * Creates the main map.
     *
     * @return the main map produced by this method
     */
    JScrollPane createMainMap();

    /**
     * Creates the cash j label.
     *
     * @return the cash jlabel produced by this method
     */
    JLabel createCashJLabel();

    /**
     * Creates the date j label.
     *
     * @return the date jlabel produced by this method
     */
    JLabel createDateJLabel();

    /**
     * Creates the build menu.
     *
     * @return the build menu produced by this method
     */
    JMenu createBuildMenu();

    /**
     * Creates the reports menu.
     *
     * @return the reports menu produced by this method
     */
    JMenu createReportsMenu();

    /**
     * Creates the game menu.
     *
     * @return the game menu produced by this method
     */
    JMenu createGameMenu();

    /**
     * Creates the display menu.
     *
     * @return the display menu produced by this method
     */
    JMenu createDisplayMenu();

    /**
     * Creates the help menu.
     *
     * @return the help menu produced by this method
     */
    JMenu createHelpMenu();

    /**
     * Creates the broker menu.
     *
     * @return the broker menu produced by this method
     */
    JMenu createBrokerMenu();
}