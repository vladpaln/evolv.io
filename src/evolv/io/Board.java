package evolv.io;

import java.util.ArrayList;

import processing.core.PFont;

class Board {
	/**
	 * 
	 */
	private final EvolvioColor evolvioColor;
	// Board
	int boardWidth;
	int boardHeight;
	Tile[][] tiles;

	// Creature
	int creatureMinimum;
	final float MIN_CREATURE_ENERGY = 1.2f;
	final float MAX_CREATURE_ENERGY = 2.0f;
	final float MINIMUM_SURVIVABLE_SIZE = 0.06f;
	final float CREATURE_STROKE_WEIGHT = 0.6f;
	ArrayList[][] softBodiesInPositions;
	ArrayList<Creature> creatures;
	Creature selectedCreature = null;
	int creatureIDUpTo = 0;
	private int creatureRankMetric = 0;
	final int LIST_SLOTS = 6;
	Creature[] list = new Creature[LIST_SLOTS];
	final int creatureMinimumIncrement = 5;
	double MANUAL_BIRTH_SIZE = 1.2f;
	boolean wasPressingB = false;

	// Time or History
	double year = 0;
	final float OBJECT_TIMESTEPS_PER_YEAR = 100;
	double timeStep;
	int POPULATION_HISTORY_LENGTH = 200;
	int[] populationHistory;
	double recordPopulationEvery = 0.02f;
	int playSpeed = 1;

	// Temperature
	float MIN_TEMPERATURE;
	float MAX_TEMPERATURE;
	final float THERMOMETER_MIN = -2;
	final float THERMOMETER_MAX = 2;
	double temperature;

	// Rocks
	final int ROCKS_TO_ADD;
	final float MIN_ROCK_ENERGY_BASE = 0.8f;
	final float MAX_ROCK_ENERGY_BASE = 1.6f;
	final float ROCK_DENSITY = 5;
	final int ROCK_COLOR;
	ArrayList<SoftBody> rocks;

	// Saving
	int[] fileSaveCounts;
	double[] fileSaveTimes;
	double imageSaveInterval = 1;
	double textSaveInterval = 1;

	// Misc or Unsorted
	final int BACKGROUND_COLOR;
	final static double MAX_DETAILED_ZOOM = 3.5f; // Maximum zoom to draw
													// details at
	int buttonColor;
	String folder = "TEST";
	final double FLASH_SPEED = 80;
	boolean userControl;

	final String[] sorts = { "Biggest", "Smallest", "Youngest", "Oldest", "A to Z", "Z to A", "Highest Gen",
			"Lowest Gen" };

