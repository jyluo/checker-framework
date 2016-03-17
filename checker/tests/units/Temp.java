import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.nullness.qual.*;

import java.util.LinkedList;
import java.util.List;
//
//// Test 1:
//
//class ClassA{}
//
//class SubClassA extends ClassA{}
//
//class ClassB< TT, HH extends TT >{
//    public void mm( ClassB< TT, HH > input) {}
//}
//
//class SubClassB extends ClassB < ClassA, @m SubClassA > {}
//
//class MM{
//    public void mm2( ClassB< ClassA, ? > input) {}
//    <TTT extends @Scalar ClassA> void mm3( ClassB< ClassA, TTT > input) {}
//    
//}
//
//// scalar explicit upper
//// unknown implicit upper
//// try this




// Test 2:

class blah{
    // lowerbound is declared to Meters, upperbound is declared as Length.
    // T must be either Meters or Length and the type instantiated for T
    // from this.<T> must not clashes with the input's type
    <@m T extends @Length Object> T declaredExplicitBounds(T input) {
        return input;
    }

    void m(){
        @UnitsBottom Object b = new @UnitsBottom Object();
        // b is UnitsBottom which is a subtype of meter, therefore accepted as a type argument?
        b = this.<@UnitsBottom Object> declaredExplicitBounds(b);
        
        // if declared explicitly, it types, if inferred it doesn't. framework bug
    }
}

