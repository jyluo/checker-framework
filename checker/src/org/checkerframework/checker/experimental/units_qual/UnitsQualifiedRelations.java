package org.checkerframework.checker.experimental.units_qual;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

import javax.annotation.processing.ProcessingEnvironment;

//based upon UnitsRelations from the classic Units Checker
// based on units instead of annotated type mirrors

/**
 * Interface that is used to specify the relation between units.
 */
public interface UnitsQualifiedRelations {
    /**
     * Initialize the object. Needs to be called before any other method.
     *
     * @param env The ProcessingEnvironment to use.
     * @return A reference to "this".
     */
    UnitsQualifiedRelations init(ProcessingEnvironment env);

    /**
     * Called for the multiplication of type p1 and p2.
     *
     * @param p1 LHS in multiplication.
     * @param p2 RHS in multiplication.
     * @return The annotation to use for the result of the multiplication or
     *      null if no special relation is known.
     */
    /*@Nullable*/ Units multiplication(Units leftUnit, Units rightUnit);

    /**
     * Called for the division of type p1 and p2.
     *
     * @param p1 LHS in division.
     * @param p2 RHS in division.
     * @return The annotation to use for the result of the division or
     *      null if no special relation is known.
     */
    /*@Nullable*/ Units division(Units leftUnit, Units rightUnit);
}