	public Board(EvolvioColor evolvioColor, int w, int h, float stepSize, float min, float max, int rta, int cm,
			int SEED, String INITIAL_FILE_NAME, double ts) {
		ROCK_COLOR = evolvioColor.color(0, 0, 0.5f);
		BACKGROUND_COLOR = evolvioColor.color(0, 0, 0.1f);
		buttonColor = evolvioColor.color(0.82f, 0.8f, 0.7f);
		this.evolvioColor = evolvioColor;
		this.evolvioColor.noiseSeed(SEED);
		this.evolvioColor.randomSeed(SEED);
		boardWidth = w;
		boardHeight = h;
		tiles = new Tile[w][h];
		for (int x = 0; x < boardWidth; x++) {
			for (int y = 0; y < boardHeight; y++) {
				float bigForce = EvolvioColor.pow(((float) y) / boardHeight, 0.5f);
				float fertility = this.evolvioColor.noise(x * stepSize * 3, y * stepSize * 3) * (1 - bigForce) * 5.0f
						+ this.evolvioColor.noise(x * stepSize * 0.5f, y * stepSize * 0.5f) * bigForce * 5.0f - 1.5f;
				float climateType = this.evolvioColor.noise(x * stepSize * 0.2f + 10000, y * stepSize * 0.2f + 10000)
						* 1.63f - 0.4f;
				climateType = EvolvioColor.min(EvolvioColor.max(climateType, 0), 0.8f);
				tiles[x][y] = new Tile(this.evolvioColor, x, y, fertility, climateType, this);
			}
		}
		MIN_TEMPERATURE = min;
		MAX_TEMPERATURE = max;

		softBodiesInPositions = new ArrayList[boardWidth][boardHeight];
		for (int x = 0; x < boardWidth; x++) {
			for (int y = 0; y < boardHeight; y++) {
				softBodiesInPositions[x][y] = new ArrayList<SoftBody>(0);
			}
		}

		ROCKS_TO_ADD = rta;
		rocks = new ArrayList<SoftBody>(0);
		for (int i = 0; i < ROCKS_TO_ADD; i++) {
			rocks.add(new SoftBody(this.evolvioColor, this.evolvioColor.random(0, boardWidth),
					this.evolvioColor.random(0, boardHeight), 0, 0, getRandomSize(), ROCK_DENSITY,
					this.evolvioColor.hue(ROCK_COLOR), this.evolvioColor.saturation(ROCK_COLOR),
					this.evolvioColor.brightness(ROCK_COLOR), this, year));
		}

		creatureMinimum = cm;
		creatures = new ArrayList<Creature>(0);
		maintainCreatureMinimum(false);
		for (int i = 0; i < LIST_SLOTS; i++) {
			list[i] = null;
		}
		folder = INITIAL_FILE_NAME;
		fileSaveCounts = new int[4];
		fileSaveTimes = new double[4];
		for (int i = 0; i < 4; i++) {
			fileSaveCounts[i] = 0;
			fileSaveTimes[i] = -999;
		}
		userControl = true;
		timeStep = ts;
		populationHistory = new int[POPULATION_HISTORY_LENGTH];
		for (int i = 0; i < POPULATION_HISTORY_LENGTH; i++) {
			populationHistory[i] = 0;
		}
	}

	public void drawBoard(float scaleUp, float camZoom, int mX, int mY) {
		for (int x = 0; x < boardWidth; x++) {
			for (int y = 0; y < boardHeight; y++) {
				tiles[x][y].drawTile(scaleUp, camZoom, (mX == x && mY == y));
			}
		}
		for (int i = 0; i < rocks.size(); i++) {
			rocks.get(i).drawSoftBody(scaleUp);
		}
		for (int i = 0; i < creatures.size(); i++) {
			creatures.get(i).drawSoftBody(scaleUp, camZoom, true);
		}
	}

	public void drawBlankBoard(float scaleUp) {
		this.evolvioColor.fill(BACKGROUND_COLOR);
		this.evolvioColor.rect(0, 0, scaleUp * boardWidth, scaleUp * boardHeight);
	}

