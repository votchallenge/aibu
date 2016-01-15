package si.vicos.annotations.editor.tracking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.coffeeshop.Callback;
import org.coffeeshop.io.Streams;
import org.coffeeshop.string.StringUtils;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.tracking.AnnotatedSequence;
import si.vicos.annotations.tracking.Interval;

// http://blog.jaimon.co.uk/simpleimageinfo/SimpleImageInfo.java.html

/**
 * The Class EditableAnnotatedSequence.
 */
public class EditableAnnotatedSequence extends AnnotatedSequence {

	/**
	 * The Interface EditOperation.
	 */
	public static interface EditOperation {

		/**
		 * Apply.
		 * 
		 * @param sequence
		 *            the sequence
		 * @return the edits the operation
		 */
		public EditOperation apply(EditableAnnotatedSequence sequence);

	}

	/**
	 * The Class EditMetadataOperation.
	 */
	public static class EditMetadataOperation implements EditOperation {

		/** The name. */
		private String name;

		/** The value. */
		private String value;

		/**
		 * Instantiates a new edits the metadata operation.
		 * 
		 * @param name
		 *            the name
		 * @param value
		 *            the value
		 */
		public EditMetadataOperation(String name, String value) {

			this.name = name;
			this.value = StringUtils.empty(value) ? null : value;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.
		 * EditOperation
		 * #apply(si.vicos.annotations.editor.tracking.EditableAnnotatedSequence
		 * )
		 */
		public EditOperation apply(EditableAnnotatedSequence sequence) {

			if (StringUtils.same((String) sequence.metadata.get(name), value))
				return null;

			String old = (String) sequence.metadata.get(name);

			if (value == null)
				sequence.metadata.remove(name);
			else
				sequence.metadata.put(name, value);

			return new EditMetadataOperation(name, old);
		}

	}

	/**
	 * The Interface FrameEditOperation.
	 */
	public static interface FrameEditOperation extends EditOperation {

		/**
		 * Gets the frame.
		 * 
		 * @return the frame
		 */
		public int getFrame();

	}

	/**
	 * The Class EditRegionOperation.
	 */
	public static class EditRegionOperation implements FrameEditOperation {

		/** The frame. */
		private int frame;

		/** The region. */
		private Annotation region;

		/**
		 * Instantiates a new edits the region operation.
		 * 
		 * @param frame
		 *            the frame
		 * @param region
		 *            the region
		 */
		public EditRegionOperation(int frame, Annotation region) {

			this.frame = frame;
			this.region = region;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.
		 * EditOperation
		 * #apply(si.vicos.annotations.editor.tracking.EditableAnnotatedSequence
		 * )
		 */
		public EditOperation apply(EditableAnnotatedSequence sequence) {

			if (frame < 0 || frame >= sequence.size())
				return null;

			Annotation old = sequence.trajectory.get(frame);

			sequence.trajectory.set(frame, region);

			return new EditRegionOperation(frame, old);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.
		 * FrameEditOperation#getFrame()
		 */
		@Override
		public int getFrame() {

			return frame;

		}

	}

	/**
	 * The Class EditTagAddOperation.
	 */
	public static class EditTagAddOperation implements FrameEditOperation {

		/** The frame. */
		private int frame;

		/** The tag. */
		private String tag;

		/**
		 * Instantiates a new edits the tag add operation.
		 * 
		 * @param frame
		 *            the frame
		 * @param tag
		 *            the tag
		 */
		public EditTagAddOperation(int frame, String tag) {

			this.frame = frame;
			this.tag = tag;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.
		 * EditOperation
		 * #apply(si.vicos.annotations.editor.tracking.EditableAnnotatedSequence
		 * )
		 */
		public EditOperation apply(EditableAnnotatedSequence sequence) {

			if (frame < 0 || frame >= sequence.size())
				return null;

			if (sequence.tags.hasTag(frame, tag))
				return null;

			sequence.tags.addTag(frame, tag);

			return new EditTagRemoveOperation(frame, tag);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.
		 * FrameEditOperation#getFrame()
		 */
		@Override
		public int getFrame() {

			return frame;

		}

	}

	/**
	 * The Class EditTagRemoveOperation.
	 */
	public static class EditTagRemoveOperation implements FrameEditOperation {

		/** The frame. */
		private int frame;

		/** The tag. */
		private String tag;

