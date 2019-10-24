package enigma;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Yuan Sun
 */
class Permutation {


    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _permutation = cycles;
        _permuteMap = new HashMap<>(size());
        _invertMap = new HashMap<>(size());
        addCycle(cycles);
    }

    /** Return cycles of P.*/
    String returnCycles(Permutation p) {
        return p._permutation;
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    void addCycle(String cycle) {
        String regex = "[^\\s\\*\\(\\)]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(cycle);
        while (matcher.find()) {
            String c = matcher.group();
            int i = 0;
            for (i = 0; i < c.length() - 1; i++) {
                char cur = c.charAt(i);
                char next = c.charAt(i + 1);
                int icur = alphabet().toInt(cur);
                int inext = alphabet().toInt(next);
                _permuteMap.put(icur, inext);
                _invertMap.put(inext, icur);
            }
            if (i == c.length() - 1) {
                char cur = c.charAt(i);
                char next = c.charAt(0);
                int icur = alphabet().toInt(cur);
                int inext = alphabet().toInt(next);
                _permuteMap.put(icur, inext);
                _invertMap.put(inext, icur);
            }
        }
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
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        if (_permutation.equals("")) {
            return wrap(p);
        }
        if ((_permuteMap.get(p)) != null) {
            int iNext = _permuteMap.get(p);
            return (wrap(iNext));
        } else {
            return wrap(p);
        }
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        if (_permutation.equals("")) {
            return wrap(c);
        }
        if ((_permuteMap.get(c)) != null) {
            int iPrev = _invertMap.get(c);
            return (wrap(iPrev));
        } else {
            return (wrap(c));
        }
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return _alphabet.toChar(permute(alphabet().toInt(p)));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return (alphabet().toChar(invert(alphabet().toInt(c))));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < size(); i++) {
            if (_permuteMap.get(i) == null) {
                return false;
            } else if ((_permuteMap.get(i) == i)) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Permutation that specifies a cycle. */
    private String _permutation;

    /** Hashmap that stores a forward permutation. */
    private HashMap<Integer, Integer> _permuteMap;

    /** Hashmap that stores a backward invert. */
    private HashMap<Integer, Integer> _invertMap;
}
