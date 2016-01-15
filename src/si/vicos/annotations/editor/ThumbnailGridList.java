package si.vicos.annotations.editor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.coffeeshop.cache.Cache;

import si.vicos.annotations.editor.ThumbnailGenerator.ThumbnailGeneratorCallback;

/**
 * The Class ThumbnailGridList.
 * 
 * @param <E>
 *            the element type
 */
public class ThumbnailGridList<E> extends JList<E> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The border. */
	private Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);

	/**
	 * The Class ThumbnailCell.
	 */
	private class ThumbnailCell extends JPanel implements
			ThumbnailGeneratorCallback {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** The index. */
		private int index;

		/** The object. */
		private Object object;

		/** The failure. */
		private boolean failure = false;

		/** The focused. */
		private boolean focused;

		/**
		 * Instantiates a new thumbnail cell.
		 * 
		 * @param object
		 *            the object
		 * @param index
		 *            the index
		 * @param selected
		 *            the selected
		 * @param focused
		 *            the focused
		 */
		public ThumbnailCell(Object object, int index, boolean selected,
				boolean focused) {

			this.object = object;

			this.index = index;

			setToolTipText(object.toString());

			setBackground(selected ? getSelectionBackground()
					: ThumbnailGridList.this.getBackground());

			this.focused = focused;

			setPreferredSize(itemSize);

			setBorder(border);

			repaint(1000L);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			if (failure)
				return;

			BufferedImage img = generator.generate(object, this);

			if (img != null) {

				Insets insets = getInsets();

				float scale = Math.max(
						(float) (getWidth() - insets.left - insets.right)
								/ (float) img.getWidth(), (float) (getHeight()
								- insets.bottom - insets.top)
								/ (float) img.getHeight());

				int w = (int) ((float) img.getWidth() * scale);
				int h = (int) ((float) img.getHeight() * scale);
				int x = (getWidth() + insets.left - w) / 2;
				int y = (getHeight() + insets.top - h) / 2;

				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);

				g.setClip(insets.left, insets.top, getWidth() - insets.left
						- insets.right, getHeight() - insets.bottom
						- insets.top);

				g.drawImage(img, x, y, w, h, 0, 0, img.getWidth(),
						img.getHeight(), this);

			} else {

				String text = object.toString();

				Rectangle2D b = g.getFontMetrics().getStringBounds(text, g);

				g.drawString(text, (int) (getWidth() - b.getWidth()) / 2,
						(int) (getHeight() - b.getHeight()) / 2);

			}

			if (focused) {

				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * si.vicos.annotations.editor.ThumbnailGenerator.ThumbnailGeneratorCallback
		 * #failed()
		 */
		@Override
		public void failed() {
			ThumbnailGridList.this.repaint(getCellBounds(index, index + 1));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * si.vicos.annotations.editor.ThumbnailGenerator.ThumbnailGeneratorCallback
		 * #retrieved(java.awt.image.BufferedImage)
		 */
		@Override
		public void retrieved(BufferedImage image) {
			ThumbnailGridList.this.repaint(getCellBounds(index, index + 1));
		}

	}

	/** The generator. */
	private ThumbnailGenerator generator;

	/** The item size. */
	private Dimension itemSize;

	/**
	 * Instantiates a new thumbnail grid list.
	 * 
	 * @param data
	 *            the data
	 * @param itemSize
	 *            the item size
	 * @param renderer
	 *            the renderer
	 * @param cache
	 *            the cache
	 */
	public ThumbnailGridList(ListModel<E> data, Dimension itemSize,
			ThumbnailRenderer renderer, Cache<Object, BufferedImage> cache) {

		super(data);

		generator = new ThumbnailGenerator(renderer, cache);

		this.itemSize = new Dimension(itemSize);

		setLayoutOrientation(JList.HORIZONTAL_WRAP);
		setVisibleRowCount(-1);

		setCellRenderer(new ListCellRenderer<E>() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				return new ThumbnailCell(value, index, isSelected, cellHasFocus);
			}
		});

		data.addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent arg0) {

			}

			@Override
			public void intervalAdded(ListDataEvent arg0) {

			}

			@Override
			public void contentsChanged(ListDataEvent a) {
				if (a.getSource() != getModel())
					return;

				for (int i = a.getIndex0(); i < a.getIndex1(); i++)
					generator.invalidate(getModel().getElementAt(i));
			}
		});

	}

}
