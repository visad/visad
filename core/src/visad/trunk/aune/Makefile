# makefile for visad/aune/ShallowFluid

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

default: ShallowFluid.class libShallowFluid.so

# these Fortran functions compile correctly under sparc Solaris
# 2.5.1
#
OBJS = asfilt.o cyclic.o makdat.o shalstep.o x1der4.o chanel.o \
	init.o re_read_1.o switch.o citcis.o kdiff.o rflctn.o tanflt.o

libShallowFluid.so: ShallowFluidImp.o $(OBJS)
	${SHFLD} ${SHFLDFLAGS} \
		-o libShallowFluid.so ShallowFluidImp.o \
		$(OBJS) ${SHFLIBS}

ShallowFluidImp.o: visad_aune_ShallowFluid.h

visad_aune_ShallowFluid.h: ShallowFluid.class
	$(JH) -jni visad.aune.ShallowFluid

ShallowFluid.class: ShallowFluid.java

clean:
	$(RM) visad_aune_ShallowFluid.h
	$(RM) ShallowFluidImp.o ${OBJS}
	$(RM) libShallowFluid.so
	$(RM) ShallowFluid*.class

############################################################################
# Packaging rules
############################################################################

TAR_FILES = \
	visad/aune/README.aune				\
	visad/aune/DATE					\
	visad/aune/Makefile				\
	visad/aune/shsize.fcm				\
	visad/aune/*.java				\
	visad/aune/*.f					\
	visad/aune/*.c					\
	visad/aune/*.h

tar:
	date > DATE ; \
	cd ../.. ; \
	tar -cvf visad.aune.tar $(TAR_FILES) ; \
	compress visad.aune.tar ; \
	mv visad.aune.tar.Z visad

jar:
	date > DATE ; \
	cd ../.. ; \
	jar cvf visad.aune.jar $(TAR_FILES) ; \
	mv visad.aune.jar visad
