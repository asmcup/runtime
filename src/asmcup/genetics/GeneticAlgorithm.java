package asmcup.genetics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GeneticAlgorithm {
	public Random random;
	
	public Evaluator evaluator;

	public Gene[] population;
	public int generation;

	public Gene defaultGene;
	public int mutationRate = 50;
	public int minMutationRate = 1;
	public int maxMutationRate = 100;
	public int mutationSize = 4;
	public ArrayList<Gene> pinned;
	
	public int dnaLength = 256;

	// FIXME: Handle DNA length changes gracefully
	// TODO: Meaningful initial population?
	
	public GeneticAlgorithm(Evaluator evaluator) {
		this.evaluator = evaluator;

		random = new Random();
		population = new Gene[0];
		pinned = new ArrayList<>();
	}

	public void initializePopulation(int populationSize) {
		population = new Gene[populationSize];
		for (int i=0; i < population.length; i++) {
			population[i] = getDefaultGene();
		}
	}
	
	public void resizePopulation(int newSize) {
		if (population.length == newSize) {
			return;
		}
		
		Gene[] newPop = new Gene[newSize];
		
		for (int i=0; i < newSize; i++) {
			if (i < population.length) {
				newPop[i] = population[i];
			} else {
				newPop[i] = getDefaultGene();
			}
		}
		
		population = newPop;
	}
	
	public void rescorePopulation() {
		for (int i = 0; i < population.length; i++) {
			population[i] = new Gene(population[i].dna);
		}
	}
	
	public void nextGeneration() {
		adjustMutationRate();
		
		int halfPoint = population.length / 2;
		int pin = pinned.size();
		
		for (int i=halfPoint; i < population.length; i++) {
			if (pin > 0) {
				pin--;
				population[i] = cross(pinned.get(pin), selectRandomGene());
			} else {
				population[i] = cross();
			}
		}
		
		Arrays.sort(population);
		generation++;
	}

	private byte randomByte() {
		return (byte)random.nextInt(256);
	}
	
	public Gene getDefaultGene() {
		return new Gene(randomDNA());
		// FIXME: Where to get default gene from?
		//return defaultGene.clone();
	}
	
	private byte[] randomDNA() {
		byte[] dna = new byte[dnaLength];
		for (int i = 0; i < dnaLength; i++) {
			dna[i] = randomByte();
		}
		return dna;
	}

	public Gene selectRandomGene() {
		int i = random.nextInt(population.length / 2);
		return population[i];
	}
	
	public Gene cross() {
		int a, b;
		
		do {
			a = random.nextInt(population.length / 2);
			b = random.nextInt(population.length / 2);
		} while (a == b);
		
		return cross(population[a], population[b]);
	}
	
	public Gene cross(Gene mom, Gene dad) {
		byte[] dna = mom.dna.clone();
		
		int src, dest, size;
		
		if (random.nextInt(100) <= mutationRate) {
			dest = random.nextInt(dnaLength);
			size = 1 + random.nextInt(mutationSize);
			
			for (int i=0; i < size; i++) {
				dna[(dest + i) % dnaLength] = randomByte();
			}
		}
		src = random.nextInt(dnaLength);
		dest = random.nextInt(dnaLength);
		size = 1 + random.nextInt(dnaLength);
		
		for (int i=0; i < size; i++) {
			dna[(dest + i) % dnaLength] = dad.dna[(src + i) % dnaLength];
		}
		
		return new Gene(dna);
	}

	public byte[] getBestDNA() {
		return population[0].dna.clone();
	}
	
	public float getBestScore() {
		return population[0].score;
	}
	
	public float getWorstScore() {
		return population[population.length / 2 - 1].score;
	}
	
	public void adjustMutationRate() {
		float p = getWorstScore() / getBestScore();
		// TODO: That's the laziest lerp I've ever seen...
		mutationRate = minMutationRate + (int)(p * maxMutationRate);
		mutationRate = Math.max(minMutationRate, mutationRate);
		mutationRate = Math.min(maxMutationRate, mutationRate);
	}
	
	public void pin() {
		pinned.add(population[0]);
	}
	
	public void unpin() {
		pinned.clear();
	}
	
	
	private class Gene implements Comparable<Gene> {
		public final byte[] dna;
		public final float score;
		public final int gen;
		
		
		// TODO: Constructor (so gen is always defined)
		// TODO: Score gene in constructor?

		public Gene(byte[] dna) {
			this(dna, evaluator.score(dna), generation);
		}
		
		private Gene(byte[] dna, float score, int gen) {
			this.dna = dna;
			this.score = score;
			this.gen = gen;
		}

		public int compareTo(Gene other) {
			float d = score - other.score;
			
			if (d == 0) {
				return other.gen - gen;
			} else if (d < 0) {
				return 1;
			}
			
			return -1;
		}
		
		public Gene clone()
		{
			return new Gene(dna.clone(), score, generation);
		}
	}
}