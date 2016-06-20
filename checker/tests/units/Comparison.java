import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;
import org.checkerframework.checker.units.qual.time.instant.*;
import org.checkerframework.checker.units.UnitsTools;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class Comparison {
    @m int meter = 20 * UnitsTools.m;
    @s int second = 30 * UnitsTools.s;

    void basicComparison() {
        if (meter == meter);
        if (meter != meter);
        if (meter > meter);
        if (meter >= meter);
        if (meter < meter);
        if (meter <= meter);

        // comparisons can only be performed on operands that have matching units
        //:: error: (operands.unit.mismatch)
        if (meter == second);
        //:: error: (operands.unit.mismatch)
        if (meter != second);
        //:: error: (operands.unit.mismatch)
        if (meter > second);
        //:: error: (operands.unit.mismatch)
        if (meter >= second);
        //:: error: (operands.unit.mismatch)
        if (meter < second);
        //:: error: (operands.unit.mismatch)
        if (meter <= second);
    }

    void undeclaredComparison(int x, int y) {
        // comparison of two Scalar variables
        if (x == y);
        if (x != y);
        if (x > y);
        if (x >= y);
        if (x < y);
        if (x <= y);

        // comparison of Scalar variable to Scalar constant
        // might have to override and allow as parameters need to be unknown
        if (x == 30);
        if (x != 30);
        if (x > 30);
        if (x >= 30);
        if (x < 30);
        if (x <= 30);
    }

    void ternaryComparison() {
        @m double x;

        x = meter == meter ? meter : meter;
        x = meter != meter ? meter : meter;
        x = meter > meter ? meter : meter;
        x = meter >= meter ? meter : meter;
        x = meter < meter ? meter : meter;
        x = meter <= meter ? meter : meter;

        //:: error: (operands.unit.mismatch)
        x = meter == second ? meter : meter;
        //:: error: (operands.unit.mismatch)
        x = meter != second ? meter : meter;
        //:: error: (operands.unit.mismatch)
        x = meter > second ? meter : meter;
        //:: error: (operands.unit.mismatch)
        x = meter >= second ? meter : meter;
        //:: error: (operands.unit.mismatch)
        x = meter < second ? meter : meter;
        //:: error: (operands.unit.mismatch)
        x = meter <= second ? meter : meter;
    }

    // the internal workings of the foreach loop used on an array compares
    // an index to the array's length property
    // units checker's comparison rules need to be checked here as well
    void foreachLoopIndexComparison() {
        int [] x = new int[5];
        for (int i : x);
    }

    <T> void typeVarLoop(T[] x) {
        for (T i : x);
    }

    void m(Number num) {
        //:: error: (assignment.type.incompatible)
        Class<? extends @s Object> c = num.getClass();

        List<Class<? extends @s Object>> l = new ArrayList<Class<? extends @s Object>>();
        l.add(c);

        if ( l.contains(c) ) {}
    }

    // contains() has a hidden comparison
    private static final List<Class<? extends @Scalar Number>> INTEGERS = Arrays.<Class<? extends @Scalar Number>>asList(
        Long.class, Integer.class, Short.class, Byte.class);

    private static int factory(Number num) {
        if (INTEGERS.contains(num.getClass())) {
            return 5;
        }
        return 9;
    }
}