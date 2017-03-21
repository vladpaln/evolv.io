package evolv.io;

import java.util.Random;

class NameGenerator {
	private static final Random RANDOM = new Random();
	private static final float[] LETTER_FREQUENCIES = { 8.167f, 1.492f, 2.782f, 4.253f, 12.702f, 2.228f, 2.015f, 6.094f,
			6.966f, 0.153f, 0.772f, 4.025f, 2.406f, 6.749f, 7.507f, 1.929f, 0.095f, 5.987f, 6.327f, 9.056f, 2.758f,
			0.978f, 2.361f, 0.150f, 1.974f, 0.074f };

	public static String newName() {
		int chosenLength = Configuration.MINIMUM_NAME_LENGTH
				+ RANDOM.nextInt(Configuration.MAXIMUM_NAME_LENGTH - Configuration.MINIMUM_NAME_LENGTH);
		StringBuilder sb = new StringBuilder();
		sb.ensureCapacity(chosenLength);
		for (int i = 0; i < chosenLength; i++) {
			sb.append(getRandomChar());
		}
		return sanitizeName(sb);
	}

	public static String mutateName(String input) {
		StringBuilder sb = new StringBuilder(input);
		if (sb.length() >= 3) {
			if (RANDOM.nextFloat() < 0.2f) {
				int removeIndex = RANDOM.nextInt(sb.length());
				sb.deleteCharAt(removeIndex);
			}
		}
		if (sb.length() <= 9) {
			if (RANDOM.nextFloat() < 0.2f) {
				int insertIndex = RANDOM.nextInt(sb.length() + 1);
				sb.insert(insertIndex, getRandomChar());
			}
		}
		int changeIndex = RANDOM.nextInt(sb.length());
		sb.setCharAt(changeIndex, getRandomChar());
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
						extraChar = getRandomChar();
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

	private static char getRandomChar() {
		float letterFactor = RANDOM.nextFloat() * 100;
		int letterChoice = 0;
		while (letterFactor > 0) {
			letterFactor -= LETTER_FREQUENCIES[letterChoice];
			letterChoice++;
		}
		return (char) (letterChoice - 1 + 'a');
	}

	private static boolean isVowel(char a) {
		return (a == 'a' || a == 'e' || a == 'i' || a == 'o' || a == 'u' || a == 'y');
	}
}