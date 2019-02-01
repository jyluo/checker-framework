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
import org.checkerframework.checker.units.qual.BaseUnit;
import org.checkerframework.checker.units.qual.UnitsRep;
import org.checkerframework.checker.units.utils.UnitsRepresentationUtils;
import org.checkerframework.checker.units.utils.UnitsTypecheckUtils;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.type.AnnotatedTypeFormatter;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotationClassLoader;
import org.checkerframework.framework.type.DefaultAnnotatedTypeFormatter;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
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

    /** reference to the UnitsAnnotationClassLoader instance for loading external units */
    protected UnitsAnnotationClassLoader unitsAnnotationClassLoader;

    /** reference to the units representation utilities library */
    protected UnitsRepresentationUtils unitsRepUtils;

    /** reference to the units type check utilities library */
    protected UnitsTypecheckUtils unitsTypecheckUtils;

    public UnitsAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker, true);
        unitsRepUtils = new UnitsRepresentationUtils(processingEnv, elements);
        postInit();

        unitsTypecheckUtils = new UnitsTypecheckUtils(unitsRepUtils);

        // add implicit units for exceptions and void
        addTypeNameImplicit(java.lang.Exception.class, unitsRepUtils.DIMENSIONLESS);
        addTypeNameImplicit(java.lang.Throwable.class, unitsRepUtils.DIMENSIONLESS);
        addTypeNameImplicit(java.lang.Void.class, unitsRepUtils.BOTTOM);
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
            if (AnnotationUtils.areSameByClass(metaAnno, UnitsRep.class)
                    || AnnotationUtils.areSameByClass(metaAnno, BaseUnit.class)) {
                return unitsRepUtils.getInternalAliasUnit(anno);
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
        return new UnitsAnnotationFormatter(checker, unitsRepUtils);
    }

    // Programmatically set the qualifier defaults
    @Override
    protected void addCheckedCodeDefaults(QualifierDefaults defs) {
        // set DIMENSIONLESS as the default qualifier in hierarchy
        defs.addCheckedCodeDefault(unitsRepUtils.DIMENSIONLESS, TypeUseLocation.OTHERWISE);
        // defaults for upper bounds is DIMENSIONLESS, individual bounds can be manually set to
        // UnknownUnits if they want to use units
        // defs.addCheckedCodeDefault(unitsRepUtils.DIMENSIONLESS, TypeUseLocation.UPPER_BOUND);
        defs.addCheckedCodeDefault(
                unitsRepUtils.DIMENSIONLESS, TypeUseLocation.EXPLICIT_UPPER_BOUND);
        defs.addCheckedCodeDefault(unitsRepUtils.TOP, TypeUseLocation.IMPLICIT_UPPER_BOUND);
        // defaults for lower bounds is BOTTOM, individual bounds can be manually set
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

        /** Programmatically set {@link UnitsRepresentationUtils#BOTTOM} as the bottom */
        @Override
        protected Set<AnnotationMirror> findBottoms(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> newBottoms = super.findBottoms(supertypes);
            newBottoms.remove(unitsRepUtils.RAWUNITSREP);
            newBottoms.add(unitsRepUtils.BOTTOM);

            // set direct supertypes of bottom
            Set<AnnotationMirror> supertypesOfBottom = new LinkedHashSet<>();
            supertypesOfBottom.add(unitsRepUtils.TOP);
            supertypes.put(unitsRepUtils.BOTTOM, supertypesOfBottom);

            return newBottoms;
        }

        /** Programmatically set {@link UnitsRepresentationUtils#TOP} as the top */
        @Override
        protected void finish(
                QualifierHierarchy qualHierarchy,
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypesMap,
                Map<AnnotationMirror, AnnotationMirror> polyQualifiers,
                Set<AnnotationMirror> tops,
                Set<AnnotationMirror> bottoms,
                Object... args) {
            super.finish(qualHierarchy, supertypesMap, polyQualifiers, tops, bottoms, args);

            // swap every instance of RAWUNITSREP with TOP
            assert supertypesMap.containsKey(unitsRepUtils.RAWUNITSREP);
            // Set direct supertypes of TOP
            supertypesMap.put(unitsRepUtils.TOP, supertypesMap.get(unitsRepUtils.RAWUNITSREP));
            supertypesMap.remove(unitsRepUtils.RAWUNITSREP);

            // Set direct supertypes of PolyAll
            // replace raw {@link UnitsRep} with UnitsTop in super of PolyAll
            assert supertypesMap.containsKey(unitsRepUtils.POLYALL);
            Set<AnnotationMirror> polyAllSupers = AnnotationUtils.createAnnotationSet();
            polyAllSupers.addAll(supertypesMap.get(unitsRepUtils.POLYALL));
            polyAllSupers.add(unitsRepUtils.TOP);
            polyAllSupers.remove(unitsRepUtils.RAWUNITSREP);
            supertypesMap.put(unitsRepUtils.POLYALL, Collections.unmodifiableSet(polyAllSupers));

            // Set direct supertypes of PolyUnit
            // replace raw {@link UnitsRep} with UnitsTop in super of PolyUnit
            assert supertypesMap.containsKey(unitsRepUtils.POLYUNIT);
            Set<AnnotationMirror> polyUnitSupers = AnnotationUtils.createAnnotationSet();
            polyUnitSupers.addAll(supertypesMap.get(unitsRepUtils.POLYUNIT));
            polyUnitSupers.add(unitsRepUtils.TOP);
            polyUnitSupers.remove(unitsRepUtils.RAWUNITSREP);
            supertypesMap.put(unitsRepUtils.POLYUNIT, Collections.unmodifiableSet(polyUnitSupers));

            // Set direct supertypes of BOTTOM
            Set<AnnotationMirror> bottomSupers = AnnotationUtils.createAnnotationSet();
            bottomSupers.addAll(supertypesMap.get(unitsRepUtils.BOTTOM));
            // bottom already has top in its super set
            bottomSupers.remove(unitsRepUtils.RAWUNITSREP);
            supertypesMap.put(unitsRepUtils.BOTTOM, Collections.unmodifiableSet(bottomSupers));

            // Update polyQualifiers
            assert polyQualifiers.containsKey(unitsRepUtils.RAWUNITSREP);
            polyQualifiers.put(unitsRepUtils.TOP, polyQualifiers.get(unitsRepUtils.RAWUNITSREP));
            polyQualifiers.remove(unitsRepUtils.RAWUNITSREP);

            // Update tops
            tops.remove(unitsRepUtils.RAWUNITSREP);
            tops.add(unitsRepUtils.TOP);
        }

        // TODO: remove commented code
        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            // System.err.println(" === checking SUBTYPE \n "
            // + getAnnotationFormatter().formatAnnotationMirror(subAnno) + " <:\n"
            // + getAnnotationFormatter().formatAnnotationMirror(superAnno) + "\n");

            // replace RAWUNITSREP with DIMENSIONLESS
            // for some reason this shows up in inference mode when building the lattice
            if (AnnotationUtils.areSame(subAnno, unitsRepUtils.RAWUNITSREP)) {
                subAnno = unitsRepUtils.DIMENSIONLESS;
            }
            if (AnnotationUtils.areSame(superAnno, unitsRepUtils.RAWUNITSREP)) {
                superAnno = unitsRepUtils.DIMENSIONLESS;
            }

            // Case: All units <: Top
            if (AnnotationUtils.areSame(superAnno, unitsRepUtils.TOP)) {
                return true;
            }

            // Case: Bottom <: All units
            if (AnnotationUtils.areSame(subAnno, unitsRepUtils.BOTTOM)) {
                return true;
            }

            // TODO: find out how annos take on poly all or poly unit and flow to here
            if (AnnotationUtils.areSame(subAnno, unitsRepUtils.POLYALL)
                    || AnnotationUtils.areSame(subAnno, unitsRepUtils.POLYUNIT)
                    || AnnotationUtils.areSame(superAnno, unitsRepUtils.POLYALL)
                    || AnnotationUtils.areSame(superAnno, unitsRepUtils.POLYUNIT)) {
                assert false;
            }

            //            // Case: {@link PolyAll} <: All units
            //            // Case: {@link PolyUnit} <: {@link PolyAll} and All units
            //            // Case: All units <: {@link PolyAll} and {@link PolyUnit}
            //            if (AnnotationUtils.areSame(subAnno, unitsRepUtils.POLYALL)
            //                    || AnnotationUtils.areSame(subAnno, unitsRepUtils.POLYUNIT)
            //                    || AnnotationUtils.areSame(superAnno, unitsRepUtils.POLYALL)
            //                    || AnnotationUtils.areSame(superAnno, unitsRepUtils.POLYUNIT)) {
            //                return true;
            //            }

            // Case: {@link UnitsRep}(x) <: {@link UnitsRep}(y)
            if (AnnotationUtils.areSameByClass(subAnno, UnitsRep.class)
                    && AnnotationUtils.areSameByClass(superAnno, UnitsRep.class)
                    && AnnotationUtils.areSameByName(subAnno, superAnno)) {

                boolean result = unitsTypecheckUtils.unitsEqual(subAnno, superAnno);

                // if (AnnotationUtils.areSame(superAnno, unitsRepUtils.METER)) {
                // System.err.println(" === checking SUBTYPE \n "
                // + getAnnotationFormatter().formatAnnotationMirror(subAnno) + " <:\n"
                // + getAnnotationFormatter().formatAnnotationMirror(superAnno) + "\n"
                // + " result: " + result);
                // }

                return result;
            }

            throw new BugInCF(
                    "Uncaught subtype check case:"
                            + "\n    subtype:   "
                            + getAnnotationFormatter().formatAnnotationMirror(subAnno)
                            + "\n    supertype: "
                            + getAnnotationFormatter().formatAnnotationMirror(superAnno));
        }
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new UnitsTypecheckImplicitsTreeAnnotator(), new UnitsPropagationTreeAnnotator());
    }

    protected final class UnitsTypecheckImplicitsTreeAnnotator extends ImplicitsTreeAnnotator {
        // Programmatically set the qualifier implicits
        public UnitsTypecheckImplicitsTreeAnnotator() {
            super(UnitsAnnotatedTypeFactory.this);

            // set BOTTOM as the implicit qualifier for null literals
            addLiteralKind(LiteralKind.NULL, unitsRepUtils.BOTTOM);
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
