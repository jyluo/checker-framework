package org.checkerframework.checker.experimental.units_qual;

import com.sun.source.tree.MethodInvocationTree;
import org.checkerframework.qualframework.base.CheckerAdapter;
import org.checkerframework.qualframework.base.TypecheckVisitorAdapter;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * The {@link org.checkerframework.common.basetype.BaseTypeVisitor} for the Units-Qual type system.
 *
 * @see org.checkerframework.checker.units.UnitsVisitor
 */

public class UnitsTypecheckVisitor extends TypecheckVisitorAdapter<Units> {

    public UnitsTypecheckVisitor(CheckerAdapter<Units> checker) {
        super(checker);
        
        ProcessingEnvironment env = checker.getProcessingEnvironment();
        
    }
    
    // TODO: 
    // in UnitsVisitor it only checked compound assignments
    
    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p){
        
        // TODO: apply Units type hierarchy here??
        return super.visitMethodInvocation(node, p);
    }
}

