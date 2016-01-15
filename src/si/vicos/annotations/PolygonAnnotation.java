package si.vicos.annotations;

import java.awt.geom.Point2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.coffeeshop.string.StringUtils;

/**
 * The Class PolygonAnnotation.
 */
public class PolygonAnnotation extends ShapeAnnotation implements
		Iterable<Point2D> {

	/** The points. */
	private Vector<Point2D> points = new Vector<Point2D>();

	/**
	 * Instantiates a new polygon annotation.
	 */
	public PolygonAnnotation() {

	}

	/**
	 * Instantiates a new polygon annotation.
	 * 
	 * @param data
	 *            the data
	 * @throws ParseException
	 *             the parse exception
	 */
	public PolygonAnnotation(String data) throws ParseException {
		this.unpack(data);
	}

	/**
	 * Instantiates a new polygon annotation.
	 * 
	 * @param points
	 *            the points
	 */
	public PolygonAnnotation(List<Point2D> points) {
		this.points.addAll(points);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.ShapeAnnotation#getBoundingBox()
	 */
	@Override
	public RectangleAnnotation getBoundingBox() {
		double minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;

		for (Point2D point : points) {
			minX = Math.min(minX, point.getX());
			minY = Math.min(minY, point.getY());
			maxX = Math.max(maxX, point.getX());
			maxY = Math.max(maxY, point.getY());

		}

		return new RectangleAnnotation(minX, minY, maxX - minX, maxY - minY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#reset()
	 */
	@Override
	public void reset() {
		if (points != null)
			points.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#pack()
	 */
	@Override
	public String pack() {

		if (isNull())
			return "";

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < points.size(); i++) {
			Point2D p = points.get(i);
			if (i > 0)
				builder.append(",");
			builder.append(String.format(SERIALIZATION_LOCALE, "%.3f,%.3f",
					p.getX(), p.getY()));
		}

		return builder.toString();
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

		this.points.clear();

		StringTokenizer tokens = new StringTokenizer(data, ",");

		try {
			while (tokens.hasMoreElements()) {

				double x = Float.parseFloat(tokens.nextToken());
				double y = Float.parseFloat(tokens.nextToken());
				points.add(new Point2D.Double(x, y));
			}
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
		return new PolygonAnnotation(points);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.Annotation#validate(si.vicos.annotations.Annotation)
	 */
	@Override
	public boolean validate(Annotation a) {
		return (a instanceof PolygonAnnotation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#getType()
	 */
	@Override
	public AnnotationType getType() {
		return AnnotationType.POLYGON;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#isNull()
	 */
	@Override
	public boolean isNull() {
		return points.size() < 3;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Point2D> iterator() {
		return points.iterator();
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public int size() {
		return points.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.ShapeAnnotation#getPolygon()
	 */
	@Override
	public List<Point2D> getPolygon() {
		return new ArrayList<Point2D>(points);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.SituatedAnnotation#getCenter()
	 */
	public Point2D getCenter() {

		double sumX = 0;
		double sumY = 0;

		for (Point2D p : points) {
			sumX += p.getX();
			sumY += p.getY();
		}

		return new Point2D.Double(sumX / points.size(), sumY / points.size());
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

		if (a instanceof PolygonAnnotation)
			return a;

		if (a instanceof RectangleAnnotation) {

			RectangleAnnotation r = (RectangleAnnotation) a;
			return new PolygonAnnotation(r.getPolygon());

		}

		return super.convert(a);
	}

}
