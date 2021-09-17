package si.vicos.annotations.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.coffeeshop.swing.ImageStore.ImageProvider;

/**
 * The Class FontImageProvider.
 */
public class FontImageProvider implements ImageProvider {

	/** The font. */
	private Font font;

	/** The size. */
	private Dimension size;

	/** The prefix. */
	private String prefix;

	/** Color of the foreground */
	private Color color;

	/**
	 * Instantiates a new font image provider.
	 * 
	 * @param prefix
	 *            the prefix
	 * @param font
	 *            the font
	 * @param size
	 *            the size
	 */
	public FontImageProvider(String prefix, Font font, Dimension size, Color color) {
		this.font = font;

		this.size = new Dimension(size);

		this.prefix = prefix;

		this.color = color;
	}

	/**
	 * Instantiates a new font image provider.
	 * 
	 * @param prefix
	 *            the prefix
	 * @param base
	 *            the base
	 * @param name
	 *            the name
	 * @param size
	 *            the size
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FontFormatException
	 *             the font format exception
	 */
	public FontImageProvider(String prefix, Class<?> base, String name,
			Dimension size, Color color) throws IOException, FontFormatException {

		Font font = Font.createFont(Font.TRUETYPE_FONT,
				base.getResourceAsStream(name));

		this.font = font;

		this.size = new Dimension(size);

		this.prefix = prefix;

		this.color = color;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.coffeeshop.swing.ImageStore.ImageProvider#loadImage(java.lang.String)
	 */
	@Override
	public Image loadImage(String name) {
		if (name == null || !name.startsWith(prefix))
			return null;

		String text = name.substring(prefix.length());

		BufferedImage img = new BufferedImage(size.width, size.height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = img.createGraphics();
		g.setFont(font);
		g.setBackground(new Color(0, true));
		g.clearRect(0, 0, size.width, size.height);

		Rectangle2D bounds = font.getStringBounds(text,
				g.getFontRenderContext());
		g.setColor(this.color);
		double scaling = Math.min(size.getWidth() / bounds.getWidth(),
				size.getHeight() / bounds.getHeight()) * 0.9;

		AffineTransform transform = AffineTransform.getTranslateInstance(0, 0);
		transform.translate(size.getWidth() / 2, size.getHeight() / 2);
		transform.scale(scaling, scaling);
		transform.translate(-bounds.getWidth() / 2, -bounds.getY() / 2);
		g.setTransform(transform);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// System.out.println(text + " " + scaling + " " + bounds);
		// g.drawString(text, size.width / 2 -(int) bounds.getX() - (int)
		// (scaling * bounds.getWidth() / 2), -(int) bounds.getY());
		// g.drawString(text, 0, (int) (bounds.getHeight() - bounds.getY()));
		g.drawString(text, 0, 0);
		return img;
	}

}
