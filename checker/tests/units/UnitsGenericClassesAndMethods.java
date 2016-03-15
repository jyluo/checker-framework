import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;
import org.checkerframework.checker.units.qual.time.instant.*;
import org.checkerframework.checker.units.UnitsTools;

import java.util.List;
import java.util.LinkedList;

class UnitsGenericClassesAndMethods {

    // =======================
    // Generic Classes
    // =======================

    void referencesTest() {
        // local references are by default @UnknownUnits
        Number defaultRef = new Integer(5);
        Number defaultRef1 = new @UnknownUnits Integer(5);
        Number defaultRef2 = new @m Integer(5);

        @Scalar Number scalarRef = new Integer(5);
        //:: error: (assignment.type.incompatible)
        @Scalar Number scalarRef1 = new @UnknownUnits Integer(5);
        //:: error: (assignment.type.incompatible)
        @Scalar Number scalarRef2 = new @m Integer(5);
    }

    void listTest() {
        List<Number> scalarList = new LinkedList<Number>();
        scalarList.add(new Integer(5));
        //:: error: (argument.type.incompatible)
        scalarList.add(new @m Integer(5));
        //:: error: (argument.type.incompatible)
        scalarList.add(new @UnknownUnits Integer(5));

        List<@UnknownUnits Number> unknownList = new LinkedList<Number>();
        unknownList.add(new Integer(5));
        unknownList.add(new @m Integer(5));

        List<@Length Number> lengthList = new LinkedList<@Length Number>();
        lengthList.add(new @m Integer(5));
        //:: error: (argument.type.incompatible)
        lengthList.add(new Integer(5));
        //:: error: (argument.type.incompatible)
        lengthList.add(new @s Integer(5));
    }

    class MyList<T> {
        public MyList(){}
        void add(T value) {}
    }

    void myListTest() {
        MyList<@Length Number> list = new MyList<@Length Number>();
        list.add(new @m Integer(5));
        //:: error: (argument.type.incompatible)
        list.add(new @s Integer(5));
        //:: error: (argument.type.incompatible)
        list.add(new @UnknownUnits Integer(5));
    }

    // explicitly extend a category of units in class declaration
    class A<T extends @Length Object> {
        public A() {}

        T method(T input) {
            return input;
        }
    }

    void classATest() {
        A<@m(Prefix.nano) Number> a = new A<@m(Prefix.nano) Number>();
        a.method(new @m(Prefix.nano) Integer(5));
        //:: error: (argument.type.incompatible)
        a.method(new @UnknownUnits Integer(5));

        //:: error: (type.argument.type.incompatible)
        A<@s Number> b = new A<@s Number>();
    }

    // =======================
    // Generic Methods
    // =======================

    // implicit and explicity upper bounds are defaulted to UnknownUnits
    <T> T defaultImplicitBounds(T input) {
        return input;
    }

    void defaultImplicitBoundsTest() {
        @UnknownUnits Object w = new @UnknownUnits Object();
        Object x = null; // flow refined to UnitsBottom
        Number y = null; // flow refined to UnitsBottom
        @m Integer z = new @m Integer(5);

        // explicit type invocation
        this.<@UnknownUnits Object>defaultImplicitBounds(w);

        // because w is UnknownUnits, declaring T explicitly as Object causes
        // argument type incompatible error since w is an UnknownUnits Object,
        // and Scalar is the default type of Classes including Object
        //:: error: (argument.type.incompatible)
        this.<Object>defaultImplicitBounds(w);

        // x is Scalar Object, no problem here
        this.<Object>defaultImplicitBounds(x);

        // y is Scalar Number, also no problem
        this.<Number>defaultImplicitBounds(y);

        // explicitly declaring T as Scalar Integer causes argument type
        // incompatible error as z is a Meter Integer
        //:: error: (argument.type.incompatible)
        this.<Integer>defaultImplicitBounds(z);

        // bottom is a subtype of m
        this.<@m Object>defaultImplicitBounds(x);
        this.<@m Integer>defaultImplicitBounds(z);
        this.<@Length Integer>defaultImplicitBounds(z);

        // meter is not a subtype of scalar
        //:: error: (argument.type.incompatible)
        this.<Integer>defaultImplicitBounds(z);

        // implicit type invocation
        w = defaultImplicitBounds(w);
        x = defaultImplicitBounds(x);
        y = defaultImplicitBounds(y);
        z = defaultImplicitBounds(z);
    }

    // lower bound is declared to be meters, accepted units of T are Unknown,
    // Length and Meter
    <@m T> T meterLowerBound(T input) {
        return input;
    }

    // lower bound is declared to be length, accepted units of T are Unknown,
    // Length
    <@Length T> T lengthLowerBound(T input) {
        return input;
    }

