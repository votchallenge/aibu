package si.vicos.annotations.editor;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToggleButton;

/**
 * The Class ToolbarToggleButton.
 */
public class ToolbarToggleButton extends JToggleButton {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new toolbar toggle button.
	 * 
	 * @param action
	 *            the action
	 */
	public ToolbarToggleButton(Action action) {
		super(action);

		if (action != null
				&& (action.getValue(Action.SMALL_ICON) != null || action
						.getValue(Action.LARGE_ICON_KEY) != null)) {
			setHideActionText(true);
		}
		setHorizontalTextPosition(JButton.CENTER);
		setVerticalTextPosition(JButton.BOTTOM);

	}

}
