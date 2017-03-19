package evolv.io;

import java.util.ArrayList;

class Brain {
	private static final int BRAIN_HEIGHT = 11 + Configuration.MEMORY_COUNT + 1;
	private static final String[] INPUT_LABELS = new String[BRAIN_HEIGHT];
	private static final String[] OUTPUT_LABELS = new String[BRAIN_HEIGHT];

	private final EvolvioColor evolvioColor;
	// Brain
	private final Axon[][][] axons;
	private final double[][] neurons;

	static {
		// input
		INPUT_LABELS[0] = "0Hue";
		INPUT_LABELS[1] = "0Sat";
		INPUT_LABELS[2] = "0Bri";
		INPUT_LABELS[3] = "1Hue";
		INPUT_LABELS[4] = "1Sat";
		INPUT_LABELS[5] = "1Bri";
		INPUT_LABELS[6] = "2Hue";
		INPUT_LABELS[7] = "2Sat";
		INPUT_LABELS[8] = "2Bri";
		INPUT_LABELS[9] = "Size";
		INPUT_LABELS[10] = "MHue";

		// output
		OUTPUT_LABELS[0] = "BHue";
		OUTPUT_LABELS[1] = "Accel.";
		OUTPUT_LABELS[2] = "Turn";
		OUTPUT_LABELS[3] = "Eat";
		OUTPUT_LABELS[4] = "Fight";
		OUTPUT_LABELS[5] = "Birth";
		OUTPUT_LABELS[6] = "How funny?";
		OUTPUT_LABELS[7] = "How popular?";
		OUTPUT_LABELS[8] = "How generous?";
		OUTPUT_LABELS[9] = "How smart?";
		OUTPUT_LABELS[10] = "MHue";

		// TODO do we need a memory and const output?

		// memory
		for (int i = 0; i < Configuration.MEMORY_COUNT; i++) {
			INPUT_LABELS[i + 11] = "Memory";
			OUTPUT_LABELS[i + 11] = "Memory";
		}

		// TODO is this the bias?
		// const
		INPUT_LABELS[BRAIN_HEIGHT - 1] = "Const.";
		OUTPUT_LABELS[BRAIN_HEIGHT - 1] = "Const.";
	}

	public Brain(EvolvioColor evolvioColor, Axon[][][] tbrain, double[][] tneurons) {
		this.evolvioColor = evolvioColor;
		// initialize brain
		if (tbrain == null) {
			axons = new Axon[Configuration.BRAIN_WIDTH - 1][BRAIN_HEIGHT][BRAIN_HEIGHT - 1];
			neurons = new double[Configuration.BRAIN_WIDTH][BRAIN_HEIGHT];
			for (int x = 0; x < Configuration.BRAIN_WIDTH - 1; x++) {
				for (int y = 0; y < BRAIN_HEIGHT; y++) {
					for (int z = 0; z < BRAIN_HEIGHT - 1; z++) {
						double startingWeight = (Math.random() * 2 - 1) * Configuration.STARTING_AXON_VARIABILITY;
						axons[x][y][z] = new Axon(startingWeight, Configuration.AXON_START_MUTABILITY);
					}
				}
			}
			for (int x = 0; x < Configuration.BRAIN_WIDTH; x++) {
				for (int y = 0; y < BRAIN_HEIGHT; y++) {
					if (y == BRAIN_HEIGHT - 1) {
						neurons[x][y] = 1;
					} else {
						neurons[x][y] = 0;
					}
				}
			}
		} else {
			axons = tbrain;
			neurons = tneurons;
		}
	}

	public Brain evolve(ArrayList<Creature> parents) {
		int parentsTotal = parents.size();
		Axon[][][] newBrain = new Axon[Configuration.BRAIN_WIDTH - 1][BRAIN_HEIGHT][BRAIN_HEIGHT - 1];
		double[][] newNeurons = new double[Configuration.BRAIN_WIDTH][BRAIN_HEIGHT];
		float randomParentRotation = this.evolvioColor.random(0, 1);
		for (int x = 0; x < Configuration.BRAIN_WIDTH - 1; x++) {
			for (int y = 0; y < BRAIN_HEIGHT; y++) {
				for (int z = 0; z < BRAIN_HEIGHT - 1; z++) {
					float axonAngle = EvolvioColor.atan2((y + z) / 2.0f - BRAIN_HEIGHT / 2.0f,
							x - Configuration.BRAIN_WIDTH / 2) / (2 * EvolvioColor.PI) + EvolvioColor.PI;
					Brain parentForAxon = parents
							.get((int) (((axonAngle + randomParentRotation) % 1.0f) * parentsTotal)).getBrain();
					newBrain[x][y][z] = parentForAxon.axons[x][y][z].mutateAxon();
				}
			}
		}
		for (int x = 0; x < Configuration.BRAIN_WIDTH; x++) {
			for (int y = 0; y < BRAIN_HEIGHT; y++) {
				float axonAngle = EvolvioColor.atan2(y - BRAIN_HEIGHT / 2.0f, x - Configuration.BRAIN_WIDTH / 2)
						/ (2 * EvolvioColor.PI) + EvolvioColor.PI;
				Brain parentForAxon = parents.get((int) (((axonAngle + randomParentRotation) % 1.0f) * parentsTotal))
						.getBrain();
				newNeurons[x][y] = parentForAxon.neurons[x][y];
			}
		}
		return new Brain(this.evolvioColor, newBrain, newNeurons);
	}