	public void drawUI(float scaleUp, float camZoom, double timeStep, int x1, int y1, int x2, int y2, PFont font) {
		this.evolvioColor.fill(0, 0, 0);
		this.evolvioColor.noStroke();
		this.evolvioColor.rect(x1, y1, x2 - x1, y2 - y1);

		this.evolvioColor.pushMatrix();
		this.evolvioColor.translate(x1, y1);

		this.evolvioColor.fill(0, 0, 1);
		this.evolvioColor.textAlign(EvolvioColor.RIGHT);
		this.evolvioColor.text(EvolvioColor.nfs(camZoom * 100, 0, 3) + " %", 0, y2 - y1 - 30);
		this.evolvioColor.textAlign(EvolvioColor.LEFT);
		this.evolvioColor.textFont(font, 48);
		String yearText = "Year " + EvolvioColor.nf((float) year, 0, 2);
		this.evolvioColor.text(yearText, 10, 48);
		float seasonTextXCoor = this.evolvioColor.textWidth(yearText) + 50;
		this.evolvioColor.textFont(font, 24);
		this.evolvioColor.text("Population: " + creatures.size(), 10, 80);
		String[] seasons = { "Winter", "Spring", "Summer", "Autumn" };
		this.evolvioColor.text(seasons[(int) (getSeason() * 4)] + "\nSeed: " + this.evolvioColor.SEED, seasonTextXCoor,
				30);

		if (selectedCreature == null) {
			for (int i = 0; i < LIST_SLOTS; i++) {
				list[i] = null;
			}
			for (int i = 0; i < creatures.size(); i++) {
				int lookingAt = 0;
				boolean done = false;
				while (lookingAt < LIST_SLOTS && list[lookingAt] != null & !done) {
					if (creatureRankMetric == 4 && list[lookingAt].name.compareTo(creatures.get(i).name) < 0) {
						lookingAt++;
					} else if (creatureRankMetric == 5 && list[lookingAt].name.compareTo(creatures.get(i).name) >= 0) {
						lookingAt++;
					} else if (list[lookingAt].measure(creatureRankMetric) > creatures.get(i)
							.measure(creatureRankMetric)) {
						lookingAt++;
					} else {
						done = true;
					}
				}

				if (lookingAt < LIST_SLOTS) {
					for (int j = LIST_SLOTS - 1; j >= lookingAt + 1; j--) {
						list[j] = list[j - 1];
					}
					list[lookingAt] = creatures.get(i);
				}
			}
			double maxEnergy = 0;
			for (int i = 0; i < LIST_SLOTS; i++) {
				if (list[i] != null && list[i].energy > maxEnergy) {
					maxEnergy = list[i].energy;
				}
			}
			for (int i = 0; i < LIST_SLOTS; i++) {
				if (list[i] != null) {
					list[i].preferredRank += (i - list[i].preferredRank) * 0.4f;
					float y = y1 + 175 + 70 * list[i].preferredRank;
					drawCreature(list[i], 45, y + 5, 2.3f, scaleUp);
					this.evolvioColor.textFont(font, 24);
					this.evolvioColor.textAlign(EvolvioColor.LEFT);
					this.evolvioColor.noStroke();
					this.evolvioColor.fill(0.333f, 1, 0.4f);
					float multi = (x2 - x1 - 200);
					if (list[i].energy > 0) {
						this.evolvioColor.rect(85, y + 5, (float) (multi * list[i].energy / maxEnergy), 25);
					}
					if (list[i].energy > 1) {
						this.evolvioColor.fill(0.333f, 1, 0.8f);
						this.evolvioColor.rect(85 + (float) (multi / maxEnergy), y + 5,
								(float) (multi * (list[i].energy - 1) / maxEnergy), 25);
					}
					this.evolvioColor.fill(0, 0, 1);
					this.evolvioColor.text(
							list[i].getCreatureName() + " [" + list[i].id + "] (" + toAge(list[i].birthTime) + ")", 90,
							y);
					this.evolvioColor.text("Energy: " + EvolvioColor.nf(100 * (float) (list[i].energy), 0, 2), 90,
							y + 25);
				}
			}
			this.evolvioColor.noStroke();
			this.evolvioColor.fill(buttonColor);
			this.evolvioColor.rect(10, 95, 220, 40);
			this.evolvioColor.rect(240, 95, 220, 40);
			this.evolvioColor.fill(0, 0, 1);
			this.evolvioColor.textAlign(EvolvioColor.CENTER);
			this.evolvioColor.text("Reset zoom", 120, 123);
			this.evolvioColor.text("Sort by: " + sorts[creatureRankMetric], 350, 123);

			this.evolvioColor.textFont(font, 19);
			String[] buttonTexts = { "Brain Control", "Maintain pop. at " + creatureMinimum, "Screenshot now",
					"-   Image every " + EvolvioColor.nf((float) imageSaveInterval, 0, 2) + " years   +",
					"Text file now",
					"-    Text every " + EvolvioColor.nf((float) textSaveInterval, 0, 2) + " years    +",
					"-    Play Speed (" + playSpeed + "x)    +", "This button does nothing" };
			if (userControl) {
				buttonTexts[0] = "Keyboard Control";
			}
			for (int i = 0; i < 8; i++) {
				float x = (i % 2) * 230 + 10;
				float y = EvolvioColor.floor(i / 2) * 50 + 570;
				this.evolvioColor.fill(buttonColor);
				this.evolvioColor.rect(x, y, 220, 40);
				if (i >= 2 && i < 6) {
					double flashAlpha = 1.0f * Math.pow(0.5f, (year - fileSaveTimes[i - 2]) * FLASH_SPEED);
					this.evolvioColor.fill(0, 0, 1, (float) flashAlpha);
					this.evolvioColor.rect(x, y, 220, 40);
				}
				this.evolvioColor.fill(0, 0, 1, 1);
				this.evolvioColor.text(buttonTexts[i], x + 110, y + 17);
				if (i == 0) {
				} else if (i == 1) {
					this.evolvioColor.text(
							"-" + creatureMinimumIncrement + "                    +" + creatureMinimumIncrement,
							x + 110, y + 37);
				} else if (i <= 5) {
					this.evolvioColor.text(getNextFileName(i - 2), x + 110, y + 37);
				}
			}
		} else {
			float energyUsage = (float) selectedCreature.getEnergyUsage(timeStep);
			this.evolvioColor.noStroke();
			if (energyUsage <= 0) {
				this.evolvioColor.fill(0, 1, 0.5f);
			} else {
				this.evolvioColor.fill(0.33f, 1, 0.4f);
			}
			float EUbar = 20 * energyUsage;
			this.evolvioColor.rect(110, 280, EvolvioColor.min(EvolvioColor.max(EUbar, -110), 110), 25);
			if (EUbar < -110) {
				this.evolvioColor.rect(0, 280, 25, (-110 - EUbar) * 20 + 25);
			} else if (EUbar > 110) {
				float h = (EUbar - 110) * 20 + 25;
				this.evolvioColor.rect(185, 280 - h, 25, h);
			}
			this.evolvioColor.fill(0, 0, 1);
			this.evolvioColor.text("Name: " + selectedCreature.getCreatureName(), 10, 225);
			this.evolvioColor.text("Energy: " + EvolvioColor.nf(100 * (float) selectedCreature.energy, 0, 2) + " yums",
					10, 250);
			this.evolvioColor.text("E Change: " + EvolvioColor.nf(100 * energyUsage, 0, 2) + " yums/year", 10, 275);

			this.evolvioColor.text("ID: " + selectedCreature.id, 10, 325);
			this.evolvioColor.text("X: " + EvolvioColor.nf((float) selectedCreature.px, 0, 2), 10, 350);
			this.evolvioColor.text("Y: " + EvolvioColor.nf((float) selectedCreature.py, 0, 2), 10, 375);
			this.evolvioColor.text("Rotation: " + EvolvioColor.nf((float) selectedCreature.rotation, 0, 2), 10, 400);
			this.evolvioColor.text("B-day: " + toDate(selectedCreature.birthTime), 10, 425);
			this.evolvioColor.text("(" + toAge(selectedCreature.birthTime) + ")", 10, 450);
			this.evolvioColor.text("Generation: " + selectedCreature.gen, 10, 475);
			this.evolvioColor.text("Parents: " + selectedCreature.parents, 10, 500, 210, 255);
			this.evolvioColor.text("Hue: " + EvolvioColor.nf((float) (selectedCreature.hue), 0, 2), 10, 550, 210, 255);
			this.evolvioColor.text("Mouth hue: " + EvolvioColor.nf((float) (selectedCreature.mouthHue), 0, 2), 10, 575,
					210, 255);

			if (userControl) {
				this.evolvioColor
						.text("Controls:\nUp/Down: Move\nLeft/Right: Rotate\nSpace: Eat\nF: Fight\nV: Vomit\nU, J: Change color"
								+ "\nI, K: Change mouth color\nB: Give birth (Not possible if under "
								+ Math.round((MANUAL_BIRTH_SIZE + 1) * 100) + " yums)", 10, 625, 250, 400);
			}
			this.evolvioColor.pushMatrix();
			this.evolvioColor.translate(400, 80);
			float apX = EvolvioColor.round((this.evolvioColor.mouseX - 400 - x1) / 46.0f);
			float apY = EvolvioColor.round((this.evolvioColor.mouseY - 80 - y1) / 46.0f);
			selectedCreature.drawBrain(font, 46, (int) apX, (int) apY);
			this.evolvioColor.popMatrix();
		}
		drawPopulationGraph(x1, x2, y1, y2);
		this.evolvioColor.fill(0, 0, 0);
		this.evolvioColor.textAlign(EvolvioColor.RIGHT);
		this.evolvioColor.textFont(font, 24);
		this.evolvioColor.text("Population: " + creatures.size(), x2 - x1 - 10, y2 - y1 - 10);
		this.evolvioColor.popMatrix();

		this.evolvioColor.pushMatrix();
		this.evolvioColor.translate(x2, y1);
		this.evolvioColor.textAlign(EvolvioColor.RIGHT);
		this.evolvioColor.textFont(font, 24);
		this.evolvioColor.text("Temperature", -10, 24);
		drawThermometer(-45, 30, 20, 660, temperature, THERMOMETER_MIN, THERMOMETER_MAX,
				this.evolvioColor.color(0, 1, 1));
		this.evolvioColor.popMatrix();

		if (selectedCreature != null) {
			drawCreature(selectedCreature, x1 + 65, y1 + 147, 2.3f, scaleUp);
		}
	}

