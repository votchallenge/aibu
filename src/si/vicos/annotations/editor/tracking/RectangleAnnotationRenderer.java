package si.vicos.annotations.editor.tracking;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.editor.AnnotationRenderer;

/**
 * The Class RectangleAnnotationRenderer.
 */
public class RectangleAnnotationRenderer implements AnnotationRenderer {

	/** The shape. */
	private Shape shape;

	/**
	 * Instantiates a new rectangle annotation renderer.
	 * 
	 * @param annotation
	 *            the annotation
	 */
	public RectangleAnnotationRenderer(RectangleAnnotation annotation) {

		shape = new Rectangle((int) annotation.getX(), (int) annotation.getY(),
				(int) annotation.getWidth(), (int) annotation.getHeight());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.AnnotationRenderer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g) {

		g.draw(shape);

	}

}
