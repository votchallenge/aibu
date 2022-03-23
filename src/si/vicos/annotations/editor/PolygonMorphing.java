package si.vicos.annotations.editor;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import si.vicos.annotations.PolygonAnnotation;
import si.vicos.annotations.ShapeAnnotation;

/**
 * The Class PolygonMorphing.
 */
public class PolygonMorphing {

	/**
	 * The Class Polygon.
	 */
	private static class Polygon {

		/** The points. */
		private Vector<Point2D> points;

		/** The normals. */
		private Vector<EdgeNormal> normals;

		/** The minweight. */
		private double maxweight, minweight;

		/**
		 * Instantiates a new polygon.
		 */
		Polygon() {
			maxweight = 0;
			minweight = 0;
			points = new Vector<Point2D>();
			normals = new Vector<EdgeNormal>();
		}

		/**
		 * Adds the point.
		 * 
		 * @param p
		 *            the p
		 */
		void addPoint(Point2D p) {
			if (points.isEmpty()) {
				points.addElement(p);
			} else {
				normals.addElement(new EdgeNormal(p, (Point2D) points
						.lastElement()));
				double lastweight = ((EdgeNormal) normals.lastElement())
						.getWeight();
				points.addElement(p);
				if (lastweight > maxweight)
					maxweight = lastweight;
				if (lastweight < minweight)
					minweight = lastweight;
			}
		}

		/**
		 * Gets the bounding box.
		 * 
		 * @return the bounding box
		 */
		public Rectangle2D getBoundingBox() {
			double minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;

			for (Point2D point : points) {
				minX = Math.min(minX, point.getX());
				minY = Math.min(minY, point.getY());
				maxX = Math.max(maxX, point.getX());
				maxY = Math.max(maxY, point.getY());

			}

			return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		}

		/**
		 * Adds the final normal.
		 */
		void addFinalNormal() {
			if (!points.isEmpty()) {
				if (normals.size() != points.size() - 1) {

				} else {
					normals.addElement(new EdgeNormal((Point2D) points
							.firstElement(), (Point2D) points.lastElement()));
					double lastweight = ((EdgeNormal) normals.lastElement())
							.getWeight();

					if (lastweight > maxweight)
						maxweight = lastweight;
					if (lastweight < minweight)
						minweight = lastweight;
				}
			}
		}

		/**
		 * Gets the maxweight.
		 * 
		 * @return the maxweight
		 */
		private double getMaxweight() {
			return maxweight;
		}

		/**
		 * Gets the minweight.
		 * 
		 * @return the minweight
		 */
		private double getMinweight() {
			return minweight;
		}

		/**
		 * Size.
		 * 
		 * @return the int
		 */
		private int size() {
			return points.size();
		}

		/**
		 * Point at.
		 * 
		 * @param i
		 *            the i
		 * @return the point2 d
		 */
		private Point2D pointAt(int i) {
			return (Point2D) points.elementAt(i);
		}

		/**
		 * Normal at.
		 * 
		 * @param i
		 *            the i
		 * @return the edge normal
		 */
		private EdgeNormal normalAt(int i) {
			return (EdgeNormal) normals.elementAt(i);
		}

		/**
		 * Translate.
		 * 
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 */
		private void translate(double x, double y) {

			for (Point2D point : points) {

				point.setLocation(point.getX() + x, point.getY() + y);

			}

		}

		/**
		 * Adds the first normal.
		 * 
		 * @param p
		 *            the p
		 * @param v
		 *            the v
		 */
		private void addFirstNormal(Point2D p, EdgeNormal v) {
			points.addElement(p);
			normals.addElement(v);
			points.addElement(new Point2D.Double(p.getX() - v.y, p.getY() + v.x));
		}

