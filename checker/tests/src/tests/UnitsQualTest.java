package tests;

import java.io.File;
import java.util.Collection;

import org.checkerframework.framework.test.ParameterizedCheckerTest;
import org.junit.runners.Parameterized.Parameters;

public class UnitsQualTest extends ParameterizedCheckerTest {

    public UnitsQualTest(File testFile) {
        super(testFile,
                org.checkerframework.checker.experimental.units_qual.UnitsCheckerAdapter.class,
                "src",
                "-Anomsgtext");
    }

    @Parameters
    public static Collection<Object[]> data() {
        return testFiles("units", "all-systems");
    }
}