	public void drawPopulationGraph(float x1, float x2, float y1, float y2) {
		float barWidth = (x2 - x1) / ((POPULATION_HISTORY_LENGTH));
		this.evolvioColor.noStroke();
		this.evolvioColor.fill(0.33333f, 1, 0.6f);
		int maxPopulation = 0;
		for (int i = 0; i < POPULATION_HISTORY_LENGTH; i++) {
			if (populationHistory[i] > maxPopulation) {
				maxPopulation = populationHistory[i];
			}
		}
		for (int i = 0; i < POPULATION_HISTORY_LENGTH; i++) {
			float h = (((float) populationHistory[i]) / maxPopulation) * (y2 - 770);
			this.evolvioColor.rect((POPULATION_HISTORY_LENGTH - 1 - i) * barWidth, y2 - h, barWidth, h);
		}
	}

	public String getNextFileName(int type) {
		String[] modes = { "manualImgs", "autoImgs", "manualTexts", "autoTexts" };
		String ending = ".png";
		if (type >= 2) {
			ending = ".txt";
		}
		return folder + "/" + modes[type] + "/" + EvolvioColor.nf(fileSaveCounts[type], 5) + ending;
	}

	public void iterate(double timeStep) {
		double prevYear = year;
		year += timeStep;
		if (Math.floor(year / recordPopulationEvery) != Math.floor(prevYear / recordPopulationEvery)) {
			for (int i = POPULATION_HISTORY_LENGTH - 1; i >= 1; i--) {
				populationHistory[i] = populationHistory[i - 1];
			}
			populationHistory[0] = creatures.size();
		}
		temperature = getGrowthRate(getSeason());
		double tempChangeIntoThisFrame = temperature - getGrowthRate(getSeason() - timeStep);
		double tempChangeOutOfThisFrame = getGrowthRate(getSeason() + timeStep) - temperature;
		if (tempChangeIntoThisFrame * tempChangeOutOfThisFrame <= 0) { // Temperature
																		// change
																		// flipped
																		// direction.
			for (int x = 0; x < boardWidth; x++) {
				for (int y = 0; y < boardHeight; y++) {
					tiles[x][y].iterate();
				}
			}
		}
		/*
		 * for(int x = 0; x < boardWidth; x++) { for(int y = 0; y < boardHeight;
		 * y++) { tiles[x][y].iterate(this, year); } }
		 */
		for (int i = 0; i < creatures.size(); i++) {
			creatures.get(i).setPreviousEnergy();
		}
		/*
		 * for(int i = 0; i < rocks.size(); i++) {
		 * rocks.get(i).collide(timeStep*OBJECT_TIMESTEPS_PER_YEAR); }
		 */
		maintainCreatureMinimum(false);
		for (int i = 0; i < creatures.size(); i++) {
			Creature me = creatures.get(i);
			me.collide(timeStep);
			me.metabolize(timeStep);
			me.useBrain(timeStep, !userControl);
			if (userControl) {
				if (me == selectedCreature) {
					if (this.evolvioColor.keyPressed) {
						if (this.evolvioColor.key == EvolvioColor.CODED) {
							if (this.evolvioColor.keyCode == EvolvioColor.UP)
								me.accelerate(0.04f, timeStep * OBJECT_TIMESTEPS_PER_YEAR);
							if (this.evolvioColor.keyCode == EvolvioColor.DOWN)
								me.accelerate(-0.04f, timeStep * OBJECT_TIMESTEPS_PER_YEAR);
							if (this.evolvioColor.keyCode == EvolvioColor.LEFT)
								me.turn(-0.1f, timeStep * OBJECT_TIMESTEPS_PER_YEAR);
							if (this.evolvioColor.keyCode == EvolvioColor.RIGHT)
								me.turn(0.1f, timeStep * OBJECT_TIMESTEPS_PER_YEAR);
						} else {
							if (this.evolvioColor.key == ' ')
								me.eat(0.1f, timeStep * OBJECT_TIMESTEPS_PER_YEAR);
							if (this.evolvioColor.key == 'v' || this.evolvioColor.key == 'V')
								me.eat(-0.1f, timeStep * OBJECT_TIMESTEPS_PER_YEAR);
							if (this.evolvioColor.key == 'f' || this.evolvioColor.key == 'F')
								me.fight(0.5f, timeStep * OBJECT_TIMESTEPS_PER_YEAR);
							if (this.evolvioColor.key == 'u' || this.evolvioColor.key == 'U')
								me.setHue(me.hue + 0.02f);
							if (this.evolvioColor.key == 'j' || this.evolvioColor.key == 'J')
								me.setHue(me.hue - 0.02f);

							if (this.evolvioColor.key == 'i' || this.evolvioColor.key == 'I')
								me.setMouthHue(me.mouthHue + 0.02f);
							if (this.evolvioColor.key == 'k' || this.evolvioColor.key == 'K')
								me.setMouthHue(me.mouthHue - 0.02f);
							if (this.evolvioColor.key == 'b' || this.evolvioColor.key == 'B') {
								if (!wasPressingB) {
									me.reproduce(MANUAL_BIRTH_SIZE, timeStep);
								}
								wasPressingB = true;
							} else {
								wasPressingB = false;
							}
						}
					}
				}
			}
		}
		finishIterate(timeStep);
	}

