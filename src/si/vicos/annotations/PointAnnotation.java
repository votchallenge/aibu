package si.vicos.annotations;

import java.awt.geom.Point2D;
import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.coffeeshop.string.StringUtils;

/**
 * The Class PointAnnotation.
 */
public class PointAnnotation extends SituatedAnnotation {

	/** The y. */
	private double x, y;

	/**
	 * Instantiates a new point annotation.
	 */
	public PointAnnotation() {
		reset();
	}

	/**
	 * Instantiates a new point annotation.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	public PointAnnotation(double x, double y) {
		set(x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#pack()
	 */
	@Override
	public String pack() {
		return isNull() ? "" : String.format(SERIALIZATION_LOCALE, "%f,%f", x,
				y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#reset()
	 */
	@Override
	public void reset() {
		x = Integer.MAX_VALUE;
		y = 0;
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
			set(Integer.parseInt(tokens.nextToken()),
					Integer.parseInt(tokens.nextToken()));
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
		return new PointAnnotation(x, y);
	}

	/**
	 * Sets the.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	private void set(double x, double y) {
		this.x = x;
		this.y = y;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.SituatedAnnotation#getCenter()
	 */
	public Point2D getCenter() {
		return new Point2D.Double(x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("(%.2f,%.2f)", x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.Annotation#validate(si.vicos.annotations.Annotation)
	 */
	@Override
	public boolean validate(Annotation a) {
		return a instanceof PointAnnotation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#getType()
	 */
	@Override
	public AnnotationType getType() {
		return AnnotationType.POINT;
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

		double x = (float) this.x * scale;
		double y = (float) this.y * scale;

		return new PointAnnotation(x, y);

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

		if (a instanceof PointAnnotation)
			return a;

		if (a instanceof RectangleAnnotation) {
			RectangleAnnotation ra = (RectangleAnnotation) a;
			return new PointAnnotation(ra.getCenterX(), ra.getCenterY());
		}

		if (a instanceof PolygonAnnotation) {
			Point2D center = ((PolygonAnnotation) a).getCenter();
			return new PointAnnotation(center.getX(), center.getY());
		}

		return super.convert(a);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#isNull()
	 */
	@Override
	public boolean isNull() {
		return x == Integer.MAX_VALUE;
	}
}