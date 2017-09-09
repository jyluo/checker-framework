package org.checkerframework.checker.units.parser.ast;

public class UnitsASTUnitNameNode implements UnitsASTNode {
    private String name;
    private int power;

    public UnitsASTUnitNameNode(String name) {
        this.name = name;
    }

    public void setPower(int power) {
        this.power = power;
    }

    @Override
    public String toString() {
        switch (power) {
            case 0:
                return "1";
            case 1:
                return name;
            default:
                return name + "^" + power;
        }
    }
}
