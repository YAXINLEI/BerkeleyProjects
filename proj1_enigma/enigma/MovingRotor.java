package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author yaxinlei
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */

    /** This is a variable that stores the notches. */
    private String _notches;

    /** This is a variable that stores the name. */
    private String _name;

    /** This is a variable that stores the permutation. */
    private Permutation _permutation;

    /** This takes in NAME, PERM, NOTCHES. */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _name = name;
        _notches = notches.replaceAll(" ", "");
        _permutation = perm;
    }


    @Override
    void advance() {
        set(_permutation.wrap(setting() + 1));
    }

    /** Defaults to printing true @return a boolean. */
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        String[] notchesSplit = _notches.split("");
        int[] temp = new int [notchesSplit.length];
        for (int i = 0; i < notchesSplit.length; i += 1) {
            temp[i] = _permutation.alphabet().toInt(notchesSplit[i].charAt(0));
        }
        for (int k : temp) {
            if (super.setting() == _permutation.wrap(k + 1)) {
                return true;
            }
        }
        return false;
    }

    @Override
    boolean atNotch2() {
        String[] notchesSplit = _notches.split("");
        int[] temp = new int [notchesSplit.length];
        for (int i = 0; i < notchesSplit.length; i += 1) {
            temp[i] = _permutation.alphabet().toInt(notchesSplit[i].charAt(0));
        }
        for (int k : temp) {
            if (super.setting() == _permutation.wrap(k)) {
                return true;
            }
        }
        return false;
    }
}
