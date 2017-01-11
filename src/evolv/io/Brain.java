package evolv.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import processing.core.PFont;

class Brain {
	// Constants - Bring to a config file eventually
	final static int MEMORY_COUNT = 0;
	final static int BRAIN_WIDTH = 5;
	final static int BRAIN_HEIGHT = 11 + MEMORY_COUNT + 1;
	final static int STARTING_AXON_COUNT = 20;

	private final EvolvioColor evolvioColor;
	List<Axon> axons;
	List<Axon> learning;
	double[][] neurons;
	int count;

	// labels
	String[] inputLabels = new String[BRAIN_HEIGHT];
	String[] outputLabels = new String[BRAIN_HEIGHT];

	// Generate a new brain
	public Brain(EvolvioColor evolvioColor) {
		this.evolvioColor = evolvioColor;

		// Make sure axon bounds are set before the set are made
		// In case we have different sized brains
		Axon.setMaxX(BRAIN_WIDTH);
		Axon.setMaxY(BRAIN_HEIGHT);

		// initialize brain
		axons = new ArrayList<Axon>();
		for (int i = 0; i < STARTING_AXON_COUNT; i++) {
			axons.add(new Axon());
		}
		Collections.sort(axons);
		simplifyBrain();

		learning = new ArrayList<Axon>();
		count = 0;
		
		neurons = new double[BRAIN_WIDTH][BRAIN_HEIGHT];
		initArray(neurons);

		initStrings();
	}

	// Create a brain from existing ones
	public Brain(ArrayList<Creature> parents) {
		this.evolvioColor = parents.get(0).brain.evolvioColor;

		// initialize brain
		axons = new ArrayList<Axon>();
		for (Iterator<Creature> iter = parents.iterator(); iter.hasNext();) {
			for(Iterator<Axon> ater = iter.next().brain.axons.iterator();ater.hasNext();){
				axons.add(ater.next().mutateAxon());
			}
		}
		Collections.sort(axons);
		simplifyBrain();
		
		learning = new ArrayList<Axon>();
		count = 0;
		
		neurons = new double[BRAIN_WIDTH][BRAIN_HEIGHT];
		initArray(neurons);

		initStrings();
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
		for (Iterator<Axon> iter = axons.iterator(); iter.hasNext();) {
			drawAxon(iter.next(), scaleUp);
		}
		for (Iterator<Axon> iter = learning.iterator(); iter.hasNext();) {
			drawAxon(iter.next(), scaleUp);
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

	public double[] outputs() {
		int end = BRAIN_WIDTH - 1;
		double[] output = new double[11];
		for (int i = 0; i < 11; i++) {
			output[i] = neurons[end][i];
		}
		return output;
	}

	private void drawAxon(Axon axon, float scaleUp) {
		this.evolvioColor.stroke(neuronFillColor(axon.weight * neurons[axon.startX][axon.startY]));

		this.evolvioColor.line(axon.startX * scaleUp, axon.startY * scaleUp, axon.endX * scaleUp, axon.endY * scaleUp);
	}

	private void initArray(double[][] array) {
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[x].length; y++) {
				array[x][y] = 0;
			}
		}
	}

	private void initStrings() {
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

	private int neuronFillColor(double d) {
		if (d >= 0) {
			return this.evolvioColor.color(0, 0, 1, (float) (0.5+d));
		} else {
			return this.evolvioColor.color(0, 0, 0, (float) (0.5-d));
		}
	}

	private int neuronTextColor(double d) {
		if (d >= 0) {
			return this.evolvioColor.color(0, 0, 0);
		} else {
			return this.evolvioColor.color(0, 0, 1);
		}
	}

	private void runBrain() {
		double[][] temp = new double[neurons.length][neurons[0].length];
		initArray(temp);

		for (Iterator<Axon> iter = axons.iterator(); iter.hasNext();) {
			Axon curr = iter.next();
			temp[curr.endX][curr.endY] += curr.weight * neurons[curr.startX][curr.startY];
		}
		for (Iterator<Axon> iter = learning.iterator(); iter.hasNext();) {
			Axon curr = iter.next();
			temp[curr.endX][curr.endY] += curr.weight * neurons[curr.startX][curr.startY];
		}
		for (int x = 1; x < temp.length - 1; x++) {
			for (int y = 0; y < temp[x].length; y++) {
				neurons[x][y] = sigmoid(temp[x][y]);
			}
		}
		for (int y = 0; y < temp[temp.length - 1].length; y++) {
			neurons[temp.length - 1][y] = temp[temp.length - 1][y];
		}
		count++;
		outputLabels[8] = Integer.toString(count);
		if(this.count>1000){
			count = 0;
			learning.add(new Axon());
			for(ListIterator<Axon> iter = learning.listIterator();iter.hasNext();){
				iter.set(iter.next().mutateAxon());
			}
		}
	}

	private double sigmoid(double input) {
		return 1.0f / (1.0f + Math.pow(2.71828182846f, -input));
	}

	private void simplifyBrain() {
		ListIterator<Axon> iter = axons.listIterator();
		Axon last = iter.next();
		List<Axon> duplicates = new ArrayList<Axon>();
		boolean change = false;
		while (iter.hasNext()) {
			change = false;
			duplicates.clear();
			Axon curr = iter.next();
			while (curr.compareTo(last) == 0) {
				change = true;
				iter.remove();
				if (iter.hasNext())
					curr = iter.next();
				else
					break;
			}
			if (change) {
				duplicates.add(iter.previous());
				iter.set(new Axon(duplicates));
			}
			last = curr;
		}
	}
}