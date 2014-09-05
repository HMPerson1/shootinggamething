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

public class StartPanel extends JPanel {
	private final DefaultTableModel model;
	private final Function0<Unit> startCallback;
	private final Function0<Unit> exitCallback;

	/**
	 * Create the panel.
	 */
	public StartPanel(Function0<Unit> startCallback, Function0<Unit> exitCallback) {
		
		this.startCallback = startCallback;
		this.exitCallback = exitCallback;
		
		JScrollPane scrollPane = new JScrollPane();
		JButton btnStart = new JButton("Start");
		JButton btnExit = new JButton("Exit");
		JTable table = new JTable();
		
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StartPanel.this.startCallback.apply();
			}
		});
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StartPanel.this.exitCallback.apply();
			}
		});
		
		model = new DefaultTableModel(new String[] {"History"}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setModel(model);
		
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
	
	public void addToHistory(String result) {
		model.addRow(new Object[] { result });
	}
}