		/**
		 * Adds the normal.
		 * 
		 * @param v
		 *            the v
		 */
		private void addNormal(EdgeNormal v) {
			if (size() > 0) {
				normals.addElement(v);
				Point2D p = points.elementAt(points.size() - 1);
				points.addElement(new Point2D.Double(p.getX() - v.y, p.getY()
						+ v.x));
				if (v.getWeight() > maxweight)
					maxweight = v.getWeight();
				if (v.getWeight() < minweight)
					minweight = v.getWeight();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			String pointString = new String(points.size()
					+ "Points are (x,y) = ");
			String normalString = new String(normals.size()
					+ "Normals are (angle, weight) = ");
			for (int i = 0; i < points.size(); i++)
				pointString += "(" + pointAt(i).getX() + ", "
						+ pointAt(i).getY() + ") ";
			for (int i = 0; i < normals.size(); i++)
				normalString += "(" + normalAt(i).getAngle() + ", "
						+ normalAt(i).getWeight() + ") ";
			return (pointString + "\n" + normalString);
		}

		/**
		 * Compress.
		 * 
		 * @param threshold
		 *            the threshold
		 * @return the polygon
		 */
		private Polygon compress(double threshold) {

			Vector<Vector<Point2D>> gathered = new Vector<Vector<Point2D>>();

			gathered.add(new Vector<Point2D>());

			for (Point2D point : points) {

				if (gathered.lastElement().isEmpty()) {
					gathered.lastElement().add(point);

				} else {

					if (point.distance(gathered.lastElement().lastElement()) < threshold) {

						gathered.lastElement().add(point);

					} else {

						gathered.add(new Vector<Point2D>());
						gathered.lastElement().add(point);

					}

				}

			}

			// should we also merge first and last cluster?
			if (gathered.size() > 2
					&& gathered.firstElement().firstElement()
							.distance(gathered.lastElement().lastElement()) < threshold) {

				gathered.firstElement().addAll(gathered.lastElement()); // Add
																		// all
																		// from
																		// last
																		// cluster
																		// to
																		// first
																		// one
				gathered.remove(gathered.size() - 1); // Remove last cluster

			}

			Polygon compressed = new Polygon();

			for (Vector<Point2D> set : gathered) {

				double sumX = 0, sumY = 0;

				for (Point2D point : set) {
					sumX += point.getX();
					sumY += point.getY();
				}

				compressed.addPoint(new Point2D.Double(sumX / set.size(), sumY
						/ set.size()));

			}

			compressed.addFinalNormal();

			return compressed;

		}

	}

	/**
	 * The Class EdgeNormal.
	 */
	private static class EdgeNormal {

		/** The sina. */
		// between 0 and 2*pi
		private double angle, cosa, sina;

		/** The weight. */
		private double weight;

		/** The y. */
		// the x y components
		public double x, y;

		/**
		 * Instantiates a new edge normal.
		 * 
		 * @param b
		 *            the b
		 * @param a
		 *            the a
		 */
		// constructor from two points
		EdgeNormal(Point2D b, Point2D a) {
			x = -b.getY() + a.getY();
			y = -a.getX() + b.getX();
			angle = Math.atan2(y, x);
			if (angle < 0)
				angle += Math.PI * 2;
			// angle = 2*Math.PI - angle;
			weight = Math.sqrt(x * x + y * y);
			// cosa = x/weight;
			// sina = y/weight;
			cosa = Math.cos(angle);
			sina = Math.sin(angle);
		}

		/**
		 * Instantiates a new edge normal.
		 * 
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param weight
		 *            the weight
		 * @param angle
		 *            the angle
		 * @param cosa
		 *            the cosa
		 * @param sina
		 *            the sina
		 */
		EdgeNormal(float x, float y, double weight, double angle, double cosa,
				double sina) {
			this.x = x;
			this.y = y;
			this.weight = weight;
			this.angle = angle;
			this.cosa = cosa;
			this.sina = sina;
		}

		/**
		 * Sets the.
		 * 
		 * @param a
		 *            the a
		 * @param b
		 *            the b
		 */
		// mutator from two points
		void set(Point2D a, Point2D b) {
			x = -b.getY() + a.getY();
			y = -a.getX() + b.getX();
			angle = Math.atan2(y, x);
			if (angle < 0)
				angle += Math.PI * 2;
			// angle = 2*Math.PI - angle;
			weight = Math.sqrt(x * x + y * y);
			cosa = Math.cos(angle);
			sina = Math.sin(angle);
		}

		/**
		 * Gets the cosa.
		 * 
		 * @return the cosa
		 */
		public double getCosa() {
			return cosa;
		}

		/**
		 * Gets the sina.
		 * 
		 * @return the sina
		 */
		public double getSina() {
			return sina;
		}

		/**
		 * Gets the angle.
		 * 
		 * @return the angle
		 */
		public double getAngle() {
			return angle;
		}

		/**
		 * Gets the weight.
		 * 
		 * @return the weight
		 */
		public double getWeight() {
			return weight;
		}

		/**
		 * Scale by.
		 * 
		 * @param scaleFactor
		 *            the scale factor
		 */
		public void scaleBy(double scaleFactor) {
			x *= scaleFactor;
			y *= scaleFactor;
			if (scaleFactor >= 0) {
				weight *= scaleFactor;
			} else {
				weight *= -scaleFactor;
				if (angle > Math.PI)
					angle -= Math.PI;
				else
					angle += Math.PI;
				cosa = -cosa;
				sina = -sina;
			}
		}

