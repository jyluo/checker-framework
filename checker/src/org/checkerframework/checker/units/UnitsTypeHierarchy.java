package org.checkerframework.checker.units;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.DefaultTypeHierarchy;

public class UnitsTypeHierarchy extends DefaultTypeHierarchy {
    public UnitsTypeHierarchy(BaseTypeChecker checker, UnitsAnnotatedTypeFactory factory) {
        // true as the last parameter allows covariant type arguments
        super(
                checker,
                factory.getQualifierHierarchy(),
                checker.hasOption("ignoreRawTypeArguments"),
                checker.hasOption("invariantArrays"),
                true);
    }
}
