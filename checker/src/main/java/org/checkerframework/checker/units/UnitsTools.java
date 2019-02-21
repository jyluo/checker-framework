package org.checkerframework.checker.units;

import org.checkerframework.checker.units.qual.*;

/** Utility methods to generate annotated types and to convert between them. */
@SuppressWarnings({"units", "checkstyle:constantname"})
public class UnitsTools {
    // Acceleration
    public static final @mPERs2 int mPERs2 = 1;

    // Angle
    public static final @rad double rad = 1;
    public static final @deg double deg = 1;

    public static @rad double toRadians(@deg double angdeg) {
        return Math.toRadians(angdeg);
    }

    public static @deg double toDegrees(@rad double angrad) {
        return Math.toDegrees(angrad);
    }

    // Area
    public static final @mm2 int mm2 = 1;
    public static final @m2 int m2 = 1;
    public static final @km2 int km2 = 1;

    // Current
    public static final @A int A = 1;

    // Luminance
    public static final @cd int cd = 1;

    // Lengths
    public static final @mm int mm = 1;
    public static final @m int m = 1;
    public static final @km int km = 1;

    public static @m int fromMilliMeterToMeter(@mm int mm) {
        return mm / 1000;
    }

    public static @mm int fromMeterToMilliMeter(@m int m) {
        return m * 1000;
    }

    public static @km int fromMeterToKiloMeter(@m int m) {
        return m / 1000;
    }

    public static @m int fromKiloMeterToMeter(@km int km) {
        return km * 1000;
    }

    // Mass
    public static final @g int g = 1;
    public static final @kg int kg = 1;

    public static @kg int fromGramToKiloGram(@g int g) {
        return g / 1000;
    }

    public static @g int fromKiloGramToGram(@kg int kg) {
        return kg * 1000;
    }

    // Speed
    public static final @mPERs int mPERs = 1;
    public static final @kmPERh int kmPERh = 1;

    public static @kmPERh double fromMeterPerSecondToKiloMeterPerHour(@mPERs double mps) {
        return mps * 3.6d;
    }

    public static @mPERs double fromKiloMeterPerHourToMeterPerSecond(@kmPERh double kmph) {
        return kmph / 3.6d;
    }

    // Substance
    public static final @mol int mol = 1;

    // Temperature
    public static final @K int K = 1;
    public static final @C int C = 1;

    public static @C int fromKelvinToCelsius(@K int k) {
        return k - (int) 273.15;
    }

    public static @K int fromCelsiusToKelvin(@C int c) {
        return c + (int) 273.15;
    }

    // Time
    public static final @ns int ns = 1;
    public static final @us int us = 1;
    public static final @ms int ms = 1;
    public static final @s int s = 1;
    public static final @min int min = 1;
    public static final @h int h = 1;
    public static final @day int day = 1;

    public static @min int fromSecondToMinute(@s int s) {
        return s / 60;
    }

    public static @s int fromMinuteToSecond(@min int min) {
        return min * 60;
    }

    public static @h int fromMinuteToHour(@min int min) {
        return min / 60;
    }

    public static @min int fromHourToMinute(@h int h) {
        return h * 60;
    }
}
