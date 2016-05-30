import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.*;
import org.checkerframework.checker.units.qual.time.instant.*;
import org.checkerframework.checker.units.UnitsTools;

public class BasicUnits {

    void AutoWidening() {
        @m byte meterByte = (byte) (100 * UnitsTools.m);
        @m short meterShort = meterByte;
        //:: error: (assignment.type.incompatible)
        @s short secondShort = meterByte;
        @m int meterInt = meterShort;
        @m long meterLong = meterInt;
        @m float meterFloat = meterLong;
        @m double meterDouble = meterFloat;
    }

    void demo() {
        //:: error: (assignment.type.incompatible)
        @m int merr = 5;

        @m int m = 5 * UnitsTools.m;
        @s int s = 9 * UnitsTools.s;

        //:: error: (assignment.type.incompatible)
        @km int kmerr = 10;
        @km int km = 10 * UnitsTools.km;

        // this is allowed, unqualified is a supertype of all units
        int bad = m / s;

        @mPERs int good = m / s;

        //:: error: (assignment.type.incompatible)
        @mPERs int b1 = s / m;

        //:: error: (assignment.type.incompatible)
        @mPERs int b2 = m * s;

        @mPERs2 int goodaccel = m / s / s;

        //:: error: (assignment.type.incompatible)
        @mPERs2 int badaccel1 = s / m / s;

        //:: error: (assignment.type.incompatible)
        @mPERs2 int badaccel2 = s / s / m;

        //:: error: (assignment.type.incompatible)
        @mPERs2 int badaccel3 = s * s / m;

        //:: error: (assignment.type.incompatible)
        @mPERs2 int badaccel4 = m * s * s;

        @Area int ae = m * m;
        @m2 int gae = m * m;

        //:: error: (assignment.type.incompatible)
        @Area int bae = m * m * m;

        //:: error: (assignment.type.incompatible)
        @km2 int bae1 = m * m;

        @rad double rad = 20.0d * UnitsTools.rad;
        @deg double deg = 30.0d * UnitsTools.deg;

        @deg double rToD1 = UnitsTools.toDegrees(rad);
        //:: error: (argument.type.incompatible)
        @deg double rToD2 = UnitsTools.toDegrees(deg);
        //:: error: (assignment.type.incompatible)
        @rad double rToD3 = UnitsTools.toDegrees(rad);

        @rad double dToR1 = UnitsTools.toRadians(deg);
        //:: error: (argument.type.incompatible)
        @rad double rToR2 = UnitsTools.toRadians(rad);
        //:: error: (assignment.type.incompatible)
        @deg double rToR3 = UnitsTools.toRadians(deg);

        // speed conversion
        @mPERs int mPs = 30 * UnitsTools.mPERs;
        @kmPERh int kmPhr = 20 * UnitsTools.kmPERh;

        @kmPERh int kmPhrRes = (int) UnitsTools.fromMeterPerSecondToKiloMeterPerHour(mPs);
        @mPERs int mPsRes = (int) UnitsTools.fromKiloMeterPerHourToMeterPerSecond(kmPhr);

        //:: error: (assignment.type.incompatible)
        @mPERs int mPsResBad = (int) UnitsTools.fromMeterPerSecondToKiloMeterPerHour(mPs);
        //:: error: (assignment.type.incompatible)
        @kmPERh int kmPhrResBad = (int) UnitsTools.fromKiloMeterPerHourToMeterPerSecond(kmPhr);

        // speeds
        @km int kilometers = 10 * UnitsTools.km;
        @h int hours = UnitsTools.h;
        @kmPERh int speed = kilometers / hours;

        // TimePoint
        @TimeInstant int aTimePt = 5 * UnitsTools.CALmin;
        @TimeInstant int bTimePt = 5 * UnitsTools.CALh;

        aTimePt = aTimePt % 5;
        bTimePt = bTimePt % speed;

        // Addition/substraction only accepts another @kmPERh value
        //:: error: (assignment.type.incompatible)
        speed = speed + 5;
        speed = speed + speed;
        speed = speed - speed;

        // Multiplication/division with an unqualified type is allowed
        speed = speed * 2;
        speed = speed / 2;
    }

    void prefixOutputTest() {
        @m int x = 5 * UnitsTools.m;
        @m(Prefix.kilo) int y = 2 * UnitsTools.km;
        @m(Prefix.one) int z = 3 * UnitsTools.m;
        @km int y2 = 3 * UnitsTools.km;

        //:: error: (assignment.type.incompatible)
        y2 = z;
        //:: error: (assignment.type.incompatible)
        y2 = x;
        //:: error: (assignment.type.incompatible)
        y = z;
        //:: error: (assignment.type.incompatible)
        y = x;

        //:: error: (assignment.type.incompatible)
        y2 = x * x;
        //:: error: (assignment.type.incompatible)
        y2 = z * z;
    }
}