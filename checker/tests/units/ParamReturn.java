import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;
import org.checkerframework.checker.units.qual.time.instant.*;
import org.checkerframework.checker.units.UnitsTools;
import java.util.List;
import java.util.LinkedList;

public class ParamReturn {
    @m int meter = 20 * UnitsTools.m;
    @s int second = 30 * UnitsTools.s;

    void voidReturn() {
        return;
    }

    Object nullObjReturn() {
        return null;
    }

    Object objReturn() {
        return new Object();
    }

    int intScalarReturn() {
        return 5;
    }

    int intObjReturn() {
        return new Integer(5);
    }

    int[] intArrReturn() {
        return new int[5];
    }

    int scalarParamScalarReturn(int x) {
        return x + x;
    }

    int scalarParamScalarReturnBad(int x) {
        // expected to fail, can't pass a meter out unless return type is annotated
        //:: error: (return.type.incompatible)
        return x * meter;
    }

    @m int scalarParamMeterReturn(int x) {
        // expected to pass
        return x * meter;
    }

    @m int scalarParamMeterReturnBad(int x) {
        // expected to fail as we are returning a second where a meter is expected
        //:: error: (return.type.incompatible)
        return x * second;
    }

    void methodAcceptingScalarObjects(Integer x) {
        return;
    }

    void methodAcceptingMeterObjects(@m Integer x) {
        return;
    }

    void paramTest(@UnknownUnits int y) {
        // pass scalar into scalar
        scalarParamScalarReturn(5);

        // unknown local variable refined into scalar
        int x = 5;
        // pass scalar into scalar
        scalarParamScalarReturn(x);

        // pass unknown into scalar
        //:: error: (argument.type.incompatible)
        scalarParamScalarReturn(y);

        // unknown parameter refined into scalar
        y = 5;
        scalarParamScalarReturn(y);

        // scalar type converted to Unknown and passed into scalar
        scalarParamScalarReturn( (@UnknownUnits int) 5 );

        // scalar type converted to Unknown and passed into scalar
        scalarParamScalarReturn( (@UnknownUnits int) (5 + 30 + 100) );

        // 1 equation value is (scalar converted to Unknown), the result of the additions is Unknown and passed into scalar
        scalarParamScalarReturn( (@UnknownUnits int) 5 + 30 + 100 );
    }

    void methodCalls() {
        // assigning meter into unknown, x becomes meter
        int x = scalarParamMeterReturn(5) + meter;

        // assigning unknown into scalar should fail
        //:: error: (assignment.type.incompatible)
        @Scalar int xBad = scalarParamMeterReturn(5) + second;

        // assigning meter into scalar should fail
        //:: error: (assignment.type.incompatible)
        @Scalar int y = scalarParamMeterReturn(5) + meter;

        // pass
        @Scalar int a = scalarParamScalarReturn(5);

        int unknownPrimitiveInt = (@UnknownUnits int) 5;
        //:: error: (argument.type.incompatible)
        @Scalar int b = scalarParamScalarReturn(unknownPrimitiveInt);

        // References are by default UnknownUnits
        // Objects are by default Scalar
        Integer unknownInteger = new @UnknownUnits Integer(5);
        // default receiver is Scalar, but we allow it to be invoked on UnknownUnits receivers in ATF
        unknownInteger.toString();

        //:: error: (argument.type.incompatible)
        methodAcceptingScalarObjects(unknownInteger);
        methodAcceptingMeterObjects(new @m Integer(30));
        //:: error: (argument.type.incompatible)
        methodAcceptingMeterObjects(unknownInteger);

        Integer scalarInteger = new Integer(30);
        scalarInteger.toString();
        methodAcceptingScalarObjects(scalarInteger);
        //:: error: (argument.type.incompatible)
        methodAcceptingMeterObjects(scalarInteger);

        methodAcceptingScalarObjects(null);
        methodAcceptingMeterObjects(null);
    }

    @Scalar Object methodReturningUnknownObject(@UnknownUnits Object x) {
        //:: error: (return.type.incompatible)
        return x;
    }

    void objectParameterTest(@m Object x, @s Object y) {
        //:: error: (operands.unit.mismatch)
        if(x == y);
    }
}
