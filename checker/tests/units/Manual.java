import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;

// Include all the examples from the manual here,
// to ensure they work as expected.
class Manual {
    void demo1() {
        @m int meters = 5 * UnitsTools.m;
        @s int secs = 2 * UnitsTools.s;
        @mPERs int speed = meters / secs;
    }
}
