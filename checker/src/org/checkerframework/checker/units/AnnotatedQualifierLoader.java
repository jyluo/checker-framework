package org.checkerframework.checker.units;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.tools.Diagnostic.Kind;

import org.checkerframework.common.basetype.BaseTypeChecker;

public abstract class AnnotatedQualifierLoader {
    // For loading from a source package directory
    private String packageName;
    private static final String QUAL_PACKAGE_SUFFIX = ".qual";

    // For loading from a Jar file
    private static final String CLASS_SUFFIX = ".class";

    // Constants
    private static final char DOT = '.';
    private static final char SLASH = '/';

    // Processing Env used to create an Annotation Builder this is done in the
    // Default Annotated Qualifier Loader or any loader which extends this class
    // to build the annotation mirror from the loaded Class object
    protected ProcessingEnvironment processingEnv;

    // obtain the URL of the Checker class of a particular checker
    URL resourceURL;

    // Stores a mapping of the Annotation object and the converted AnnotationMirror
    private Map<Class<? extends Annotation>, AnnotationMirror> loadedAnnotations;

    // constructor
    public AnnotatedQualifierLoader(ProcessingEnvironment pe, Class<? extends BaseTypeChecker> checker) {
        processingEnv = pe;
        packageName = checker.getPackage().getName().replace(SLASH, DOT) + QUAL_PACKAGE_SUFFIX;
        //resourceURL = Thread.currentThread().getContextClassLoader().getResource(packageName.replace(DOT, SLASH));
        // TODO: add debug crash output if resourceURL == null

        resourceURL = this.getClass().getClassLoader().getResource(packageName.replace(DOT, SLASH));
        loadedAnnotations = loadAnnotations();
    }

    /**
     * Every subclass of AnnotatedQualifierLoader must implement how it will convert a class into an annotation mirror
     * @param annoClass
     * @return AnnotationMirror of the annotation, or null if this annotation isn't required
     */
    protected abstract AnnotationMirror createAnnotationMirrorFromClass(Class<? extends Annotation> annoClass);

    // provides the qualifier set based on the loaded annotations
    public final Set<Class<? extends Annotation>> getAnnotatedQualSet() {
        // TODO: future extension: if needed sort the set of annotation classes according to top to bottom of hierarchy
        // Set<Class<? extends Annotation>> qualSet = new HashSet<Class<? extends Annotation>>();

        // return just the set of annotation classes without regard to order
        return loadedAnnotations.keySet();
    }

    // provides the annotation mirror set based on the loaded annotations
    public final Set<AnnotationMirror> getAnnotationMirrorSet() {
        return new HashSet<AnnotationMirror>(loadedAnnotations.values());
    }

