package asmcup.genetics;

import java.util.Objects;

public class Gene implements Comparable<Gene> {
	public final byte[] dna;
	public final float score;
	public final int gen;
	
	public Gene(byte[] dna, int gen, float score) {
		this.dna = dna;
		this.gen = gen;
		this.score = score;
	}

	@Override
	public int compareTo(Gene other) {
		float d = score - other.score;
		
		if (d == 0) {
			return 0;
		} else if (d < 0) {
			return 1;
		}
		
		return -1;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Gene && compareTo((Gene) obj) == 0;
	}

	@Override
	public int hashCode() {
		// explicit default implementation to hashCode the internal pointer
		return Objects.hashCode(this);
	}
}