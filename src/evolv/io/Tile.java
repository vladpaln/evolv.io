package evolv.io;

class Tile {
	/**
	 * 
	 */
	private final EvolvioColor evolvioColor;
	public final int barrenColor;
	public final int fertileColor;
	public final int blackColor;
	public final int waterColor;
	public final float FOOD_GROWTH_RATE = 1.0f;

	double fertility;
	double foodLevel;
	private final float maxGrowthLevel = 3.0f;
	private int posX;
	private int posY;
	private double lastUpdateTime = 0;

	public double climateType;
	public double foodType;

	Board board;

	public Tile(EvolvioColor evolvioColor, int x, int y, double f, float type, Board b) {
		barrenColor = evolvioColor.color(0, 0, 1);
		fertileColor = evolvioColor.color(0, 0, 0.2f);
		blackColor = evolvioColor.color(0, 1, 0);
		waterColor = evolvioColor.color(0, 0, 0);
		this.evolvioColor = evolvioColor;
		posX = x;
		posY = y;
		fertility = Math.max(0, f);
		foodLevel = fertility;
		climateType = foodType = type;
		board = b;
	}

	public double getFertility() {
		return fertility;
	}

	public double getFoodLevel() {
		return foodLevel;
	}

	public void setFertility(double f) {
		fertility = f;
	}

	public void setFoodLevel(double f) {
		foodLevel = f;
	}

	public void drawTile(float scaleUp, float camZoom, boolean showEnergy) {
		this.evolvioColor.stroke(0, 0, 0, 1);
		this.evolvioColor.strokeWeight(2);
		int landColor = getColor();
		this.evolvioColor.fill(landColor);
		this.evolvioColor.rect(posX * scaleUp, posY * scaleUp, scaleUp, scaleUp);
		if (showEnergy && camZoom > Board.MAX_DETAILED_ZOOM) {
			if (this.evolvioColor.brightness(landColor) >= 0.7f) {
				this.evolvioColor.fill(0, 0, 0, 1);
			} else {
				this.evolvioColor.fill(0, 0, 1, 1);
			}
			this.evolvioColor.textAlign(EvolvioColor.CENTER);
			this.evolvioColor.textFont(this.evolvioColor.font, 21);
			this.evolvioColor.text(EvolvioColor.nf((float) (100 * foodLevel), 0, 2) + " yums", (posX + 0.5f) * scaleUp,
					(posY + 0.3f) * scaleUp);
			this.evolvioColor.text("Clim: " + EvolvioColor.nf((float) (climateType), 0, 2), (posX + 0.5f) * scaleUp,
					(posY + 0.6f) * scaleUp);
			this.evolvioColor.text("Food: " + EvolvioColor.nf((float) (foodType), 0, 2), (posX + 0.5f) * scaleUp,
					(posY + 0.9f) * scaleUp);
		}
	}

	public void iterate() {
		double updateTime = board.year;
		if (Math.abs(lastUpdateTime - updateTime) >= 0.00001f) {
			double growthChange = board.getGrowthOverTimeRange(lastUpdateTime, updateTime);
			if (fertility > 1) { // This means the tile is water.
				foodLevel = 0;
			} else {
				if (growthChange > 0) { // Food is growing. Exponentially
										// approach maxGrowthLevel.
					if (foodLevel < maxGrowthLevel) {
						double newDistToMax = (maxGrowthLevel - foodLevel)
								* Math.pow(2.71828182846f, -growthChange * fertility * FOOD_GROWTH_RATE);
						double foodGrowthAmount = (maxGrowthLevel - newDistToMax) - foodLevel;
						addFood(foodGrowthAmount, climateType, false);
					}
				} else { // Food is dying off. Exponentially approach 0.
					removeFood(foodLevel - foodLevel * Math.pow(2.71828182846f, growthChange * FOOD_GROWTH_RATE),
							false);
				}
				/*
				 * if (growableTime > 0) { if (foodLevel < maxGrowthLevel) {
				 * double foodGrowthAmount = (maxGrowthLevel - foodLevel) *
				 * fertility * FOOD_GROWTH_RATE * timeStep * growableTime;
				 * addFood(foodGrowthAmount, climateType); } } else { foodLevel
				 * += maxGrowthLevel * foodLevel * FOOD_GROWTH_RATE * timeStep *
				 * growableTime; }
				 */
			}
			foodLevel = Math.max(foodLevel, 0);
			lastUpdateTime = updateTime;
		}
	}

	public void addFood(double amount, double addedFoodType, boolean canCauseIteration) {
		if (canCauseIteration) {
			iterate();
		}
		foodLevel += amount;
		/*
		 * if (foodLevel > 0) { foodType += (addedFoodType - foodType) * (amount
		 * / foodLevel); // We're adding new plant growth, so we gotta "mix" the
		 * colors of the tile. }
		 */
	}

	public void removeFood(double amount, boolean canCauseIteration) {
		if (canCauseIteration) {
			iterate();
		}
		foodLevel -= amount;
	}

	public int getColor() {
		iterate();
		int foodColor = this.evolvioColor.color((float) (foodType), 1, 1);
		if (fertility > 1) {
			return waterColor;
		} else if (foodLevel < maxGrowthLevel) {
			return interColorFixedHue(interColor(barrenColor, fertileColor, fertility), foodColor,
					foodLevel / maxGrowthLevel, this.evolvioColor.hue(foodColor));
		} else {
			return interColorFixedHue(foodColor, blackColor, 1.0f - maxGrowthLevel / foodLevel,
					this.evolvioColor.hue(foodColor));
		}
	}

	public int interColor(int a, int b, double x) {
		double hue = inter(this.evolvioColor.hue(a), this.evolvioColor.hue(b), x);
		double sat = inter(this.evolvioColor.saturation(a), this.evolvioColor.saturation(b), x);
		double bri = inter(this.evolvioColor.brightness(a), this.evolvioColor.brightness(b), x); // I
																									// know
																									// it's
		// dumb to
		// do
		// interpolation
		// with HSL
		// but oh
		// well
		return this.evolvioColor.color((float) (hue), (float) (sat), (float) (bri));
	}

	public int interColorFixedHue(int a, int b, double x, double hue) {
		double satB = this.evolvioColor.saturation(b);
		if (this.evolvioColor.brightness(b) == 0) { // I want black to be
													// calculated as 100%
			// saturation
			satB = 1;
		}
		double sat = inter(this.evolvioColor.saturation(a), satB, x);
		double bri = inter(this.evolvioColor.brightness(a), this.evolvioColor.brightness(b), x); // I
																									// know
																									// it's
		// dumb to
		// do
		// interpolation
		// with HSL
		// but oh
		// well
		return this.evolvioColor.color((float) (hue), (float) (sat), (float) (bri));
	}

	public double inter(double a, double b, double x) {
		return a + (b - a) * x;
	}
}