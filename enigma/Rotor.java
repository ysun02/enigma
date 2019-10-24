package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Yuan Sun
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = setting();
        _ring = ringSetting();
        _pawl = pawl();
        settingAfterRing();
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return (pawl() == 1);
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = posn;
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        _setting = cposn;
    }

    /** return my pawl(), default to 0, unless I'm a mover. */
    int pawl() {
        return 0;
    }

    /** return my ring. */
    String ringSetting() {
        return _ring;
    }

    /** set my ring to R. */
    void ring(String r) {
        _ring = r;
    }

    /** return MySetting if possible. */
    int settingAfterRing() {
        if (this.ringSetting() != null) {
            String ringS = this.ringSetting();
            char ring = (ringS.charAt(0));
            int setting = setting() - alphabet().toInt(ring);
            return permutation().wrap(setting);
        }
        return _setting;
    }
    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int setting = this.settingAfterRing();
        int now = permutation().wrap(setting + p);
        int permuteMiddle = permutation().permute(now);
        int getBack = _permutation.wrap(permuteMiddle - setting);
        return (getBack);
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int setting = this.settingAfterRing();
        int now = permutation().wrap(e + setting);
        int permuteMiddle = permutation().invert(now);
        int getBack = _permutation.wrap(permuteMiddle - setting);
        return (getBack);
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
        set(permutation().wrap(setting() + 1));
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** The current setting. */
    private int _setting;

    /** ring. */
    private String _ring;

    /** pawl. */
    private int _pawl;
}
