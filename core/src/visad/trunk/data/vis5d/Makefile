# makefile for visad/data/vis5d/Vis5DForm

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
# If DEC or Linux, (Little-endian), add -DLITTLE to CFLAGS
#
CFLAGS = $(JAVAINCS) $(SHCFLAGS)
FFLAGS = $(SHFFLAGS)

.SUFFIXES : .java .class

.java.class:
	javac $<

default: Vis5DForm.class libvis5d.so

OBJS = v5d_read.o v5d.o binio.o

# you may need to change these include directory paths
libvis5d.so: $(OBJS)
	${SHLD} ${SHLDFLAGS} -o libvis5d.so $(OBJS) \
		-lm -lc

v5d_read.o: v5d_read.c visad_data_vis5d_Vis5DForm.h binio.h v5d.h
	$(CC) $(CFLAGS) $(JAVAINCS) -c v5d_read.c

visad_data_vis5d_Vis5DForm.h: Vis5DForm.class
	javah -jni visad.data.vis5d.Vis5DForm

Vis5DForm.class: Vis5DForm.java

v5d.o:      v5d.c v5d.h vis5d.h
	$(CC) $(CFLAGS) -c v5d.c

binio.o:    binio.c binio.h
	$(CC) $(CFLAGS) -c binio.c

clean:
	-rm -f libvis5d.so
	-rm -f $(OBJS)
	-rm -f visad_data_vis5d_Vis5DForm.h
	-rm -f Vis5DForm*.class
