package asmcup.sandbox;

import java.awt.LayoutManager;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FrontPanel extends JPanel {

	GridBagLayout gridLayout = new GridBagLayout();
	GridBagConstraints cItem = new GridBagConstraints();
	GridBagConstraints cLabel = new GridBagConstraints();
	
	int currentRow = 0;
	
	public FrontPanel() {
		this.setLayout(gridLayout);
		cLabel.gridx = 0;
		cItem.gridx = 1;
		cLabel.anchor = GridBagConstraints.WEST;
		cItem.fill = GridBagConstraints.HORIZONTAL;
	}
	
	public void addRow(String label, JComponent component) {
		addRow(label, component, "");
	}
	
	public void addRow(String labelText, JComponent component, String hint) {
		JLabel label = new JLabel(labelText);
		component.setToolTipText(hint);
		label.setToolTipText(hint);
		addItem(label, component);
	}

	public void addItem(JLabel a, JComponent b) {
		cLabel.gridy = currentRow;
		cItem.gridy = currentRow;
		add(a, cLabel);
		add(b, cItem);
		currentRow++;
	}
	public void addItems(JComponent a, JComponent b) {
		cItem.gridy = currentRow;
		cItem.gridx = 0;
		add(a, cItem);
		cItem.gridx = 1;
		add(b, cItem);
		currentRow++;
	}
}
