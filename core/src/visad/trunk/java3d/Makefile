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

default: DisplayImplJ3D.class TwoDDisplayRendererJ3D.class \
	ImmersaDeskDisplayRendererJ3D.class VisADBranchGroup.class \
	libTrackdAPI.so

# these Fortran functions compile correctly under sparc Solaris
# 2.5.1
#
# if you port them you may need to change 'recl=352' in so_read_1.f
# and 'recl=76' in re_read_1.f, which are in bytes on Solaris (but
# were in words on Irix 5 for VisAD 1.1)
#
OBJS = 

libTrackdAPI.so: TrackdAPI.o $(OBJS)
	${SHFLD} ${SHFLDFLAGS} \
		-o libTrackdAPI.so TrackdAPI.o \
		$(OBJS) ${SHFLIBS}

TrackdAPI.o: visad_java3d_TrackdJNI.h

visad_java3d_TrackdJNI.h: TrackdJNI.class
	$(JH) -jni visad.java3d.TrackdJNI

clean:
	$(RM) visad_java3d_TrackdJNI.h
	$(RM) TrackdAPI.o ${OBJS}
	$(RM) libTrackdAPI.so
	$(RM) *.class
	$(RM) *~

