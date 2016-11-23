class Creature extends SoftBody {
  // Energy
  double ACCELERATION_ENERGY = 0.18;
  double ACCELERATION_BACK_ENERGY = 0.24;
  double SWIM_ENERGY = 0.008;
  double TURN_ENERGY = 0.05;
  double EAT_ENERGY = 0.05;
  double EAT_SPEED = 0.5; // 1 is instant, 0 is nonexistent, 0.001 is verrry slow.
  double EAT_WHILE_MOVING_INEFFICIENCY_MULTIPLIER = 2.0; // The bigger this number is, the less effiently creatures eat when they're moving.
  double FIGHT_ENERGY = 0.06;
  double INJURED_ENERGY = 0.25;
  double METABOLISM_ENERGY = 0.004;
  double AGE_FACTOR = 1; // 0 no ageing
  double currentEnergy;
  final int ENERGY_HISTORY_LENGTH = 6;
  double[] previousEnergy = new double[ENERGY_HISTORY_LENGTH];

  // Family
  String name;
  String parents;
  int gen;
  int id;

  // Vision or View or Preference
  double MAX_VISION_DISTANCE = 10;
  final double FOOD_SENSITIVITY = 0.3;
  final double MAX_DETAILED_ZOOM = 3.5; // Maximum zoom to draw details at
  double[] visionAngles = {0, -0.4, 0.4};
  double[] visionDistances = {0, 0.7, 0.7};
  //double visionAngle;
  //double visionDistance;
  double[] visionOccludedX = new double[visionAngles.length];
  double[] visionOccludedY = new double[visionAngles.length];
  double visionResults[] = new double[9];

  Brain brain;
  final float BRIGHTNESS_THRESHOLD = 0.7;

  // Misc or Unsorted
  float preferredRank = 8;
  float CROSS_SIZE = 0.022;
  double mouthHue;
  double vr = 0;
  double rotation = 0;
  final double SAFE_SIZE = 1.25;
  final double MATURE_AGE = 0.01;

  NameGenerator nameGenerator = new NameGenerator();

  public Creature(double tpx, double tpy, double tvx, double tvy, double tenergy, 
    double tdensity, double thue, double tsaturation, double tbrightness, Board tb, double bt, 
    double rot, double tvr, String tname, String tparents, boolean mutateName, 
    Brain brain, int tgen, double tmouthHue) {

    super(tpx, tpy, tvx, tvy, tenergy, tdensity, thue, tsaturation, tbrightness, tb, bt);

    if(brain ==null)brain = new Brain(null,null);
    this.brain = brain;

    rotation = rot;
    vr = tvr;
    isCreature = true;
    id = board.creatureIDUpTo+1;
    if (tname.length() >= 1) {
      if (mutateName) {
        name = nameGenerator.mutateName(tname);
      } else {
        name = tname;
      }
      name = nameGenerator.sanitizeName(name);
    } else {
      name = nameGenerator.newName();
    }
    parents = tparents;
    board.creatureIDUpTo++;
    //visionAngle = 0;
    //visionDistance = 0;
    //visionEndX = getVisionStartX();
    //visionEndY = getVisionStartY();
    for (int i = 0; i < 9; i++) {
      visionResults[i] = 0;
    }
    gen = tgen;
    mouthHue = tmouthHue;
  }

  public void drawBrain(PFont font, float scaleUp, int mX, int mY) {
    brain.draw(font, scaleUp, mX, mY);
  }

  public void useBrain(double timeStep, boolean useOutput) {
    double inputs[]= new double[11];
    for (int i = 0; i < 9; i++) {
      inputs[i] = visionResults[i];
    }
    inputs[9]= energy;
    inputs[10] = mouthHue;
    brain.input(inputs);

    if (useOutput) {
      double[] output = brain.outputs();
      hue = Math.abs(output[0]) % 1.0;
      accelerate(output[1], timeStep);
      turn(output[2], timeStep);
      eat(output[3], timeStep);
      fight(output[4], timeStep * 100);
      if (output[5] > 0 && board.year-birthTime >= MATURE_AGE && energy > SAFE_SIZE) {
        reproduce(SAFE_SIZE, timeStep);
      }
      mouthHue = Math.abs(output[10]) % 1.0;
    }
  }


  public void drawSoftBody(float scaleUp, float camZoom, boolean showVision) {
    ellipseMode(RADIUS);
    double radius = getRadius();
    if (showVision && camZoom > MAX_DETAILED_ZOOM) {
      drawVisionAngles(board, scaleUp);
    }
    noStroke();
    if (fightLevel > 0) {
      fill(0, 1, 1, (float)(fightLevel * 0.8));
      ellipse((float)(px * scaleUp), (float)(py * scaleUp), (float)(FIGHT_RANGE * radius * scaleUp), (float)(FIGHT_RANGE * radius * scaleUp));
    }
    strokeWeight(board.CREATURE_STROKE_WEIGHT);
    stroke(0, 0, 1);
    fill(0, 0, 1);
    if (this == board.selectedCreature) {
      ellipse((float)(px * scaleUp), (float)(py * scaleUp), 
        (float)(radius * scaleUp + 1 + 75.0 / camZoom), (float)(radius * scaleUp + 1 + 75.0 / camZoom));
    }
    super.drawSoftBody(scaleUp);

    if (camZoom > MAX_DETAILED_ZOOM) {
      drawMouth(board, scaleUp, radius, rotation, camZoom, mouthHue);
      if (showVision) {
        fill(0, 0, 1);
        textFont(font, 0.2 * scaleUp);
        textAlign(CENTER);
        text(getCreatureName(), (float)(px * scaleUp), (float)((py - getRadius() * 1.4 - 0.07) * scaleUp));
      }
    }
  }

  public void drawVisionAngles(Board board, float scaleUp) {
    for (int i = 0; i < visionAngles.length; i++) {
      color visionUIcolor = color(0, 0, 1);
      if (visionResults[i * 3 + 2] > BRIGHTNESS_THRESHOLD) {
        visionUIcolor = color(0, 0, 0);
      }
      stroke(visionUIcolor);
      strokeWeight(board.CREATURE_STROKE_WEIGHT);
      float endX = (float)getVisionEndX(i);
      float endY = (float)getVisionEndY(i);
      line((float)(px * scaleUp), (float)(py * scaleUp), endX * scaleUp, endY * scaleUp);
      noStroke();
      fill(visionUIcolor);
      ellipse((float)(visionOccludedX[i] * scaleUp), (float)(visionOccludedY[i] * scaleUp), 2 * CROSS_SIZE * scaleUp, 2 * CROSS_SIZE * scaleUp);
      stroke((float)(visionResults[i * 3]), (float)(visionResults[i * 3 + 1]), (float)(visionResults[i * 3 + 2]));
      strokeWeight(board.CREATURE_STROKE_WEIGHT);
      line((float)((visionOccludedX[i] - CROSS_SIZE) * scaleUp), (float)((visionOccludedY[i] - CROSS_SIZE) * scaleUp), 
        (float)((visionOccludedX[i] + CROSS_SIZE) * scaleUp), (float)((visionOccludedY[i] + CROSS_SIZE) * scaleUp));
      line((float)((visionOccludedX[i] - CROSS_SIZE) * scaleUp), (float)((visionOccludedY[i] + CROSS_SIZE) * scaleUp), 
        (float)((visionOccludedX[i] + CROSS_SIZE) * scaleUp), (float)((visionOccludedY[i] - CROSS_SIZE) * scaleUp));
    }
  }

  public void drawMouth(Board board, float scaleUp, double radius, double rotation, float camZoom, double mouthHue) {
    noFill();
    strokeWeight(board.CREATURE_STROKE_WEIGHT);
    stroke(0, 0, 1);
    ellipseMode(RADIUS);
    ellipse((float)(px * scaleUp), (float)(py * scaleUp), 
      (float)(board.MINIMUM_SURVIVABLE_SIZE * scaleUp), (float)(board.MINIMUM_SURVIVABLE_SIZE * scaleUp));
    pushMatrix();
    translate((float)(px * scaleUp), (float)(py * scaleUp));
    scale((float)radius);
    rotate((float)rotation);
    strokeWeight((float)(board.CREATURE_STROKE_WEIGHT / radius));
    stroke(0, 0, 0);
    fill((float)mouthHue, 1.0, 1.0);
    ellipse(0.6 * scaleUp, 0, 0.37 * scaleUp, 0.37 * scaleUp);
    popMatrix();
  }

  public void metabolize(double timeStep) {
    double age = AGE_FACTOR * (board.year - birthTime); // the older the more work necessary
    loseEnergy(energy * METABOLISM_ENERGY * age * timeStep);
  }

  public void accelerate(double amount, double timeStep) {
    double multiplied = amount * timeStep / getMass();
    vx += Math.cos(rotation) * multiplied;
    vy += Math.sin(rotation) * multiplied;
    if (amount >= 0) {
      loseEnergy(amount * ACCELERATION_ENERGY * timeStep);
    } else {
      loseEnergy(Math.abs(amount * ACCELERATION_BACK_ENERGY * timeStep));
    }
  }

  public void turn(double amount, double timeStep) {
    vr += 0.04 * amount * timeStep / getMass();
    loseEnergy(Math.abs(amount * TURN_ENERGY * energy * timeStep));
  }

  public Tile getRandomCoveredTile() {
    double radius = (float)getRadius();
    double choiceX = 0;
    double choiceY = 0;
    while (dist((float)px, (float)py, (float)choiceX, (float)choiceY) > radius) {
      choiceX = (Math.random() * 2 * radius - radius) + px;
      choiceY = (Math.random() * 2 * radius - radius) + py;
    }
    int x = xBound((int)choiceX);
    int y = yBound((int)choiceY);
    return board.tiles[x][y];
  }

  public void eat(double attemptedAmount, double timeStep) {
    double amount = attemptedAmount / (1.0 + distance(0, 0, vx, vy) * EAT_WHILE_MOVING_INEFFICIENCY_MULTIPLIER); // The faster you're moving, the less efficiently you can eat.
    if (amount < 0) {
      dropEnergy(-amount * timeStep);
      loseEnergy(-attemptedAmount * EAT_ENERGY * timeStep);
    } else {
      Tile coveredTile = getRandomCoveredTile();
      double foodToEat = coveredTile.foodLevel * (1 - Math.pow((1 - EAT_SPEED), amount * timeStep));
      if (foodToEat > coveredTile.foodLevel) {
        foodToEat = coveredTile.foodLevel;
      }
      coveredTile.removeFood(foodToEat, true);
      double foodDistance = Math.abs(coveredTile.foodType - mouthHue);
      double multiplier = 1.0 - foodDistance / FOOD_SENSITIVITY;
      if (multiplier >= 0) {
        addEnergy(foodToEat * multiplier);
      } else {
        loseEnergy(-foodToEat * multiplier);
      }
      loseEnergy(attemptedAmount * EAT_ENERGY * timeStep);
    }
  }

  public void fight(double amount, double timeStep) {
    if (amount > 0 && board.year-birthTime >= MATURE_AGE) {
      fightLevel = amount;
      loseEnergy(fightLevel * FIGHT_ENERGY * energy * timeStep);
      for (int i = 0; i < colliders.size(); i++) {
        SoftBody collider = colliders.get(i);
        if (collider.isCreature) {
          float distance = dist((float)px, (float)py, (float)collider.px, (float)collider.py);
          double combinedRadius = getRadius() * FIGHT_RANGE + collider.getRadius();
          if (distance < combinedRadius) {
            ((Creature)collider).dropEnergy(fightLevel * INJURED_ENERGY * timeStep);
          }
        }
      }
    } else {
      fightLevel = 0;
    }
  }

  public void loseEnergy(double energyLost) {
    if (energyLost > 0) {
      energy -= energyLost;
    }
  }

  public void dropEnergy(double energyLost) {
    if (energyLost > 0) {
      energyLost = Math.min(energyLost, energy);
      energy -= energyLost;
      getRandomCoveredTile().addFood(energyLost, hue, true);
    }
  }

  public void see(double timeStep) {
    for (int k = 0; k < visionAngles.length; k++) {
      double visionStartX = px;
      double visionStartY = py;
      double visionTotalAngle = rotation + visionAngles[k];

      double endX = getVisionEndX(k);
      double endY = getVisionEndY(k);

      visionOccludedX[k] = endX;
      visionOccludedY[k] = endY;
      color c = getColorAt(endX, endY);
      visionResults[k * 3] = hue(c);
      visionResults[k * 3 + 1] = saturation(c);
      visionResults[k * 3 + 2] = brightness(c);

      int tileX = 0;
      int tileY = 0;
      int prevTileX = -1;
      int prevTileY = -1;
      ArrayList<SoftBody> potentialVisionOccluders = new ArrayList<SoftBody>();
      for (int DAvision = 0; DAvision < visionDistances[k] + 1; DAvision++) {
        tileX = (int)(visionStartX + Math.cos(visionTotalAngle) * DAvision);
        tileY = (int)(visionStartY + Math.sin(visionTotalAngle) * DAvision);
        if (tileX != prevTileX || tileY != prevTileY) {
          addPVOs(tileX, tileY, potentialVisionOccluders);
          if (prevTileX >= 0 && tileX != prevTileX && tileY != prevTileY) {
            addPVOs(prevTileX, tileY, potentialVisionOccluders);
            addPVOs(tileX, prevTileY, potentialVisionOccluders);
          }
        }
        prevTileX = tileX;
        prevTileY = tileY;
      }
      double[][] rotationMatrix = new double[2][2];
      rotationMatrix[1][1] = rotationMatrix[0][0] = Math.cos(-visionTotalAngle);
      rotationMatrix[0][1] = Math.sin(-visionTotalAngle);
      rotationMatrix[1][0] = -rotationMatrix[0][1];
      double visionLineLength = visionDistances[k];
      for (int i = 0; i < potentialVisionOccluders.size(); i++) {
        SoftBody body = potentialVisionOccluders.get(i);
        double x = body.px-px;
        double y = body.py-py;
        double r = body.getRadius();
        double translatedX = rotationMatrix[0][0] * x + rotationMatrix[1][0] * y;
        double translatedY = rotationMatrix[0][1] * x + rotationMatrix[1][1] * y;
        if (Math.abs(translatedY) <= r) {
          if ((translatedX >= 0 && translatedX < visionLineLength && translatedY < visionLineLength) ||
            distance(0, 0, translatedX, translatedY) < r ||
            distance(visionLineLength, 0, translatedX, translatedY) < r) { // YES! There is an occlussion.
            visionLineLength = translatedX-Math.sqrt(r * r - translatedY * translatedY);
            visionOccludedX[k] = visionStartX + visionLineLength * Math.cos(visionTotalAngle);
            visionOccludedY[k] = visionStartY + visionLineLength * Math.sin(visionTotalAngle);
            visionResults[k * 3] = body.hue;
            visionResults[k * 3 + 1] = body.saturation;
            visionResults[k * 3 + 2] = body.brightness;
          }
        }
      }
    }
  }

  public color getColorAt(double x, double y) {
    if (x >= 0 && x < board.boardWidth && y >= 0 && y < board.boardHeight) {
      return board.tiles[(int)(x)][(int)(y)].getColor();
    } else {
      return board.BACKGROUND_COLOR;
    }
  }

  public double distance(double x1, double y1, double x2, double y2) {
    return(Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));
  }

  public void addPVOs(int x, int y, ArrayList<SoftBody> PVOs) {
    if (x >= 0 && x < board.boardWidth && y >= 0 && y < board.boardHeight) {
      for (int i = 0; i < board.softBodiesInPositions[x][y].size(); i++) {
        SoftBody newCollider = (SoftBody)board.softBodiesInPositions[x][y].get(i);
        if (!PVOs.contains(newCollider) && newCollider != this) {
          PVOs.add(newCollider);
        }
      }
    }
  }

  public void returnToEarth() {
    int pieces = 20;
    double radius = (float)getRadius();
    for (int i = 0; i < pieces; i++) {
      getRandomCoveredTile().addFood(energy / pieces, hue, true);
    }
    for (int x = SBIPMinX; x <= SBIPMaxX; x++) {
      for (int y = SBIPMinY; y <= SBIPMaxY; y++) {
        board.softBodiesInPositions[x][y].remove(this);
      }
    }
    if (board.selectedCreature == this) {
      board.unselect();
    }
  }

  public void reproduce(double babySize, double timeStep) {
    if (colliders == null) {
      collide(timeStep);
    }
    int highestGen = 0;
    if (babySize >= 0) {
      ArrayList<Creature> parents = new ArrayList<Creature>(0);
      parents.add(this);
      double availableEnergy = getBabyEnergy();
      for (int i = 0; i < colliders.size(); i++) {
        SoftBody possibleParent = colliders.get(i);
        if (possibleParent.isCreature && ((Creature)possibleParent).brain.outputs()[9] > -1) { // Must be a WILLING creature to also give birth.
          float distance = dist((float)px, (float)py, (float)possibleParent.px, (float)possibleParent.py);
          double combinedRadius = getRadius() * FIGHT_RANGE + possibleParent.getRadius();
          if (distance < combinedRadius) {
            parents.add((Creature)possibleParent);
            availableEnergy += ((Creature)possibleParent).getBabyEnergy();
          }
        }
      }
      if (availableEnergy > babySize) {
        double newPX = random(-0.01, 0.01);
        double newPY = random(-0.01, 0.01); //To avoid landing directly on parents, resulting in division by 0)
        double newHue = 0;
        double newSaturation = 0;
        double newBrightness = 0;
        double newMouthHue = 0;
        int parentsTotal = parents.size();
        String[] parentNames = new String[parentsTotal];
        Brain newBrain = brain.evolve(parents);
        for (int i = 0; i < parentsTotal; i++) {
          int chosenIndex = (int)random(0, parents.size());
          Creature parent = parents.get(chosenIndex);
          parents.remove(chosenIndex);
          parent.energy -= babySize * (parent.getBabyEnergy() / availableEnergy);
          newPX += parent.px / parentsTotal;
          newPY += parent.py / parentsTotal;
          newHue += parent.hue / parentsTotal;
          newSaturation += parent.saturation / parentsTotal;
          newBrightness += parent.brightness / parentsTotal;
          newMouthHue += parent.mouthHue / parentsTotal;
          parentNames[i] = parent.name;
          if (parent.gen > highestGen) {
            highestGen = parent.gen;
          }
        }
        newSaturation = 1;
        newBrightness = 1;
        board.creatures.add(new Creature(newPX, newPY, 0, 0, 
          babySize, density, newHue, newSaturation, newBrightness, board, board.year, random(0, 2 * PI), 0, 
          stitchName(parentNames), andifyParents(parentNames), true, 
          newBrain, highestGen + 1, newMouthHue));
      }
    }
  }

  public String stitchName(String[] s) {
    String result = "";
    for (int i = 0; i < s.length; i++) {
      float portion = ((float)s[i].length()) / s.length;
      int start = (int)min(max(round(portion * i), 0), s[i].length());
      int end = (int)min(max(round(portion * (i + 1)), 0), s[i].length());
      result = result + s[i].substring(start, end);
    }
    return result;
  }

  public String andifyParents(String[] s) {
    String result = "";
    for (int i = 0; i < s.length; i++) {
      if (i >= 1) {
        result = result + " & ";
      }
      result = result + capitalize(s[i]);
    }
    return result;
  }

  public String getCreatureName() {
    return capitalize(name);
  }

  public String capitalize(String n) {
    return n.substring(0, 1).toUpperCase() + n.substring(1, n.length());
  }

  public void applyMotions(double timeStep) {
    if (getRandomCoveredTile().fertility > 1) {
      loseEnergy(SWIM_ENERGY * energy);
    }
    super.applyMotions(timeStep);
    rotation += vr;
    vr *= Math.max(0, 1 - FRICTION / getMass());
  }

  public double getEnergyUsage(double timeStep) {
    return (energy - previousEnergy[ENERGY_HISTORY_LENGTH - 1]) / ENERGY_HISTORY_LENGTH / timeStep;
  }

  public double getBabyEnergy() {
    return energy - SAFE_SIZE;
  }

  public void addEnergy(double amount) {
    energy += amount;
  }

  public void setPreviousEnergy() {
    for (int i = ENERGY_HISTORY_LENGTH - 1; i >= 1; i--) {
      previousEnergy[i] = previousEnergy[i - 1];
    }
    previousEnergy[0] = energy;
  }

  public double measure(int choice) {
    int sign = 1 - 2 * (choice % 2);
    if (choice < 2) {
      return sign * energy;
    } else if (choice < 4) {
      return sign * birthTime;
    } else if (choice == 6 || choice == 7) {
      return sign * gen;
    }
    return 0;
  }



  public void setHue(double set) {
    hue = Math.min(Math.max(set, 0), 1);
  }

  public void setMouthHue(double set) {
    mouthHue = Math.min(Math.max(set, 0), 1);
  }

  public void setSaturarion(double set) {
    saturation = Math.min(Math.max(set, 0), 1);
  }

  public void setBrightness(double set) {
    brightness = Math.min(Math.max(set, 0), 1);
  }

  /*public void setVisionAngle(double set) {
   visionAngle = set;//Math.min(Math.max(set, -Math.PI/2), Math.PI/2);
   while(visionAngle < -Math.PI) {
   visionAngle += Math.PI*2;
   }
   while(visionAngle > Math.PI) {
   visionAngle -= Math.PI*2;
   }
   }
   public void setVisionDistance(double set) {
   visionDistance = Math.min(Math.max(set, 0), MAX_VISION_DISTANCE);
   }*/
  /*public double getVisionStartX() {
   return px;//+getRadius()*Math.cos(rotation);
   }
   public double getVisionStartY() {
   return py;//+getRadius()*Math.sin(rotation);
   }*/

  public double getVisionEndX(int i) {
    double visionTotalAngle = rotation + visionAngles[i];
    return px + visionDistances[i] * Math.cos(visionTotalAngle);
  }

  public double getVisionEndY(int i) {
    double visionTotalAngle = rotation + visionAngles[i];
    return py + visionDistances[i] * Math.sin(visionTotalAngle);
  }
}