package evolv.io.renderers;

import processing.core.PApplet;

public interface Renderer<T> {
	void render(PApplet applet, T rendered);
}
