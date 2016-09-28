import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;
import org.checkerframework.checker.units.qual.time.instant.*;

class AutoWidening {
    // This test case ensures that auto-widening of the number types pass if it
    // has a correct unit, and fails if it doesn't.
    void test() {
        @m byte meterByte = (byte) (100 * UnitsTools.m);
        @m short meterShort = meterByte;
        //:: error: (assignment.type.incompatible)
        @s short secondShort = meterByte;
        @m int meterInt = meterShort;
        @m long meterLong = meterInt;
        @m float meterFloat = meterLong;
        @m double meterDouble = meterFloat;
    }
}
