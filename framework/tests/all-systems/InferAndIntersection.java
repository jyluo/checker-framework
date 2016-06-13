class InferAndIntersection {

    <T> void toInfer(Iterable<T> t) {}

    // In units checker, default type for parameters is @Scalar and default
    // extends bound is @UnknownUnits, thus this will clash 
    @SuppressWarnings("units")
    <U extends Object & Iterable<Object>> void context(U u) {
        toInfer(u);
    }
}
