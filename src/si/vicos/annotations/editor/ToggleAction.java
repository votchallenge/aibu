package si.vicos.annotations.editor;

import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.Icon;

import org.coffeeshop.swing.ToolTipAction;

/**
 * The Class ToggleAction.
 */
public abstract class ToggleAction extends ToolTipAction {

	/**
	 * The Class ToggleActionGroup.
	 */
	public static class ToggleActionGroup {

		/** The actions. */
		private Vector<ToggleAction> actions = new Vector<ToggleAction>();

		/**
		 * Instantiates a new toggle action group.
		 */
		public ToggleActionGroup() {

		}

		/**
		 * Adds the.
		 * 
		 * @param action
		 *            the action
		 */
		private void add(ToggleAction action) {

			actions.add(action);

		}

		/**
		 * Select.
		 * 
		 * @param select
		 *            the select
		 */
		public void select(String select) {

			if (select == null)
				return;

			for (ToggleAction action : actions) {

				String identifier = (String) action.getValue(Action.NAME);

				if (identifier != null && identifier.compareTo(select) == 0) {

					select(action);
					return;

				}

			}

		}

		/**
		 * Select.
		 * 
		 * @param select
		 *            the select
		 */
		public void select(ToggleAction select) {

			if (!actions.contains(select))
				return;

			ActionEvent event = new ActionEvent(this, 0, "internal");

			selectWithEvent(select, event);

		}

		/**
		 * Select with event.
		 * 
		 * @param select
		 *            the select
		 * @param event
		 *            the event
		 */
		private void selectWithEvent(ToggleAction select, ActionEvent event) {

			for (ToggleAction action : actions) {

				if (action == select) {
					action.putValue(Action.SELECTED_KEY, true);
					action.actionSelected(event);
				} else {
					action.putValue(Action.SELECTED_KEY, false);
					action.actionDeselected(event);
				}

			}

		}

	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The group. */
	private ToggleActionGroup group;

	/**
	 * Instantiates a new toggle action.
	 * 
	 * @param title
	 *            the title
	 * @param icon
	 *            the icon
	 * @param group
	 *            the group
	 */
	public ToggleAction(String title, Icon icon, ToggleActionGroup group) {
		super(title, icon);
		putValue(Action.SELECTED_KEY, false);

		this.group = group;
		if (this.group != null)
			this.group.add(this);
	}

	/**
	 * Instantiates a new toggle action.
	 * 
	 * @param title
	 *            the title
	 * @param iconId
	 *            the icon id
	 * @param group
	 *            the group
	 */
	public ToggleAction(String title, String iconId, ToggleActionGroup group) {
		super(title, iconId);
		putValue(Action.SELECTED_KEY, false);

		this.group = group;
		if (this.group != null)
			this.group.add(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {

		boolean selected = (Boolean) getValue(Action.SELECTED_KEY);

		if (selected) {

			if (group == null) {
				actionSelected(arg0);
			} else {
				group.selectWithEvent(this, arg0);
			}

		} else {

			if (group == null) {

				actionDeselected(arg0);

			} else {

				// Cannot manually deselect action in group
				putValue(Action.SELECTED_KEY, true);

			}

		}

	}

	/**
	 * Action selected.
	 * 
	 * @param e
	 *            the e
	 */
	public abstract void actionSelected(ActionEvent e);

	/**
	 * Action deselected.
	 * 
	 * @param e
	 *            the e
	 */
	public abstract void actionDeselected(ActionEvent e);
}
