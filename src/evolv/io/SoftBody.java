package evolv.io;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class SoftBody {
	private static final float ENERGY_DENSITY = 1.0f
			/ (Configuration.MINIMUM_SURVIVABLE_SIZE * Configuration.MINIMUM_SURVIVABLE_SIZE * EvolvioColor.PI);

	private final EvolvioColor evolvioColor;
	private final Board board;
	private final double birthTime;
	/*
	 * Set so when a creature is of minimum size, it equals one.
	 */
	private final double density;
	private final List<SoftBody> colliders = new ArrayList<SoftBody>(0);

	private double px;
	private double py;
	private double vx;
	private double vy;
	private double energy;
	private double hue;
	private double saturation;
	private double brightness;
	private double fightLevel;

	private int prevSBIPMinX;
	private int prevSBIPMinY;
	private int prevSBIPMaxX;
	private int prevSBIPMaxY;
	private int SBIPMinX;
	private int SBIPMinY;
	private int SBIPMaxX;
	private int SBIPMaxY;

	public SoftBody(EvolvioColor evolvioColor, Board tb, double tpx, double tpy, double tvx, double tvy, double tenergy,
			double tdensity, double thue, double tsaturation, double tbrightness) {
		this.evolvioColor = evolvioColor;
		px = tpx;
		py = tpy;
		vx = tvx;
		vy = tvy;
		energy = tenergy;
		density = tdensity;
		hue = thue;
		saturation = tsaturation;
		brightness = tbrightness;
		board = tb;
		setSBIP(false);
		setSBIP(false); // Just to set previous SBIPs as well.
		birthTime = tb.getYear();
	}

	public void setSBIP(boolean shouldRemove) {
		double radius = getRadius() * Configuration.FIGHT_RANGE;
		prevSBIPMinX = SBIPMinX;
		prevSBIPMinY = SBIPMinY;
		prevSBIPMaxX = SBIPMaxX;
		prevSBIPMaxY = SBIPMaxY;
		SBIPMinX = xBound((int) (Math.floor(px - radius)));
		SBIPMinY = yBound((int) (Math.floor(py - radius)));
		SBIPMaxX = xBound((int) (Math.floor(px + radius)));
		SBIPMaxY = yBound((int) (Math.floor(py + radius)));
		if (prevSBIPMinX != SBIPMinX || prevSBIPMinY != SBIPMinY || prevSBIPMaxX != SBIPMaxX
				|| prevSBIPMaxY != SBIPMaxY) {
			if (shouldRemove) {
				for (int x = prevSBIPMinX; x <= prevSBIPMaxX; x++) {
					for (int y = prevSBIPMinY; y <= prevSBIPMaxY; y++) {
						if (x < SBIPMinX || x > SBIPMaxX || y < SBIPMinY || y > SBIPMaxY) {
							board.getSoftBodiesInPosition(x, y).remove(this);
						}
					}
				}
			}
			for (int x = SBIPMinX; x <= SBIPMaxX; x++) {
				for (int y = SBIPMinY; y <= SBIPMaxY; y++) {
					if (x < prevSBIPMinX || x > prevSBIPMaxX || y < prevSBIPMinY || y > prevSBIPMaxY) {
						board.getSoftBodiesInPosition(x, y).add(this);
					}
				}
			}
		}
	}

	public int xBound(int x) {
		return Math.min(Math.max(x, 0), Configuration.BOARD_WIDTH - 1);
	}

	public int yBound(int y) {
		return Math.min(Math.max(y, 0), board.getBoardHeight() - 1);
	}

	public double xBodyBound(double x) {
		double radius = getRadius();
		return Math.min(Math.max(x, radius), Configuration.BOARD_WIDTH - radius);
	}

	public double yBodyBound(double y) {
		double radius = getRadius();
		return Math.min(Math.max(y, radius), board.getBoardHeight() - radius);
	}

	public void collide(double timeStep) {
		colliders.clear();
		for (int x = SBIPMinX; x <= SBIPMaxX; x++) {
			for (int y = SBIPMinY; y <= SBIPMaxY; y++) {
				for (int i = 0; i < board.getSoftBodiesInPosition(x, y).size(); i++) {
					SoftBody newCollider = board.getSoftBodiesInPosition(x, y).get(i);
					if (!colliders.contains(newCollider) && newCollider != this) {
						colliders.add(newCollider);
					}
				}
			}
		}
		for (int i = 0; i < colliders.size(); i++) {
			SoftBody collider = colliders.get(i);
			float distance = EvolvioColor.dist((float) px, (float) py, (float) collider.px, (float) collider.py);
			double combinedRadius = getRadius() + collider.getRadius();
			if (distance < combinedRadius) {
				double force = combinedRadius * Configuration.COLLISION_FORCE;
				vx += ((px - collider.px) / distance) * force / getMass();
				vy += ((py - collider.py) / distance) * force / getMass();
			}
		}
		fightLevel = 0;
	}

	public void applyMotions(double timeStep) {
		px = xBodyBound(px + vx * timeStep);
		py = yBodyBound(py + vy * timeStep);
		vx *= Math.max(0, 1 - Configuration.FRICTION / getMass());
		vy *= Math.max(0, 1 - Configuration.FRICTION / getMass());
		setSBIP(true);
	}

	public void drawSoftBody(float scaleUp) {
		double radius = getRadius();
		this.evolvioColor.stroke(0);
		this.evolvioColor.strokeWeight(Configuration.CREATURE_STROKE_WEIGHT);
		this.evolvioColor.fill((float) hue, (float) saturation, (float) brightness);
		this.evolvioColor.ellipseMode(EvolvioColor.RADIUS);
		this.evolvioColor.ellipse((float) (px * scaleUp), (float) (py * scaleUp), (float) (radius * scaleUp),
				(float) (radius * scaleUp));
	}

	public Board getBoard() {
		return board;
	}

	public double getRadius() {
		if (energy <= 0) {
			return 0;
		} else {
			return Math.sqrt(energy / ENERGY_DENSITY / Math.PI);
		}
	}

	public double getMass() {
		return energy / ENERGY_DENSITY * density;
	}

	public double getDensity() {
		return density;
	}

	public List<SoftBody> getColliders() {
		return colliders;
	}

	public double getPx() {
		return px;
	}

	public double getPy() {
		return py;
	}
	
	public Point2D getPoint2D() {
		return new Point2D.Double(px, py);
	}

	public double getVx() {
		return vx;
	}

	public double getVy() {
		return vy;
	}

	public void setVx(double vx) {
		this.vx = vx;
	}

	public void setVy(double vy) {
		this.vy = vy;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getBirthTime() {
		return birthTime;
	}

	public double getAge() {
		return getBoard().getYear() - getBirthTime();
	}

	public double getHue() {
		return hue;
	}

	public double getSaturation() {
		return saturation;
	}

	public double getBrightness() {
		return brightness;
	}

	public void setHue(double hue) {
		this.hue = Math.min(Math.max(hue, 0), 1);
	}

	public void setSaturation(double saturation) {
		this.saturation = Math.min(Math.max(saturation, 0), 1);
	}

	public void setBrightness(double brightness) {
		this.brightness = Math.min(Math.max(brightness, 0), 1);
	}

	public double getFightLevel() {
		return fightLevel;
	}

	public void setFightLevel(double fightLevel) {
		this.fightLevel = fightLevel;
	}

	public int getSBIPMinX() {
		return SBIPMinX;
	}

	public int getSBIPMaxX() {
		return SBIPMaxX;
	}

	public int getSBIPMinY() {
		return SBIPMinY;
	}

	public int getSBIPMaxY() {
		return SBIPMaxY;
	}
}
