package evolv.io;

import processing.core.PApplet;
import processing.core.PFont;
import processing.event.MouseEvent;

public class EvolvioColor extends PApplet {

	Board evoBoard;
	final int SEED = parseInt(random(1000000));
	final float NOISE_STEP_SIZE = Configuration.NOISE_STEP_SIZE;
	final int BOARD_WIDTH = Configuration.BOARD_WIDTH;
	final int BOARD_HEIGHT = Configuration.BOARD_HEIGHT;

	final float SCALE_TO_FIX_BUG = Configuration.SCALE_TO_FIXBUG;

	final double TIME_STEP = Configuration.TIME_STEP;
	final float MIN_TEMPERATURE = Configuration.MINIMUM_TEMPERATURE;
	final float MAX_TEMPERATURE = Configuration.MAXIMUM_TEMPERATURE;

	final int ROCKS_TO_ADD = Configuration.ROCKS_TO_ADD;
	final int CREATURE_MINIMUM = Configuration.CREATURE_MINIMUM;

	float scaleFactor;
	int windowWidth;
	int windowHeight;
	float cameraX = BOARD_WIDTH * 0.5f;
	float cameraY = BOARD_HEIGHT * 0.5f;
	float cameraR = 0;
	float zoom = 1;
	PFont font;
	int dragging = 0; // 0 = no drag, 1 = drag screen, 2 and 3 are dragging temp
						// extremes.
	float prevMouseX;
	float prevMouseY;
	boolean draggedFar = false;
	final String INITIAL_FILE_NAME = "PIC";

