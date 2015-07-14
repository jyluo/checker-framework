package org.checkerframework.checker.experimental.units_qual_poly;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

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

    private final ProcessingEnvironment processingEnv;

    public UnitsQualifierHierarchy(ProcessingEnvironment pe){
        processingEnv = pe;
    }
    
    public UnitsQualifierHierarchy(){
        processingEnv = null;
    }

    // TODO: in classic this is isSubtype(lhs, rhs), need to fix?
    // TODO: match classic logic?

    // checks to see if left hand side is a sub type of right hand side
    // left == actual right == expected
    @Override
    public boolean isSubtype(Units subtype, Units supertype) {
        
        //if(processingEnv != null) {
        //processingEnv.getMessager().printMessage(Kind.NOTE, " subtype check actual : " + subtype + " expected : " + supertype);
        //}
        
        // if the actual and expected qualifiers match, return true
        if(subtype.equals(supertype)) return true;

        // if subtype is a sub type of supertype
        if(subtype.isSubType(supertype)) return true;

        // else by default return false
        return false;
    }


    @Override
    public Units leastUpperBound(Units a, Units b) {
        // return least upper bound of two qualifiers

        // top of hierarchy
        if(a.equals(this.getTop()) || b.equals(this.getTop())) {
            return this.getTop();
        }
        // units are the same
        else if(a.equals(b)) {
            return a;
        }
        else
        {
            // construct two stacks tracing each of the Unit's super types until it reaches Top
            Stack<Units> aHierarchyPath = new Stack<Units>();
            Stack<Units> bHierarchyPath = new Stack<Units>();

            Units currentA = a;
            Units currentB = b;

            while(currentA != null && !(currentA.equals(this.getTop())))
            {
                aHierarchyPath.push(currentA);
                currentA = currentA.getSuperType();
            }

            while(currentB != null && !(currentB.equals(this.getTop())))
            {
                bHierarchyPath.push(currentB);
                currentB = currentB.getSuperType();
            }

            // pop the stacks in reverse until they don't match anymore, that will be our least upper bound
            Units lub = this.getTop();
            boolean different = false;
            while(!different)
            {
                Units aPop = aHierarchyPath.pop();
                Units bPop = bHierarchyPath.pop();

                if(aPop.equals(bPop))
                {
                    lub = aPop;
                }
                else
                {
                    return lub;
                }
            }
        }

        return this.getTop();
    }

    // TODO: check to see if alias units are handled
    @Override
    public Units greatestLowerBound(Units a, Units b) {
        // returns greatest lower bound of two qualifiers
        // bottom of hierarchy
        if(a.equals(this.getBottom()) || b.equals(this.getBottom())) {
            return this.getBottom();
        }
        // units are the same
        else if(a.equals(b)) {
            return a;
        }
        else {
            return this.getBottom();
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