    void lowerBoundsTest() {
        @UnknownUnits Object w = new @UnknownUnits Object();
        Object x = null; // flow refined to UnitsBottom
        Number y = null; // flow refined to UnitsBottom

        @m Integer meter = new @m Integer(5);
        @Length Integer length = new @Length Integer(5);
        @s Integer second = new @s Integer(5);

        // explicit type invocation
        // all types involved are precisely @m
        this.<@m Object>meterLowerBound(meter);
        // this passes because @Length Object is a supertype of @m T and bottom
        // is a subtype of @Length
        this.<@Length Object>meterLowerBound(null);
        this.<@Length Object>meterLowerBound(length);
        // same for UnknownUnits
        this.<@UnknownUnits Object>meterLowerBound(null);
        this.<@UnknownUnits Object>meterLowerBound(length);
        this.<@UnknownUnits Object>meterLowerBound(meter);
        // this passes because @Unknown Object overrides @m T, and @s is a
        // subtype of @Unknown
        this.<@UnknownUnits Object>meterLowerBound(second);
        // while bottom is a subtype of Speed, Speed is not a supertype of m
        //:: error: (type.argument.type.incompatible)
        this.<@Speed Object>meterLowerBound(null);

        this.<@Length Object>lengthLowerBound(meter);
        // second is not a subtype of length
        //:: error: (argument.type.incompatible)
        this.<@Length Object>lengthLowerBound(second);
        // meter is not a supertype of length
        //:: error: (type.argument.type.incompatible)
        this.<@m Object>lengthLowerBound(meter);
        // scalar is not a supertype of length
        //:: error: (type.argument.type.incompatible)
        this.<Object>meterLowerBound(null);

        // implicit type invocation
        meterLowerBound(meter);
        meterLowerBound(null);
        meterLowerBound(length);
        // type parameter unit second is not a supertype of meter
        //:: error: (type.argument.type.incompatible)
        meterLowerBound(second);

        meter = meterLowerBound(meter);
        meter = meterLowerBound(new @UnitsBottom Integer(5));
        meter = meterLowerBound(null);

        length = meterLowerBound(length);

        // meter sets the type of T, and seconds is not a subtype of meter
        //:: error: (assignment.type.incompatible)
        meter = meterLowerBound(second);
        // type parameter unit second is not a supertype of meter
        //:: error: (type.argument.type.incompatible)
        second = meterLowerBound(second);

        w = meterLowerBound(w);
        w = meterLowerBound(null);
        x = meterLowerBound(x);
        x = meterLowerBound(null);
        y = meterLowerBound(y);
        y = meterLowerBound(null);
    }

    // lowerbound is defaulted to UnitsBottom, upperbound is defaulted to
    // UnknownUnits. all units are accepted unless the type instantiated
    // for T from this.<T> clashes with the input's type
    <T extends Object> T defaultExplicitUpperBound(T input) {
        return input;
    }

    void defaultExplicitUpperBoundTest() {
        @UnknownUnits Object w = new @UnknownUnits Object();
        Object x = null; // flow refined to UnitsBottom
        Number y = null; // flow refined to UnitsBottom
        @m Integer z = new @m Integer(5);

        // explicit type invocation
        this.<@UnknownUnits Object>defaultExplicitUpperBound(w);

        // because w is UnknownUnits, declaring T explicitly as Object causes
        // argument type incompatible error since w is an UnknownUnits Object,
        // and Scalar is the default type of Classes including Object
        //:: error: (argument.type.incompatible)
        this.<Object>defaultExplicitUpperBound(w);

        // x is Scalar Object, no problem here
        this.<Object>defaultExplicitUpperBound(x);

        // y is Scalar Number, also no problem
        this.<Number>defaultExplicitUpperBound(y);

        // explicitly declaring T as Scalar Integer causes argument type
        // incompatible error as z is a Meter Integer
        //:: error: (argument.type.incompatible)
        this.<Integer>defaultExplicitUpperBound(z);

        // bottom is a subtype of m
        this.<@m Object>defaultExplicitUpperBound(x);
        this.<@m Integer>defaultExplicitUpperBound(z);
        this.<@Length Integer>defaultExplicitUpperBound(z);

        // meter is not a subtype of scalar
        //:: error: (argument.type.incompatible)
        this.<Integer>defaultExplicitUpperBound(z);

        // implicit type invocation
        w = defaultExplicitUpperBound(w);
        x = defaultExplicitUpperBound(x);
        y = defaultExplicitUpperBound(y);
        z = defaultExplicitUpperBound(z);
    }

    // lowerbound is defaulted to UnitsBottom, upperbound is declared as
    // UnknownUnits. all units are accepted unless the type instantiated
    // for T from this.<T> clashes with the input's type
    <T extends @UnknownUnits Object> T declaredExplicitUpperBound(T input) {
        return input;
    }

