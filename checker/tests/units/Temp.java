import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.nullness.qual.*;

import java.util.LinkedList;
import java.util.List;

// Test 1:

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
        //:: error: (assignment.type.incompatible)
        b = declaredExplicitBounds(b);
    }
}
