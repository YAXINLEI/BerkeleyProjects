package enigma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

import static enigma.EnigmaException.error;

/** Class that represents a complete enigma machine.
 *  @author yaxinlei
 */
class Machine {

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Records te number of rotors. */
    private int _numRotors;

    /** Records pawls. */
    private int _pawls;

    /** Records the collection of all rotors. */
    private Collection<Rotor> _allRotors;

    /** Records a set of all used rotors, filtered by
     * the insert rotors function. */
    private ArrayList<Rotor> _rotorsAList;

    /** This saves the plugboard. */
    private Permutation _plugboard;

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
        _plugboard = new Permutation("", _alphabet);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        try {

            for (int i = 0; i < rotors.length; i += 1) {
                for (int j = i + 1; j < rotors.length; j += 1) {
                    if (rotors[i].trim().equals(rotors[j].trim())) {
                        throw new NoSuchElementException();
                    }
                }
            }
            int movingrotor = 0;
            _rotorsAList = new ArrayList<Rotor>();
            for (String rotorName : rotors) {
                boolean found = false;
                for (Rotor theRotor : _allRotors) {
                    if (theRotor.name().toUpperCase().trim().
                            equals(rotorName.toUpperCase().trim())) {
                        _rotorsAList.add(theRotor);
                        found = true;
                        if (theRotor.rotates()) {
                            movingrotor += 1;
                        }
                    }
                }
                if (!found) {
                    throw new NoSuchElementException();
                }
            }
            if (movingrotor != numPawls()) {
                throw new NoSuchElementException();
            }
        } catch (NoSuchElementException ex) {
            throw error("bad rotor name");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 upper-case letters. The first letter refers to the
     *  leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        setting = setting.toUpperCase();
        char settingLetter;
        for (int index = 0; index < _rotorsAList.size() - 1; index += 1) {
            settingLetter = setting.charAt(index);
            _rotorsAList.get(index + 1).set(settingLetter);
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine.
     *  The three conditions for a rotor before going through
     *  the reflector to rotate is:
     *  1. the rotor is a moving rotor;
     *  2. the rotor is at notch;
     *  3. the rotor at its right has just moved.
     *  */
    int convert(int c) {

        boolean[] checkAdvance = new boolean [_rotorsAList.size() - 1];
        _rotorsAList.get(_rotorsAList.size() - 1).advance();
        if (_rotorsAList.size() > 1) {
            checkAdvance[_rotorsAList.size() - 2] = true;
        }
        for (int i = _rotorsAList.size() - 1; i > 0; i -= 1) {
            if ((checkAdvance[i - 1]) && (_rotorsAList.get(i - 1).rotates())
                    && ((_rotorsAList.get(i).atNotch())
                    || (_rotorsAList.get(i - 1).atNotch2()))) {
                _rotorsAList.get(i - 1).advance();
                checkAdvance[i - 2] = true;
            }
        }

        int beforeReflector = _plugboard.permute(c);
        for (int j = _rotorsAList.size() - 1; j >= 0; j -= 1) {
            beforeReflector =
                    _rotorsAList.get(j).convertForward(beforeReflector);
        }

        int afterReflector = beforeReflector;
        for (int k = 1; k < _rotorsAList.size(); k += 1) {
            afterReflector =
                    _rotorsAList.get(k).convertBackward(afterReflector);
        }

        afterReflector = _plugboard.permute(afterReflector);
        return afterReflector;
    }


    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        msg = msg.replaceAll(" ", "").toUpperCase();
        String result = "";

        int charInt;
        for (int index = 0; index < msg.length(); index += 1) {
            charInt = convert(_alphabet.toInt(msg.charAt(index)));
            result = result + _alphabet.toChar(charInt);
        }
        return result;
    }
}
