/*
 * $Id: ActionContainerFactory.java,v 1.6 2005/10/10 18:02:43 rbair Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jdesktop.swingx.action;

import java.awt.Insets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * Creates user interface elements based on action ids and lists of action ids.
 * All action ids must represent actions managed by the ActionManager.
 * <p>
 * <h3>Action Lists</h3>
 * Use the createXXX(List) methods to construct containers of actions like menu 
 * bars, menus, popups and toolbars from actions represented as action ids in a 
 * <i>java.util.List</i>. Each element in the action-list can be one of 3 types:
 * <ul>
 * <li>action id: corresponds to an action managed by the ActionManager
 * <li>null: indicates a separator should be inserted.
 * <li>java.util.List: represents a submenu. See the note below which describes 
 * the configuration of menus. 
 * </li>
 * The order of elements in an action-list determines the arrangement of the ui 
 * components which are contructed from the action-list.
 * <p>
 * For a menu or submenu, the first element in the action-list represents a menu 
 * and subsequent elements represent menu items or separators (if null). 
 * <p>
 * This class can be used as a general component factory which will construct
 * components from Actions if the <code>create&lt;comp&gt;(Action,...)</code>
 * methods are used.
 *
 * @see ActionManager
 */
public class ActionContainerFactory {
    /**
     * Standard margin for toolbar buttons to improve their look
     */
    private static Insets TOOLBAR_BUTTON_MARGIN = new Insets(1, 1, 1, 1);
    
    private ActionManager manager;

    // Map between group id + component and the ButtonGroup
    private Map groupMap;

    /**
     * Constructs an container factory which uses managed actions.
     *
     * @param manager use the actions managed with this manager for
     *                constructing ui componenents.
     */
    public ActionContainerFactory(ActionManager manager) {
        setActionManager(manager);
    }

    /**
     * Gets the ActionManager instance. If the ActionManager has not been explicitly
     * set then the default ActionManager instance will be used.
     *
     * @return the ActionManager used by the ActionContainerFactory.
     * @see #setActionManager
     */
    public ActionManager getActionManager() {
        if (manager == null) {
            manager = ActionManager.getInstance();
        }
        return manager;
    }

    /**
     * Sets the ActionManager instance that will be used by this
     * ActionContainerFactory
     */
    public void setActionManager(ActionManager manager) {
        ActionManager oldManager = this.manager;
        if (oldManager != null) {
            oldManager.setFactory(null);
        }
        this.manager = manager;
        
        if (manager != null) {
            manager.setFactory(this);
        }
    }

    /**
     * Constructs a toolbar from an action-list id. By convention,
     * the identifier of the main toolbar should be "main-toolbar"
     *
     * @param list a list of action ids used to construct the toolbar.
     * @return the toolbar or null
     */
    private JToolBar createToolBar(Object[] list) {
        return createToolBar(Arrays.asList(list));
    }

    /**
     * Constructs a toolbar from an action-list id. By convention,
     * the identifier of the main toolbar should be "main-toolbar"
     *
     * @param list a list of action ids used to construct the toolbar.
     * @return the toolbar or null
     */
    public JToolBar createToolBar(List list) {
        JToolBar toolbar = new JToolBar();
        Iterator iter = list.iterator();
        while(iter.hasNext()) {
            Object element = iter.next();

            if (element == null) {
                toolbar.addSeparator();
            } else {
                AbstractButton button = createButton(element, toolbar);
                // toolbar buttons shouldn't steal focus
                button.setFocusable(false);
                /*
                 * TODO
                 * The next two lines improve the default look of the buttons.
                 * This code should be changed to retrieve the default look
                 * from some UIDefaults object.
                 */
                button.setMargin(TOOLBAR_BUTTON_MARGIN);
                button.setBorderPainted(false);
                
                toolbar.add(button);
            }
        }
        return toolbar;
    }


