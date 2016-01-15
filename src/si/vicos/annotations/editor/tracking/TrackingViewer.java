package si.vicos.annotations.editor.tracking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.coffeeshop.application.Application;
import org.coffeeshop.awt.ColorIterator;
import org.coffeeshop.settings.PrefixProxySettings;
import org.coffeeshop.settings.Settings;
import org.coffeeshop.string.StringUtils;
import org.coffeeshop.swing.PersistentWindow;
import org.coffeeshop.swing.ToolTipAction;
import org.coffeeshop.swing.figure.FigurePanel;
import org.coffeeshop.swing.figure.PlotObject;
import org.coffeeshop.swing.viewers.Timeline;
import org.coffeeshop.swing.viewers.Timeline.TimelineListener;

import si.vicos.annotations.editor.AnnotatedImageFigure;
import si.vicos.annotations.editor.AnnotatedImageFigure.ReadOnlyFrameAnnotationPeer;
import si.vicos.annotations.editor.AnnotationEditor;
import si.vicos.annotations.editor.AnnotationViewer;
import si.vicos.annotations.tracking.Annotations;

/**
 * The Class TrackingViewer.
 */
public class TrackingViewer extends PersistentWindow {

	/** The Constant PREVIOUS_STROKE. */
	private static final KeyStroke PREVIOUS_STROKE = KeyStroke.getKeyStroke(
			KeyEvent.VK_W, 0);

	/** The Constant NEXT_STROKE. */
	private static final KeyStroke NEXT_STROKE = KeyStroke.getKeyStroke(
			KeyEvent.VK_E, 0);

	/** The Constant FIRST_STROKE. */
	private static final KeyStroke FIRST_STROKE = KeyStroke.getKeyStroke(
			KeyEvent.VK_Q, 0);

	/** The Constant LAST_STROKE. */
	private static final KeyStroke LAST_STROKE = KeyStroke.getKeyStroke(
			KeyEvent.VK_R, 0);

	/**
	 * The Enum ValueType.
	 */
	private static enum ValueType {

		/** The label. */
		LABEL,
		/** The numerical. */
		NUMERICAL,
		/** The strings. */
		STRINGS,
		/** The points. */
		POINTS
	}

	/** The cached colors. */
	private static Hashtable<String, Color> cachedColors = new Hashtable<String, Color>();

	/** The unique color iterator. */
	private static ColorIterator uniqueColorIterator = new ColorIterator.HueColorIterator();

	/**
	 * Gets the unique color.
	 * 
	 * @param hint
	 *            the hint
	 * @return the unique color
	 */
	public Color getUniqueColor(String hint) {

		Color color = cachedColors.get(hint);

		if (color != null)
			return color;

		color = uniqueColorIterator.next();

		cachedColors.put(hint, color);

		return color;

	}

	/**
	 * The Class ViewPeer.
	 */
	private class ViewPeer extends ReadOnlyFrameAnnotationPeer {

		/**
		 * Instantiates a new view peer.
		 * 
		 * @param annotations
		 *            the annotations
		 * @param index
		 *            the index
		 */
		public ViewPeer(Annotations annotations, int index) {

			super(annotations, index);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.AnnotatedImageFigure.
		 * ReadOnlyFrameAnnotationPeer#repaint()
		 */
		public void repaint() {
			if (panel != null)
				panel.repaint();
		}

	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The first. */
	private Action first = new ToolTipAction("First", "go-first-16.png") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			timeline.setPosition(0);

		}
	};

