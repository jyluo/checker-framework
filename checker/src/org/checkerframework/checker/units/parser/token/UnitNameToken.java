package org.checkerframework.checker.units.parser.token;

public class UnitNameToken implements UnitsToken {
    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public UnitNameToken(final String name) {
        // TODO: is this the best place to validate?
        this.name = name;
    }

    public String Name() {
        return this.name;
    }
}
