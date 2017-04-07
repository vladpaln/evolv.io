package evolv.io;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Eye {

	private static final float CROSS_SIZE = 0.022f;

	private final Creature creature;
	private final EvolvioColor evolvioColor;
	final double angle;
	final double distance;

	private final List<SoftBody> potentialVisionOccluders = new ArrayList<SoftBody>();
	private double visionOccludedX;
	private double visionOccludedY;

	private final EyeResult eyeResult;

	public class EyeResult {
		public double hue;
		public double saturation;
		public double brightness;
	}

	public Eye(EvolvioColor evolvioColor, Creature creature, double angle, double distance) {
		this.creature = creature;
		this.evolvioColor = evolvioColor;
		this.angle = angle;
		this.distance = distance;

		eyeResult = new EyeResult();
	}

	public void see() {
		
		Point2D visionStart = creature.getPoint2D();
		double visionTotalAngle = creature.getRotation() + angle;

		double endX = getVisionEndX();
		double endY = getVisionEndY();

		visionOccludedX = endX;
		visionOccludedY = endY;
		int c = creature.getBoard().getColorAt(endX, endY);
		eyeResult.hue = evolvioColor.hue(c);
		eyeResult.saturation = evolvioColor.saturation(c);
		eyeResult.brightness = evolvioColor.brightness(c);

		getPVOs(visionStart, visionTotalAngle);

		double[][] rotationMatrix = new double[2][2];
		rotationMatrix[1][1] = rotationMatrix[0][0] = Math.cos(-visionTotalAngle);
		rotationMatrix[0][1] = Math.sin(-visionTotalAngle);
		rotationMatrix[1][0] = -rotationMatrix[0][1];
		double visionLineLength = distance;
		for (SoftBody body : potentialVisionOccluders) {
			double x = body.getPx() - creature.getPx();
			double y = body.getPy() - creature.getPy();
			double r = body.getRadius();
			double translatedX = rotationMatrix[0][0] * x + rotationMatrix[1][0] * y;
			double translatedY = rotationMatrix[0][1] * x + rotationMatrix[1][1] * y;
			if (Math.abs(translatedY) <= r) {
				if ((translatedX >= 0 && translatedX < visionLineLength && translatedY < visionLineLength)
						|| distance(0, 0, translatedX, translatedY) < r
						|| distance(visionLineLength, 0, translatedX, translatedY) < r) {
					// YES! There is an occlussion.
					visionLineLength = translatedX - Math.sqrt(r * r - translatedY * translatedY);
					visionOccludedX = visionStart.getX() + visionLineLength * Math.cos(visionTotalAngle);
					visionOccludedY = visionStart.getY() + visionLineLength * Math.sin(visionTotalAngle);
					eyeResult.hue = body.getHue();
					eyeResult.saturation = body.getSaturation();
					eyeResult.brightness = body.getBrightness();
				}
			}
		}
	}

	public void drawVisionAngle(float scaleUp) {
		int visionUIcolor = this.evolvioColor.color(0, 0, 1);
		if (getEyeResult().brightness > Configuration.BRIGHTNESS_THRESHOLD) {
			visionUIcolor = this.evolvioColor.color(0, 0, 0);
		}
		this.evolvioColor.stroke(visionUIcolor);
		this.evolvioColor.strokeWeight(Configuration.CREATURE_STROKE_WEIGHT);
		float endX = (float) getVisionEndX();
		float endY = (float) getVisionEndY();
		this.evolvioColor.line((float) (creature.getPx() * scaleUp), (float) (creature.getPy() * scaleUp),
				endX * scaleUp, endY * scaleUp);
		this.evolvioColor.noStroke();
		this.evolvioColor.fill(visionUIcolor);
		this.evolvioColor.ellipse((float) (visionOccludedX * scaleUp), (float) (visionOccludedY * scaleUp),
				2 * CROSS_SIZE * scaleUp, 2 * CROSS_SIZE * scaleUp);
		this.evolvioColor.stroke((float) (getEyeResult().hue), (float) (getEyeResult().saturation),
				(float) (getEyeResult().brightness));
		this.evolvioColor.strokeWeight(Configuration.CREATURE_STROKE_WEIGHT);
		this.evolvioColor.line((float) ((visionOccludedX - CROSS_SIZE) * scaleUp),
				(float) ((visionOccludedY - CROSS_SIZE) * scaleUp), (float) ((visionOccludedX + CROSS_SIZE) * scaleUp),
				(float) ((visionOccludedY + CROSS_SIZE) * scaleUp));
		this.evolvioColor.line((float) ((visionOccludedX - CROSS_SIZE) * scaleUp),
				(float) ((visionOccludedY + CROSS_SIZE) * scaleUp), (float) ((visionOccludedX + CROSS_SIZE) * scaleUp),
				(float) ((visionOccludedY - CROSS_SIZE) * scaleUp));
	}

	private double distance(double x1, double y1, double x2, double y2) {
		return (Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));
	}
	
	private void getPVOs(Point2D visionStart, double visionTotalAngle) {
		potentialVisionOccluders.clear();
		
		int tileX = 0;
		int tileY = 0;
		int prevTileX = -1;
		int prevTileY = -1;
		
		for (int DAvision = 0; DAvision < distance + 1; DAvision++) {
			tileX = (int) (visionStart.getX() + Math.cos(visionTotalAngle) * DAvision);
			tileY = (int) (visionStart.getY() + Math.sin(visionTotalAngle) * DAvision);
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
	}

	private void addPVOs(int x, int y, List<SoftBody> PVOs) {
		if (x >= 0 && x < Configuration.BOARD_WIDTH && y >= 0 && y < creature.getBoard().getBoardHeight()) {
			for (SoftBody newCollider : creature.getBoard().getSoftBodiesInPosition(x, y)) {
				if (!PVOs.contains(newCollider) && newCollider != creature) {
					PVOs.add(newCollider);
				}
			}
		}
	}

	private double getVisionEndX() {
		double visionTotalAngle = creature.getRotation() + angle;
		return creature.getPx() + distance * Math.cos(visionTotalAngle);
	}

	private double getVisionEndY() {
		double visionTotalAngle = creature.getRotation() + angle;
		return creature.getPy() + distance * Math.sin(visionTotalAngle);
	}

	public EyeResult getEyeResult() {
		return eyeResult;
	}
}
