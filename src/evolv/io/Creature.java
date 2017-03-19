package evolv.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.core.PFont;

class Creature extends SoftBody {
	private static final float CROSS_SIZE = 0.022f;
	private static final double[] VISION_ANGLES = { 0, -0.4f, 0.4f };
	private static final double[] VISION_DISTANCES = { 0, 0.7f, 0.7f };
	private static final List<CreatureAction> CREATURE_ACTIONS = Arrays.asList(new CreatureAction.AdjustHue(),
			new CreatureAction.Accelerate(), new CreatureAction.Rotate(), new CreatureAction.Eat(),
			new CreatureAction.Fight(), new CreatureAction.Reproduce(), new CreatureAction.None(),
			new CreatureAction.None(), new CreatureAction.None(), new CreatureAction.None(),
			new CreatureAction.AdjustMouthHue());

	private final EvolvioColor evolvioColor;

	private final double[] previousEnergy = new double[Configuration.ENERGY_HISTORY_LENGTH];

	// Family
	private final String name;
	private final String parents;
	private final int gen;
	private final int id;

	// Vision or View or Preference
	private final double[] visionOccludedX = new double[VISION_ANGLES.length];
	private final double[] visionOccludedY = new double[VISION_ANGLES.length];
	private final double visionResults[] = new double[9];

	private final Brain brain;

	// Misc or Unsorted
	private float preferredRank = 8;
	private double mouthHue;
	private double vr;
	private double rotation;

	// TODO can the size of these constructors be reduced?

	public Creature(EvolvioColor evolvioColor, Board board) {
		this(evolvioColor, evolvioColor.random(0, board.getBoardWidth()),
				evolvioColor.random(0, board.getBoardHeight()), 0, 0,
				evolvioColor.random(Configuration.MINIMUM_CREATURE_ENERGY, Configuration.MAXIMUM_CREATURE_ENERGY), 1,
				evolvioColor.random(0, 1), 1, 1, board, evolvioColor.random(0, 2 * EvolvioColor.PI), 0, "",
				"[PRIMORDIAL]", true, null, 1, evolvioColor.random(0, 1));
	}

	public Creature(EvolvioColor evolvioColor, double tpx, double tpy, double tvx, double tvy, double tenergy,
			double tdensity, double thue, double tsaturation, double tbrightness, Board tb, double rot, double tvr,
			String tname, String tparents, boolean mutateName, Brain brain, int tgen, double tmouthHue) {
		super(evolvioColor, tpx, tpy, tvx, tvy, tenergy, tdensity, thue, tsaturation, tbrightness, tb);
		this.evolvioColor = evolvioColor;

		if (brain == null) {
			brain = new Brain(this.evolvioColor, null, null);
		}
		this.brain = brain;
		this.rotation = rot;
		this.vr = tvr;
		this.isCreature = true;
		this.id = board.getCreatureIdUpTo() + 1;
		this.name = createName(tname, mutateName);
		this.parents = tparents;
		board.incrementCreatureIdUpTo();
		this.gen = tgen;
		this.mouthHue = tmouthHue;
	}

	private String createName(String tname, boolean mutateName) {
		if (tname.isEmpty()) {
			return NameGenerator.newName();
		}
		if (mutateName) {
			tname = NameGenerator.mutateName(tname);
		}
		return NameGenerator.sanitizeName(tname);
	}

	public void drawBrain(PFont font, float scaleUp, int mX, int mY) {
		brain.draw(font, scaleUp, mX, mY);
	}

	public void useBrain(double timeStep, boolean useOutput) {
		double inputs[] = new double[11];
		for (int i = 0; i < 9; i++) {
			inputs[i] = visionResults[i];
		}
		inputs[9] = energy;
		inputs[10] = mouthHue;
		brain.input(inputs);

		if (useOutput) {
			double[] output = brain.outputs();
			for (int i = 0; i < output.length; i++) {
				CREATURE_ACTIONS.get(i).doAction(this, output[i], timeStep);
			}
		}
	}

