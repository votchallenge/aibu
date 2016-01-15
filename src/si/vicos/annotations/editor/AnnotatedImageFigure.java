package si.vicos.annotations.editor;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.coffeeshop.swing.figure.FigureObserver;
import org.coffeeshop.swing.figure.ImageFigure;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.tracking.Annotations;

/**
 * The Class AnnotatedImageFigure.
 */
public class AnnotatedImageFigure extends ImageFigure {

	/**
	 * The Interface AnnotationPeer.
	 */
	public interface AnnotationPeer {

		/**
		 * Repaint.
		 */
		public void repaint();

		/**
		 * Sets the annotation.
		 * 
		 * @param a
		 *            the new annotation
		 */
		public void setAnnotation(Annotation a);

		/**
		 * Gets the annotation.
		 * 
		 * @return the annotation
		 */
		public Annotation getAnnotation();

	}

	/**
	 * The Class ReadOnlyFrameAnnotationPeer.
	 */
	public static class ReadOnlyFrameAnnotationPeer implements AnnotationPeer {

		/** The index. */
		private int index;

		/** The annotations. */
		private Annotations annotations;

		/**
		 * Instantiates a new read only frame annotation peer.
		 * 
		 * @param annotations
		 *            the annotations
		 * @param index
		 *            the index
		 */
		public ReadOnlyFrameAnnotationPeer(Annotations annotations, int index) {

			this.index = index;
			this.annotations = annotations;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer#
		 * getAnnotation()
		 */
		public Annotation getAnnotation() {

			if (index >= annotations.size())
				return new RectangleAnnotation();

			return annotations.get(index);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer#repaint
		 * ()
		 */
		public void repaint() {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer#
		 * setAnnotation(si.vicos.annotations.Annotation)
		 */
		@Override
		public void setAnnotation(Annotation a) {

		}

	}

	/** The Constant PLACEHOLDER. */
	private static final BufferedImage PLACEHOLDER = new BufferedImage(1, 1,
			BufferedImage.TYPE_INT_RGB);

	/** The annotation viewers. */
	private AnnotationViewer[] annotationViewers = null;

	/** The image to screen. */
	private AffineTransform imageToScreen = new AffineTransform();

	/**
	 * Instantiates a new annotated image figure.
	 * 
	 * @param image
	 *            the image
	 * @param viewers
	 *            the viewers
	 */
	public AnnotatedImageFigure(Image image, AnnotationViewer... viewers) {

		super(image == null ? PLACEHOLDER : image);

		this.annotationViewers = viewers;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.figure.ImageFigure#paint(java.awt.Graphics2D,
	 * java.awt.geom.Rectangle2D, java.awt.Rectangle,
	 * org.coffeeshop.swing.figure.FigureObserver)
	 */
	public void paint(Graphics2D g, Rectangle2D figureSize,
			Rectangle windowSize, FigureObserver observer) {

		super.paint(g, figureSize, windowSize, observer);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (annotationViewers == null || annotationViewers.length == 0)
			return;

		g.setClip(windowSize.x, windowSize.y, windowSize.width,
				windowSize.height);

		float scale = (float) windowSize.width / (float) figureSize.getWidth();

		float offsetX = (float) figureSize.getX() * scale
				- (float) windowSize.x;
		float offsetY = (float) figureSize.getY() * scale
				- (float) windowSize.y;

		imageToScreen.setTransform(scale, 0, 0, scale, -offsetX, -offsetY);
		AffineTransform old = g.getTransform();
		old.concatenate(imageToScreen);
		g.setTransform(old);

		for (int i = 0; i < annotationViewers.length; i++) {

			if (annotationViewers[i] == null)
				continue;
			annotationViewers[i].paint(g);
		}

	}

};