package org.checkerframework.checker.experimental.units_qual_poly;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.checkerframework.checker.experimental.units_qual_poly.qual.Area;
import org.checkerframework.checker.experimental.units_qual_poly.qual.Prefix;
import org.checkerframework.checker.units.qual.UnitsMultiple;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.TypeQualifier;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

//UnitsBuilder used to construct qualifiers programmatically
public final class UnitsQualReflectionLoader{
    //TODO: store some map of prebuilt qualifiers, if during building it already has one of these qualifiers then return a reference to it instead 
    private static UnitsQualReflectionLoader pool; // singleton pool

    private static List<Units> supportedUnits;        // singleton

    private static ProcessingEnvironment processingEnv;

    private static final char DOT = '.';
    private static final char SLASH = '/';
    private static final String CLASS_SUFFIX = ".class";
    private static final String pathToJar = "/home/jeff/workspace/jsr308/checker-framework/checker/dist/checker.jar";

    private static final String JSR308DirKey = "JSR308";
    private static String JSR308;

    //private static final String projDir = "/checker-framework/checker/experimental/units_qual_poly/qual";
    private static final String projDir = "/checker-framework/checker/src/";
    private static final String checkerDir = dotToSlash("org.checkerframework.checker.experimental.units_qual_poly.qual.");
    private static String packageName;
    private static final String qualSubpackage = ".qual.";
    
    // private constructors for creating a Units qualifier
    // we expose two getQualifier methods for instantiating a new qualifier instead, so that we can maintain a lean memory
    // footprint for the total number of qualifier objects in memory
    //    private UnitsQualPool(String name, Class<? extends Annotation> anno) {
    //        super(name, anno);
    //    }
    //    private UnitsQualPool(String name, Prefix p, Class<? extends Annotation> anno) {
    //        super(name, p, anno);
    //    }
    //    private UnitsQualPool(String name, Units superUnit, Class<? extends Annotation> anno) {
    //        super(name, superUnit, anno);
    //    }
    //    private UnitsQualPool(String name, Prefix p, Units superUnit, Class<? extends Annotation> anno) {
    //        super(name, p, superUnit, anno);
    //    }

