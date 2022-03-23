package si.vicos.annotations.editor;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.coffeeshop.cache.Cache;

/**
 * The Class ThumbnailGenerator.
 */
public class ThumbnailGenerator {

	/**
	 * The Interface ThumbnailGeneratorCallback.
	 */
	public interface ThumbnailGeneratorCallback {

		/**
		 * Retrieved.
		 * 
		 * @param image
		 *            the image
		 */
		public void retrieved(BufferedImage image);

		/**
		 * Failed.
		 */
		public void failed();

	}

	/**
	 * The Class GenerateTask.
	 */
	private class GenerateTask {

		/** The object. */
		private Object object;

		/** The callback. */
		private ThumbnailGeneratorCallback callback;

		/**
		 * Instantiates a new generate task.
		 * 
		 * @param object
		 *            the object
		 * @param callback
		 *            the callback
		 */
		protected GenerateTask(Object object,
				ThumbnailGeneratorCallback callback) {
			super();
			this.object = object;
			this.callback = callback;
		}

	}

	/** The cache. */
	private Cache<Object, BufferedImage> cache = null;

	/** The tasks. */
	private ConcurrentLinkedQueue<GenerateTask> tasks = new ConcurrentLinkedQueue<GenerateTask>();

	/** The fetchers. */
	private LinkedList<FetcherThread> fetchers;

	/**
	 * The Class FetcherThread.
	 */
	private class FetcherThread extends Thread {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			while (true) {

				GenerateTask task = null;

				synchronized (tasks) {
					while (true) {

						if (tasks.isEmpty())
							try {
								tasks.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
								break;
							}
						else
							break;
					}
					task = tasks.poll();
				}

				if (task == null)
					break;

				BufferedImage cachedImage = null;

				if (cache != null)
					cachedImage = cache.query(task.object);

				if (cachedImage == null) {

					try {

						cachedImage = renderer.render(task.object);

						if (cache != null) {
							synchronized (cache) {
								cache.insert(task.object, cachedImage);
							}
						}

					} catch (Exception e) {

						task.callback.failed();

					}

				}

				task.callback.retrieved(cachedImage);

				continue;

			}
		}
	}

	/** The renderer. */
	private ThumbnailRenderer renderer;

	/**
	 * Instantiates a new thumbnail generator.
	 * 
	 * @param renderer
	 *            the renderer
	 * @param cache
	 *            the cache
	 */
	public ThumbnailGenerator(ThumbnailRenderer renderer,
			Cache<Object, BufferedImage> cache) {

		this.cache = cache;

		this.renderer = renderer;

		this.fetchers = new LinkedList<ThumbnailGenerator.FetcherThread>();
		for (int i = 0; i < 2; i++) {
			FetcherThread th = new FetcherThread();
			th.setDaemon(true);
			th.start();

			fetchers.add(th);
		}
	}

	/**
	 * Generate.
	 * 
	 * @param object
	 *            the object
	 * @param callback
	 *            the callback
	 * @return the buffered image
	 */
	public BufferedImage generate(Object object,
			ThumbnailGeneratorCallback callback) {

		synchronized (tasks) {

			synchronized (cache) {

				if (cache != null) {

					BufferedImage cached_img = cache.query(object);

					if (cached_img != null) {
						return cached_img;
					}
				}
			}
			tasks.add(new GenerateTask(object, callback));
			tasks.notifyAll();

			return null;
		}

	}

	/**
	 * Invalidate.
	 * 
	 * @param object
	 *            the object
	 */
	public void invalidate(Object object) {

		synchronized (cache) {

			cache.remove(object);

		}

	}

	/**
	 * Gets the width.
	 * 
	 * @return the width
	 */
	public int getWidth() {
		return renderer.getWidth();
	}

	/**
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public int getHeight() {
		return renderer.getHeight();
	}

}