		/**
		 * Instantiates a new edits the tag remove operation.
		 * 
		 * @param frame
		 *            the frame
		 * @param tag
		 *            the tag
		 */
		public EditTagRemoveOperation(int frame, String tag) {

			this.frame = frame;
			this.tag = tag;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.
		 * EditOperation
		 * #apply(si.vicos.annotations.editor.tracking.EditableAnnotatedSequence
		 * )
		 */
		public EditOperation apply(EditableAnnotatedSequence sequence) {

			if (frame < 0 || frame >= sequence.size())
				return null;

			if (!sequence.tags.hasTag(frame, tag))
				return null;

			sequence.tags.removeTag(frame, tag);

			return new EditTagAddOperation(frame, tag);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.
		 * FrameEditOperation#getFrame()
		 */
		@Override
		public int getFrame() {

			return frame;

		}

	}

	/**
	 * The Class EditList.
	 */
	public static class EditList extends Vector<EditOperation> {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiates a new edits the list.
		 * 
		 * @param edits
		 *            the edits
		 */
		public EditList(EditOperation... edits) {
			super();
			for (EditOperation edit : edits)
				add(edit);
		}

	}

	/** The source. */
	private File source;

	/** The modified. */
	protected boolean modified = false;

	/**
	 * Instantiates a new editable annotated sequence.
	 * 
	 * @param images
	 *            the images
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public EditableAnnotatedSequence(File[] images) throws IOException {
		super();

		if (images.length < 1)
			throw new IllegalArgumentException("No file given");

		directory = images[0].getParentFile();
		name = directory.getName();

		source = new File(directory, "groundtruth.txt");

		for (int i = 0; i < images.length; i++) {
			files.add(images[i].getName());
			trajectory.add(new RectangleAnnotation());
		}

		metadata.setProperty("list", "images.txt");
		modified = true;

	}

	/**
	 * Instantiates a new editable annotated sequence.
	 * 
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public EditableAnnotatedSequence(File file) throws IOException {
		super(file);

		if (!file.isDirectory())
			source = file;
		else
			source = new File(file, "groundtruth.txt");

		checksum = getMetadata("checksum");

		modified = false;

	}

	/**
	 * Apply edits.
	 * 
	 * @param edits
	 *            the edits
	 * @param applied
	 *            the applied
	 * @param reverses
	 *            the reverses
	 */
	protected void applyEdits(Collection<EditOperation> edits,
			List<EditOperation> applied, List<EditOperation> reverses) {

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		HashSet<String> keys = new HashSet<String>();

		for (EditOperation edit : edits) {

			EditOperation reverse = edit.apply(this);

			if (reverse == null)
				continue;

			if (edit instanceof FrameEditOperation) {

				min = Math.min(min, ((FrameEditOperation) edit).getFrame());
				max = Math.max(max, ((FrameEditOperation) edit).getFrame());

			} else if (edit instanceof EditMetadataOperation) {
				keys.add(((EditMetadataOperation) edit).name);
			}

			if (reverses != null)
				reverses.add(reverse);

			if (applied != null)
				applied.add(edit);

		}

		if (!keys.isEmpty() || min <= max) {

			if (!keys.isEmpty())
				notifyMetadataChanged(keys);

			if (min <= max)
				notifyIntervalChanged(new Interval(min, max));

			touch();
		}

	}

	/**
	 * Edits the.
	 * 
	 * @param edits
	 *            the edits
	 * @return the collection
	 */
	public Collection<EditOperation> edit(Collection<EditOperation> edits) {

		Vector<EditOperation> reverses = new Vector<EditOperation>();

		applyEdits(edits, null, reverses);

		return reverses;
	}

	/**
	 * Gets the source.
	 * 
	 * @return the source
	 */
	public File getSource() {

		return source;

	}

	/**
	 * Sets the region.
	 * 
	 * @param index
	 *            the index
	 * @param annotation
	 *            the annotation
	 */
	public void setRegion(int index, Annotation annotation) {

		edit(new EditList(new EditRegionOperation(index, annotation)));

	}

	/**
	 * Sets the metadata.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public void setMetadata(String name, String value) {

		edit(new EditList(new EditMetadataOperation(name, value)));

	}

	/**
	 * Adds the tag.
	 * 
	 * @param index
	 *            the index
	 * @param tag
	 *            the tag
	 */
	public void addTag(int index, String tag) {

		HashSet<String> set = new HashSet<String>();
		set.add(tag);

		addTags(new Interval(index), set);
	}

	/**
	 * Adds the tag.
	 * 
	 * @param interval
	 *            the interval
	 * @param tag
	 *            the tag
	 */
	public void addTag(Interval interval, String tag) {

		HashSet<String> set = new HashSet<String>();
		set.add(tag);

		addTags(interval, set);

	}

	/**
	 * Adds the tags.
	 * 
	 * @param interval
	 *            the interval
	 * @param add
	 *            the add
	 */
	public void addTags(Interval interval, Set<String> add) {

		if (!interval.isWithin(new Interval(0, size())) || add == null
				|| add.isEmpty())
			return;

		EditList list = new EditList();

		for (Integer index : interval) {

			for (String tag : add)
				list.add(new EditTagAddOperation(index, tag));

		}

		edit(list);

	}