    // loads annotations via reflection
    public final Map<Class<? extends Annotation>, AnnotationMirror> loadAnnotations() {
        Map<Class<? extends Annotation>, AnnotationMirror> annos = new HashMap<Class<? extends Annotation>, AnnotationMirror>();

        //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "finding classes in " + packageName);

        Set<String> annoFiles = getAnnotationNames();
        //Set<String> annoFiles = getAnnotationNamesFromJar();

        for(String fileName : annoFiles) {
            try {
                String annoName = packageName + DOT + fileName;
                // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "loading: " + anno);

                // Load in the class files
                Class<?> cls = Class.forName(annoName);

                // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, anno);
                // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, cls.getCanonicalName());
                // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, cls + " is annotation = " + (cls.isAnnotation()));

                // ensure that the freshly loaded class is an annotation, and has the @Target annotation
                if(cls.isAnnotation() && cls.getAnnotation(Target.class) != null) {
                    // scan through the @Target annotation for its values
                    for(ElementType element : cls.getAnnotation(Target.class).value())
                    {
                        // ensure that the @Target annotation has the value of ElementType.TYPE_USE
                        if(element.equals(ElementType.TYPE_USE))
                        {
                            // if so, process the annotation and get its equivalent AnnotationMirror
                            Class<? extends Annotation> annoClass = cls.asSubclass(Annotation.class);
                            // createAnnotationMirrorFromClass is defined by each individual checker
                            // returns an annotation mirror if the checker handles it, or 
                            // null if it either doesn't handle it or fails to produce an annotation mirror
                            AnnotationMirror convertedMirror = createAnnotationMirrorFromClass(annoClass);

                            if(convertedMirror != null) {
                                // convert the annotation class into an annotation mirror, add the class and the mirror into the hashmap
                                annos.put(annoClass, convertedMirror);
                            }

                            break;
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                //TODO: give better feedback
                e.printStackTrace();
            }
        }

        return annos;
    }

    // Retrieves the annotation class file names from the qual directory of a particular checker
    private final Set<String> getAnnotationNames() {
        Set<String> results = null;

        // debug output
        //processingEnv.getMessager().printMessage(Kind.NOTE, "Getting annotation names for " + packageName);
        //processingEnv.getMessager().printMessage(Kind.NOTE, 
        //        "\n URL: " + resourceURL.toString() +
        //        "\n File: " + resourceURL.getFile() +
        //        "\n Package: " + packageName);

        // if the Checker class file is contained within a jar, which means the whole checker is shipped or loaded as a jar file, then process
        // the package as a jar file and load the annotations contained within the jar
        if(resourceURL.getProtocol().equals("jar"))
        {
            try {
                JarURLConnection connection = (JarURLConnection) resourceURL.openConnection();
                JarFile jarFile = connection.getJarFile();

                // debug output
                // String jarPath = jarFile.getName();
                // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "JAR Path: " + jarPath);

                // display Jar class names inside the jar file within the particular package
                results = getAnnotationNamesFromJar(jarFile);
                //for(String annotationClassName : results) {
                //    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, annotationClassName);
                //}
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //  Error: cannot open connection to Jar file, or cannot retrieve the jar file from connection
                e.printStackTrace();
            }
        }
        // else if the Checker class file is found within the file system itself within some directory (usually development build directories), then
        // process the package as a file directory in the file system and load the annotations contained in the qual directory
        else if(resourceURL.getProtocol().equals("file")) {
            results = new HashSet<String>();
            // open up the directory
            File packageDir = new File(resourceURL.getFile());
            for (File file : packageDir.listFiles()) {
                String fileName = file.getName();
                // filter for just class files
                if(fileName.endsWith(CLASS_SUFFIX)) {
                    String annotationClassName = fileName.substring(0, fileName.lastIndexOf('.'));
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, annotationClassName);
                    results.add(annotationClassName);
                }
                //else {
                //    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, fileName);
                //}
            }
        }

        return results;
    }

    private final Set<String> getAnnotationNamesFromJar(JarFile jar) {
        Set<String> annos = new HashSet<String>();

        // get an enumeration iterator for all the content entries in the jar file
        Enumeration<JarEntry> jarEntries = jar.entries();

        // enumerate through the entries
        while(jarEntries.hasMoreElements()) {
            JarEntry je = jarEntries.nextElement();
            // filter out directories and non-class files
            if(je.isDirectory() || !je.getName().endsWith(CLASS_SUFFIX)){
                continue;
            }

            // get rid of the .class suffix
            String className = je.getName().substring(0, je.getName().lastIndexOf('.'));
            // convert path notation to class notation 
            className = className.replace(SLASH, DOT);

            // filter for qual package and only add class names that are relevant
            if(className.startsWith(packageName)) {
                // remove qual package prefix, keeping only the class name
                className = className.substring( (packageName + DOT).length() );
                // add to set
                annos.add(className);
            }
        }

        //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "loaded number of classes: " + annos.size());
        //for(String s : annos) {
        //    if(s.startsWith(this.getClass().getPackage().getName() + qualDir)) {
        //        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "loaded: " + s);
        //    }
        //}

        return annos;
    }
}