	public void finishIterate(double timeStep) {
		for (int i = 0; i < rocks.size(); i++) {
			rocks.get(i).applyMotions(timeStep * OBJECT_TIMESTEPS_PER_YEAR);
		}
		for (int i = 0; i < creatures.size(); i++) {
			creatures.get(i).applyMotions(timeStep * OBJECT_TIMESTEPS_PER_YEAR);
			creatures.get(i).see(timeStep * OBJECT_TIMESTEPS_PER_YEAR);
		}
		if (Math.floor(fileSaveTimes[1] / imageSaveInterval) != Math.floor(year / imageSaveInterval)) {
			prepareForFileSave(1);
		}
		if (Math.floor(fileSaveTimes[3] / textSaveInterval) != Math.floor(year / textSaveInterval)) {
			prepareForFileSave(3);
		}
	}

	private double getGrowthRate(double theTime) {
		double temperatureRange = MAX_TEMPERATURE - MIN_TEMPERATURE;
		return MIN_TEMPERATURE + temperatureRange * 0.5f - temperatureRange * 0.5f * Math.cos(theTime * 2 * Math.PI);
	}

	double getGrowthOverTimeRange(double startTime, double endTime) {
		double temperatureRange = MAX_TEMPERATURE - MIN_TEMPERATURE;
		double m = MIN_TEMPERATURE + temperatureRange * 0.5f;
		return (endTime - startTime) * m + (temperatureRange / Math.PI / 4.0f)
				* (Math.sin(2 * Math.PI * startTime) - Math.sin(2 * Math.PI * endTime));
	}

