package si.vicos.annotations;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * The Class ShapeAnnotation.
 */
public abstract class ShapeAnnotation extends SituatedAnnotation {

	/**
	 * Gets the bounding box.
	 * 
	 * @return the bounding box
	 */
	public abstract RectangleAnnotation getBoundingBox();

	/**
	 * Gets the polygon.
	 * 
	 * @return the polygon
	 */
	public abstract List<Point2D> getPolygon();

}
