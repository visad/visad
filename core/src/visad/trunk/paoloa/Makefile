# makefile for visad/paoloa/GoesCollaboration

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
# if you port them you may need to change 'recl=352' in so_read_1.f
# and 'recl=76' in re_read_1.f, which are in bytes on Solaris (but
# were in words on Irix 5 for VisAD 1.1)
#
FORTRANS = change_profil.o ev_diff_prof.o goesrte_2.o so_read_1.o \
	dbdtgx_1.o get_profil.o re_read_1.o

# program used to clean up the results of previous builds
#
RM = rm -f

############################################################################
# You shouldn't need to change anything beyond this point
############################################################################

default: Nasti.class GoesCollaboration.class libGoesCollaboration.so

libGoesCollaboration.so: GoesCollaborationImp.o $(FORTRANS)
	${SHLD} ${SHLDFLAGS} -o libGoesCollaboration.so \
		GoesCollaborationImp.o $(FORTRANS) \
		-lM77 -lF77 -lsunmath -lm -lc

GoesCollaborationImp.o: visad_paoloa_GoesCollaboration.h

visad_paoloa_GoesCollaboration.h: GoesCollaboration.class
	javah -jni visad.paoloa.GoesCollaboration

GoesCollaboration.class: GoesCollaboration.java
	$(JC) $(JFLAGS) GoesCollaboration.java

Nasti.class: Nasti.java
	$(JC) $(JFLAGS) Nasti.java

clean:
	$(RM) visad_paoloa_GoesCollaboration.h
	$(RM) *.o
	$(RM) *.so
	$(RM) *.class

############################################################################
# Packaging rules
############################################################################

TAR_NO_DATA_FILES = \
	visad/paoloa/README.paoloa			\
	visad/paoloa/DATE				\
	visad/paoloa/Makefile				\
	visad/paoloa/*.java				\
	visad/paoloa/*.f				\
	visad/paoloa/*.c				\
	visad/paoloa/*.h

tar_no_data:
	date > DATE ; \
	cd ../.. ; \
	tar -cvf visad.paoloa.no_data.tar $(TAR_NO_DATA_FILES) ; \
	compress visad.paoloa.no_data.tar ; \
	mv visad.paoloa.no_data.tar.Z visad

jar_no_data:
	date > DATE ; \
	cd ../.. ; \
	jar cvf visad.paoloa.no_data.jar $(TAR_NO_DATA_FILES) ; \
	mv visad.paoloa.no_data.jar visad

TAR_FILES = \
	visad/paoloa/README.paoloa			\
	visad/paoloa/DATE				\
	visad/paoloa/Makefile				\
	visad/paoloa/goesrtcf				\
	visad/paoloa/data_obs_1.dat			\
	visad/paoloa/data_sou_1.dat			\
	visad/paoloa/*.java				\
	visad/paoloa/*.f				\
	visad/paoloa/*.c				\
	visad/paoloa/*.h

tar:
	date > DATE ; \
	cd ../.. ; \
	tar -cvf visad.paoloa.tar $(TAR_FILES) ; \
	compress visad.paoloa.tar ; \
	mv visad.paoloa.tar.Z visad

jar:
	date > DATE ; \
	cd ../.. ; \
	jar cvf visad.paoloa.jar $(TAR_FILES) ; \
	mv visad.paoloa.jar visad
