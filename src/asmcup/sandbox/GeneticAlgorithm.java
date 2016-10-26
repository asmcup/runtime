package asmcup.sandbox;

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
		population = new Gene[1];
		population[0] = new Gene();
		population[0].dna = new byte[dnaLength];
		generation = 0;
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
		// FIXME: Where to get default gene from?
		return population[0].clone();
		//return defaultGene.clone();
	}
	
	public Gene selectRandomGene() {
		int i = random.nextInt(population.length / 2);
		return population[i];
	}
	
	public void scorePopulation() {
		for (Gene gene : population) {
			gene.score = evaluator.score(gene.dna);
		}
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
		Gene gene = new Gene();
		gene.dna = mom.dna.clone();
		gene.gen = generation;
		
		int src, dest, size;
		
		if (random.nextInt(100) <= mutationRate) {
			dest = random.nextInt(dnaLength);
			size = 1 + random.nextInt(mutationSize);
			
			for (int i=0; i < size; i++) {
				gene.dna[(dest + i) % dnaLength] = randomByte();
			}
		}
		src = random.nextInt(dnaLength);
		dest = random.nextInt(dnaLength);
		size = 1 + random.nextInt(dnaLength);
		
		for (int i=0; i < size; i++) {
			gene.dna[(dest + i) % dnaLength] = dad.dna[(src + i) % dnaLength];
		}
		
		gene.score = evaluator.score(gene.dna);
		return gene;
	}

	public Gene getBest() {
		return population[0];
	}
	
	public Gene getWorst() {
		return population[population.length / 2 - 1];
	}
	
	public void adjustMutationRate() {
		float p = getWorst().score / getBest().score;
		mutationRate = minMutationRate + (int)(p * maxMutationRate);
		mutationRate = Math.max(minMutationRate, mutationRate);
		mutationRate = Math.min(maxMutationRate, mutationRate);
	}
	
	public void pin() {
		pinned.add(getBest());
	}
	
	public void unpin() {
		pinned.clear();
	}
	

	public static class Gene implements Comparable<Gene> {
		byte[] dna;
		float score;
		int gen;
		
		// TODO: Constructor (so gen is always defined)
		// TODO: Score gene in constructor?
		
		public int compareTo(Gene other) {
			float d = score - other.score;
			
			if (d == 0) {
				return other.gen - gen;
			} else if (d < 0) {
				return 1;
			}
			
			return -1;
		}
		
		// TODO: resizeDna method!
		
		public Gene clone()
		{
			Gene clone = new Gene();
			clone.dna = dna.clone();
			clone.score = score;
			clone.gen = 0;
			return clone;
		}
	}
}