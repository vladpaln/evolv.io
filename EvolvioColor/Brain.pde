class Brain {
  // Brain
  final int MEMORY_COUNT = 1;
  final int BRAIN_WIDTH = 3;
  final int BRAIN_HEIGHT = 11+MEMORY_COUNT+1;
  final double AXON_START_MUTABILITY = 0.0005;
  final double STARTING_AXON_VARIABILITY = 1.0;
  Axon[][][] axons;
  double[][] neurons;

  //labels
  String[] inputLabels = new String[BRAIN_HEIGHT];
  String[] outputLabels = new String[BRAIN_HEIGHT];

  public Brain(Axon[][][] tbrain, double[][] tneurons) {
    //initialize brain
    if (tbrain == null) {
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
          if (y == BRAIN_HEIGHT-1) {
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
    
    //initialize labels
    String[] baseInput = {"0Hue", "0Sat", "0Bri", "1Hue", 
      "1Sat", "1Bri", "2Hue", "2Sat", "2Bri", "Size", "MHue"};
    String[] baseOutput = {"BHue", "Accel.", "Turn", "Eat", "Fight", "Birth", "How funny?", 
      "How popular?", "How generous?", "How smart?", "MHue"};
    for (int i = 0; i<11; i++) {
      inputLabels[i]=baseInput[i];
      outputLabels[i] = baseOutput[i];
    }
    for (int i = 0; i<MEMORY_COUNT; i++) {
      inputLabels[i+11]="memory";
      outputLabels[i+11] = "memory";
    }
    inputLabels[BRAIN_HEIGHT-1] = "const.";
    outputLabels[BRAIN_HEIGHT-1] = "const.";
  }

  //this would be a static method, but processing doesn't like mixing types
  public Brain evolve(ArrayList<Creature> parents) {
    int parentsTotal = parents.size();
    Axon[][][] newBrain = new Axon[BRAIN_WIDTH - 1][BRAIN_HEIGHT][BRAIN_HEIGHT - 1];
    double[][] newNeurons = new double[BRAIN_WIDTH][BRAIN_HEIGHT];
    float randomParentRotation = random(0, 1);
    for (int x = 0; x < BRAIN_WIDTH - 1; x++) {
      for (int y = 0; y < BRAIN_HEIGHT; y++) {
        for (int z = 0; z < BRAIN_HEIGHT - 1; z++) {
          float axonAngle = atan2((y + z) / 2.0 - BRAIN_HEIGHT / 2.0, x - BRAIN_WIDTH / 2) / (2 * PI) + PI;
          Brain parentForAxon = parents.get((int)(((axonAngle + randomParentRotation) % 1.0) * parentsTotal)).brain;
          newBrain[x][y][z] = parentForAxon.axons[x][y][z].mutateAxon();
        }
      }
    }
    for (int x = 0; x < BRAIN_WIDTH; x++) {
      for (int y = 0; y < BRAIN_HEIGHT; y++) {
        float axonAngle = atan2(y - BRAIN_HEIGHT / 2.0, x - BRAIN_WIDTH / 2) / (2 * PI) + PI;
        Brain parentForAxon = parents.get((int)(((axonAngle + randomParentRotation) % 1.0) * parentsTotal)).brain;
        newNeurons[x][y] = parentForAxon.neurons[x][y];
      }
    }
    return new Brain(newBrain, newNeurons);
  }

  public void draw(PFont font, float scaleUp, int mX, int mY) {
    final float neuronSize = 0.4;
    noStroke();
    fill(0, 0, 0.4);
    rect((-1.7 - neuronSize) * scaleUp, -neuronSize * scaleUp, (2.4 + BRAIN_WIDTH + neuronSize * 2) * scaleUp, (BRAIN_HEIGHT + neuronSize * 2) * scaleUp);

    ellipseMode(RADIUS);
    strokeWeight(2);
    textFont(font, 0.58 * scaleUp);
    fill(0, 0, 1);
    for (int y = 0; y < BRAIN_HEIGHT; y++) {
      textAlign(RIGHT);
      text(inputLabels[y], (-neuronSize - 0.1) * scaleUp, (y + (neuronSize * 0.6)) * scaleUp);
      textAlign(LEFT);
      text(outputLabels[y], (BRAIN_WIDTH - 1 + neuronSize + 0.1) * scaleUp, (y + (neuronSize * 0.6)) * scaleUp);
    }
    textAlign(CENTER);
    for (int x = 0; x < BRAIN_WIDTH; x++) {
      for (int y = 0; y < BRAIN_HEIGHT; y++) {
        noStroke();
        double val = neurons[x][y];
        fill(neuronFillColor(val));
        ellipse(x * scaleUp, y * scaleUp, neuronSize * scaleUp, neuronSize * scaleUp);
        fill(neuronTextColor(val));
        text(nf((float)val, 0, 1), x * scaleUp, (y + (neuronSize * 0.6)) * scaleUp);
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
    for (int i = 0; i<11; i++) {
      neurons[0][i] = inputs[i];
    }
    for (int i = 0; i<MEMORY_COUNT; i++) {
      neurons[0][11+i] = neurons[end][11+i];
    }
    neurons[0][BRAIN_HEIGHT-1] = 1;
    for (int x = 1; x < BRAIN_WIDTH; x++) {
      for (int y = 0; y < BRAIN_HEIGHT-1; y++) {
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
    for (int i = 0; i<11; i++) {
      output[i] = neurons[end][i];
    }
    return output;
  }


  private void drawAxon(int x1, int y1, int x2, int y2, float scaleUp) {
    stroke(neuronFillColor(axons[x1][y1][y2].weight*neurons[x1][y1]));

    line(x1 * scaleUp, y1 * scaleUp, x2 * scaleUp, y2 * scaleUp);
  }

  private double sigmoid(double input) {
    return 1.0 / (1.0 + Math.pow(2.71828182846, -input));
  }

  private color neuronFillColor(double d) {
    if (d >= 0) {
      return color(0, 0, 1, (float)(d));
    } else {
      return color(0, 0, 0, (float)(-d));
    }
  }

  private color neuronTextColor(double d) {
    if (d >= 0) {
      return color(0, 0, 0);
    } else {
      return color(0, 0, 1);
    }
  }
}