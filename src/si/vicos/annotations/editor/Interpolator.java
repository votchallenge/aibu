package si.vicos.annotations.editor;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.PointAnnotation;
import si.vicos.annotations.PolygonAnnotation;
import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.ShapeAnnotation;
import si.vicos.annotations.SituatedAnnotation;

/**
 * The Class Interpolator.
 */
public class Interpolator {

	/** The end. */
	private Annotation start, end;

	/** The polygon interpolation. */
	private PolygonMorphing polygonInterpolation = null;

	/**
	 * Instantiates a new interpolator.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 */
	public Interpolator(Annotation start, Annotation end) {
		this.start = start;
		this.end = end;

		if (!(start instanceof SituatedAnnotation))
			throw new UnsupportedOperationException(
					"Cannot interpolate unsituated annotations");

		if (!(end instanceof SituatedAnnotation))
			throw new UnsupportedOperationException(
					"Cannot interpolate unsituated annotations");
	}

	/**
	 * Interpolate.
	 * 
	 * @param factor
	 *            the factor
	 * @return the annotation
	 */
	public Annotation interpolate(float factor) {

		if ((start instanceof ShapeAnnotation)
				&& (end instanceof ShapeAnnotation)) {

			if ((start instanceof PolygonAnnotation)
					|| (end instanceof PolygonAnnotation)) {

				if (polygonInterpolation == null) {

					PolygonAnnotation p1 = (start instanceof PolygonAnnotation) ? (PolygonAnnotation) start
							: new PolygonAnnotation(
									((ShapeAnnotation) start).getPolygon());
					PolygonAnnotation p2 = (end instanceof PolygonAnnotation) ? (PolygonAnnotation) end
							: new PolygonAnnotation(
									((ShapeAnnotation) end).getPolygon());

					polygonInterpolation = new PolygonMorphing(p1, p2);

				}

				return polygonInterpolation.morph(factor);

			}

			if ((start instanceof RectangleAnnotation)
					&& (end instanceof RectangleAnnotation)) {

				return interpolateRectangle((RectangleAnnotation) start,
						(RectangleAnnotation) end, factor);

			}

			PolygonAnnotation p1 = new PolygonAnnotation(
					((ShapeAnnotation) start).getPolygon());
			PolygonAnnotation p2 = new PolygonAnnotation(
					((ShapeAnnotation) end).getPolygon());

			return interpolatePolygon(p1, p2, factor);

		} else {

			return interpolateCenter((SituatedAnnotation) start,
					(SituatedAnnotation) end, factor);

		}

	}

	/**
	 * Interpolate center.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @param factor
	 *            the factor
	 * @return the point annotation
	 */
	public static PointAnnotation interpolateCenter(SituatedAnnotation start,
			SituatedAnnotation end, float factor) {

		Point2D p1 = start.getCenter();
		Point2D p2 = start.getCenter();

		double x = (p1.getX()) + ((p2.getX()) - (p1.getX())) * factor;
		double y = (p1.getY()) + ((p2.getY()) - (p1.getY())) * factor;

		return new PointAnnotation((int) x, (int) y);

	}

	/**
	 * Interpolate rectangle.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @param factor
	 *            the factor
	 * @return the rectangle annotation
	 */
	public static RectangleAnnotation interpolateRectangle(
			RectangleAnnotation start, RectangleAnnotation end, float factor) {

		float x = ((float) start.getX())
				+ (((float) end.getX()) - ((float) start.getX())) * factor;
		float y = ((float) start.getY())
				+ (((float) end.getY()) - ((float) start.getY())) * factor;
		float w = ((float) start.getWidth())
				+ (((float) end.getWidth()) - ((float) start.getWidth()))
				* factor;
		float h = ((float) start.getHeight())
				+ (((float) end.getHeight()) - ((float) start.getHeight()))
				* factor;

		return new RectangleAnnotation(x, y, w, h);

	}

	/**
	 * Interpolate polygon.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @param factor
	 *            the factor
	 * @return the polygon annotation
	 */
	public static PolygonAnnotation interpolatePolygon(PolygonAnnotation start,
			PolygonAnnotation end, float factor) {

		if (start.size() != end.size())
			return null;

		List<Point2D> pointsFrom = end.getPolygon();
		List<Point2D> pointsTo = start.getPolygon();

		Point2D centerFrom = end.getCenter();
		Point2D centerTo = start.getCenter();

		List<Point2D> points = new Vector<Point2D>();

		for (Point2D pointTo : pointsTo) {

			Point2D pointToNorm = new Point2D.Double(pointTo.getX()
					- centerTo.getX(), pointTo.getY() - centerTo.getY());

			Point2D closest = null;
			double mindist = Float.MAX_VALUE;
			for (int i = 0; i < pointsFrom.size(); i++) {

				Point2D pointFrom = pointsFrom.get(i);
				Point2D pointFromNorm = new Point2D.Double(pointFrom.getX()
						- centerFrom.getX(), pointFrom.getY()
						- centerFrom.getY());

				double distance = pointFromNorm.distance(pointToNorm);

				if (distance < mindist) {
					closest = pointFrom;
					mindist = distance;
				}

			}

			double x = (pointTo.getX()) + ((closest.getX()) - (pointTo.getX()))
					* factor;
			double y = (pointTo.getY()) + ((closest.getY()) - (pointTo.getY()))
					* factor;

			points.add(new Point2D.Double(x, y));

			pointsFrom.remove(closest);
		}

		return new PolygonAnnotation(points);
	}
}
