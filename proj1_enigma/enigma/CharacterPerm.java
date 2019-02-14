package enigma;

import static enigma.EnigmaException.*;

/** An Alphabet consisting of the Unicode characters in a certain range in
 *  order.
 *  @author yaxinlei
 */

class CharacterPerm extends Alphabet {

    /** An alphabet consisting of all characters in this string. */
    private String[] _charString;

    /** An alphabet constructor that takes a string of INFO. */
    CharacterPerm(String info) {
        _charString = info.trim().split("");
    }

    @Override
    int size() {
        return _charString.length;
    }

    @Override
    boolean contains(char ch) {
        for (String temp : _charString) {
            if (temp.charAt(0) == ch) {
                return true;
            }
        }
        return false;
    }

    @Override
    char toChar(int index) {
        return _charString[index].charAt(0);
    }

    @Override
    int toInt(char ch) {
        int result = 0;
        for (String temp: _charString) {
            if (temp.charAt(0) == ch) {
                break;
            }
            result += 1;
        }
        return result;
    }

    @Override
    int[] allAlphabets() {
        return new int[1];
    }
}
