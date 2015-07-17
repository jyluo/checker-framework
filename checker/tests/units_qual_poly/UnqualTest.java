import org.checkerframework.checker.experimental.units_qual_poly.UnitsTools;
import org.checkerframework.checker.experimental.units_qual_poly.qual.*;

public class UnqualTest {
    //:: error: (assignment.type.incompatible)
    @kg int kg = 5;
    int nonkg = kg;

    @kg int alsokg = nonkg;
}
