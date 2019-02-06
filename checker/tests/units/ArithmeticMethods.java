import org.checkerframework.checker.units.qual.*;

public class ArithmeticMethods {

    // The parameters and returns need to be declared @UnknownUnits to permit the passing of values
    // with units into the method, and to ensure the body type checks
    @UnitsMultiplication(res = -1, larg = 1, rarg = 2)
    @UnknownUnits int calcArea(@UnknownUnits int width, @UnknownUnits int height) {
        return width * height;
    }

    @UnitsMultiplication(res = -1, larg = 1, rarg = 2)
    static @UnknownUnits int calcAreaStatic(@UnknownUnits int width, @UnknownUnits int height) {
        return width * height;
    }

    @UnitsAddition(res = -1, larg = 1, rarg = 2)
    @UnknownUnits int sum(@UnknownUnits int x, @UnknownUnits int y) {
        return x + y;
    }

    @UnitsAddition(res = -1, larg = 1, rarg = 2)
    static @UnknownUnits int sumStatic(@UnknownUnits int x, @UnknownUnits int y) {
        return x + y;
    }

    @m int m1, m2;
    @km int km1, km2;
    @m2 int msq;
    @km2 int kmsq;

    void testAddition() {
        m1 = sum(m1, m2);

        // :: error: (assignment.type.incompatible)
        m2 = sum(m1, km2);

        // :: error: (assignment.type.incompatible)
        km1 = sum(m1, m2);

        km2 = sum(km1, km2);

        m1 = ArithmeticMethods.sumStatic(m1, m2);

        // :: error: (assignment.type.incompatible)
        m2 = ArithmeticMethods.sumStatic(m1, km2);
    }

    void testMultiplication() {
        msq = calcArea(m1, m2);

        // :: error: (assignment.type.incompatible)
        msq = calcArea(m1, km2);

        // :: error: (assignment.type.incompatible)
        kmsq = calcArea(m1, m2);

        kmsq = calcArea(km1, km2);

        msq = ArithmeticMethods.calcAreaStatic(m1, m2);

        // :: error: (assignment.type.incompatible)
        msq = ArithmeticMethods.calcAreaStatic(m1, km2);
    }
}
