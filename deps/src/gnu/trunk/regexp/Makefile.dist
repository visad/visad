
# As long as javac, javacc, and javadoc are in the path, no configure script
# should be needed.

JAVA = java
JAVAC = javac
JAVAFLAGS =
JAVACC = javacc
JAVADOC = javadoc
JAVADOCFLAGS = -version -author

# testsuite specifics
RUNTEST = runtest
RUNTESTFLAGS = #--all --verbose

# Simple dialog box classes
JAVA = 				\
	 RE.java \
	 CharIndexed.java \
	 CharIndexedCharArray.java \
	 CharIndexedInputStream.java \
	 CharIndexedString.java \
	 CharIndexedStringBuffer.java \
	 REException.java \
	 REFilterInputStream.java \
	 REMatch.java \
	 REMatchEnumeration.java \
	 RESyntax.java \
	 REToken.java \
	 RETokenAny.java \
	 RETokenBackRef.java \
	 RETokenChar.java \
	 RETokenEnd.java \
	 RETokenOneOf.java \
	 RETokenPOSIX.java \
	 RETokenRange.java \
	 RETokenRepeated.java \
	 RETokenStart.java 


# Classes used by the JavaCC parser
CLASSES := $(JAVA:%.java=%.class)

#
# Build the *.java files from the grammar files and then build the *.class
# files. 

all: util $(CLASSES)

.PHONY: util
util:
	(cd util; $(MAKE) $(MFLAGS) all)

# Remove all classes
clean:
	-$(RM) *.class
	-$(RM) *~
	-$(RM) *%
	-$(RM) -r doc/[a-z]*
	(cd util; $(MAKE) $(MFLAGS) clean)


distclean: clean
	(cd util; $(MAKE) $(MFLAGS) distclean)

# Rules

%.class : %.java
	$(JAVAC) $(JAVAFLAGS) $<
