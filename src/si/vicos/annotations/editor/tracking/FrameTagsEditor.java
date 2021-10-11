package si.vicos.annotations.editor.tracking;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import si.vicos.annotations.tracking.Interval;

/**
 * The Class FrameTagsEditor.
 */
public class FrameTagsEditor extends JComponent {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The tags. */
	private JList<String> tags = new JList<String>();

	/** The annotations. */
	private EditableAnnotatedSequence annotations;

	/** The model. */
	DefaultListModel<String> model = new DefaultListModel<String>();

	/** The status. */
	private JLabel status = new JLabel();

	/** The interval. */
	private Interval interval;

	/** The selection. */
	private Set<String> selection;

	/** The hidden. */
	private Set<String> hidden;

	/** The renderer. */
	private ListCellRenderer<String> renderer = new ListCellRenderer<String>() {

		@Override
		public Component getListCellRendererComponent(
				JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {

			return null;
		}

	};

	/**
	 * Instantiates a new frame tags editor.
	 * 
	 * @param ant
	 *            the ant
	 * @param hiddenTags
	 *            the hidden tags
	 */
	public FrameTagsEditor(EditableAnnotatedSequence ant, Set<String> hiddenTags) {
		super();
		setLayout(new BorderLayout(5, 5));

		annotations = ant;

		hidden = hiddenTags;

		add(new JScrollPane(tags, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);

		add(status, BorderLayout.NORTH);

		updateList();

		tags.setModel(model);

		tags.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				if (selection == null)
					return;

				Set<String> newSelection = new HashSet<String>(tags
						.getSelectedValuesList());

				Set<String> added = new HashSet<String>(tags
						.getSelectedValuesList());

				Set<String> removed = new HashSet<String>(selection);

				added.removeAll(selection);
				removed.removeAll(newSelection);

				annotations.addTags(interval, added);

				annotations.removeTags(interval, removed);

				selection = newSelection;

			}

		});

		tags.setSelectionModel(new DefaultListSelectionModel() {

			private static final long serialVersionUID = 1L;

			@Override
			public void setSelectionInterval(int index0, int index1) {
				if (tags.isSelectedIndex(index0)) {
					tags.removeSelectionInterval(index0, index1);
				} else {
					tags.addSelectionInterval(index0, index1);
				}
			}
		});

		final JTextField add = new JTextField();
		add.setToolTipText("Add new tag");
		add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				String query = add.getText().trim();

				if (!query.isEmpty()) {
					Set<String> added = new HashSet<String>();
					added.add(query);

					annotations.addTags(interval, added);
				}

				add.setText("");

			}
		});

		add(add, BorderLayout.SOUTH);

	}

	/**
	 * Update list.
	 */
	private void updateList() {

		// model = new DefaultListModel<String>();

		for (String tag : annotations.getTags()) {
			if (hidden.contains(tag))
				continue;

			if (model.contains(tag))
				continue;
			// if (model.g)

			model.addElement(tag);
		}

		// tags.setModel(model);

	}

	/**
	 * Sets the interval.
	 * 
	 * @param interval
	 *            the new interval
	 */
	public void setInterval(Interval interval) {

		updateList();

		HashSet<String> tagSet = new HashSet<String>();

		for (Integer i : interval)
			tagSet.addAll(annotations.getTags(i));

		tagSet.removeAll(hidden);

		int[] selection = new int[tagSet.size()];

		int i = 0;
		for (String tag : tagSet)
			selection[i++] = model.indexOf(tag);

		this.selection = null; // Important so that selection does not trigger
								// tag toggle
		tags.clearSelection();
		tags.setSelectedIndices(selection);
		this.selection = tagSet;

		this.interval = new Interval(interval);

		if (interval.isEmpty())
			status.setText(String.format("Tags on frame %d",
					interval.getBegin()));
		else
			status.setText(String.format("Tags on interval %d:%d",
					interval.getBegin(), interval.getEnd()));

	}

	/**
	 * Gets the interval.
	 * 
	 * @return the interval
	 */
	public Interval getInterval() {

		return interval;

	}

}
