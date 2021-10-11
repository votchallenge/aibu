package si.vicos.annotations;

import java.text.ParseException;
import java.util.Locale;

/**
 * The Class Annotation.
 * 
 * @author lukacu
 */
public abstract class Annotation implements Cloneable {

	/** The serialization locale. */
	public static Locale SERIALIZATION_LOCALE = Locale.US;

	/**
	 * The Interface AnnotationSummary.
	 * 
	 * @author lukacu
	 * @param <T>
	 *            the generic type
	 */
	public static interface AnnotationSummary<T extends Annotation> {

		/**
		 * Adds the.
		 * 
		 * @param annotation
		 *            the annotation
		 */
		public void add(T annotation);

		/**
		 * Gets the.
		 * 
		 * @return the t
		 */
		public T get();

	}

	/**
	 * The Enum AnnotationType.
	 * 
	 * @author lukacu
	 */
	public enum AnnotationType {
		/** The point. */
		POINT,
		/** The label. */
		LABEL,
		/** The rectangle. */
		RECTANGLE,
		/** The polygon. */
		POLYGON,
		/** The rectangles. */
		RECTANGLES,
		/** The segmentation mask. */
		SEGMENTATION_MASK
	}

	/** The Constant aliases. */
	private static final Object[][] aliases = {
			{ "point", PointAnnotation.class, AnnotationType.POINT },
			{ "label", LabelAnnotation.class, AnnotationType.LABEL },
			{ "rect", RectangleAnnotation.class, AnnotationType.RECTANGLE },
			{ "polygon", PolygonAnnotation.class, AnnotationType.POLYGON },
			{ "mask", SegmentationMaskAnnotation.class, AnnotationType.SEGMENTATION_MASK}};

	/**
	 * Alias to class.
	 * 
	 * @param alias
	 *            the alias
	 * @return the class
	 */
	@SuppressWarnings("unchecked")
	public static Class<Annotation> aliasToClass(String alias) {

		for (int i = 0; i < aliases.length; i++) {
			if (((String) aliases[i][0]).compareToIgnoreCase(alias) == 0) {
				return (Class<Annotation>) (aliases[i][1]);
			}
		}
		return null;
	}

	/**
	 * Class to alias.
	 * 
	 * @param cls
	 *            the cls
	 * @return the string
	 */
	public static String classToAlias(Class<Annotation> cls) {

		for (int i = 0; i < aliases.length; i++) {
			if (aliases[i][1].equals(cls)) {
				return (String) (aliases[i][0]);
			}
		}
		return null;
	}

	/**
	 * Class to enum.
	 * 
	 * @param cls
	 *            the cls
	 * @return the annotation type
	 */
	public static AnnotationType classToEnum(Class<? extends Annotation> cls) {

		for (int i = 0; i < aliases.length; i++) {
			if (aliases[i][1].equals(cls)) {
				return (AnnotationType) (aliases[i][2]);
			}
		}
		return null;
	}

	/**
	 * Alias to enum.
	 * 
	 * @param alias
	 *            the alias
	 * @return the annotation type
	 */
	public static AnnotationType aliasToEnum(String alias) {

		alias = alias.toLowerCase();

		for (int i = 0; i < aliases.length; i++) {
			if (aliases[i][0].equals(alias)) {
				return (AnnotationType) (aliases[i][2]);
			}
		}
		return null;
	}

	/**
	 * Enum to class.
	 * 
	 * @param type
	 *            the type
	 * @return the class
	 */
	@SuppressWarnings("unchecked")
	public static Class<Annotation> enumToClass(AnnotationType type) {

		for (int i = 0; i < aliases.length; i++) {
			if (aliases[i][2].equals(type)) {
				return (Class<Annotation>) (aliases[i][1]);
			}
		}
		return null;
	}

	/**
	 * Enum to alias.
	 * 
	 * @param type
	 *            the type
	 * @return the string
	 */
	public static String enumToAlias(AnnotationType type) {

		for (int i = 0; i < aliases.length; i++) {
			if (aliases[i][2].equals(type)) {
				return (String) (aliases[i][0]);
			}
		}
		return null;
	}

	/**
	 * Object to alias.
	 * 
	 * @param a
	 *            the a
	 * @return the string
	 */
	public static String objectToAlias(Annotation a) {

		Class<?> acls = a.getClass();

		for (int i = 0; i < aliases.length; i++) {
			if (aliases[i][1].equals(acls)) {
				return (String) (aliases[i][0]);
			}
		}
		return null;
	}

	/**
	 * Gets the empty.
	 * 
	 * @param type
	 *            the type
	 * @return the empty
	 */
	public static Annotation getEmpty(AnnotationType type) {

		Class<?> cl = enumToClass(type);

		if (cl == null)
			return null;

		try {
			return (Annotation) cl.newInstance();
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}

		return null;
	}

	/**
	 * Instantiates a new annotation.
	 */
	public Annotation() {
		this.reset();
	}

	/**
	 * Instantiates a new annotation.
	 * 
	 * @param data
	 *            the data
	 * @throws ParseException
	 *             the parse exception
	 */
	public Annotation(String data) throws ParseException {
		this.unpack(data);
	}

	/**
	 * Reset.
	 */
	public abstract void reset();

	/**
	 * Pack.
	 * 
	 * @return the string
	 */
	public abstract String pack();

	/**
	 * Unpack.
	 * 
	 * @param data
	 *            the data
	 * @throws ParseException
	 *             the parse exception
	 */
	public abstract void unpack(String data) throws ParseException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public abstract Annotation clone();

	/**
	 * Validate.
	 * 
	 * @param a
	 *            the a
	 * @return true, if successful
	 */
	public abstract boolean validate(Annotation a);

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public abstract AnnotationType getType();

	/**
	 * Can interpolate.
	 * 
	 * @return true, if successful
	 */
	public boolean canInterpolate() {
		return false;
	}

	/**
	 * Scale.
	 * 
	 * @param scale
	 *            the scale
	 * @return the annotation
	 * @throws UnsupportedOperationException
	 *             the unsupported operation exception
	 */
	public Annotation scale(float scale) throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Scaling not supported for type " + getType());
	}

	/**
	 * Convert.
	 * 
	 * @param a
	 *            annotation object
	 * @return the annotation
	 * @throws UnsupportedOperationException
	 *             the unsupported operation exception
	 */
	public Annotation convert(Annotation a)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Conversion not supported for type " + getType());
	}

	/**
	 * Checks if is null.
	 * 
	 * @return true, if is null
	 */
	public abstract boolean isNull();

}
