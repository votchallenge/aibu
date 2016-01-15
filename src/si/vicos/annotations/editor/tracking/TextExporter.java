package si.vicos.annotations.editor.tracking;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.swing.filechooser.FileFilter;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.RectangleAnnotation;
import si.vicos.annotations.ShapeAnnotation;
import si.vicos.annotations.tracking.AnnotatedSequence;

/**
 * The Class TextExporter.
 */
public abstract class TextExporter extends FileFilter {

	/**
	 * The Class EightPointsExporter.
	 */
	public static class EightPointsExporter extends TextExporter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return "Eight points";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * si.vicos.annotations.editor.tracking.TextExporter#export(java.io.
		 * OutputStream, si.vicos.annotations.tracking.AnnotatedSequence)
		 */
		@Override
		public void export(OutputStream out, AnnotatedSequence annotations) {

			PrintWriter writer = new PrintWriter(out);

			for (int i = 0; i < annotations.size(); i++) {

				Annotation a = annotations.get(i);

				if (a == null || a.isNull() || !(a instanceof ShapeAnnotation)) {
					writer.println();
					continue;
				}

				RectangleAnnotation ra = ((ShapeAnnotation) a).getBoundingBox();

				writer.format("%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
						ra.getLeft(), ra.getTop(), ra.getRight(), ra.getTop(),
						ra.getRight(), ra.getBottom(), ra.getLeft(),
						ra.getBottom());

			}

			writer.flush();

		}

	}

	/**
	 * The Class BoundingBoxExporter.
	 */
	public static class BoundingBoxExporter extends TextExporter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return "Bounding box";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * si.vicos.annotations.editor.tracking.TextExporter#export(java.io.
		 * OutputStream, si.vicos.annotations.tracking.AnnotatedSequence)
		 */
		@Override
		public void export(OutputStream out, AnnotatedSequence annotations) {

			PrintWriter writer = new PrintWriter(out);

			for (int i = 0; i < annotations.size(); i++) {

				Annotation a = annotations.get(i);

				if (a == null || a.isNull() || !(a instanceof ShapeAnnotation)) {
					writer.println();
					continue;
				}

				RectangleAnnotation ra = ((ShapeAnnotation) a).getBoundingBox();

				writer.format("%.2f,%.2f,%.2f,%.2f\n", ra.getLeft(),
						ra.getTop(), ra.getWidth(), ra.getHeight());

			}

			writer.flush();

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {

		if (f.isDirectory())
			return true;

		String name = f.getName();

		return name.endsWith(".csv") || name.endsWith(".txt");
	}

	/**
	 * Export.
	 * 
	 * @param out
	 *            the out
	 * @param annotations
	 *            the annotations
	 */
	public abstract void export(OutputStream out, AnnotatedSequence annotations);

}
