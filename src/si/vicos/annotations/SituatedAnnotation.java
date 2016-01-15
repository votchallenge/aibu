package si.vicos.annotations;

import java.awt.geom.Point2D;

/**
 * The Class SituatedAnnotation.
 */
public abstract class SituatedAnnotation extends Annotation {

	/**
	 * Gets the center.
	 * 
	 * @return the center
	 */
	public abstract Point2D getCenter();

}
