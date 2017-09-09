package org.checkerframework.checker.units.parser.ast;

public class UnitsASTDivNode implements UnitsASTNode {
    private UnitsASTNode left;
    private UnitsASTNode right;

    public UnitsASTDivNode(UnitsASTNode left, UnitsASTNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "(" + left.toString() + " / " + right.toString() + ")";
    }
}
