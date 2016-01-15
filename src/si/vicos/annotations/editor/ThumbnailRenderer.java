package si.vicos.annotations.editor;

import java.awt.image.BufferedImage;

/**
 * The Interface ThumbnailRenderer.
 */
public interface ThumbnailRenderer {

	/**
	 * Render.
	 * 
	 * @param obj
	 *            the obj
	 * @return the buffered image
	 */
	public BufferedImage render(Object obj);

	/**
	 * Gets the width.
	 * 
	 * @return the width
	 */
	public int getWidth();

	/**
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public int getHeight();

}
