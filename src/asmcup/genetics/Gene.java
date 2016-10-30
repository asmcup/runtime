package asmcup.genetics;

public class Gene implements Comparable<Gene> {
	public final byte[] dna;
	public final float score;
	public final int gen;
	
	public Gene(byte[] dna, int gen, float score) {
		this.dna = dna;
		this.gen = gen;
		this.score = score;
	}
	
	public int compareTo(Gene other) {
		float d = score - other.score;
		
		if (d == 0) {
			return 0;
		} else if (d < 0) {
			return 1;
		}
		
		return -1;
	}
}