	/** The previous. */
	private Action previous = new ToolTipAction("Previous",
			"go-previous-16.png") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			timeline.setPosition(timeline.getPosition() - 1);

		}
	};

	/** The next. */
	private Action next = new ToolTipAction("Next", "go-next-16.png") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			timeline.setPosition(timeline.getPosition() + 1);

		}
	};

	/** The last. */
	private Action last = new ToolTipAction("Last", "go-last-16.png") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			timeline.setPosition(model.length - 1);

		}
	};

	/** The panel. */
	private FigurePanel panel = new FigurePanel();

	/** The timeline. */
	private Timeline timeline;

	/** The provider. */
	private ImagesProvider provider;

	/** The model. */
	private ViewerTreeModel model;

	/** The values. */
	private DefaultTreeModel values;

	/** The tree. */
	private JTree tree;

	/**
	 * The Class ViewerTreeCellRenderer.
	 */
	class ViewerTreeCellRenderer extends DefaultTreeCellRenderer {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** The value label. */
		JLabel valueLabel = new JLabel();

		/** The source label. */
		JLabel sourceLabel = new JLabel();

		/**
		 * Instantiates a new viewer tree cell renderer.
		 */
		public ViewerTreeCellRenderer() {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent
		 * (javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int,
		 * boolean)
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			Component returnValue = null;

			if (value == null)
				return null;

			if (value instanceof ViewerTreeModel.AnnotationSource) {

				ViewerTreeModel.AnnotationSource obj = (ViewerTreeModel.AnnotationSource) value;

				sourceLabel.setText(value.toString());

				sourceLabel
						.setBackground(selected ? getBackgroundSelectionColor()
								: getBackgroundNonSelectionColor());

				returnValue = sourceLabel;

			}

			if (value instanceof ViewerTreeModel.AnnotationSource.ValuesAnnotations) {

				ViewerTreeModel.AnnotationSource.ValuesAnnotations obj = (ViewerTreeModel.AnnotationSource.ValuesAnnotations) value;

				valueLabel.setText(value.toString());

				valueLabel
						.setBackground(selected ? getBackgroundSelectionColor()
								: getBackgroundNonSelectionColor());

				valueLabel.setFont(obj.isPlotVisible() ? getFont().deriveFont(
						Font.BOLD) : getFont());

				returnValue = valueLabel;

			}

			if (returnValue == null) {
				returnValue = super.getTreeCellRendererComponent(tree, value,
						selected, expanded, leaf, row, hasFocus);
			}
			return returnValue;
		}
	}

	/**
	 * The Class ViewerTreeModel.
	 */
	private class ViewerTreeModel extends TagPlot implements TreeNode {

		/** The crash shape. */
		private Shape crashShape = TagPlot.getMarker(MarkerShape.CROSS, 4);

		/** The initialization shape. */
		private Shape initializationShape = TagPlot.getMarker(
				MarkerShape.CIRCLE, 4);

		/** The failure shape. */
		private Shape failureShape = TagPlot.getMarker(MarkerShape.DIAMOND, 4);

		/**
		 * Instantiates a new viewer tree model.
		 */
		public ViewerTreeModel() {
			this.bounds.setRect(0, 0, 1, 1);
		}

		/**
		 * The Class AnnotationSource.
		 */
		private class AnnotationSource extends TransferableAnnotations
				implements TreeNode {

			/**
			 * The Class ValuesAnnotations.
			 */
			private class ValuesAnnotations implements TreeNode {

				/** The key. */
				private String key;

				/** The type. */
				private ValueType type;

				/** The plot. */
				private PlotObject plot;

				/**
				 * Instantiates a new values annotations.
				 * 
				 * @param key
				 *            the key
				 * @param type
				 *            the type
				 */
				public ValuesAnnotations(String key, ValueType type) {
					this.key = key;
					this.type = type;
					this.plot = null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see javax.swing.tree.TreeNode#children()
				 */
				@Override
				public Enumeration<?> children() {
					return null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see javax.swing.tree.TreeNode#getAllowsChildren()
				 */
				@Override
				public boolean getAllowsChildren() {
					return false;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see javax.swing.tree.TreeNode#getChildAt(int)
				 */
				@Override
				public TreeNode getChildAt(int arg0) {
					return null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see javax.swing.tree.TreeNode#getChildCount()
				 */
				@Override
				public int getChildCount() {
					return 0;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
				 */
				@Override
				public int getIndex(TreeNode arg0) {
					return 0;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see javax.swing.tree.TreeNode#getParent()
				 */
				@Override
				public TreeNode getParent() {
					return AnnotationSource.this;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see javax.swing.tree.TreeNode#isLeaf()
				 */
				@Override
				public boolean isLeaf() {
					return true;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Object#toString()
				 */
				@Override
				public String toString() {
					return key + " [" + type.name() + "]";
				}

				/**
				 * Toggle plot.
				 */
				public void togglePlot() {

					if (plot != null) {
						timeline.getTrack(key).removeObject(plot);
						plot = null;
						return;
					}

					switch (type) {
					case NUMERICAL:
						plot = new ValuePlot(getAnnotations(), key, color);
						break;
					case STRINGS:
						break;
					case LABEL:
						break;
					case POINTS:
						break;
					default:
						break;

					}

					if (plot != null)
						timeline.getTrack(key).addObject(plot);
				}

				/**
				 * Checks if is plot visible.
				 * 
				 * @return true, if is plot visible
				 */
				public boolean isPlotVisible() {
					return plot != null;
				}

			}

			/** The values. */
			private Vector<ValuesAnnotations> values = new Vector<ValuesAnnotations>();

			/** The failures. */
			private Collection<Integer> initializations, crashes, failures;

			/** The color. */
			private Color color;

			/**
			 * Instantiates a new annotation source.
			 * 
			 * @param name
			 *            the name
			 * @param annotations
			 *            the annotations
			 * @param color
			 *            the color
			 * @param ancestry
			 *            the ancestry
			 */
			public AnnotationSource(String name, Annotations annotations,
					Color color, String ancestry) {
				super(name, annotations, color, ancestry);

				this.color = color;

				initializations = annotations.findTag("initialization");
				crashes = annotations.findTag("crash");
				failures = annotations.findTag("failure");

				Vector<String> labelKeys = new Vector<String>(
						annotations.getTags());

				Collections.sort(labelKeys);

				for (String label : labelKeys) {
					values.add(new ValuesAnnotations(label, ValueType.LABEL));
				}

				Vector<String> valueKeys = new Vector<String>(
						annotations.getValueKeys());

				Collections.sort(valueKeys);

				for (String key : valueKeys) {
					values.add(new ValuesAnnotations(key, ValueType.NUMERICAL));
				}

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.tree.TreeNode#children()
			 */
			@Override
			public Enumeration<?> children() {
				return values.elements();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.tree.TreeNode#getAllowsChildren()
			 */
			@Override
			public boolean getAllowsChildren() {
				return true;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.tree.TreeNode#getChildAt(int)
			 */
			@Override
			public TreeNode getChildAt(int childIndex) {
				return values.elementAt(childIndex);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.tree.TreeNode#getChildCount()
			 */
			@Override
			public int getChildCount() {
				return values.size();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
			 */
			@Override
			public int getIndex(TreeNode node) {
				return values.indexOf(node);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.tree.TreeNode#getParent()
			 */
			@Override
			public TreeNode getParent() {
				return ViewerTreeModel.this;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.tree.TreeNode#isLeaf()
			 */
			@Override
			public boolean isLeaf() {
				return getChildCount() == 0;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				return getName();
			}

			/**
			 * Gets the color.
			 * 
			 * @return the color
			 */
			public Color getColor() {

				return color;

			}

		}

		/** The length. */
		int length = 0;

		/** The instances. */
		private Vector<AnnotationSource> instances = new Vector<AnnotationSource>();

		/**
		 * Gets the length.
		 * 
		 * @return the length
		 */
		public int getLength() {
			return length;
		}

		/**
		 * Adds the.
		 * 
		 * @param name
		 *            the name
		 * @param annotations
		 *            the annotations
		 * @param colorHint
		 *            the color hint
		 * @param ancestry
		 *            the ancestry
		 */
		public void add(String name, Annotations annotations, String colorHint,
				String ancestry) {

			if (!instances.isEmpty()
					&& !StringUtils.same(getAncestry(), ancestry))
				return;

			for (TransferableAnnotations a : instances) {
				if (a.getAnnotations() == annotations)
					return;
			}

			instances.add(new AnnotationSource(name, annotations,
					getUniqueColor(colorHint), ancestry));

			length = Math.max(length, annotations.size());
			this.bounds.setRect(0, 0, getLength(), 1);

			timeline.setLength(length);
			timeline.repaint();
			timeline.getTrack("labels").addObject(this);

			values.reload(this);

		}

		/**
		 * Removes the.
		 * 
		 * @param annotations
		 *            the annotations
		 */
		public void remove(Annotations annotations) {

			TransferableAnnotations remove = null;

			for (TransferableAnnotations a : instances) {
				if (a.getAnnotations() == annotations) {
					remove = a;
					break;
				}
			}

			if (remove != null) {
				instances.remove(remove);

				values.reload(this);
			}

		}

		/**
		 * Gets the ancestry.
		 * 
		 * @return the ancestry
		 */
		public String getAncestry() {

			if (instances.isEmpty())
				return null;

			return instances.firstElement().getAncestry();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.tree.TreeNode#children()
		 */
		@Override
		public Enumeration<?> children() {
			return instances.elements();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.tree.TreeNode#getAllowsChildren()
		 */
		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.tree.TreeNode#getChildAt(int)
		 */
		@Override
		public TreeNode getChildAt(int childIndex) {
			return instances.elementAt(childIndex);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.tree.TreeNode#getChildCount()
		 */
		@Override
		public int getChildCount() {
			return instances.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
		 */
		@Override
		public int getIndex(TreeNode node) {
			return instances.indexOf(node);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.tree.TreeNode#getParent()
		 */
		@Override
		public TreeNode getParent() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.tree.TreeNode#isLeaf()
		 */
		@Override
		public boolean isLeaf() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getAncestry();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.coffeeshop.swing.figure.PlotObject#paint(java.awt.Graphics2D,
		 * float, java.awt.geom.AffineTransform)
		 */
		@Override
		public void paint(Graphics2D g, float scale,
				AffineTransform pretransform) {

			float step = 1.0f / (float) (instances.size() + 2);

			float current = step;

			for (AnnotationSource instance : instances) {

				g.setColor(instance.getColor());

				if (instance.initializations != null)
					this.paintMarkers(instance.initializations, g,
							pretransform, initializationShape, current);

				if (instance.failures != null)
					this.paintMarkers(instance.failures, g, pretransform,
							failureShape, current);

				if (instance.crashes != null)
					this.paintMarkers(instance.crashes, g, pretransform,
							crashShape, current);

				current += step;

			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.coffeeshop.swing.figure.PlotObject#getToolTip(java.awt.geom.Point2D
		 * )
		 */
		@Override
		public String getToolTip(Point2D point) {

			int frame = (int) point.getX();

			return null;
		}
	};

	/** The updater. */
	private TimelineListener updater = new TimelineListener() {

		@Override
		public void positionChanged(Timeline timeline, int frame) {

			setFrame(frame);

		}

		@Override
		public void selectionChanged(Timeline timeline, int begin, int end) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * Instantiates a new tracking viewer.
	 * 
	 * @param provider
	 *            the provider
	 */
	public TrackingViewer(ImagesProvider provider) {

		super("org.annotations.TrackingViewer", "Viewer");

		setLayout(new BorderLayout());

		this.provider = provider;

		this.model = new ViewerTreeModel();

		Settings settings = Application.getApplication().getSettingsManager()
				.getSettings("aibu.viewer.ini");

		this.timeline = new Timeline(1, new PrefixProxySettings(settings,
				"timeline@"));
		this.timeline.getTrack("labels").addObject(model);

		this.values = new DefaultTreeModel(model);

		this.tree = new JTree(this.values);
		this.tree.setRootVisible(false);
		this.tree.setCellRenderer(new ViewerTreeCellRenderer());

		tree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					Object selected = selPath.getLastPathComponent();
					if (e.getClickCount() == 2
							&& selected instanceof ViewerTreeModel.AnnotationSource.ValuesAnnotations) {

						ViewerTreeModel.AnnotationSource.ValuesAnnotations value = (ViewerTreeModel.AnnotationSource.ValuesAnnotations) selected;

						value.togglePlot();

					}
				}
			}
		});

		JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		horizontalSplit.setLeftComponent(panel);

		horizontalSplit.setRightComponent(new JScrollPane(this.tree));

		horizontalSplit.setResizeWeight(0.9);

		horizontalSplit.setDividerLocation(0.5);

		JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		verticalSplit.setTopComponent(horizontalSplit);

		verticalSplit.setBottomComponent(timeline);

		verticalSplit.setResizeWeight(0.9);

		verticalSplit.setDividerLocation(0.8);

		add(verticalSplit, BorderLayout.CENTER);

		JToolBar menu = new JToolBar(JToolBar.HORIZONTAL);

		menu.setFloatable(false);

		menu.add(first);
		menu.add(previous);
		menu.add(next);
		menu.add(last);

		add(menu, BorderLayout.NORTH);

		initKeyboardShortcuts();

		timeline.addTimelineListener(updater);

		timeline.setBorder(BorderFactory.createLoweredBevelBorder());

		timeline.setPosition(0);
		timeline.setToolTipText("test");

		setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE,
				new DropTargetAdapter() {

					@Override
					public void drop(DropTargetDropEvent event) {
						try {
							Transferable tr = event.getTransferable();
							Properties properties = (Properties) tr
									.getTransferData(TransferableAnnotations.ANNOTATIONS_PROPERTIES_FLAVOR);

							String color = properties.getProperty("color");

							Annotations annotations = (Annotations) tr
									.getTransferData(TransferableAnnotations.ANNOTATIONS_FLAVOR);

							String name = properties.getProperty("name");
							String ancestry = properties
									.getProperty("ancestry");

							if (!model.instances.isEmpty()
									&& !StringUtils.same(ancestry,
											model.getAncestry())) {
								event.rejectDrop();
								return;
							}

							if (annotations != null) {
								event.acceptDrop(DnDConstants.ACTION_COPY);

								add(name, annotations, color, ancestry);

								event.dropComplete(true);
								return;
							}
							event.rejectDrop();
						} catch (Exception e) {
							e.printStackTrace();
							event.rejectDrop();
						}
					}
				}, true));
	}

	/**
	 * Inits the keyboard shortcuts.
	 */
	private void initKeyboardShortcuts() {

		getRootPane().getActionMap().put("previous", previous);
		getRootPane().getActionMap().put("next", next);
		getRootPane().getActionMap().put("first", first);
		getRootPane().getActionMap().put("last", last);

		InputMap im = getRootPane().getInputMap(
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		im.put(PREVIOUS_STROKE, "previous");
		im.put(NEXT_STROKE, "next");
		im.put(FIRST_STROKE, "first");
		im.put(LAST_STROKE, "last");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.PersistentWindow#defaultState()
	 */
	@Override
	protected void defaultState() {

	}

	/**
	 * Sets the frame.
	 * 
	 * @param frame
	 *            the new frame
	 */
	private void setFrame(int frame) {

		AnnotationViewer[] viewers = new AnnotationViewer[model.instances
				.size()];

		for (int i = 0; i < model.instances.size(); i++) {
			ViewerTreeModel.AnnotationSource instance = model.instances.get(i);
			viewers[i] = AnnotationEditor.getAnnotationEditor(new ViewPeer(
					instance.getAnnotations(), frame), instance.getColor());
		}

		File file = provider.getImage(frame);
		Image image = null;

		if (file != null && file.exists()) {
			try {
				image = ImageIO.read(file);
			} catch (IOException e) {
			}
		}

		AnnotatedImageFigure figure = new AnnotatedImageFigure(image, viewers);

		panel.setFigure(figure);

		// updateAnnotations();

		if (model.instances.size() == 0) {
			setTitle("Frame " + frame + "/" + model.length);
		} else if (model.instances.size() == 1) {
			setTitle("Frame " + frame + "/" + model.length + " ("
					+ model.instances.firstElement().getName() + ")");
		} else {
			setTitle("Frame " + frame + "/" + model.length + " (comparing "
					+ model.instances.size() + " sets)");

		}

	}

	/**
	 * Adds the.
	 * 
	 * @param name
	 *            the name
	 * @param annotations
	 *            the annotations
	 * @param color
	 *            the color
	 * @param ancestry
	 *            the ancestry
	 */
	public void add(String name, Annotations annotations, String color,
			String ancestry) {

		model.add(name, annotations, color, ancestry);

		setFrame(timeline.getPosition()); // Refresh the viewer
	}

	/**
	 * Removes the.
	 * 
	 * @param annotations
	 *            the annotations
	 */
	public void remove(Annotations annotations) {
		model.remove(annotations);

		setFrame(timeline.getPosition()); // Refresh the viewer
	}

}
