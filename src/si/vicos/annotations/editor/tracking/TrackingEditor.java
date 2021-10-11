package si.vicos.annotations.editor.tracking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.UndoManager;

import org.coffeeshop.Callback;
import org.coffeeshop.ReferenceCollection.Weak;
import org.coffeeshop.application.Application;
import org.coffeeshop.dialogs.OrganizedSettings;
import org.coffeeshop.dialogs.SettingsGroup;
import org.coffeeshop.dialogs.SettingsValue;
import org.coffeeshop.settings.PrefixProxySettings;
import org.coffeeshop.settings.Settings;
import org.coffeeshop.settings.SettingsNotFoundException;
import org.coffeeshop.settings.WriteableSettings;
import org.coffeeshop.string.StringUtils;
import org.coffeeshop.string.parsers.BooleanStringParser;
import org.coffeeshop.string.parsers.EnumeratedStringParser;
import org.coffeeshop.string.parsers.EnumeratedSubsetStringParser;
import org.coffeeshop.string.parsers.FileStringParser;
import org.coffeeshop.string.parsers.IntegerStringParser;
import org.coffeeshop.string.parsers.ParseException;
import org.coffeeshop.string.parsers.StringStringParser;
import org.coffeeshop.swing.ActionSetManager;
import org.coffeeshop.swing.ImageStore;
import org.coffeeshop.swing.KeyBindingsEditor;
import org.coffeeshop.swing.PersistentWindow;
import org.coffeeshop.swing.SettingsEditor.CommitStrategy;
import org.coffeeshop.swing.SettingsPanel;
import org.coffeeshop.swing.SimpleDialog;
import org.coffeeshop.swing.StatusBar;
import org.coffeeshop.swing.StatusBar.MessageType;
import org.coffeeshop.swing.ToolTipAction;
import org.coffeeshop.swing.figure.ButtonAction;
import org.coffeeshop.swing.figure.FigurePanel;
import org.coffeeshop.swing.figure.FigurePanel.Button;
import org.coffeeshop.swing.figure.MoveAction;
import org.coffeeshop.swing.viewers.FigureViewer;
import org.coffeeshop.swing.viewers.Timeline;
import org.coffeeshop.swing.viewers.Timeline.TimelineListener;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.editor.*;
import si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer;
import si.vicos.annotations.editor.AnnotationEditor.AnnotationEditorFactory;
import si.vicos.annotations.editor.SegmentationMaskAnnotationEditor;
import si.vicos.annotations.editor.ToggleAction.ToggleActionGroup;
import si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.EditList;
import si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.EditRegionOperation;
import si.vicos.annotations.editor.tracking.TagPlot.SingleTagPlot;
import si.vicos.annotations.tracking.AnnotatedSequence;
import si.vicos.annotations.tracking.AnnotatedSequenceListener;
import si.vicos.annotations.tracking.Interval;

/**
 * The Class TrackingEditor.
 */
public class TrackingEditor extends PersistentWindow {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * The Interface EditAssistant.
	 */
	public static interface EditAssistant {

		/**
		 * Edits the frame.
		 * 
		 * @param sequence
		 *            the sequence
		 * @param frame
		 *            the frame
		 * @param region
		 *            the region
		 * @return the edits the list
		 */
		EditList editFrame(EditableAnnotatedSequence sequence, int frame,
				Annotation region);

	}

	/** The default assistant. */
	private EditAssistant defaultAssistant = new EditAssistant() {

		@Override
		public EditList editFrame(EditableAnnotatedSequence sequence,
				int frame, Annotation region) {

			EditList edits = new EditList();
			Annotation before = annotations.get(frame);

			if (before == null || before.isNull() || region == null
					|| region.isNull()) {
				edits.add(new EditRegionOperation(frame, region));
			} else {
				edits.add(new EditRegionOperation(frame, region.clone()));
			}

			return edits;
		}
	};

	/**
	 * The Interface ImportFormat.
	 */
	public static interface ImportFormat {

		/**
		 * Import rectangle.
		 * 
		 * @param tokens
		 *            the tokens
		 * @return the rectangle annotation
		 */
		public RectangleAnnotation importRectangle(List<String> tokens);

	}

	/** The import delimiters. */
	private static Map<String, String> importDelimiters = new LinkedHashMap<String, String>();

	/** The import formats. */
	private static Map<String, ImportFormat> importFormats = new LinkedHashMap<String, ImportFormat>();

	/** The hidden tags. */
	private static Set<String> hiddenTags = new HashSet<String>();

	/** The annotation editors. */
	private static Map<String, AnnotationEditorFactory<?>> annotationEditors = new LinkedHashMap<String, AnnotationEditor.AnnotationEditorFactory<?>>();

