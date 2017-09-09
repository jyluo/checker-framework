package org.checkerframework.checker.units.parser;

import java.io.IOException;
import java.io.StringReader;

/** Same as java.io.StringReader with peek functionality implemented. */
public class UnitsStringReader extends StringReader {

    public UnitsStringReader(String arg0) {
        super(arg0);
    }

    /**
     * Peeks at the next character in the string by creating a marker.
     *
     * @return the next character or -1 if there's no more characters in the string.
     * @throws IOException If an I/O error occurs
     */
    public int peek() throws IOException {
        this.mark(1);
        int nextVal = this.read();
        this.reset();
        return nextVal;
    }
}
