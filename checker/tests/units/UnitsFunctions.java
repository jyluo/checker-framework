import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.*;

public class UnitsFunctions {
    @m int m = 10 * UnitsTools.m;
    @s int s = 20 * UnitsTools.s;

    void addition() {
        m = m + m;
        s = s + s;

        //:: error: (assignment.type.incompatible)
        m = s + m;
        //:: error: (assignment.type.incompatible)
        s = m + s;
    }

    void subtraction() {
        m = m - m;
        s = s - s;

        //:: error: (assignment.type.incompatible)
        m = s - m;
        //:: error: (assignment.type.incompatible)
        s = m - s;
    }

    void multiplication() {
        // TODO: fix multiplication of scalar rule
        // short term: hack rule
        // long term: create correct default representation
        m = m * 10;
        s = s * 20;

        //:: error: (assignment.type.incompatible)
        m = m * m;
        //:: error: (assignment.type.incompatible)
        s = s * s;
    }

    void division() {}
}
