package si.vicos.annotations.editor;

import org.coffeeshop.application.Application;
import org.coffeeshop.log.Logger;

/**
 * The Class ApplicationExceptionHandler.
 */
public class ApplicationExceptionHandler implements
		Thread.UncaughtExceptionHandler {

	/** The log. */
	public Logger log;

	/**
	 * Instantiates a new application exception handler.
	 */
	public ApplicationExceptionHandler() {

		this.log = Application.getApplicationLogger();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
	 * .Thread, java.lang.Throwable)
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {

		log.report(e);

	}

}