	private double getSeason() {
		return (year % 1.0f);
	}

	private void drawThermometer(float x1, float y1, float w, float h, double prog, double min, double max,
			int fillColor) {
		this.evolvioColor.noStroke();
		this.evolvioColor.fill(0, 0, 0.2f);
		this.evolvioColor.rect(x1, y1, w, h);
		this.evolvioColor.fill(fillColor);
		double proportionFilled = (prog - min) / (max - min);
		this.evolvioColor.rect(x1, (float) (y1 + h * (1 - proportionFilled)), w, (float) (proportionFilled * h));

		double zeroHeight = (0 - min) / (max - min);
		double zeroLineY = y1 + h * (1 - zeroHeight);
		this.evolvioColor.textAlign(EvolvioColor.RIGHT);
		this.evolvioColor.stroke(0, 0, 1);
		this.evolvioColor.strokeWeight(3);
		this.evolvioColor.line(x1, (float) (zeroLineY), x1 + w, (float) (zeroLineY));
		double minY = y1 + h * (1 - (MIN_TEMPERATURE - min) / (max - min));
		double maxY = y1 + h * (1 - (MAX_TEMPERATURE - min) / (max - min));
		this.evolvioColor.fill(0, 0, 0.8f);
		this.evolvioColor.line(x1, (float) (minY), x1 + w * 1.8f, (float) (minY));
		this.evolvioColor.line(x1, (float) (maxY), x1 + w * 1.8f, (float) (maxY));
		this.evolvioColor.line(x1 + w * 1.8f, (float) (minY), x1 + w * 1.8f, (float) (maxY));

		this.evolvioColor.fill(0, 0, 1);
		this.evolvioColor.text("Zero", x1 - 5, (float) (zeroLineY + 8));
		this.evolvioColor.text(EvolvioColor.nf(MIN_TEMPERATURE, 0, 2), x1 - 5, (float) (minY + 8));
		this.evolvioColor.text(EvolvioColor.nf(MAX_TEMPERATURE, 0, 2), x1 - 5, (float) (maxY + 8));
	}

