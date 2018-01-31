package testlib.defaulting;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotationClassLoader;

public class DefaultingUpperBoundAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    public DefaultingUpperBoundAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        this.postInit();
    }

    @Override
    protected AnnotationClassLoader createAnnotationClassLoader() {
        // DefaultingUpperBoundChecker does not use a class loader
        return null;
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        return new HashSet<Class<? extends Annotation>>(
                Arrays.asList(
                        UpperBoundQual.UB_TOP.class,
                        UpperBoundQual.UB_EXPLICIT.class,
                        UpperBoundQual.UB_IMPLICIT.class,
                        UpperBoundQual.UB_BOTTOM.class));
    }
}
