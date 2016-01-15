package si.vicos.annotations.editor;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.coffeeshop.application.Application;
import org.coffeeshop.cache.ObjectCache;
import org.coffeeshop.settings.SettingsNotFoundException;
import org.coffeeshop.swing.RecentDocuments;
import org.coffeeshop.swing.Splash;
import org.coffeeshop.swing.ToolTipAction;

import si.vicos.annotations.editor.tracking.EditableAnnotatedSequence;
import si.vicos.annotations.editor.tracking.UndoableAnnotatedSequence;

/**
 * The Class AnnotatorSplash.
 */
public class AnnotatorSplash extends Splash {

	/** The list. */
	private ThumbnailGridList<File> list;

	/**
	 * Instantiates a new annotator splash.
	 * 
	 * @param title
	 *            the title
	 * @param image
	 *            the image
	 * @param history
	 *            the history
	 */
	public AnnotatorSplash(String title, Image image, RecentDocuments history) {
		super(title, image);

		list = new ThumbnailGridList<File>(history, new Dimension(128, 128),
				new AnnotationsDocumentRenderer(),
				new ObjectCache<Object, BufferedImage>(10));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.Splash#createSidebarComponent()
	 */
	@Override
	protected JComponent createSidebarComponent() {

		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				if (e.getValueIsAdjusting())
					return;

				closeWithResult(list.getSelectedValue());

			}
		});

		JScrollPane listpane = new JScrollPane(list,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		listpane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(20, 20, 20, 20),
				listpane.getBorder()));

		listpane.setPreferredSize(new Dimension(128 * 3 + 70, 200));

		return listpane;

	}

	/**
	 * Browse.
	 * 
	 * @return the file
	 */
	public File browse() {
		String path = ".";
		try {
			path = Application.getApplicationSettings()
					.getString("browse.path");
		} catch (SettingsNotFoundException ex) {
		}

		JFileChooser chooser = new JFileChooser(path);

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		chooser.setFileFilter(Annotator.FILE_FILTER);

		chooser.showOpenDialog(null);

		if (chooser.getSelectedFile() != null) {

			Application.getApplicationSettings().setString("browse.path",
					chooser.getSelectedFile().toString());

			return chooser.getSelectedFile();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coffeeshop.swing.Splash#createActions()
	 */
	@Override
	protected Collection<Action> createActions() {

		Collection<Action> actions = new Vector<Action>();

		actions.add(new ToolTipAction("New sequence", "new") {

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

					Application.getApplicationSettings()
							.setString("browse.path",
									chooser.getSelectedFile().toString());

					EditableAnnotatedSequence writer;
					try {
						File[] files = chooser.getSelectedFiles();
						Arrays.sort(files);

						writer = new UndoableAnnotatedSequence(files);

						closeWithResult(writer);
					} catch (IOException e) {
						Application.getApplicationLogger().report(e);
					}

				}

			}
		});

		actions.add(new ToolTipAction("Load", "load") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {

				File selection = browse();

				if (selection != null)
					closeWithResult(selection);

			}
		});

		actions.add(new ToolTipAction("Exit", "exit") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {

				closeWithResult(null);

			}
		});

		return actions;
	}

}
