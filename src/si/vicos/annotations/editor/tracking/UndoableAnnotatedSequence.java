package si.vicos.annotations.editor.tracking;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.coffeeshop.ReferenceCollection;

/**
 * The Class UndoableAnnotatedSequence.
 */
public class UndoableAnnotatedSequence extends EditableAnnotatedSequence {

	/**
	 * The Class AnnotationEdit.
	 */
	private class AnnotationEdit implements UndoableEdit {

		/** The edits. */
		private Collection<EditOperation> edits;

		/** The redo. */
		private boolean redo = false;

		/** The state. */
		private Object state;

		/**
		 * Instantiates a new annotation edit.
		 * 
		 * @param edits
		 *            the edits
		 * @param state
		 *            the state
		 */
		public AnnotationEdit(Collection<EditOperation> edits, Object state) {

			this.edits = edits;
			this.state = state;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.undo.UndoableEdit#addEdit(javax.swing.undo.UndoableEdit)
		 */
		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.undo.UndoableEdit#replaceEdit(javax.swing.undo.UndoableEdit
		 * )
		 */
		@Override
		public boolean replaceEdit(UndoableEdit anEdit) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.UndoableEdit#die()
		 */
		@Override
		public void die() {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.UndoableEdit#undo()
		 */
		@Override
		public void undo() throws CannotUndoException {

			if (redo)
				throw new CannotUndoException();

			state = updateState(state);

			edits = editInternal(edits);

			redo = true;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.UndoableEdit#canUndo()
		 */
		@Override
		public boolean canUndo() {

			return !redo;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.UndoableEdit#redo()
		 */
		@Override
		public void redo() throws CannotRedoException {

			if (!redo)
				throw new CannotRedoException();

			state = updateState(state);

			edits = editInternal(edits);

			redo = false;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.UndoableEdit#canRedo()
		 */
		@Override
		public boolean canRedo() {
			return redo;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.UndoableEdit#isSignificant()
		 */
		@Override
		public boolean isSignificant() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.UndoableEdit#getPresentationName()
		 */
		@Override
		public String getPresentationName() {
			return "Annotation edit";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.UndoableEdit#getUndoPresentationName()
		 */
		@Override
		public String getUndoPresentationName() {
			return "Undo annotation edit";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.UndoableEdit#getRedoPresentationName()
		 */
		@Override
		public String getRedoPresentationName() {
			return "Redo annotation edit";
		};

	}

	/** The edit listeners. */
	private ReferenceCollection<UndoableEditListener> editListeners = new ReferenceCollection<UndoableEditListener>();

	/** The saved state. */
	private Object savedState = new Object();

	/** The current state. */
	private Object currentState = savedState;

	/**
	 * Instantiates a new undoable annotated sequence.
	 * 
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public UndoableAnnotatedSequence(File file) throws IOException {
		super(file);
	}

	/**
	 * Instantiates a new undoable annotated sequence.
	 * 
	 * @param files
	 *            the files
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public UndoableAnnotatedSequence(File[] files) throws IOException {
		super(files);
	}

	/**
	 * Update state.
	 * 
	 * @param state
	 *            the state
	 * @return the object
	 */
	private Object updateState(Object state) {

		Object tmp = currentState;

		currentState = state;

		return tmp;

	}

	/**
	 * Edits the internal.
	 * 
	 * @param edits
	 *            the edits
	 * @return the collection
	 */
	private Collection<EditOperation> editInternal(
			Collection<EditOperation> edits) {

		return super.edit(edits);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.tracking.EditableAnnotatedSequence#edit(java
	 * .util.Collection)
	 */
	@Override
	public synchronized Collection<EditOperation> edit(
			Collection<EditOperation> edits) {

		Object state = updateState(new Object());
		Collection<EditOperation> reverse = editInternal(edits);

		if (reverse.isEmpty()) {
			state = updateState(state);
			return null;
		}

		AnnotationEdit edit = new AnnotationEdit(reverse, state);

		notifyUndoableEdit(edit);

		return edit.edits;
	}

	/**
	 * Notify undoable edit.
	 * 
	 * @param edit
	 *            the edit
	 */
	private void notifyUndoableEdit(AnnotationEdit edit) {

		UndoableEditEvent e = new UndoableEditEvent(this, edit);

		for (UndoableEditListener listener : editListeners)
			listener.undoableEditHappened(e);

	}

	/**
	 * Adds the edit listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addEditListener(UndoableEditListener listener) {

		editListeners.add(listener);

	}

	/**
	 * Removes the edit listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeEditListener(UndoableEditListener listener) {

		editListeners.remove(listener);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.tracking.EditableAnnotatedSequence#write(
	 * java.io.File)
	 */
	@Override
	public void write(File directory) throws IOException {

		super.write(directory);

		savedState = currentState;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * si.vicos.annotations.editor.tracking.EditableAnnotatedSequence#isModified
	 * ()
	 */
	@Override
	public boolean isModified() {

		return savedState != currentState;

	}

}
