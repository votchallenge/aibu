package si.vicos.annotations.editor;

import java.awt.*;
import java.util.Vector;

import javax.swing.JComponent;

import org.coffeeshop.swing.figure.FigurePanel;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.PointAnnotation;
import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.SegmentationMaskAnnotation;
import si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer;

/**
 * The Class RectangleAnnotationEditor.
 */
public class SegmentationMaskAnnotationEditor extends AnnotationEditor {

    /** The Constant FACTORY. */
    public static final AnnotationEditorFactory<SegmentationMaskAnnotationEditor> FACTORY = new AnnotationEditorFactory<SegmentationMaskAnnotationEditor>() {

        @Override
        public String getName() {
            return "Segmentation mask";
        }

        @Override
        public SegmentationMaskAnnotationEditor getEditor(AnnotationPeer peer,
                                                          Color color) {
            return new SegmentationMaskAnnotationEditor(peer, color);
        }

    };

    /** The points. */
    private Vector<Point> points = new Vector<Point>();

    private boolean[][] shape = new boolean[1000][1000];

    /**
     * Instantiates a new segmentation mask annotation editor.
     *
     * @param peer
     *            the peer
     * @param color
     *            the color
     */
    public SegmentationMaskAnnotationEditor(AnnotationPeer peer, Color color) {
        super(peer, color);
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.editor.AnnotationViewer#getComponent()
     */
    @Override
    public JComponent getComponent() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * si.vicos.annotations.editor.AnnotationViewer#paint(java.awt.Graphics2D)
     */
    @Override
    public void paint(Graphics2D g) {
        shape = getAnnotation().getMask();
        int x0 = getAnnotation().getX0();
        int y0 = getAnnotation().getY0();

        if(this.shape == null)
            return;
        g.setColor(color);
        g.setStroke(normalStroke);
        for (int i = 0; i < shape.length; i++)
            for (int j = 0; j < shape[i].length; j++)
            {
                if(shape[i][j]){
                    g.draw(new Rectangle(x0 + j,y0 + i,1, 1));

                }
            }
    }



    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.editor.AnnotationEditor#resetInput()
     */
    @Override
    public void resetInput() {

        points.clear();
        notifyRepaint();

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * si.vicos.annotations.editor.AnnotationEditor#onMove(org.coffeeshop.swing
     * .figure.FigurePanel, java.awt.Point, java.awt.Point, boolean, int)
     */
    public Cursor onMove(FigurePanel source, Point from, Point to,
                         boolean drag, int modifiers) {
        SegmentationMaskAnnotation annotation = getAnnotation();

        if (annotation == null)
            return null;

        if (drag) {
            if(!from.equals(to)) {
                points.add(to);
                annotation.addPoints(points);
                peer.setAnnotation(annotation);
                points.clear();
                updateGraphics();
            }
            return pointCursor;

        }
        return defaultCursor;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * si.vicos.annotations.editor.AnnotationEditor#onClick(org.coffeeshop.swing
     * .figure.FigurePanel, java.awt.Point, int, int)
     */
    public void onClick(FigurePanel source, Point position, int clicks,
                        int modifiers) {
        SegmentationMaskAnnotation annotation = getAnnotation();

        if(position != null)
            points.add(position);

        if (annotation == null)
            return;

        else {
            annotation.addPoints(points);
            peer.setAnnotation(annotation);
            points.clear();
            updateGraphics();
            }

    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.editor.AnnotationViewer#updateGraphics()
     */
    @Override
    public void updateGraphics() {
        SegmentationMaskAnnotation annotation = getAnnotation();

        if (annotation == null)
            return;

        shape = annotation.getMask();



    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.editor.AnnotationEditor#reset()
     */
    @Override
    public void reset() {

        peer.setAnnotation(new RectangleAnnotation());
        resetInput();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * si.vicos.annotations.editor.AnnotationViewer#getToolTip(org.coffeeshop
     * .swing.figure.FigurePanel, java.awt.Point)
     */
    @Override
    public String getToolTip(FigurePanel source, Point position) {
        SegmentationMaskAnnotation annotation = getAnnotation();

        if (annotation != null)
            return String.format("(%d, %d)",position.x,position.y);
        return null;
    }

    /**
     * Gets the annotation.
     *
     * @return the annotation
     */
    private SegmentationMaskAnnotation getAnnotation() {

        Annotation a = peer.getAnnotation();


        if (!(a instanceof SegmentationMaskAnnotation))
            return new SegmentationMaskAnnotation();

        return (SegmentationMaskAnnotation) a;
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.editor.AnnotationEditor#getCurrent()
     */
    @Override
    public Annotation getCurrent() {
        if (points.isEmpty()) {
            return new SegmentationMaskAnnotation();
        }

        return getAnnotation();
    }

}