package tests;

import java.io.File;
import java.util.Collection;

import org.checkerframework.framework.test.ParameterizedCheckerTest;
import org.junit.runners.Parameterized.Parameters;

public class UnitsQualPolyTest extends ParameterizedCheckerTest {

    public UnitsQualPolyTest(File testFile) {
        super(testFile,
                org.checkerframework.checker.experimental.units_qual_poly.UnitsChecker.class,
                "src",
                "-Anomsgtext");
    }

    @Parameters
    public static Collection<Object[]> data() {
        // return testFiles("units", "all-systems");
        return testFiles("units_qual_poly");
    }
}