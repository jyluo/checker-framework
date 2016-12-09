import org.checkerframework.checker.units.qual.*;

class FormattingTestCase {
    //:: warning: (cast.unsafe)
    int x = (@m int) 5;
    //:: warning: (cast.unsafe)
    Integer y = (@m Integer) 5;
}
