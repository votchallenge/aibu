package si.vicos.annotations.editor.tracking;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.coffeeshop.swing.figure.AbstractFigure;
import org.coffeeshop.swing.figure.FigureObserver;
import org.coffeeshop.swing.viewers.FigureViewer;
import org.coffeeshop.swing.viewers.Viewable;

/**
 * The Class BackgroundFigure.
 */
public abstract class BackgroundFigure extends AbstractFigure implements
		Viewable {

	/** The image to screen. */
	private AffineTransform imageToScreen = new AffineTransform();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.figure.Figure#paint(java.awt.Graphics2D,
	 * java.awt.geom.Rectangle2D, java.awt.Rectangle,
	 * org.coffeeshop.swing.figure.FigureObserver)
	 */
	public final void paint(Graphics2D g, Rectangle2D figureSize,
			Rectangle windowSize, FigureObserver observer) {

		/*
		 * g.setClip(windowSize.x, windowSize.y, windowSize.width,
		 * windowSize.height);
		 */

		float scale = (float) windowSize.width / (float) figureSize.getWidth();

		float offsetX = (float) figureSize.getX() * scale
				- (float) windowSize.x;
		float offsetY = (float) figureSize.getY() * scale
				- (float) windowSize.y;

		imageToScreen.setTransform(scale, 0, 0, scale, -offsetX, -offsetY);

		AffineTransform oldT = g.getTransform();
		AffineTransform newT = new AffineTransform(oldT);
		newT.concatenate(imageToScreen);
		g.setTransform(newT);

		paintGeometry(g, scale, (float) figureSize.getWidth(),
				(float) figureSize.getHeight(), observer);

		g.setTransform(oldT);

	}

	/**
	 * Paint geometry.
	 * 
	 * @param g
	 *            the g
	 * @param scale
	 *            the scale
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param observer
	 *            the observer
	 */
	protected abstract void paintGeometry(Graphics2D g, float scale,
			float width, float height, FigureObserver observer);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.viewers.Viewable#view(java.util.Map)
	 */
	public boolean view(Map<String, String> parameters) {
		String title = parameters.get("title");
		if (title == null) {
			title = getName();
		}

		FigureViewer viewer = new FigureViewer(title, this);
		viewer.setVisible(true);

		return true;
	}
}
