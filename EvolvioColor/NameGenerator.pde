class NameGenerator {
  final int MIN_NAME_LENGTH = 3;
  final int MAX_NAME_LENGTH = 10;
  final float[] LETTER_FREQUENCIES = {8.167, 1.492, 2.782, 4.253, 12.702, 2.228, 2.015, 6.094, 6.966, 0.153, 0.772, 4.025, 2.406, 6.749, 
    7.507, 1.929, 0.095, 5.987, 6.327, 9.056, 2.758, 0.978, 2.361, 0.150, 1.974, 0.074};

  public String newName() {
    String nameSoFar = "";
    int chosenLength = (int)(random(MIN_NAME_LENGTH, MAX_NAME_LENGTH));
    for (int i = 0; i < chosenLength; i++) {
      nameSoFar += getRandomChar();
    }
    return sanitizeName(nameSoFar);
  }

  public String mutateName(String input) {
    if (input.length() >= 3) {
      if (random(0, 1) < 0.2) {
        int removeIndex = (int)random(0, input.length());
        input = input.substring(0, removeIndex)+input.substring(removeIndex+1, input.length());
      }
    }
    if (input.length() <= 9) {
      if (random(0, 1) < 0.2) {
        int insertIndex = (int)random(0, input.length()+1);
        input = input.substring(0, insertIndex) + getRandomChar() + input.substring(insertIndex, input.length());
      }
    }
    int changeIndex = (int)random(0, input.length());
    input = input.substring(0, changeIndex) + getRandomChar() + input.substring(changeIndex+1, input.length());
    return input;
  }

  public char getRandomChar() {
    float letterFactor = random(0, 100);
    int letterChoice = 0;
    while (letterFactor > 0) {
      letterFactor -= LETTER_FREQUENCIES[letterChoice];
      letterChoice++;
    }
    return (char)(letterChoice+96);
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
        output = output+ch;
      } else {
        double chanceOfAddingChar = 0.5;
        if (input.length() <= MIN_NAME_LENGTH) {
          chanceOfAddingChar = 1.0;
        } else if (input.length() >= MAX_NAME_LENGTH) {
          chanceOfAddingChar = 0.0;
        }
        if (random(0, 1) < chanceOfAddingChar) {
          char extraChar = ' ';
          while (extraChar == ' ' || (isVowel(ch) == isVowel(extraChar))) {
            extraChar = getRandomChar();
          }
          output = output+extraChar+ch;
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