	private void drawVerticalSlider(float x1, float y1, float w, float h, double prog, int fillColor, int antiColor) {
		this.evolvioColor.noStroke();
		this.evolvioColor.fill(0, 0, 0.2f);
		this.evolvioColor.rect(x1, y1, w, h);
		if (prog >= 0) {
			this.evolvioColor.fill(fillColor);
		} else {
			this.evolvioColor.fill(antiColor);
		}
		this.evolvioColor.rect(x1, (float) (y1 + h * (1 - prog)), w, (float) (prog * h));
	}

	boolean setMinTemperature(float temp) {
		MIN_TEMPERATURE = tempBounds(THERMOMETER_MIN + temp * (THERMOMETER_MAX - THERMOMETER_MIN));
		if (MIN_TEMPERATURE > MAX_TEMPERATURE) {
			float placeHolder = MAX_TEMPERATURE;
			MAX_TEMPERATURE = MIN_TEMPERATURE;
			MIN_TEMPERATURE = placeHolder;
			return true;
		}
		return false;
	}

	boolean setMaxTemperature(float temp) {
		MAX_TEMPERATURE = tempBounds(THERMOMETER_MIN + temp * (THERMOMETER_MAX - THERMOMETER_MIN));
		if (MIN_TEMPERATURE > MAX_TEMPERATURE) {
			float placeHolder = MAX_TEMPERATURE;
			MAX_TEMPERATURE = MIN_TEMPERATURE;
			MIN_TEMPERATURE = placeHolder;
			return true;
		}
		return false;
	}

