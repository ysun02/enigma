package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a reflector in the enigma.
 *  @author Yuan Sun
 */
class Reflector extends FixedRotor {

    /** A non-moving rotor named NAME whose permutation at the 0 setting
     * is PERM. */
    Reflector(String name, Permutation perm) {
        super(name, perm);
    }

    @Override
    void set(int posn) {
        if (posn != 0) {
            throw error("reflector has only one position");
        }
    }

    @Override
    void ring(String r) {
        if (r.compareTo("A") == 0) {
            throw error("reflector has only one ring");
        }
    }

    @Override
    boolean reflecting() {
        return true;
    }
}
