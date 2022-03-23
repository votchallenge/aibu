package si.vicos.annotations.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.JComponent;

import org.coffeeshop.swing.figure.FigurePanel;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.PolygonAnnotation;
import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.ShapeAnnotation;
import si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer;
import si.vicos.annotations.editor.tracking.PolygonAnnotationRenderer;

/**
 * The Class RotatedRectangleAnnotationEditor.
 */
public class RotatedRectangleAnnotationEditor extends AnnotationEditor {

	/**
	 * The Enum ManipulationMode.
	 */
	public enum ManipulationMode {
		/** The set. */
		SET, /** The move. */
		MOVE, /** The rotate. */
		ROTATE, /** The scale. */
		SCALE, /** The rotate scale. */
		ROTATE_SCALE
	};

	/** The Constant FACTORY. */
	public static final AnnotationEditorFactory<RotatedRectangleAnnotationEditor> FACTORY = new AnnotationEditorFactory<RotatedRectangleAnnotationEditor>() {

		@Override
		public String getName() {
			return "Rotated rectangle";
		}

		@Override
		public RotatedRectangleAnnotationEditor getEditor(AnnotationPeer peer,
				Color color) {
			return new RotatedRectangleAnnotationEditor(peer, color);
		}

	};

	/** The points. */
	private Vector<Point2D> points = new Vector<Point2D>();

	/** The shape. */
	private Shape shape;

	/** The working shape. */
	private Shape workingShape = null;

	/** The mode. */
	private ManipulationMode mode = ManipulationMode.SET;

	// private Point2D edited

