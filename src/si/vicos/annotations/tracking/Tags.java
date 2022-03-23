package si.vicos.annotations.tracking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.coffeeshop.string.StringUtils;

/**
 * The Class Tags.
 */
public class Tags {

	/** The dictionary. */
	private Set<String> dictionary = new HashSet<String>();

	/** The tags. */
	private Vector<Set<String>> tags = new Vector<Set<String>>();

	/** The length. */
	int length = 0;

	/**
	 * Instantiates a new tags.
	 */
	public Tags() {

	}

	/**
	 * Instantiates a new tags.
	 * 
	 * @param length
	 *            the length
	 */
	public Tags(int length) {

		this.length = length;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Tags clone() {

		Tags clone = new Tags();

		for (Set<String> frame : tags) {
			if (frame != null) {
				clone.tags.add(new HashSet<String>(frame));
			} else {
				clone.tags.add(null);
			}
		}

		return clone;

	}

	/**
	 * Read.
	 * 
	 * @param tag
	 *            the tag
	 * @param source
	 *            the source
	 */
	public void read(String tag, File source) {

		try {
			read(tag, new FileInputStream(source));
		} catch (FileNotFoundException e) {
		}

	}

	/**
	 * Read.
	 * 
	 * @param tag
	 *            the tag
	 * @param ins
	 *            the ins
	 */
	public void read(String tag, InputStream ins) {

		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					ins));

			int frame = 0;

			while (true) {

				String line = reader.readLine();

				if (line == null)
					break;

				if (line.compareTo("1") == 0)
					addTag(frame, tag);

				frame++;
			}

			reader.close();

		} catch (IOException e) {
		}

		dictionary.addAll(getTags());

	}

	/**
	 * Write.
	 * 
	 * @param tag
	 *            the tag
	 * @param outs
	 *            the outs
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void write(String tag, OutputStream outs) throws IOException {

		PrintWriter out = new PrintWriter(outs);

		for (Set<String> ls : tags) {

			if (ls == null || !ls.contains(tag))
				out.println("0");
			else
				out.println("1");

		}

		if (tags.size() < length)
			for (int i = 0; i < length - tags.size(); i++)
				out.println("0");

		out.flush();

	}

	/**
	 * Append tags.
	 * 
	 * @param tags
	 *            the tags
	 */
	public void appendTags(Set<String> tags) {

		if (tags == null)
			tags = new HashSet<String>();

		addTags(size(), tags);

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

		if (index < 0 || tag == null || StringUtils.empty(tag))
			return;

		if (index >= tags.size()) {

			for (int i = tags.size(); i <= index; i++)
				tags.add(null);

		}

		Set<String> frame = this.tags.get(index);

		if (frame == null) {
			frame = new HashSet<String>();
			this.tags.set(index, frame);
		}

		frame.add(tag);
		dictionary.add(tag);

		length = Math.max(length, tags.size());

	}

	/**
	 * Adds the tags.
	 * 
	 * @param index
	 *            the index
	 * @param tags
	 *            the tags
	 */
	public void addTags(int index, Set<String> tags) {

		if (index < 0 || tags == null)
			return;

		if (index >= this.tags.size()) {

			for (int i = this.tags.size(); i <= index; i++)
				this.tags.add(null);

		}

		Set<String> tmp = this.tags.get(index);

		if (tmp == null) {
			tmp = new HashSet<String>();
			this.tags.set(index, tmp);
		}

		tmp.addAll(tags);
		dictionary.addAll(tags);

		length = Math.max(length, tags.size());

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

		if (index < 0 || tag == null || StringUtils.empty(tag))
			return;

		if (index >= tags.size())
			return;

		Set<String> labels = this.tags.get(index);

		if (labels == null)
			return;

		labels.remove(tag);

	}

	/**
	 * Removes the tags.
	 * 
	 * @param index
	 *            the index
	 * @param tags
	 *            the tags
	 */
	public void removeTags(int index, Set<String> tags) {

		if (index < 0 || tags == null)
			return;

		if (index >= this.tags.size())
			return;

		Set<String> tmp = this.tags.get(index);

		if (tmp == null)
			return;

		tmp.removeAll(tags);

	}

	/**
	 * Removes all tags for a given frame.
	 * 
	 * @param index
	 *            the index
	 */
	public void removeTags(int index) {

		if (index < 0 || tags == null)
			return;

		if (index >= this.tags.size())
			return;

		Set<String> tmp = this.tags.get(index);

		if (tmp == null)
			return;

		tmp.clear();

	}
	
	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public int size() {

		return tags.size();

	}

	/**
	 * Checks for tag.
	 * 
	 * @param index
	 *            the index
	 * @param tag
	 *            the tag
	 * @return true, if successful
	 */
	public boolean hasTag(int index, String tag) {

		if (index < 0 || index >= size() || tag == null
				|| StringUtils.empty(tag))
			return false;

		Set<String> labels = this.tags.get(index);

		if (labels == null)
			return false;

		return labels.contains(tag);
	}

	/**
	 * Gets the tags.
	 * 
	 * @return the tags
	 */
	public Set<String> getTags() {

		HashSet<String> lbl = new HashSet<String>();

		for (Set<String> frame : tags) {
			if (frame != null)
				lbl.addAll(frame);
		}

		return lbl;

	}

	/**
	 * Count tag.
	 * 
	 * @param tag
	 *            the tag
	 * @return the int
	 */
	public int countTag(String tag) {

		int count = 0;

		for (int i = 0; i < Math.min(tags.size(), size()); i++) {

			if (tags.get(i) != null && tags.get(i).contains(tag))
				count++;

		}

		return count;
	}

	/**
	 * Gets the tag.
	 * 
	 * @param frame
	 *            the frame
	 * @return the tag
	 */
	public Set<String> getTag(int frame) {

		if (tags == null || tags.isEmpty())
			return new HashSet<String>();

		if (frame < 0 || frame >= tags.size())
			return new HashSet<String>();

		Set<String> l = tags.get(frame);

		if (l == null)
			return new HashSet<String>();
		else
			return new HashSet<String>(l);

	}

	/**
	 * Find.
	 * 
	 * @param tag
	 *            the tag
	 * @return the collection
	 */
	public Collection<Integer> find(String tag) {

		Vector<Integer> frames = new Vector<Integer>();

		for (int i = 0; i < Math.min(tags.size(), size()); i++) {

			if (tags.get(i) != null && tags.get(i).contains(tag))
				frames.add(i);

		}

		return frames;
	}

	/**
	 * Checks if is empty.
	 * 
	 * @return true, if is empty
	 */
	public boolean isEmpty() {

		for (Set<String> l : tags) {
			if (l == null || l.isEmpty())
				continue;
			return false;
		}
		return true;
	}

	/**
	 * Returns a set of all tag names that were ever used in this tags object
	 * even if they are no longer present.
	 * 
	 * @return an unmodifiable set of all tag names
	 */
	public Set<String> getDictionary() {

		return Collections.unmodifiableSet(dictionary);

	}

}
