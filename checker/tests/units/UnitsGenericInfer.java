import org.checkerframework.checker.units.qual.*;

import java.util.LinkedList;
import java.util.List;

// Test 1:

class ClassA{}

class ClassB< TT, HH extends TT >{}

class MM{
    public void mm( ClassB< ClassA, ? > input) {}
}

// Test 2:

class ClassC<V extends ClassC<V>> {}

class ClassD extends ClassC<ClassD> {
    // inference of ? currently stuck, to be fixed in the future
    @SuppressWarnings({"units"})
    public ClassD(ClassE<ClassD, ?> input) {}
}

class ClassE<V extends ClassC<V>, S extends ClassE<V, S>> {}

// Test 3:

interface A<T> {
    public abstract int transformSuper(List<? super T> function);
    public abstract int transformExtend(List<? extends T> function);
}

// Test 4:

class MyClass<T extends String>{}

class XX{
    // extends bound higher than class declaration
    MyClass<? extends Object> x = new MyClass<String>();
}

// Test 5:

class TypeVarsArrays<T> {
    private T[] array;

    public void triggerBug(int index, T val) {
        array[index] = val;
        array[index] = null;
    }
}