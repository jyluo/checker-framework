package org.checkerframework.checker.units.parser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.parser.token.DivToken;
import org.checkerframework.checker.units.parser.token.LeftParenToken;
import org.checkerframework.checker.units.parser.token.MinusToken;
import org.checkerframework.checker.units.parser.token.MulToken;
import org.checkerframework.checker.units.parser.token.NumberToken;
import org.checkerframework.checker.units.parser.token.PowerToken;
import org.checkerframework.checker.units.parser.token.RightParenToken;
import org.checkerframework.checker.units.parser.token.UnitNameToken;
import org.checkerframework.checker.units.parser.token.UnitsToken;

public class UnitsTokenizer {
    private UnitsStringReader reader;

    // TODO: accept valid unit names list.
    public UnitsTokenizer() {}

    public List<UnitsToken> scan(@NonNull String expression) throws Exception {
        List<UnitsToken> tokens = new LinkedList<UnitsToken>();
        reader = new UnitsStringReader(expression);

        try {
            while (reader.peek() != -1) {
                char c = (char) reader.peek();

                // Skip all white space characters
                if (Character.isWhitespace(c)) {
                    reader.read();
                    continue;
                }

                // Create tokens for supported characters
                if (c == '*') {
                    tokens.add(new MulToken());
                    reader.read();
                } else if (c == '/') {
                    tokens.add(new DivToken());
                    reader.read();
                } else if (c == '(') {
                    tokens.add(new LeftParenToken());
                    reader.read();
                } else if (c == ')') {
                    tokens.add(new RightParenToken());
                    reader.read();
                } else if (c == '^') {
                    tokens.add(new PowerToken());
                    reader.read();
                } else if (c == '-') {
                    tokens.add(new MinusToken());
                    reader.read();
                } else if (Character.isDigit(c)) {
                    int number = ParseNumber();
                    tokens.add(new NumberToken(number));
                } else if (isLetter(c)) {
                    String text = ParseUnitName();
                    tokens.add(new UnitNameToken(text));
                } else {
                    throw new Exception(
                            "Unknown character '" + c + "' in Unit \"" + expression + "\"");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokens;
    }

    /**
     * Consumes a continuous sequence of digits from the reader and converts the sequence into an
     * integer.
     *
     * @return the integer value of the sequence.
     * @throws IOException If an I/O error occurs
     */
    private int ParseNumber() throws IOException {
        StringBuffer number = new StringBuffer();
        // TODO: restrict to ISO-LATIN digits aka 0 to 9
        while (Character.isDigit(reader.peek())) {
            number.append((char) reader.read());
        }

        return Integer.parseInt(number.toString());
    }

    /**
     * Consumes a continuous sequence of upper-case and lower-case letter from the reader and
     * converts the sequence into a unit name.
     *
     * @return the unit name, if it is a valid unit name.
     * @throws IOException If an I/O error occurs
     */
    private String ParseUnitName() throws IOException {
        StringBuffer name = new StringBuffer();
        while (isLetter(reader.peek())) {
            name.append((char) reader.read());
        }
        // TODO: validate unit name or throw exception
        return name.toString();
    }

    private boolean isLetter(char c) {
        // TODO: Lower and upper case letters include non alphabetical letters
        // figure out if those are needed for unit names
        return Character.getType(c) == Character.LOWERCASE_LETTER
                || Character.getType(c) == Character.UPPERCASE_LETTER;
    }

    private boolean isLetter(int c) {
        return isLetter((char) c);
    }
}
