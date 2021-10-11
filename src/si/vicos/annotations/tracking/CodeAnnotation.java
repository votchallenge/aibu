package si.vicos.annotations.tracking;

import java.text.ParseException;

import si.vicos.annotations.Annotation;

/**
 * The Class CodeAnnotation.
 */
public class CodeAnnotation extends Annotation {

	/** The code. */
	private int code;

	/**
	 * Instantiates a new code annotation.
	 * 
	 * @param code
	 *            the code
	 */
	public CodeAnnotation(int code) {
		this.code = code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#reset()
	 */
	@Override
	public void reset() {
		code = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#pack()
	 */
	@Override
	public String pack() {
		return String.valueOf(code);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#unpack(java.lang.String)
	 */
	@Override
	public void unpack(String data) throws ParseException {

		code = Integer.parseInt(data);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#clone()
	 */
	@Override
	public Annotation clone() {
		return new CodeAnnotation(code);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.Annotation#validate(si.vicos.annotations.Annotation)
	 */
	@Override
	public boolean validate(Annotation a) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#getType()
	 */
	@Override
	public AnnotationType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see si.vicos.annotations.Annotation#isNull()
	 */
	@Override
	public boolean isNull() {
		return false;
	}

	/**
	 * Gets the code.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

}
