import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;
import org.checkerframework.checker.units.qual.time.instant.*;

class Comparison {
    @m int meter = 20 * UnitsTools.m;
    @s int second = 30 * UnitsTools.s;

    void basicComparison() {
        if (meter == meter) ;
        if (meter != meter) ;
        if (meter > meter) ;
        if (meter >= meter) ;
        if (meter < meter) ;
        if (meter <= meter) ;

        // comparisons can only be performed on operands that have matching units
        //:: error: (operands.unit.mismatch)
        if (meter == second) ;
        //:: error: (operands.unit.mismatch)
        if (meter != second) ;
        //:: error: (operands.unit.mismatch)
        if (meter > second) ;
        //:: error: (operands.unit.mismatch)
        if (meter >= second) ;
        //:: error: (operands.unit.mismatch)
        if (meter < second) ;
        //:: error: (operands.unit.mismatch)
        if (meter <= second) ;
    }

    void undeclaredComparison(int x, int y) {
        // comparison of two Scalar variables
        if (x == y) ;
        if (x != y) ;
        if (x > y) ;
        if (x >= y) ;
        if (x < y) ;
        if (x <= y) ;

        // comparison of Scalar variable to Scalar constant
        // might have to override and allow as parameters need to be unknown
        if (x == 30) ;
        if (x != 30) ;
        if (x > 30) ;
        if (x >= 30) ;
        if (x < 30) ;
        if (x <= 30) ;
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
    void foreachLoopIndexComparisonPrimitiveTypeArray() {
        int[] x = new int[5];
        for (int i : x) ;
    }
    // also test the foreach loop used on a generic type array
    <T> void foreachLoopIndexComparisonGenericTypeArray(T[] x) {
        for (T i : x) ;
    }

    // contains() has a hidden comparison
    void containsComparisonSecondClasses(Number num) {
        //:: error: (assignment.type.incompatible)
        Class<? extends @s Object> c = num.getClass();

        List<Class<? extends @s Object>> l = new ArrayList<Class<? extends @s Object>>();
        l.add(c);

        if (l.contains(c)) {}
    }

    int containsComparisonScalarClasses(Number num, @s Number numSec) {
        List<Class<? extends Number>> INTEGERS =
                Arrays.<Class<? extends Number>>asList(
                        Long.class, Integer.class, Short.class, Byte.class);
        if (INTEGERS.contains(num.getClass())) {
            return 5;
        } else if (INTEGERS.contains(numSec.getClass())) {
            return 6;
        }
        return 9;
    }
}