	/**
	 * Instantiates a new rotated rectangle annotation editor.
	 * 
	 * @param peer
	 *            the peer
	 * @param color
	 *            the color
	 */
	public RotatedRectangleAnnotationEditor(AnnotationPeer peer, Color color) {
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
		mode = ManipulationMode.SET;
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

		PolygonAnnotation annotation = getAnnotation();

		if (annotation == null)
			return null;

		if (drag) {

			if (points.size() > 2) {
				switch (mode) {
				case MOVE: {
					int vx = to.x - from.x;
					int vy = to.y - from.y;

					for (int i = 0; i < points.size(); i++) {
						Point2D p = points.elementAt(i);
						points.set(
								i,
								new Point2D.Double(p.getX() + vx, p.getY() + vy));
					}

					workingShape = points.isEmpty() ? null
							: PolygonAnnotationRenderer.pointsToShape(points);

					break;
				}
				case ROTATE: {
					Point2D pivot = annotation.getCenter();

					double angle = Math.atan2(to.getY() - pivot.getY(),
							to.getX() - pivot.getX())
							- Math.atan2(from.getY() - pivot.getY(),
									from.getX() - pivot.getX());

					for (int i = 0; i < points.size(); i++) {
						Point2D p = points.get(i);
						double x = ((p.getX() - pivot.getX()) * Math.cos(angle))
								- ((p.getY() - pivot.getY()) * Math.sin(angle))
								+ pivot.getX();
						double y = ((p.getX() - pivot.getX()) * Math.sin(angle))
								+ ((p.getY() - pivot.getY()) * Math.cos(angle))
								+ pivot.getY();
						points.set(i, new Point2D.Double(x, y));
					}

					workingShape = points.isEmpty() ? null
							: PolygonAnnotationRenderer.pointsToShape(points);

					break;
				}
				case SCALE: {
					Point2D pivot = annotation.getCenter();
					double scale = pivot.distance(to) / pivot.distance(from);

					for (int i = 0; i < points.size(); i++) {
						Point2D p = points.get(i);
						double x = ((p.getX() - pivot.getX()) * scale)
								+ pivot.getX();
						double y = ((p.getY() - pivot.getY()) * scale)
								+ pivot.getY();
						points.set(i, new Point2D.Double(x, y));
					}

					workingShape = points.isEmpty() ? null
							: PolygonAnnotationRenderer.pointsToShape(points);

					break;
				}
				case ROTATE_SCALE: {
					Point2D pivot = annotation.getCenter();

					double scale = pivot.distance(to) / pivot.distance(from);
					double angle = Math.atan2(to.getY() - pivot.getY(),
							to.getX() - pivot.getX())
							- Math.atan2(from.getY() - pivot.getY(),
									from.getX() - pivot.getX());

					for (int i = 0; i < points.size(); i++) {
						Point2D p = points.get(i);
						double x = ((p.getX() - pivot.getX()) * Math.cos(angle))
								- ((p.getY() - pivot.getY()) * Math.sin(angle))
								+ pivot.getX();
						double y = ((p.getX() - pivot.getX()) * Math.sin(angle))
								+ ((p.getY() - pivot.getY()) * Math.cos(angle))
								+ pivot.getY();
						x = ((x - pivot.getX()) * scale) + pivot.getX();
						y = ((y - pivot.getY()) * scale) + pivot.getY();
						points.set(i, new Point2D.Double(x, y));
					}

					workingShape = points.isEmpty() ? null
							: PolygonAnnotationRenderer.pointsToShape(points);

					break;
				}
				case SET: {
				}
				}

				notifyRepaint();
				return dragCursor;
			}

			if (points.size() == 0) {

				if (!annotation.isNull() && shape.contains(from) && from == to) {
					points.clear();
					points.addAll(annotation.getPolygon());

					if ((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0)
						mode = ManipulationMode.ROTATE_SCALE;
					else
						mode = ManipulationMode.MOVE;

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

			Point2D p = points.elementAt(0);
			int x1 = (int) Math.min(p.getX(), to.x);
			int y1 = (int) Math.min(p.getY(), to.y);
			int x2 = (int) Math.max(p.getX(), to.x);
			int y2 = (int) Math.max(p.getY(), to.y);
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

		PolygonAnnotation annotation = getAnnotation();

		if (annotation == null)
			return;

		if (mode == ManipulationMode.SET) {

			if (points.size() < 2 && position != null)
				points.add(position);

			if (points.size() == 2) {
				Point2D p1 = points.elementAt(0);
				Point2D p2 = points.elementAt(1);
				RectangleAnnotation a = new RectangleAnnotation(p1.getX(),
						p1.getY(), p2.getX() - p1.getX(), p2.getY() - p1.getY());
				annotation = new PolygonAnnotation(a.getPolygon());
				peer.setAnnotation(annotation);

				updateGraphics();
				resetInput();
			}

		} else {

			if (points.size() > 2) {
				annotation = new PolygonAnnotation(points);
				peer.setAnnotation(annotation);
				updateGraphics();
				resetInput();
				points.clear();
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationViewer#updateGraphics()
	 */
	@Override
	public void updateGraphics() {
		PolygonAnnotation annotation = getAnnotation();

		if (annotation == null)
			return;

		shape = PolygonAnnotationRenderer.annotationToShape(annotation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationEditor#reset()
	 */
	@Override
	public void reset() {

		peer.setAnnotation(new PolygonAnnotation());
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

		PolygonAnnotation annotation = getAnnotation();

		if (annotation != null && shape.contains(position)) {
			Rectangle bounds = shape.getBounds();

			return String.format("%d points [%.1f, %.1f]", annotation.size(),
					bounds.getWidth(), bounds.getHeight());
		}
		return null;
	}

	/**
	 * Gets the annotation.
	 * 
	 * @return the annotation
	 */
	private PolygonAnnotation getAnnotation() {

		Annotation a = peer.getAnnotation();

		if (a == null)
			return new PolygonAnnotation();

		if (a instanceof PolygonAnnotation)
			return (PolygonAnnotation) a;

		if (a instanceof ShapeAnnotation) {

			return new PolygonAnnotation(((ShapeAnnotation) a).getPolygon());

		}

		return new PolygonAnnotation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationEditor#getCurrent()
	 */
	@Override
	public Annotation getCurrent() {
		// TODO Auto-generated method stub
		return null;
	}

}