	public void drawSoftBody(float scaleUp, float camZoom, boolean showVision) {
		this.evolvioColor.ellipseMode(EvolvioColor.RADIUS);
		double radius = getRadius();
		if (showVision && camZoom > Configuration.MAX_DETAILED_ZOOM) {
			drawVisionAngles(board, scaleUp);
		}
		this.evolvioColor.noStroke();
		if (fightLevel > 0) {
			this.evolvioColor.fill(0, 1, 1, (float) (fightLevel * 0.8f));
			this.evolvioColor.ellipse((float) (px * scaleUp), (float) (py * scaleUp),
					(float) (FIGHT_RANGE * radius * scaleUp), (float) (FIGHT_RANGE * radius * scaleUp));
		}
		this.evolvioColor.strokeWeight(Configuration.CREATURE_STROKE_WEIGHT);
		this.evolvioColor.stroke(0, 0, 1);
		this.evolvioColor.fill(0, 0, 1);
		if (this == board.getSelectedCreature()) {
			this.evolvioColor.ellipse((float) (px * scaleUp), (float) (py * scaleUp),
					(float) (radius * scaleUp + 1 + 75.0f / camZoom), (float) (radius * scaleUp + 1 + 75.0f / camZoom));
		}
		super.drawSoftBody(scaleUp);

		if (camZoom > Configuration.MAX_DETAILED_ZOOM) {
			drawMouth(board, scaleUp, radius, rotation, camZoom, mouthHue);
			if (showVision) {
				this.evolvioColor.fill(0, 0, 1);
				this.evolvioColor.textFont(this.evolvioColor.font, 0.2f * scaleUp);
				this.evolvioColor.textAlign(EvolvioColor.CENTER);
				this.evolvioColor.text(getCreatureName(), (float) (px * scaleUp),
						(float) ((py - getRadius() * 1.4f - 0.07f) * scaleUp));
			}
		}
	}

	public void drawVisionAngles(Board board, float scaleUp) {
		for (int i = 0; i < VISION_ANGLES.length; i++) {
			int visionUIcolor = this.evolvioColor.color(0, 0, 1);
			if (visionResults[i * 3 + 2] > Configuration.BRIGHTNESS_THRESHOLD) {
				visionUIcolor = this.evolvioColor.color(0, 0, 0);
			}
			this.evolvioColor.stroke(visionUIcolor);
			this.evolvioColor.strokeWeight(Configuration.CREATURE_STROKE_WEIGHT);
			float endX = (float) getVisionEndX(i);
			float endY = (float) getVisionEndY(i);
			this.evolvioColor.line((float) (px * scaleUp), (float) (py * scaleUp), endX * scaleUp, endY * scaleUp);
			this.evolvioColor.noStroke();
			this.evolvioColor.fill(visionUIcolor);
			this.evolvioColor.ellipse((float) (visionOccludedX[i] * scaleUp), (float) (visionOccludedY[i] * scaleUp),
					2 * CROSS_SIZE * scaleUp, 2 * CROSS_SIZE * scaleUp);
			this.evolvioColor.stroke((float) (visionResults[i * 3]), (float) (visionResults[i * 3 + 1]),
					(float) (visionResults[i * 3 + 2]));
			this.evolvioColor.strokeWeight(Configuration.CREATURE_STROKE_WEIGHT);
			this.evolvioColor.line((float) ((visionOccludedX[i] - CROSS_SIZE) * scaleUp),
					(float) ((visionOccludedY[i] - CROSS_SIZE) * scaleUp),
					(float) ((visionOccludedX[i] + CROSS_SIZE) * scaleUp),
					(float) ((visionOccludedY[i] + CROSS_SIZE) * scaleUp));
			this.evolvioColor.line((float) ((visionOccludedX[i] - CROSS_SIZE) * scaleUp),
					(float) ((visionOccludedY[i] + CROSS_SIZE) * scaleUp),
					(float) ((visionOccludedX[i] + CROSS_SIZE) * scaleUp),
					(float) ((visionOccludedY[i] - CROSS_SIZE) * scaleUp));
		}
	}

