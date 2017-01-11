package evolv.io;

import java.util.ArrayList;

import processing.core.PFont;

class Brain {
	// Constants - Bring to a config file eventually
	final static int MEMORY_COUNT = 1;
	final static int BRAIN_WIDTH = 5;
	final static int BRAIN_HEIGHT = 11 + MEMORY_COUNT + 1;
	final static double AXON_START_MUTABILITY = 0.0005f;
	final static double STARTING_AXON_VARIABILITY = 1.0f;

	private final EvolvioColor evolvioColor;
	Axon[][][] axons;
	double[][] neurons;

	// labels
	String[] inputLabels = new String[BRAIN_HEIGHT];
	String[] outputLabels = new String[BRAIN_HEIGHT];

	//Generate a new brain
	public Brain(EvolvioColor evolvioColor) {
		this.evolvioColor = evolvioColor;

		// generate brain
		axons = new Axon[BRAIN_WIDTH - 1][BRAIN_HEIGHT][BRAIN_HEIGHT - 1];
		neurons = new double[BRAIN_WIDTH][BRAIN_HEIGHT];
		for (int x = 0; x < BRAIN_WIDTH - 1; x++) {
			for (int y = 0; y < BRAIN_HEIGHT; y++) {
				for (int z = 0; z < BRAIN_HEIGHT - 1; z++) {
					double startingWeight = (Math.random() * 2 - 1) * STARTING_AXON_VARIABILITY;
					axons[x][y][z] = new Axon(startingWeight, AXON_START_MUTABILITY);
				}
			}
		}
		neurons = new double[BRAIN_WIDTH][BRAIN_HEIGHT];
		for (int x = 0; x < BRAIN_WIDTH; x++) {
			for (int y = 0; y < BRAIN_HEIGHT; y++) {
				if (y == BRAIN_HEIGHT - 1) {
					neurons[x][y] = 1;
				} else {
					neurons[x][y] = 0;
				}
			}
		}

		initializeStrings();
	}

	//Create a brain from existing ones
	public Brain(ArrayList<Creature> parents) {
		evolvioColor = parents.get(0).brain.evolvioColor;
		int parentsTotal = parents.size();
		axons = new Axon[BRAIN_WIDTH - 1][BRAIN_HEIGHT][BRAIN_HEIGHT - 1];
		neurons = new double[BRAIN_WIDTH][BRAIN_HEIGHT];
		float randomParentRotation = this.evolvioColor.random(0, 1);
		for (int x = 0; x < BRAIN_WIDTH - 1; x++) {
			for (int y = 0; y < BRAIN_HEIGHT; y++) {
				for (int z = 0; z < BRAIN_HEIGHT - 1; z++) {
					float axonAngle = EvolvioColor.atan2((y + z) / 2.0f - BRAIN_HEIGHT / 2.0f, x - BRAIN_WIDTH / 2)
							/ (2 * EvolvioColor.PI) + EvolvioColor.PI;
					Brain parentForAxon = parents
							.get((int) (((axonAngle + randomParentRotation) % 1.0f) * parentsTotal)).brain;
					axons[x][y][z] = parentForAxon.axons[x][y][z].mutateAxon();
				}
			}
		}
		for (int x = 0; x < BRAIN_WIDTH; x++) {
			for (int y = 0; y < BRAIN_HEIGHT; y++) {
				float axonAngle = EvolvioColor.atan2(y - BRAIN_HEIGHT / 2.0f, x - BRAIN_WIDTH / 2)
						/ (2 * EvolvioColor.PI) + EvolvioColor.PI;
				Brain parentForAxon = parents
						.get((int) (((axonAngle + randomParentRotation) % 1.0f) * parentsTotal)).brain;
				neurons[x][y] = parentForAxon.neurons[x][y];
			}
		}
		
		initializeStrings();
	}

