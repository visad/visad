# makefile for visad/paoloa/spline
 
# JAVADIR should be the top-level JDK1.2 directory
# JAVAINCS should list all the necessary include files under JAVADIR
#
JAVADIR = /opt/java
JAVAINCS = -I${JAVADIR}/include -I${JAVADIR}/include/solaris
 
# LIBS lists the directories for ALL the shared libraries needed by the
# Fortran code.  If the GoesRetrieval demo doesn't work, it's likely
# that you need to add one or more directories to this list.
#
LIB1 = /opt/SUNWspro/lib
LIB2 = /opt/SUNWspro/SC3.0.1/lib
LIB3 = /usr/ucblib
LIBS = -L${LIB1} -R${LIB1} -L${LIB2} -R${LIB2} -L${LIB3} -R${LIB3}
 
# these hold the flags needed to tell the C and Fortran compilers
# to build shared object files
#
SHCFLAGS =
SHFFLAGS =
 
# SHLD is the shared library loader
# SHLDFLAGS are the flags needed to tell SHLD to build a shared library
#
SHLD = ld
SHLDFLAGS = -G -z defs
 
# we want to build shared objects by default
#
CFLAGS = $(JAVAINCS) $(SHCFLAGS)
FFLAGS = $(SHFFLAGS)
 
# these Fortran functions compile correctly under sparc Solaris
# 2.5.1
#
# if you port them you may need to change 'recl=352' in so_read_1.f
# and 'recl=76' in re_read_1.f, which are in bytes on Solaris (but
# were in words on Irix 5 for VisAD 1.1)
#
FORTRANS = gcvspl.o tpspline.o
SOURCE = gcvspl.f tpspline.f
#INCLUDE = 
 

# program used to clean up the results of previous builds
#
RM = rm -f

default: Spline.class Spline2D.class Spline.so Spline2D.so

Spline.so: SplineImp.o $(FORTRANS)
	${SHLD} ${SHLDFLAGS} -o libSpline.so \
		SplineImp.o $(FORTRANS) \
		$(LIBS) -lM77 -lF77 -lsunmath -lm -lc

SplineImp.o: visad_paoloa_spline_Spline.h

visad_paoloa_spline_Spline.h: Spline.class
	javah -jni visad.paoloa.spline.Spline

Spline.class: Spline.java
	javac Spline.java

Spline2D.so: Spline2DImp.o $(FORTRANS)
	${SHLD} ${SHLDFLAGS} -o libSpline2D.so \
		Spline2DImp.o $(FORTRANS) \
		$(LIBS) -lM77 -lF77 -lsunmath -lm -lc

Spline2DImp.o: visad_paoloa_spline_Spline2D.h

visad_paoloa_spline_Spline2D.h: Spline2D.class
	javah -jni visad.paoloa.spline.Spline2D

Spline2D.class: Spline2D.java
	javac Spline2D.java

$(FORTRANS) : $(SOURCE)
	f77 -c $(FFLAGS) $*.f

clean:
	$(RM) visad_paoloa_spline_Spline.h
	$(RM) visad_paoloa_spline_Spline2D.h
	$(RM) *.o
	$(RM) *.so

