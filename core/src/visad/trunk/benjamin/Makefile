# makefile for visad/benjamin/Galaxy
#  (hydrostatic equilibrium galaxy)

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

default: Galaxy.class libGalaxy.so

# these Fortran functions compile correctly under sparc Solaris
# 2.5.1
#
# if you port them you may need to change 'recl=352' in so_read_1.f
# and 'recl=76' in re_read_1.f, which are in bytes on Solaris (but
# were in words on Irix 5 for VisAD 1.1)
#
OBJS = mw.o species_rd.o errmsg.o tcdensity.o dmdsm.o util.o profile.o galtosol.o
SOURCE = mw.f species_rd.f errmsg.f tcdensity.f dmdsm.f util.f profile.f galtosol.f
INCLUDE = dimen.h const.h atomic.h galstruct.h

libGalaxy.so: GalaxyImp.o $(OBJS)
	${SHFLD} ${SHFLDFLAGS} \
		-o libGalaxy.so GalaxyImp.o \
		$(OBJS) ${SHFLIBS}

GalaxyImp.o: visad_benjamin_Galaxy.h

visad_benjamin_Galaxy.h: Galaxy.class
	$(JH) -jni visad.benjamin.Galaxy

Galaxy.class: Galaxy.java
	$(JC) $(JFLAGS) Galaxy.java

$(OBJS): $(INCLUDE) $(SOURCE)
	$(FC) -c $(FFLAGS) $*.f

clean:
	$(RM) visad_benjamin_Galaxy.h
	$(RM) GalaxyImp.o ${OBJS}
	$(RM) libGalaxy.so
	$(RM) Galaxy*.class
	$(RM) *~