	public void drawMouth(Board board, float scaleUp, double radius, double rotation, float camZoom, double mouthHue) {
		this.evolvioColor.noFill();
		this.evolvioColor.strokeWeight(Configuration.CREATURE_STROKE_WEIGHT);
		this.evolvioColor.stroke(0, 0, 1);
		this.evolvioColor.ellipseMode(EvolvioColor.RADIUS);
		this.evolvioColor.ellipse((float) (px * scaleUp), (float) (py * scaleUp),
				Configuration.MINIMUM_SURVIVABLE_SIZE * scaleUp, Configuration.MINIMUM_SURVIVABLE_SIZE * scaleUp);
		this.evolvioColor.pushMatrix();
		this.evolvioColor.translate((float) (px * scaleUp), (float) (py * scaleUp));
		this.evolvioColor.scale((float) radius);
		this.evolvioColor.rotate((float) rotation);
		this.evolvioColor.strokeWeight((float) (Configuration.CREATURE_STROKE_WEIGHT / radius));
		this.evolvioColor.stroke(0, 0, 0);
		this.evolvioColor.fill((float) mouthHue, 1.0f, 1.0f);
		this.evolvioColor.ellipse(0.6f * scaleUp, 0, 0.37f * scaleUp, 0.37f * scaleUp);
		this.evolvioColor.popMatrix();
	}

	public void metabolize(double timeStep) {
		/*
		 * the older the more work necessary
		 */
		double age = Configuration.AGE_FACTOR * (board.getYear() - birthTime);
		loseEnergy(energy * Configuration.METABOLISM_ENERGY * age * timeStep);

		if (energy < Configuration.SAFE_SIZE) {
			returnToEarth();
			board.removeCreature(this);
		}
	}

	public void accelerate(double amount, double timeStep) {
		double multiplied = amount * timeStep / getMass();
		vx += Math.cos(rotation) * multiplied;
		vy += Math.sin(rotation) * multiplied;
		if (amount >= 0) {
			loseEnergy(amount * Configuration.ACCELERATION_ENERGY * timeStep);
		} else {
			loseEnergy(Math.abs(amount * Configuration.ACCELERATION_BACKWARDS_ENERGY * timeStep));
		}
	}

	public void rotate(double amount, double timeStep) {
		vr += 0.04f * amount * timeStep / getMass();
		loseEnergy(Math.abs(amount * Configuration.TURN_ENERGY * energy * timeStep));
	}

	public Tile getRandomCoveredTile() {
		double radius = (float) getRadius();
		double choiceX = 0;
		double choiceY = 0;
		while (EvolvioColor.dist((float) px, (float) py, (float) choiceX, (float) choiceY) > radius) {
			choiceX = (Math.random() * 2 * radius - radius) + px;
			choiceY = (Math.random() * 2 * radius - radius) + py;
		}
		int x = xBound((int) choiceX);
		int y = yBound((int) choiceY);
		return board.getTile(x, y);
	}

	public void eat(double attemptedAmount, double timeStep) {
		/*
		 * The faster you're moving, the less efficiently you can eat.
		 */
		double amount = attemptedAmount
				/ (1.0f + distance(0, 0, vx, vy) * Configuration.EAT_WHILE_MOVING_INEFFICIENCY_MULTIPLIER);
		if (amount < 0) {
			dropEnergy(-amount * timeStep);
			loseEnergy(-attemptedAmount * Configuration.EAT_ENERGY * timeStep);
		} else {
			Tile coveredTile = getRandomCoveredTile();
			double foodToEat = coveredTile.foodLevel * (1 - Math.pow((1 - Configuration.EAT_SPEED), amount * timeStep));
			if (foodToEat > coveredTile.foodLevel) {
				foodToEat = coveredTile.foodLevel;
			}
			coveredTile.removeFood(foodToEat, true);
			double foodDistance = Math.abs(coveredTile.foodType - mouthHue);
			double multiplier = 1.0f - foodDistance / Configuration.FOOD_SENSITIVITY;
			if (multiplier >= 0) {
				addEnergy(foodToEat * multiplier);
			} else {
				loseEnergy(-foodToEat * multiplier);
			}
			loseEnergy(attemptedAmount * Configuration.EAT_ENERGY * timeStep);
		}
	}

