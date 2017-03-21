package evolv.io;

public interface CreatureAction {

	public void doAction(Creature creature, double modifier, double timeStep);

	public class Accelerate implements CreatureAction {

		@Override
		public void doAction(Creature creature, double amount, double timeStep) {
			creature.accelerate(amount, timeStep);
		}
	}

	public class AdjustHue implements CreatureAction {

		@Override
		public void doAction(Creature creature, double modifier, double timeStep) {
			creature.setHue(Math.abs(modifier) % 1.0f);
		}
	}

	public class AdjustMouthHue implements CreatureAction {

		@Override
		public void doAction(Creature creature, double modifier, double timeStep) {
			creature.setMouthHue(Math.abs(modifier) % 1.0f);
		}

	}

	public class Eat implements CreatureAction {

		@Override
		public void doAction(Creature creature, double attemptedAmount, double timeStep) {
			creature.eat(attemptedAmount, timeStep);
		}
	}

	public class Fight implements CreatureAction {

		@Override
		public void doAction(Creature creature, double amount, double timeStep) {
			creature.fight(amount, timeStep);
		}
	}

	public class None implements CreatureAction {

		@Override
		public void doAction(Creature creature, double modifier, double timeStep) {
		}
	}

	public class Reproduce implements CreatureAction {

		@Override
		public void doAction(Creature creature, double modifier, double timeStep) {
			if (modifier <= 0) {
				return; // This creature doesn't want to reproduce
			}
			if (creature.getAge() < Configuration.MATURE_AGE) {
				return; // This creature is too young
			}
			if (creature.getEnergy() <= Configuration.SAFE_SIZE) {
				return; // This creature is too small
			}

			double babySize = Configuration.SAFE_SIZE;
			creature.reproduce(babySize, timeStep);
		}
	}

	public class Rotate implements CreatureAction {

		@Override
		public void doAction(Creature creature, double amount, double timeStep) {
			creature.rotate(amount, timeStep);
		}
	}
}
