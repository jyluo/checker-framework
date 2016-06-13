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
        // Java Collections classes have been annotated with an upperbound of
        // @UnknownUnits, thus it can be instantiated with any unit
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

    // The implicit upperbound is by default @UnknownUnits, and here we
    // explicitly set the lowerbound to @Scalar, thus this class can only be
    // instantiated with Scalar or UnknownUnits type arguments
    class MyScalarList<@Scalar T> {
        public MyScalarList() {
        }

        void add(T value) {
        }
    }

    void myScalarListTest() {
        // By default, type arguments are @Scalar
        MyScalarList<Number> list = new MyScalarList<Number>();
        MyScalarList<@Scalar Number> list2 = new MyScalarList<@Scalar Number>();
        MyScalarList<@UnknownUnits Number> list3 = new MyScalarList<@UnknownUnits Number>();
        //:: error: (type.argument.type.incompatible)
        MyScalarList<@m Number> list4 = new MyScalarList<@m Number>();

        list.add(new Integer(5));
        list.add(new @Scalar Integer(5));
        // UnknownUnits cannot be added to a Scalar list
        //:: error: (argument.type.incompatible)
        list.add(new @UnknownUnits Integer(5));
        //:: error: (argument.type.incompatible)
        list.add(new @m Integer(5));
        list.add(null);

        list2.add(new Integer(5));
        list2.add(new @Scalar Integer(5));
        //:: error: (argument.type.incompatible)
        list2.add(new @UnknownUnits Integer(5));
        //:: error: (argument.type.incompatible)
        list2.add(new @m Integer(5));
        list2.add(null);

        // UnknownUnits could be added to a list explicitly declared with
        // UnknownUnits as its type argument
        list3.add(new Integer(5));
        list3.add(new @Scalar Integer(5));
        list3.add(new @UnknownUnits Integer(5));
        list3.add(new @m Integer(5));
        list3.add(null);
    }

    // The explicit upperbound is set to @Scalar, thus this class can be
    // instantiated with @Scalar or @UnitsBottom type arguments
    class MyScalarList2<T extends @Scalar Object> {
        public MyScalarList2() {
        }

        void add(T value) {
        }
    }

    void myScalarList2Test() {
        MyScalarList2<Number> list = new MyScalarList2<Number>();
        MyScalarList2<@Scalar Number> list2 = new MyScalarList2<@Scalar Number>();
        //:: error: (type.argument.type.incompatible)
        MyScalarList2<@UnknownUnits Number> list3 = new MyScalarList2<@UnknownUnits Number>();
        //:: error: (type.argument.type.incompatible)
        MyScalarList2<@m Number> list4 = new MyScalarList2<@m Number>();

        list.add(new Integer(5));
        list.add(new @Scalar Integer(5));
        //:: error: (argument.type.incompatible)
        list.add(new @UnknownUnits Integer(5));
        //:: error: (argument.type.incompatible)
        list.add(new @m Integer(5));
        list.add(null);

        list2.add(new Integer(5));
        list2.add(new @Scalar Integer(5));
        //:: error: (argument.type.incompatible)
        list2.add(new @UnknownUnits Integer(5));
        //:: error: (argument.type.incompatible)
        list2.add(new @m Integer(5));
        list2.add(null);
    }

    // Both the explicit lowerbound and upperbound is set to @Scalar, thus this
    // class can only be instantiated with @Scalar type arguments
    class MyScalarList3<@Scalar T extends @Scalar Object> {
        public MyScalarList3() {
        }

        void add(T value) {
        }
    }

    void myScalarList3Test() {
        MyScalarList3<Number> list = new MyScalarList3<Number>();
        MyScalarList3<@Scalar Number> list2 = new MyScalarList3<@Scalar Number>();
        //:: error: (type.argument.type.incompatible)
        MyScalarList3<@UnknownUnits Number> list3 = new MyScalarList3<@UnknownUnits Number>();
        //:: error: (type.argument.type.incompatible)
        MyScalarList3<@m Number> list4 = new MyScalarList3<@m Number>();

        list.add(new Integer(5));
        list.add(new @Scalar Integer(5));
        //:: error: (argument.type.incompatible)
        list.add(new @UnknownUnits Integer(5));
        //:: error: (argument.type.incompatible)
        list.add(new @m Integer(5));
        // TODO: should be error
        list.add(null);

        list2.add(new Integer(5));
        list2.add(new @Scalar Integer(5));
        //:: error: (argument.type.incompatible)
        list2.add(new @UnknownUnits Integer(5));
        //:: error: (argument.type.incompatible)
        list2.add(new @m Integer(5));
        // TODO: should be error
        list2.add(null);
    }

    // By default, the upper bound is @UnknownUnits, which makes generic classes
    // declarable with all units in the units checker
    class MyList<T> {
        public MyList() {
        }

        void add(T value) {
        }
    }

    void myListTest() {
        MyList<@Length Number> list = new MyList<@Length Number>();
        MyList<@m Number> list2 = new MyList<@m Number>();

        list.add(new @m Integer(5));
        //:: error: (argument.type.incompatible)
        list.add(new @s Integer(5));
        //:: error: (argument.type.incompatible)
        list.add(new @UnknownUnits Integer(5));

        list2.add(new @m Integer(5));
        //:: error: (argument.type.incompatible)
        list2.add(new @Length Integer(5));
    }

    // Custom generic classes can be explicitly annotated for a category of
    // units, making it useable with only that category of units, and forbidding
    // its use with other incompatible units
    class A<T extends @Length Object> {
        public A() {
        }

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

    // Fields are by default Scalar
    Object x = null;
    Number y = null;
    @UnknownUnits Object uu = new @UnknownUnits Object();
    @m Integer m = new @m Integer(5);
    @s Integer s = new @s Integer(5);

    // the default implicit upperbound of method type arguments is @UnknownUnits
    // and the default lowerbound is @UnitsBottom, all units are accepted unless
    // the type instantiated for T from this.<T> clashes with the input's type
    <T> T defaultImplicitExtendsBound(T input) {
        return input;
    }

    void defaultImplicitExtendsBoundTest() {
        x = this.defaultImplicitExtendsBound(x);
        y = this.defaultImplicitExtendsBound(y);
        uu = this.defaultImplicitExtendsBound(uu);
        m = this.defaultImplicitExtendsBound(m);

        // if the method was invoked with an explicit type argument, the type
        // argument must be a subtype of @UnknownUnits as well (which they all
        // are)
        this.<Object> defaultImplicitExtendsBound(x);
        this.<@UnknownUnits Object> defaultImplicitExtendsBound(x);
        // explicitly declaring T as a @Scalar Object causes argument type
        // incompatible error as uu is a @UnknownUnits Object
        //:: error: (argument.type.incompatible)
        this.<Object> defaultImplicitExtendsBound(uu);
        // explicitly declaring T as a @Scalar Integer causes argument type
        // incompatible error as m is a @Meter Integer
        //:: error: (argument.type.incompatible)
        this.<Integer> defaultImplicitExtendsBound(m);

        // @UnitsBottom (for null) is a subtype of @m, and @m is a subtype of
        // @UnknownUnits
        this.<@m Object> defaultImplicitExtendsBound(null);
    }

    // the default explicit upperbound of method type arguments is @UnknownUnits
    // and the default lowerbound is @UnitsBottom, all units are accepted unless
    // the type instantiated for T from this.<T> clashes with the input's type
    <T extends Object> T defaultExplicitExtendsBound(T input) {
        return input;
    }

    void defaultExplicitExtendsBoundTest() {
        x = this.defaultExplicitExtendsBound(x);
        y = this.defaultExplicitExtendsBound(y);
        uu = this.defaultExplicitExtendsBound(uu);
        m = this.defaultExplicitExtendsBound(m);

        this.<Object> defaultImplicitExtendsBound(x);
        this.<@UnknownUnits Object> defaultImplicitExtendsBound(x);
        //:: error: (argument.type.incompatible)
        this.<Object> defaultImplicitExtendsBound(uu);
        //:: error: (argument.type.incompatible)
        this.<Integer> defaultImplicitExtendsBound(m);
        this.<@m Object> defaultImplicitExtendsBound(null);
    }

    // lowerbound is defaulted to UnitsBottom, upperbound is declared as Length.
    // all subtypes of Length are accepted unless the type instantiated for T
    // from this.<T> clashes with the input's type
    <T extends @Length Object> T declaredExplicitLengthUpperBound(T input) {
        return input;
    }

    void declaredExplicitLengthUpperBoundTest() {
        // implicit type invocation
        // T = UnknownUnits Object from argument w, and it is not a subtype of
        // Length
        //:: error: (type.argument.type.incompatible)
        uu = declaredExplicitLengthUpperBound(uu);
        declaredExplicitLengthUpperBound(null);

        m = declaredExplicitLengthUpperBound(m);
        //:: error: (type.argument.type.incompatible)
        declaredExplicitLengthUpperBound(s);

        // explicit type invocation
        // the explicitly declared T has to be a subtype of Length
        //:: error: (type.argument.type.incompatible)
        this.<@UnknownUnits Object> declaredExplicitLengthUpperBound(uu);

        // w is UnknownUnits Object, which is not a subtype of Length
        //:: error: (argument.type.incompatible)
        this.<@Length Object> declaredExplicitLengthUpperBound(uu);

        // T = Scalar Object is not a subtype of Length Object
        //:: error: (type.argument.type.incompatible)
        this.<Object> declaredExplicitLengthUpperBound(x);

        // UnitsBottom is a subtype of Length
        this.<@Length Object> declaredExplicitLengthUpperBound(null);

        // bottom is a subtype of m
        this.<@m Object> declaredExplicitLengthUpperBound(null);

        this.<@m Integer> declaredExplicitLengthUpperBound(m);
        // meter is a subtype of length
        this.<@Length Integer> declaredExplicitLengthUpperBound(m);
    }

    // we can declare the lower bound of a method type argument to any unit that
    // is a subtype of the upperbound
    <@m T> T meterLowerBoundBad(T input) {
        return input;
    }

    // to declare the lower bound, the upperbound must also be declared
    // lower bound is declared to be meters, accepted units of T are Unknown,
    // Length and Meter
    <@m T extends @UnknownUnits Object> T meterLowerBound(T input) {
        return input;
    }

    // lower bound is declared to be length, accepted units of T are Unknown,
    // Length
    <@Length T extends @UnknownUnits Object> T lengthLowerBound(T input) {
        return input;
    }

    void lowerBoundsTest() {
        @m Integer meter = new @m Integer(5);
        @Length Integer length = new @Length Integer(5);
        @s Integer second = new @s Integer(5);

        // implicit type invocation
        meterLowerBound(meter);
        // TODO: should be error
        meterLowerBound(null);
        meterLowerBound(length);
        // type parameter unit second is not a supertype of meter
        //:: error: (type.argument.type.incompatible)
        meterLowerBound(second);

        meter = meterLowerBound(meter);
        meter = meterLowerBound(new @UnitsBottom Integer(5));
        // TODO: should be error
        meter = meterLowerBound(null);

        length = meterLowerBound(length);

        // meter sets the type of T, and seconds is not a subtype of meter
        //:: error: (assignment.type.incompatible)
        meter = meterLowerBound(second);
        // type parameter unit second is not a supertype of meter
        //:: error: (type.argument.type.incompatible)
        second = meterLowerBound(second);
        // scalar is also not a supertype of meter
        //:: error: (type.argument.type.incompatible)
        x = meterLowerBound(x);

        uu = meterLowerBound(uu);
        // TODO: should be error
        uu = meterLowerBound(null);

        // explicit type invocation
        // all types involved are precisely @m
        this.<@m Object> meterLowerBound(meter);
        // this passes because @Length Object is a supertype of @m T
        this.<@Length Object> meterLowerBound(length);
        // TODO: should be error (currently checks that Bottom is a subtype of
        // Length only
        this.<@Length Object> meterLowerBound(null);

        // same for UnknownUnits
        this.<@UnknownUnits Object> meterLowerBound(length);
        this.<@UnknownUnits Object> meterLowerBound(meter);
        // TODO: should be error
        this.<@UnknownUnits Object> meterLowerBound(null);

        // this passes because @Unknown Object overrides @m T, and @s is a
        // subtype of @Unknown
        this.<@UnknownUnits Object> meterLowerBound(second);
        // while bottom is a subtype of Speed, Speed is not a supertype of m
        //:: error: (type.argument.type.incompatible)
        this.<@Speed Object> meterLowerBound(null);

        this.<@Length Object> lengthLowerBound(meter);
        // second is not a subtype of length
        //:: error: (argument.type.incompatible)
        this.<@Length Object> lengthLowerBound(second);
        // meter is not a supertype of length
        //:: error: (type.argument.type.incompatible)
        this.<@m Object> lengthLowerBound(meter);
        // scalar is not a supertype of length
        //:: error: (type.argument.type.incompatible)
        this.<Object> meterLowerBound(null);
    }

    // both the lower bound and the upperbound can be set to specific units
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
        // implicit type invocation
        // T = UnknownUnits Object from argument w, and it is not a subtype of
        // Length
        //:: error: (type.argument.type.incompatible)
        uu = declaredExplicitBounds(uu);
        // Scalar is not a subtype of Length
        //:: error: (type.argument.type.incompatible)
        x = declaredExplicitBounds(x);

        m = declaredExplicitBounds(m);

        // explicit type invocation
        // the explicitly declared T has to be a subtype of Length
        //:: error: (type.argument.type.incompatible)
        this.<@UnknownUnits Object> declaredExplicitBounds(uu);

        // the explicitly declared T has to be a supertype of Meter
        //:: error: (type.argument.type.incompatible)
        this.<@UnitsBottom Object> declaredExplicitBounds(null);

        this.<@m Object> declaredExplicitBounds(null);
        // TODO: should be error
        this.declaredExplicitBounds(null);

        this.<@m Integer> declaredExplicitBounds(m);
        // meter is a subtype of length
        this.<@Length Integer> declaredExplicitBounds(m);
    }
}
