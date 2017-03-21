package evolv.io;

import java.util.Comparator;

import javax.swing.SortOrder;

public class CreatureComparators {
	private static abstract class BaseComparator implements Comparator<Creature> {
		private final SortOrder sortOrder;

		public BaseComparator(SortOrder sortOrder) {
			this.sortOrder = sortOrder;
		}

		@Override
		public final int compare(Creature creature1, Creature creature2) {
			if (sortOrder == SortOrder.UNSORTED) {
				return 0;
			}
			int comparison = getComparison(creature1, creature2);
			return (sortOrder == SortOrder.ASCENDING) ? comparison : -comparison;
		}

		protected abstract int getComparison(Creature creature1, Creature creature2);
	}

	public static class NameComparator extends BaseComparator {
		public NameComparator(SortOrder sortOrder) {
			super(sortOrder);
		}

		@Override
		public int getComparison(Creature creature1, Creature creature2) {
			return creature2.getName().compareTo(creature1.getName());
		}
	}

	public static class SizeComparator extends BaseComparator {
		public SizeComparator(SortOrder sortOrder) {
			super(sortOrder);
		}

		@Override
		public int getComparison(Creature creature1, Creature creature2) {
			return (int) Math.signum(creature1.getEnergy() - creature2.getEnergy());
		}
	}

	public static class AgeComparator extends BaseComparator {
		public AgeComparator(SortOrder sortOrder) {
			super(sortOrder);
		}

		@Override
		public int getComparison(Creature creature1, Creature creature2) {
			return (int) Math.signum(creature1.getBirthTime() - creature2.getBirthTime());
		}
	}

	public static class GenComparator extends BaseComparator {
		public GenComparator(SortOrder sortOrder) {
			super(sortOrder);
		}

		@Override
		public int getComparison(Creature creature1, Creature creature2) {
			return creature1.getGen() - creature2.getGen();
		}
	}
}
