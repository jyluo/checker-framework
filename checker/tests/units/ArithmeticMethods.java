import org.checkerframework.checker.units.qual.*;

public class ArithmeticMethods {

    // The parameters and returns need to be declared @UnknownUnits to permit the passing of values
    // with units into the method, and to ensure the body type checks

    @UnitsAddition(res = -1, larg = 1, rarg = 2)
    @UnknownUnits int sum(@UnknownUnits int x, @UnknownUnits int y) {
        return x + y;
    }

    @UnitsAddition(res = -1, larg = 1, rarg = 2)
    @UnknownUnits long sum(@UnknownUnits long x, @UnknownUnits long y) {
        return x + y;
    }

    @UnitsAddition(res = -1, larg = 1, rarg = 2)
    static @UnknownUnits long sumStatic(@UnknownUnits long x, @UnknownUnits long y) {
        return x + y;
    }

    @UnitsSubtraction(res = -1, larg = 1, rarg = 2)
    @UnknownUnits int diff(@UnknownUnits int x, @UnknownUnits int y) {
        return x - y;
    }

    @UnitsSubtraction(res = -1, larg = 1, rarg = 2)
    @UnknownUnits long diff(@UnknownUnits long x, @UnknownUnits long y) {
        return x - y;
    }

    @UnitsSubtraction(res = -1, larg = 1, rarg = 2)
    static @UnknownUnits long diffStatic(@UnknownUnits long x, @UnknownUnits long y) {
        return x - y;
    }

    @UnitsMultiplication(res = -1, larg = 1, rarg = 2)
    @UnknownUnits int calcArea(@UnknownUnits int width, @UnknownUnits int height) {
        return width * height;
    }

    @UnitsMultiplication(res = -1, larg = 1, rarg = 2)
    @UnknownUnits long calcArea(@UnknownUnits long width, @UnknownUnits long height) {
        return width * height;
    }

    @UnitsMultiplication(res = -1, larg = 1, rarg = 2)
    static @UnknownUnits long calcAreaStatic(@UnknownUnits long width, @UnknownUnits long height) {
        return width * height;
    }

    @m int m1, m2;
    @km long km1, km2;
    @m2 int msq;
    @km2 long kmsq;

    void testAddition() {
        m1 = sum(m1, m2);

        // :: error: (assignment.type.incompatible)
        m2 = sum(m1, (int) km2);

        // :: error: (assignment.type.incompatible)
        km1 = sum(m1, m2);

        km2 = sum(km1, (int) km2);

        m1 = (int) ArithmeticMethods.sumStatic(m1, m2);

        // :: error: (assignment.type.incompatible)
        km2 = ArithmeticMethods.sumStatic(m1, km2);

        m1 = Math.addExact(m1, m2);

        // :: error: (assignment.type.incompatible)
        km1 = Math.addExact(km1, m2);
    }

    void testSubtraction() {
        m1 = diff(m1, m2);

        // :: error: (assignment.type.incompatible)
        m2 = diff(m1, (int) km2);

        // :: error: (assignment.type.incompatible)
        km1 = diff(m1, m2);

        km2 = diff(km1, (int) km2);

        m1 = (int) ArithmeticMethods.diffStatic(m1, m2);

        // :: error: (assignment.type.incompatible)
        km2 = ArithmeticMethods.diffStatic(m1, km2);

        m1 = Math.subtractExact(m1, m2);

        // :: error: (assignment.type.incompatible)
        km1 = Math.subtractExact(km1, m2);
    }

    void testMultiplication() {
        msq = calcArea(m1, m2);

        // :: error: (assignment.type.incompatible)
        msq = calcArea(m1, (int) km2);

        // :: error: (assignment.type.incompatible)
        kmsq = calcArea(m1, m2);

        kmsq = calcArea(km1, (int) km2);

        msq = (int) ArithmeticMethods.calcAreaStatic(m1, m2);

        // :: error: (assignment.type.incompatible)
        msq = (int) ArithmeticMethods.calcAreaStatic(m1, km2);
    }
}
