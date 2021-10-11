package si.vicos.annotations.tracking;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.coffeeshop.ReferenceCollection;
import org.coffeeshop.string.StringUtils;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.AnnotationsMetadata;

/**
 * The Class AnnotatedSequence.
 */
public class AnnotatedSequence extends AbstractAnnotatedSequence {

	/**
	 * The Class AnnotationsMetadataReader.
	 */
	public static class AnnotationsMetadataReader implements
			AnnotationsMetadata {

		/** The metadata. */
		protected Properties metadata = new Properties();

		/** The thumbnail. */
		protected BufferedImage thumbnail;

		/** The region. */
		protected Annotation region;

		/**
		 * Instantiates a new annotations metadata reader.
		 * 
		 * @param file
		 *            the file
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public AnnotationsMetadataReader(File file) throws IOException {

			if (!file.exists())
				throw new IOException("File does not exist");

			if (file.isDirectory()) {

				readFromDirectory(file);

			} else if (file.getName().compareTo("groundtruth.txt") == 0) {

				readFromDirectory(file.getParentFile());

			} else {

				if (!file.isFile()
						|| !file.toString().toLowerCase()
								.endsWith(FILE_EXTENSION))
					throw new IOException("File must be a valid AVT file");

				ZipFile zip = new ZipFile(file);

				ZipEntry metadataEntry = zip.getEntry("properties.txt");

				if (metadataEntry == null) {
					zip.close();
					throw new IOException(
							"Not a valid AVT file (must contain metadata)");
				}

				metadata.load(zip.getInputStream(metadataEntry));

				try {

					ZipEntry thumbnailEntry = zip.getEntry("thumbnail.png");

					if (thumbnailEntry != null) {
						thumbnail = ImageIO.read(zip
								.getInputStream(thumbnailEntry));
					}

				} catch (IOException e) {
				}

				zip.close();

			}

		}

		/**
		 * Read from directory.
		 * 
		 * @param file
		 *            the file
		 * @throws FileNotFoundException
		 *             the file not found exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void readFromDirectory(File file) throws FileNotFoundException,
				IOException {

			File imageFile = new File(file, String.format("%08d.jpg", 1));

			try {

				if (imageFile.exists() && imageFile.isFile()) {

					thumbnail = ImageIO.read(new FileInputStream(imageFile));

				}

				File groundtruthFile = new File(file, "groundtruth.txt");

				BufferedReader reader = new BufferedReader(new FileReader(
						groundtruthFile));

				String line = reader.readLine();
				region = Trajectory.parseRegion(line);

				reader.close();

			} catch (IOException e) {

			} catch (ParseException e) {

			}

			File propertiesFile = new File(file, "properties.txt");

			if (propertiesFile.exists() && propertiesFile.isFile())
				metadata.load(new FileInputStream(propertiesFile));

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * si.vicos.annotations.AnnotationsMetadata#getMetadata(java.lang.String
		 * )
		 */
		@Override
		public String getMetadata(String name) {
			return metadata.getProperty(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.AnnotationsMetadata#getPreviewImage()
		 */
		@Override
		public Image getPreviewImage() {
			return thumbnail;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.AnnotationsMetadata#getKeys()
		 */
		@Override
		public Set<String> getKeys() {
			Set<String> set = new HashSet<String>();

			metadata.keySet().addAll(set);

			return set;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.AnnotationsMetadata#getPreviewRegion()
		 */
		@Override
		public Annotation getPreviewRegion() {
			return region;
		}

	}

	/** The Constant FILE_EXTENSION. */
	public static final String FILE_EXTENSION = ".avt";

	/** The directory. */
	protected File directory;

	/** The metadata. */
	protected Properties metadata = new Properties();

	/** The files. */
	protected Vector<String> files = new Vector<String>();

	/** The trajectory. */
	protected Trajectory trajectory = new Trajectory();

	/** The tags. */
	protected Tags tags = new Tags();

	/** The values. */
	protected Values values = new Values();

	/** The name. */
	protected String name;

	/** The listeners. */
	private ReferenceCollection<AnnotatedSequenceListener> listeners = new ReferenceCollection<AnnotatedSequenceListener>();

	/**
	 * Instantiates a new annotated sequence.
	 */
	protected AnnotatedSequence() {

	}

	/**
	 * Instantiates a new annotated sequence.
	 * 
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public AnnotatedSequence(File file) throws IOException {

		if (!file.exists())
			throw new IOException("File does not exist");

		if (file.isDirectory()) {

			readDirectory(file);

			name = file.getName();
			directory = file;

		} else if (file.getName().compareTo("groundtruth.txt") == 0) {

			readDirectory(file.getParentFile());

			name = file.getParentFile().getName();
			directory = file.getParentFile();

		} else {

			if (!file.isFile()
					|| !file.toString().toLowerCase().endsWith(FILE_EXTENSION))
				throw new IOException("File must be a valid AVT file");

			name = file.getName();
			name = name.substring(0, name.length() - FILE_EXTENSION.length());
			directory = file.getParentFile();

			readCompressedFile(file);

		}
	}

	/**
	 * Read directory.
	 * 
	 * @param directory
	 *            the directory
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void readDirectory(File directory) throws IOException {

		File regionsFile = new File(directory, "groundtruth.txt");

		if (!regionsFile.exists())
			throw new IOException("Unable to open groundtruth file");

		File metadataFile = new File(directory, "properties.txt");

		if (metadataFile.exists()) {

			metadata.load(new FileInputStream(metadataFile));

		}

		trajectory = new Trajectory(regionsFile);

		File imagesFile = new File(directory, "images.txt");

		if (imagesFile.exists()) {

			readFiles(new FileInputStream(imagesFile));
			metadata.setProperty("list", "images.txt");

		} else {

			String pattern = metadata.getProperty("pattern", "%08d.jpg");

			for (int i = 1; i <= trajectory.size(); i++) {
				files.add(String.format(pattern, i));
			}

		}

		File[] labelFiles = directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".label");
			}
		});

		tags = new Tags(trajectory.size()); // Make the tags storage equal to
											// trajectory in size

		for (File labelFile : labelFiles) {
			tags.read(removeSuffix(labelFile.getName(), ".label"), labelFile);
		}

		File[] valueFiles = directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".value");
			}
		});

		for (File valueFile : valueFiles) {
			values.readValue(removeSuffix(valueFile.getName(), ".value"),
					valueFile);
		}

	}

	/**
	 * Read compressed file.
	 * 
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void readCompressedFile(File file) throws IOException {
		ZipFile zip = new ZipFile(file);

		ZipEntry metadataEntry = zip.getEntry("properties.txt");

		if (metadataEntry == null) {
			zip.close();
			throw new IOException(
					"Not a valid AVT file (must contain metadata)");
		}

		metadata.load(zip.getInputStream(metadataEntry));

		if (metadata.contains("path")) {
			directory = new File(metadata.getProperty("path"));
			if (!directory.isAbsolute()) {
				directory = new File(file.getParentFile(),
						metadata.getProperty("path"));
			}
		} else {
			directory = file.getParentFile();
		}

		ZipEntry filesEntry = zip.getEntry("images.txt");

		if (filesEntry != null) {
			readFiles(zip.getInputStream(filesEntry));
		}

		ZipEntry rectanglesEntry = zip.getEntry("groundtruth.txt");

		if (rectanglesEntry != null) {
			trajectory = new Trajectory(zip.getInputStream(rectanglesEntry));
		}

		for (Enumeration<? extends ZipEntry> e = zip.entries(); e
				.hasMoreElements();) {

			ZipEntry entry = e.nextElement();

			if (entry.isDirectory())
				continue;

			if (entry.getName().endsWith(".label")) {
				tags.read(removeSuffix(entry.getName(), ".label"),
						zip.getInputStream(entry));
				continue;
			}

			if (entry.getName().endsWith(".value")) {
				values.readValue(removeSuffix(entry.getName(), ".value"),
						zip.getInputStream(entry));
				continue;
			}
		}

		zip.close();
	}

	/**
	 * Read files.
	 * 
	 * @param in
	 *            the in
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void readFiles(InputStream in) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		while (true) {

			String line = reader.readLine();

			if (line == null)
				break;

			if (StringUtils.empty(line))
				files.add(null);
			else
				files.add(line);
		}

		reader.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#getName()
	 */
	public String getName() {

		return name;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.AnnotationsMetadata#getMetadata(java.lang.String)
	 */
	public String getMetadata(String key) {

		return metadata.getProperty(key);

	}

	/**
	 * Gets the metadata.
	 * 
	 * @return the metadata
	 */
	public Map<String, String> getMetadata() {

		HashMap<String, String> m = new HashMap<String, String>();

		metadata.putAll(m);

		return m;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#getValueKeys()
	 */
	public Set<String> getValueKeys() {

		return values.getValueKeys();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#getTags()
	 */
	public Set<String> getTags() {

		return tags.getTags();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#get(int)
	 */
	public Annotation get(int frame) {
		if (frame < 0 || frame >= trajectory.size())
			return null;

		return trajectory.get(frame);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#getTags(int)
	 */
	public Set<String> getTags(int frame) {

		return tags.getTag(frame);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#hasTag(int,
	 * java.lang.String)
	 */
	public boolean hasTag(int index, String tag) {

		return tags.hasTag(index, tag);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AbstractAnnotatedSequence#size()
	 */
	public int size() {

		return files.size();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Context#getImageFile(java.lang.Object)
	 */
	@Override
	public File getImageFile(Integer entry) {
		if (entry < 0 || entry >= size())
			return null;
		return new File(directory, files.elementAt(entry));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#getDirectory()
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * Write.
	 * 
	 * @param destination
	 *            the destination
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void write(File destination) throws IOException {

		if (destination.isDirectory()) {

			writeToDirectory(destination);

		} else if (destination.getName().compareTo("groundtruth.txt") == 0) {

			writeToDirectory(destination.getParentFile());

		} else {

			writeToFile(destination);

		}

	}

	/**
	 * Write to directory.
	 * 
	 * @param destination
	 *            the destination
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void writeToDirectory(File destination) throws IOException {

		File metadataFile = new File(destination, "properties.txt");

		if (!metadata.isEmpty())
			metadata.store(new FileOutputStream(metadataFile), "Aibu");
		else if (metadataFile.exists())
			metadataFile.delete();

		if (metadata.containsKey("list")) {
			writeImageList(new FileOutputStream(new File(destination,
					"images.txt")));
		}

		trajectory.writeTrajectory(new FileOutputStream(new File(destination,
				"groundtruth.txt")));

		Set<String> tagDictionary = tags.getDictionary();
		Set<String> tagSet = tags.getTags();
		for (String tag : tagDictionary) {
			File tagFile = new File(destination, String.format("%s.label", tag));
			if (!tagSet.contains(tag) && tagFile.exists()) {
				tagFile.delete();
				continue;
			}
			tags.write(tag, new FileOutputStream(tagFile));
		}

		for (String value : values.getValueKeys()) {
			values.writeValue(value, new FileOutputStream(new File(destination,
					String.format("%s.value", value))));
		}

	}

	/**
	 * Write to file.
	 * 
	 * @param destination
	 *            the destination
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void writeToFile(File destination) throws IOException {

		FileOutputStream output = new FileOutputStream(destination);
		ZipOutputStream zip = new ZipOutputStream(output);

		writeToZipStream(zip);

		zip.finish();

		zip.close();
	}

	/**
	 * Write to zip stream.
	 * 
	 * @param zip
	 *            the zip
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected void writeToZipStream(ZipOutputStream zip) throws IOException {

		zip.putNextEntry(new ZipEntry("properties.txt"));

		metadata.store(zip, "");

		if (metadata.containsKey("list")) {
			zip.putNextEntry(new ZipEntry("images.txt"));
			writeImageList(zip);
		}

		zip.putNextEntry(new ZipEntry("groundtruth.txt"));

		trajectory.writeTrajectory(zip);

		for (String tag : tags.getTags()) {
			zip.putNextEntry(new ZipEntry(String.format("%s.label", tag)));
			tags.write(tag, zip);
		}

		for (String value : values.getValueKeys()) {
			zip.putNextEntry(new ZipEntry(String.format("%s.value", value)));
			values.writeValue(value, zip);
		}

	}

	/**
	 * Write image list.
	 * 
	 * @param outs
	 *            the outs
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeImageList(OutputStream outs) throws IOException {

		PrintWriter out = new PrintWriter(outs);

		for (String f : files) {

			if (f == null)
				out.println();
			else
				out.println(f);

		}

		out.flush();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#getPreviewImage()
	 */
	public Image getPreviewImage() {

		return getImage(1);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#findTag(java.
	 * lang.String)
	 */
	public Collection<Integer> findTag(String tag) {

		return tags.find(tag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#countTagOccurences
	 * (java.lang.String)
	 */
	public int countTagOccurences(String tag) {

		return tags.countTag(tag);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.tracking.AbstractAnnotatedSequence#findValues(java
	 * .lang.String)
	 */
	public List<String> findValues(String key) {

		return values.findValues(key);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.AnnotationsMetadata#getKeys()
	 */
	@Override
	public Set<String> getKeys() {
		Set<String> set = new HashSet<String>();

		metadata.keySet().addAll(set);

		return set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.AnnotationsMetadata#getPreviewRegion()
	 */
	@Override
	public Annotation getPreviewRegion() {
		return get(1);
	}

	/**
	 * Adds the annotated sequence listener.
	 * 
	 * @param l
	 *            the l
	 */
	public void addAnnotatedSequenceListener(AnnotatedSequenceListener l) {

		listeners.add(l);

	}

	/**
	 * Removes the annotated sequence listener.
	 * 
	 * @param l
	 *            the l
	 */
	public void removeAnnotatedSequenceListener(AnnotatedSequenceListener l) {

		listeners.remove(l);

	}

	/**
	 * Notify interval changed.
	 * 
	 * @param interval
	 *            the interval
	 */
	protected void notifyIntervalChanged(Interval interval) {

		for (AnnotatedSequenceListener l : new CopyOnWriteArrayList<AnnotatedSequenceListener>(
				listeners))
			l.intervalChanged(this, interval);

	}

	/**
	 * Notify metadata changed.
	 * 
	 * @param keys
	 *            the keys
	 */
	protected void notifyMetadataChanged(Set<String> keys) {

		Set<String> rokeys = Collections.unmodifiableSet(keys);

		for (AnnotatedSequenceListener l : listeners)
			l.metadataChanged(this, rokeys);

	}

}
