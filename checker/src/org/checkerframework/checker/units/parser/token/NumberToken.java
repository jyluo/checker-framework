package org.checkerframework.checker.units.parser.token;

public class NumberToken implements UnitsToken {
    private final int value;

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public NumberToken(final int value) {
        this.value = value;
    }

    public int Value() {
        return value;
    }
}
