package si.vicos.annotations.tracking;

import java.awt.Image;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.AnnotationsMetadata;
import si.vicos.annotations.Context;

/**
 * The Class AbstractAnnotatedSequence.
 */
public abstract class AbstractAnnotatedSequence extends Context<Integer>
		implements AnnotationsMetadata, Annotations {

	/**
	 * Removes the suffix.
	 * 
	 * @param str
	 *            the str
	 * @param suffix
	 *            the suffix
	 * @return the string
	 */
	public static final String removeSuffix(String str, String suffix) {

		if (!str.endsWith(suffix))
			return str;

		return str.substring(0, str.length() - suffix.length());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.Annotations#getName()
	 */
	public abstract String getName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.Annotations#get(int)
	 */
	public abstract Annotation get(int frame);

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.Annotations#getTags(int)
	 */
	public abstract Set<String> getTags(int frame);

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.Annotations#hasTag(int,
	 * java.lang.String)
	 */
	public abstract boolean hasTag(int index, String tag);

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.Annotations#getValueKeys()
	 */
	public abstract Set<String> getValueKeys();

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.Annotations#getTags()
	 */
	public abstract Set<String> getTags();

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.Annotations#size()
	 */
	public abstract int size();

	/**
	 * Gets the directory.
	 * 
	 * @return the directory
	 */
	public abstract File getDirectory();

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.AnnotationsMetadata#getPreviewImage()
	 */
	public abstract Image getPreviewImage();

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.Annotations#findTag(java.lang.String)
	 */
	public abstract Collection<Integer> findTag(String tag);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.Annotations#countTagOccurences(java.lang
	 * .String)
	 */
	public abstract int countTagOccurences(String tag);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.Annotations#findValues(java.lang.String)
	 */
	public abstract List<String> findValues(String key);

}
