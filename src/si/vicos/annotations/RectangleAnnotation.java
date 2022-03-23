package si.vicos.annotations;

import java.awt.geom.Point2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.coffeeshop.string.StringUtils;

/**
 * The Class RectangleAnnotation.
 */
public class RectangleAnnotation extends ShapeAnnotation {

	/**
	 * The Class AverageRectangle.
	 */
	public static class AverageRectangle implements
			AnnotationSummary<RectangleAnnotation> {

		/** The h. */
		private double x, y, w, h;

		/** The count. */
		private int count = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.Annotation.AnnotationSummary#add(si.vicos.
		 * annotations.Annotation)
		 */
		@Override
		public void add(RectangleAnnotation annotation) {

			x += annotation.x;
			y += annotation.x;
			w += annotation.w;
			h += annotation.h;
			count++;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.Annotation.AnnotationSummary#get()
		 */
		@Override
		public RectangleAnnotation get() {

			if (count == 0)
				return null;

			return new RectangleAnnotation(x / count, y / count, w / count, h
					/ count);
		}

	}

	/** The h. */
	private double x, y, w, h;

	/**
	 * Instantiates a new rectangle annotation.
	 */
	public RectangleAnnotation() {
		this(0, 0, -1, -1);
	}

	/**
	 * Instantiates a new rectangle annotation.
	 * 
	 * @param box
	 *            the box
	 */
	public RectangleAnnotation(RectangleAnnotation box) {
		this(box.x, box.y, box.w, box.h);
	}