	private void initializeStrings() {
		String[] baseInput = { "0Hue", "0Sat", "0Bri", "1Hue", "1Sat", "1Bri", "2Hue", "2Sat", "2Bri", "Size", "MHue" };
		String[] baseOutput = { "BHue", "Accel.", "Turn", "Eat", "Fight", "Birth", "How funny?", "How popular?",
				"How generous?", "How smart?", "MHue" };
		for (int i = 0; i < 11; i++) {
			inputLabels[i] = baseInput[i];
			outputLabels[i] = baseOutput[i];
		}
		for (int i = 0; i < MEMORY_COUNT; i++) {
			inputLabels[i + 11] = "memory";
			outputLabels[i + 11] = "memory";
		}
		inputLabels[BRAIN_HEIGHT - 1] = "const.";
		outputLabels[BRAIN_HEIGHT - 1] = "const.";
	}

	public void draw(PFont font, float scaleUp, int mX, int mY) {
		final float neuronSize = 0.4f;
		this.evolvioColor.noStroke();
		this.evolvioColor.fill(0, 0, 0.4f);
		this.evolvioColor.rect((-1.7f - neuronSize) * scaleUp, -neuronSize * scaleUp,
				(2.4f + BRAIN_WIDTH + neuronSize * 2) * scaleUp, (BRAIN_HEIGHT + neuronSize * 2) * scaleUp);

		this.evolvioColor.ellipseMode(EvolvioColor.RADIUS);
		this.evolvioColor.strokeWeight(2);
		this.evolvioColor.textFont(font, 0.58f * scaleUp);
		this.evolvioColor.fill(0, 0, 1);
		for (int y = 0; y < BRAIN_HEIGHT; y++) {
			this.evolvioColor.textAlign(EvolvioColor.RIGHT);
			this.evolvioColor.text(inputLabels[y], (-neuronSize - 0.1f) * scaleUp, (y + (neuronSize * 0.6f)) * scaleUp);
			this.evolvioColor.textAlign(EvolvioColor.LEFT);
			this.evolvioColor.text(outputLabels[y], (BRAIN_WIDTH - 1 + neuronSize + 0.1f) * scaleUp,
					(y + (neuronSize * 0.6f)) * scaleUp);
		}
		this.evolvioColor.textAlign(EvolvioColor.CENTER);
		for (int x = 0; x < BRAIN_WIDTH; x++) {
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
		if (mX >= 0 && mX < BRAIN_WIDTH && mY >= 0 && mY < BRAIN_HEIGHT) {
			for (int y = 0; y < BRAIN_HEIGHT; y++) {
				if (mX >= 1 && mY < BRAIN_HEIGHT - 1) {
					drawAxon(mX - 1, y, mX, mY, scaleUp);
				}
				if (mX < BRAIN_WIDTH - 1 && y < BRAIN_HEIGHT - 1) {
					drawAxon(mX, mY, mX + 1, y, scaleUp);
				}
			}
		}
	}

	public void input(double[] inputs) {
		int end = BRAIN_WIDTH - 1;
		for (int i = 0; i < 11; i++) {
			neurons[0][i] = inputs[i];
		}
		for (int i = 0; i < MEMORY_COUNT; i++) {
			neurons[0][11 + i] = neurons[end][11 + i];
		}
		neurons[0][BRAIN_HEIGHT - 1] = 1;
		runBrain();
	}

	private void runBrain() {
		for (int x = 1; x < BRAIN_WIDTH; x++) {
			for (int y = 0; y < BRAIN_HEIGHT - 1; y++) {
				double total = 0;
				for (int input = 0; input < BRAIN_HEIGHT; input++) {
					total += neurons[x - 1][input] * axons[x - 1][input][y].weight;
				}
				if (x == BRAIN_WIDTH - 1) {
					neurons[x][y] = total;
				} else {
					neurons[x][y] = sigmoid(total);
				}
			}
		}
	}

	public double[] outputs() {
		int end = BRAIN_WIDTH - 1;
		double[] output = new double[11];
		for (int i = 0; i < 11; i++) {
			output[i] = neurons[end][i];
		}
		return output;
	}

	private void drawAxon(int x1, int y1, int x2, int y2, float scaleUp) {
		this.evolvioColor.stroke(neuronFillColor(axons[x1][y1][y2].weight * neurons[x1][y1]));

		this.evolvioColor.line(x1 * scaleUp, y1 * scaleUp, x2 * scaleUp, y2 * scaleUp);
	}

	private double sigmoid(double input) {
		return 1.0f / (1.0f + Math.pow(2.71828182846f, -input));
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