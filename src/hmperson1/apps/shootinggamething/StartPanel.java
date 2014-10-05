package hmperson1.apps.shootinggamething;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;

import scala.Function0;
import scala.Unit;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * A panel that displays a history of past games, a start button, and an exit
 * button.
 * 
 * @author HMPerson1
 */
public class StartPanel extends JPanel {
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 4975374297843088070L;
	/**
	 * The table model used for modifying the history table.
	 */
	private final DefaultTableModel model;

	/**
	 * Create the panel.
	 */
	public StartPanel(Function0<Unit> startCallback,
			Function0<Unit> exitCallback) {

		JScrollPane scrollPane = new JScrollPane();
		JButton btnStart = new JButton("Start");
		JButton btnExit = new JButton("Exit");
		JTable table = new JTable();

		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startCallback.apply();
			}
		});
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exitCallback.apply();
			}
		});

		model = new DefaultTableModel(new String[] { "History" }, 0) {
			/**
			 * Serial Version UID
			 */
			private static final long serialVersionUID = -356332616103933906L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setModel(model);

		// Layout
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane)
						.addComponent(btnExit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnStart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnStart)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnExit)
					.addContainerGap())
		);

		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
		setLayout(groupLayout);
		setPreferredSize(new Dimension(200, 200));
	}

	/**
	 * Adds the given string to the history table.
	 * 
	 * @param str
	 *            the string to be added
	 */
	public void addToHistory(String str) {
		model.addRow(new Object[] { str });
	}
}
