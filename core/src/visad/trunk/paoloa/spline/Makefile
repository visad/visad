# makefile for visad/paoloa/spline

# JAVADIR should be the top-level JDK1.2 directory
# JAVAARCH should be the local machine architecture
# JAVAINCS should list all the necessary include files under JAVADIR
#
JAVADIR = /opt/java
JAVAARCH = solaris
JAVAINCS = -I${JAVADIR}/include -I${JAVADIR}/include/${JAVAARCH}

# JC is the Java compiler
# JFLAGS specifies special flags for the Java compiler
# JH is the Java header file generator (javah)
#
JC = ${JAVADIR}/bin/javac
JFLAGS =
JH = ${JAVADIR}/bin/javah

# these hold the flags needed to tell the C and Fortran compilers
# to build shared object files
#
SHCFLAGS =
SHFFLAGS =

# this is the list of Fortran libraries referenced
# by objects in the shared library
#
SHFLIBS = -lM77 -lF77 -lsunmath -lm -lc

# SHLD is the shared library loader
# SHLDFLAGS are the flags needed to tell SHLD to build a shared library
#
SHLD = $(CC)
SHLDFLAGS = -G -z defs

# SHFLD is the Fortran shared library loader
# SHFLDFLAGS are the flags needed to tell SHFLD to build a shared library
#
SHFLD = $(FC)
SHFLDFLAGS = -G -Qoption ld -z,defs

# we want to build shared objects by default
#
CFLAGS = $(JAVAINCS) $(SHCFLAGS)
FFLAGS = $(SHFFLAGS)

# program used to clean up the results of previous builds
#
RM = rm -f

############################################################################
# You shouldn't need to change anything beyond this point
############################################################################

.SUFFIXES : .java .class

.java.class:
	$(JC) $(JFLAGS) $<

default: Spline.class Spline2D.class libSpline.so libSpline2D.so

libSpline.so: SplineImp.o gcvspl.o
	${SHLD} ${SHLDFLAGS} -o libSpline.so SplineImp.o \
		gcvspl.o ${SHFLIBS}

SplineImp.o: visad_paoloa_spline_Spline.h

visad_paoloa_spline_Spline.h: Spline.class
	$(JH) -jni visad.paoloa.spline.Spline
	touch visad_paoloa_spline_Spline.h

Spline.class: Spline.java
	$(JC) $(JFLAGS) Spline.java

libSpline2D.so: Spline2DImp.o tpspline.o
	${SHLD} ${SHLDFLAGS} -o libSpline2D.so Spline2DImp.o \
		tpspline.o ${SHFLIBS}

Spline2DImp.o: visad_paoloa_spline_Spline2D.h

visad_paoloa_spline_Spline2D.h: Spline2D.class
	$(JH) -jni visad.paoloa.spline.Spline2D
	touch visad_paoloa_spline_Spline2D.h

Spline2D.class: Spline2D.java
	$(JC) $(JFLAGS) Spline2D.java

clean:
	$(RM) visad_paoloa_spline_Spline.h
	$(RM) visad_paoloa_spline_Spline2D.h
	$(RM) *.o
	$(RM) *.so
	$(RM) *.class
