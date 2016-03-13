import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.nullness.qual.*;



import java.util.LinkedList;
import java.util.List;

// Test 1:

// @UnknownUnits class X{}  // would pass
class ClassA{}

class ClassB< T, H extends  T >{}

class M{
    @SuppressWarnings({"units"})
    public void m( ClassB< ClassA, ? > input) {}
}

// Test 2:

class Test2{
    // lower bound is declared to be meters, accepted units of T are Unknown, Length and Meter
    // <@m T> T meterLowerBound(T input) {
    <T extends @m Object> T meterLowerBound(T input) {
        return input;
    }

    void x() {
        @m Integer meter = meterLowerBound(null);
    }
}

// Test 3:

class Test3{

    void m(){

        List<@UnknownUnits Number> unknownList = new LinkedList<Number>();
        List<@Length Number> lengthList = new LinkedList<@Length Number>();
    }

}

