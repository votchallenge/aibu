package si.vicos.annotations.editor.tracking;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Set;
import java.util.Vector;

import org.coffeeshop.awt.Colors;
import org.coffeeshop.swing.figure.PlotObject;

import si.vicos.annotations.tracking.AnnotatedSequence;
import si.vicos.annotations.tracking.AnnotatedSequenceListener;
import si.vicos.annotations.tracking.Interval;

/**
 * The Class TagPlot.
 */
public abstract class TagPlot implements PlotObject {

	/**
	 * The Enum MarkerShape.
	 */
	public static enum MarkerShape {
		/** The circle. */
		CIRCLE, /** The cross. */
		CROSS, /** The rectangle. */
		RECTANGLE, /** The diamond. */
		DIAMOND, /** The triangle. */
		TRIANGLE
	}

	/**
	 * Gets the marker.
	 * 
	 * @param shape
	 *            the shape
	 * @param size
	 *            the size
	 * @return the marker
	 */
	public static Shape getMarker(MarkerShape shape, int size) {

		Path2D s;
		switch (shape) {
		case CIRCLE:
			return new Ellipse2D.Float(-size, -size, 2 * size, 2 * size);
		case CROSS:
			s = new Path2D.Float();
			s.moveTo(-size, 0);
			s.lineTo(size, 0);
			s.moveTo(0, -size);
			s.lineTo(0, size);
			return s;
		case RECTANGLE:
			s = new Path2D.Float();
			s.moveTo(-size, -size);
			s.lineTo(size, -size);
			s.lineTo(size, size);
			s.lineTo(-size, size);
			s.closePath();
			return s;
		case DIAMOND:
			s = new Path2D.Float();
			s.moveTo(-size, 0);
			s.lineTo(0, -size);
			s.lineTo(size, 0);
			s.lineTo(0, -size);
			s.closePath();
			return s;
		case TRIANGLE:
			s = new Path2D.Float();
			s.moveTo(-size, size / 2);
			s.lineTo(0, -size / 2);
			s.lineTo(size, size / 2);
			s.closePath();
			return s;
		default:
			return null;
		}

	}

	/**
	 * The Class SingleTagPlot.
	 */
	public static class SingleTagPlot extends TagPlot {

		/** The intervals. */
		private Collection<Interval> intervals;

		/** The label. */
		private String label;

		/** The color. */
		private Color color;

		/** The offset. */
		private float offset;

		/** The single frame. */
		private Shape singleFrame = getMarker(MarkerShape.CIRCLE, 5);

		/**
		 * Instantiates a new single tag plot.
		 * 
		 * @param annotations
		 *            the annotations
		 * @param label
		 *            the label
		 * @param color
		 *            the color
		 */
		public SingleTagPlot(AnnotatedSequence annotations, String label,
				Color color) {
			this(annotations, label, color, 0.5f);
		}

		/**
		 * Instantiates a new single tag plot.
		 * 
		 * @param annotations
		 *            the annotations
		 * @param label
		 *            the label
		 * @param color
		 *            the color
		 * @param offset
		 *            the offset
		 */
		public SingleTagPlot(AnnotatedSequence annotations, String label,
				Color color, float offset) {

			super();

			this.name = "Tag '" + label + "' for " + annotations.getName();
			this.label = label;
			this.color = color;

			this.offset = Math.min(1, Math.max(0, offset));

			update(annotations);

			annotations
					.addAnnotatedSequenceListener(new AnnotatedSequenceListener() {

						public void metadataChanged(AnnotatedSequence sequence,
								Set<String> keys) {

						}

						public void intervalChanged(AnnotatedSequence sequence,
								Interval interval) {

							update(sequence);

						}

					});

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.coffeeshop.swing.figure.PlotObject#paint(java.awt.Graphics2D,
		 * float, java.awt.geom.AffineTransform)
		 */
		@Override
		public void paint(Graphics2D g, float scale,
				AffineTransform pretransform) {

			g.setColor(color);

			paintIntervals(intervals, g, pretransform, offset);

		}

