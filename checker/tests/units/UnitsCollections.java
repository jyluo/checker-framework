import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;

import java.lang.Iterable;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
class CollectionsTests {

    // java.lang.Iterable tests
    class MyCollection<E extends @UnknownUnits Object> implements Iterable<E> {
        public Iterator<E> iterator() {
            return new MyIterator<E>();
        }
    }

    class MyIterator<T extends @UnknownUnits Object> implements Iterator<T> {
        // dummy return values for testing purposes
        public boolean hasNext() {
            return true;
        }

        public T next() {
            return null;
        }

        public void remove() {
        }
    }

    void customIterableTest() {
        MyCollection<@Length Integer> collection = new MyCollection<@Length Integer>();

        for (@Length Integer len : collection) {
            // do nothing
        }

        //:: error: (enhancedfor.type.incompatible)
        for (@m Integer meter : collection) {

        }
    }

    void iteratorTest() {
        Vector<@Length Integer> v = new Vector<@Length Integer>();

        Iterator<@Length Integer> itrLength = v.iterator();
        Iterator<@UnknownUnits Integer> itrUU = v.iterator();
        //:: error: (assignment.type.incompatible)
        Iterator<@m Integer> itrMeter = v.iterator();
    }

    void vectorTest(){
        @Length Integer e1 = new @Length Integer(5);
        @m Integer e2 = new @m Integer(20);
        @s Integer e3 = new @s Integer(30);

        Vector<@Length Integer> v = new Vector<@Length Integer>();
        v.add(e1);
        v.add(e2);
        //:: error: (argument.type.incompatible)
        v.add(e3);

        v.contains(e1);
        v.contains(e2);
        v.contains(e3);

        v.remove(e1);
        v.remove(e2);
        v.remove(e3);

        //:: error: (assignment.type.incompatible)
        Enumeration<@m Integer> en = v.elements();
        // okay only because of the forced type on en
        @m Integer i = en.nextElement();

        //:: error: (assignment.type.incompatible)
        i = v.elements().nextElement();
        // test toArray as well

        // Cloning constructors
        Vector<@Length Integer> v2 = new Vector(v);
        Vector<@Length Integer> v2ok = new Vector<@Length Integer>(v);
        // By default, type arguments have the unit of @Scalar
        //:: error: (argument.type.incompatible)
        Vector<Integer> v2Scalar = new Vector(v);
        // The only reference that can accept the cloning constructor without
        // an explicity type argument is if it has @UnknownUnits as the unit of
        // each element in the collection
        Vector<@UnknownUnits Integer> v2Top = new Vector(v);
        // downcasting length to meter is also an error
        //:: error: (argument.type.incompatible)
        Vector<@m Integer> v3 = new Vector<@m Integer>(v);

        // array copy
        @Length Integer[] intLengthArr = new @Length Integer[10];
        @Area Integer[] intAreaArr = new @Area Integer[10];

        v.copyInto(intLengthArr);
        // see BaseTypeVisitor
        //:: error: (vector.copyinto.type.incompatible)
        v.copyInto(intAreaArr);

        intLengthArr = v.toArray(intLengthArr);
        // TODO: should also be an error like above
        intAreaArr = v.toArray(intAreaArr);

        // future TODO: this should copy the unit within the type parameter of Vector, and not be an error
        //:: error: (assignment.type.incompatible)
        @Length Object[] objArr = v.toArray();

        // clone retains the unit of the whole vector object 
        //:: error: (assignment.type.incompatible)
        @Length Object vClone = v.clone();

        v.containsAll(v);
        v.addAll(v);
        v.retainAll(v);
        v.removeAll(v);

        List<@Length Integer> l = v.subList(0, v.size());
        // can't assign the list to a downcasted list reference
        //:: error: (assignment.type.incompatible)
        List<@m Integer> lBad = v.subList(0, v.size());

        Iterator<@Length Integer> itr = v.iterator();

        // can't assign the iterator to a downcasted iterator reference
        //:: error: (assignment.type.incompatible)
        Iterator<@m Integer> itrBad = v.iterator();
    }

    void setTest(){
        Set<@Length Integer> set = new HashSet<@Length Integer>();

        // TODO move to Collections test
        Set<@m Integer> emptySet = Collections.EMPTY_SET;
        emptySet = Collections.emptySet();


    }

    void listTest(){

    }

    void weakHashMapTest(){
        @m Integer key = new @m Integer(5);
        Integer key2 = new Integer(39);
        @m2 Double val = new @m2 Double(23.5);

        WeakHashMap<@Length Integer, @Area Double> whm = new WeakHashMap<@Length Integer, @Area Double>();

        //:: error: (assignment.type.incompatible)
        whm = new WeakHashMap(whm);

        whm = new WeakHashMap<@Length Integer, @Area Double>(whm);

        WeakHashMap<@UnknownUnits Integer, @UnknownUnits Double> whm2 = new WeakHashMap(whm);

        whm.put(key, val);
        //:: error: (argument.type.incompatible)
        whm.put(key2, val);

        @Area Double value = whm.get(key);
        //:: error: (assignment.type.incompatible)
        @Temperature Double value2 = whm.get(key2);

        whm.containsKey(key);
        whm.containsKey(key2);

        whm.putAll(whm);
        // cannot put the collection of unknown Integers and Doubles into a
        // weak hash map of length Integers and area Doubles
        //:: error: (argument.type.incompatible)
        whm.putAll(whm2);

        whm.remove(key);

        whm.containsValue(val);

        Set<@Length Integer> keySet = whm.keySet();
        //:: error: (assignment.type.incompatible)
        Set<Integer> keySetBad = whm.keySet();

        // TODO
        //:: error: (assignment.type.incompatible)
        Collection<Double> valuesBad = whm.values();

        // TODO
        //:: error: (assignment.type.incompatible)
        Set<Entry<Integer, Double>> entrySetBad = whm.entrySet();
    }

}
