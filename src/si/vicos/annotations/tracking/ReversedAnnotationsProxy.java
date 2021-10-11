package si.vicos.annotations.tracking;

import java.awt.Image;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import si.vicos.annotations.Annotation;

/**
 * The Class ReversedAnnotationsProxy.
 */
public class ReversedAnnotationsProxy extends AbstractAnnotatedSequence {

	/** The parent. */
	private AbstractAnnotatedSequence parent;

	/**
	 * Instantiates a new reversed annotations proxy.
	 * 
	 * @param parent
	 *            the parent
	 */
	public ReversedAnnotationsProxy(AbstractAnnotatedSequence parent) {
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.AnnotationsMetadata#getMetadata(java.lang.String)
	 */
	public String getMetadata(String name) {
		return parent.getMetadata(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Context#getImage(java.lang.Object)
	 */
	public Image getImage(Integer entry) {
		return parent.getImage(parent.size() - entry - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#getName()
	 */
	public String getName() {
		return parent.getName() + " [reversed]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#get(int)
	 */
	public Annotation get(int frame) {
		return parent.get(parent.size() - frame - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#getTags(int)
	 */
	public Set<String> getTags(int frame) {
		return parent.getTags(parent.size() - frame - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Context#getImageFile(java.lang.Object)
	 */
	public File getImageFile(Integer entry) {
		return parent.getImageFile(parent.size() - entry - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#hasTag(int,
	 * java.lang.String)
	 */
	public boolean hasTag(int index, String tag) {
		return parent.hasTag(parent.size() - index - 1, tag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#getDirectory()
	 */
	public File getDirectory() {
		return parent.getDirectory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#getPreviewImage()
	 */
	public Image getPreviewImage() {
		return parent.getPreviewImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.AnnotationsMetadata#getPreviewRegion()
	 */
	@Override
	public Annotation getPreviewRegion() {
		return parent.getPreviewRegion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#countTagOccurences
	 * (java.lang.String)
	 */
	public int countTagOccurences(String tag) {
		return parent.countTagOccurences(tag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#findTag(java.
	 * lang.String)
	 */
	public Collection<Integer> findTag(String tag) {
		Collection<Integer> labels = parent.findTag(tag);

		if (labels == null)
			return null;

		Vector<Integer> reversed = new Vector<Integer>();

		for (Integer i : labels) {
			reversed.add(parent.size() - i - 1);
		}

		return reversed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#size()
	 */
	public int size() {
		return parent.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.AnnotationsMetadata#getKeys()
	 */
	public Set<String> getKeys() {
		return parent.getKeys();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#getValueKeys()
	 */
	@Override
	public Set<String> getValueKeys() {
		return parent.getValueKeys();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#getTags()
	 */
	@Override
	public Set<String> getTags() {
		return parent.getTags();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#findValues(java
	 * .lang.String)
	 */
	@Override
	public List<String> findValues(String key) {
		List<String> result = parent.findValues(key);
		Collections.reverse(result);
		return result;
	}

}