		/**
		 * Update.
		 * 
		 * @param annotations
		 *            the annotations
		 */
		public void update(AnnotatedSequence annotations) {

			bounds.setRect(0, 0, annotations.size(), 1);

			intervals = extractIntervals(annotations.findTag(label));

		}

		/**
		 * Sets the color.
		 * 
		 * @param color
		 *            the new color
		 */
		public void setColor(Color color) {
			this.color = color;
		}

		/**
		 * Gets the color.
		 * 
		 * @return the color
		 */
		public Color getColor() {
			return color;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.coffeeshop.swing.figure.PlotObject#getToolTip(java.awt.geom.Point2D
		 * )
		 */
		@Override
		public String getToolTip(Point2D point) {

			return null;
		}

		/**
		 * Paint intervals.
		 * 
		 * @param intervals
		 *            the intervals
		 * @param g
		 *            the g
		 * @param pretransform
		 *            the pretransform
		 * @param offset
		 *            the offset
		 */
		protected void paintIntervals(Collection<Interval> intervals,
				Graphics2D g, AffineTransform pretransform, double offset) {

			Color border = getColor();

			Color fill = Colors.changeBrightness(border, 0.9);

			for (Interval i : intervals) {
				Point2D p1 = new Point2D.Double(), p2 = new Point2D.Double();
				if (i.isEmpty()) {
					p1.setLocation(i.getBegin(), offset);
					// p2.setLocation(i.getEnd()+ 1, offset - 0.1);

					pretransform.transform(p1, p1);
					// pretransform.transform(p2, p2);
					g.translate(p1.getX(), p1.getY());
					g.setColor(fill);
					g.fill(singleFrame);
					g.setColor(border);
					g.draw(singleFrame);
					g.translate(-p1.getX(), -p1.getY());
				} else {
					p1.setLocation(i.getBegin(), offset + 0.1);
					p2.setLocation(i.getEnd(), offset - 0.1);

					pretransform.transform(p1, p1);
					pretransform.transform(p2, p2);
					g.setColor(fill);
					g.fillRect((int) p1.getX(), (int) p1.getY(),
							(int) (p2.getX() - p1.getX()),
							(int) (p2.getY() - p1.getY()));
					g.setColor(border);
					g.drawRect((int) p1.getX(), (int) p1.getY(),
							(int) (p2.getX() - p1.getX()),
							(int) (p2.getY() - p1.getY()));
				}
			}

		}

		/**
		 * Extract intervals.
		 * 
		 * @param frames
		 *            the frames
		 * @return the collection
		 */
		private Collection<Interval> extractIntervals(Collection<Integer> frames) {

			Vector<Interval> intervals = new Vector<Interval>();

			int previous = Integer.MIN_VALUE;
			int begin = -1;
			int end = 0;

			for (int i : frames) {

				if (previous == i - 1) {

					end = i;
					previous = i;

				} else {

					if (begin != -1) {
						intervals.add(new Interval(begin, end));
					}

					begin = i;
					end = i;
					previous = i;
				}

			}

			if (begin != -1)
				intervals.add(new Interval(begin, end));

			return intervals;

		}

	}

	/**
	 * Paint markers.
	 * 
	 * @param labels
	 *            the labels
	 * @param g
	 *            the g
	 * @param pretransform
	 *            the pretransform
	 * @param shape
	 *            the shape
	 * @param offset
	 *            the offset
	 */
	protected void paintMarkers(Collection<Integer> labels, Graphics2D g,
			AffineTransform pretransform, Shape shape, double offset) {

		AffineTransform old = g.getTransform();

		Point2D p = new Point2D.Double();
		for (Integer i : labels) {
			p.setLocation(i, offset);
			pretransform.transform(p, p);
			g.translate(p.getX(), p.getY());
			g.draw(shape);
			g.setTransform(old);
		}

	}

	/** The name. */
	protected String name;

	/** The bounds. */
	protected Rectangle2D bounds = new Rectangle2D.Double();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.figure.PlotObject#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.figure.PlotObject#getBounds()
	 */
	@Override
	public Rectangle2D getBounds() {
		return bounds;
	};

}
