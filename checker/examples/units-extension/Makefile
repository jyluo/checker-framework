FILES=qual/Frequency.java \
  qual/FrequencyRelations.java \
  qual/Hz.java \
  qual/kHz.java

JAVAOPTS=-source 7 -target 7  -classpath .:../../dist/checker.jar

JAVAC?=../../bin/javac

# gets the full path to the directory of the make file, which is also the root dir of the qual folder
# for custom projects, it is best to encode the full root path as a variable
PROJECTDIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))

all: compile named-quals-test qual-folder-test clean

# compile qualifiers
compile:
	$(JAVAC) $(JAVAOPTS) $(FILES)

# manual demo
demo: compile
	@echo "***** This command is expected to produce errors on line 15 & 71:"
	$(JAVAC) -classpath $(PROJECTDIR) -AprintErrorStack -processor org.checkerframework.checker.units.UnitsChecker -AunitsDirs=$(PROJECTDIR) UnitsExtensionDemo.java

# test case for using externally defined units by explicitly naming them using the -Aunits option
named-quals-test:
	$(JAVAC) -classpath $(PROJECTDIR) -AprintErrorStack -processor org.checkerframework.checker.units.UnitsChecker -Aunits=qual.Hz,qual.kHz,qual.Frequency UnitsExtensionDemo.java > Out.txt 2>&1 || true
	diff -u Expected.txt Out.txt
	-rm Out.txt

# test case for using externally defined units by loading them from a directory using the -AunitsDirs option
qual-folder-test:
	$(JAVAC) -classpath $(PROJECTDIR) -AprintErrorStack -processor org.checkerframework.checker.units.UnitsChecker -AunitsDirs=$(PROJECTDIR) UnitsExtensionDemo.java > Out.txt 2>&1 || true
	diff -u Expected.txt Out.txt
	-rm Out.txt

# test clean up
clean:
	-rm qual/*.class

# manual infer demo
infer: compile
	$(CHECKERFRAMEWORK)/checker/bin/infer-and-annotate.sh "UnitsChecker" ../../dist/checker.jar:$(PROJECTDIR) -AprintErrorStack -classpath $(PROJECTDIR) -AunitsDirs=$(PROJECTDIR) UnitsExtensionDemo.java
	cp DemoBackup.java UnitsExtensionDemo.java