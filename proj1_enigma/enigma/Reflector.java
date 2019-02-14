package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a reflector in the enigma.
 *  @author yaxinlei
 */
class Reflector extends FixedRotor {

    /** A non-moving rotor named NAME whose permutation at the 0 setting
     * is PERM. */
    private String _name;

    /** Records permutation. */
    private Permutation _perm;

    /** Constructor with NAME and PERM as
     *  the permutation. */
    Reflector(String name, Permutation perm) {
        super(name, perm);
        _name = name;
        _perm = perm;
    }

    /** This gives the name of the rotor and @return a string. */
    public String toString() {
        return "Rotor " + _name;
    }

    @Override
    int convertBackward(int e) {
        throw error("Reflector can't convert backward");
    }

    @Override
    void set(int posn) {
        if (posn != 0) {
            throw error("reflector has only one position");
        }
    }



}
