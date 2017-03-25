package evolv.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RouletteWheel<T> {
	private static final Random RANDOM = new Random();

	private final List<T> elements = new ArrayList<>();
	private final List<Double> chances = new ArrayList<>();
	private double totalChance;

	public boolean addElement(double chance, T element) {
		if (chance <= 0) {
			throw new IllegalArgumentException("Chance must be greater than zero: " + chance);
		}
		if (elements.contains(element)) {
			return false;
		}
		totalChance += chance;
		chances.add(totalChance);
		elements.add(element);
		return true;
	}

	public boolean removeElement(T element) {
		int index = elements.indexOf(element);
		if (index == -1) {
			return false;
		}
		elements.remove(index);
		double chance = chances.remove(index);
		for (int i = index; i < chances.size(); i++) {
			chances.set(i, chances.get(i) - chance);
		}
		totalChance -= chance;
		return true;
	}

	public int getElementCount() {
		return elements.size();
	}

	public double getTotalChance() {
		return totalChance;
	}

	public T getRandom() {
		if (elements.isEmpty()) {
			return null;
		}
		double nextRandom = RANDOM.nextDouble() * totalChance;
		int elementIndex = Collections.binarySearch(chances, nextRandom);
		if (elementIndex < 0) {
			// get next highest index
			elementIndex = ~elementIndex;
		}
		return elements.get(elementIndex);
	}
}
