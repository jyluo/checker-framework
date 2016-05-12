import org.checkerframework.checker.units.qual.*;

public class OverrideMethods {
    @Override
    public String toString() {
        return "blah";
    }

    @Override
    public boolean equals(@UnknownUnits Object obj) {
        // TODO Auto-generated method stub
        return super.equals(obj);
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
}
