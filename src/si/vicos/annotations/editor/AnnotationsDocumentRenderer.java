package si.vicos.annotations.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.PolygonAnnotation;
import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.editor.tracking.PolygonAnnotationRenderer;
import si.vicos.annotations.editor.tracking.RectangleAnnotationRenderer;
import si.vicos.annotations.tracking.AnnotatedSequence.AnnotationsMetadataReader;

/**
 * The Class AnnotationsDocumentRenderer.
 */
public class AnnotationsDocumentRenderer implements ThumbnailRenderer {

	/** The Constant THUMBNAIL_WIDTH. */
	public static final int THUMBNAIL_WIDTH = 256;

	/** The Constant THUMBNAIL_HEIGHT. */
	public static final int THUMBNAIL_HEIGHT = 256;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.ThumbnailRenderer#render(java.lang.Object)
	 */
	@Override
	public BufferedImage render(Object obj) {

		if (obj instanceof File) {

			try {

				AnnotationsMetadataReader metadata = new AnnotationsMetadataReader(
						(File) obj);

				return renderThumbnail(metadata);

			} catch (IOException e) {

			}

			BufferedImage img = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics g = img.getGraphics();

			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());

			g.setColor(Color.BLACK);

			String text = ((File) obj).getName();

			Rectangle2D b = g.getFontMetrics().getStringBounds(text, g);

			g.drawString(text, (int) (getWidth() - b.getWidth()) / 2,
					(int) (getHeight() - b.getHeight()) / 2);

			return img;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.ThumbnailRenderer#getWidth()
	 */
	@Override
	public int getWidth() {
		return THUMBNAIL_WIDTH;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.editor.ThumbnailRenderer#getHeight()
	 */
	@Override
	public int getHeight() {
		return THUMBNAIL_HEIGHT;
	}

	/**
	 * Render thumbnail.
	 * 
	 * @param metadata
	 *            the metadata
	 * @return the buffered image
	 */
	private BufferedImage renderThumbnail(AnnotationsMetadataReader metadata) {

		try {

			Image original = metadata.getPreviewImage();

			Annotation region = metadata.getPreviewRegion();

			AnnotationRenderer renderer = null;

			if (region instanceof RectangleAnnotation) {
				renderer = new RectangleAnnotationRenderer(
						(RectangleAnnotation) region);
			}
			if (region instanceof PolygonAnnotation) {
				renderer = new PolygonAnnotationRenderer(
						(PolygonAnnotation) region);
			}

			float scale = Math
					.min((float) THUMBNAIL_WIDTH
							/ (float) original.getWidth(null),
							(float) THUMBNAIL_HEIGHT
									/ (float) original.getHeight(null));

			int w = (int) ((float) original.getWidth(null) * scale);
			int h = (int) ((float) original.getHeight(null) * scale);

			BufferedImage image = new BufferedImage(w, h,
					BufferedImage.TYPE_INT_RGB);

			Graphics2D g = image.createGraphics();

			g.setTransform(AffineTransform.getScaleInstance(scale, scale));
			g.drawImage(original, 0, 0, null);

			if (renderer != null) {

				g.setColor(Color.RED);
				g.setStroke(new BasicStroke(2 * scale));

				renderer.paint(g);
			}

			return image;

		} catch (Exception e) {
			return null;
		}

	}

}