	private float tempBounds(float temp) {
		return EvolvioColor.min(EvolvioColor.max(temp, THERMOMETER_MIN), THERMOMETER_MAX);
	}

	float getHighTempProportion() {
		return (MAX_TEMPERATURE - THERMOMETER_MIN) / (THERMOMETER_MAX - THERMOMETER_MIN);
	}

	float getLowTempProportion() {
		return (MIN_TEMPERATURE - THERMOMETER_MIN) / (THERMOMETER_MAX - THERMOMETER_MIN);
	}

	private String toDate(double d) {
		return "Year " + EvolvioColor.nf((float) (d), 0, 2);
	}

	private String toAge(double d) {
		return EvolvioColor.nf((float) (year - d), 0, 2) + " yrs old";
	}

	private void maintainCreatureMinimum(boolean choosePreexisting) {
		while (creatures.size() < creatureMinimum) {
			if (choosePreexisting) {
				Creature c = getRandomCreature();
				c.addEnergy(c.SAFE_SIZE);
				c.reproduce(c.SAFE_SIZE, timeStep);
			} else {
				creatures.add(new Creature(this.evolvioColor, this.evolvioColor.random(0, boardWidth),
						this.evolvioColor.random(0, boardHeight), 0, 0,
						this.evolvioColor.random(MIN_CREATURE_ENERGY, MAX_CREATURE_ENERGY), 1,
						this.evolvioColor.random(0, 1), 1, 1, this, year,
						this.evolvioColor.random(0, 2 * EvolvioColor.PI), 0, "", "[PRIMORDIAL]", true, null, 1,
						this.evolvioColor.random(0, 1)));
			}
		}
	}

	private Creature getRandomCreature() {
		int index = (int) (this.evolvioColor.random(0, creatures.size()));
		return creatures.get(index);
	}

	private double getRandomSize() {
		return EvolvioColor.pow(this.evolvioColor.random(MIN_ROCK_ENERGY_BASE, MAX_ROCK_ENERGY_BASE), 4);
	}

	private void drawCreature(Creature c, float x, float y, float scale, float scaleUp) {
		this.evolvioColor.pushMatrix();
		float scaleIconUp = scaleUp * scale;
		this.evolvioColor.translate((float) (-c.px * scaleIconUp), (float) (-c.py * scaleIconUp));
		this.evolvioColor.translate(x, y);
		c.drawSoftBody(scaleIconUp, 40.0f / scale, false);
		this.evolvioColor.popMatrix();
	}

	void prepareForFileSave(int type) {
		fileSaveTimes[type] = -999999;
	}

	void fileSave() {
		for (int i = 0; i < 4; i++) {
			if (fileSaveTimes[i] < -99999) {
				fileSaveTimes[i] = year;
				if (i < 2) {
					this.evolvioColor.saveFrame(getNextFileName(i));
				} else {
					String[] data = this.toBigString();
					this.evolvioColor.saveStrings(getNextFileName(i), data);
				}
				fileSaveCounts[i]++;
			}
		}
	}

	public void incrementSort() {
		this.evolvioColor.evoBoard.creatureRankMetric = (this.evolvioColor.evoBoard.creatureRankMetric + 1)
				% sorts.length;
	}

	public void decrementSort() {
		this.evolvioColor.evoBoard.creatureRankMetric = (this.evolvioColor.evoBoard.creatureRankMetric + sorts.length
				- 1) % sorts.length;
	}

	public String[] toBigString() { // Convert current evolvio board into
									// string. Does not work
		String[] placeholder = { "Goo goo", "Ga ga" };
		return placeholder;
	}

	public void unselect() {
		selectedCreature = null;
	}
}