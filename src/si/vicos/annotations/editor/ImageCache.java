package si.vicos.annotations.editor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.coffeeshop.cache.DataCache;
import org.coffeeshop.io.TempDirectory;

/**
 * The Class ImageCache.
 */
public class ImageCache extends DataCache<Object, BufferedImage> {

	/**
	 * Instantiates a new image cache.
	 * 
	 * @param memoryLimit
	 *            the memory limit
	 * @param totalLimit
	 *            the total limit
	 * @param tempDir
	 *            the temp dir
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ImageCache(long memoryLimit, long totalLimit, TempDirectory tempDir)
			throws IOException {
		super(memoryLimit, totalLimit, tempDir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.cache.DataCache#getDataLength(java.lang.Object)
	 */
	@Override
	protected long getDataLength(BufferedImage object) {
		if (object == null)
			return 0;
		return object.getWidth() * object.getHeight()
				* object.getColorModel().getNumComponents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.cache.DataCache#readData(java.io.File, long)
	 */
	@Override
	protected BufferedImage readData(File file, long length) throws IOException {
		return ImageIO.read(file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.cache.DataCache#writeData(java.io.File,
	 * java.lang.Object)
	 */
	@Override
	protected void writeData(File file, BufferedImage data) throws IOException {
		ImageIO.write(data, "PNG", file);
	}

}
