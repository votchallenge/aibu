package si.vicos.annotations.editor.tracking;

import org.coffeeshop.application.Application;

import si.vicos.annotations.Annotation;
import si.vicos.annotations.editor.Interpolator;
import si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.EditList;
import si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.EditRegionOperation;
import si.vicos.annotations.editor.tracking.EditableAnnotatedSequence.EditTagAddOperation;
import si.vicos.annotations.editor.tracking.TrackingEditor.EditAssistant;

/**
 * The Class InterpolationAssistant.
 */
public class InterpolationAssistant implements EditAssistant {

	/** The Constant instance. */
	public static final InterpolationAssistant instance = new InterpolationAssistant();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.tracking.TrackingEditor.EditAssistant#editFrame
	 * (si.vicos.annotations.editor.tracking.EditableAnnotatedSequence, int,
	 * si.vicos.annotations.Annotation)
	 */
	@Override
	public EditList editFrame(EditableAnnotatedSequence sequence, int frame,
			Annotation region) {

		EditList list = new EditList();

		try {

			list.add(new EditRegionOperation(frame, region));
			list.add(new EditTagAddOperation(frame, "keyframe"));

			// Interpolate forward if possible

			int last = 0;

			for (last = frame + 1; last < sequence.size(); last++)
				if (sequence.hasTag(last, "keyframe")
						&& !sequence.get(last).isNull())
					break;

			if (!(last == sequence.size() || frame > last - 2))
				list.addAll(interpolate(sequence, frame, region, last,
						sequence.get(frame)));

			// Interpolate backwards if possible

			int first = 0;

			for (first = frame - 1; first >= 0; first--)
				if (sequence.hasTag(first, "keyframe")
						&& !sequence.get(first).isNull())
					break;

			if (!(first == -1 || first > frame - 2))
				list.addAll(interpolate(sequence, first, sequence.get(first),
						frame, region));

		} catch (Exception e) {

			Application.getApplicationLogger().report(e);

		}

		return list;
	}

	/**
	 * Interpolate.
	 * 
	 * @param sequence
	 *            the sequence
	 * @param start
	 *            the start
	 * @param startValue
	 *            the start value
	 * @param stop
	 *            the stop
	 * @param stopValue
	 *            the stop value
	 * @return the edits the list
	 */
	private EditList interpolate(EditableAnnotatedSequence sequence, int start,
			Annotation startValue, int stop, Annotation stopValue) {

		if (start > stop - 2)
			throw new IllegalArgumentException("Interval too short");

		if (startValue == null)
			throw new IllegalArgumentException("Start item does not exist");

		if (stopValue == null)
			throw new IllegalArgumentException("Stop item does not exist");

		if (startValue.isNull())
			throw new IllegalArgumentException("Start value is not set");

		if (stopValue.isNull())
			throw new IllegalArgumentException("Stop value is not set");

		float step = 1.0f / (float) (stop - start);
		float value = step;

		Interpolator interpolator = new Interpolator(startValue, stopValue);

		EditList list = new EditList();

		for (int i = start + 1; i < stop; i++) {

			Annotation a = interpolator.interpolate(value);

			if (a == null)
				continue;

			list.add(new EditableAnnotatedSequence.EditRegionOperation(i, a));

			value += step;
		}

		return list;

	}

}
