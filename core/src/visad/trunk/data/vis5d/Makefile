# makefile for visad/data/vis5d/Vis5DForm

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
# If DEC or Linux, (Little-endian), add -DLITTLE to CFLAGS
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

default: Vis5DForm.class libvis5d.so

OBJS = v5d_read.o v5d.o binio.o

libvis5d.so: $(OBJS)
	${SHLD} ${SHLDFLAGS} -o libvis5d.so $(OBJS) -lm -lc

v5d_read.o: v5d_read.c visad_data_vis5d_Vis5DForm.h binio.h v5d.h
	$(CC) $(CFLAGS) $(JAVAINCS) -c v5d_read.c

visad_data_vis5d_Vis5DForm.h: Vis5DForm.class
	$(JH) -jni visad.data.vis5d.Vis5DForm

Vis5DForm.class: Vis5DForm.java

v5d.o:      v5d.c v5d.h vis5d.h
binio.o:    binio.c binio.h

clean:
	-${RM} visad_data_vis5d_Vis5DForm.h
	-${RM} $(OBJS)
	-${RM} libvis5d.so
	-${RM} Vis5DForm*.class
