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
JAVA = 			\
	Egrep.java	\
	Tests.java	\
	Grep.java	\
	REApplet.java	\
	RETest.java

# Classes used by the JavaCC parser
CLASSES := $(JAVA:%.java=%.class)

#
# Build the *.java files from the grammar files and then build the *.class
# files. 

all: $(CLASSES)



# Remove all classes
clean:
	-$(RM) *.class
	-$(RM) *~
	-$(RM) *%


distclean: clean

# Rules

%.class : %.java
	$(JAVAC) $(JAVAFLAGS) $<