	public static void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "evolv.io.EvolvioColor" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}

	@Override
	public void settings() {
		// Get users window size
		windowWidth = displayWidth;
		windowHeight = displayHeight;

		// Set scaling to be custom to current users screen size
		scaleFactor = ((float) windowHeight) / BOARD_HEIGHT / SCALE_TO_FIX_BUG;

		// Allow window to be docked and resized the UI still needs to be
		// changed to make UI look good after resize
		size(windowWidth, windowHeight);
	}

	@Override
	public void setup() {
		surface.setResizable(true);
		colorMode(HSB, 1.0f);
		font = loadFont("Jygquip1-48.vlw");
		evoBoard = new Board(this, BOARD_WIDTH, BOARD_HEIGHT, NOISE_STEP_SIZE, MIN_TEMPERATURE, MAX_TEMPERATURE,
				ROCKS_TO_ADD, CREATURE_MINIMUM, SEED, INITIAL_FILE_NAME, TIME_STEP);
		resetZoom();
	}

	@Override
	public void draw() {
		for (int iteration = 0; iteration < evoBoard.playSpeed; iteration++) {
			evoBoard.iterate(TIME_STEP);
		}
		if (dist(prevMouseX, prevMouseY, mouseX, mouseY) > 5) {
			draggedFar = true;
		}
		if (dragging == 1) {
			cameraX -= toWorldXCoordinate(mouseX, mouseY) - toWorldXCoordinate(prevMouseX, prevMouseY);
			cameraY -= toWorldYCoordinate(mouseX, mouseY) - toWorldYCoordinate(prevMouseX, prevMouseY);
		} else if (dragging == 2) { // UGLY UGLY CODE. Do not look at this
			if (evoBoard.setMinTemperature(1.0f - (mouseY - 30) / 660.0f)) {
				dragging = 3;
			}
		} else if (dragging == 3) {
			if (evoBoard.setMaxTemperature(1.0f - (mouseY - 30) / 660.0f)) {
				dragging = 2;
			}
		}
		if (evoBoard.userControl && evoBoard.selectedCreature != null) {
			cameraX = (float) evoBoard.selectedCreature.px;
			cameraY = (float) evoBoard.selectedCreature.py;
			cameraR = -PI / 2.0f - (float) evoBoard.selectedCreature.rotation;
		} else {
			cameraR = 0;
		}
		pushMatrix();
		scale(scaleFactor);
		evoBoard.drawBlankBoard(SCALE_TO_FIX_BUG);
		translate(BOARD_WIDTH * 0.5f * SCALE_TO_FIX_BUG, BOARD_HEIGHT * 0.5f * SCALE_TO_FIX_BUG);
		scale(zoom);
		if (evoBoard.userControl && evoBoard.selectedCreature != null) {
			rotate(cameraR);
		}
		translate(-cameraX * SCALE_TO_FIX_BUG, -cameraY * SCALE_TO_FIX_BUG);
		evoBoard.drawBoard(SCALE_TO_FIX_BUG, zoom, (int) toWorldXCoordinate(mouseX, mouseY),
				(int) toWorldYCoordinate(mouseX, mouseY));
		popMatrix();
		evoBoard.drawUI(SCALE_TO_FIX_BUG, zoom, TIME_STEP, windowHeight, 0, windowWidth, windowHeight, font);

		evoBoard.fileSave();
		prevMouseX = mouseX;
		prevMouseY = mouseY;
	}

	@Override
	public void mouseWheel(MouseEvent event) {
		float delta = event.getCount();
		if (delta >= 0.5f) {
			setZoom(zoom * 0.90909f, mouseX, mouseY);
		} else if (delta <= -0.5f) {
			setZoom(zoom * 1.1f, mouseX, mouseY);
		}
	}

	@Override
	public void mousePressed() {
		if (mouseX < windowHeight) {
			dragging = 1;
		} else {
			if (abs(mouseX - (windowHeight + 65)) <= 60 && abs(mouseY - 147) <= 60
					&& evoBoard.selectedCreature != null) {
				cameraX = (float) evoBoard.selectedCreature.px;
				cameraY = (float) evoBoard.selectedCreature.py;
				zoom = 16;
			} else if (mouseY >= 95 && mouseY < 135 && evoBoard.selectedCreature == null) {
				if (mouseX >= windowHeight + 10 && mouseX < windowHeight + 230) {
					resetZoom();
				} else if (mouseX >= windowHeight + 240 && mouseX < windowHeight + 460) {
					if (mouseButton == LEFT) {
						evoBoard.incrementSort();
					} else if (mouseButton == RIGHT) {
						evoBoard.decrementSort();
					}
				}
			} else if (mouseY >= 570) {
				float x = (mouseX - (windowHeight + 10));
				float y = (mouseY - 570);
				boolean clickedOnLeft = (x % 230 < 110);
				if (x >= 0 && x < 460 && y >= 0 && y < 200 && x % 230 < 220 && y % 50 < 40) { // 460
																								// =
																								// 2
																								// *
																								// 230
																								// and
																								// 200
																								// =
																								// 4
																								// *
																								// 50
					int mX = (int) (x / 230);
					int mY = (int) (y / 50);
					int buttonNum = mX + mY * 2;

					switch (buttonNum) {

					case (0):
						evoBoard.userControl = !evoBoard.userControl;
						break;

					case (1):
						if (clickedOnLeft) {
							evoBoard.creatureMinimum -= evoBoard.creatureMinimumIncrement;
						} else {
							evoBoard.creatureMinimum += evoBoard.creatureMinimumIncrement;
						}
						break;

					case (2):
						evoBoard.prepareForFileSave(0);
						break;

					case (3):
						if (clickedOnLeft) {
							evoBoard.imageSaveInterval *= 0.5f;
						} else {
							evoBoard.imageSaveInterval *= 2.0f;
						}
						if (evoBoard.imageSaveInterval >= 0.7f) {
							evoBoard.imageSaveInterval = Math.round(evoBoard.imageSaveInterval);
						}
						break;

					case (4):
						evoBoard.prepareForFileSave(2);
						break;

					case (5):
						if (clickedOnLeft) {
							evoBoard.textSaveInterval *= 0.5f;
						} else {
							evoBoard.textSaveInterval *= 2.0f;
						}
						if (evoBoard.textSaveInterval >= 0.7f) {
							evoBoard.textSaveInterval = Math.round(evoBoard.textSaveInterval);
						}
						break;

					case (6):
						if (clickedOnLeft) {
							if (evoBoard.playSpeed >= 2) {
								evoBoard.playSpeed /= 2;
							} else {
								evoBoard.playSpeed = 0;
							}
						} else {
							if (evoBoard.playSpeed == 0) {
								evoBoard.playSpeed = 1;
							} else {
								evoBoard.playSpeed *= 2;
							}
						}
						break;

					case (7):
						// Code for the eighth button
						break;
					}
				}
			} else if (mouseX >= height + 10 && mouseX < width - 50 && evoBoard.selectedCreature == null) {
				int listIndex = (mouseY - 150) / 70;
				if (listIndex >= 0 && listIndex < evoBoard.LIST_SLOTS) {
					evoBoard.selectedCreature = evoBoard.list[listIndex];
					cameraX = (float) evoBoard.selectedCreature.px;
					cameraY = (float) evoBoard.selectedCreature.py;
					zoom = 16;
				}
			}
			if (mouseX >= width - 50) {
				float toClickTemp = (mouseY - 30) / 660.0f;
				float lowTemp = 1.0f - evoBoard.getLowTempProportion();
				float highTemp = 1.0f - evoBoard.getHighTempProportion();
				if (abs(toClickTemp - lowTemp) < abs(toClickTemp - highTemp)) {
					dragging = 2;
				} else {
					dragging = 3;
				}
			}
		}
		draggedFar = false;
	}

	@Override
	public void mouseReleased() {
		if (!draggedFar) {
			if (mouseX < windowHeight) { // DO NOT LOOK AT THIS CODE EITHER it
											// is bad
				dragging = 1;
				float mX = toWorldXCoordinate(mouseX, mouseY);
				float mY = toWorldYCoordinate(mouseX, mouseY);
				int x = (floor(mX));
				int y = (floor(mY));
				evoBoard.unselect();
				cameraR = 0;
				if (x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT) {
					for (int i = 0; i < evoBoard.softBodiesInPositions[x][y].size(); i++) {
						SoftBody body = (SoftBody) evoBoard.softBodiesInPositions[x][y].get(i);
						if (body.isCreature) {
							float distance = dist(mX, mY, (float) body.px, (float) body.py);
							if (distance <= body.getRadius()) {
								evoBoard.selectedCreature = (Creature) body;
								zoom = 16;
							}
						}
					}
				}
			}
		}
		dragging = 0;
	}

	public void resetZoom() {
		cameraX = BOARD_WIDTH * 0.5f;
		cameraY = BOARD_HEIGHT * 0.5f;
		zoom = 1;
	}

	public void setZoom(float target, float x, float y) {
		float grossX = grossify(x, BOARD_WIDTH);
		cameraX -= (grossX / target - grossX / zoom);
		float grossY = grossify(y, BOARD_HEIGHT);
		cameraY -= (grossY / target - grossY / zoom);
		zoom = target;
	}

	public float grossify(float input, float total) { // Very weird function
		return (input / scaleFactor - total * 0.5f * SCALE_TO_FIX_BUG) / SCALE_TO_FIX_BUG;
	}

	public float toWorldXCoordinate(float x, float y) {
		float w = windowHeight / 2;
		float angle = atan2(y - w, x - w);
		float dist = dist(w, w, x, y);
		return cameraX + grossify(cos(angle - cameraR) * dist + w, BOARD_WIDTH) / zoom;
	}

	public float toWorldYCoordinate(float x, float y) {
		float w = windowHeight / 2;
		float angle = atan2(y - w, x - w);
		float dist = dist(w, w, x, y);
		return cameraY + grossify(sin(angle - cameraR) * dist + w, BOARD_HEIGHT) / zoom;
	}
}
