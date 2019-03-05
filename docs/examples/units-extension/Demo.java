import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.s;
import qual.Frequency;
import qual.Hz;
import qual.kHz;

class UnitsExtensionDemo {
    @Hz int frq;

    void bad() {
        // Error! Unqualified value assigned to a @Hz value.
        // :: error: (assignment.type.incompatible)
        frq = 5;

        // suppress all warnings issued by the units checker for the d1
        // assignment statement
        @SuppressWarnings("units")
        @Hz int d1 = 9;

        // specifically suppress warnings related to any frequency units for the
        // d2 assigment statement
        @SuppressWarnings("frequency")
        @Hz int d2 = 10;
    }

    // specifically suppresses warnings for the hz annotation for the toHz
    // method
    @SuppressWarnings("hz")
    static @Hz int toHz(int hz) {
        return hz;
    }

    // public static @K int fromCelsiusToKelvin(@C int c) {
    // return c + (int) 273.15;
    // }
    //
    // public static @C int fromKelvinToCelsius(@K int k) {
    // return k - (int) 273.15;
    // }
    //
    // public static @kg int fromGramToKiloGram(@g int g) {
    // return g / 1000;
    // }
    //
    // public static @g int fromKiloGramToGram(@kg int kg) {
    // return kg * 1000;
    // }
    //
    // public static @min int fromHourToMinute(@h int h) {
    // return h * 60;
    // }
    //
    // public static @mPERs double fromKiloMeterPerHourToMeterPerSecond(@kmPERh double kmph) {
    // return kmph / 3.6d;
    // }
    //
    // public static @m int fromKiloMeterToMeter(@km int km) {
    // return km * 1000;
    // }
    // public static @kmPERh double fromMeterPerSecondToKiloMeterPerHour(@mPERs double mps) {
    // return mps * 3.6d;
    // }
    // public static @km int fromMeterToKiloMeter(@m int m) {
    // return m / 1000;
    // }
    // public static @mm int fromMeterToMilliMeter(@m int m) {
    // return m * 1000;
    // }
    // public static @m int fromMilliMeterToMeter(@mm int mm) {
    // return mm / 1000;
    // }
    // public static @h int fromMinuteToHour(@min int min) {
    // return min / 60;
    // }
    //
    // public static @s int fromMinuteToSecond(@min int min) {
    // return min * 60;
    // }
    //
    // public static @min int fromSecondToMinute(@s int s) {
    // return s / 60;
    // }
    //
    // public static @deg double toDegrees(@rad double angrad) {
    // return Math.toDegrees(angrad);
    // }
    //
    // public static @rad double toRadians(@deg double angdeg) {
    // return Math.toRadians(angdeg);
    // }

    @deg double rToD1 = UnitsTools.toDegrees(rad);
    // :: error: (argument.type.incompatible)
    @deg double rToD2 = UnitsTools.toDegrees(deg);
    // :: error: (assignment.type.incompatible)
    @rad double rToD3 = UnitsTools.toDegrees(rad);

    @rad double dToR1 = UnitsTools.toRadians(deg);
    // :: error: (argument.type.incompatible)
    @rad double rToR2 = UnitsTools.toRadians(rad);
    // :: error: (assignment.type.incompatible)
    @deg double rToR3 = UnitsTools.toRadians(deg);

    void good() {
        frq = toHz(9);

        @s double time = 5 * UnitsTools.s;
        @Hz double freq2 = 20 / time;
    }

    void auto(@s int time) {
        // The @Hz annotation is automatically added to the result
        // of the division, because we provide class FrequencyRelations.
        frq = 99 / time;
    }

    public static void main(String[] args) {
        @Hz int hertz = toHz(20);
        @s int seconds = 5 * UnitsTools.s;

        @SuppressWarnings("units")
        @s(Prefix.milli) int millisec = 10;

        @SuppressWarnings("hz")
        @kHz int kilohertz = 30;

        @Hz int resultHz = hertz + 20 / seconds;
        System.out.println(resultHz);

        @kHz int resultkHz = kilohertz + 50 / millisec;
        System.out.println(resultkHz);

        // this demonstrates the type hierarchy resolution: the common supertype
        // of Hz and kHz is Frequency, so this statement will pass
        @Frequency int okTernaryAssign = seconds > 10 ? hertz : kilohertz;

        // on the other hand, this statement expects the right hand side to be a
        // Hz, so it will fail
        // :: error: (assignment.type.incompatible)
        @Hz int badTernaryAssign = seconds > 10 ? hertz : kilohertz;
    }
}