    void declaredExplicitUpperBoundTest() {
        @UnknownUnits Object w = new @UnknownUnits Object();
        Object x = null; // flow refined to UnitsBottom
        Number y = null; // flow refined to UnitsBottom
        @m Integer z = new @m Integer(5);

        // explicit type invocation
        this.<@UnknownUnits Object>declaredExplicitUpperBound(w);

        // because w is UnknownUnits, declaring T explicitly as Object causes
        // argument type incompatible error since w is an UnknownUnits Object,
        // and Scalar is the default type of Classes including Object
        //:: error: (argument.type.incompatible)
        this.<Object>declaredExplicitUpperBound(w);

        // x is Scalar Object, no problem here
        this.<Object>declaredExplicitUpperBound(x);

        // y is Scalar Number, also no problem
        this.<Number>declaredExplicitUpperBound(y);

        // explicitly declaring T as Scalar Integer causes argument type
        // incompatible error as z is a Meter Integer
        //:: error: (argument.type.incompatible)
        this.<Integer>declaredExplicitUpperBound(z);

        // bottom is a subtype of m
        this.<@m Object>declaredExplicitUpperBound(x);
        this.<@m Integer>declaredExplicitUpperBound(z);
        this.<@Length Integer>declaredExplicitUpperBound(z);

        // meter is not a subtype of scalar
        //:: error: (argument.type.incompatible)
        this.<Integer>declaredExplicitUpperBound(z);

        // implicit type invocation
        w = declaredExplicitUpperBound(w);
        x = declaredExplicitUpperBound(x);
        y = declaredExplicitUpperBound(y);
        z = declaredExplicitUpperBound(z);
    }

    // lowerbound is defaulted to UnitsBottom, upperbound is declared as Length.
    // all subtypes of Length are accepted unless the type instantiated for T
    // from this.<T> clashes with the input's type
    <T extends @Length Object> T declaredExplicitLengthUpperBound(T input) {
        return input;
    }

    void declaredExplicitLengthUpperBoundTest() {
        @UnknownUnits Object w = new @UnknownUnits Object();
        Object x = null; // flow refined to UnitsBottom
        Number y = null; // flow refined to UnitsBottom
        @m Integer z = new @m Integer(5);

        // explicit type invocation
        // the explicitly declared T has to be a subtype of Length
        //:: error: (type.argument.type.incompatible)
        this.<@UnknownUnits Object>declaredExplicitLengthUpperBound(w);

        // w is UnknownUnits Object, which is not a subtype of Length
        //:: error: (argument.type.incompatible)
        this.<@Length Object>declaredExplicitLengthUpperBound(w);

        // T = Scalar Object is not a subtype of Length Object
        //:: error: (type.argument.type.incompatible)
        this.<Object>declaredExplicitLengthUpperBound(x);

        // UnitsBottom is a subtype of Length
        this.<@Length Object>declaredExplicitLengthUpperBound(x);

        // bottom is a subtype of m
        this.<@m Object>declaredExplicitLengthUpperBound(x);
        this.<@m Integer>declaredExplicitLengthUpperBound(z);
        this.<@Length Integer>declaredExplicitLengthUpperBound(z);

        // implicit type invocation
        // T = UnknownUnits Object from argument w, and it is not a subtype of Length
        //:: error: (type.argument.type.incompatible)
        w = declaredExplicitLengthUpperBound(w);
        x = declaredExplicitLengthUpperBound(x);
        y = declaredExplicitLengthUpperBound(y);
        z = declaredExplicitLengthUpperBound(z);
    }

    // lowerbound is declared to Meters, upperbound is declared as Length.
    // T must be either Meters or Length and the type instantiated for T
    // from this.<T> must not clashes with the input's type
    <@m T extends @Length Object> T declaredExplicitBounds(T input) {
        return input;
    }

    //:: error: (bound.type.incompatible)
    <@Length T extends @m Object> T declaredExplicitBoundsBAD(T input) {
        return input;
    }

    void declaredExplicitBoundsTest() {
        @UnknownUnits Object w = new @UnknownUnits Object();
        @UnitsBottom Object b = new @UnitsBottom Object();
        Object x = new Object(); // Scalar Object
        Number y = null; // flow refined to UnitsBottom
        @m Integer z = new @m Integer(5);

        // explicit type invocation
        // the explicitly declared T has to be a subtype of Length
        //:: error: (type.argument.type.incompatible)
        this.<@UnknownUnits Object>declaredExplicitBounds(w);

        // the explicitly declared T has to be a supertype of Meter
        //:: error: (type.argument.type.incompatible)
        this.<@UnitsBottom Object>declaredExplicitBounds(b);

        // the flow refined UnitsBottom x can be passed in, so no
        // argument.type.incompatible error
        //:: error: (type.argument.type.incompatible)
        this.<@UnitsBottom Object>declaredExplicitBounds(y);

        // bottom is a subtype of m
        this.<@m Object>declaredExplicitBounds(y);
        this.<@m Integer>declaredExplicitBounds(z);
        this.<@Length Integer>declaredExplicitBounds(z);

        // implicit type invocation
        // T = UnknownUnits Object from argument w, and it is not a subtype of Length
        //:: error: (type.argument.type.incompatible)
        w = declaredExplicitBounds(w);

        // b is UnitsBottom which is a subtype of meter, therefore accepted as a type argument?
        //:: error: (assignment.type.incompatible)
        b = declaredExplicitBounds(b);
        // Scalar is not a subtype of Length
        //:: error: (type.argument.type.incompatible)
        x = declaredExplicitBounds(x);
        y = declaredExplicitBounds(y);
        z = declaredExplicitBounds(z);
    }
}
