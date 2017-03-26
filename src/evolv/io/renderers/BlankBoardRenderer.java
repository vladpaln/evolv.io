package evolv.io.renderers;

import evolv.io.Board;
import evolv.io.Configuration;
import processing.core.PApplet;

public class BlankBoardRenderer implements Renderer<Board> {
	@Override
	public void render(PApplet applet, Board board) {
		applet.fill(board.getBackgroundColor());
		applet.rect(0, 0, Configuration.SCALE_TO_FIXBUG * Configuration.BOARD_WIDTH,
				Configuration.SCALE_TO_FIXBUG * Configuration.BOARD_HEIGHT);
	}
}
