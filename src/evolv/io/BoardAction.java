package evolv.io;

public interface BoardAction {
	void doAction(Board board, boolean isOnLeft);

	public class None implements BoardAction {
		@Override
		public void doAction(Board board, boolean isOnLeft) {
		}
	}

	public class ToggleUserControl implements BoardAction {
		@Override
		public void doAction(Board board, boolean isOnLeft) {
			board.setUserControl(!board.isUserControl());
		}
	}

	public class ChangeCreatureMinimum implements BoardAction {
		@Override
		public void doAction(Board board, boolean isOnLeft) {
			if (isOnLeft) {
				board.decreaseCreatureMinimum();
			} else {
				board.increaseCreatureMinimum();
			}
		}
	}

	public class PrepareForFileSave implements BoardAction {
		private final int type;

		public PrepareForFileSave(int type) {
			this.type = type;
		}

		@Override
		public void doAction(Board board, boolean isOnLeft) {
			board.prepareForFileSave(type);
		}
	}

	public class ChangeImageSaveInterval implements BoardAction {
		@Override
		public void doAction(Board board, boolean isOnLeft) {
			if (isOnLeft) {
				board.decreaseImageSaveInterval();
			} else {
				board.increaseImageSaveInterval();
			}
		}
	}

	public class ChangeTextSaveInterval implements BoardAction {
		@Override
		public void doAction(Board board, boolean isOnLeft) {
			if (isOnLeft) {
				board.decreaseTextSaveInterval();
			} else {
				board.increaseTextSaveInterval();
			}
		}
	}

	public class ChangePlaySpeed implements BoardAction {
		@Override
		public void doAction(Board board, boolean isOnLeft) {
			if (isOnLeft) {
				board.decreasePlaySpeed();
			} else {
				board.increasePlaySpeed();
			}
		}
	}
}
