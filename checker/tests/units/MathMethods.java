import static java.lang.Math.*;

import org.checkerframework.checker.units.qual.*;

public class MathMethods {

    @m int m1, m2;
    @km int km1, km2;
    @rad double r1, r2;
    @deg double d1, d2;

    void testTrig() {
        @Dimensionless double x = sin(r1);
        @Dimensionless double y = cos(r1);
        @Dimensionless double z = tan(r1);

        // :: error: (argument.type.incompatible)
        x = sin(d1);
        // :: error: (argument.type.incompatible)
        y = cos(d1);
        // :: error: (argument.type.incompatible)
        z = tan(d1);

        r2 = asin(x);
        r2 = acos(y);
        r2 = atan(z);
        r2 = atan2(x, y);

        // :: error: (assignment.type.incompatible)
        d2 = asin(x);
        // :: error: (assignment.type.incompatible)
        d2 = acos(y);
        // :: error: (assignment.type.incompatible)
        d2 = atan(z);
        // :: error: (assignment.type.incompatible)
        d2 = atan2(x, y);

        // :: error: (units.differ)
        r2 = atan2(r1, d1);

        x = sinh(r1);
        y = cosh(r1);
        z = tanh(r1);

        // :: error: (argument.type.incompatible)
        x = sinh(d1);
        // :: error: (argument.type.incompatible)
        y = cosh(d1);
        // :: error: (argument.type.incompatible)
        z = tanh(d1);

        d1 = toDegrees(r1);
        r2 = toRadians(d2);

        // :: error: (argument.type.incompatible)
        d1 = toDegrees(d2);
        // :: error: (argument.type.incompatible)
        r2 = toRadians(r1);

        // :: error: (assignment.type.incompatible)
        r2 = toDegrees(r1);
        // :: error: (assignment.type.incompatible)
        d1 = toRadians(d2);

        d2 = hypot(d1, d2);

        // :: error: (assignment.type.incompatible)
        r1 = hypot(d1, d2);
        // :: error: (assignment.type.incompatible) :: error: (units.differ)
        d2 = hypot(r1, d2);
        // :: error: (units.differ)
        d2 = hypot(d1, r1);
    }
}
