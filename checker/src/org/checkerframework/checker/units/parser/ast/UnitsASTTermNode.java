package org.checkerframework.checker.units.parser.ast;

public class UnitsASTTermNode implements UnitsASTNode {
    private UnitsASTNode enclosedExprNode;
    private int power;

    public UnitsASTTermNode(UnitsASTNode enclosedExprNode) {
        this.enclosedExprNode = enclosedExprNode;
        this.power = 1;
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
                return enclosedExprNode.toString();
            default:
                return enclosedExprNode.toString() + "^" + power;
        }
    }
}
