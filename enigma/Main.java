package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Yuan Sun
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine m = readConfig();
        String msg;
        String output;
        String res = "";
        String settingRegex = "\\*.*\\)*";
        String msgRegex = "[^\\s\\*\\(\\)]+";
        if (_input.hasNextLine()) {
            String thisLine = _input.nextLine();
            setting = getWhatIWant(thisLine, settingRegex, 0);
            setUp(m, setting);
        }
        while (_input.hasNextLine()) {
            String thisLine = _input.nextLine();
            if (thisLine.matches("\\s+")) {
                res += thisLine;
            }
            if (thisLine.contains("*")) {
                setting = getWhatIWant(thisLine, settingRegex, 0);
                setUp(m, setting);
            } else {
                msg = getAllMsg(thisLine, msgRegex, 0);
                output = m.convert(msg);
                res += printMessageLine(output);
            }
        }
        _output.printf(res);
    }

    /** Given IN, return the string that match with REGEX and GROUP.*/
    private String getWhatIWant(String in, String regex, int group)
            throws EnigmaException {
        String cleanedIn = in.trim();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(cleanedIn);
        if (matcher.find()) {
            return matcher.group(group);
        } else {
            throw error("getwhatiwant");
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alpha = "";
            int s = 0;
            int p = 0;
            _alphabet = new Alphabet();
            if (_config.hasNextLine()) {
                alpha = _config.nextLine();
                _alphabet = new Alphabet(
                        getWhatIWant(alpha,
                                "[^\\s\\*\\(\\)]+",
                                0));
            }
            if (_config.hasNextLine()) {
                String cur = _config.nextLine();
                String cleanedCur = cur.replaceAll("\\s", "");
                String integers = getWhatIWant(cleanedCur, "^\\d{1,2}", 0);
                if (integers.length() == 2) {
                    s = Integer.parseInt(integers.substring(0, 1));
                    p = Integer.parseInt(integers.substring(1));
                } else if (integers.length() == 1) {
                    s = Integer.parseInt(integers);
                    if (_config.hasNext()) {
                        String pStr = _config.next();
                        if (pStr.matches("\\d")) {
                            p = Integer.parseInt(pStr);
                        }
                    }
                }
            }
            ArrayList<Rotor> allRotors = rotors();
            Machine m = new Machine(_alphabet, s, p, allRotors);
            return m;
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Get a list of rotors available. return. */
    private ArrayList<Rotor> rotors() {
        ArrayList<Rotor> allRotors = new ArrayList<>();
        while (_config.hasNextLine()) {
            String thisLine = _config.nextLine();
            String leftOut = checkCycles(thisLine);
            if (thisLine.matches("\\s+")) {
                return allRotors;
            } else if (leftOut.compareTo("") != 0) {
                Rotor lastRotor = allRotors.get(allRotors.size() - 1);
                lastRotor.permutation().addCycle(leftOut);
            } else {
                Rotor r = readRotor(thisLine);
                allRotors.add(r);
            }
        }
        return allRotors;
    }

    /** Given IN, return the string that match with REGEX and GROUP. */
    private String matchOne(String in, String regex, int group)
            throws EnigmaException {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(in);
        if (matcher.find()) {
            return matcher.group(group);
        } else {
            throw error("string not found" + in);
        }
    }

    /** Given IN, REGEX, GROUP, return last next index of the last matched. */
    private Integer endIndex(String in, String regex, int group) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(in);
        if (matcher.find()) {
            return matcher.end();
        } else {
            return -1;
        }
    }

    /** Given LINE, return CYCLES. */
    private String checkCycles(String line) {
        String cycles = "";
        if (line.matches("^\\s*\\(.*\\)")) {
            cycles = matchOne(line, "^\\s*\\(.*\\)", 0);
        }
        return cycles;
    }

    /** Return a rotor, reading its description from _config in THISLINE. */
    private Rotor readRotor(String thisLine) {
        try {
            String name = matchOne(thisLine, "[^\\s\\(\\)]+", 0);
            int newStart = endIndex(thisLine, "[^\\s\\(\\)]+", 0);
            thisLine = thisLine.substring(newStart);

            String properties = matchOne(thisLine, "[^\\s\\(\\)]+", 0);
            newStart = endIndex(thisLine, "[^\\s\\(\\)]+", 0);
            thisLine = thisLine.substring(newStart);

            String cycles = matchOne(thisLine, "\\(.*\\)", 0);
            Permutation perm = new Permutation(cycles, _alphabet);

            if (properties.contains("M")) {
                int i = properties.indexOf("M");
                String notch = properties.substring(i + 1);
                MovingRotor r = new MovingRotor(name, perm, notch);
                return r;
            } else if (properties.contains("N")) {
                FixedRotor r = new FixedRotor(name, perm);
                return r;
            } else {
                Reflector r = new Reflector(name, perm);
                return r;
            }

        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Given IN, keep finding the string that match with REGEX and GROUP.
     *  Return all msgs. */
    private String getAllMsg(String in, String regex, int group) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(in);
        String msg = "";
        while (matcher.find()) {
            msg += matcher.group(group);
        }
        return msg;
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) throws EnigmaException {
        try {
            int size = M.numRotors();
            String regex = "[^\\s\\*\\(\\)]+";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(settings);
            String[] names = new String[size];
            String plugBoard = "";
            ArrayList<String> allItems = new ArrayList<>();

            while (matcher.find()) {
                allItems.add((matcher.group()));
            }
            for (int i = 0; i < size; i++) {
                names[i] = allItems.get(i);
            }
            if (allItems.size() < size + 1) {
                throw new EnigmaException("wrong rotors");
            }
            M.insertRotors(names);
            M.setRotors(allItems.get(size));
            int i = size;
            while (allItems.size() > i + 1) {
                String item = allItems.get(i + 1);
                if (item.length() == M.numRotors() - 1) {
                    M.setRotorsRings(item);
                } else {
                    plugBoard += allItems.get(i + 1);
                }
                i += 1;
            }
            if (plugBoard.compareTo("") != 0) {
                Permutation plugboard = new Permutation(plugBoard, _alphabet);
                M.setPlugboard(plugboard);
            }
        } catch (IndexOutOfBoundsException excp) {
            throw new EnigmaException("error");
        }
    }



    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters).
     *  return the formatted string */
    private String printMessageLine(String msg) {
        int rem = msg.length() % 5;
        int start = 0;
        String output = "";
        for (int i = 5; i <= msg.length() - rem; i += 5) {
            output += msg.substring(start, i);
            output += " ";
            start = i;
        }
        if (rem != 0) {
            String remStr = msg.substring(start);
            output += remStr;
        }
        output += "\n";
        return output;
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Current setting for machine. */
    private String setting;
}
