# makefile for visad/benjamin/Galaxy
#  (hydrostatic equilibrium galaxy)
 
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
FORTRANS = mw.o species_rd.o errmsg.o tcdensity.o dmdsm.o util.o profile.o galtosol.o
SOURCE = mw.f species_rd.f errmsg.f tcdensity.f dmdsm.f util.f profile.f galtosol.f
INCLUDE = dimen.h const.h atomic.h galstruct.h
 

# program used to clean up the results of previous builds
#
RM = rm -f

default: Galaxy.class libGalaxy.so

libGalaxy.so: GalaxyImp.o $(FORTRANS)
	${SHLD} ${SHLDFLAGS} -o libGalaxy.so \
		GalaxyImp.o $(FORTRANS) \
		$(LIBS) -lM77 -lF77 -lsunmath -lm -lc

$(FORTRANS) : $(INCLUDE) $(SOURCE)
	f77 -c $(FFLAGS) $*.f

GalaxyImp.o: visad_benjamin_Galaxy.h

visad_benjamin_Galaxy.h: Galaxy.class
	javah -jni visad.benjamin.Galaxy

Galaxy.class: Galaxy.java
	javac Galaxy.java

clean:
	$(RM) visad_benjamin_Galaxy.h
	$(RM) *.o
	$(RM) *.so
	$(RM) *.class