	/**
	 * Removes the tag.
	 * 
	 * @param index
	 *            the index
	 * @param tag
	 *            the tag
	 */
	public void removeTag(int index, String tag) {

		HashSet<String> set = new HashSet<String>();
		set.add(tag);

		removeTags(new Interval(index), set);

	}

	/**
	 * Removes the tag.
	 * 
	 * @param interval
	 *            the interval
	 * @param tag
	 *            the tag
	 */
	public void removeTag(Interval interval, String tag) {

		HashSet<String> set = new HashSet<String>();
		set.add(tag);

		removeTags(interval, set);

	}

	/**
	 * Removes the tags.
	 * 
	 * @param interval
	 *            the interval
	 * @param remove
	 *            the remove
	 */
	public void removeTags(Interval interval, Set<String> remove) {

		if (!interval.isWithin(new Interval(0, size())) || remove == null
				|| remove.isEmpty())
			return;

		EditList list = new EditList();

		for (Integer index : interval) {

			for (String tag : remove)
				list.add(new EditTagRemoveOperation(index, tag));

		}

		edit(list);
	}

	/** The checksum. */
	private String checksum;

	/**
	 * Generate checksum.
	 * 
	 * @param callback
	 *            the callback
	 * @return the string
	 */
	public String generateChecksum(Callback callback) {

		if (checksum != null)
			return checksum;

		byte[] buffer = new byte[1024];

		try {

			MessageDigest complete = MessageDigest.getInstance("MD5");

			for (int i = 0; i < size(); i++) {

				if (callback != null) {

					callback.callback(this, (float) i / size());

				}

				FileInputStream fis = new FileInputStream(getImageFile(i));

				int numRead;
				do {
					numRead = fis.read(buffer);
					if (numRead > 0) {
						complete.update(buffer, 0, numRead);
					}
				} while (numRead != -1);

				fis.close();

			}

			if (callback != null) {

				callback.callback(this, 1);

			}

			byte[] b = complete.digest();
			checksum = "";

			for (int i = 0; i < b.length; i++) {
				checksum += Integer.toString((b[i] & 0xff) + 0x100, 16)
						.substring(1);
			}

			setMetadata("checksum", checksum);

			return checksum;

		} catch (NoSuchAlgorithmException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.tracking.AnnotatedSequence#write(java.io.File)
	 */
	@Override
	public void write(File directory) throws IOException {

		if (checksum != null) {
			setMetadata("checksum", checksum);
		}

		super.write(directory);

		modified = false;

	}

	/**
	 * Export as package.
	 * 
	 * @param file
	 *            the file
	 * @param callback
	 *            the callback
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void exportAsPackage(File file, Callback callback)
			throws IOException {

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));

		for (int i = 0; i < size(); i++) {

			File image = getImageFile(i);

			out.putNextEntry(new ZipEntry(image.getName()));

			FileInputStream in = new FileInputStream(image);
			Streams.copyStream(in, out);
			in.close();

			if (callback != null) {

				callback.callback(this, (float) i / size());

			}

		}

		writeToZipStream(out);

		out.putNextEntry(new ZipEntry("README.txt"));

		printReadme(out);

		out.close();

	}

	/**
	 * Prints the readme.
	 * 
	 * @param out
	 *            the out
	 */
	private void printReadme(OutputStream out) {

		PrintWriter writer = new PrintWriter(out);

		String title = getMetadata("title");

		String author = getMetadata("author.annotation");

		writer.printf("Title: %s\n", StringUtils.empty(title) ? "n/a" : title);
		writer.printf("Author: %s\n\n", StringUtils.empty(author) ? "n/a"
				: author);

		String notes = getMetadata("notes");

		if (!StringUtils.empty(notes)) {

			writer.println(notes);

		}

		writer.println("\n=== Annotations ===\n");
		writer.println("The annotations are stored in an AVT file, which is a ZIP-based file format");
		writer.println("designed for storing annotations for visual tracking. Inside you can find");
		writer.println("several plain-text files. Information like bounding boxes can be extracted");
		writer.println("from a CSV-formatted 'rectangles' file. More about the format will be published");
		writer.println("soon along with a GUI for editing the annotations.\n");

		String reference = getMetadata("reference");

		if (!StringUtils.empty(reference)) {

			writer.println("=== Citing ===");

			writer.println("If you use this sequence in an academic paper you have to refer to it by");
			writer.println("citing the following paper:\n\n" + reference);

		}

		writer.flush();
	}

	/**
	 * Checks if is modified.
	 * 
	 * @return true, if is modified
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Touch.
	 */
	public void touch() {
		modified = true;
	}

}
