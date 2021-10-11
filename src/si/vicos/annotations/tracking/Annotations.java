package si.vicos.annotations.tracking;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import si.vicos.annotations.Annotation;

/**
 * The Interface Annotations.
 */
public interface Annotations extends AnnotationList {

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public abstract String getName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AnnotationList#get(int)
	 */
	public abstract Annotation get(int frame);

	/**
	 * Gets the tags.
	 * 
	 * @param frame
	 *            the frame
	 * @return the tags
	 */
	public abstract Set<String> getTags(int frame);

	/**
	 * Checks for tag.
	 * 
	 * @param index
	 *            the index
	 * @param tag
	 *            the tag
	 * @return true, if successful
	 */
	public abstract boolean hasTag(int index, String tag);

	/**
	 * Gets the value keys.
	 * 
	 * @return the value keys
	 */
	public abstract Set<String> getValueKeys();

	/**
	 * Gets the tags.
	 * 
	 * @return the tags
	 */
	public abstract Set<String> getTags();

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AnnotationList#size()
	 */
	public abstract int size();

	/**
	 * Find tag.
	 * 
	 * @param tag
	 *            the tag
	 * @return the collection
	 */
	public abstract Collection<Integer> findTag(String tag);

	/**
	 * Count tag occurences.
	 * 
	 * @param tag
	 *            the tag
	 * @return the int
	 */
	public abstract int countTagOccurences(String tag);

	/**
	 * Find values.
	 * 
	 * @param key
	 *            the key
	 * @return the list
	 */
	public abstract List<String> findValues(String key);

}
