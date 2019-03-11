import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.*;
import org.checkerframework.framework.qual.PolyAll;

// a class that cannot be instantiated with any units
class NoAnnotClass {}

@Dimensionless
class DimensionlessClass {}

// a class that can be instantiated with units
@UnknownUnits class UUClass {
    @PolyAll int polyAllMethod(@PolyAll UUClass this, @PolyAll int x) {
        return x;
    }

    @PolyAll int polyAllMethod2(@PolyAll UUClass this) {
        return 0;
    }
}

@PolyAll class PolyAllClass {
    @PolyAll PolyAllClass(@PolyAll int x) {}
}

@PolyUnit class PolyUnitClass {
    @PolyUnit PolyUnitClass(@PolyUnit int x) {}
}

@m class MeterClass {
    @m MeterClass(@m int x) {}
}

class Constructors {
    void nonPolyConstructorTest() {
        @Dimensionless NoAnnotClass na1 = new NoAnnotClass();
        NoAnnotClass na2 = new NoAnnotClass();
        // :: error: (constructor.invocation.invalid)
        @m NoAnnotClass na3 = new @m NoAnnotClass();
        // :: error: (assignment.type.incompatible)
        @m NoAnnotClass na4 = new NoAnnotClass();
        // :: error: (constructor.invocation.invalid)
        NoAnnotClass na5 = new @m NoAnnotClass();

        @Dimensionless DimensionlessClass d1 = new DimensionlessClass();
        DimensionlessClass d2 = new DimensionlessClass();
        // :: error: (constructor.invocation.invalid)
        @m DimensionlessClass d3 = new @m DimensionlessClass();
        // :: error: (assignment.type.incompatible)
        @m DimensionlessClass d4 = new DimensionlessClass();
        // :: error: (constructor.invocation.invalid)
        // :: error: (assignment.type.incompatible)
        DimensionlessClass d5 = new @m DimensionlessClass();

        @m UUClass uu1 = new @m UUClass();
        @s UUClass uu2 = new @s UUClass();
        // :: error: (assignment.type.incompatible)
        @s UUClass uu3 = new @m UUClass();

        // :: error: (argument.type.incompatible)
        @m MeterClass mc1 = new MeterClass(5);
        // :: error: (argument.type.incompatible)
        @m MeterClass mc2 = new @m MeterClass(5);
    }

    @SuppressWarnings("cast.unsafe")
    void polyAllConstructorTest() {
        // explicitly create a meter object
        @m PolyAllClass pac1 = new @m PolyAllClass(5 * UnitsTools.m);

        pac1 = (@m PolyAllClass) new PolyAllClass(5 * UnitsTools.m);

        // create a meter object via @PolyAll
        @m PolyAllClass pac2 = new PolyAllClass(5 * UnitsTools.m);

        // creates a dimensionless object via @PolyAll
        // :: error: (assignment.type.incompatible)
        @m PolyAllClass pac3 = new PolyAllClass(5);

        // :: error: (constructor.invocation.invalid)
        // :: error: (assignment.type.incompatible)
        @m PolyAllClass pac4 = new @s PolyAllClass(5);

        // :: error: (assignment.type.incompatible)
        pac4 = (@s PolyAllClass) new PolyAllClass(5);

        // :: error: (constructor.invocation.invalid)
        PolyAllClass pac5 = new @m PolyAllClass(5 * UnitsTools.s);
    }

    void polyAllReceiverTest() {
        @m int parc1 = (new @m UUClass()).polyAllMethod(5 * UnitsTools.m);

        // :: error: (assignment.type.incompatible)
        @m int parc2 = (new @s UUClass()).polyAllMethod(5 * UnitsTools.m);

        @m int parc3 = (new @m UUClass()).polyAllMethod2();

        // :: error: (assignment.type.incompatible)
        @m int parc4 = (new @s UUClass()).polyAllMethod2();
    }

    @SuppressWarnings("cast.unsafe")
    void polyUnitConstructorTest() {
        // explicitly create a meter object
        @m PolyUnitClass puc1 = new @m PolyUnitClass(5 * UnitsTools.m);

        puc1 = (@m PolyUnitClass) new PolyUnitClass(5 * UnitsTools.m);

        // create a meter object via @PolyAll
        @m PolyUnitClass puc2 = new PolyUnitClass(5 * UnitsTools.m);

        // creates a dimensionless object via @PolyAll
        // :: error: (assignment.type.incompatible)
        @m PolyUnitClass puc3 = new PolyUnitClass(5);

        // :: error: (constructor.invocation.invalid)
        // :: error: (assignment.type.incompatible)
        @m PolyUnitClass puc4 = new @s PolyUnitClass(5);

        // :: error: (assignment.type.incompatible)
        puc4 = (@s PolyUnitClass) new PolyUnitClass(5);

        // :: error: (constructor.invocation.invalid)
        PolyUnitClass puc5 = new @m PolyUnitClass(5 * UnitsTools.s);
    }
}
