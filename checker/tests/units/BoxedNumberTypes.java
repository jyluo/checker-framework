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

        mB.equals(mB);
        // :: error: (comparison.unit.mismatch)
        mB.equals(sB);

        mB.compareTo(mB);
        // :: error: (comparison.unit.mismatch)
        mB.compareTo(sB);

        Byte.compare(mb, mb);
        // :: error: (comparison.unit.mismatch)
        Byte.compare(mb, sb);

        mI = Byte.toUnsignedInt(mb);
        mL = Byte.toUnsignedLong(mb);
    }

    void testShort() {
        mS = Short.valueOf(ms);
        // mS = new Short(ms);

        mb = mS.byteValue();
        ms = mS.shortValue();
        mi = mS.intValue();
        ml = mS.longValue();
        mf = mS.floatValue();
        md = mS.doubleValue();

        // :: error: (assignment.type.incompatible)
        sS = Short.valueOf(ms);
        // :: error: (assignment.type.incompatible)
        sb = mS.byteValue();
        // :: error: (assignment.type.incompatible)
        ss = mS.shortValue();
        // :: error: (assignment.type.incompatible)
        si = mS.intValue();
        // :: error: (assignment.type.incompatible)
        sl = mS.longValue();
        // :: error: (assignment.type.incompatible)
        sf = mS.floatValue();
        // :: error: (assignment.type.incompatible)
        sd = mS.doubleValue();

        System.out.println(mS.toString());
        System.out.println(Short.toString(ms));
        System.out.println(mS.hashCode());
        System.out.println(Short.hashCode(ms));

        mS.equals(mS);
        // :: error: (comparison.unit.mismatch)
        mS.equals(sS);

        mS.compareTo(mS);
        // :: error: (comparison.unit.mismatch)
        mS.compareTo(sS);

        Short.compare(ms, ms);
        // :: error: (comparison.unit.mismatch)
        Short.compare(ms, ss);

        ms = Short.reverseBytes(ms);
        mI = Short.toUnsignedInt(ms);
        mL = Short.toUnsignedLong(ms);
    }

    void testInteger() {
        // autobox
        mI = mi;
        // autounbox
        mi = mI;

        mI = Integer.valueOf(mi);
        // mI = new Integer(mi);

        mb = mI.byteValue();
        ms = mI.shortValue();
        mi = mI.intValue();
        ml = mI.longValue();
        mf = mI.floatValue();
        md = mI.doubleValue();

        // :: error: (assignment.type.incompatible)
        sI = Integer.valueOf(mi);
        // :: error: (assignment.type.incompatible)
        sb = mI.byteValue();
        // :: error: (assignment.type.incompatible)
        ss = mI.shortValue();
        // :: error: (assignment.type.incompatible)
        si = mI.intValue();
        // :: error: (assignment.type.incompatible)
        sl = mI.longValue();
        // :: error: (assignment.type.incompatible)
        sf = mI.floatValue();
        // :: error: (assignment.type.incompatible)
        sd = mI.doubleValue();

        System.out.println(mI.toString());
        System.out.println(Integer.toString(mi));
        System.out.println(Integer.toString(mi, 30));
        System.out.println(Integer.toUnsignedString(mi));
        System.out.println(Integer.toUnsignedString(mi, 40));
        System.out.println(Integer.toHexString(mi));
        System.out.println(Integer.toOctalString(mi));
        System.out.println(Integer.toBinaryString(mi));
        System.out.println(mI.hashCode());
        System.out.println(Integer.hashCode(mi));

        mI.equals(mI);
        // :: error: (comparison.unit.mismatch)
        mI.equals(sI);

        mI.compareTo(mI);
        // :: error: (comparison.unit.mismatch)
        mI.compareTo(sI);

        Integer.compare(mi, mi);
        // :: error: (comparison.unit.mismatch)
        Integer.compare(mi, si);

        Integer.compareUnsigned(mi, mi);
        // :: error: (comparison.unit.mismatch)
        Integer.compareUnsigned(mi, si);

        mi = Integer.sum(mi, mi);
        // :: error: (assignment.type.incompatible)
        mi = Integer.sum(mi, si);

        @Dimensionless int a = Integer.divideUnsigned(mi, mi);
        // :: error: (assignment.type.incompatible)
        mi = Integer.divideUnsigned(mi, mi);

        mi = Integer.remainderUnsigned(mi, si);
        // :: error: (assignment.type.incompatible)
        si = Integer.remainderUnsigned(mi, mi);

        mi = Integer.highestOneBit(mi);
        mi = Integer.lowestOneBit(mi);

        @Dimensionless int x = Integer.numberOfLeadingZeros(mi);
        x = Integer.numberOfTrailingZeros(mi);
        x = Integer.bitCount(mi);
        x = Integer.signum(mi);

        mi = Integer.rotateLeft(mi, 10);
        mi = Integer.rotateRight(mi, 20);
        mi = Integer.reverse(mi);
        mi = Integer.reverseBytes(mi);
        mL = Integer.toUnsignedLong(mi);

        mi = Integer.max(mi, mi);
        // :: error: (assignment.type.incompatible)
        mi = Integer.max(mi, si);

        mi = Integer.min(mi, mi);
        // :: error: (assignment.type.incompatible)
        mi = Integer.min(mi, si);
    }
}
