# makefile for visad/benjamin/Galaxy
#  (hydrostatic equilibrium galaxy)
 
# JAVADIR should be the top-level JDK1.2 directory
# JAVAINCS should list all the necessary include files under JAVADIR
#
JAVADIR = /opt/java
JAVAINCS = -I${JAVADIR}/include -I${JAVADIR}/include/solaris
 
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
		-lM77 -lF77 -lsunmath -lm -lc

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

