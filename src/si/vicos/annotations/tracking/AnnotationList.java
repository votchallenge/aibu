package si.vicos.annotations.tracking;

import si.vicos.annotations.Annotation;

/**
 * The Interface AnnotationList.
 */
public interface AnnotationList {

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public int size();

	/**
	 * Gets the.
	 * 
	 * @param frame
	 *            the frame
	 * @return the annotation
	 */
	public Annotation get(int frame);

}
