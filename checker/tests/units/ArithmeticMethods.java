import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.*;

public class ArithmeticMethods {

    // The parameters and returns need to be declared @UnknownUnits to permit the passing of values
    // with units into the method, and to ensure the body type checks
    @UnitsMultiply(res = -1, larg = 1, rarg = 2)
    @UnknownUnits int calcArea(@UnknownUnits int width, @UnknownUnits int height) {
        return width * height;
    }

    @UnitsMultiply(res = -1, larg = 1, rarg = 2)
    static @UnknownUnits int calcAreaStatic(@UnknownUnits int width, @UnknownUnits int height) {
        return width * height;
    }

    int noAnno(int x, int y) {
        return x;
    }

    void test() {
        @m int m1, m2;
        m1 = 5 * UnitsTools.m;
        m2 = 51 * UnitsTools.m;

        @km int km1, km2;
        km1 = 5 * UnitsTools.km;
        km2 = 5 * UnitsTools.km;

        @m2 int msq;
        @km2 int kmsq;

        int x = noAnno(10, 20);

        // good
        msq = calcArea(m1, m2);

        // :: error: (assignment.type.incompatible)
        msq = calcArea(m1, km2);

        // :: error: (assignment.type.incompatible)
        kmsq = calcArea(m1, m2);

        // good
        kmsq = calcArea(km1, km2);

        msq = ArithmeticMethods.calcAreaStatic(m1, m2);
    }
}
