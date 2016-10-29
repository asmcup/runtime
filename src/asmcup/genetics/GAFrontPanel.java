package asmcup.genetics;

import javax.swing.*;

import asmcup.sandbox.FrontPanel;

public class GAFrontPanel extends FrontPanel {
	public final GeneticAlgorithm ga;
	protected JSpinner popSpinner = createSpinner(100, 1, 1000 * 1000);
	protected JSpinner mutationSpinner = createSpinner(100, 0, 100);
	protected JSpinner sizeSpinner = createSpinner(256, 1, 256);
	protected JSpinner chunkSpinner = createSpinner(4, 0, 256);
	protected JLabel bestLabel = new JLabel("0");
	protected JLabel worstLabel = new JLabel("0");
	protected JLabel genLabel = new JLabel("0");
	protected JLabel mutationLabel = new JLabel("0");
	
	public GAFrontPanel(GeneticAlgorithm ga) {
		this.ga = ga;
		
		setBorder(BorderFactory.createTitledBorder("Gene Pool"));

		addRow("Population:", popSpinner, "Number of robots that are kept in the gene pool");
		addRow("Mutation Chance:", mutationSpinner, "Maximum chance that mutation will occur during mating");
		addRow("Mutation Size:", chunkSpinner, "Maximum number of bytes that will be changed per mutation");
		addRow("Program Size:", sizeSpinner, "Number of bytes in the ROM that will be used");
		addRow("Best:", bestLabel, "Highest score in the gene pool");
		addRow("Worst:", worstLabel, "Lowest score in the gene pool");
		addRow("Mutation:", mutationLabel, "Current chance of mutation");
		addRow("Generation:", genLabel, "Current generation of gene pool");
	}

	public void update() {
		ga.maxMutationRate = getInt(mutationSpinner);
		ga.dnaLength = getInt(sizeSpinner);
		ga.mutationSize = getInt(chunkSpinner);
		
		//ga.resizePopulation(getInt(popSpinner));
	}
	
	public void updateStats() {
		worstLabel.setText(String.valueOf(ga.getWorstScore()));
		bestLabel.setText(String.valueOf(ga.getBestScore()));
		genLabel.setText(String.valueOf(ga.generation));
		mutationLabel.setText(String.valueOf(ga.mutationRate) + "%");
	}
	
	public int getPopulationSize() {
		return getInt(popSpinner);
	}
}
