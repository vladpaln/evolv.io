package evolv.io;

import java.util.Random;

public class NameGenerator {
	private static final RouletteWheel<Character> CHAR_GENERATOR = new RouletteWheel<>();
	private static final Random RANDOM = new Random();

	static {
		CHAR_GENERATOR.addElement(8.167, 'a');
		CHAR_GENERATOR.addElement(1.492, 'b');
		CHAR_GENERATOR.addElement(2.782, 'c');
		CHAR_GENERATOR.addElement(4.253, 'd');
		CHAR_GENERATOR.addElement(12.702, 'e');
		CHAR_GENERATOR.addElement(2.228, 'f');
		CHAR_GENERATOR.addElement(2.015, 'g');
		CHAR_GENERATOR.addElement(6.094, 'h');
		CHAR_GENERATOR.addElement(6.966, 'i');
		CHAR_GENERATOR.addElement(0.153, 'j');
		CHAR_GENERATOR.addElement(0.772, 'k');
		CHAR_GENERATOR.addElement(4.025, 'l');
		CHAR_GENERATOR.addElement(2.406, 'm');
		CHAR_GENERATOR.addElement(6.749, 'n');
		CHAR_GENERATOR.addElement(7.507, 'o');
		CHAR_GENERATOR.addElement(1.929, 'p');
		CHAR_GENERATOR.addElement(0.095, 'q');
		CHAR_GENERATOR.addElement(5.987, 'r');
		CHAR_GENERATOR.addElement(6.327, 's');
		CHAR_GENERATOR.addElement(9.056, 't');
		CHAR_GENERATOR.addElement(2.758, 'u');
		CHAR_GENERATOR.addElement(0.978, 'v');
		CHAR_GENERATOR.addElement(2.361, 'w');
		CHAR_GENERATOR.addElement(0.150, 'x');
		CHAR_GENERATOR.addElement(1.974, 'y');
		CHAR_GENERATOR.addElement(0.074, 'z');
	}

	public static String newName() {
		int chosenLength = Configuration.MINIMUM_NAME_LENGTH
				+ RANDOM.nextInt(Configuration.MAXIMUM_NAME_LENGTH - Configuration.MINIMUM_NAME_LENGTH);
		StringBuilder sb = new StringBuilder();
		sb.ensureCapacity(chosenLength);
		for (int i = 0; i < chosenLength; i++) {
			sb.append(CHAR_GENERATOR.getRandom());
		}
		return sanitizeName(sb);
	}

	public static String mutateName(String input) {
		StringBuilder sb = new StringBuilder(input);
		for (int i = 0; i < sb.length(); i++) {
			char letter = sb.charAt(i);
			char lowerCase = Character.toLowerCase(letter);
			sb.setCharAt(i, lowerCase);
		}
		if (sb.length() >= 3) {
			if (RANDOM.nextFloat() < 0.2f) {
				int removeIndex = RANDOM.nextInt(sb.length());
				sb.deleteCharAt(removeIndex);
			}
		}
		if (sb.length() <= 9) {
			if (RANDOM.nextFloat() < 0.2f) {
				int insertIndex = RANDOM.nextInt(sb.length() + 1);
				sb.insert(insertIndex, CHAR_GENERATOR.getRandom());
			}
		}
		int changeIndex = RANDOM.nextInt(sb.length());
		sb.setCharAt(changeIndex, CHAR_GENERATOR.getRandom());
		return sanitizeName(sb);
	}

	private static String sanitizeName(StringBuilder input) {
		StringBuilder output = new StringBuilder();
		int vowelsSoFar = 0;
		int consonantsSoFar = 0;
		for (int i = 0; i < input.length(); i++) {
			char ch = input.charAt(i);
			boolean isVowel = isVowel(ch);
			if (isVowel) {
				consonantsSoFar = 0;
				vowelsSoFar++;
			} else {
				vowelsSoFar = 0;
				consonantsSoFar++;
			}
			if (vowelsSoFar <= 2 && consonantsSoFar <= 2) {
				output.append(ch);
			} else {
				double chanceOfAddingChar = 0.5f;
				if (input.length() <= Configuration.MINIMUM_NAME_LENGTH) {
					chanceOfAddingChar = 1.0f;
				} else if (input.length() >= Configuration.MAXIMUM_NAME_LENGTH) {
					chanceOfAddingChar = 0.0f;
				}
				if (RANDOM.nextFloat() < chanceOfAddingChar) {
					char extraChar = ' ';
					while (extraChar == ' ' || (isVowel == isVowel(extraChar))) {
						extraChar = CHAR_GENERATOR.getRandom();
					}
					output.append(extraChar).append(ch);
					if (isVowel) {
						consonantsSoFar = 0;
						vowelsSoFar = 1;
					} else {
						consonantsSoFar = 1;
						vowelsSoFar = 0;
					}
				}
			}
		}
		// capitalise
		output.setCharAt(0, Character.toUpperCase(output.charAt(0)));
		return output.toString();
	}

	private static boolean isVowel(char a) {
		return (a == 'a' || a == 'e' || a == 'i' || a == 'o' || a == 'u' || a == 'y');
	}
}