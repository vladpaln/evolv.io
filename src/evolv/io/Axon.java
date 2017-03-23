package evolv.io;

public class Axon {
	private static final double MUTATE_MULTI = Math.pow(0.5f, Configuration.MUTATE_POWER);

	private final double weight;
	private final double mutationRate;

	public Axon(double w, double m) {
		this.weight = w;
		this.mutationRate = m;
	}

	/*
	 * TODO pow is expensive, use Math.random(), do the same with the random()
	 * method
	 */
	public Axon mutateAxon() {
		double mutabilityMutate = Math.pow(0.5f, pmRan() * Configuration.MUTABILITY_MUTABILITY);
		return new Axon(weight + random() * mutationRate / MUTATE_MULTI, mutationRate * mutabilityMutate);
	}

	public double getWeight() {
		return weight;
	}

	private double random() {
		return Math.pow(pmRan(), Configuration.MUTATE_POWER);
	}

	private static double pmRan() {
		return Math.random() * 2 - 1;
	}
}