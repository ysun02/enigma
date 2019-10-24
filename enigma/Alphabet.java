package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Yuan Sun
 */
class Alphabet {
    /** Alphabet of enigma. */
    private String alphabet;

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        alphabet = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return alphabet.length();
    }

    /** Returns true if (CH) is in this alphabet. */
    boolean contains(char ch) {
        boolean result = (alphabet.indexOf(ch) == -1);
        return result;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return alphabet.charAt(index);
    }

    /** Returns the index of character (CH), which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        return alphabet.indexOf(Character.toString(ch));
    }

}
