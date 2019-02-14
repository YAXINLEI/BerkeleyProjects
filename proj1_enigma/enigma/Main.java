package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author yaxinlei
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages. Otherwise, input comes from the standard
     *  input.
     *  ARGS[2] is optional; when present, it names an output
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

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;


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
        try {
            Machine enigmaMach = readConfig();
            String next;
            boolean boo = false;
            while (_input.hasNextLine()) {
                next = _input.nextLine();
                Scanner nextScan = new Scanner(next.trim());
                if (next.trim().equals("") && boo) {
                    _output.println();
                } else if (nextScan.next().trim().matches("\\W")) {
                    setUp(enigmaMach, next);
                    boo = true;
                } else if (boo) {
                    String result =
                            enigmaMach.convert(next.toUpperCase().trim());
                    printMessageLine(result);
                }
            }
            if (!boo) {
                throw new NoSuchElementException();
            }
        } catch (NoSuchElementException ex) {
            throw error("no settings");
        }
    }


    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String myAlpha = _config.nextLine().replaceAll(" ", "").trim();
            if (myAlpha.matches("[A-Z]\\-[A-Z]")) {
                _alphabet = new CharacterRange(myAlpha.charAt(0),
                        myAlpha.charAt(2));
            } else {
                _alphabet = new CharacterPerm(myAlpha);
            }

            String rotNumber = _config.nextLine().trim();
            Scanner temp = new Scanner(rotNumber);
            int rotorNum = temp.nextInt();
            int pawls = temp.nextInt();

            ArrayList<Rotor> allRotors = new ArrayList<Rotor>();
            while (_config.hasNextLine() && _config.hasNext()) {
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, rotorNum, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String rotorInfo = _config.nextLine().trim();
            rotorInfo = rotorInfo.toUpperCase();
            String[] rotorInfoArray = rotorInfo.split(" ");

            String rotorName = rotorInfoArray[0].trim();
            String notch = rotorInfoArray[1].trim().substring(1);
            String cycles = rotorInfo.substring(rotorInfo.indexOf("(")).trim();

            Scanner tempScanner = new Scanner(cycles);
            boolean tempBoolean = false;

            while (tempScanner.hasNext()) {
                String tempNext = tempScanner.next();
                if (!tempNext.matches("[[A-Z]|[0-9]|[a-z]]+")) {
                    if (!tempNext.matches("\\([[A-Z]|[0-9]]+\\)")
                            && !tempNext.matches("\\([A-Z]+\\)\\([A-Z]+\\)")) {
                        tempBoolean = true;
                    }
                }
            }

            if (tempBoolean) {
                throw new NoSuchElementException();
            } else if (_config.hasNext("\\([A-Z]+\\)")) {
                cycles = cycles.trim() + " " + _config.nextLine().trim();
            }

            cycles = cycles.toUpperCase();

            if (rotorInfoArray[1].equals("R")) {
                return new Reflector(rotorName,
                        new Permutation(cycles, _alphabet));

            } else if (rotorInfoArray[1].equals("N")) {
                return new FixedRotor(rotorName,
                        new Permutation(cycles, _alphabet));

            } else {
                return new MovingRotor(rotorName,
                        new Permutation(cycles, _alphabet), notch);
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        try {
            if (settings.trim().equals("")) {
                throw new NoSuchElementException();
            }
            int plugBoardIndex = settings.indexOf('(');
            if (plugBoardIndex != -1) {
                Permutation plugBoardPerm =
                        new Permutation(settings.substring(plugBoardIndex).
                                trim(), _alphabet);
                M.setPlugboard(plugBoardPerm);


                String rotorSettings =
                        settings.toUpperCase().substring(2, plugBoardIndex - 1);
                String[] rotorsArr = rotorSettings.trim().split(" ");

                String[] rotorsArrNew = new String[rotorsArr.length - 1];
                System.arraycopy(rotorsArr, 0,
                        rotorsArrNew, 0, rotorsArrNew.length);

                M.insertRotors(rotorsArrNew);
                M.setRotors(rotorsArr[rotorsArr.length - 1].trim());

            } else if (plugBoardIndex == -1) {
                Permutation plugBoardPerm = new Permutation("", _alphabet);
                M.setPlugboard(plugBoardPerm);


                String rotorSettings = settings.toUpperCase().substring(2);
                String[] rotorsArr = rotorSettings.trim().split(" ");
                String initialSettings = rotorsArr[rotorsArr.length - 1].trim();

                String[] rotorsArrNew = new String[rotorsArr.length - 1];
                System.arraycopy(rotorsArr, 0,
                        rotorsArrNew, 0, rotorsArrNew.length);

                if (initialSettings.length() != M.numRotors() - 1) {
                    throw new NoSuchElementException();
                }

                M.insertRotors(rotorsArrNew);
                M.setRotors(initialSettings);
            }
        } catch (NoSuchElementException ex) {
            throw error("bad setting");
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        msg = msg.trim();
        String result = msg.substring(msg.length() / 5 * 5, msg.length());
        for (int i = msg.length() / 5 - 1; i >= 0; i -= 1) {
            result = msg.substring(i * 5, i * 5 + 5) + " " + result;
        }
        _output.println(result);
    }
}