	static {

		hiddenTags.add("keyframe");

		importDelimiters.put("Space", " ");
		importDelimiters.put("Comma", ",");
		importDelimiters.put("Semicolon", ";");

		importFormats.put("(leftX, topY, width, height)", new ImportFormat() {

			@Override
			public RectangleAnnotation importRectangle(List<String> tokens) {

				try {
					return new RectangleAnnotation(Float.parseFloat(tokens
							.get(0)), Float.parseFloat(tokens.get(1)), Float
							.parseFloat(tokens.get(2)), Float.parseFloat(tokens
							.get(3)));
				} catch (NumberFormatException e) {
				} catch (IndexOutOfBoundsException e) {
				}

				return null;
			}
		});

		importFormats.put("(leftX, topY, bottomX, bottomY)",
				new ImportFormat() {

					@Override
					public RectangleAnnotation importRectangle(
							List<String> tokens) {

						try {
							float x = Float.parseFloat(tokens.get(0));
							float y = Float.parseFloat(tokens.get(1));
							return new RectangleAnnotation(x, y, Float
									.parseFloat(tokens.get(2)) - x, Float
									.parseFloat(tokens.get(3)) - y);
						} catch (NumberFormatException e) {
						} catch (IndexOutOfBoundsException e) {
						}

						return null;
					}
				});

		annotationEditors.put(RectangleAnnotationEditor.FACTORY.getName(),
				RectangleAnnotationEditor.FACTORY);
		annotationEditors.put(
				RotatedRectangleAnnotationEditor.FACTORY.getName(),
				RotatedRectangleAnnotationEditor.FACTORY);
		annotationEditors.put(SegmentationMaskAnnotationEditor.FACTORY.getName(),
				SegmentationMaskAnnotationEditor.FACTORY);
		// annotationEditors.put(PolygonAnnotationEditor.FACTORY.getName(),
		// PolygonAnnotationEditor.FACTORY);
	}

	/** The left button. */
	private ButtonAction leftButton = new ButtonAction() {

		public Object onClick(FigurePanel source, Point position, int clicks,
				int modifiers) {

			if (clicks > 1)
				return null;

			if (!source.isFigurePointInFigure(position)) {
				blurAnnotation(modifiers);
				return null;
			}

			focusAnnotation(modifiers);

			annotationEditor.onClick(source, position, clicks, modifiers);

			return null;

		}

		public void onPress(FigurePanel source, Point position, int modifiers) {

			if (!source.isFigurePointInFigure(position)) {
				blurAnnotation(modifiers);
				return;
			}

			focusAnnotation(modifiers);

			annotationEditor
					.onMove(source, position, position, true, modifiers);

		}

		public void onRelease(FigurePanel source, Point position, int modifiers) {

			if (!source.isFigurePointInFigure(position)) {
				blurAnnotation(modifiers);
				return;
			}

			focusAnnotation(modifiers);

			annotationEditor.onClick(source, null, 1, modifiers);

		}

	};

	/** The none button. */
	private ButtonAction noneButton = new ButtonAction() {

		public Object onClick(FigurePanel source, Point position, int clicks,
				int modifiers) {

			if (clicks > 1)
				return null;

			if (!source.isFigurePointInFigure(position))
				return null;

			return annotationEditor.getToolTip(source, position);

		}

		public void onPress(FigurePanel source, Point position, int modifiers) {

		}

		public void onRelease(FigurePanel source, Point position, int modifiers) {

		}

	};

	/** The left move. */
	private MoveAction leftMove = new MoveAction() {

		public Cursor onMove(FigurePanel source, Point from, Point to,
				int modifiers) {

			if (!source.isFigurePointInFigure(to)) {
				blurAnnotation(modifiers);
				return null;
			}

			focusAnnotation(modifiers);

			return annotationEditor.onMove(source, from, to, true, modifiers);

		}
	};

	/** The none move. */
	private MoveAction noneMove = new MoveAction() {

		public Cursor onMove(FigurePanel source, Point from, Point to,
				int modifiers) {

			if (!source.isFigurePointInFigure(to)) {
				blurAnnotation(modifiers);
				return null;
			}

			panel.requestFocusInWindow();
			focusAnnotation(modifiers);

			return annotationEditor.onMove(source, from, to, false, modifiers);

		}
	};

	/**
	 * The Class FrameAnnotationPeer.
	 */
	private class FrameAnnotationPeer implements AnnotationPeer {

		/** The index. */
		private int index;

		/**
		 * Instantiates a new frame annotation peer.
		 * 
		 * @param index
		 *            the index
		 */
		public FrameAnnotationPeer(int index) {

			this.index = index;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer#
		 * getAnnotation()
		 */
		public Annotation getAnnotation() {
			return annotations.get(index);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer#repaint
		 * ()
		 */
		public void repaint() {
			if (panel != null)
				panel.repaint();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.AnnotatedImageFigure.AnnotationPeer#
		 * setAnnotation(si.vicos.annotations.Annotation)
		 */
		@Override
		public void setAnnotation(Annotation a) {

			updateFrame(index, a);

		}

	}

	/**
	 * The Class MetadataProxy.
	 */
	private static class MetadataProxy extends Settings implements
			AnnotatedSequenceListener, Weak {

		/** The annotations. */
		private EditableAnnotatedSequence annotations;

		/**
		 * Instantiates a new metadata proxy.
		 * 
		 * @param a
		 *            the a
		 */
		public MetadataProxy(EditableAnnotatedSequence a) {
			super(null);
			this.annotations = a;
			this.annotations.addAnnotatedSequenceListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.coffeeshop.settings.ReadableSettings#getKeys()
		 */
		@Override
		public Set<String> getKeys() {
			return annotations.getMetadata().keySet();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.coffeeshop.settings.AbstractReadonlySettings#getProperty(java
		 * .lang.String)
		 */
		@Override
		protected String getProperty(String key) {

			String value = annotations.getMetadata(key);

			return (value == null) ? "" : value;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.coffeeshop.settings.WriteableSettings#touch()
		 */
		@Override
		public void touch() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.coffeeshop.settings.WriteableSettings#isModified()
		 */
		@Override
		public boolean isModified() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.coffeeshop.settings.WriteableSettings#remove(java.lang.String)
		 */
		@Override
		public void remove(String key) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.coffeeshop.settings.Settings#setProperty(java.lang.String,
		 * java.lang.String)
		 */
		@Override
		protected String setProperty(String key, String value) {
			annotations.setMetadata(key, value);
			return value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * si.vicos.annotations.tracking.AnnotatedSequenceListener#intervalChanged
		 * (si.vicos.annotations.tracking.AnnotatedSequence,
		 * si.vicos.annotations.tracking.Interval)
		 */
		@Override
		public void intervalChanged(AnnotatedSequence sequence,
				Interval interval) {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * si.vicos.annotations.tracking.AnnotatedSequenceListener#metadataChanged
		 * (si.vicos.annotations.tracking.AnnotatedSequence, java.util.Set)
		 */
		@Override
		public void metadataChanged(AnnotatedSequence sequence, Set<String> keys) {

			for (String key : keys) {
				notifySettingsChanged(key, getProperty(key), getProperty(key));
			}

		}

	}

	/** The keyframe labels. */
	private SingleTagPlot keyframeLabels;

	/** The key listener. */
	private KeyListener keyListener = new KeyListener() {

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() != KeyEvent.VK_ESCAPE)
				return;

			if (figure != null)
				cancelInput();
		}

		public void keyReleased(KeyEvent e) {

		}

		public void keyTyped(KeyEvent e) {

		}
	};

	/** The select start. */
	private ToolTipAction selectStart = new ToolTipAction("Select start",
			"select-start") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			int position = getFrame();

			if (timeline.isSelectionValid()
					&& timeline.getSelectionEnd() > position) {

				timeline.setSelection(position, timeline.getSelectionEnd());

			} else {

				timeline.setSelection(position, position);

			}

		}

	};

	/** The select end. */
	private ToolTipAction selectEnd = new ToolTipAction("Select end",
			"select-end") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			int position = getFrame();

			if (timeline.isSelectionValid()
					&& timeline.getSelectionStart() < position) {

				timeline.setSelection(timeline.getSelectionStart(), position);

			} else {

				timeline.setSelection(position, position);

			}

		}

	};