    private UnitsQualReflectionLoader(ProcessingEnvironment pe)
    {
        processingEnv = pe;

        supportedUnits = new ArrayList<Units>();

        Field[] declaredUnits = Units.class.getDeclaredFields();

        for(Field unitField : declaredUnits) {
            if(     java.lang.reflect.Modifier.isPublic(unitField.getModifiers()) &&
                    java.lang.reflect.Modifier.isStatic(unitField.getModifiers()) && 
                    java.lang.reflect.Modifier.isFinal(unitField.getModifiers()) &&
                    unitField.getType().equals(Units.class)) {

                try {
                    Units u = (Units) unitField.get(Units.class);

                    supportedUnits.add(u);

                    // System.out.println("Units Qual: " + u.toString());
                } catch (IllegalArgumentException e) {
                    // TODO better error handling here
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO better error handling here
                    e.printStackTrace();
                }
            }
        }

        // assigns package name based on the loader's package name: Loader must be in the same folder as the Checker, and the quals under a qual sub-folder
        packageName = this.getClass().getPackage().getName();

        // =======================================
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Loading annos...");
        Map<String, AnnotationMirror> loadedAnnos = loadAnnotations();
        for(String annoName : loadedAnnos.keySet()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "loaded anno: " + annoName);
        }
    }

    // loads annotations via reflection
    @SuppressWarnings("resource")
    public Map<String, AnnotationMirror> loadAnnotations() {
        Map<String, AnnotationMirror> annos = new HashMap<String, AnnotationMirror>();

        Set<String> annoFiles = getAnnotationFileNames();

        File qualDir = new File(JSR308 + projDir + checkerDir);
        URL url = null;
        try {
            url = qualDir.toURI().toURL();
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        URL[] urls = new URL[]{url};
        // Create a new class loader with the directory
        ClassLoader cl = new URLClassLoader(urls);

        for(String fileName : annoFiles) {
            try {

                String anno = packageName + qualSubpackage + fileName.substring(0, fileName.lastIndexOf('.'));
                //                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "loading: " + anno);

                // Load in the class files
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> cls = (Class<? extends Annotation>) cl.loadClass(anno);

                // convert the annotation class into an annotation mirror
                annos.put(anno, createAnnotationMirrorFromClass(cls));

            } catch (ClassNotFoundException e) {
                //TODO: give better feedback
                e.printStackTrace();
            }
        }

        return annos;
    }

    @SuppressWarnings("resource")
    public Set<String> loadAnnotationsFromJar() {

        Set<String> annos = new HashSet<String>();

        JarFile jar = null;
        try {
            jar = new JarFile(pathToJar);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Enumeration<JarEntry> e = jar.entries();
        //        try {
        //            URL[] urls = { new URL("jar:file:" + pathToJar + "!/") };
        //        } catch (MalformedURLException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        }
        //URLClassLoader uCL = URLClassLoader.newInstance(urls);

        while(e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if(je.isDirectory() || !je.getName().endsWith(CLASS_SUFFIX)){
                continue;
            }

            String className = je.getName().substring(0, je.getName().lastIndexOf('.'));
            className = className.replace(SLASH, DOT);
            // Class c = uCL.loadClass(className);

            // filter for qual package and only add class names that are relevant
            if(className.startsWith(packageName + qualSubpackage)) {
                annos.add(className);
            }
        }
        //
        //        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "loaded number of classes: " + annos.size());
        //        for(String s : annos) {
        //            if(s.startsWith(this.getClass().getPackage().getName() + qualDir)) {
        //                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "loaded: " + s);
        //            }
        //        }

        return annos;
    }

    // takes a class representing a compiled java file and filters out non-type qualifier annotations, then converts it to an annotation mirror using the AnnotationBuilder
    public AnnotationMirror createAnnotationMirrorFromClass(Class<? extends Annotation> annoClass) {

        boolean isTypeQual = false;
        for(Annotation metaAnnotation : annoClass.getAnnotations()) {
            // see if it is a type qualifier
            if( metaAnnotation.annotationType().getCanonicalName().equals(TypeQualifier.class.getCanonicalName()) ) {
                // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Anno is a type qualifier");
                isTypeQual = true;
                break;
            }
        }

        // filter out non-type qualifiers
        // TODO: Future Improvement to Annotation Builder -> while it attempts to build a new annotation, it will reject anything that isn't actually an annotation class
        if(isTypeQual) {
            // build the initial annotation mirror (missing prefix)
            AnnotationBuilder builder = new AnnotationBuilder(processingEnv, annoClass);
            AnnotationMirror initialResult = builder.build();

            // further refine 
            for(AnnotationMirror metaAnno : initialResult.getAnnotationType().asElement().getAnnotationMirrors() ) {
                //                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Meta Anno: " + metaAnno);

                // default in hierarchy
                if( metaAnno.getAnnotationType().toString().equals(DefaultQualifierInHierarchy.class.getCanonicalName())) {
                    // TODO : setup qual hierarchy
                }

                // TODO : special treatment of invisible qualifiers?

                // annotations which are a SI prefix multiple of some base unit 
                if( metaAnno.getAnnotationType().toString().equals(UnitsMultiple.class.getCanonicalName())) {
                    @SuppressWarnings("unchecked")
                    // retrieve the quantity, which is the base annotation class of the SI Unit
                    Class<? extends Annotation> baseUnitAnnoClass = (Class<? extends Annotation>) AnnotationUtils.getElementValueClass(metaAnno, "quantity", true);
                    // and retrieve the SI Prefix
                    Prefix prefix = AnnotationUtils.getElementValueEnum(metaAnno, "prefix", Prefix.class, true);
                    // build the canonical annotation
                    AnnotationBuilder baseBuilder = new AnnotationBuilder(processingEnv, baseUnitAnnoClass);
                    baseBuilder.setValue("value", prefix);
                    AnnotationMirror res = builder.build();
                    // insert the alias class name and the canonical annotation into the map
                    // TODO: qual version of this
                    // aliasMap.put(aname, res);
                    // return the canonical annotation
                    return res;
                }
            }
        }
        return null;
    }

    //    private boolean loadAndCompileAnnos() {
    //        Set<String> annoFiles = getAnnotationFileNames();
    //        for(String fileName : annoFiles) {
    //            String file = JSR308 + projDir + checkerDir + fileName;
    //            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "compiling: " + file);
    //
    //            compileJavaFile(file);
    //        }
    //        return true;
    //    }

    public Set<String> getAnnotationFileNames() {
        Set<String> annoFileNames = new HashSet<String> ();

        // Get the system env value for home directory of JSR308 project
        Map<String, String> env = System.getenv();
        //        for(String envName : env.keySet()) {
        //            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, envName + " : " + env.get(envName));
        //        }

        JSR308 = env.get(JSR308DirKey);

        // Create a File object on the root of the directory containing the class file
        File file = new File(JSR308 + projDir + checkerDir);
        File[] annotations = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".java");
            }
        } );

        for(File anno : annotations) {
            String annoName = anno.getName();
            annoFileNames.add(annoName);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "reading anno file name: " + annoName);
        }

        // returns a set of file names of the annotations
        return annoFileNames;
    }

    //    public boolean compileJavaFile(String fileName){
    //        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    //        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    //        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
    //        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(fileName));
    //        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null,
    //                null, compilationUnits);
    //        boolean success = task.call();
    //        try {
    //            fileManager.close();
    //        } catch (IOException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        }
    //        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Compile success = " + success);
    //        return success;
    //    }

    private static String dotToSlash(String input) {
        return input.replace(DOT, SLASH);
    }

    private static String slashToDot(String input) {
        return input.replace(SLASH, DOT);
    }


    // singleton pool methods
    public static UnitsQualReflectionLoader getInstance(ProcessingEnvironment pe) {
        if(pool == null) {
            pool = new UnitsQualReflectionLoader(pe);
        }
        else {
            processingEnv = pe;
        }

        return pool;
    }

    // Units Qualifier Loader ======================

    public final List<Units> getSupportedUnits() {
        return supportedUnits;
    }

    public final List<Units> getSupportedUnitsWithAnnotations() {
        List<Units> annoUnitsList = new ArrayList<Units>();

        for(Units u : getSupportedUnits())
        {
            if(u.getAnnotation() != null)
                annoUnitsList.add(u);
        }

        return annoUnitsList;
    }

    public final Set<String> getSupportedAnnotationNames() {
        //new HashSet<>(Arrays.asList(org.checkerframework.checker.experimental.units_qual_poly.Units.class.getName())),
        HashSet<String> annoNames = new HashSet<String>();

        for(Units u : getSupportedUnitsWithAnnotations()) {
            annoNames.add(u.getAnnotation().getName());
        }

        return annoNames;
    }

    // behaves just like Singleton.getInstance() in concept: checks to see if there's already an existing qualifier with
    // a matching name and prefix. If so it will return a reference to the existing one, if not it will make a new one
    // and add it to the list of qualifiers
    public final Units getQualifier(String name, Prefix p, Units superUnit, Class<? extends Annotation> anno) {
        Units targetQual = null;

        // ensure p is never null, by default it will be assigned to Prefix.one
        if(p == null)
            p = Prefix.one;

        // loop through all existing qualifiers in the pool
        for(Units qual : getSupportedUnits()) {
            // see if there's an existing qualifier that matches the desired qualifier name
            if(qual.getUnitName().equals(name)) {
                // if its prefix also matches, then return that unit
                if(qual.getPrefix() == p) {
                    //                    if(processingEnv != null)
                    //                        processingEnv.getMessager().printMessage(Kind.NOTE, "=== Existing Unit ===: " + qual.toString());
                    return qual;
                }
                // otherwise store this qual as a target qual, where the new one constructed share's the same super type as this qual
                else {
                    targetQual = qual;
                }
            }
        }

        // if there's a super type passed in, then create a brand new unit qualifier
        if(superUnit != null) {
            Units brandNewQual = new Units(name, p, superUnit, anno);
            getSupportedUnits().add(brandNewQual);
            //            if(processingEnv != null)
            //                processingEnv.getMessager().printMessage(Kind.NOTE, "=== New Unit with custom super ===: " + brandNewQual.toString() + "==================================");
            return brandNewQual;
        }
        // otherwise, if there's an existing unit that has the same name but different prefix (as detected earlier), 
        // create a new qualifier with the existing unit's super type
        else if(targetQual != null) {
            Units qualWithNewPrefix = new Units(name, p, targetQual.getSuperType(), anno);
            getSupportedUnits().add(qualWithNewPrefix);
            //            if(processingEnv != null)
            //                processingEnv.getMessager().printMessage(Kind.NOTE, "=== New Unit with existing super ===: " + qualWithNewPrefix.toString() + "==================================");
            return qualWithNewPrefix;
        }
        else {
            // otherwise create a new qualifier, add it to the supportedUnits list, then return it
            Units newUpperQual = new Units(name, p, Units.UNITSUNKNOWN, anno);
            getSupportedUnits().add(newUpperQual);
            //            if(processingEnv != null)
            //                processingEnv.getMessager().printMessage(Kind.NOTE, "=== New Unit with default super ===: " + newUpperQual.toString() + "==================================");
            return newUpperQual;
        }
    }

    public final Units getQualifier(String name, Prefix p, Units superUnit) {
        return getQualifier(name, p, superUnit, null);
    }

    public final Units getQualifier(String name, Prefix p) {
        return getQualifier(name, p, null);
    }

    //
    //        // detects whether the desired qualifier has already been created in the supportedUnits list
    //        private static final boolean qualifierExists(String name, Prefix p) {
    //            for(Units qual : supportedUnits) {
    //                if(qual.getUnitName().equals(name) && qual.getPrefix() == p)
    //                    return true;
    //            }
    //            return false;
    //        }
}