    /**
     * Constructs a popup menu from an array of action ids.  
     *
     * @param list an array of action ids used to construct the popup.
     * @return the popup or null
     */
    private JPopupMenu createPopup(Object[] list) {
        return createPopup(Arrays.asList(list));
    }

    /**
     * Constructs a popup menu from a list of action ids.
     *
     * @param list a list of action ids used to construct the popup.
     * @return the popup or null
     */
    public JPopupMenu createPopup(List list) {
        JPopupMenu popup = new JPopupMenu();
        Iterator iter = list.iterator();
        while(iter.hasNext()) {
            Object element = iter.next();

            if (element == null) {
                popup.addSeparator();
            } else if (element instanceof List) {
                JMenu newMenu= createMenu((List)element);
                if (newMenu!= null) {
                    popup.add(newMenu);
                }
            } else {
                popup.add(createMenuItem(element, popup));
            }
        }
        return popup;
    }

    /**
     * Constructs a menu tree from a list of actions or lists of lists or actions.
     * TODO This method is broken. It <em>should</em> expect either that every
     * entry is a List (thus, the sub menus off the main MenuBar), or it should
     * handle normal actions properly. By submitting a List of all Actions, nothing
     * is created....
     * <p>
     * For example, If my list is [action, action, action], then nothing is added
     * to the menu bar. However, if my list is [list[action], action, action, action] then
     * I get a menu and under it the tree actions. This should not be, because if I
     * wanted those actions to be on the sub menu, then they should have been
     * listed within the sub list!
     *
     * @param list a list which represents the root item.
     * @return a menu bar which represents the menu bar tree
     */
    public JMenuBar createMenuBar(List list) {
        JMenuBar menubar = new JMenuBar();
        JMenu menu = null;

        Iterator iter = list.iterator();
        while(iter.hasNext()) {
            Object element = iter.next();

            if (element == null) {
                if (menu != null) {
                    menu.addSeparator();
                }
            } else if (element instanceof List) {
                menu = createMenu((List)element);
                if (menu != null) {
                    menubar.add(menu);
                }
            } else  {
                if (menu != null) {
                    menu.add(createMenuItem(element, menu));
                }
            }
        }
        return menubar;
    }


    /**
     * Creates and returns a menu from a List which represents actions, separators
     * and sub-menus. The menu
     * constructed will have the attributes from the first action in the List.
     * Subsequent actions in the list represent menu items.
     *
     * @param list a list of action ids used to construct the menu and menu items.
     *             the first element represents the action used for the menu,
     * @return the constructed JMenu or null
     */
    public JMenu createMenu(List list) {
        // The first item will be the action for the JMenu
        Action action = getAction(list.get(0));
        if (action == null) {
            return null;
        }
        JMenu menu = new JMenu(action);

        // The rest of the items represent the menu items.
        Iterator iter = list.listIterator(1);
        while(iter.hasNext()) {
            Object element = iter.next();
            if (element == null) {
                menu.addSeparator();
            } else if (element instanceof List) {
                JMenu newMenu = createMenu((List)element);
                if (newMenu != null) {
                    menu.add(newMenu);
                }
            } else  {
                menu.add(createMenuItem(element, menu));
            }
        }
        return menu;
    }


    /**
     * Convenience method to get the action from an ActionManager.
     */
    private Action getAction(Object id) {
        Action action = getActionManager().getAction(id);
        if (action == null) {
            throw new RuntimeException("ERROR: No Action for " + id);
        }
        return action;
    }

    /**
     * Returns the button group corresponding to the groupid
     *
     * @param groupid the value of the groupid attribute for the action element
     * @param container a container which will further identify the ButtonGroup
     */
    private ButtonGroup getGroup(String groupid, JComponent container) {
        if (groupMap == null) {
            groupMap = new HashMap();
        }
        int intCode = groupid.hashCode();
        if (container != null) {
            intCode ^= container.hashCode();
        }
        Integer hashCode = new Integer(intCode);

        ButtonGroup group = (ButtonGroup)groupMap.get(hashCode);
        if (group == null) {
            group = new ButtonGroup();
            groupMap.put(hashCode, group);
        }
        return group;
    }

