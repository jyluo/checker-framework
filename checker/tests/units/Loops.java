
class Loops{
    void foreachLoopIndexComparison() {
        int [] x = new int[5];
        for(int i : x);
    }
    
    <T> void typeVarLoop(T[] x) {
        for(T i : x);
    }
}