package si.vicos.annotations.editor.tracking;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.coffeeshop.swing.figure.PlotObject;

import si.vicos.annotations.tracking.Annotations;

/**
 * The Class ValuePlot.
 */
public class ValuePlot implements PlotObject {

	/** The plot. */
	private Path2D.Float plot = new Path2D.Float();

	/** The values. */
	private double[] values;

	/** The color. */
	private Color color;

	/** The bounds. */
	private Rectangle2D bounds = new Rectangle2D.Double();

	/** The key. */
	private String name, key;

	/**
	 * Instantiates a new value plot.
	 * 
	 * @param annotations
	 *            the annotations
	 * @param key
	 *            the key
	 * @param color
	 *            the color
	 */
	public ValuePlot(Annotations annotations, String key, Color color) {
		this(annotations, key, color, Double.MIN_VALUE, Double.MAX_VALUE);
	}

	/**
	 * Instantiates a new value plot.
	 * 
	 * @param annotations
	 *            the annotations
	 * @param key
	 *            the key
	 * @param color
	 *            the color
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 */
	public ValuePlot(Annotations annotations, String key, Color color,
			double min, double max) {

		this.color = color;
		this.name = annotations.getName() + " " + key;
		this.key = key;

		List<String> values = annotations.findValues(key);
		this.values = new double[values.size()];

		int frame = 0;
		boolean broken = true;

		double minValue = Double.MAX_VALUE;
		double maxValue = Double.MIN_VALUE;

		for (String value : values) {

			try {

				if (value != null) {
					double number = Double.parseDouble(value);

					number = Math.min(max, Math.max(min, number));

					minValue = Math.min(minValue, number);
					maxValue = Math.max(maxValue, number);

					if (broken)
						plot.moveTo(frame, number);
					else
						plot.lineTo(frame, number);

					broken = false;

					this.values[frame] = number;

				} else {
					broken = true;
					this.values[frame] = Double.NaN;
				}

			} catch (NumberFormatException e) {
				broken = true;
				this.values[frame] = Double.NaN;
			}

			frame++;
		}

		if (min != Double.MIN_VALUE)
			minValue = min;
		if (max != Double.MAX_VALUE)
			maxValue = max;

		bounds.setRect(0, minValue, annotations.size(), maxValue - minValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.figure.PlotObject#paint(java.awt.Graphics2D,
	 * float, java.awt.geom.AffineTransform)
	 */
	@Override
	public void paint(Graphics2D g, float scale, AffineTransform pretransform) {

		g.setColor(color);

		g.draw(pretransform.createTransformedShape(plot));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.figure.PlotObject#getBounds()
	 */
	@Override
	public Rectangle2D getBounds() {
		return bounds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.figure.PlotObject#getName()
	 */
	@Override
	public String getName() {
		return name;
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
	 * org.coffeeshop.swing.figure.PlotObject#getToolTip(java.awt.geom.Point2D)
	 */
	@Override
	public String getToolTip(Point2D point) {

		int frame = (int) point.getX();

		if (frame < 0 || frame >= values.length)
			return null;

		if (Double.isNaN(values[frame]))
			return null;

		return String.format("%s: %.5f", name, values[frame]);
	}

}