	public void draw(float scaleUp, int mX, int mY) {
		final float neuronSize = 0.4f;
		this.evolvioColor.noStroke();
		this.evolvioColor.fill(0, 0, 0.4f);
		this.evolvioColor.rect((-1.7f - neuronSize) * scaleUp, -neuronSize * scaleUp,
				(2.4f + Configuration.BRAIN_WIDTH + neuronSize * 2) * scaleUp,
				(BRAIN_HEIGHT + neuronSize * 2) * scaleUp);

		this.evolvioColor.ellipseMode(EvolvioColor.RADIUS);
		this.evolvioColor.strokeWeight(2);
		this.evolvioColor.textSize(0.58f * scaleUp);
		this.evolvioColor.fill(0, 0, 1);
		for (int y = 0; y < BRAIN_HEIGHT; y++) {
			this.evolvioColor.textAlign(EvolvioColor.RIGHT);
			this.evolvioColor.text(INPUT_LABELS[y], (-neuronSize - 0.1f) * scaleUp,
					(y + (neuronSize * 0.6f)) * scaleUp);
			this.evolvioColor.textAlign(EvolvioColor.LEFT);
			this.evolvioColor.text(OUTPUT_LABELS[y], (Configuration.BRAIN_WIDTH - 1 + neuronSize + 0.1f) * scaleUp,
					(y + (neuronSize * 0.6f)) * scaleUp);
		}
		this.evolvioColor.textAlign(EvolvioColor.CENTER);
		for (int x = 0; x < Configuration.BRAIN_WIDTH; x++) {
			for (int y = 0; y < BRAIN_HEIGHT; y++) {
				this.evolvioColor.noStroke();
				double val = neurons[x][y];
				this.evolvioColor.fill(neuronFillColor(val));
				this.evolvioColor.ellipse(x * scaleUp, y * scaleUp, neuronSize * scaleUp, neuronSize * scaleUp);
				this.evolvioColor.fill(neuronTextColor(val));
				this.evolvioColor.text(EvolvioColor.nf((float) val, 0, 1), x * scaleUp,
						(y + (neuronSize * 0.6f)) * scaleUp);
			}
		}
		if (mX >= 0 && mX < Configuration.BRAIN_WIDTH && mY >= 0 && mY < BRAIN_HEIGHT) {
			for (int y = 0; y < BRAIN_HEIGHT; y++) {
				if (mX >= 1 && mY < BRAIN_HEIGHT - 1) {
					drawAxon(mX - 1, y, mX, mY, scaleUp);
				}
				if (mX < Configuration.BRAIN_WIDTH - 1 && y < BRAIN_HEIGHT - 1) {
					drawAxon(mX, mY, mX + 1, y, scaleUp);
				}
			}
		}
	}

	public void input(double[] inputs) {
		int end = Configuration.BRAIN_WIDTH - 1;
		for (int i = 0; i < 11; i++) {
			neurons[0][i] = inputs[i];
		}
		for (int i = 0; i < Configuration.MEMORY_COUNT; i++) {
			neurons[0][11 + i] = neurons[end][11 + i];
		}
		neurons[0][BRAIN_HEIGHT - 1] = 1;
		for (int x = 1; x < Configuration.BRAIN_WIDTH; x++) {
			for (int y = 0; y < BRAIN_HEIGHT - 1; y++) {
				double total = 0;
				for (int input = 0; input < BRAIN_HEIGHT; input++) {
					total += neurons[x - 1][input] * axons[x - 1][input][y].getWeight();
				}
				if (x == Configuration.BRAIN_WIDTH - 1) {
					neurons[x][y] = total;
				} else {
					neurons[x][y] = sigmoid(total);
				}
			}
		}
	}

	public double[] outputs() {
		int end = Configuration.BRAIN_WIDTH - 1;
		double[] output = new double[11];
		for (int i = 0; i < 11; i++) {
			output[i] = neurons[end][i];
		}
		return output;
	}

	private void drawAxon(int x1, int y1, int x2, int y2, float scaleUp) {
		this.evolvioColor.stroke(neuronFillColor(axons[x1][y1][y2].getWeight() * neurons[x1][y1]));
		this.evolvioColor.line(x1 * scaleUp, y1 * scaleUp, x2 * scaleUp, y2 * scaleUp);
	}

	private double sigmoid(double input) {
		return 1.0f / (1.0f + Math.exp(-input));
	}

	private int neuronFillColor(double d) {
		if (d >= 0) {
			return this.evolvioColor.color(0, 0, 1, (float) (d));
		} else {
			return this.evolvioColor.color(0, 0, 0, (float) (-d));
		}
	}

	private int neuronTextColor(double d) {
		if (d >= 0) {
			return this.evolvioColor.color(0, 0, 0);
		} else {
			return this.evolvioColor.color(0, 0, 1);
		}
	}
}