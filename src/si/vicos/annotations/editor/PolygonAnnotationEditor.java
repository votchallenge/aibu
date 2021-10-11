package si.vicos.annotations.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;

import org.coffeeshop.swing.figure.FigurePanel;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.PointAnnotation;
import si.vicos.annotations.PolygonAnnotation;
import si.vicos.annotations.ShapeAnnotation;
import si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer;
import si.vicos.annotations.editor.tracking.PolygonAnnotationRenderer;

/**
 * The Class PolygonAnnotationEditor.
 */
public class PolygonAnnotationEditor extends AnnotationEditor {

	/**
	 * The Enum ManipulationMode.
	 */
	public enum ManipulationMode {
		/** The points. */
		POINTS, /** The move. */
		MOVE, /** The rotate. */
		ROTATE, /** The scale. */
		SCALE
	};

	/** The Constant KEYPOINT_SIZE. */
	public static final double KEYPOINT_SIZE = 5;

	/** The Constant FACTORY. */
	public static final AnnotationEditorFactory<PolygonAnnotationEditor> FACTORY = new AnnotationEditorFactory<PolygonAnnotationEditor>() {

		@Override
		public String getName() {
			return "Polygon";
		}

		@Override
		public PolygonAnnotationEditor getEditor(AnnotationPeer peer,
				Color color) {
			return new PolygonAnnotationEditor(peer, color);
		}

	};

	/** The points. */
	private Vector<Point2D> points = new Vector<Point2D>();

	/** The shape. */
	private Shape shape;

	/** The working shape. */
	private Shape workingShape = null;

	/** The point editor. */
	private PointAnnotationEditor pointEditor = null;

	/** The mode. */
	private ManipulationMode mode = ManipulationMode.POINTS;

	/**
	 * The Class PointAnnotationPeer.
	 */
	private class PointAnnotationPeer implements AnnotationPeer {

		/** The index. */
		private int index;

		/**
		 * Instantiates a new point annotation peer.
		 * 
		 * @param index
		 *            the index
		 */
		public PointAnnotationPeer(int index) {
			this.index = index;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer#repaint
		 * ()
		 */
		@Override
		public void repaint() {

			PolygonAnnotationEditor.this.peer.repaint();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer#
		 * setAnnotation(si.vicos.annotations.Annotation)
		 */
		@Override
		public void setAnnotation(Annotation a) {

			PolygonAnnotation annotation = PolygonAnnotationEditor.this
					.getAnnotation();
			if (annotation == null)
				return;

			if (!(a instanceof PointAnnotation))
				return;

			List<Point2D> points = annotation.getPolygon();
			points.set(index, new Point2D.Double(((PointAnnotation) a).getX(),
					((PointAnnotation) a).getY()));
			annotation = new PolygonAnnotation(points);
			peer.setAnnotation(annotation);

			updateGraphics();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer#
		 * getAnnotation()
		 */
		@Override
		public Annotation getAnnotation() {

			PolygonAnnotation annotation = PolygonAnnotationEditor.this
					.getAnnotation();
			if (annotation == null)
				return null;

			Point2D point = annotation.getPolygon().get(index);

			return new PointAnnotation((float) point.getX(),
					(float) point.getY());
		}

	}

	// private Point2D edited

	/**
	 * Instantiates a new polygon annotation editor.
	 * 
	 * @param peer
	 *            the peer
	 * @param color
	 *            the color
	 */
	public PolygonAnnotationEditor(AnnotationPeer peer, Color color) {
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
			Point2D startPoint = points.get(0);
			if (mode == ManipulationMode.POINTS)
				g.drawOval((int) startPoint.getX() - 3,
						(int) startPoint.getY() - 3, 6, 6);
		}

		if (pointEditor != null)
			pointEditor.paint(g);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.AnnotationEditor#resetInput()
	 */
	@Override
	public void resetInput() {

		if (pointEditor != null) {
			pointEditor.resetInput();
			pointEditor = null;
		}

		workingShape = null;
		points.clear();
		mode = ManipulationMode.POINTS;
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

		if (pointEditor != null) {
			Cursor cursor = pointEditor.onMove(source, from, to, drag,
					modifiers);
			Annotation a = pointEditor.getCurrent();
			if (a != null
					&& ((PointAnnotation) a).getCenter().distance(to) > KEYPOINT_SIZE) {
				pointEditor = null;
				notifyRepaint();
			} else
				return cursor;
		}

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
					// List<Point2D> original = annotation.getPolygon();
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
				case POINTS: {
				}
				}

				notifyRepaint();
				return dragCursor;
			}

			if (points.size() == 0) {

				if (!annotation.isNull() && shape.contains(from) && from == to) {
					points.clear();
					points.addAll(annotation.getPolygon());

					if ((modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0)
						mode = ManipulationMode.ROTATE;
					else if ((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0)
						mode = ManipulationMode.SCALE;
					else
						mode = ManipulationMode.MOVE;

					notifyRepaint();
					return dragCursor;
				}
				return defaultCursor;
			}

			return pointCursor;

		} else {

			// not moving the polygon
			if (points.size() == 0) {

				int point = closestPoint(to, KEYPOINT_SIZE);

				if (point > -1) {

					pointEditor = new PointAnnotationEditor(
							new PointAnnotationPeer(point), color,
							KEYPOINT_SIZE);
					pointEditor.setSelected(true);
					pointEditor.onMove(source, from, to, drag, modifiers);
					notifyRepaint();

				} else {

					if (pointEditor != null) {
						pointEditor = null;
						notifyRepaint();
					}

					if (!annotation.isNull() && shape.contains(to))
						return dragPossibleCursor;
					return defaultCursor;
				}
			} else {

				if (mode == ManipulationMode.POINTS && points.size() > 1) {

					points.set(points.size() - 1, to);

				}

			}

			workingShape = points.isEmpty() ? null : PolygonAnnotationRenderer
					.pointsToShape(points);
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

		if (pointEditor != null) {
			pointEditor.onClick(source, position, clicks, modifiers);
			return;
		}

		if (position != null) {
			if (points.size() > 2
					&& points.get(0).distance(position) < KEYPOINT_SIZE) {
				points.remove(points.size() - 1); // Remove last, working point
				annotation = new PolygonAnnotation(points);
				peer.setAnnotation(annotation);
				updateGraphics();
				resetInput();
				points.clear();
			} else {

				if (points.size() < 1)
					points.add(position); // Add another point that will be
											// moved around
				else
					points.set(points.size() - 1, position);
				points.add(position);
			}

		} else if (points.size() > 2) {
			if (mode != ManipulationMode.POINTS) {
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
	 * Closest point.
	 * 
	 * @param point
	 *            the point
	 * @param threshold
	 *            the threshold
	 * @return the int
	 */
	private int closestPoint(Point2D point, double threshold) {

		PolygonAnnotation annotation = getAnnotation();

		if (annotation == null)
			return -1;

		List<Point2D> points = annotation.getPolygon();

		int closest = -1;
		double mindist = Float.MAX_VALUE;
		for (int i = 0; i < points.size(); i++) {

			double distance = points.get(i).distance(point);

			if (distance < threshold && distance < mindist) {
				closest = i;
				mindist = distance;
			}

		}

		return closest;
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