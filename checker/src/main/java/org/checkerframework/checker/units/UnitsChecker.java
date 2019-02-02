package org.checkerframework.checker.units;

import javax.annotation.processing.SupportedOptions;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.StubFiles;

/**
 * Units Checker main class.
 *
 * <p>Supports "units" option to add support for additional individually named and externally
 * defined units, and "unitsDirs" option to add support for directories of externally defined units.
 * Directories must be well-formed paths from file system root, separated by colon (:) between each
 * directory.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@StubFiles({
    "JavaBoxedPrimitives.astub",
    "JavaIOPrintstream.astub",
    "JavaLang.astub",
    "JavaMath.astub",
    "JavaThread.astub",
    "JavaUtil.astub",
    "JavaUtilConcurrent.astub",
})
@SupportedOptions({"units", "unitsDirs"})
public class UnitsChecker extends BaseTypeChecker {}
