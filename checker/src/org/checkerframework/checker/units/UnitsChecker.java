package org.checkerframework.checker.units;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.StubFiles;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.SupportedOptions;

/**
 * Units Checker main class.
 * <p>
 *
 * Supports "units" option to add support for additional individually named and
 * externally defined units, and "unitsDirs" option to add support for
 * directories of externally defined units. Directories must be well-formed
 * paths from file system root, separated by colon (:) between each directory.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@SupportedOptions({ "units", "unitsDirs" })
@StubFiles({
    "Math.astub", "StrictMath.astub", "PrintStream.astub",
    "JavaSystemTime.astub", "JavaUtilTime.astub", "JavaThread.astub",
    "Java8Time.astub", "Java8TemporalTime.astub",
    "JavaGenerics.astub"
})
public class UnitsChecker extends BaseTypeChecker {
    /**
     * Copied from SubtypingChecker; cannot reuse it, because SubtypingChecker
     * is final. TODO: SubtypingChecker might also want to always call super.
     */
    @Override
    public Collection<String> getSuppressWarningsKeys() {
        Set<String> swKeys = new HashSet<String>(
                super.getSuppressWarningsKeys());
        Set<Class<? extends Annotation>> annos = ((BaseTypeVisitor<?>) visitor)
                .getTypeFactory().getSupportedTypeQualifiers();

        for (Class<? extends Annotation> anno : annos) {
            swKeys.add(anno.getSimpleName().toLowerCase());
        }

        return swKeys;
    }
}
