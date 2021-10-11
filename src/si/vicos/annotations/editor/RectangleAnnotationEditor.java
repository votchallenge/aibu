package si.vicos.annotations.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.JComponent;

import org.coffeeshop.swing.figure.FigurePanel;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer;

/**
 * The Class RectangleAnnotationEditor.
 */
public class RectangleAnnotationEditor extends AnnotationEditor {

	/** The Constant FACTORY. */
	public static final AnnotationEditorFactory<RectangleAnnotationEditor> FACTORY = new AnnotationEditorFactory<RectangleAnnotationEditor>() {

		@Override
		public String getName() {
			return "Rectangle";
		}

		@Override
		public RectangleAnnotationEditor getEditor(AnnotationPeer peer,
				Color color) {
			return new RectangleAnnotationEditor(peer, color);
		}

	};

	/** The points. */
	private Vector<Point> points = new Vector<Point>();

	/** The shape. */
	private Rectangle shape;

	/** The working shape. */
	private Rectangle workingShape = null;

	/**
	 * Instantiates a new rectangle annotation editor.
	 * 
	 * @param peer
	 *            the peer
	 * @param color
	 *            the color
	 */
	public RectangleAnnotationEditor(AnnotationPeer peer, Color color) {
		super(peer, color);
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
		g.draw(shape);

		if (isSelected() && workingShape != null) {
			g.setColor(Color.BLACK);
			g.setStroke(selectedStroke);
			g.setXORMode(Color.WHITE);
			g.draw(workingShape);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationEditor#resetInput()
	 */
	@Override
	public void resetInput() {

		workingShape = null;
		points.clear();
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
		RectangleAnnotation annotation = getAnnotation();

		if (annotation == null)
			return null;

		if (drag) {

			if (points.size() == 2) {
				int x = to.x - from.x;
				int y = to.y - from.y;
				points.elementAt(0).x += x;
				points.elementAt(0).y += y;
				points.elementAt(1).x += x;
				points.elementAt(1).y += y;

				Point p1 = points.elementAt(0);
				Point p2 = points.elementAt(1);
				workingShape = new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y
						- p1.y);

				notifyRepaint();
				return dragCursor;
			}

			if (points.size() == 0) {

				if (!annotation.isNull() && shape.contains(from) && from == to) {
					points.add(new Point((int) annotation.getX(),
							(int) annotation.getY()));
					points.add(new Point((int) annotation.getX()
							+ (int) annotation.getWidth(), (int) annotation
							.getY() + (int) annotation.getHeight()));
					notifyRepaint();
					return dragCursor;
				}
				return defaultCursor;
			}

			return pointCursor;

		} else {

			if (points.size() == 0) {
				if (!annotation.isNull() && shape.contains(to))
					return dragPossibleCursor;
				return defaultCursor;
			}

			Point p = points.elementAt(0);
			int x1 = Math.min(p.x, to.x);
			int y1 = Math.min(p.y, to.y);
			int x2 = Math.max(p.x, to.x);
			int y2 = Math.max(p.y, to.y);
			workingShape = new Rectangle(x1, y1, x2 - x1, y2 - y1);
			notifyRepaint();
			return pointCursor;
		}
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
		RectangleAnnotation annotation = getAnnotation();

		if (annotation == null)
			return;

		if (points.size() < 2 && position != null)
			points.add(position);

		if (points.size() == 2) {
			Point p1 = points.elementAt(0);
			Point p2 = points.elementAt(1);
			annotation = new RectangleAnnotation(p1.x, p1.y, p2.x - p1.x, p2.y
					- p1.y);
			peer.setAnnotation(annotation);

			updateGraphics();
			resetInput();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationViewer#updateGraphics()
	 */
	@Override
	public void updateGraphics() {
		RectangleAnnotation annotation = getAnnotation();

		if (annotation == null)
			return;

		shape = new Rectangle((int) annotation.getX(), (int) annotation.getY(),
				(int) annotation.getWidth(), (int) annotation.getHeight());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationEditor#reset()
	 */
	@Override
	public void reset() {
		peer.setAnnotation(new RectangleAnnotation());
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
		RectangleAnnotation annotation = getAnnotation();

		if (annotation != null && shape.contains(position))
			return String.format("(%.1f, %.1f) [%.1f, %.1f]",
					annotation.getX(), annotation.getY(),
					annotation.getWidth(), annotation.getHeight());
		return null;
	}

	/**
	 * Gets the annotation.
	 * 
	 * @return the annotation
	 */
	private RectangleAnnotation getAnnotation() {

		Annotation a = peer.getAnnotation();

		if (a == null || !(a instanceof RectangleAnnotation))
			return new RectangleAnnotation();

		return (RectangleAnnotation) a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationEditor#getCurrent()
	 */
	@Override
	public Annotation getCurrent() {
		if (points.size() == 2) {
			Point p1 = points.elementAt(0);
			Point p2 = points.elementAt(1);
			return new RectangleAnnotation(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
		}

		return getAnnotation();
	}

}