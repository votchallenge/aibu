package si.vicos.annotations.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Stroke;

import org.coffeeshop.swing.figure.FigurePanel;

import si.vicos.annotations.*;
import si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer;

/**
 * This class is an abstract base of specific annotation type editing logic
 * class.
 * 
 * @author lukacu
 */
public abstract class AnnotationEditor extends AnnotationViewer {

	/**
	 * A factory for creating AnnotationEditor objects.
	 * 
	 * @param <E>
	 *            the element type
	 */
	public static interface AnnotationEditorFactory<E extends AnnotationEditor> {

		/**
		 * Gets the name.
		 * 
		 * @return the name
		 */
		public String getName();

		/**
		 * Gets the editor.
		 * 
		 * @param peer
		 *            the peer
		 * @param color
		 *            the color
		 * @return the editor
		 */
		public E getEditor(AnnotationPeer peer, Color color);

	}

	/** The Constant defaultCursor. */
	protected static final Cursor defaultCursor = new Cursor(
			Cursor.DEFAULT_CURSOR);

	/** The Constant pointCursor. */
	protected static final Cursor pointCursor = new Cursor(
			Cursor.CROSSHAIR_CURSOR);

	/** The Constant dragPossibleCursor. */
	protected static final Cursor dragPossibleCursor = new Cursor(
			Cursor.HAND_CURSOR);

	/** The Constant dragCursor. */
	protected static final Cursor dragCursor = new Cursor(Cursor.MOVE_CURSOR);

	/** The Constant selectedStroke. */
	protected static final Stroke selectedStroke = new BasicStroke(2);

	/** The Constant subselectedStroke. */
	protected static final Stroke subselectedStroke = new BasicStroke(3);

	/** The Constant normalStroke. */
	protected static final Stroke normalStroke = new BasicStroke(1);

	/**
	 * Instantiates a new annotation editor.
	 * 
	 * @param peer
	 *            the peer
	 * @param color
	 *            the color
	 */
	public AnnotationEditor(AnnotationPeer peer, Color color) {
		super(peer, color);
	}

	/**
	 * On move.
	 * 
	 * @param source
	 *            the source
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param drag
	 *            the drag
	 * @param modifiers
	 *            the modifiers
	 * @return the cursor
	 */
	public abstract Cursor onMove(FigurePanel source, Point from, Point to,
			boolean drag, int modifiers);

	/**
	 * On click.
	 * 
	 * @param source
	 *            the source
	 * @param position
	 *            the position
	 * @param clicks
	 *            the clicks
	 * @param modifiers
	 *            the modifiers
	 */
	public abstract void onClick(FigurePanel source, Point position,
			int clicks, int modifiers);

	/**
	 * Reset.
	 */
	public abstract void reset();

	/**
	 * Reset input.
	 */
	public abstract void resetInput();

	/**
	 * Gets the current.
	 * 
	 * @return the current
	 */
	public abstract Annotation getCurrent();

	/**
	 * Gets the annotation editor.
	 * 
	 * @param peer
	 *            the peer
	 * @param color
	 *            the color
	 * @return the annotation editor
	 */
	public static AnnotationEditor getAnnotationEditor(AnnotationPeer peer,
			Color color) {

		Annotation a = peer.getAnnotation();

		if (a == null)
			return null;
		// if (a instanceof CodeAnnotation) return null;

		if (a instanceof PointAnnotation)
			return new PointAnnotationEditor(peer, color);
		if (a instanceof RectangleAnnotation)
			return new RectangleAnnotationEditor(peer, color);
		if (a instanceof PolygonAnnotation)
			return new PolygonAnnotationEditor(peer, color);
		if(a instanceof SegmentationMaskAnnotation)
			return new SegmentationMaskAnnotationEditor(peer, color);

		return null;
	}

}