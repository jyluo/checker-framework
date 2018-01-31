package testlib.supportedquals;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotationClassLoader;
import testlib.supportedquals.qual.BottomQualifier;
import testlib.supportedquals.qual.Qualifier;

/**
 * Tests that annotations that have @Target(TYPE_USE, OTHER) (where OTHER is not TYPE_PARAMETER) may
 * be in the qual package so long as {@link BaseAnnotatedTypeFactory#createSupportedTypeQualifiers}
 * is overridden.
 */
public class SupportedQualsChecker extends BaseTypeChecker {
    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new BaseTypeVisitor<SupportedQualsAnnotatedTypeFactory>(this) {
            @Override
            protected SupportedQualsAnnotatedTypeFactory createTypeFactory() {
                return new SupportedQualsAnnotatedTypeFactory(checker);
            }
        };
    }

    class SupportedQualsAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
        public SupportedQualsAnnotatedTypeFactory(BaseTypeChecker checker) {
            super(checker);
            postInit();
        }

        @Override
        protected AnnotationClassLoader createAnnotationClassLoader() {
            // SupportedQualsChecker does not use a class loader
            return null;
        }

        @Override
        protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
            return new HashSet<Class<? extends Annotation>>(
                    Arrays.asList(Qualifier.class, BottomQualifier.class));
        }
    }
}
