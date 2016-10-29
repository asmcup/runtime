package asmcup.sandbox;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

public class FrontPanel extends JPanel {

	GridBagLayout gridLayout = new GridBagLayout();
	GridBagConstraints cComponent = new GridBagConstraints();
	GridBagConstraints cLabel = new GridBagConstraints();
	GridBagConstraints cWideItem = new GridBagConstraints();
	GridBagConstraints cItemLeft = new GridBagConstraints();
	GridBagConstraints cItemRight = new GridBagConstraints();

	protected ArrayList<JSpinner> spinners = new ArrayList<>();
	
	int currentRow = 0;
	
	public FrontPanel() {
		this.setLayout(gridLayout);
		cLabel.gridx = 0;
		cLabel.fill = GridBagConstraints.HORIZONTAL;
		cComponent.gridx = 1;
		cComponent.weightx = 1;
		cComponent.fill = GridBagConstraints.HORIZONTAL;
		cItemLeft.gridx = 0;
		cItemLeft.weightx = 1;
		cItemLeft.fill = GridBagConstraints.HORIZONTAL;
		cItemRight.gridx = 1;
		cItemRight.weightx = 1;
		cItemRight.fill = GridBagConstraints.HORIZONTAL;
		cWideItem.gridx = 0;
		cWideItem.gridwidth = 2;
		normalLabels();
	}

	public void minimizeLabels() {
		cLabel.weightx = 0;
	}
	
	public void normalLabels() {
		cLabel.weightx = 1;
	}
	
	public JSpinner createSpinner(int value, int min, int max) {
		SpinnerModel model = new SpinnerNumberModel(value, min, max, 1);
		JSpinner spinner = new JSpinner(model);
		spinners.add(spinner);
		return spinner;
	}
	
	public void setSpinnersEnabled(boolean enabled) {
		for (JSpinner spinner : spinners) {
			spinner.setEnabled(enabled);
		}
	}
	
	public int getInt(JSpinner spinner) {
		return (Integer)spinner.getValue();
	}
	
	public void addRow(String label, JComponent component) {
		addRow(label, component, "");
	}
	
	public void addRow(String labelText, JComponent component, String hint) {
		JLabel label = new JLabel(labelText);
		component.setToolTipText(hint);
		label.setToolTipText(hint);
		addLabelledItem(label, component);
	}

	public void addWideItem(JComponent item) {
		cWideItem.gridy = currentRow;
		add(item, cWideItem);
		currentRow++;
	}
	public void addLabelledItem(JLabel label, JComponent component) {
		cLabel.gridy = currentRow;
		cComponent.gridy = currentRow;
		add(label, cLabel);
		add(component, cComponent);
		currentRow++;
	}
	public void addItems(JComponent left, JComponent right) {
		cItemLeft.gridy = currentRow;
		cItemRight.gridy = currentRow;
		add(left, cItemLeft);
		add(right, cItemRight);
		currentRow++;
	}
}
