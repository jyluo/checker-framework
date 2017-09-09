package org.checkerframework.checker.units.parser;

import java.util.List;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.parser.ast.UnitsASTDivNode;
import org.checkerframework.checker.units.parser.ast.UnitsASTMulNode;
import org.checkerframework.checker.units.parser.ast.UnitsASTNode;
import org.checkerframework.checker.units.parser.ast.UnitsASTUnitNameNode;
import org.checkerframework.checker.units.parser.token.DivToken;
import org.checkerframework.checker.units.parser.token.LeftParenToken;
import org.checkerframework.checker.units.parser.token.MinusToken;
import org.checkerframework.checker.units.parser.token.MulToken;
import org.checkerframework.checker.units.parser.token.NumberToken;
import org.checkerframework.checker.units.parser.token.PowerToken;
import org.checkerframework.checker.units.parser.token.RightParenToken;
import org.checkerframework.checker.units.parser.token.UnitNameToken;
import org.checkerframework.checker.units.parser.token.UnitsToken;

public class UnitsParser {
    UnitsTokenizer tokenizer = new UnitsTokenizer();
    UnitsTokenWalker walker;

    // TODO: allowed unit names list
    public UnitsParser() {}

    // EBNF Grammar:
    // Expression := Term { ("*" | "/") Term }
    // Term := UnitName [ "^" Power ] | "(" Expression ")"
    // UnitName := "m" | "s" | "km" | "mm" | "ms" | ...
    // Power := [ "-" ] Digit{Digit}
    // Digit := "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
    //
    // Notes:
    // - UnitName is a finite generated list of accepted unit names, each consisting of a sequence
    // of upper-case or lower-case letters.
    // - If fractional powers are required, change Power to Digit{Digit} [ "." Digit{Digit} ]

    public UnitsASTNode parse(@NonNull String expression, Messager msgr) throws Exception {
        List<UnitsToken> tokens = tokenizer.scan(expression);
        msgr.printMessage(Kind.NOTE, " tokens: " + tokens.toString());

        walker = new UnitsTokenWalker(tokens);
        msgr.printMessage(Kind.NOTE, " hasnext: " + walker.hasNext());

        UnitsASTNode tree = parseExpression();

        if (walker.hasNext()) {
            throw new Exception(
                    "Expecting End of Expression but there are still tokens remaining after parsing, next token: "
                            + peekNextToken());
        }

        // TODO: better error messages. Currently says what the next token is but not where this is
        // or the parsed and unparse parts.

        return tree;
    }

    // Expression := Term { ("*" | "/") Term }
    public UnitsASTNode parseExpression() throws Exception {
        UnitsASTNode term = parseTerm();

        while (walker.isNextOfType(MulToken.class) || walker.isNextOfType(DivToken.class)) {
            UnitsToken op = walker.getNext();

            UnitsASTNode termTwo;
            if (walker.hasNext()) {
                termTwo = parseTerm();
            } else {
                throw new Exception("A unit is expected after an operator.");
            }

            UnitsASTNode opNode;
            if (op.getClass().equals(MulToken.class)) {
                opNode = new UnitsASTMulNode(term, termTwo);
            } else if (op.getClass().equals(DivToken.class)) {
                opNode = new UnitsASTDivNode(term, termTwo);
            } else {
                throw new Exception("The given operator is unsupported.");
            }

            // left associative
            term = opNode;
        }

        return term;
    }

    // Term := UnitName [ "^" Power ] | "(" Expression ")"
    public UnitsASTNode parseTerm() throws Exception {
        if (walker.isNextOfType(UnitNameToken.class)) {
            UnitsASTUnitNameNode unitName = parseUnitName();
            if (walker.isNextOfType(PowerToken.class)) {
                walker.getNext(); // skip the power token
                int power = parsePower();
                unitName.setPower(power);
            }
            return unitName;
        } else if (walker.isNextOfType(LeftParenToken.class)) {
            walker.getNext(); // skip the left parenthesis
            UnitsASTNode expr = parseExpression();
            if (!walker.isNextOfType(RightParenToken.class)) {
                throw new Exception("Expecting ')' in expression, instead got " + peekNextToken());
            }
            walker.getNext(); // skip the right parenthesis
            return expr;
        } else {
            throw new Exception(
                    "Expecting a unit or a ')' in expression, instead got " + peekNextToken());
        }
    }

    // UnitName := "m" | "s" | "km" | "mm" | "ms" | ...
    public UnitsASTUnitNameNode parseUnitName() throws Exception {
        if (walker.isNextOfType(UnitNameToken.class)) {
            UnitNameToken name = (UnitNameToken) walker.getNext();
            // TODO: validation of name here??

            UnitsASTUnitNameNode node = new UnitsASTUnitNameNode(name.Name());
            node.setPower(1);
            return node;
        }
        return null;
    }

    // Power := [ "-" ] Digit{Digit}
    public int parsePower() throws Exception {
        boolean negate = walker.isNextOfType(MinusToken.class);
        if (negate) {
            walker.getNext(); // skip the negate token
        }

        if (walker.isNextOfType(NumberToken.class)) {
            NumberToken number = (NumberToken) walker.getNext();
            int power = number.Value();
            if (negate) {
                power = -power;
            }
            return power;
        } else {
            throw new Exception(
                    "Expecting an exponent in expression, instead got " + peekNextToken());
        }
    }

    private String peekNextToken() throws Exception {
        return walker.peekNext() != null
                ? "'" + walker.peekNext().toString() + "'"
                : "End of expression";
    }
}
