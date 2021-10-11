package si.vicos.annotations.tracking;

import java.util.Set;

/**
 * The listener interface for receiving annotatedSequence events. The class that
 * is interested in processing a annotatedSequence event implements this
 * interface, and the object created with that class is registered with a
 * component using the component's
 * <code>addAnnotatedSequenceListener<code> method. When
 * the annotatedSequence event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see AnnotatedSequenceEvent
 */
public interface AnnotatedSequenceListener {

	/**
	 * Interval changed.
	 * 
	 * @param sequence
	 *            the sequence
	 * @param interval
	 *            the interval
	 */
	public void intervalChanged(AnnotatedSequence sequence, Interval interval);

	/**
	 * Metadata changed.
	 * 
	 * @param sequence
	 *            the sequence
	 * @param key
	 *            the key
	 */
	public void metadataChanged(AnnotatedSequence sequence, Set<String> key);

}
