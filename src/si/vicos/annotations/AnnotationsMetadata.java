package si.vicos.annotations;

import java.awt.Image;
import java.util.Set;

/**
 * The Interface AnnotationsMetadata.
 */
public interface AnnotationsMetadata {

	/**
	 * Gets the keys.
	 * 
	 * @return the keys
	 */
	public Set<String> getKeys();

	/**
	 * Gets the metadata.
	 * 
	 * @param name
	 *            the name
	 * @return the metadata
	 */
	public String getMetadata(String name);

	/**
	 * Gets the preview image.
	 * 
	 * @return the preview image
	 */
	public Image getPreviewImage();

	/**
	 * Gets the preview region.
	 * 
	 * @return the preview region
	 */
	public Annotation getPreviewRegion();

}
