# makefile for visad/aune/ShallowFluid

# JAVADIR should be the top-level JDK1.2 directory
# JAVAINCS should list all the necessary include files under JAVADIR
#
JAVADIR = /opt/java
JAVAINCS = -I${JAVADIR}/include -I${JAVADIR}/include/solaris
 
# JC is the Java compiler
# JFLAGS specifies special flags for the Java compiler
#
JC = javac
JFLAGS =

# these hold the flags needed to tell the C and Fortran compilers
# to build shared object files
#
SHCFLAGS =
SHFFLAGS =
 
# SHLD is the shared library loader
# SHLDFLAGS are the flags needed to tell SHLD to build a shared library
#
SHLD = f77
SHLDFLAGS = -G -Qoption ld -z,defs
 
# we want to build shared objects by default
#
CFLAGS = $(JAVAINCS) $(SHCFLAGS)
FFLAGS = $(SHFFLAGS)
 
# these Fortran functions compile correctly under sparc Solaris
# 2.5.1
#
FORTRANS = asfilt.o cyclic.o makdat.o shalstep.o x1der4.o chanel.o \
	init.o re_read_1.o switch.o citcis.o kdiff.o rflctn.o tanflt.o
 
# program used to clean up the results of previous builds
#
RM = rm -f
 
############################################################################
# You shouldn't need to change anything beyond this point
############################################################################

default: ShallowFluid.class libShallowFluid.so

libShallowFluid.so: ShallowFluidImp.o $(FORTRANS)
	${SHLD} ${SHLDFLAGS} -o libShallowFluid.so \
		ShallowFluidImp.o $(FORTRANS) \
		-lM77 -lF77 -lsunmath -lm -lc

ShallowFluidImp.o: visad_aune_ShallowFluid.h

visad_aune_ShallowFluid.h: ShallowFluid.class
	javah -jni visad.aune.ShallowFluid

ShallowFluid.class: ShallowFluid.java
	$(JC) $(JFLAGS) ShallowFluid.java

clean:
	$(RM) visad_aune_ShallowFluid.h
	$(RM) *.o
	$(RM) *.so
	$(RM) *.class

############################################################################
# Packaging rules
############################################################################

TAR_FILES = \
	visad/aune/README.aune				\
	visad/aune/Makefile				\
	visad/aune/shsize.fcm				\
	visad/aune/*.java				\
	visad/aune/*.f					\
	visad/aune/*.c					\
	visad/aune/*.h

tar:
	cd ../.. ; \
	tar -cvf visad.aune.tar $(TAR_FILES) ; \
	compress visad.aune.tar ; \
	mv visad.aune.tar.Z visad

jar:
	cd ../.. ; \
	jar cvf visad.aune.jar $(TAR_FILES) ; \
	mv visad.aune.jar visad

