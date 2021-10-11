package si.vicos.annotations.tracking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.PolygonAnnotation;
import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.SegmentationMaskAnnotation;

/**
 * The Class Trajectory.
 */
public class Trajectory implements List<Annotation>, AnnotationList {

	/** The data. */
	private Vector<Annotation> data = new Vector<Annotation>();

	/**
	 * Write trajectory.
	 * 
	 * @param out
	 *            the out
	 */
	public void writeTrajectory(OutputStream out) {

		PrintWriter writer = new PrintWriter(out);

		for (Annotation a : data) {
			String string = a.pack();
			writer.print(string);
			writer.print("\n");
		}

		writer.flush();

	}

	/**
	 * Read regions.
	 * 
	 * @param in
	 *            the in
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParseException
	 *             the parse exception
	 */
	private void readRegions(InputStream in) throws IOException, ParseException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		int lineno = 0;

		while (true) {

			String line = reader.readLine();
			lineno++;

			if (line == null)
				break;

			Annotation region = null;

			try {

				region = parseRegion(line);

				add(region);

			} catch (ParseException e) {
				throw new ParseException(e.getMessage(), lineno);
			}

		}

	}

	/**
	 * Instantiates a new trajectory.
	 */
	public Trajectory() {

	}

	/**
	 * Instantiates a new trajectory.
	 * 
	 * @param source
	 *            the source
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Trajectory(InputStream source) throws IOException {

		try {

			readRegions(source);

		} catch (ParseException pe) {

			throw new IOException(pe);
		}

	}

	/**
	 * Instantiates a new trajectory.
	 * 
	 * @param source
	 *            the source
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Trajectory(File source) throws IOException {
		try {

			readRegions(new FileInputStream(source));

		} catch (ParseException pe) {

			throw new IOException(pe);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(java.lang.Object)
	 */
	@Override
	public boolean add(Annotation e) {
		return data.add(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, Annotation element) {
		data.add(index, element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends Annotation> c) {
		return data.addAll(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends Annotation> c) {
		return data.addAll(index, c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#clear()
	 */
	@Override
	public void clear() {
		data.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return data.contains(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#get(int)
	 */
	@Override
	public Annotation get(int index) {
		return data.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		return data.indexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#iterator()
	 */
	@Override
	public Iterator<Annotation> iterator() {
		return data.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		return data.lastIndexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<Annotation> listIterator() {
		return data.listIterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<Annotation> listIterator(int index) {
		return data.listIterator(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		return data.remove(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(int)
	 */
	@Override
	public Annotation remove(int index) {
		return data.remove(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		return data.removeAll(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return data.retainAll(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public Annotation set(int index, Annotation element) {
		return data.set(index, element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#size()
	 */
	@Override
	public int size() {
		return data.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<Annotation> subList(int fromIndex, int toIndex) {
		return data.subList(fromIndex, toIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray()
	 */
	@Override
	public Object[] toArray() {
		return data.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return data.toArray(a);
	}

	/**
	 * Parses the region.
	 * 
	 * @param data
	 *            the data
	 * @return the annotation
	 * @throws ParseException
	 *             the parse exception
	 */
	public static Annotation parseRegion(String data) throws ParseException {

		if (data.isEmpty()) {

			return new CodeAnnotation(0);

		} else {

			String[] tokens = data.split(",");
			if( tokens[0].charAt(0) == 'm'){
				return new SegmentationMaskAnnotation(tokens);
			}
			else if (tokens.length == 1) {

				return new CodeAnnotation(Integer.parseInt(tokens[0]));

			} else if (tokens.length == 4) {

				return new RectangleAnnotation(Float.parseFloat(tokens[0]),
						Float.parseFloat(tokens[1]),
						Float.parseFloat(tokens[2]),
						Float.parseFloat(tokens[3]));

			} else if (tokens.length > 5 && tokens.length % 2 == 0) {

				return new PolygonAnnotation(data);

			}

			throw new ParseException("Unknown region format", -1);
		}

	}

}
