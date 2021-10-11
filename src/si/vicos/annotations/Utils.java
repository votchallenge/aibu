package si.vicos.annotations;

import java.lang.reflect.Array;

/**
 * The Class Utils.
 */
public final class Utils {

	/**
	 * Copy of.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param original
	 *            the original
	 * @param newLength
	 *            the new length
	 * @return the t[]
	 */
	@SuppressWarnings("unchecked")
	// In Java 5, the java.util.Arrays class has no copyOf() members
	public static <T> T[] copyOf(T[] original, int newLength) {
		Class<T> type = (Class<T>) original.getClass().getComponentType();
		T[] newArr = (T[]) Array.newInstance(type, newLength);

		System.arraycopy(original, 0, newArr, 0,
				Math.min(original.length, newLength));

		return newArr;
	}

	/**
	 * Copy of.
	 * 
	 * @param original
	 *            the original
	 * @param newLength
	 *            the new length
	 * @return the int[]
	 */
	public static int[] copyOf(int[] original, int newLength) {
		int[] newArr = new int[newLength];

		System.arraycopy(original, 0, newArr, 0,
				Math.min(original.length, newLength));

		return newArr;
	}

	/**
	 * Copy of.
	 * 
	 * @param original
	 *            the original
	 * @param newLength
	 *            the new length
	 * @return the char[]
	 */
	public static char[] copyOf(char[] original, int newLength) {
		char[] newArr = new char[newLength];

		System.arraycopy(original, 0, newArr, 0,
				Math.min(original.length, newLength));

		return newArr;
	}

	/**
	 * Append.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param original
	 *            the original
	 * @param add
	 *            the add
	 * @return the t[]
	 */
	public static <T> T[] append(T[] original, T add) {

		T[] newArr = copyOf(original, original.length + 1);
		newArr[original.length] = add;

		return newArr;
	}

}
