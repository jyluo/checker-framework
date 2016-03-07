import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;
import org.checkerframework.checker.units.qual.time.instant.*;
import org.checkerframework.checker.units.UnitsTools;

import java.util.List;
import java.util.LinkedList;

public class GenericsTests {

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

    // lower bound is declared to be meters, accepted units of T are Unknown, Length and Meter
    <@m T> T meterLowerBound(T input) {
        return input;
    }

    // lower bound is declared to be length, accepted units of T are Unknown, Length
    <@Length T> T lengthLowerBound(T input) {
        return input;
    }

    void defaultImplicitBoundsTest() {
        Object x = null; // flow refined to UnitsBottom
        Number y = null; // flow refined to UnitsBottom
        @m Integer z = new @m Integer(5);

        // explicit type invocation
        this.<Object>defaultImplicitBounds(x);
        this.<Number>defaultImplicitBounds(y);
        // explicitly declaring T as Scalar Integer causes argument type incompatible error
        //:: error: (argument.type.incompatible)
        this.<Integer>defaultImplicitBounds(z);

        this.<@m Object>defaultImplicitBounds(x); // bottom is a subtype of m

        this.<@m Integer>defaultImplicitBounds(z);
        this.<@Length Integer>defaultImplicitBounds(z);
        // meter is not a subtype of scalar
        //:: error: (argument.type.incompatible)
        this.<Integer>defaultImplicitBounds(z);

        // implicit type invocation
        x = defaultImplicitBounds(x);
        y = defaultImplicitBounds(y);
        z = defaultImplicitBounds(z);
    }

    void lowerBoundsTest(){
        Object x = null; // flow refined to UnitsBottom
        Number y = null; // flow refined to UnitsBottom
        @m Integer meter = new @m Integer(5);
        @Length Integer length = new @Length Integer(5);
        @s Integer second = new @s Integer(5);

        // explicit type invocation
        // all types involved are precisely @m
        this.<@m Object>meterLowerBound(meter);
        // this passes because @Length Object is a supertype of @m T and bottom is a subtype of @Length
        this.<@Length Object>meterLowerBound(null);
        this.<@Length Object>meterLowerBound(length);
        // same for UnknownUnits
        this.<@UnknownUnits Object>meterLowerBound(null);
        this.<@UnknownUnits Object>meterLowerBound(length);
        this.<@UnknownUnits Object>meterLowerBound(meter);
        // this passes because @Unknown Object overrides @m T, and @s is a subtype of @Unknown
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

        // TODO: currently reports an assignment.type.incompatible error, shouldn't though
        meter = meterLowerBound(null);

        length = meterLowerBound(length);



        //        
        //        // meter sets the type of T, and seconds is not a subtype of meter
        //        //:: error: (assignment.type.incompatible)
        //        meter = meterLowerBound(second);
        //        // type parameter unit second is not a supertype of meter
        //        //:: error: (type.argument.type.incompatible)
        //        second = meterLowerBound(second);
        //        
        //        meter = lengthLowerBound(meter);
        //        length = lengthLowerBound(meter);
        //        
        //        x = meterLowerBound(x);
        //        x = meterLowerBound(null);
        //        y = meterLowerBound(y);
        //        y = meterLowerBound(null);
        //        

    }


    //
    //    void implicitUpperTest() {
    //        Object x = null;
    //        x = defaultImplicitBounds(x);
    //        x = meterLowerBound(x);
    //
    //        @UnknownUnits Object y = new @UnknownUnits Object();
    //        y = defaultImplicitBounds(y);
    //        y = meterLowerBound(y);
    //
    //        @m Object z = new @m Object();
    //        z = defaultImplicitBounds(z);
    //        z = meterLowerBound(z);
    //    }
    //
    //    <T extends Object> T methodDefaultExplicitUpper(T input) {
    //        return input;
    //    }
    //
    //    <@UnknownUnits T extends @UnknownUnits Object> T methodDeclaredExplicitUpper(T input) {
    //        return input;
    //    }
    //
    //    void explicitUpperTest() {
    //        // Scalar by default
    //        Object x = new Object();
    //        x = methodDefaultExplicitUpper(x);
    //        x = methodDeclaredExplicitUpper(x);
    //
    //        @UnknownUnits Object y = new @UnknownUnits Object();
    //        y = methodDefaultExplicitUpper(y);
    //        y = methodDeclaredExplicitUpper(y);
    //
    //        @m Object z = new @m Object();
    //        z = methodDefaultExplicitUpper(z);
    //        z = methodDeclaredExplicitUpper(z);
    //    }

    public class TypeVarsArrays<T> {
        private T[] array;

        public void triggerBug(int index, T val) {
            array[index] = val;
            array[index] = null;
        }
    }
}


