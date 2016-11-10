package asmcup.evaluation;

import asmcup.genetics.Spawn;

public class SpawnEvaluator extends Evaluator {
	final protected Spawns spawns;
	
	public SpawnEvaluator(Spawns spawns, boolean simplified) {
		super(simplified);
		this.spawns = spawns;
	}
	
	@Override
	public float score(byte[] ram) {
		baseSeed = spawns.getCombinedSeed();
		float score = super.score(ram);
		
		Scorer scorer = new Scorer();
		for (Spawn spawn : spawns) {
			score += scorer.calculate360(ram, spawn);
		}
		
		return score;
	}
}