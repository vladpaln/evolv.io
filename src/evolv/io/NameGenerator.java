package evolv.io;

class NameGenerator {
	/**
	 * 
	 */
	private final EvolvioColor evolvioColor;

	/**
	 * @param evolvioColor
	 */
	NameGenerator(EvolvioColor evolvioColor) {
		this.evolvioColor = evolvioColor;
	}

	final int MIN_NAME_LENGTH = 3;
	final int MAX_NAME_LENGTH = 10;
	final float[] LETTER_FREQUENCIES = { 8.167f, 1.492f, 2.782f, 4.253f, 12.702f, 2.228f, 2.015f, 6.094f, 6.966f,
			0.153f, 0.772f, 4.025f, 2.406f, 6.749f, 7.507f, 1.929f, 0.095f, 5.987f, 6.327f, 9.056f, 2.758f, 0.978f,
			2.361f, 0.150f, 1.974f, 0.074f };

	public String newName() {
		String nameSoFar = "";
		int chosenLength = (int) (this.evolvioColor.random(MIN_NAME_LENGTH, MAX_NAME_LENGTH));
		for (int i = 0; i < chosenLength; i++) {
			nameSoFar += getRandomChar();
		}
		return sanitizeName(nameSoFar);
	}

	public String mutateName(String input) {
		if (input.length() >= 3) {
			if (this.evolvioColor.random(0, 1) < 0.2f) {
				int removeIndex = (int) this.evolvioColor.random(0, input.length());
				input = input.substring(0, removeIndex) + input.substring(removeIndex + 1, input.length());
			}
		}
		if (input.length() <= 9) {
			if (this.evolvioColor.random(0, 1) < 0.2f) {
				int insertIndex = (int) this.evolvioColor.random(0, input.length() + 1);
				input = input.substring(0, insertIndex) + getRandomChar()
						+ input.substring(insertIndex, input.length());
			}
		}
		int changeIndex = (int) this.evolvioColor.random(0, input.length());
		input = input.substring(0, changeIndex) + getRandomChar()
				+ input.substring(changeIndex + 1, input.length());
		return input;
	}

	public char getRandomChar() {
		float letterFactor = this.evolvioColor.random(0, 100);
		int letterChoice = 0;
		while (letterFactor > 0) {
			letterFactor -= LETTER_FREQUENCIES[letterChoice];
			letterChoice++;
		}
		return (char) (letterChoice + 96);
	}

	public String sanitizeName(String input) {
		String output = "";
		int vowelsSoFar = 0;
		int consonantsSoFar = 0;
		for (int i = 0; i < input.length(); i++) {
			char ch = input.charAt(i);
			if (isVowel(ch)) {
				consonantsSoFar = 0;
				vowelsSoFar++;
			} else {
				vowelsSoFar = 0;
				consonantsSoFar++;
			}
			if (vowelsSoFar <= 2 && consonantsSoFar <= 2) {
				output = output + ch;
			} else {
				double chanceOfAddingChar = 0.5f;
				if (input.length() <= MIN_NAME_LENGTH) {
					chanceOfAddingChar = 1.0f;
				} else if (input.length() >= MAX_NAME_LENGTH) {
					chanceOfAddingChar = 0.0f;
				}
				if (this.evolvioColor.random(0, 1) < chanceOfAddingChar) {
					char extraChar = ' ';
					while (extraChar == ' ' || (isVowel(ch) == isVowel(extraChar))) {
						extraChar = getRandomChar();
					}
					output = output + extraChar + ch;
					if (isVowel(ch)) {
						consonantsSoFar = 0;
						vowelsSoFar = 1;
					} else {
						consonantsSoFar = 1;
						vowelsSoFar = 0;
					}
				}
			}
		}
		return output;
	}

	public boolean isVowel(char a) {
		return (a == 'a' || a == 'e' || a == 'i' || a == 'o' || a == 'u' || a == 'y');
	}
}