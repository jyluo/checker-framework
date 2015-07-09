package org.checkerframework.checker.experimental.units_qual;


import javax.annotation.processing.ProcessingEnvironment;

import org.checkerframework.qualframework.base.QualifierHierarchy;

/**
 * QualifierHierarchy for the Units-Qual type system. The Hierarchy consists of
 * RegexTop, RegexBottom, RegexVal and PartialRegex.
 *
 * <ul>
 *   <li>RegexVal and PartialRegex are incomparable.</li>
 *   <li>A PartialRegex is a subtype of another PartialRegex if they have the same partial Units.</li>
 *   <li>A RegexVal is a subtype of another RegexVal with a smaller count.</li>
 * </ul>
 */

public class UnitsQualifierHierarchy implements QualifierHierarchy<Units>{

    private ProcessingEnvironment processingEnv;

    public UnitsQualifierHierarchy(ProcessingEnvironment pe){
        processingEnv = pe;
    }
    
    // TODO: in classic this is isSubtype(lhs, rhs), need to fix?
    
    // checks to see if left hand side is a subtype of right hand side
    // left == actual right == expected
    @Override
    public boolean isSubtype(Units subtype, Units supertype) {
        
        //processingEnv.getMessager().printMessage(Kind.NOTE, " subtype check actual : " + subtype + " expected : " + supertype);
        
        /* UnknownUnits is the top of the hierarchy */
        if (supertype.equals(Units.UnitsUnknown) ) {
            return true;
        }

        /* if the actual and expected qualifiers match, return true */
        if (subtype.equals(supertype)) {
            return true;
        }
        
        // if subtype is a subtype of supertype
        if(subtype.isSubType(supertype)) {
            return true;
        }
        
        // else
        return false;
    }


    @Override
    public Units leastUpperBound(Units a, Units b) {
        // return least upper bound of two qualifiers
        /* top of hierarchy */

        if(a.equals(Units.UnitsUnknown))
        {
            return Units.UnitsUnknown;
        }
        else
        {
            return b;
        }

        /*
        if(a == UnitsUnknown || b == UnitsUnknown) {
            return UnitsUnknown;

            // base SI Units
        } else if(a == g && b == g) {
            return g;
        } else if (a == m && b == m) {
            return m;
        }

        // default:
        return UnitsUnknown;
         */
    }

    @Override
    public Units greatestLowerBound(Units a, Units b) {
        // returns greatest lower bound of two qualifiers
        if (a.equals(Units.UnitsUnknown)){
            return b;
        } else {
            return a;
        }
    }

    @Override
    public Units getTop() {
        return Units.UnitsUnknown;
    }

    @Override
    public Units getBottom() {
        return Units.BOTTOM;
    }
}




