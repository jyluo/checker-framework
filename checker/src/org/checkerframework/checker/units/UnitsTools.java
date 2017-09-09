package org.checkerframework.checker.units;

import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.s;

/** Utility methods to generate annotated types and to convert between them. */
@SuppressWarnings("units")

// TODO: add fromTo methods for all useful unit combinations.
public class UnitsTools {

    // Lengths
    public static final @m int m = 1;

    // Time
    public static final @s int s = 1;
}
