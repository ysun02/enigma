package enigma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Yuan Sun
 */
class Machine {



    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
        _allRotorsNames = new HashMap<>();
        setRotorMap();
        _slots = new ArrayList<>();
        _plugBoard = new HashMap<>();

    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** RETURN my rotorMaps. */
    HashMap<String, Rotor> rotorMaps() {
        return _allRotorsNames;
    }

    /** initialize a hashmap for all rotors. */
    void setRotorMap() {
        Iterator<Rotor> rotor = _allRotors.iterator();
        while (rotor.hasNext()) {
            Rotor cur = rotor.next();
            String name = cur.name();
            _allRotorsNames.put(name, cur);
        }
    }
    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) throws EnigmaException {
        _slots = new ArrayList<>();
        int c = 0;
        for (String name : rotors) {
            Rotor target = _allRotorsNames.get(name);
            if (c == 0 && !target.reflecting()) {
                throw new EnigmaException("wrong reflector");
            }
            _slots.add(target);
            c += 1;
        }
    }

    /** Set my rotors according to param SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).
     *  doesn't return.
     *  set my pawls. */
    void setRotors(String setting) {
        if (setting.length() < numRotors() - 1) {
            throw error("Wheel settings too short");
        } else if (setting.length() > numRotors() - 1) {
            throw error("Wheel settings too long");
        } else {
            for (int i = 0; i < setting.length(); i++) {
                char cur = setting.charAt(i);
                int curI = _alphabet.toInt(cur);
                _slots.get(i + 1).set(curI);
            }
        }
    }

    /** Set my RINGS in place, doesn't return. */
    void setRotorsRings(String rings) {
        for (int i = 0; i < rings.length(); i++) {
            char cur = rings.charAt(i);
            String curS = Character.toString(cur);
            _slots.get(i + 1).ring(curS);
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugBoard = new HashMap<>();
        String p = plugboard.returnCycles(plugboard);
        for (int i = 0; i < p.length(); i += 2) {
            char cur = p.charAt(i);
            char next = p.charAt(i + 1);
            int curI = _alphabet.toInt(cur);
            int nextI = _alphabet.toInt(next);
            _plugBoard.put(curI, nextI);
            _plugBoard.put(nextI, curI);
        }
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        Rotor last = _slots.get(_slots.size() - 1);
        char nextC = _alphabet.toChar(last.setting() + 1);
        int nextI = _alphabet.toInt(nextC);
        return nextI;
    }

    /** Given I, advance me one position, check if I'm notch,
     * advance my left moving rotor one position if I am. Recurse. */
    void checkLeftMover(int i) {
        Rotor rotor = _slots.get(i);
        rotor.advance();
    }

    /** converting parameter O from the rightmost rotor to the leftmost.
        Then return */
    int keepTurningForward(int o) {
        int i = _slots.size() - 2;
        while (i >= 0) {
            Rotor r = _slots.get(i);
            o = r.convertForward(o);
            i -= 1;
        }
        return o;
    }

    /** converting number C from the leftmost rotor to the rightmost.
     * return last converted number */
    int keepTurningBackward(int c) {
        int i = 1;
        while (i <= _slots.size() - 1) {
            Rotor r = _slots.get(i);
            c = r.convertBackward(c);
            i += 1;
        }
        return c;
    }

    /** check multiple conditions and then advance, doesn't
     * return. */
    void checkDouble() throws EnigmaException {
        Rotor reflet = _slots.get(0);
        if (!reflet.reflecting()) {
            throw new EnigmaException("wrong reflector.");
        }
        int i = 1;
        int p = 0;
        Rotor last = _slots.get(_slots.size() - 1);
        int lastI = last.setting();
        while (i < numRotors() - 1) {
            Rotor me = _slots.get(i);
            Rotor next = _slots.get(i + 1);
            if (me.pawl() == 1) {
                if (next.atNotch()) {
                    me.advance();
                    next.advance();
                    i += 2;
                }
                i += 1;
            } else {
                i += 1;
            }
        }

        for (i = 0; i < _slots.size(); i++) {
            if (_slots.get(i).pawl() == 1) {
                p += 1;
            }
        }
        if (p != numPawls()) {
            throw new EnigmaException("wrong moving rotors.");
        }
        if (last.pawl() == 1) {
            if (lastI == last.setting()) {
                last.advance();
            }
        }
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        int j = _numRotors - 1;
        Rotor curRotor = _slots.get(j);
        String out = "";
        for (int i = 0; i < msg.length(); i++) {
            checkDouble();
            char cur = msg.charAt(i);
            int curI = _alphabet.toInt(cur);
            if (_plugBoard.get(curI) != null) {
                curI = _plugBoard.get(curI);
            }
            int conversion = curRotor.convertForward(curI);
            int cTail = keepTurningForward(conversion);
            int cHead = keepTurningBackward(cTail);
            if (_plugBoard.get(cHead) != null) {
                cHead = _plugBoard.get(cHead);
            }
            char c = _alphabet.toChar(cHead);
            String s = Character.toString(c);
            out += s;
        }
        return out;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** An arraylist with interface Collection of ALLROTORS. */
    private Collection<Rotor> _allRotors;

    /** number of pawls. */
    private int _pawls;

    /** number of rotors. */
    private int _numRotors;

    /** number of slots. */
    private ArrayList<Rotor> _slots;

    /** allRotors names in a hashmap. */
    private HashMap<String, Rotor> _allRotorsNames;

    /** plugboard info in hashmap. */
    private HashMap<Integer, Integer> _plugBoard;
}
