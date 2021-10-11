package si.vicos.annotations.editor;

import java.awt.Dimension;
import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.coffeeshop.application.Application;
import org.coffeeshop.arguments.ArgumentsException;
import org.coffeeshop.io.TempDirectory;
import org.coffeeshop.log.Logger;
import org.coffeeshop.settings.SettingsNotFoundException;
import org.coffeeshop.swing.ImageStore;
import org.coffeeshop.swing.PersistentWindow;
import org.coffeeshop.swing.RecentDocuments;
import org.coffeeshop.swing.ToolTipAction;
import org.coffeeshop.swing.viewers.FigureViewer;

import si.vicos.annotations.editor.tracking.EditableAnnotatedSequence;
import si.vicos.annotations.editor.tracking.TrackingEditor;
import si.vicos.annotations.editor.tracking.UndoableAnnotatedSequence;

/**
 * The Class Annotator.
 */
public class Annotator extends Application {

	/** The Constant Authors. */
	public static final String[][] Authors = { { "Luka \u010Cehovin",
			"http://www.vicos.si/lukacu" } };

	/** The Constant HOMEPAGE. */
	public static final String HOMEPAGE = "http://www.vicos.si/lukacu/aibu/";

	/** The Constant VERSION. */
	public static final String VERSION = "0.3 (alpha)";

	/** The Constant FILE_FILTER. */
	public static final FileFilter FILE_FILTER = new FileFilter() {

		@Override
		public String getDescription() {
			return "Annotations";
		}

		@Override
		public boolean accept(File arg0) {

			if (arg0.isDirectory())
				return true;

			String name = arg0.getName();

			return name.endsWith(".avt")
					|| name.compareTo("groundtruth.txt") == 0;

		}
	};

	/** The cache. */
	private static ImageCache cache;

	/** The history. */
	private static RecentDocuments history = null;

	/**
	 * Instantiates a new annotator application object.
	 * 
	 * @param args
	 *            the arguments array
	 * 
	 * @throws ArgumentsException
	 *             if the arguments are not recognized
	 */
	public Annotator(String[] args) throws ArgumentsException {

		super("Aibu", args);

	}

	/**
	 * The main method - starting point of the application.
	 * 
	 * @param args
	 *            the arguments array
	 * 
	 * @throws Exception
	 *             miscellaneous booting exception
	 */
	public static void main(String[] args) throws Exception {

		System.setProperty("coffeeshop.application.logging", "10");

		new Annotator(args);

		Application.getApplicationLogger().addOutputStream(System.out);
		Application.getApplicationLogger().enableAllChannels();

		ImageStore.registerAnchorClass(Annotator.class);
		ImageStore.registerAnchorClass(FigureViewer.class);

		try {
			ImageStore.registerImageProvider(new FontImageProvider("fa:",
					Annotator.class, "fontawesome.ttf", new Dimension(16, 16)));
			ImageStore.registerImageProvider(new FontImageProvider(
					"annotation:", Annotator.class, "annotation.ttf",
					new Dimension(16, 16)));
		} catch (FontFormatException e1) {
			Application.getApplicationLogger().report(e1);
		}

		ImageStore.registerImageProvider(new ImageStore.AliasImageProvider(
				Annotator.class.getResourceAsStream("fontawesome.ini")));

		ImageStore.registerImageProvider(new ImageStore.AliasImageProvider(
				Annotator.class.getResourceAsStream("annotation.ini")));

		ImageStore.registerImageProvider(new ImageStore.AliasImageProvider(
				Annotator.class.getResourceAsStream("general.ini")));

		ImageStore.registerImageProvider(new ImageStore.AliasImageProvider(
				Annotator.class.getResourceAsStream("aibu.ini")));

		System.setProperty("splash.background", "#FFFFFF");

		Thread.setDefaultUncaughtExceptionHandler(new ApplicationExceptionHandler());
		System.setProperty("sun.awt.exception.handler",
				ApplicationExceptionHandler.class.getName());

		history = new RecentDocuments(getApplicationSettings(), "history.");

		cache = new ImageCache(1024 * 1024 * 10, 1024 * 1024 * 30,
				new TempDirectory(Application.getApplication().getUnixName()));

		AnnotatorSplash splash = new AnnotatorSplash(
				getApplication().getName(), ImageStore.getImage("splash.png"),
				history);

		PersistentWindow.setExitOnAllClosed(true);

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		Object choice = splash.block();

		if (choice == null)
			System.exit(0);

		if (choice instanceof File) {
			File file = (File) choice;

			openFile(file);

		} else if (choice instanceof EditableAnnotatedSequence) {

			TrackingEditor te = new TrackingEditor(
					(EditableAnnotatedSequence) choice);
			te.setIconImage(ImageStore.getImage("icon"));
			te.setVisible(true);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.coffeeshop.application.Application#defineDefaults(org.coffeeshop.
	 * application.Application.SettingsSetter)
	 */
	@Override
	protected void defineDefaults(SettingsSetter setter) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.application.Application#getLongDescription()
	 */
	@Override
	public String getLongDescription() {
		return "Aibu - a generic editor for image database annotation";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.application.Application#getShortDescription()
	 */
	@Override
	public String getShortDescription() {
		return "Aibu Annotation Editor";
	}

	/**
	 * Adds the recent document.
	 * 
	 * @param file
	 *            the file
	 */
	public static void addRecentDocument(File file) {

		history.addDocument(file);

	}

	/**
	 * Gets the image cache.
	 * 
	 * @return the image cache
	 */
	public static ImageCache getImageCache() {
		return cache;
	}

	/**
	 * Open file.
	 * 
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void openFile(File file) throws IOException {
		history.addDocument(file);

		Application.getApplicationLogger().report(
				Logger.APPLICATION_INTERNAL_1, "Loading %s", file);

		Annotator.getApplicationSettings().setString("browse.path",
				file.toString());

		EditableAnnotatedSequence writer = new UndoableAnnotatedSequence(file);

		TrackingEditor te = new TrackingEditor(writer);
		te.setVisible(true);

	}

	/** The Constant newSequenceAction. */
	public static final Action newSequenceAction = new ToolTipAction(
			"New sequence", "new") {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {

			String path = ".";
			try {
				path = Application.getApplicationSettings().getString(
						"browse.path");
			} catch (SettingsNotFoundException ex) {
			}

			JFileChooser chooser = new JFileChooser(path);

			chooser.setMultiSelectionEnabled(true);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			chooser.setFileFilter(new FileFilter() {

				@Override
				public String getDescription() {
					return "Image files";
				}

				@Override
				public boolean accept(File arg0) {

					if (arg0.isDirectory())
						return true;

					String name = arg0.getName();

					return name.endsWith(".jpg") || name.endsWith(".png");

				}
			});

			chooser.showOpenDialog(null);

			if (chooser.getSelectedFile() != null) {

				Application.getApplicationSettings().setString("browse.path",
						chooser.getSelectedFile().toString());

				EditableAnnotatedSequence writer;
				try {
					File[] files = chooser.getSelectedFiles();
					Arrays.sort(files);

					writer = new EditableAnnotatedSequence(files);

					// closeWithResult(writer);
				} catch (IOException e) {
					Application.getApplicationLogger().report(e);
				}

			}

		}
	};

}