	/**
	 * Instantiates a new rectangle annotation.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 */
	public RectangleAnnotation(double x, double y, double w, double h) {
		set(x, y, w, h);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#pack()
	 */
	@Override
	public String pack() {
		return isNull() ? "" : String.format(SERIALIZATION_LOCALE,
				"%f,%f,%f,%f", x, y, w, h);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#reset()
	 */
	@Override
	public void reset() {
		x = 0;
		y = 0;
		w = -1;
		h = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#unpack(java.lang.String)
	 */
	@Override
	public void unpack(String data) throws ParseException {
		if (StringUtils.empty(data))
			return;

		StringTokenizer tokens = new StringTokenizer(data, ",");

		try {
			double x = Double.parseDouble(tokens.nextToken());
			double y = Double.parseDouble(tokens.nextToken());
			double w = Double.parseDouble(tokens.nextToken());
			double h = Double.parseDouble(tokens.nextToken());

			if (w < 0) {
				x += w;
				w = -w;
			}

			if (h < 0) {
				y += h;
				h = -h;
			}

			set(x, y, w, h);
		} catch (NoSuchElementException e) {
			throw new ParseException("Unable to parse", -1);
		} catch (NumberFormatException e) {
			throw new ParseException("Unable to parse", -1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#clone()
	 */
	@Override
	public Annotation clone() {
		return new RectangleAnnotation(x, y, w, h);
	}

	/**
	 * Sets the.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 */
	private void set(double x, double y, double w, double h) {

		if (w < 0) {
			this.x = x + w;
			this.w = -w;
		} else {
			this.x = x;
			this.w = w;
		}

		if (h < 0) {
			this.y = y + h;
			this.h = -h;
		} else {
			this.y = y;
			this.h = h;
		}

	}

	/**
	 * Gets the x.
	 * 
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * Gets the y.
	 * 
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * Gets the width.
	 * 
	 * @return the width
	 */
	public double getWidth() {
		return w;
	}

	/**
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public double getHeight() {
		return h;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("(%d,%d) [%d,%d]", x, y, w, h);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.Annotation#validate(si.vicos.annotations.Annotation)
	 */
	@Override
	public boolean validate(Annotation a) {
		return a instanceof RectangleAnnotation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#getType()
	 */
	@Override
	public AnnotationType getType() {
		return AnnotationType.RECTANGLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#canInterpolate()
	 */
	@Override
	public boolean canInterpolate() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#scale(float)
	 */
	@Override
	public Annotation scale(float scale) throws UnsupportedOperationException {

		float x = (float) this.x * scale;
		float y = (float) this.y * scale;
		float w = (float) this.w * scale;
		float h = (float) this.h * scale;

		return new RectangleAnnotation((int) x, (int) y, (int) w, (int) h);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.ShapeAnnotation#getBoundingBox()
	 */
	@Override
	public RectangleAnnotation getBoundingBox() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#isNull()
	 */
	@Override
	public boolean isNull() {
		return w <= 0 || h <= 0;
	}

	/**
	 * Intersect.
	 * 
	 * @param b1
	 *            the b1
	 * @param b2
	 *            the b2
	 * @return the rectangle annotation
	 */
	public static RectangleAnnotation intersect(RectangleAnnotation b1,
			RectangleAnnotation b2) {

		double x1 = Math.max(b1.x, b2.x);
		double y1 = Math.max(b1.y, b2.y);
		double x2 = Math.min(b1.x + b1.w, b2.x + b2.w);
		double y2 = Math.min(b1.y + b1.h, b2.y + b2.h);

		return new RectangleAnnotation(x1, x2, x2 - x1, y2 - y1);

	}

	/**
	 * Union.
	 * 
	 * @param b1
	 *            the b1
	 * @param b2
	 *            the b2
	 * @return the rectangle annotation
	 */
	public static RectangleAnnotation union(RectangleAnnotation b1,
			RectangleAnnotation b2) {

		double x1 = Math.min(b1.x, b2.x);
		double y1 = Math.min(b1.y, b2.y);
		double x2 = Math.max(b1.x + b1.w, b2.x + b2.w);
		double y2 = Math.max(b1.y + b1.h, b2.y + b2.h);

		return new RectangleAnnotation(x1, x2, x2 - x1, y2 - y1);

	}

	/**
	 * Contains.
	 * 
	 * @param a
	 *            the a
	 * @return true, if successful
	 */
	public boolean contains(PointAnnotation a) {

		if (a == null || a.isNull())
			return false;

		if (x > a.getX() || x + w < a.getX())
			return false;

		if (y > a.getY() || y + h < a.getY())
			return false;

		return true;

	}

	/**
	 * Gets the top.
	 * 
	 * @return the top
	 */
	public double getTop() {
		return y;
	}

	/**
	 * Gets the left.
	 * 
	 * @return the left
	 */
	public double getLeft() {
		return x;
	}

	/**
	 * Gets the bottom.
	 * 
	 * @return the bottom
	 */
	public double getBottom() {
		return y + h;
	}

	/**
	 * Gets the right.
	 * 
	 * @return the right
	 */
	public double getRight() {
		return x + w;
	}

	/**
	 * Gets the center x.
	 * 
	 * @return the center x
	 */
	public double getCenterX() {
		return (x + w) / 2;
	}

	/**
	 * Gets the center y.
	 * 
	 * @return the center y
	 */
	public double getCenterY() {
		return (y + h) / 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.SituatedAnnotation#getCenter()
	 */
	@Override
	public Point2D getCenter() {
		return new Point2D.Double(getCenterX(), getCenterY());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.ShapeAnnotation#getPolygon()
	 */
	@Override
	public List<Point2D> getPolygon() {

		ArrayList<Point2D> points = new ArrayList<Point2D>(4);

		points.add(new Point2D.Double(getLeft(), getBottom()));
		points.add(new Point2D.Double(getRight(), getBottom()));
		points.add(new Point2D.Double(getRight(), getTop()));
		points.add(new Point2D.Double(getLeft(), getTop()));

		return points;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.Annotation#convert(si.vicos.annotations.Annotation)
	 */
	@Override
	public Annotation convert(Annotation a)
			throws UnsupportedOperationException {

		if (a instanceof RectangleAnnotation)
			return a;

		if (a instanceof ShapeAnnotation) {
			return ((ShapeAnnotation) a).getBoundingBox();
		}

		return super.convert(a);
	}
}