# makefile for visad/paoloa/GoesCollaboration

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

default: Nasti.class GoesCollaboration.class libGoesCollaboration.so

# these Fortran functions compile correctly under sparc Solaris
# 2.5.1
#
# if you port them you may need to change 'recl=352' in so_read_1.f
# and 'recl=76' in re_read_1.f, which are in bytes on Solaris (but
# were in words on Irix 5 for VisAD 1.1)
#
OBJS = change_profil.o ev_diff_prof.o goesrte_2.o so_read_1.o \
	dbdtgx_1.o get_profil.o re_read_1.o

libGoesCollaboration.so: GoesCollaborationImp.o $(OBJS)
	${SHFLD} ${SHFLDFLAGS} \
		-o libGoesCollaboration.so GoesCollaborationImp.o \
		$(OBJS) ${SHFLIBS}

GoesCollaborationImp.o: visad_paoloa_GoesCollaboration.h

visad_paoloa_GoesCollaboration.h: GoesCollaboration.class
	$(JH) -jni visad.paoloa.GoesCollaboration

GoesCollaboration.class: GoesCollaboration.java
Nasti.class: Nasti.java

clean:
	$(RM) visad_paoloa_GoesCollaboration.h
	$(RM) GoesCollaborationImp.o ${OBJS}
	$(RM) libGoesCollaboration.so
	$(RM) GoesCollaboration*.class
	$(RM) Nasti*.class

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
	$(TAR_NO_DATA_FILES)				\
	visad/paoloa/goesrtcf				\
	visad/paoloa/data_obs_1.dat			\
	visad/paoloa/data_sou_1.dat

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
