# makefile for visad/aune/ShallowFluid

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
		$(LIBS) -lM77 -lF77 -lsunmath -lm -lc

ShallowFluidImp.o: visad_aune_ShallowFluid.h

visad_aune_ShallowFluid.h: ShallowFluid.class
	javah -jni visad.aune.ShallowFluid

ShallowFluid.class: ShallowFluid.java
	javac ShallowFluid.java

clean:
	$(RM) visad_paoloa_GoesRetrieval.h
	$(RM) visad_paoloa_GoesCollaboration.h
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

