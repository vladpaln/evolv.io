class NameGenerator {
    final int MIN_NAME_LENGTH = 3;
    final int MAX_NAME_LENGTH = 10;
    
    public String newName(Board board) {
        String nameSoFar = "";
        int chosenLength = (int)(random(MIN_NAME_LENGTH, MAX_NAME_LENGTH));
        for(int i = 0; i < chosenLength; i++) {
            nameSoFar += getRandomChar(board);
        }
        return sanitizeName(nameSoFar, board);
    }
    
    public String mutateName(String input, Board board) {
        if (input.length() >= 3) {
            if (random(0, 1) < 0.2) {
                int removeIndex = (int)random(0, input.length());
                input = input.substring(0, removeIndex)+input.substring(removeIndex+1, input.length());
            }
        }
        if (input.length() <= 9) {
            if (random(0, 1) < 0.2) {
                int insertIndex = (int)random(0, input.length()+1);
                input = input.substring(0, insertIndex) + getRandomChar(board) + input.substring(insertIndex, input.length());
            }
        }
        int changeIndex = (int)random(0, input.length());
        input = input.substring(0, changeIndex) + getRandomChar(board) + input.substring(changeIndex+1, input.length());
        return input;
    }
    
    public char getRandomChar(Board board) {
        float letterFactor = random(0, 100);
        int letterChoice = 0;
        while(letterFactor > 0) {
            letterFactor -= board.letterFrequencies[letterChoice];
            letterChoice++;
        }
        return (char)(letterChoice+96);
    }
    
    public String sanitizeName(String input, Board board) {
        String output = "";
        int vowelsSoFar = 0;
        int consonantsSoFar = 0;
        for(int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (isVowel(ch)) {
                consonantsSoFar = 0;
                vowelsSoFar++;
            }else {
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
                    while(extraChar == ' ' || (isVowel(ch) == isVowel(extraChar))) {
                        extraChar = getRandomChar(board);
                    }
                    output = output+extraChar+ch;
                    if (isVowel(ch)) {
                        consonantsSoFar = 0;
                        vowelsSoFar = 1;
                    } else {
                        consonantsSoFar = 1;
                        vowelsSoFar = 0;
                    }
                } else { // do nothing
                }
            }
        }
        return output;
    }
    
    public boolean isVowel(char a) {
        return (a == 'a' || a == 'e' || a == 'i' || a == 'o' || a == 'u' || a == 'y');
    }
}