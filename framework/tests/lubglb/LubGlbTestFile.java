package lubglb;

//@skip-tests


// Type hierarchy:
//    A       <-- @DefaultQualifierInHierarchy
//   / \
//  B   C       <-- B is default for parameters
//   \ / \
//    D   E
//     \ /
//      F
import lubglb.quals.*;

public class LubGlbTestFile {
    class I{}
    class J extends I{}
    class K{}
    class L{}
    class M{}
    class N{}

    class DefaultBounds<T extends I> {}
    
    class AnnotatedTopAndBottomBounds<@F T extends @A I> {}
    
    class AnnotatedTightBounds<@D T extends @C I> {}

    void method() {
        DefaultBounds<? extends I> x1;
        DefaultBounds<? super J> y1;
        
        DefaultBounds<@B ? extends @A I> x11;
        DefaultBounds<@A ? super @B J> y11;
        //:: error: (bound.type.incompatible)
        DefaultBounds<@B ? super @A J> y11bad;
        
        AnnotatedTopAndBottomBounds<? extends I> x2;
        AnnotatedTopAndBottomBounds<? super J> y2;
        
        AnnotatedTopAndBottomBounds<@B ? extends @A I> x21;
        AnnotatedTopAndBottomBounds<@A ? super @B J> y21;
        //:: error: (bound.type.incompatible)
        AnnotatedTopAndBottomBounds<@B ? super @A J> y21bad;
        
        // default type of I is @A, thus upper bound of wildcard set to @A, so error
        //:: error: (type.argument.type.incompatible)
        AnnotatedTightBounds<? extends I> x3;
        // default lower bound is @F, so Wildcard lower bound is set to @F, so error
        //:: error: (type.argument.type.incompatible)
        AnnotatedTightBounds<? super J> y3;
        
        AnnotatedTightBounds<@D ? extends @C I> x31;
        AnnotatedTightBounds<@C ? super @D J> y31;
        //:: error: (bound.type.incompatible)
        AnnotatedTightBounds<@D ? super @C J> y31bad;
        
    }
}

class Other{

    class ClassA{}

    class ClassB< T, H extends  T >{}

    class ClassC{
        public void m( ClassB< ClassA, ? > input) {}
    }

}
