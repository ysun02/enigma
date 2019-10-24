package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Yuan Sun
 */
class MovingRotor extends Rotor {



    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
    }

    @Override
    boolean atNotch() {
        String current = Character.toString(alphabet().toChar(setting()));
        return _notches.contains(current);
    }

    @Override
    void advance() {
        set(permutation().wrap(setting() + 1));
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    int pawl() {
        return 1;
    }

    /** indicate the notch a moving rotor is at. */
    private String _notches;

}
