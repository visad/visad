# default target is "all" -- DO NOT CHANGE THIS!!!!
#
all::

############################################################################
# Library-specific definitions
############################################################################

# base name for shared libraries
#
SLIB = Spline
SLIB2 = Splint2D

# object files to include in the shared library
#
OBJS = gcvspl.o SplineImp.o
OBJS2 = tpspline.o Spline2DImp.o

# name of the generated JNI header files
#
JNI_HEADERS = visad_paoloa_spline_Spline.h visad_paoloa_spline_Spline2D.h

# rule to build the generated JNI header files from the appropriate classes
#
visad_paoloa_spline_Spline.h: Spline.class
	$(JH) -jni visad.paoloa.spline.Spline
visad_paoloa_spline_Spline2D.h: Spline2D.class
	$(JH) -jni visad.paoloa.spline.Spline2D

# object files which depend on the generated JNI files
#
SplineImp.o: visad_paoloa_spline_Spline.h
Spline2DImp.o: visad_paoloa_spline_Spline2D.h

# special header/library files needed by this package
#
PKGINCS =
PKGLIBS =

# set LINK_F77 to 'true' if any Fortran .o files are
# to be included in the shared library
#
LINK_F77 = true

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

############################################################################
# Build tool definitions
############################################################################

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

default: all

all::
	@for j in *.java; do						\
	  b=`basename $$j .java`;					\
	  c=$$b.class;							\
	  compile=false;						\
	  if [ ! -f $$c ]; then						\
	    compile=true;						\
	  else								\
	    set `ls -td $$j $$c` X;					\
	    if [ $$c != $$1 ]; then					\
	      compile=true;						\
	    fi;								\
	  fi;								\
	  if $$compile; then						\
	    (echo "$(JC) $(JFLAGS) $$j" &&				\
	     $(JC) $(JFLAGS) $$j || true);				\
	  fi;								\
	  case "$$b" in							\
	  *Remote*Impl)							\
	    set `ls -t $$b.class $${b}_Stub.class 2>/dev/null || true` X; \
	    if [ $${b}_Stub.class != $$1 ]; then			\
	      (echo "${RMIC} -d $$root $$i.$$b" &&			\
	       ${RMIC} -d $$root $$i.$$b || true);			\
	    fi;								\
	    ;;								\
	  esac;								\
	done

all::
	@UNAME=`uname -s`; \
	case "$$UNAME" in \
	IRIX*) arch=irix;; \
	SunOS*) arch=solaris;; \
	Linux*) arch=linux;; \
	*) echo "Unknown system type $$UNAME"; exit 1;; \
	esac; \
	make \
		JAVADIR="$(JAVADIR)" \
		JC="$(JC)" \
		JFLAGS="$(JFLAGS)" \
		$$arch

.c.o:
	$(CC) $(SHCFLAGS) -c $*.c $(JAVAINCS) $(PKGINCS)

shared_library:: lib$(SLIB).so

lib$(SLIB).so: $(OBJS)
	@if [ -z "$(LINK_F77)" ]; then \
	  echo $(SHLD) $(SHLDFLAGS) $(OBJS) $(PKGLIBS) $(SYSLIBS) \
		-o lib$(SLIB).so; \
	  $(SHLD) $(SHLDFLAGS) $(OBJS) $(PKGLIBS) $(SYSLIBS) \
		-o lib$(SLIB).so; \
	else \
	  echo $(SHFLD) $(SHFLDFLAGS) $(OBJS) $(PKGLIBS) $(SYSLIBS) \
		-o lib$(SLIB).so; \
	  $(SHFLD) $(SHFLDFLAGS) $(OBJS) $(PKGLIBS) $(SYSLIBS) \
		-o lib$(SLIB).so; \
	fi

shared_library:: lib$(SLIB2).so

lib$(SLIB2).so: $(OBJS2)
	@if [ -z "$(LINK_F77)" ]; then \
	  echo $(SHLD) $(SHLDFLAGS) $(OBJS2) $(PKGLIBS) $(SYSLIBS) \
		-o lib$(SLIB2).so; \
	  $(SHLD) $(SHLDFLAGS) $(OBJS2) $(PKGLIBS) $(SYSLIBS) \
		-o lib$(SLIB2).so; \
	else \
	  echo $(SHFLD) $(SHFLDFLAGS) $(OBJS2) $(PKGLIBS) $(SYSLIBS) \
		-o lib$(SLIB2).so; \
	  $(SHFLD) $(SHFLDFLAGS) $(OBJS2) $(PKGLIBS) $(SYSLIBS) \
		-o lib$(SLIB2).so; \
	fi

clean:
	rm -f lib$(SLIB).so lib$(SLIB2).so
	rm -f so_locations
	$(RM) $(OBJS) $(OBJS2)
	$(RM) $(JNI_HEADERS)
	$(RM) *.class
	$(RM) *~

solaris: required_macros
	@make \
		JAVAARCH=solaris \
		SHLD="$(CC)" \
		SHLDFLAGS="-G -z defs" \
		SHFLD="$(FC)" \
		SHFLDFLAGS="-G -Qoption ld -z,defs" \
		SYSLIBS="-lM77 -lF77 -lsunmath -lnsl -lm -lc" \
		JAVADIR="$(JAVADIR)" \
		JC="$(JC)" \
		JFLAGS="$(JFLAGS)" \
		shared_library

irix: required_macros
	@make \
		JAVAARCH=irix \
		SHLD="$(CC)" \
		SHLDFLAGS="-shared -Wl,-x" \
		SHFLD="$(FC)" \
		SHFLDFLAGS="" \
		SYSLIBS="-lm" \
		JAVADIR="$(JAVADIR)" \
		JC="$(JC)" \
		JFLAGS="$(JFLAGS)" \
		shared_library

linux: required_macros
	@make \
		JAVAARCH=linux \
		SHCFLAGS="-fPIC -DPIC" \
		SHLD="cc" \
		SHLDFLAGS="-shared" \
		SHFLD="$(FC)" \
		SHFLDFLAGS="-shared" \
		SYSLIBS="-lm" \
		JAVADIR="$(JAVADIR)" \
		JC="$(JC)" \
		JFLAGS="$(JFLAGS)" \
		shared_library

required_macros::