		/**
		 * Mult by.
		 * 
		 * @param scaleFactor
		 *            the scale factor
		 * @return the edge normal
		 */
		public EdgeNormal multBy(double scaleFactor) {
			// System.out.println("Scaling by "+Float.toString(scaleFactor));
			if (scaleFactor >= 0)
				return (new EdgeNormal((float) (x * scaleFactor),
						(float) (y * scaleFactor), weight * scaleFactor, angle,
						cosa, sina));
			else
				return (new EdgeNormal((float) (x * scaleFactor),
						(float) (y * scaleFactor), -weight * scaleFactor,
						angle > Math.PI ? angle - Math.PI : angle + Math.PI,
						-cosa, -sina));
			// return(new MMnormal(new MMpoint(y * scaleFactor, -x *
			// scaleFactor), new MMpoint (0, 0)));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return x + ", " + y + " ," + weight + ", " + angle + ", " + cosa
					+ ", " + sina;
		}
	}

	/**
	 * The Class MorphableNormal.
	 */
	private static class MorphableNormal {

		// increments

		/** The angle. */
		// between 0 and 2*pi
		private double angle0, angle;

		/** The weight. */
		private double weight0, weight;

		/** The y. */
		// the x y components
		public double x0, x, y0, y;

		/**
		 * Instantiates a new morphable normal.
		 * 
		 * @param vec1
		 *            the vec1
		 * @param vec2
		 *            the vec2
		 */
		// constructor from two points
		MorphableNormal(EdgeNormal vec1, EdgeNormal vec2) {
			x0 = vec1.x;
			y0 = vec1.y;
			weight0 = vec1.getWeight();
			x = vec2.x - x0;
			y = vec2.y - y0;
			weight = (.01 * (vec2.getWeight() - weight0));
		}

		/**
		 * Normal when.
		 * 
		 * @param p
		 *            the p
		 * @return the edge normal
		 */
		// when the percentage is p the normal is...
		EdgeNormal normalWhen(double p) {
			return new EdgeNormal(new Point2D.Double((y0 + y * p),
					-(x0 + p * x)), new Point2D.Double(0, 0));
		}
	}

	/**
	 * The Class MorphablePolygon.
	 */
	private static class MorphablePolygon {

		/** The target. */
		protected Polygon source, target;

		/** The current. */
		protected Polygon current;

		/** The mnormals. */
		protected MorphableNormal[] mnormals;

		/** The sorted. */
		protected EdgeNormal[] sorted;

		/** The max weight diff. */
		protected double maxWeightDiff;

		/**
		 * Instantiates a new morphable polygon.
		 * 
		 * @param source
		 *            the source
		 * @param target
		 *            the target
		 * @param lambda
		 *            the lambda
		 * @param sigma
		 *            the sigma
		 */
		MorphablePolygon(Polygon source, Polygon target, float lambda,
				float sigma) {

			// maximum weight difference between the two files
			this.maxWeightDiff = Math.max(
					source.getMaxweight() - target.getMinweight(),
					target.getMaxweight() - source.getMinweight());

			double[][] weights = new double[source.size()][target.size()];
			double[] sourceSum = new double[source.size()];
			double[] targetSum = new double[target.size()];

			mnormals = new MorphableNormal[source.size() * target.size()];
			sorted = new EdgeNormal[source.size() * target.size()];
			this.source = source;
			this.target = target;

			// individual weights
			for (int i = 0; i < source.size(); i++)
				for (int j = 0; j < target.size(); j++) {
					double dAngle = angleUnitDist(source.normalAt(i),
							target.normalAt(j));
					double dWeight = weightUnitDist(source.normalAt(i),
							target.normalAt(j));
					double thingToExp = -sigma
							* (lambda
									* angleUnitDist(source.normalAt(i),
											target.normalAt(j)) + weightUnitDist(
										source.normalAt(i), target.normalAt(j)));
					weights[i][j] = Math.exp(thingToExp);
				}

			// inverted source sums
			for (int i = 0; i < source.size(); i++) {
				sourceSum[i] = 0;
				for (int j = 0; j < target.size(); j++)
					sourceSum[i] += weights[i][j];
				sourceSum[i] = 1 / sourceSum[i];
			}

			// inverted target sums
			for (int j = 0; j < target.size(); j++) {
				targetSum[j] = 0;
				for (int i = 0; i < source.size(); i++)
					targetSum[j] += weights[i][j];
				targetSum[j] = 1 / targetSum[j];
			}

			// create all pairs (m*n of 'em)
			for (int i = 0; i < source.size(); i++)
				for (int j = 0; j < target.size(); j++)
					mnormals[i * target.size() + j] = new MorphableNormal(
							source.normalAt(i).multBy(
									weights[i][j] * sourceSum[i]), target
									.normalAt(j).multBy(
											weights[i][j] * targetSum[j]));
		}