	public void fight(double amount, double timeStep) {
		if (amount > 0 && board.getYear() - birthTime >= Configuration.MATURE_AGE) {
			fightLevel = amount;
			loseEnergy(fightLevel * Configuration.FIGHT_ENERGY * energy * timeStep);
			for (int i = 0; i < colliders.size(); i++) {
				SoftBody collider = colliders.get(i);
				if (collider.isCreature) {
					float distance = EvolvioColor.dist((float) px, (float) py, (float) collider.px,
							(float) collider.py);
					double combinedRadius = getRadius() * FIGHT_RANGE + collider.getRadius();
					if (distance < combinedRadius) {
						((Creature) collider).dropEnergy(fightLevel * Configuration.INJURED_ENERGY * timeStep);
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
		for (int k = 0; k < VISION_ANGLES.length; k++) {
			double visionStartX = px;
			double visionStartY = py;
			double visionTotalAngle = rotation + VISION_ANGLES[k];

			double endX = getVisionEndX(k);
			double endY = getVisionEndY(k);

			visionOccludedX[k] = endX;
			visionOccludedY[k] = endY;
			int c = getColorAt(endX, endY);
			visionResults[k * 3] = this.evolvioColor.hue(c);
			visionResults[k * 3 + 1] = this.evolvioColor.saturation(c);
			visionResults[k * 3 + 2] = this.evolvioColor.brightness(c);

			int tileX = 0;
			int tileY = 0;
			int prevTileX = -1;
			int prevTileY = -1;
			ArrayList<SoftBody> potentialVisionOccluders = new ArrayList<SoftBody>();
			for (int DAvision = 0; DAvision < VISION_DISTANCES[k] + 1; DAvision++) {
				tileX = (int) (visionStartX + Math.cos(visionTotalAngle) * DAvision);
				tileY = (int) (visionStartY + Math.sin(visionTotalAngle) * DAvision);
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
			double visionLineLength = VISION_DISTANCES[k];
			for (int i = 0; i < potentialVisionOccluders.size(); i++) {
				SoftBody body = potentialVisionOccluders.get(i);
				double x = body.px - px;
				double y = body.py - py;
				double r = body.getRadius();
				double translatedX = rotationMatrix[0][0] * x + rotationMatrix[1][0] * y;
				double translatedY = rotationMatrix[0][1] * x + rotationMatrix[1][1] * y;
				if (Math.abs(translatedY) <= r) {
					if ((translatedX >= 0 && translatedX < visionLineLength && translatedY < visionLineLength)
							|| distance(0, 0, translatedX, translatedY) < r
							|| distance(visionLineLength, 0, translatedX, translatedY) < r) {
						// YES! There is an occlussion.
						visionLineLength = translatedX - Math.sqrt(r * r - translatedY * translatedY);
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

	public int getColorAt(double x, double y) {
		if (x >= 0 && x < board.getBoardWidth() && y >= 0 && y < board.getBoardHeight()) {
			return board.getTile((int) (x), (int) (y)).getColor();
		} else {
			return board.getBackgroundColor();
		}
	}

	public double distance(double x1, double y1, double x2, double y2) {
		return (Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));
	}

	public void addPVOs(int x, int y, ArrayList<SoftBody> PVOs) {
		if (x >= 0 && x < board.getBoardWidth() && y >= 0 && y < board.getBoardHeight()) {
			for (int i = 0; i < board.getSoftBodiesInPosition(x, y).size(); i++) {
				SoftBody newCollider = board.getSoftBodiesInPosition(x, y).get(i);
				if (!PVOs.contains(newCollider) && newCollider != this) {
					PVOs.add(newCollider);
				}
			}
		}
	}

	public void returnToEarth() {
		int pieces = 20;
		for (int i = 0; i < pieces; i++) {
			getRandomCoveredTile().addFood(energy / pieces, hue, true);
		}
		for (int x = SBIPMinX; x <= SBIPMaxX; x++) {
			for (int y = SBIPMinY; y <= SBIPMaxY; y++) {
				board.getSoftBodiesInPosition(x, y).remove(this);
			}
		}
		if (board.getSelectedCreature() == this) {
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
				/*
				 * Must be a WILLING creature to also give birth.
				 */
				if (possibleParent.isCreature && ((Creature) possibleParent).brain.outputs()[9] > -1) {
					float distance = EvolvioColor.dist((float) px, (float) py, (float) possibleParent.px,
							(float) possibleParent.py);
					double combinedRadius = getRadius() * FIGHT_RANGE + possibleParent.getRadius();
					if (distance < combinedRadius) {
						parents.add((Creature) possibleParent);
						availableEnergy += ((Creature) possibleParent).getBabyEnergy();
					}
				}
			}
			if (availableEnergy > babySize) {
				/*
				 * To avoid landing directly on parents, resulting in division
				 * by 0)
				 */
				double newPX = this.evolvioColor.random(-0.01f, 0.01f);
				double newPY = this.evolvioColor.random(-0.01f, 0.01f);
				double newHue = 0;
				double newSaturation = 0;
				double newBrightness = 0;
				double newMouthHue = 0;
				int parentsTotal = parents.size();
				String[] parentNames = new String[parentsTotal];
				Brain newBrain = brain.evolve(parents);
				for (int i = 0; i < parentsTotal; i++) {
					int chosenIndex = (int) this.evolvioColor.random(0, parents.size());
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
				board.addCreature(new Creature(this.evolvioColor, newPX, newPY, 0, 0, babySize, density, newHue,
						newSaturation, newBrightness, board, this.evolvioColor.random(0, 2 * EvolvioColor.PI), 0,
						stitchName(parentNames), andifyParents(parentNames), true, newBrain, highestGen + 1,
						newMouthHue));
			}
		}
	}

	public String stitchName(String[] s) {
		String result = "";
		for (int i = 0; i < s.length; i++) {
			float portion = ((float) s[i].length()) / s.length;
			int start = Math.min(Math.max(Math.round(portion * i), 0), s[i].length());
			int end = Math.min(Math.max(Math.round(portion * (i + 1)), 0), s[i].length());
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

	public String getName() {
		return name;
	}

	public String getCreatureName() {
		return capitalize(name);
	}

	public String capitalize(String n) {
		return n.substring(0, 1).toUpperCase() + n.substring(1, n.length());
	}

	@Override
	public void applyMotions(double timeStep) {
		if (getRandomCoveredTile().fertility > 1) {
			loseEnergy(Configuration.SWIM_ENERGY * energy);
		}
		super.applyMotions(timeStep);
		rotation += vr;
		vr *= Math.max(0, 1 - FRICTION / getMass());
	}

	public Brain getBrain() {
		return brain;
	}

	public double getEnergyUsage(double timeStep) {
		return (energy - previousEnergy[Configuration.ENERGY_HISTORY_LENGTH - 1]) / Configuration.ENERGY_HISTORY_LENGTH
				/ timeStep;
	}

	public double getBabyEnergy() {
		return energy - Configuration.SAFE_SIZE;
	}

	public void addEnergy(double amount) {
		energy += amount;
	}

	public void setPreviousEnergy() {
		for (int i = Configuration.ENERGY_HISTORY_LENGTH - 1; i >= 1; i--) {
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

	public float getPreferredRank() {
		return preferredRank;
	}

	public void setPreferredRank(float preferredRank) {
		this.preferredRank = preferredRank;
	}

	public String getParents() {
		return parents;
	}

	public int getGen() {
		return gen;
	}

	public int getId() {
		return id;
	}

	public double getRotation() {
		return rotation;
	}

	public void setHue(double set) {
		hue = Math.min(Math.max(set, 0), 1);
	}

	public double getMouthHue() {
		return mouthHue;
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

	public double getVisionEndX(int i) {
		double visionTotalAngle = rotation + VISION_ANGLES[i];
		return px + VISION_DISTANCES[i] * Math.cos(visionTotalAngle);
	}

	public double getVisionEndY(int i) {
		double visionTotalAngle = rotation + VISION_ANGLES[i];
		return py + VISION_DISTANCES[i] * Math.sin(visionTotalAngle);
	}
}