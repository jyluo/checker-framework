package org.checkerframework.checker.units.parser.ast;

public class UnitsASTUnitNameNode implements UnitsASTNode {
    private String name;

    public UnitsASTUnitNameNode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
