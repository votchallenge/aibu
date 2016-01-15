package si.vicos.annotations.editor.tracking;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import si.vicos.annotations.Polygon2D;
import si.vicos.annotations.PolygonAnnotation;
import si.vicos.annotations.editor.AnnotationRenderer;

/**
 * The Class PolygonAnnotationRenderer.
 */
public class PolygonAnnotationRenderer implements AnnotationRenderer {

	/** The shape. */
	private Shape shape;

	/**
	 * The Class EmptyPathIterator.
	 */
	public static class EmptyPathIterator implements PathIterator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.geom.PathIterator#currentSegment(float[])
		 */
		@Override
		public int currentSegment(float[] coords) {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.geom.PathIterator#currentSegment(double[])
		 */
		@Override
		public int currentSegment(double[] coords) {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.geom.PathIterator#getWindingRule()
		 */
		@Override
		public int getWindingRule() {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.geom.PathIterator#isDone()
		 */
		@Override
		public boolean isDone() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.geom.PathIterator#next()
		 */
		@Override
		public void next() {
		}

	}

	/**
	 * The Class EmptyShape.
	 */
	public static class EmptyShape implements Shape {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Shape#contains(java.awt.geom.Point2D)
		 */
		@Override
		public boolean contains(Point2D p) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Shape#contains(java.awt.geom.Rectangle2D)
		 */
		@Override
		public boolean contains(Rectangle2D r) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Shape#contains(double, double)
		 */
		@Override
		public boolean contains(double x, double y) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Shape#contains(double, double, double, double)
		 */
		@Override
		public boolean contains(double x, double y, double w, double h) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Shape#getBounds()
		 */
		@Override
		public Rectangle getBounds() {
			return new Rectangle();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Shape#getBounds2D()
		 */
		@Override
		public Rectangle2D getBounds2D() {
			return new Rectangle2D.Double();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform)
		 */
		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			return new EmptyPathIterator();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform,
		 * double)
		 */
		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			return new EmptyPathIterator();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Shape#intersects(java.awt.geom.Rectangle2D)
		 */
		@Override
		public boolean intersects(Rectangle2D r) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Shape#intersects(double, double, double, double)
		 */
		@Override
		public boolean intersects(double x, double y, double w, double h) {
			return false;
		}

	}

	/**
	 * Instantiates a new polygon annotation renderer.
	 * 
	 * @param annotation
	 *            the annotation
	 */
	public PolygonAnnotationRenderer(PolygonAnnotation annotation) {

		shape = annotationToShape(annotation);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.AnnotationRenderer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g) {

		g.draw(shape);

	}

	/**
	 * Annotation to shape.
	 * 
	 * @param annotation
	 *            the annotation
	 * @return the shape
	 */
	public static Shape annotationToShape(PolygonAnnotation annotation) {

		int npoints = annotation.size();

		if (npoints < 1) {
			return new EmptyShape();
		}

		float[] xpoints = new float[npoints];
		float[] ypoints = new float[npoints];

		int i = 0;
		for (Point2D p : annotation) {
			xpoints[i] = (float) p.getX();
			ypoints[i] = (float) p.getY();
			i++;
		}

		return new Polygon2D(xpoints, ypoints, npoints);

	}

	/**
	 * Points to shape.
	 * 
	 * @param points
	 *            the points
	 * @return the shape
	 */
	public static Shape pointsToShape(List<Point2D> points) {

		if (points == null || points.size() < 1)
			return new EmptyShape();

		int npoints = points.size();

		float[] xpoints = new float[npoints];
		float[] ypoints = new float[npoints];

		int i = 0;
		for (Point2D p : points) {
			xpoints[i] = (float) p.getX();
			ypoints[i] = (float) p.getY();
			i++;
		}

		return new Polygon2D(xpoints, ypoints, npoints);

	}
}