    /**
     * Creates a menu item based on the attributes of the action element.
     * Will return a JMenuItem, JRadioButtonMenuItem or a JCheckBoxMenuItem
     * depending on the context of the Action.
     *
     * @return a JMenuItem or subclass depending on type.
     */
    private JMenuItem createMenuItem(Object id, JComponent container) {
        return createMenuItem(getAction(id), container);
    }


    /**
     * Creates a menu item based on the attributes of the action element.
     * Will return a JMenuItem, JRadioButtonMenuItem or a JCheckBoxMenuItem
     * depending on the context of the Action.
     *
     * @param action a mangaged Action
     * @param container the parent container may be null for non-group actions.
     * @return a JMenuItem or subclass depending on type.
     */
    public JMenuItem createMenuItem(Action action, JComponent container) {
        JMenuItem menuItem = null;
        if (action instanceof AbstractActionExt) {
            AbstractActionExt ta = (AbstractActionExt)action;

            if (ta.isStateAction()) {
                String groupid = (String)ta.getGroup();
                if (groupid != null) {
                    // If this action has a groupid attribute then it's a
                    // GroupAction
                    menuItem = createRadioButtonMenuItem(getGroup(groupid, container),
                                                         (AbstractActionExt)action);
                } else {
                    menuItem = createCheckBoxMenuItem((AbstractActionExt)action);
                }
            }
        }

        if (menuItem == null) {
            menuItem= new JMenuItem(action);
            configureMenuItem(menuItem, action);
        }
        return menuItem;
    }

    /**
     * Creates a menu item based on the attributes of the action.
     * Will return a JMenuItem, JRadioButtonMenuItem or a JCheckBoxMenuItem
     * depending on the context of the Action.
     *
     * @param action an action used to create the menu item
     * @return a JMenuItem or subclass depending on type.
     */
    public JMenuItem createMenuItem(Action action) {
        return createMenuItem(action, null);
    }


    /**
     * Creates a button based on the attributes of the action element.
     * Will return a JButton or a JToggleButton.
     */
    private AbstractButton createButton(Object id, JComponent container) {
        return createButton(getAction(id), container);
    }

    /**
     * Creates a button based on the attributes of the action. If the container
     * parameter is non-null then it will be used to uniquely identify
     * the returned component within a ButtonGroup. If the action doesn't
     * represent a grouped component then this value can be null.
     *
     * @param action an action used to create the button
     * @param container the parent container to uniquely identify
     *        grouped components or null
     * @return will return a JButton or a JToggleButton.
     */
    public AbstractButton createButton(Action action, JComponent container) {
        if (action == null) {
            return null;
        }

        AbstractButton button = null;
        if (action instanceof AbstractActionExt) {
            // Check to see if we should create a toggle button
            AbstractActionExt ta = (AbstractActionExt)action;

            if (ta.isStateAction()) {
                // If this action has a groupid attribute then it's a
                // GroupAction
                String groupid = (String)ta.getGroup();
                if (groupid == null) {
                    button = createToggleButton(ta);
                } else {
                    button = createToggleButton(ta, getGroup(groupid, container));
                }
            }
        }

        if (button == null) {
            // Create a regular button
            button = new JButton(action);
            configureButton(button, action);
        }
        return button;
    }

    /**
     * Creates a button based on the attributes of the action.
     *
     * @param action an action used to create the button
     * @return will return a JButton or a JToggleButton.
     */
    public AbstractButton createButton(Action action)  {
        return createButton(action, null);
    }

    /**
     * Adds and configures a toggle button.
     * @param a an abstraction of a toggle action.
     */
    private JToggleButton createToggleButton(AbstractActionExt a)  {
        return createToggleButton(a, null);
    }

