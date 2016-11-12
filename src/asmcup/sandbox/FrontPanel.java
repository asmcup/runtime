package asmcup.sandbox;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

public class FrontPanel extends JPanel {
	protected GridBagLayout gridLayout = new GridBagLayout();
	protected GridBagConstraints cComponent = new GridBagConstraints();
	protected GridBagConstraints cLabel = new GridBagConstraints();
	protected GridBagConstraints cWideItem = new GridBagConstraints();
	protected GridBagConstraints cItemLeft = new GridBagConstraints();
	protected GridBagConstraints cItemRight = new GridBagConstraints();
	protected ArrayList<JComponent> components = new ArrayList<>();
	protected int currentRow = 0;
	
	public FrontPanel() {
		setLayout(gridLayout);
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
	
	@SuppressWarnings("rawtypes")
	public JSpinner createSpinner(Number value, Comparable min, Comparable max) {
		return createSpinner(value, min, max, 1);
	}

	@SuppressWarnings("rawtypes")
	public JSpinner createSpinner(Number value, Comparable min, Comparable max, Number step) {
		SpinnerModel model = new SpinnerNumberModel(value, min, max, step);
		JSpinner spinner = new JSpinner(model);
		components.add(spinner);
		return spinner;
	}
	
	public JCheckBox createCheckBox() {
		JCheckBox checkbox = new JCheckBox();
		components.add(checkbox);
		return checkbox;
	}
	
	public void setComponentsEnabled(boolean enabled) {
		for (JComponent component : components) {
			component.setEnabled(enabled);
		}
	}
	
	public int getInt(JSpinner spinner) {
		return (Integer)spinner.getValue();
	}
	
	public float getFloat(JSpinner spinner) {
		return (Float)spinner.getValue();
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
