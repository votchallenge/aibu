package si.vicos.annotations.editor.tracking;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Properties;

import si.vicos.annotations.tracking.AbstractAnnotatedSequence;
import si.vicos.annotations.tracking.Annotations;

/**
 * The Class TransferableAnnotations.
 */
public class TransferableAnnotations implements Transferable {

	/** The Constant ANNOTATIONS_FLAVOR. */
	protected static final DataFlavor ANNOTATIONS_FLAVOR = new DataFlavor(
			AbstractAnnotatedSequence.class, "Tracking Annotations");

	/** The Constant ANNOTATIONS_PATH_FLAVOR. */
	protected static final DataFlavor ANNOTATIONS_PATH_FLAVOR = new DataFlavor(
			String.class, "Tracking Annotations File Path");

	/** The Constant ANNOTATIONS_PROPERTIES_FLAVOR. */
	protected static final DataFlavor ANNOTATIONS_PROPERTIES_FLAVOR = new DataFlavor(
			Properties.class, "Tracking Annotations Properties");

	/** The Constant SUPPORTED_FLAVORS. */
	protected static final DataFlavor[] SUPPORTED_FLAVORS = {
			ANNOTATIONS_FLAVOR, ANNOTATIONS_PATH_FLAVOR,
			ANNOTATIONS_PROPERTIES_FLAVOR };

	/** The annotations. */
	private Annotations annotations;

	/** The ancestry. */
	private String path, name, ancestry;

	/** The color. */
	private String color;

	/**
	 * Instantiates a new transferable annotations.
	 * 
	 * @param name
	 *            the name
	 * @param annotations
	 *            the annotations
	 * @param color
	 *            the color
	 * @param ancestry
	 *            the ancestry
	 */
	public TransferableAnnotations(String name, Annotations annotations,
			Color color, String ancestry) {
		this.name = name;
		this.color = "#" + Integer.toHexString(color.getRGB());
		this.ancestry = ancestry;
		this.annotations = annotations;
	}

	/**
	 * Instantiates a new transferable annotations.
	 * 
	 * @param name
	 *            the name
	 * @param annotations
	 *            the annotations
	 * @param colorHint
	 *            the color hint
	 * @param ancestry
	 *            the ancestry
	 */
	public TransferableAnnotations(String name, Annotations annotations,
			String colorHint, String ancestry) {
		this.name = name;
		this.color = colorHint;
		this.ancestry = ancestry;
		this.annotations = annotations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return SUPPORTED_FLAVORS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.
	 * datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.equals(ANNOTATIONS_FLAVOR))
			return true;
		if (flavor.equals(ANNOTATIONS_PATH_FLAVOR))
			return true;
		if (flavor.equals(ANNOTATIONS_PROPERTIES_FLAVOR))
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer
	 * .DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException {
		if (flavor.equals(ANNOTATIONS_FLAVOR))
			return annotations;
		else if (flavor.equals(ANNOTATIONS_PATH_FLAVOR))
			return path;
		else if (flavor.equals(ANNOTATIONS_PROPERTIES_FLAVOR)) {

			Properties properties = new Properties();
			properties.setProperty("name", name);
			properties.setProperty("color", color);

			properties.setProperty("ancestry", ancestry);

			return properties;
		} else
			throw new UnsupportedFlavorException(flavor);
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name == null ? annotations.getName() : name;
	}

	/**
	 * Gets the annotations.
	 * 
	 * @return the annotations
	 */
	public Annotations getAnnotations() {
		return annotations;
	}

	/**
	 * Gets the color hint.
	 * 
	 * @return the color hint
	 */
	public String getColorHint() {
		return color;
	}

	/**
	 * Gets the ancestry.
	 * 
	 * @return the ancestry
	 */
	public String getAncestry() {
		return ancestry;
	}

}