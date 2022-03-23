package si.vicos.annotations.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JComponent;

import org.coffeeshop.swing.figure.FigurePanel;

import si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer;

/**
 * The Class AnnotationViewer.
 */
public abstract class AnnotationViewer {

	/**
	 * Instantiates a new annotation viewer.
	 * 
	 * @param peer
	 *            the peer
	 * @param color
	 *            the color
	 */
	public AnnotationViewer(AnnotationPeer peer, Color color) {
		this.peer = peer;
		this.color = color;

		updateGraphics();
	}

	/** The color. */
	protected Color color;

	/** The peer. */
	protected AnnotationPeer peer;

	/** The selected. */
	private boolean selected;

	/**
	 * Notify repaint.
	 */
	protected void notifyRepaint() {
		peer.repaint();
	}

	/**
	 * Update graphics.
	 */
	public abstract void updateGraphics();

	/**
	 * Paint.
	 * 
	 * @param g
	 *            the g
	 */
	public abstract void paint(Graphics2D g);

	/**
	 * Gets the component.
	 * 
	 * @return the component
	 */
	public abstract JComponent getComponent();

	/**
	 * Gets the tool tip.
	 * 
	 * @param source
	 *            the source
	 * @param position
	 *            the position
	 * @return the tool tip
	 */
	public abstract String getToolTip(FigurePanel source, Point position);

	/**
	 * Gets the color.
	 * 
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the selected.
	 * 
	 * @param selected
	 *            the new selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
		notifyRepaint();
	}

	/**
	 * Checks if is selected.
	 * 
	 * @return true, if is selected
	 */
	public boolean isSelected() {
		return selected;
	}

}
