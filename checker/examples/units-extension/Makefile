JAVAC?=../../bin/javac

FILES=Frequency.java \
  FrequencyRelations.java \
  Hz.java

all: compile check

compile: $(FILES)
	$(JAVAC) $(FILES)

check: compile $(FILES)
	@echo "***** This command is expected to produce errors on line 9:"
	$(JAVAC) -Xbootclasspath/p:. -AprintErrorStack -processor org.checkerframework.checker.units.UnitsChecker -Aunits=Hz,Frequency Demo.java