	/** The zoom normal. */
	private ToolTipAction zoomNormal = new ToolTipAction("Zoom 100%",
			"zoom-normal") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			if (panel != null)
				panel.setZoom(1);

		}

	};

	/** The zoom in. */
	private ToolTipAction zoomIn = new ToolTipAction("Zoom in", "zoom-in") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			if (panel == null)
				return;

			double zoom = panel.getZoom();

			if (zoom < 1) {
				zoom = (1 / zoom) - 0.1;
				panel.setZoom(1 / zoom);
			} else {
				panel.setZoom(zoom + 0.1);
			}
		}

	};

	/** The zoom out. */
	private ToolTipAction zoomOut = new ToolTipAction("Zoom out", "zoom-out") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			if (panel == null)
				return;

			double zoom = panel.getZoom();

			if (zoom < 1) {
				zoom = (1 / zoom) + 0.1;
				panel.setZoom(1 / zoom);
			} else {
				panel.setZoom(zoom - 0.1);
			}
		}

	};

	/** The zoom fit. */
	private ToolTipAction zoomFit = new ToolTipAction("Zoom to fit", "zoom-fit") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			if (panel == null)
				return;

			double zoom = 1 / Math.max(panel.getFigure().getWidth(null)
					/ (double) panel.getViewportWidth(), panel.getFigure()
					.getHeight(null) / (double) panel.getViewportHeight());

			panel.setZoom(zoom);
		}

	};

	/** The undo. */
	private ToolTipAction undo = new ToolTipAction("Undo", "undo") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			if (!history.canUndo())
				return;

			history.undo();

			updateActions();

		}

	};

	/** The redo. */
	private ToolTipAction redo = new ToolTipAction("Redo", "redo") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			if (!history.canRedo())
				return;

			history.redo();

			updateActions();

		}

	};

	/** The cancel. */
	private ToolTipAction cancel = new ToolTipAction("Cancel input") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			cancelInput();

		}

	};

	/** The reset. */
	private ToolTipAction reset = new ToolTipAction("Reset input") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			resetAnnotation();

		}

	};

	/** The toggle keyframe. */
	private ToolTipAction toggleKeyframe = new ToolTipAction("Keyframe",
			"keyframe") {

		private static final long serialVersionUID = 1L;

		{
			putValue(ToolTipAction.SELECTED_KEY, false);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			toggleKeyFrame(getFrame());

		}

	};

	/** The mode interpolate. */
	private ToolTipAction modeInterpolate = new ToolTipAction("Interpolate",
			"interpolate") {

		private static final long serialVersionUID = 1L;
		{
			putValue(ToolTipAction.SELECTED_KEY, true);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			boolean selected = (Boolean) getValue(ToolTipAction.SELECTED_KEY);

			assistant = selected ? InterpolationAssistant.instance
					: defaultAssistant;

		}

	};

	/** The go previous frame. */
	private ToolTipAction goPreviousFrame = new ToolTipAction("Previous frame",
			"go-previous") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			int frame = getFrame() - 1;

			if (frame < 0 || frame >= annotations.size())
				return;

			timeline.setPosition(frame);
		}

	};

	/** The go next frame. */
	private ToolTipAction goNextFrame = new ToolTipAction("Next frame",
			"go-next") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			int frame = getFrame() + 1;

			if (frame < 0 || frame >= annotations.size())
				return;

			timeline.setPosition(frame);
		}

	};

	/** The go previous key frame. */
	private ToolTipAction goPreviousKeyFrame = new ToolTipAction(
			"Previous keyframe", "go-previous-keyframe") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			int frame = previousKeyframe(getFrame());

			if (frame < 0 || frame >= annotations.size())
				return;

			timeline.setPosition(frame);
		}

	};

	/** The go next key frame. */
	private ToolTipAction goNextKeyFrame = new ToolTipAction("Next keyframe",
			"go-next-keyframe") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			int frame = nextKeyframe(getFrame());

			if (frame < 0 || frame >= annotations.size())
				return;

			timeline.setPosition(frame);
		}

	};

	/** The go first frame. */
	private ToolTipAction goFirstFrame = new ToolTipAction("First frame",
			"go-first") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			if (annotations.size() == 0)
				return;

			timeline.setPosition(0);
		}

	};

	/** The go last frame. */
	private ToolTipAction goLastFrame = new ToolTipAction("Last frame",
			"go-last") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			if (annotations.size() == 0)
				return;

			timeline.setPosition(annotations.size() - 1);
		}

	};

	/** The save. */
	private ToolTipAction save = new ToolTipAction("Save", "save") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			if (annotations.getDirectory() != null) {
				try {

					File destination = annotations.getSource();

					annotations.write(destination);

					// TODO: handle with signals
					Annotator.addRecentDocument(destination);

					status.setMessage("Saved annotations to " + destination,
							MessageType.INFO);

				} catch (IOException e) {
					Application.getApplicationLogger().report(e);

					status.setMessage("Saving failed: " + e.getMessage(),
							MessageType.ERROR);
				}
			}

			updateTitle();
		}

	};

	/** The quit. */
	private ToolTipAction quit = new ToolTipAction("Exit", "exit") {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {

			close();

		}

	};

	/** The settings. */
	private ToolTipAction settings = new ToolTipAction("Settings", "settings") {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {

			ActionMap actions = ((JPanel) TrackingEditor.this.getContentPane())
					.getActionMap();
			InputMap inputs = ((JPanel) TrackingEditor.this.getContentPane())
					.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

			KeyBindingsEditor editor = new KeyBindingsEditor(
					Application.getApplicationSettings(), actions, inputs,
					"tracking.keybindings.");

			SimpleDialog dialog = new SimpleDialog(TrackingEditor.this,
					"Keyboard", editor);

			dialog.showDialog();

			inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel"); // Add
																					// default
																					// cancel
																					// key

		}

	};

	/** The open. */
	private ToolTipAction open = new ToolTipAction("Open", "load") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {

			String path = ".";
			try {
				path = Application.getApplicationSettings().getString(
						"browse.path");
			} catch (SettingsNotFoundException ex) {
			}

			JFileChooser chooser = new JFileChooser(path);

			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			chooser.setFileFilter(Annotator.FILE_FILTER);

			chooser.showOpenDialog(null);

			if (chooser.getSelectedFile() != null) {

				Application.getApplicationSettings().setString("browse.path",
						chooser.getSelectedFile().toString());

				try {
					Annotator.openFile(chooser.getSelectedFile());
				} catch (IOException e) {
					Application.getApplicationLogger().report(e);
				}
			}

		}

	};

	/** The export as text. */
	private ToolTipAction exportAsText = new ToolTipAction(
			"Export as text file", "save-16.png") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {
			String path = ".";
			try {
				path = Application.getApplicationSettings().getString(
						"browse.path");
			} catch (SettingsNotFoundException ex) {
			}

			JFileChooser chooser = new JFileChooser(path);

			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			chooser.setAcceptAllFileFilterUsed(false);
			chooser.addChoosableFileFilter(new TextExporter.BoundingBoxExporter());
			chooser.addChoosableFileFilter(new TextExporter.EightPointsExporter());

			chooser.showOpenDialog(null);

			TextExporter exporter = (TextExporter) chooser.getFileFilter();

			if (chooser.getSelectedFile() != null) {

				try {

					OutputStream out = new FileOutputStream(
							chooser.getSelectedFile());

					exporter.export(out, annotations);

					out.close();

				} catch (IOException e) {
					e.printStackTrace();
					status.setMessage("Export failed", MessageType.ERROR);
					Application.getApplicationLogger().report(e);
				}

				status.setProgress(-1);

				Application.getApplicationSettings().setString("browse.path",
						chooser.getSelectedFile().toString());
			}

		}

	};

	/** The export as package. */
	private ToolTipAction exportAsPackage = new ToolTipAction(
			"Export as package", "save") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0) {
			String path = ".";
			try {
				path = Application.getApplicationSettings().getString(
						"browse.path");
			} catch (SettingsNotFoundException ex) {
			}

			JFileChooser chooser = new JFileChooser(path);

			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			chooser.setFileFilter(new FileFilter() {

				@Override
				public String getDescription() {
					return "ZIP files";
				}

				@Override
				public boolean accept(File arg0) {

					if (arg0.isDirectory())
						return true;

					String name = arg0.getName();

					return name.endsWith(".zip");

				}
			});

			chooser.showOpenDialog(null);

			if (chooser.getSelectedFile() != null) {

				try {
					annotations.exportAsPackage(chooser.getSelectedFile(),
							new Callback() {

								@Override
								public void callback(Object source,
										Object parameter) {
									if (parameter instanceof Float)
										status.setProgress((int) ((Float) parameter * 100));
								}

							});
				} catch (IOException e) {
					e.printStackTrace();
					status.setMessage("Export failed", MessageType.ERROR);
					Application.getApplicationLogger().report(e);
				}

				status.setProgress(-1);

				Application.getApplicationSettings().setString("browse.path",
						chooser.getSelectedFile().toString());
			}

		}

	};

	/** The import annotations. */
	private ToolTipAction importAnnotations = new ToolTipAction(
			"Import from file", "load-16.png") {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent arg0) {

			Settings settings = Application.getApplicationSettings();
			OrganizedSettings structure = new OrganizedSettings(
					"Import from file");

			SettingsGroup group = structure.createSubgroup("Source");
			SettingsValue sourceValue = group.attachValue("import.source",
					"Source file", new FileStringParser(true, false, true));

			group = structure.createSubgroup("Format");
			SettingsValue delimiterValue = group.attachValue(
					"import.delimiters", "Delimiter characters",
					new EnumeratedSubsetStringParser(importDelimiters.keySet(),
							true));
			SettingsValue formatValue = group.attachValue("import.format",
					"Region format",
					new EnumeratedStringParser(importFormats.keySet(), true));

			structure.attachValue("import.overwrite", "Overwrite",
					BooleanStringParser.getParser());
			structure.attachValue("import.offset", "Offset",
					IntegerStringParser.getParser());

			SimpleDialog dialog = new SimpleDialog(TrackingEditor.this,
					Application.getApplicationSettings(), structure);

			if (dialog.showDialog()) {

				int imported = 0;

				try {
					Set<String> delimiters = (Set<String>) delimiterValue
							.parse(settings);
					ImportFormat format = importFormats
							.get((String) formatValue.parse(settings));
					File source = (File) sourceValue.parse(settings);

					BufferedReader reader = new BufferedReader(new FileReader(
							source));

					String delims = "";
					for (String d : delimiters)
						delims += importDelimiters.get(d);

					int frame = settings.getInt("import.offset", 0);
					boolean overwrite = settings.getBoolean("import.overwrite",
							false);

					while (true) {

						String line = reader.readLine();

						if (line == null)
							break;

						StringTokenizer tokenizer = new StringTokenizer(line,
								delims);
						Vector<String> tokens = new Vector<String>();

						while (tokenizer.hasMoreElements()) {
							String token = tokenizer.nextToken().trim();
							if (!StringUtils.empty(token))
								tokens.add(token);
						}

						RectangleAnnotation annotation = format
								.importRectangle(tokens);

						if (annotation != null) {

							if (frame >= 0 && frame < annotations.size()) {

								Annotation old = annotations.get(frame);

								if (overwrite || old == null || old.isNull()) {
									annotations.setRegion(frame, annotation);

									imported++;
								}

							}

						}

						frame++;

					}

					reader.close();

				} catch (SettingsNotFoundException e) {
					Application.getApplicationLogger().report(e);
				} catch (ParseException e) {
					Application.getApplicationLogger().report(e);
				} catch (FileNotFoundException e) {
					status.setMessage("File not found.", MessageType.ERROR);

					Application.getApplicationLogger().report(e);
				} catch (IOException e) {
					status.setMessage("File access error.", MessageType.ERROR);
					Application.getApplicationLogger().report(e);
				}

				status.setMessage("Imported annotations for " + imported
						+ " frames.", MessageType.INFO);

			}

		}

	};

	/*
	 * private ToolTipAction interpolate = new ToolTipAction("Interpolate",
	 * "interpolate-16.png") {
	 * 
	 * private static final long serialVersionUID = 1L;
	 * 
	 * public void actionPerformed(ActionEvent a) {
	 * 
	 * if ()
	 * 
	 * JToggleButton b = (JToggleButton) a.getSource(); b.isSelected() }
	 * 
	 * };
	 */

	/** The history. */
	private UndoManager history = new UndoManager() {

		private static final long serialVersionUID = 1L;

		@Override
		public void undoableEditHappened(UndoableEditEvent e) {

			super.undoableEditHappened(e);

			updateActions();

		}

	};

	/** The tags editor model. */
	private DefaultListModel<String> tagsEditorModel = new DefaultListModel<String>();

	/** The assistant. */
	private EditAssistant assistant = InterpolationAssistant.instance;

	/** The panel. */
	private FigurePanel panel = new FigurePanel();

	/** The figure. */
	private AnnotatedImageFigure figure = null;

	/** The annotations. */
	private EditableAnnotatedSequence annotations = null;

	/** The annotation editor. */
	private AnnotationEditor annotationEditor;

	/** The annotation editor selector. */
	private ToggleActionGroup annotationEditorSelector = new ToggleActionGroup();

	/** The action manager. */
	private ActionSetManager actionManager = new ActionSetManager();

	/** The tags editor. */
	private FrameTagsEditor tagsEditor;

	/** The main menu. */
	private JToolBar mainMenu = new JToolBar(JToolBar.HORIZONTAL);

	/** The navigation menu. */
	private JToolBar navigationMenu = new JToolBar(JToolBar.HORIZONTAL);

	/** The timeline. */
	private Timeline timeline;

	/** The status. */
	private StatusBar status = new StatusBar();

	/** The metadata. */
	private Settings metadata;

	/** The split outer. */
	JSplitPane splitOuter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

	/** The split inner. */
	JSplitPane splitInner = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	/**
	 * Instantiates a new tracking editor.
	 * 
	 * @param writer
	 *            the writer
	 */
	public TrackingEditor(EditableAnnotatedSequence writer) {

		super("editor.annotations", "Annotations editor");

		setIconImage(ImageStore.getImage("icon.png", "icon"));

		this.annotations = writer;

		metadata = new MetadataProxy(annotations);

		// input =
		// Application.getApplication().getSettingsManager().getSettings("tracking.ini");

		timeline = new Timeline(annotations.size());
		timeline.setBorder(BorderFactory.createLoweredBevelBorder());

		initGUI();

		timeline.setPosition(0);

		keyframeLabels = new SingleTagPlot(annotations, "keyframe", Color.GRAY);

		timeline.getTrack("Key frames").addObject(keyframeLabels);

		updateTimeline();

		/*
		 * (new Thread() {
		 * 
		 * @Override public void run() {
		 * 
		 * annotations.generateChecksum(new Callback() {
		 * 
		 * @Override public void callback(Object source, Object parameter) { if
		 * (parameter instanceof Float) status.setProgress((int)( (Float)
		 * parameter * 100)); }
		 * 
		 * });
		 * 
		 * status.setProgress(-1);
		 * 
		 * }
		 * 
		 * }).start();
		 */

		annotations
				.addAnnotatedSequenceListener(new AnnotatedSequenceListener() {

					@Override
					public void metadataChanged(AnnotatedSequence sequence,
							Set<String> keys) {

						updateTitle();

					}

					@Override
					public void intervalChanged(AnnotatedSequence sequence,
							Interval interval) {

						tagsEditor.setInterval(tagsEditor.getInterval());

						updateTimeline();

						timeline.repaint();

						if (interval.contains(getFrame())) {

							setFrame(getFrame()); // Refresh frame

						}

						updateTitle();
					}

				});

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {

				close();

			}

		});

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				splitOuter.setDividerLocation((int) Application
						.getApplicationSettings().getDouble(
								getIdentifier() + ".split.outer",
								getHeight() - 200));
				splitInner.setDividerLocation((int) Application
						.getApplicationSettings().getDouble(
								getIdentifier() + ".split.inner",
								getWidth() - 300));

			}
		});

		if (annotations instanceof UndoableAnnotatedSequence) {

			((UndoableAnnotatedSequence) annotations).addEditListener(history);

			history.setLimit(100);

		}

		updateActions();

		updateTitle();
	}

	/**
	 * Inits the tools sidebar.
	 * 
	 * @return the j component
	 */
	private JComponent initToolsSidebar() {

		JTabbedPane tabs = new JTabbedPane();

		OrganizedSettings metadata = new OrganizedSettings(null);

		metadata.attachValue("title", "Title", StringStringParser.getParser());

		metadata.attachValue("author.annotation", "Annotation author",
				StringStringParser.getParser());

		metadata.attachValue("author.sequence", "Sequence author",
				StringStringParser.getParser());

		metadata.attachValue("location", "Location",
				StringStringParser.getParser());

		metadata.attachValue("timestamp", "Date and time",
				StringStringParser.getParser());

		metadata.attachValue("tags", "Tags", StringStringParser.getParser());

		metadata.attachValue("notes", "Notes", new StringStringParser(true));

		metadata.attachValue("reference", "Reference", new StringStringParser(
				true));

		tabs.addTab(
				"Metadata",
				ImageStore.getIcon("tools-metadata"),
				new SettingsPanel(this.metadata, metadata, CommitStrategy.FOCUS));
		/*
		 * JPanel tools = new JPanel(new StackLayout(Orientation.VERTICAL, 10,
		 * 10, true));
		 * 
		 * tools.add(new JButton(crop));
		 * 
		 * if (!this.input.containsKey("input.target"))
		 * this.input.setString("input.target",
		 * EditTarget.RECTANGLE.toString());
		 * 
		 * if (!this.input.containsKey("input.mode"))
		 * this.input.setString("input.mode", EditMode.DEFAULT.toString());
		 * 
		 * OrganizedSettings input = new OrganizedSettings("Input");
		 * 
		 * input.attachValue("input.target", "Edit", new
		 * EnumeratedStringParser(EditTarget.class, true),
		 * EditTarget.RECTANGLE.toString());
		 * 
		 * input.attachValue("input.mode", "Mode", new
		 * EnumeratedStringParser(EditMode.class, true),
		 * EditMode.DEFAULT.toString());
		 * 
		 * input.attachValue("input.recenter", "Auto recenter", new
		 * BooleanStringParser());
		 * 
		 * this.input.addSettingsListener(new SettingsListener() {
		 * 
		 * @Override public void settingsChanged(SettingsChangedEvent e) {
		 * 
		 * mode =
		 * EditMode.valueOf(TrackingEditor.this.input.getString("input.mode"));
		 * 
		 * } });
		 * 
		 * mode =
		 * EditMode.valueOf(TrackingEditor.this.input.getString("input.mode"));
		 * 
		 * tools.add(new SettingsPanel(this.input, input));
		 */

		tagsEditor = new FrameTagsEditor(annotations,
				Collections.synchronizedSet(hiddenTags));

		tabs.addTab("Tags", ImageStore.getIcon("tools-tags"), tagsEditor);

		/*
		 * JPanel conversions = new JPanel(new StackLayout(Orientation.VERTICAL,
		 * 10, 10, true));
		 * 
		 * conversions.add(new JButton(importAnnotations));
		 * 
		 * conversions.add(new JButton(exportAsPackage));
		 * 
		 * conversions.add(new JButton(exportAsText));
		 * 
		 * tabs.addTab("Conversions", conversions);
		 */

		return tabs;

	}

	/**
	 * Inits the gui.
	 */
	private void initGUI() {

		/*
		 * setContentPane(new JPanel() {
		 * 
		 * protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int
		 * condition, boolean pressed) {
		 * 
		 * InputMap map = getInputMap(condition); ActionMap am = getActionMap();
		 * 
		 * if(map != null && am != null && isEnabled()) { Object binding =
		 * map.get(ks); Action action = (binding == null) ? null :
		 * am.get(binding); if (action != null) { return
		 * SwingUtilities.notifyAction(action, ks, e, this, e.getModifiers()); }
		 * } return false; }
		 * 
		 * });
		 */

		ImageStore.registerAnchorClass(FigureViewer.class);

		setLayout(new BorderLayout());

		setIconImage(ImageStore.getImage("image-16.png"));

		mainMenu.setFloatable(false);
		navigationMenu.setFloatable(false);

		add(mainMenu, BorderLayout.NORTH);

		JPanel panelContainer = new JPanel(new BorderLayout(10, 10));
		panelContainer.add(panel, BorderLayout.CENTER);
			panelContainer.add(navigationMenu, BorderLayout.SOUTH);

		splitOuter.setTopComponent(splitInner);
		splitOuter.setBottomComponent(timeline);

		splitInner.setRightComponent(initToolsSidebar());
		splitInner.setLeftComponent(panelContainer);

		add(splitOuter, BorderLayout.CENTER);

		panel.setMinimumSize(new Dimension(100, 100));

		add(status, BorderLayout.SOUTH);

		installMenu();
		installActions();

		ActionMap actions = ((JPanel) getContentPane()).getActionMap();
		actions.put("cancel", cancel);
		actions.put("previous", goPreviousFrame);
		actions.put("next", goNextFrame);
		actions.put("previous_keyframe", goPreviousKeyFrame);
		actions.put("next_keyframe", goNextKeyFrame);
		actions.put("first", goFirstFrame);
		actions.put("last", goLastFrame);
		actions.put("reset", reset);
		actions.put("save", save);
		actions.put("quit", quit);
		actions.put("select_start", selectStart);
		actions.put("select_end", selectEnd);
		actions.put("undo", undo);
		actions.put("redo", redo);

		actionManager.add("undo", undo);
		actionManager.add("redo", redo);
		actionManager.add("edit", undo, redo);

		InputMap inputs = ((JPanel) getContentPane())
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyBindingsEditor.install(inputs,
				new PrefixProxySettings(Application.getApplicationSettings(),
						"tracking.keybindings."));

		((JPanel) getContentPane()).setInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputs);

		timeline.addTimelineListener(new TimelineListener() {

			@Override
			public void positionChanged(Timeline timeline, int frame) {
				setFrame(frame);

				status.setLabel("frame", "@" + frame);
			}

			@Override
			public void selectionChanged(Timeline timeline, int begin, int end) {

				status.setLabel("selection", timeline.isSelectionValid() ? "["
						+ begin + ":" + end + "]" : "");

				if (timeline.isSelectionValid()) {
					tagsEditor.setInterval(new Interval(begin, end));
				} else {
					tagsEditor.setInterval(new Interval(timeline.getPosition(),
							timeline.getPosition()));
				}

			}
		});

		disableKeyboard(splitInner);
		disableKeyboard(splitOuter);
		disableKeyboard(mainMenu);

	}

	/**
	 * Disable keyboard.
	 * 
	 * @param c
	 *            the c
	 */
	private void disableKeyboard(JComponent c) {
		SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_FOCUSED, null);
		SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_IN_FOCUSED_WINDOW,
				null);
		SwingUtilities.replaceUIInputMap(c,
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
	}

	/**
	 * Install menu.
	 */
	private void installMenu() {

		mainMenu.removeAll();

		mainMenu.add(open);
		mainMenu.add(save);

		mainMenu.addSeparator(new Dimension(40, 10));

		mainMenu.add(redo);
		mainMenu.add(undo);

		mainMenu.add(Box.createHorizontalGlue());

		mainMenu.add(settings);

		navigationMenu.add(Box.createHorizontalGlue());

		for (final Entry<String, AnnotationEditorFactory<?>> e : annotationEditors
				.entrySet()) {

			navigationMenu.add(new ToolbarToggleButton(new ToggleAction(e
					.getValue().getName(), "annotation-editor-"
					+ e.getKey().toLowerCase().replace(' ', '-'),
					annotationEditorSelector) {

				private String editor = e.getValue().getName();

				private static final long serialVersionUID = 1L;

				@Override
				public void actionSelected(ActionEvent e) {
					Application.getApplicationSettings().setString(
							"annotator.editor", editor);

					setFrame(getFrame());
				}

				@Override
				public void actionDeselected(ActionEvent e) {

				}

			}));

		}

		annotationEditorSelector.select(Application.getApplicationSettings()
				.getString("annotator.editor",
						RectangleAnnotationEditor.FACTORY.getName()));

		navigationMenu.addSeparator(new Dimension(40, 10));

		navigationMenu.add(new ToolbarToggleButton(toggleKeyframe));
		navigationMenu.add(new ToolbarToggleButton(modeInterpolate));

		navigationMenu.addSeparator(new Dimension(40, 10));
		navigationMenu.add(goFirstFrame);
		navigationMenu.add(goPreviousKeyFrame);
		navigationMenu.add(goPreviousFrame);
		navigationMenu.add(goNextFrame);
		navigationMenu.add(goNextKeyFrame);
		navigationMenu.add(goLastFrame);

		navigationMenu.addSeparator(new Dimension(20, 10));

		navigationMenu.add(selectStart);
		navigationMenu.add(selectEnd);

		navigationMenu.addSeparator(new Dimension(20, 10));

		navigationMenu.add(zoomIn);
		navigationMenu.add(zoomOut);
		navigationMenu.add(zoomFit);
		navigationMenu.add(zoomNormal);
		navigationMenu.add(Box.createHorizontalGlue());
	}

	/**
	 * Install actions.
	 */
	private void installActions() {

		panel.setButtonAction(Button.LEFT, leftButton);
		panel.setButtonAction(Button.NONE, noneButton);
		panel.setMoveAction(Button.NONE, noneMove);
		panel.setMoveAction(Button.LEFT, leftMove);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.PersistentWindow#defaultState()
	 */
	@Override
	protected void defaultState() {

		setSize(500, 500);
		center();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.coffeeshop.swing.PersistentWindow#saveState(org.coffeeshop.settings
	 * .WriteableSettings, boolean)
	 */
	@Override
	protected void saveState(WriteableSettings settings, boolean closing) {

		settings.setFloat("split.inner", splitInner.getDividerLocation());
		settings.setFloat("split.outer", splitOuter.getDividerLocation());
	}

	/**
	 * Sets the frame.
	 * 
	 * @param frame
	 *            the new frame
	 */
	private void setFrame(int frame) {

		String editor = Application.getApplicationSettings()
				.getString("annotator.editor",
						RectangleAnnotationEditor.FACTORY.getName());

		annotationEditor = annotationEditors.get(editor).getEditor(
				new FrameAnnotationPeer(getFrame()), Color.RED);

		Image image = annotations.getImage(frame);

		figure = new AnnotatedImageFigure(image, annotationEditor);

		panel.setFigure(figure);

		tagsEditorModel.removeAllElements();
		for (String tag : annotations.getTags(frame)) {
			if (hiddenTags.contains(tag))
				continue;
			tagsEditorModel.addElement(tag);
		}

		if (!timeline.isSelectionValid())
			tagsEditor.setInterval(new Interval(frame, frame));

		toggleKeyframe.putValue(ToolTipAction.SELECTED_KEY,
				annotations.hasTag(frame, "keyframe"));

		if (figure == null)
			return;

	}

	/**
	 * Update title.
	 */
	private void updateTitle() {

		String modified = annotations.isModified() ? "*" : "";

		setTitle("Aibu [" + annotations.getName() + ":"
				+ annotations.getSource().getAbsolutePath() + modified + "]");

	}

	/**
	 * Update timeline.
	 */
	private void updateTimeline() {

		for (String label : annotations.getTags()) {
			if (hiddenTags.contains(label))
				continue;

			String name = "Tag: " + label;

			if (timeline.isTrack(name))
				continue;

			SingleTagPlot labelPlot = new SingleTagPlot(annotations, label,
					Color.RED);

			timeline.getTrack(name).addObject(labelPlot);
		}

	}

	/**
	 * Update actions.
	 */
	private void updateActions() {

		if (history.canUndo())
			actionManager.enableSet("undo");
		else
			actionManager.disableSet("undo");

		if (history.canRedo())
			actionManager.enableSet("redo");
		else
			actionManager.disableSet("redo");

	}

	/**
	 * Gets the frame.
	 * 
	 * @return the frame
	 */
	private int getFrame() {
		return timeline.getPosition();
	}

	/**
	 * Cancel input.
	 */
	private void cancelInput() {

		if (annotationEditor != null)
			annotationEditor.resetInput();

	}

	/**
	 * Reset annotation.
	 */
	private void resetAnnotation() {

		if (annotationEditor != null)
			annotationEditor.reset();

	}

	/**
	 * Focus annotation.
	 * 
	 * @param modifiers
	 *            the modifiers
	 */
	private void focusAnnotation(int modifiers) {

		if (annotationEditor != null)
			annotationEditor.setSelected(true);

	}

	/**
	 * Blur annotation.
	 * 
	 * @param modifiers
	 *            the modifiers
	 */
	private void blurAnnotation(int modifiers) {

		if (annotationEditor != null)
			annotationEditor.setSelected(false);

	}

	/**
	 * Toggle key frame.
	 * 
	 * @param index
	 *            the index
	 */
	private void toggleKeyFrame(int index) {

		if (annotations.hasTag(index, "keyframe"))
			annotations.removeTag(index, "keyframe");
		else
			annotations.addTag(index, "keyframe");

		keyframeLabels.update(annotations);
		timeline.repaint();

	}

	/**
	 * Update frame.
	 * 
	 * @param index
	 *            the index
	 * @param a
	 *            the a
	 */
	private void updateFrame(int index, Annotation a) {

		EditList list = assistant.editFrame(annotations, index, a);

		if (list != null && !list.isEmpty()) {

			annotations.edit(list);

		}

	}

	/**
	 * Next keyframe.
	 * 
	 * @param index
	 *            the index
	 * @return the int
	 */
	private int nextKeyframe(int index) {

		int last = 0;

		for (last = index + 1; last < annotations.size(); last++) {

			if (annotations.hasTag(last, "keyframe")
					&& !annotations.get(last).isNull())
				return last;

		}

		return -1;
	}

	/**
	 * Previous keyframe.
	 * 
	 * @param index
	 *            the index
	 * @return the int
	 */
	private int previousKeyframe(int index) {

		int first = 0;

		for (first = index - 1; first >= 0; first--) {

			if (annotations.hasTag(first, "keyframe")
					&& !annotations.get(first).isNull())
				return first;

		}

		return -1;

	}

	/**
	 * Close.
	 */
	private void close() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		if (annotations.isModified()) {
			int result = JOptionPane
					.showConfirmDialog(
							TrackingEditor.this,
							"Do you want to save the changes before closing the editor?",
							"Changes not saved",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							ImageStore.getIcon("icon"));

			if (result == JOptionPane.YES_OPTION) {
				File destination = annotations.getSource();
				try {
					annotations.write(destination);
					setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					setVisible(false);

				} catch (IOException e) {
					Application.getApplicationLogger().report(e);

				}
				return;
			} else if (result == JOptionPane.NO_OPTION) {
				setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				setVisible(false);
			}
			return;
		}

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(false);
	}

}
