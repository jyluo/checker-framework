package org.checkerframework.checker.experimental.units_qual_poly;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
 */

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

public class UnitsQualifiedRelationsDefault implements UnitsQualifiedRelations {

    private ProcessingEnvironment processingEnv;
    private Messager messager;

    @Override
    public UnitsQualifiedRelations init(ProcessingEnvironment env) {
        // TODO Auto-generated method stub
        processingEnv = env;
        messager = processingEnv.getMessager();

        return this;
    }

    @Override
    public /*@Nullable*/ Units multiplication(Units leftUnit, Units rightUnit) {
        // needs to handle the form of returnUnit = leftUnit * rightUnit
        // if unable to assign a unit, return null

        // we only handle the cases where both sides have units here,
        // the other cases are handled in the type factory
        if(leftUnit == null || rightUnit == null) {
            return null;
        }

        // messager.printMessage(Kind.NOTE, "urDef mul - left Unit: " + leftUnit + " right Unit: " + rightUnit);

        // multiplication of the same unit with itself => return that unit squared
        if(leftUnit.equals(rightUnit)) {
            if(leftUnit.equals(Units.km)) {
                return Units.km2;
            }
            else if(leftUnit.equals(Units.mm)) {
                return Units.mm2;
            }
            else if(leftUnit.equals(Units.m)) {
                return Units.m2;
            }
            // unrecognized base unit of multiplication
            else {
                return null;
            }
        }
        else if(hasUnitsPairOrderIgnored(leftUnit, rightUnit, Units.s, Units.mPERs)) {
            return Units.m;
        }
        else if(hasUnitsPairOrderIgnored(leftUnit, rightUnit, Units.s, Units.mPERs2)) {
            return Units.mPERs;
        }
        else if(hasUnitsPairOrderIgnored(leftUnit, rightUnit, Units.h, Units.kmPERh)) {
            return Units.km;
        }
        else {
            return null;
        }
    }

    private boolean hasUnitsPairOrderIgnored(Units left, Units right, Units a, Units b)
    {
        return (left.equals(a) && right.equals(b) ||
                left.equals(b) && right.equals(a) );
    }

    @Override
    public /*@Nullable*/ Units division(Units leftUnit, Units rightUnit) {
        // needs to handle the form of returnUnit = leftUnit / rightUnit
        // if unable to assign a unit, return null

        // we only handle the cases where both sides have units here,
        // the other cases are handled in the type factory
        if(leftUnit == null || rightUnit == null) {
            return null;
        }

        //messager.printMessage(Kind.NOTE, "UnitRelations div - left Unit: " + leftUnit + " right Unit: " + rightUnit);

        if(leftUnit.equals(Units.m) && rightUnit.equals(Units.s)) {
            return Units.mPERs;
        }
        else if(leftUnit.equals(Units.km) && rightUnit.equals(Units.h)) {
            return Units.kmPERh;
        }
        else if(leftUnit.equals(Units.m2) && rightUnit.equals(Units.m)) {
            return Units.m;
        }
        else if(leftUnit.equals(Units.km2) && rightUnit.equals(Units.km)) {
            return Units.km;
        }
        else if(leftUnit.equals(Units.mm2) && rightUnit.equals(Units.mm)) {
            return Units.mm;
        }
        else if(leftUnit.equals(Units.m) && rightUnit.equals(Units.mPERs)) {
            return Units.s;
        }
        else if(leftUnit.equals(Units.km) && rightUnit.equals(Units.kmPERh)) {
            return Units.h;
        }
        else if(leftUnit.equals(Units.mPERs) && rightUnit.equals(Units.s)) {
            return Units.mPERs2;
        }
        else if(leftUnit.equals(Units.mPERs) && rightUnit.equals(Units.mPERs2)) {
            return Units.s;
        }
        else
        {
            return null;
        }
    }

}
