# makefile for visad/paoloa/nesti/Nesti

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
FORTRANS = readprof.o nastirte.o

# program used to clean up the results of previous builds
#
RM = rm -f

############################################################################
# You shouldn't need to change anything beyond this point
############################################################################

default: Nesti.class libNesti.so 

libNesti.so: NestiImp.o $(FORTRANS)
	${SHLD} ${SHLDFLAGS} -o libNesti.so \
		NestiImp.o $(FORTRANS) \
		$(LIBS) -lM77 -lF77 -lsunmath -lm -lc

NestiImp.o: visad_paoloa_nesti_Nesti.h

visad_paoloa_nesti_Nesti.h: Nesti.class
	javah -jni visad.paoloa.nesti.Nesti

Nesti.class: Nesti.java
	javac Nesti.java

clean:
	$(RM) visad_paoloa_nesti_Nesti.h
	$(RM) *.o
	$(RM) *.so

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
