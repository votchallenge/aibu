package si.vicos.annotations;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * The Class Context.
 * 
 * @param <E>
 *            the element type
 */
public abstract class Context<E> {

	/**
	 * Gets the image represented by a given UID.
	 * 
	 * @param entry
	 *            the entry
	 * 
	 * @return the image or <code>null</code>.
	 */
	public Image getImage(E entry) {

		File imageFile = getImageFile(entry);

		if (!imageFile.exists())
			return null;

		try {

			Image image = ImageIO.read(imageFile);

			return image;

		} catch (IOException e) {
		}

		return null;
	}

	/**
	 * Gets the image file.
	 * 
	 * @param entry
	 *            the entry
	 * @return the image file
	 */
	public abstract File getImageFile(E entry);

}
