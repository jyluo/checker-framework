import org.checkerframework.checker.units.qual.*;

public class BoxedNumberTypes {
    @m Number mN;
    @s Number sN;

    @m Byte mB;
    @m Short mS;
    @m Integer mI;
    @m Long mL;
    @m Float mF;
    @m Double mD;

    @s Byte sB;
    @s Short sS;
    @s Integer sI;
    @s Long sL;
    @s Float sF;
    @s Double sD;

    @m byte mb;
    @m short ms;
    @m int mi;
    @m long ml;
    @m float mf;
    @m double md;

    @s byte sb;
    @s short ss;
    @s int si;
    @s long sl;
    @s float sf;
    @s double sd;

    void testNumber() {
        mb = mN.byteValue();
        ms = mN.shortValue();
        mi = mN.intValue();
        ml = mN.longValue();
        mf = mN.floatValue();
        md = mN.doubleValue();

        // :: error: (assignment.type.incompatible)
        sb = mN.byteValue();
        // :: error: (assignment.type.incompatible)
        ss = mN.shortValue();
        // :: error: (assignment.type.incompatible)
        si = mN.intValue();
        // :: error: (assignment.type.incompatible)
        sl = mN.longValue();
        // :: error: (assignment.type.incompatible)
        sf = mN.floatValue();
        // :: error: (assignment.type.incompatible)
        sd = mN.doubleValue();
    }

    // blocked by this in stubs
    private class CustomByte {
        @PolyUnit CustomByte(@PolyUnit byte val) {}
    }

    void testByte() {
        mB = Byte.valueOf(mb);
        // mB = new Byte(mb);

        @m CustomByte cb = new CustomByte(mb);

        mb = mB.byteValue();
        ms = mB.shortValue();
        mi = mB.intValue();
        ml = mB.longValue();
        mf = mB.floatValue();
        md = mB.doubleValue();

        // :: error: (assignment.type.incompatible)
        sB = Byte.valueOf(mb);
        // :: error: (assignment.type.incompatible)
        sb = mB.byteValue();
        // :: error: (assignment.type.incompatible)
        ss = mB.shortValue();
        // :: error: (assignment.type.incompatible)
        si = mB.intValue();
        // :: error: (assignment.type.incompatible)
        sl = mB.longValue();
        // :: error: (assignment.type.incompatible)
        sf = mB.floatValue();
        // :: error: (assignment.type.incompatible)
        sd = mB.doubleValue();

        System.out.println(mB.toString());
        System.out.println(Byte.toString(mb));
        System.out.println(mB.hashCode());
        System.out.println(Byte.hashCode(mb));

        mB.compareTo(mB);
        // :: error: (comparison.unit.mismatch)
        mB.compareTo(sB);

        mI = Byte.toUnsignedInt(mb);
        mL = Byte.toUnsignedLong(mb);
    }
}
