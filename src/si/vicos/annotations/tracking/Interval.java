package si.vicos.annotations.tracking;

import java.util.Iterator;

/**
 * The Class Interval.
 */
public class Interval implements Iterable<Integer> {

	/** The begin. */
	private int begin;

	/** The end. */
	private int end;

	/**
	 * Instantiates a new interval.
	 * 
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 */
	public Interval(int begin, int end) {
		super();
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}

	/**
	 * Instantiates a new interval.
	 * 
	 * @param position
	 *            the position
	 */
	public Interval(int position) {
		this(position, position);
	}

	/**
	 * Instantiates a new interval.
	 * 
	 * @param interval
	 *            the interval
	 */
	public Interval(Interval interval) {

		this(interval.begin, interval.end);
	}

	/**
	 * Gets the begin.
	 * 
	 * @return the begin
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * Gets the end.
	 * 
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Length.
	 * 
	 * @return the int
	 */
	public int length() {
		return end - begin;
	}

	/**
	 * Checks if is empty.
	 * 
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return begin == end;
	}

	/**
	 * Returns true if the given interval full contains the interval .
	 * 
	 * @param interval
	 *            the interval
	 * @return true, if is within
	 */
	public boolean isWithin(Interval interval) {

		return (interval.begin <= begin && interval.end >= end);

	}

	/**
	 * Returns true if the position is within the interval.
	 * 
	 * @param position
	 *            the position
	 * @return true, if successful
	 */
	public boolean contains(int position) {

		return (position >= begin && end >= position);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {

			int current = begin;

			@Override
			public void remove() {

			}

			@Override
			public Integer next() {

				if (!hasNext())
					return null;

				return current++;
			}

			@Override
			public boolean hasNext() {

				return current <= end;

			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + begin + ", " + end + "]";
	}

}
