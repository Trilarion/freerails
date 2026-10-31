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

package freerails.util.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Provides a mapping from a set of ButtonModels or a ComboBoxModel to a set of
 * Actions. To use with a set of buttons, call elements() to obtain the set of
 * ButtonModels to apply to the buttons, and add each button in the enumeration
 * to a ButtonGroup. Listeners should listen for changes to the model and not to
 * any events from UI components, although UI components may call setAction() in
 * order to receive property change updates and to set icons etc.
 */
public class ActionAdapter extends DefaultComboBoxModel<Object> {

    @Serial
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
     * Creates a model mapping action names to toggle-button models.
     *
     * <p>The ComboBoxModel objects are taken
     * from the NAME property of the Action. The ButtonModel icons are obtained
     * from the SMALL_ICON property.
     *
     * @param actions actions to expose; their names must be unique
     * @throws NullPointerException if the array or an action is null
     * @throws IllegalArgumentException if an action has no string name or
     *                                  names are duplicated
     */
    public ActionAdapter(Action[] actions) {
        super();
        this.actions = Objects.requireNonNull(actions, "actions").clone();
        buttonModels = new ArrayList<>();
        Set<String> names = new HashSet<>();

        for (Action action : this.actions) {
            Objects.requireNonNull(action, "actions must not contain null");
            String name = actionName(action);
            if (!names.add(name)) {
                throw new IllegalArgumentException("Action names must be unique: " + name);
            }
            buttonModels.add(new MappedButtonModel(action));
            addElement(name);
        }

        initialised = true;
    }

    /**
     * Creates a model and selects the action at the specified index.
     *
     * @param actions actions to expose
     * @param selected index of the initially selected action
     */
    public ActionAdapter(Action[] actions, int selected) {
        this(actions);

        for (int i = 0; i < buttonModels.size(); i++) {
            MappedButtonModel bm = buttonModels.get(i);
            bm.setSelected(i == selected);
        }
    }

    /**
     * Returns the actions represented by this model.
     *
     * @return a defensive copy of the actions
     */
    public Action[] getActions() {
        return actions.clone();
    }

    /**
     * Returns the button models associated with this model's actions.
     *
     * @return an enumeration of MappedButtonModel
     */
    public Enumeration<MappedButtonModel> getButtonModels() {
        return Collections.enumeration(buttonModels);
    }

    /**
     * Selects the action with the given name and optionally performs it.
     *
     * @param anObject name of the action to select, or {@code null}
     */
    @Override
    public void setSelectedItem(Object anObject) {
        // only set the item if not already selected
        if (anObject != null && anObject.equals(getSelectedItem())) {
            return;
        }

        super.setSelectedItem(anObject);

        // stop addElement from triggering actions
        if (!initialised) {
            return;
        }

        for (MappedButtonModel bm : buttonModels) {
            if (bm.actionName().equals(anObject)) {
                bm.setSelected(true);
            }
        }

        if (performActionOnSetSelectedItem) {
            for (Action action : actions) {
                if (actionName(action).equals(anObject)) {
                    action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, (String) action.getValue(Action.ACTION_COMMAND_KEY)));
                    break;
                }
            }
        }
    }

    /**
     * Controls whether selecting an item invokes its action.
     *
     * @param performActionOnSetSelectedItem {@code true} to invoke the action
     *                                       when the selected item changes
     */
    public void setPerformActionOnSetSelectedItem(boolean performActionOnSetSelectedItem) {
        this.performActionOnSetSelectedItem = performActionOnSetSelectedItem;
    }

    /**
     * Button model that mirrors selection and enabled-state changes of an action.
     */
    public class MappedButtonModel extends JToggleButton.ToggleButtonModel implements PropertyChangeListener {
        @Serial
        private static final long serialVersionUID = 3834589889856353845L;

        /**
         * The NAME of the Action to which this ButtonModel is mapped.
         */
        private final Action action;

        /**
         * Creates a model associated with an action.
         *
         * @param action action represented by this button model
         */
        private MappedButtonModel(Action action) {
            this.action = action;
            action.addPropertyChangeListener(this);
            setEnabled(action.isEnabled());
        }

        /**
         * Returns the current name of the associated action.
         *
         * @return the action's name
         */
        private String actionName() {
            return ActionAdapter.actionName(action);
        }

        /**
         * Updates this model's selection and synchronizes the combo-box selection.
         *
         * @param b whether this model should be selected
         */
        @Override
        public void setSelected(boolean b) {
            if (isSelected() != b) {
                super.setSelected(b);

                if (b) {
                    setSelectedItem(actionName());
                }
            }
        }

        /**
         * Mirrors enabled and name property changes from the associated action.
         *
         * @param evt action property change event
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setEnabled(((Action) evt.getSource()).isEnabled());
            if (Action.NAME.equals(evt.getPropertyName())) {
                int index = buttonModels.indexOf(this);
                String updatedName = actionName();
                for (MappedButtonModel model : buttonModels) {
                    if (model != this && model.actionName().equals(updatedName)) {
                        throw new IllegalArgumentException("Action names must be unique: " + updatedName);
                    }
                }
                Object selectedItem = getSelectedItem();
                boolean oldValue = performActionOnSetSelectedItem;
                performActionOnSetSelectedItem = false;
                try {
                    ActionAdapter.this.removeElementAt(index);
                    ActionAdapter.this.insertElementAt(updatedName, index);
                    setSelectedItem(Objects.equals(selectedItem, evt.getOldValue())
                            ? updatedName : selectedItem);
                } finally {
                    performActionOnSetSelectedItem = oldValue;
                }
            }
        }
    }

    /**
     * Returns the action's string-valued name property.
     *
     * @param action action to inspect
     * @return the action name
     * @throws IllegalArgumentException if the name property is not a string
     */
    private static String actionName(Action action) {
        Object name = action.getValue(Action.NAME);
        if (!(name instanceof String stringName)) {
            throw new IllegalArgumentException("Actions must have a non-null String name.");
        }
        return stringName;
    }
}