    /**
     * Adds and configures a toggle button.
     * @param a an abstraction of a toggle action.
     * @param group the group to add the toggle button or null
     */
    private JToggleButton createToggleButton(AbstractActionExt a, ButtonGroup group)  {
        JToggleButton button = new JToggleButton();
        configureButton(button, a, group);
        return button;
    }

    /**
     * 
     * @param button
     * @param a
     * @param group
     * @return
     */
    public void configureButton(JToggleButton button, AbstractActionExt a, ButtonGroup group) {
        button.setAction(a);
        button.addItemListener(a);
        button.setSelected(a.isSelected());
        if (group != null) {
            group.add(button);
        }
        configureToggleButton(button, a);
    }

    /**
     * This method will be called after toggle buttons are created.
     * Override for custom configuration but the overriden method should be called
     * first.
     *
     * @param button the button to be configured
     * @param action the action used to construct the menu item.
     */
    protected void configureToggleButton(JToggleButton button, Action action) {
        configureButton(button, action);

        // The PropertyChangeListener that gets added
        // to the Action doesn't know how to handle the "selected" property change
        // in the meantime, the corect thing to do is to add another PropertyChangeListener
        // to the AbstractActionExt until this is fixed.
        action.addPropertyChangeListener(new ToggleActionPropertyChangeListener(button));
    }


    /**
     * This method will be called after buttons created from an action.
     * Override for custom configuration.
     *
     * @param button the button to be configured
     * @param action the action used to construct the menu item.
     */
    protected void configureButton(AbstractButton button, Action action)  {
        if (action.getValue(Action.SHORT_DESCRIPTION) == null) {
            button.setToolTipText((String)action.getValue(Action.NAME));
        }
        // Use the large icon for toolbar buttons.
        if (action.getValue(AbstractActionExt.LARGE_ICON) != null) {
            button.setIcon((Icon)action.getValue(AbstractActionExt.LARGE_ICON));
        }
        // Don't show the text under the toolbar buttons if they have an icon
        if (button.getIcon() != null) {
            button.setText("");
        }
    }

    /**
     * This method will be called after toggle type menu items (like
     * JRadioButtonMenuItem and JCheckBoxMenuItem) are created.
     * Override for custom configuration but the overriden method should be called
     * first.
     *
     * @param menuItem the menu item to be configured
     * @param action the action used to construct the menu item.
     */
    protected void configureToggleMenuItem(JMenuItem menuItem, Action action) {
        configureMenuItem(menuItem, action);

        // The PropertyChangeListener that gets added
        // to the Action doesn't know how to handle the "selected" property change
        // in the meantime, the corect thing to do is to add another PropertyChangeListener
        // to the AbstractActionExt until this is fixed.
        action.addPropertyChangeListener(new ToggleActionPropertyChangeListener(menuItem));
    }


    /**
     * This method will be called after menu items are created.
     * Override for custom configuration.
     *
     * @param menuItem the menu item to be configured
     * @param action the action used to construct the menu item.
     */
    protected void configureMenuItem(JMenuItem menuItem, Action action) {
    }

    /**
     * Helper method to add a checkbox menu item.
     */
    private JCheckBoxMenuItem createCheckBoxMenuItem(AbstractActionExt a) {
        JCheckBoxMenuItem mi = new JCheckBoxMenuItem(a);
        mi.addItemListener(a);
        mi.setSelected(a.isSelected());

        configureToggleMenuItem(mi, a);
        return mi;
    }

    /**
     * Helper method to add a radio button menu item.
     */
    private JRadioButtonMenuItem createRadioButtonMenuItem(ButtonGroup group,
                                                                  AbstractActionExt a)  {
        JRadioButtonMenuItem mi = new JRadioButtonMenuItem(a);
        mi.addItemListener(a);
        mi.setSelected(a.isSelected());
        if (group != null) {
            group.add(mi);
        }
        configureToggleMenuItem(mi, a);
        return mi;
    }
}
