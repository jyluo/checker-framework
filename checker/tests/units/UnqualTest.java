import org.checkerframework.checker.units.qual.*;

public class UnqualTest {
    //:: error: (assignment.type.incompatible)
    @kg int kg = 5;
    int nonkg = kg;

    @kg int alsokg = nonkg;
}
