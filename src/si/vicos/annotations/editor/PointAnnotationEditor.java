package si.vicos.annotations.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JComponent;

import org.coffeeshop.swing.figure.FigurePanel;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.PointAnnotation;
import si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer;

/**
 * The Class PointAnnotationEditor.
 */
public class PointAnnotationEditor extends AnnotationEditor {

	/** The shape. */
	private Point shape;

	/** The working point. */
	private Point workingPoint = null;

	/** The radius. */
	private double radius;

	/** The Constant FACTORY. */
	public static final AnnotationEditorFactory<PointAnnotationEditor> FACTORY = new AnnotationEditorFactory<PointAnnotationEditor>() {

		@Override
		public String getName() {
			return "Point";
		}

		@Override
		public PointAnnotationEditor getEditor(AnnotationPeer peer, Color color) {
			return new PointAnnotationEditor(peer, color);
		}

	};

	/**
	 * Instantiates a new point annotation editor.
	 * 
	 * @param peer
	 *            the peer
	 * @param color
	 *            the color
	 */
	public PointAnnotationEditor(AnnotationPeer peer, Color color) {
		this(peer, color, 3);
	}

	/**
	 * Instantiates a new point annotation editor.
	 * 
	 * @param peer
	 *            the peer
	 * @param color
	 *            the color
	 * @param radius
	 *            the radius
	 */
	public PointAnnotationEditor(AnnotationPeer peer, Color color, double radius) {
		super(peer, color);
		this.radius = radius;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationViewer#getComponent()
	 */
	@Override
	public JComponent getComponent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.AnnotationViewer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g) {

		if (shape == null)
			return;

		g.setColor(color);
		g.setStroke(isSelected() ? selectedStroke : normalStroke);
		g.drawOval(shape.x - (int) radius, shape.y - (int) radius,
				2 * (int) radius, 2 * (int) radius);

		if (isSelected() && workingPoint != null) {
			g.setColor(Color.BLACK);
			g.setStroke(selectedStroke);
			g.setXORMode(Color.WHITE);
			g.drawOval(workingPoint.x - (int) radius, workingPoint.y
					- (int) radius, 2 * (int) radius, 2 * (int) radius);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationEditor#resetInput()
	 */
	@Override
	public void resetInput() {

		workingPoint = null;
		notifyRepaint();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.AnnotationEditor#onMove(org.coffeeshop.swing
	 * .figure.FigurePanel, java.awt.Point, java.awt.Point, boolean, int)
	 */
	public Cursor onMove(FigurePanel source, Point from, Point to,
			boolean drag, int modifiers) {

		PointAnnotation annotation = getAnnotation();
		if (annotation == null)
			return null;

		if (drag) {

			if (workingPoint != null) {
				int x = to.x - from.x;
				int y = to.y - from.y;

				workingPoint
						.setLocation(workingPoint.x + x, workingPoint.y + y);

				notifyRepaint();
				return dragCursor;
			}

			if (!annotation.isNull() && contains(from) && from == to) {
				workingPoint = new Point((int) annotation.getX(),
						(int) annotation.getY());

				notifyRepaint();
				return dragCursor;
			}

			return defaultCursor;

		} else {

			if (!annotation.isNull() && contains(to))
				return dragPossibleCursor;
			return defaultCursor;

		}
	}

	/**
	 * Contains.
	 * 
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	private boolean contains(Point p) {

		return shape.distance(p) < radius;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.AnnotationEditor#onClick(org.coffeeshop.swing
	 * .figure.FigurePanel, java.awt.Point, int, int)
	 */
	public void onClick(FigurePanel source, Point position, int clicks,
			int modifiers) {
		PointAnnotation annotation = getAnnotation();

		if (annotation == null)
			return;

		if (position == null) {
			if (workingPoint != null)
				peer.setAnnotation(new PointAnnotation(workingPoint.x,
						workingPoint.y));
		} else
			peer.setAnnotation(new PointAnnotation(position.x, position.y));

		updateGraphics();
		resetInput();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationViewer#updateGraphics()
	 */
	@Override
	public void updateGraphics() {
		PointAnnotation annotation = getAnnotation();

		if (annotation == null)
			return;

		shape = new Point((int) annotation.getX(), (int) annotation.getY());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationEditor#reset()
	 */
	@Override
	public void reset() {

		resetInput();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.AnnotationViewer#getToolTip(org.coffeeshop
	 * .swing.figure.FigurePanel, java.awt.Point)
	 */
	@Override
	public String getToolTip(FigurePanel source, Point position) {
		PointAnnotation annotation = getAnnotation();

		if (annotation != null && contains(position))
			return String.format("(%.1f, %.1f)", annotation.getX(),
					annotation.getY());
		return null;
	}

	/**
	 * Gets the annotation.
	 * 
	 * @return the annotation
	 */
	private PointAnnotation getAnnotation() {

		Annotation a = peer.getAnnotation();

		if (a == null || !(a instanceof PointAnnotation))
			return null;

		return (PointAnnotation) a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationEditor#getCurrent()
	 */
	@Override
	public Annotation getCurrent() {
		if (workingPoint != null)
			return new PointAnnotation(workingPoint.x, workingPoint.y);

		return getAnnotation();
	}

}