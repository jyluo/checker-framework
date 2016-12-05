import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;
import org.checkerframework.checker.units.qual.time.instant.*;

class PrefixOutput {
    // This test is designed to deliberately output specific units qualifiers
    // to the console for visual inspection of the formatting of the prefixes,
    // namely that if the unit has no prefix (@m) or has the prefix of
    // Prefix.one then the visual output should be the same (simply @unit).
    // TODO(jyluo): integrate with test framework in a way that compares the
    // generated output messages to an expected output.
    void prefixOutputTest() {
        @m int x = 5 * UnitsTools.m;
        @m(Prefix.kilo) int y = 2 * UnitsTools.km;
        @m(Prefix.one) int z = 3 * UnitsTools.m;
        @km int y2 = 3 * UnitsTools.km;

        //:: error: (assignment.type.incompatible)
        y2 = z;
        //:: error: (assignment.type.incompatible)
        y2 = x;
        //:: error: (assignment.type.incompatible)
        y = z;
        //:: error: (assignment.type.incompatible)
        y = x;

        //:: error: (assignment.type.incompatible)
        y2 = x * x;
        //:: error: (assignment.type.incompatible)
        y2 = z * z;
    }
}