		/**
		 * Angle unit dist.
		 * 
		 * @param m
		 *            the m
		 * @param n
		 *            the n
		 * @return the double
		 */
		private double angleUnitDist(EdgeNormal m, EdgeNormal n) {
			return ((m.getAngle() > n.getAngle()) ? Math.min(
					m.getAngle() - n.getAngle(),
					2 * Math.PI + n.getAngle() - m.getAngle()) : Math.min(
					n.getAngle() - m.getAngle(),
					2 * Math.PI + m.getAngle() - n.getAngle()))
					/ (Math.PI); // max angle difference is pi since we're going
									// both ways
		}

		/**
		 * Weight unit dist.
		 * 
		 * @param m
		 *            the m
		 * @param n
		 *            the n
		 * @return the double
		 */
		private double weightUnitDist(EdgeNormal m, EdgeNormal n) {
			return (Math.abs(m.getWeight() - n.getWeight())) / maxWeightDiff;
		}

		/**
		 * Morph at.
		 * 
		 * @param percentage
		 *            the percentage
		 * @return the polygon
		 */
		public Polygon morphAt(double percentage) {
			// first create the array to sort
			for (int i = 0; i < mnormals.length; i++) {
				sorted[i] = mnormals[i].normalWhen(percentage);
			}

			// quick sort by angle
			// quickSort(0, sorted.length - 1);

			Arrays.sort(sorted, new Comparator<EdgeNormal>() {

				@Override
				public int compare(EdgeNormal a, EdgeNormal b) {
					if (a.getAngle() < b.getAngle())
						return 1;
					if (a.getAngle() > b.getAngle())
						return -1;
					return 0;
				}
			});

			// from sorted list create MMdata
			current = new Polygon();
			current.addFirstNormal(new Point2D.Double(0, 0), sorted[0]);
			for (int i = 1; i < mnormals.length - 1; i++) {
				current.addNormal(sorted[i]);
			}

			current.addFinalNormal();

			current.normalAt(current.size() - 1).scaleBy(-1);

			return current;
		}

	}

	/**
	 * Convert to polygon.
	 * 
	 * @param a
	 *            the a
	 * @return the polygon
	 */
	private static Polygon convertToPolygon(ShapeAnnotation a) {

		List<Point2D> points = a.getPolygon();

		Polygon polygon = new Polygon();

		for (Point2D point : points) {

			polygon.addPoint(new Point2D.Double(point.getX(), point.getY()));

		}

		polygon.addFinalNormal();

		return polygon;
	}

	/**
	 * Convert to annotation.
	 * 
	 * @param poly
	 *            the poly
	 * @return the polygon annotation
	 */
	private static PolygonAnnotation convertToAnnotation(Polygon poly) {

		Vector<Point2D> points = new Vector<Point2D>();

		for (Point2D point : poly.points) {

			points.add(new Point2D.Double(point.getX(), point.getY()));

		}

		return new PolygonAnnotation(points);

	}

	/** The intermediate. */
	private MorphablePolygon intermediate;

	/**
	 * Instantiates a new polygon morphing.
	 * 
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 */
	public PolygonMorphing(ShapeAnnotation source, ShapeAnnotation target) {

		Polygon sourcePolygon = convertToPolygon(source);
		Polygon targetPolygon = convertToPolygon(target);

		// System.out.println(sourcePolygon);
		// System.out.println(targetPolygon);

		intermediate = new MorphablePolygon(sourcePolygon, targetPolygon,
				sourcePolygon.size() == targetPolygon.size() ? 140 : 140, 1);

	}

	/**
	 * Morph.
	 * 
	 * @param factor
	 *            the factor
	 * @return the polygon annotation
	 */
	public PolygonAnnotation morph(double factor) {

		Polygon morphed = intermediate.morphAt(factor).compress(1);

		Rectangle2D sourceBounds = intermediate.source.getBoundingBox();
		Rectangle2D targetBounds = intermediate.target.getBoundingBox();

		Rectangle2D morphedBounds = morphed.getBoundingBox();

		double cx = sourceBounds.getCenterX()
				+ (targetBounds.getCenterX() - sourceBounds.getCenterX())
				* factor;
		double cy = sourceBounds.getCenterY()
				+ (targetBounds.getCenterY() - sourceBounds.getCenterY())
				* factor;

		morphed.translate(cx - morphedBounds.getCenterX(),
				cy - morphedBounds.getCenterY());

		return convertToAnnotation(morphed);

	}

}
