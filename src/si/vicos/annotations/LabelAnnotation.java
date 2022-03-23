package si.vicos.annotations;

import java.text.ParseException;

import org.coffeeshop.string.StringUtils;

/**
 * The Class LabelAnnotation.
 */
public class LabelAnnotation extends Annotation {

	/** The label. */
	private String label;

	/**
	 * Instantiates a new label annotation.
	 */
	public LabelAnnotation() {
	}

	/**
	 * Instantiates a new label annotation.
	 * 
	 * @param label
	 *            the label
	 */
	public LabelAnnotation(String label) {
		set(label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#pack()
	 */
	@Override
	public String pack() {
		return isNull() ? "" : label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#reset()
	 */
	@Override
	public void reset() {
		set("");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#unpack(java.lang.String)
	 */
	@Override
	public void unpack(String data) throws ParseException {
		set(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#clone()
	 */
	@Override
	public Annotation clone() {
		return new LabelAnnotation(label);
	}

	/**
	 * Sets the.
	 * 
	 * @param data
	 *            the data
	 */
	public void set(String data) {
		label = data;
	}

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.Annotation#validate(si.vicos.annotations.Annotation)
	 */
	@Override
	public boolean validate(Annotation a) {
		return a instanceof LabelAnnotation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#getType()
	 */
	@Override
	public AnnotationType getType() {
		return AnnotationType.LABEL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#isNull()
	 */
	@Override
	public boolean isNull() {
		return StringUtils.empty(label);
	}
}