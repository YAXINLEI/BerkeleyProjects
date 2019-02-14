package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author yaxinlei
 */
class Permutation {


    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Cycles input by the constructor. */
    private String _cycles;

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles;
    }


    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        this._cycles += cycle;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return this._alphabet.size();
    }

    /** Takes in @param cycles .
     * Splits the @return cycles into array. */
    String[] split(String cycles) {
        if (cycles.equals("")) {
            return null;
        }
        if (!cycles.contains(" ")) {
            String[] result = new String[1];
            cycles = cycles.replaceAll("\\(", "");
            cycles = cycles.replaceAll("\\)", "");
            result[0] = cycles;
            return result;
        } else {
            cycles = cycles.replaceAll(" ", "");
            String[] result = cycles.split("\\)\\(");
            result[0] = result[0].replaceAll("\\(", "");
            result[result.length - 1] = result[result.length - 1].
                    replaceAll("\\)", "");
            return result;
        }
    }



    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int pIndex = wrap(p);
        char pChar = _alphabet.toChar(pIndex);

        if (_cycles.equals("")) {
            return pIndex;
        }

        String[] cyclesArr = split(_cycles);
        for (String i : cyclesArr) {
            if (i.contains(Character.toString(pChar))) {
                if (i.length() == 1) {
                    return pIndex;
                } else if (i.indexOf(Character.toString(pChar))
                        == i.length() - 1) {
                    return _alphabet.toInt(i.charAt(0));
                } else {
                    int indexCurrent = i.indexOf(Character.toString(pChar));
                    return _alphabet.toInt(i.charAt(indexCurrent + 1));
                }
            }
        }

        return pIndex;

    }



    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int cIndex = wrap(c);
        char pChar = _alphabet.toChar(cIndex);

        if (_cycles.equals("")) {
            return cIndex;
        }

        String[] cyclesArr = split(_cycles);
        for (String i : cyclesArr) {
            if (i.contains(Character.toString(pChar))) {
                if (i.length() == 1) {
                    return cIndex;
                } else if (i.indexOf(Character.toString(pChar)) == 0) {
                    return _alphabet.toInt(i.charAt(i.length() - 1));
                } else {
                    int indexCurrent = i.indexOf(Character.toString(pChar));
                    return _alphabet.toInt(i.charAt(indexCurrent - 1));
                }
            }
        }

        return cIndex;

    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int index = _alphabet.toInt(p);
        int result = permute(index);
        return _alphabet.toChar(result);
    }


    /** Return the result of applying the inverse of this permutation to C. */
    int invert(char c) {
        int input = wrap(_alphabet.toInt(c));
        return _alphabet.toChar(invert(input));
    }


    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < _cycles.length() - 1; i += 1) {
            if (_cycles.charAt(i) == '(' && _cycles.charAt(i + 2) == ')') {
                return false;
            }
        }
        return true;
    }
}


