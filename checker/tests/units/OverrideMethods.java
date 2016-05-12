import org.checkerframework.checker.units.qual.*;

import java.util.Objects;
import java.util.Map;
import java.util.List;
import java.util.Collections;

public class OverrideMethods {
    @Override
    public String toString() {
        return "blah";
    }

    @Override
    public boolean equals(@UnknownUnits Object obj) {
        Objects.requireNonNull(obj);
        return super.equals(obj);
    }
}

class Something implements Comparable<Something> {
    @Override
    public int compareTo(Something arg0) {
        Objects.requireNonNull(arg0);
        return 0;
    }
}

class Test {
    void m() {
        OverrideMethods x = new OverrideMethods();
        x.toString();

        Object y = new Object();
        y.toString();

        @m Object z = new @m Object();
        z.toString();
    }
    
    void test2(Map<String, List<Integer>> x) {
        x.values().forEach(
                (c) -> Collections.sort((List<Integer>) List.class.cast(c))
                );
    }
}
