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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.coffeeshop.string.StringUtils;

/**
 * The Class Values.
 */
public class Values {

	/** The values. */
	protected Vector<Map<String, String>> values = new Vector<Map<String, String>>();

	/**
	 * Read value.
	 * 
	 * @param value
	 *            the value
	 * @param source
	 *            the source
	 */
	public void readValue(String value, File source) {

		try {
			readValue(value, new FileInputStream(source));
		} catch (FileNotFoundException e) {
		}

	}

	/**
	 * Read value.
	 * 
	 * @param value
	 *            the value
	 * @param ins
	 *            the ins
	 */
	public void readValue(String value, InputStream ins) {

		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					ins));

			int frame = 0;

			while (true) {

				String line = reader.readLine();

				if (line == null)
					break;

				if (!line.isEmpty())
					addValue(frame, value, line);

				frame++;
			}

			reader.close();

		} catch (IOException e) {
		}

	}

	/**
	 * Write value.
	 * 
	 * @param value
	 *            the value
	 * @param outs
	 *            the outs
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void writeValue(String value, OutputStream outs) throws IOException {

		PrintWriter out = new PrintWriter(outs);

		for (Map<String, String> ls : values) {

			if (ls == null || !ls.containsKey(value))
				out.println("");
			else
				out.println(ls.get(value));

		}

		out.flush();

	}

	/**
	 * Append values.
	 * 
	 * @param values
	 *            the values
	 */
	public void appendValues(Map<String, String> values) {

		if (values == null)
			values = new HashMap<String, String>();

		addValues(size(), values);

	}

	/**
	 * Adds the value.
	 * 
	 * @param index
	 *            the index
	 * @param label
	 *            the label
	 * @param value
	 *            the value
	 */
	public void addValue(int index, String label, String value) {

		if (index < 0 || label == null || StringUtils.empty(label))
			return;

		if (index >= values.size()) {

			for (int i = values.size(); i <= index; i++)
				values.add(null);

		}

		Map<String, String> tmp = this.values.get(index);

		if (tmp == null) {
			tmp = new HashMap<String, String>();
			this.values.set(index, tmp);
		}

		tmp.put(label, value);

	}

	/**
	 * Adds the values.
	 * 
	 * @param index
	 *            the index
	 * @param values
	 *            the values
	 */
	public void addValues(int index, Map<String, String> values) {

		if (index < 0 || values == null)
			return;

		if (index >= this.values.size()) {

			for (int i = this.values.size(); i <= index; i++)
				this.values.add(null);

		}

		Map<String, String> tmp = this.values.get(index);

		if (tmp == null) {
			tmp = new HashMap<String, String>();
			this.values.set(index, tmp);
		}

		tmp.putAll(values);

	}

	/**
	 * Remove all values for a given frame.
	 * 
	 * @param index
	 *            the index
	 */
	public void removeValues(int index) {

		if (index < 0 || index >= size())
			return;

		Map<String, String> tmp = values.elementAt(index);

		if (tmp == null)
			return;

		tmp.clear();

	}
	
	/**
	 * Remove specified values for a given frame.
	 * 
	 * @param index
	 *            the index
	 * @param keys
	 *            the keys
	 */
	public void removeValues(int index, Set<String> keys) {

		if (index < 0 || index >= size())
			return;

		Map<String, String> tmp = values.elementAt(index);

		if (tmp == null)
			return;

		for (String key : keys)
			tmp.remove(key);

	}
	
	/**
	 * Gets the values.
	 * 
	 * @param index
	 *            the index
	 * @return the values
	 */
	public Map<String, String> getValues(int index) {

		if (index < 0 || index >= size())
			return null;

		Map<String, String> tmp = values.elementAt(index);

		if (tmp == null)
			return null;

		return new HashMap<String, String>(tmp);

	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public int size() {

		return values.size();

	}

	/**
	 * Gets the value keys.
	 * 
	 * @return the value keys
	 */
	public Set<String> getValueKeys() {

		HashSet<String> keys = new HashSet<String>();

		for (Map<String, String> frame : values) {
			if (frame != null)
				keys.addAll(frame.keySet());
		}

		return keys;

	}

	/**
	 * Checks if is empty.
	 * 
	 * @return true, if is empty
	 */
	public boolean isEmpty() {

		for (Map<String, String> l : values) {
			if (l == null || l.isEmpty())
				continue;
			return false;
		}
		return true;
	}

	/**
	 * Find values.
	 * 
	 * @param key
	 *            the key
	 * @return the list
	 */
	public List<String> findValues(String key) {

		Vector<String> frames = new Vector<String>();

		for (int i = 0; i < Math.min(values.size(), size()); i++) {

			if (values.get(i) != null)
				frames.add(values.get(i).get(key));
			else
				frames.add(null);

		}

		return frames;
	}
}
