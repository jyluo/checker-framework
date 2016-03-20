import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.nullness.qual.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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


interface A<T> {        // unknown T
    // ? extends @Scalar Object super T extends @UnknownUnits Object
    public abstract int transform(List<? super T> function);
}


abstract class Ordering<T> implements Comparator<T> {
    //                                           ^
    // found   : T extends @UnknownUnits Object
    // required: @Scalar Object
}

// Test x
final class MissingBoundAnnotations {
    @SuppressWarnings({"nullness:type.argument.type.incompatible", "javari:type.argument.type.incompatible"})
    public static <K extends Comparable<? super K>,V> Collection<K> sortedKeySet(Map<K,V> m) {
        ArrayList<K> theKeys = new ArrayList<K>(m.keySet());
        Collections.sort(theKeys);
        return theKeys;
    }
}




/*
 * /home/jeff/workspace/jsr308/checker-framework/checker/tests/all-systems/MissingBoundAnnotations.java:5: error: [type.argument.type.incompatible] incompatible types in type argument.
  public static <K extends Comparable<? super K>,V> Collection<K> sortedKeySet(Map<K,V> m) {
                                                                                     ^
  found   : V extends @UnknownUnits Object
  required: @Scalar Object
1 error

 */


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
        //:: error: (type.argument.type.incompatible)
        b = this.<@UnitsBottom Object> declaredExplicitBounds(b);

        // if declared explicitly, it types, if inferred it doesn't. framework bug
    }
    
    private <T> void addToBindingList(Map<T, List<String>> map, T key, String value) {}

}

