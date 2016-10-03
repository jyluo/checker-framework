package tests;

import java.io.File;
import java.util.List;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

public class ZuluTest extends CheckerFrameworkPerDirectoryTest {

    public ZuluTest(List<File> testFiles) {
        super(
                testFiles,
                org.checkerframework.checker.zulu.ZuluChecker.class,
                "zulu",
                "-Anomsgtext");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"all-systems"};
        // return new String[] {"units", "all-systems"};
    }
}
