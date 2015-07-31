package org.checkerframework.checker.experimental.units_qual_poly;


import java.lang.annotation.Annotation;
import java.util.Set;

import javax.tools.Diagnostic;

import org.checkerframework.checker.experimental.units_qual_poly.Units;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.DefaultLocation;
import org.checkerframework.framework.util.defaults.QualifierDefaults;
import org.checkerframework.qualframework.base.CheckerAdapter;
import org.checkerframework.qualframework.poly.PolyQual.GroundQual;
import org.checkerframework.qualframework.poly.QualParams;

/**
 * {@link CheckerAdapter} for the Units-Qual-Param type system.
 */
public class UnitsChecker extends CheckerAdapter<QualParams<Units>> {

    public UnitsChecker() {
        super(new UnitsQualPolyChecker());
        
        //this.getTypeMirrorConverter().getQualifier(anno);
        
        //getTypeMirrorConverter().getAnnotation(
        //        underlying.getTypeFactory().getQualifierHierarchy().getBottom())
    }
    
    public Set<Class <? extends Annotation>> getAnnos() {
//        Set<Class <? extends Annotation>> qualAnos = super.getTypeFactory().getSupportedTypeQualifiers();
//        // this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Supported Annos: ");
//        
//        for(Class <? extends Annotation> anno : qualAnos) {
//            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Anno: " + anno);
//        }
//        
//        return qualAnos;
        return null;
    }

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new UnitsTypecheckVisitor((CheckerAdapter<QualParams<Units>>) this);
    }

    @Override
    public void setupDefaults(QualifierDefaults defaults) {
        
        // default qualifiers for different kinds of code elements
        
        // TODO: verify correctness of the default locations as well as the qualifiers to apply
        
        defaults.addAbsoluteDefault(
                getTypeMirrorConverter().getAnnotation(
                        new QualParams<>(new GroundQual<>(Units.BOTTOM))),
                DefaultLocation.LOWER_BOUNDS);

        defaults.addAbsoluteDefault(
                getTypeMirrorConverter().getAnnotation(
                        new QualParams<>(new GroundQual<>(Units.UNITSUNKNOWN))),
                DefaultLocation.LOCAL_VARIABLE);
    }

}
