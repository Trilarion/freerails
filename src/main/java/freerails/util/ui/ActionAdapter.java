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

package freerails.util.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Provides a mapping from a set of ButtonModels or a ComboBoxModel to a set of
 * Actions. To use with a set of buttons, call elements() to obtain the set of
 * ButtonModels to apply to the buttons, and add each button in the enumeration
 * to a ButtonGroup. Listeners should listen for changes to the model and not to
 * any events from UI components, although UI components may call setAction() in
 * order to receive property change updates and to set icons etc.
 */
public class ActionAdapter extends DefaultComboBoxModel {

    private static final long serialVersionUID = 3546920294666351415L;

    /**
     * The set of actions which each button / menu item correspond to.
     */
    private final Action[] actions;
    /**
     * The set of MappedButtonModels corresponding to the actions.
     */
    private final List<MappedButtonModel> buttonModels;
    private boolean initialised = false;
    private boolean performActionOnSetSelectedItem = true;

    /**
     * An array of the actions to be used. The ComboBoxModel objects are taken
     * from the NAME property of the Action. The ButtonModel icons are obtained
     * from the SMALL_ICON property.
     */
    public ActionAdapter(Action[] actions) {
        super();
        this.actions = actions;
        buttonModels = new ArrayList();

        for (Action action : actions) {
            buttonModels.add(new MappedButtonModel(action));
            addElement(action.getValue(Action.NAME));
        }

        initialised = true;
    }

    /**
     * @param actions  An array of the actions to be used. The ComboBoxModel objects
     *                 are taken from the NAME property of the Action. The
     *                 ButtonModel icons are obtained from the SMALL_ICON property.
     * @param selected Index of the default selected action.
     */
    public ActionAdapter(Action[] actions, int selected) {
        this(actions);

        for (int i = 0; i < buttonModels.size(); i++) {
            MappedButtonModel bm = buttonModels.get(i);
            bm.setSelected(i == selected);
        }
    }

    /**
     * @return an enumeration of Action
     */
    public Action[] getActions() {
        return actions;
    }

    /**
     * @return an enumeration of MappedButtonModel
     */
    public Enumeration<MappedButtonModel> getButtonModels() {
        return Collections.enumeration(buttonModels);
    }

    /**
     * @param anObject The NAME of the Action selected
     */
    @Override
    public void setSelectedItem(Object anObject) {
        // only set the item if not already selected
        if ((anObject != null) && anObject.equals(getSelectedItem())) {
            return;
        }

        super.setSelectedItem(anObject);

        // stop addElement from triggering actions
        if (!initialised) {
            return;
        }

        for (MappedButtonModel bm : buttonModels) {
            if (bm.actionName.equals(anObject)) {
                bm.setSelected(true);
            }
        }

        if (performActionOnSetSelectedItem) {
            for (Action action : actions) {
                if (action.getValue(Action.NAME).equals(anObject)) {
                    action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, (String) action.getValue(Action.ACTION_COMMAND_KEY)));
                }
            }
        }
    }

    /**
     * @param performActionOnSetSelectedItem
     */
    public void setPerformActionOnSetSelectedItem(boolean performActionOnSetSelectedItem) {
        this.performActionOnSetSelectedItem = performActionOnSetSelectedItem;
    }

    /**
     *
     */
    public class MappedButtonModel extends JToggleButton.ToggleButtonModel implements PropertyChangeListener {
        private static final long serialVersionUID = 3834589889856353845L;

        /**
         * The NAME of the Action to which this ButtonModel is mapped.
         */
        private final String actionName;

        /**
         * @param action
         */
        private MappedButtonModel(Action action) {
            actionName = (String) action.getValue(Action.NAME);
            action.addPropertyChangeListener(this);
            setEnabled(action.isEnabled());
        }

        @Override
        public void setSelected(boolean b) {
            if (isSelected() != b) {
                super.setSelected(b);

                if (b) {
                    setSelectedItem(actionName);
                }
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            setEnabled(((Action) evt.getSource()).isEnabled());
        }
    }
}