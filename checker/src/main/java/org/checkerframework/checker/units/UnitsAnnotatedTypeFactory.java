package org.checkerframework.checker.units;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree.Kind;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import org.checkerframework.checker.units.qual.BaseUnit;
import org.checkerframework.checker.units.qual.UnitsAlias;
import org.checkerframework.checker.units.qual.UnitsRep;
import org.checkerframework.checker.units.utils.UnitsRepresentationUtils;
import org.checkerframework.checker.units.utils.UnitsTypecheckUtils;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFormatter;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotationClassLoader;
import org.checkerframework.framework.type.DefaultAnnotatedTypeFormatter;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ImplicitsTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.AnnotationFormatter;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.framework.util.defaults.QualifierDefaults;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.UserError;

/** Annotated type factory for the Units Checker. */
public class UnitsAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    /** reference to the {@link UnitsAnnotationClassLoader} instance for loading external units */
    protected UnitsAnnotationClassLoader unitsAnnotationClassLoader;

    /**
     * reference to the {@link UnitsAnnotationFormatter} instance for formatting units in warnings
     */
    protected UnitsAnnotationFormatter unitsAnnotationFormatter;

    /** reference to the units representation utilities library */
    protected UnitsRepresentationUtils unitsRepUtils;

    /** reference to the units type check utilities library */
    protected UnitsTypecheckUtils unitsTypecheckUtils;

    public UnitsAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker, true);
        unitsRepUtils = new UnitsRepresentationUtils(processingEnv, elements);
        unitsAnnotationFormatter.postInit(unitsRepUtils);
        postInit();

        unitsTypecheckUtils = new UnitsTypecheckUtils(unitsRepUtils);
    }

    public UnitsRepresentationUtils getUnitsRepresentationUtils() {
        return unitsRepUtils;
    }

    /** Use the Units Annotated Type Loader instead of the default one */
    @Override
    protected AnnotationClassLoader createAnnotationClassLoader() {
        unitsAnnotationClassLoader = new UnitsAnnotationClassLoader(checker);
        return unitsAnnotationClassLoader;
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        // get all the loaded annotations
        Set<Class<? extends Annotation>> qualSet = new HashSet<Class<? extends Annotation>>();
        qualSet.addAll(getBundledTypeQualifiersWithPolyAll());

        // load all the external units
        unitsAnnotationClassLoader.loadAllExternalUnits(
                checker.getOption("units"), checker.getOption("unitsDirs"));

        unitsRepUtils.postInit(
                unitsAnnotationClassLoader.getBaseUnits(),
                unitsAnnotationClassLoader.getAliasUnits());

        // copy all loaded external Units to qual set
        qualSet.addAll(unitsAnnotationClassLoader.getExternalUnits());

        // create internal use annotation mirrors using the base units that have been initialized.
        if (unitsRepUtils.baseUnits().isEmpty()) {
            throw new UserError("Must supply at least 1 base unit to use Units Checker");
        }

        return qualSet;
    }

    // Make sure only {@link UnitsRep} annotations with all base units defined are considered
    // supported any {@link UnitsRep} annotations without all base units should go through aliasing
    // to have the base units filled in.
    @Override
    public boolean isSupportedQualifier(AnnotationMirror anno) {
        /**
         * getQualifierHierarchy().getTypeQualifiers() contains {@link PolyAll}, {@link PolyUnit},
         * and the AMs of Top and Bottom. We need to check all other instances of {@link UnitsRep}
         * AMs that are supported qualifiers here.
         */
        if (!super.isSupportedQualifier(anno)) {
            return false;
        }
        if (AnnotationUtils.areSameByClass(anno, UnitsRep.class)) {
            return unitsRepUtils.hasAllBaseUnits(anno);
        }
        /** Anno is {@link PolyAll} or {@link PolyUnit} */
        return AnnotationUtils.containsSame(this.getQualifierHierarchy().getTypeQualifiers(), anno);
    }

    @Override
    public AnnotationMirror aliasedAnnotation(AnnotationMirror anno) {
        // check to see if it is an internal units annotation
        if (AnnotationUtils.areSameByClass(anno, UnitsRep.class)) {
            // fill in missing base units
            return unitsRepUtils.fillMissingBaseUnits(anno);
        }

        /** check to see if it's a surface annotation such as {@link m} or {@link UnknownUnits} */
        for (AnnotationMirror metaAnno :
                anno.getAnnotationType().asElement().getAnnotationMirrors()) {

            /**
             * if it has a {@link UnitsRep} or {@link BaseUnit} meta-annotation, then it must have
             * been prebuilt return the prebuilt internal annotation
             */
            if (AnnotationUtils.areSameByClass(metaAnno, UnitsAlias.class)
                    || AnnotationUtils.areSameByClass(metaAnno, BaseUnit.class)) {
                AnnotationMirror normalizedAnno = unitsRepUtils.getUnitsRepAnno(anno);

                // System.err.println(anno + " is unit alias, returning " + normalizedAnno);
                return normalizedAnno;
            }
        }

        return super.aliasedAnnotation(anno);
    }

    // for use in AnnotatedTypeMirror.toString()
    @Override
    protected AnnotatedTypeFormatter createAnnotatedTypeFormatter() {
        boolean printVerboseGenerics = checker.hasOption("printVerboseGenerics");
        return new DefaultAnnotatedTypeFormatter(
                createAnnotationFormatter(),
                printVerboseGenerics,
                // -AprintVerboseGenerics implies -AprintAllQualifiers
                printVerboseGenerics || checker.hasOption("printAllQualifiers"));
    }

    // for use in generating error outputs
    @Override
    protected AnnotationFormatter createAnnotationFormatter() {
        if (unitsAnnotationFormatter == null) {
            unitsAnnotationFormatter = new UnitsAnnotationFormatter();
        }
        return unitsAnnotationFormatter;
    }

    // Programmatically set the qualifier defaults
    @Override
    protected void addCheckedCodeDefaults(QualifierDefaults defs) {
        // set DIMENSIONLESS as the default qualifier in hierarchy
        defs.addCheckedCodeDefault(unitsRepUtils.DIMENSIONLESS, TypeUseLocation.OTHERWISE);
        defs.addCheckedCodeDefault(
                unitsRepUtils.DIMENSIONLESS, TypeUseLocation.EXPLICIT_UPPER_BOUND);
        defs.addCheckedCodeDefault(unitsRepUtils.TOP, TypeUseLocation.IMPLICIT_UPPER_BOUND);
        defs.addCheckedCodeDefault(unitsRepUtils.BOTTOM, TypeUseLocation.LOWER_BOUND);

        // exceptions are always DIMENSIONLESS
        defs.addCheckedCodeDefault(
                unitsRepUtils.DIMENSIONLESS, TypeUseLocation.EXCEPTION_PARAMETER);
        // set TOP as the default qualifier for local variables, for dataflow refinement
        defs.addCheckedCodeDefault(unitsRepUtils.TOP, TypeUseLocation.LOCAL_VARIABLE);
    }

    // Note: remember to use
    // --cfArgs="-AuseDefaultsForUncheckedCode=source,bytecode" in cmd line option
    // -AuseDefaultsForUncheckedCode=bytecode // uses those defaults in byte code
    // -AuseDefaultsForUncheckedCode=source,bytecode // also uses those defaults in
    // source code
    @Override
    protected void addUncheckedCodeDefaults(QualifierDefaults defs) {
        super.addUncheckedCodeDefaults(defs);

        // optimistic defaults
        // top param, receiver, bot return for inference, explain unsat
        defs.addUncheckedCodeDefault(unitsRepUtils.TOP, TypeUseLocation.RECEIVER);
        defs.addUncheckedCodeDefault(unitsRepUtils.TOP, TypeUseLocation.PARAMETER);
        defs.addUncheckedCodeDefault(unitsRepUtils.BOTTOM, TypeUseLocation.RETURN);

        // DIMENSIONLESS is default for all other locations
        defs.addUncheckedCodeDefault(unitsRepUtils.DIMENSIONLESS, TypeUseLocation.OTHERWISE);
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new UnitsQualifierHierarchy(factory);
    }

    private final class UnitsQualifierHierarchy extends GraphQualifierHierarchy {
        public UnitsQualifierHierarchy(MultiGraphFactory mgf) {
            super(mgf, unitsRepUtils.BOTTOM);
        }

        /** Programmatically set {@link UnitsRepresentationUtils#TOP} as the top */
        @Override
        protected Set<AnnotationMirror> findTops(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> tops = AnnotationUtils.createAnnotationSet();

            tops.add(unitsRepUtils.TOP);

            // remove RAWUNITSREP in supertypes
            assert supertypes.containsKey(unitsRepUtils.RAWUNITSREP);
            supertypes.remove(unitsRepUtils.RAWUNITSREP);
            // add TOP to supertypes
            supertypes.put(unitsRepUtils.TOP, Collections.emptySet());

            return tops;
        }

        /** Programmatically set {@link UnitsRepresentationUtils#BOTTOM} as the bottom */
        @Override
        protected Set<AnnotationMirror> findBottoms(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> bottoms = AnnotationUtils.createAnnotationSet();

            bottoms.add(unitsRepUtils.BOTTOM);

            // set direct supertypes of BOTTOM and add to supertypes
            Set<AnnotationMirror> bottomSupers = new LinkedHashSet<>();
            bottomSupers.add(unitsRepUtils.POLYUNIT);
            bottomSupers.add(unitsRepUtils.POLYALL);
            bottomSupers.add(unitsRepUtils.TOP);
            supertypes.put(unitsRepUtils.BOTTOM, Collections.unmodifiableSet(bottomSupers));

            return bottoms;
        }

        /**
         * Programmatically set {@link UnitsRepresentationUtils#POLYUNIT} and {@link
         * UnitsRepresentationUtils#POLYALL} as the polymorphic qualifiers
         */
        @Override
        protected void addPolyRelations(
                QualifierHierarchy qualHierarchy,
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes,
                Map<AnnotationMirror, AnnotationMirror> polyQualifiers,
                Set<AnnotationMirror> tops,
                Set<AnnotationMirror> bottoms) {

            // polyQualifiers {null=@PolyAll, @UnitsRep=@PolyUnit}
            // replace RAWUNITSREP -> @PolyUnit with TOP -> @PolyUnit
            assert polyQualifiers.containsKey(unitsRepUtils.RAWUNITSREP);
            polyQualifiers.put(unitsRepUtils.TOP, polyQualifiers.get(unitsRepUtils.RAWUNITSREP));
            polyQualifiers.remove(unitsRepUtils.RAWUNITSREP);

            // add @PolyAll -> TOP to supertypes
            Set<AnnotationMirror> polyAllSupers = AnnotationUtils.createAnnotationSet();
            polyAllSupers.add(unitsRepUtils.TOP);
            supertypes.put(unitsRepUtils.POLYALL, Collections.unmodifiableSet(polyAllSupers));

            // add @PolyUnit -> {@PolyAll, TOP} to supertypes
            Set<AnnotationMirror> polyUnitSupers = AnnotationUtils.createAnnotationSet();
            polyUnitSupers.add(unitsRepUtils.POLYALL);
            polyUnitSupers.add(unitsRepUtils.TOP);
            supertypes.put(unitsRepUtils.POLYUNIT, Collections.unmodifiableSet(polyUnitSupers));

            // System.err.println(" POST ");
            // System.err.println(" supertypes {");
            // for (Entry<?, ?> e : supertypes.entrySet()) {
            // System.err.println(" " + e.getKey() + " -> " + e.getValue());
            // }
            // System.err.println(" }");
            // System.err.println(" polyQualifiers " + polyQualifiers);
            // System.err.println(" tops " + tops);
            // System.err.println(" bottoms " + bottoms);
            // System.err.println();
        }

        // TODO: remove commented code
        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            // System.err.println(" === checking SUBTYPE ");
            // // System.err.println(subAnno + " <: ");
            // // System.err.println(superAnno);
            // System.err.println(getAnnotationFormatter().formatAnnotationMirror(subAnno) + " <: "
            // + getAnnotationFormatter().formatAnnotationMirror(superAnno));
            // System.err.println();

            // replace RAWUNITSREP with DIMENSIONLESS
            // for some reason this shows up in inference mode when building the lattice
            if (AnnotationUtils.areSame(subAnno, unitsRepUtils.RAWUNITSREP)) {
                subAnno = unitsRepUtils.DIMENSIONLESS;
            }
            if (AnnotationUtils.areSame(superAnno, unitsRepUtils.RAWUNITSREP)) {
                superAnno = unitsRepUtils.DIMENSIONLESS;
            }

            // When type checking body of polymorphic methods, check the types by replacing poly
            // with TOP
            if (isPolymorphic(subAnno)) {
                subAnno = unitsRepUtils.TOP;
            }
            if (isPolymorphic(superAnno)) {
                // superAnno = unitsRepUtils.TOP;
                return true;
            }

            // Case: All units <: Top
            if (AnnotationUtils.areSame(superAnno, unitsRepUtils.TOP)) {
                return true;
            }

            // Case: Bottom <: All units
            if (AnnotationUtils.areSame(subAnno, unitsRepUtils.BOTTOM)) {
                return true;
            }

            // Case: {@link UnitsRep}(x) <: {@link UnitsRep}(y)
            if (AnnotationUtils.areSameByClass(subAnno, UnitsRep.class)
                    && AnnotationUtils.areSameByName(subAnno, superAnno)) {

                boolean result = unitsTypecheckUtils.unitsEqual(subAnno, superAnno);

                // System.err.println(" === checking SUBTYPE ");
                // System.err.println(getAnnotationFormatter().formatAnnotationMirror(subAnno) + "
                // <: "
                // + getAnnotationFormatter().formatAnnotationMirror(superAnno));
                // System.err.println();

                return result;
            }

            throw new BugInCF(
                    "Uncaught subtype check case:"
                            + "\n    subtype:   "
                            + getAnnotationFormatter().formatAnnotationMirror(subAnno)
                            + "\n    supertype: "
                            + getAnnotationFormatter().formatAnnotationMirror(superAnno));
        }

        protected boolean isPolymorphic(AnnotationMirror anno) {
            return AnnotationUtils.areSame(anno, unitsRepUtils.POLYALL)
                    || AnnotationUtils.areSame(anno, unitsRepUtils.POLYUNIT);
        }
    }

    @Override
    protected TypeAnnotator createTypeAnnotator() {
        return new ListTypeAnnotator(
                new UnitsImplicitsTypeAnnotator(this), super.createTypeAnnotator());
    }

    protected class UnitsImplicitsTypeAnnotator extends ImplicitsTypeAnnotator {
        public UnitsImplicitsTypeAnnotator(AnnotatedTypeFactory typeFactory) {
            super(typeFactory);

            // strings, chars, and bools are implicitly DIMENSIONLESS
            addTypeName(java.lang.String.class, unitsRepUtils.DIMENSIONLESS);
            addTypeName(java.lang.Character.class, unitsRepUtils.DIMENSIONLESS);
            addTypeName(java.lang.Boolean.class, unitsRepUtils.DIMENSIONLESS);
            addTypeKind(TypeKind.CHAR, unitsRepUtils.DIMENSIONLESS);
            addTypeKind(TypeKind.BOOLEAN, unitsRepUtils.DIMENSIONLESS);

            // exceptions are implicitly DIMENSIONLESS
            addTypeName(java.lang.Exception.class, unitsRepUtils.DIMENSIONLESS);
            addTypeName(java.lang.Throwable.class, unitsRepUtils.DIMENSIONLESS);

            // void is implicitly BOTTOM
            addTypeName(java.lang.Void.class, unitsRepUtils.BOTTOM);
            addTypeKind(TypeKind.NULL, unitsRepUtils.BOTTOM);
        }
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new UnitsImplicitsTreeAnnotator(), new UnitsPropagationTreeAnnotator());
    }

    protected class UnitsImplicitsTreeAnnotator extends ImplicitsTreeAnnotator {
        // Programmatically set the qualifier implicits
        public UnitsImplicitsTreeAnnotator() {
            super(UnitsAnnotatedTypeFactory.this);

            // set BOTTOM for null literals
            addLiteralKind(LiteralKind.NULL, unitsRepUtils.BOTTOM);

            // set DIMENSIONLESS for the non number literals
            addLiteralKind(LiteralKind.STRING, unitsRepUtils.DIMENSIONLESS);
            addLiteralKind(LiteralKind.CHAR, unitsRepUtils.DIMENSIONLESS);
            addLiteralKind(LiteralKind.BOOLEAN, unitsRepUtils.DIMENSIONLESS);

            // set DIMENSIONLESS for the number literals
            addLiteralKind(LiteralKind.INT, unitsRepUtils.DIMENSIONLESS);
            addLiteralKind(LiteralKind.LONG, unitsRepUtils.DIMENSIONLESS);
            addLiteralKind(LiteralKind.FLOAT, unitsRepUtils.DIMENSIONLESS);
            addLiteralKind(LiteralKind.DOUBLE, unitsRepUtils.DIMENSIONLESS);
        }
    }

    private final class UnitsPropagationTreeAnnotator extends PropagationTreeAnnotator {
        public UnitsPropagationTreeAnnotator() {
            super(UnitsAnnotatedTypeFactory.this);
        }

        @SuppressWarnings("fallthrough")
        @Override
        public Void visitBinary(BinaryTree binaryTree, AnnotatedTypeMirror type) {
            Kind kind = binaryTree.getKind();
            AnnotatedTypeMirror lhsATM = atypeFactory.getAnnotatedType(binaryTree.getLeftOperand());
            AnnotatedTypeMirror rhsATM =
                    atypeFactory.getAnnotatedType(binaryTree.getRightOperand());
            AnnotationMirror lhsAM = lhsATM.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP);
            AnnotationMirror rhsAM = rhsATM.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP);

            switch (kind) {
                case PLUS:
                    // if it is a string concatenation, result is DIMENSIONLESS
                    if (TreeUtils.isStringConcatenation(binaryTree)) {
                        type.replaceAnnotation(unitsRepUtils.DIMENSIONLESS);
                    } else {
                        type.replaceAnnotation(
                                atypeFactory.getQualifierHierarchy().leastUpperBound(lhsAM, rhsAM));
                    }
                    //
                    // else if (AnnotationUtils.areSame(lhsAM, rhsAM)) {
                    // type.replaceAnnotation(lhsAM);
                    // } else {
                    // type.replaceAnnotation(unitsRepUtils.TOP);
                    // }
                    break;
                case MINUS:
                    // if (AnnotationUtils.areSame(lhsAM, rhsAM)) {
                    // type.replaceAnnotation(lhsAM);
                    // } else {
                    // type.replaceAnnotation(unitsRepUtils.TOP);
                    // }
                    type.replaceAnnotation(
                            atypeFactory.getQualifierHierarchy().leastUpperBound(lhsAM, rhsAM));
                    break;
                case MULTIPLY:
                    type.replaceAnnotation(unitsTypecheckUtils.multiplication(lhsAM, rhsAM));
                    break;
                case DIVIDE:
                    type.replaceAnnotation(unitsTypecheckUtils.division(lhsAM, rhsAM));
                    break;
                case REMAINDER:
                    type.replaceAnnotation(lhsAM);
                    break;
                case CONDITIONAL_AND: // &&
                case CONDITIONAL_OR: // ||
                case LOGICAL_COMPLEMENT: // !
                case EQUAL_TO: // ==
                case NOT_EQUAL_TO: // !=
                case GREATER_THAN: // >
                case GREATER_THAN_EQUAL: // >=
                case LESS_THAN: // <
                case LESS_THAN_EQUAL: // <=
                    // output of comparisons is a DIMENSIONLESS binary
                    type.replaceAnnotation(unitsRepUtils.DIMENSIONLESS);
                    break;
                default:
                    // Check LUB by default
                    return super.visitBinary(binaryTree, type);
            }

            return null;
        }
    }
}
