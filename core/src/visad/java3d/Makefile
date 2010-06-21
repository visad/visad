# default target is "all" -- DO NOT CHANGE THIS!!!!
#
all::

############################################################################
# Library-specific definitions
############################################################################

# this should point to the directory holding the TrackD header file
#
# TRACKD_INCDIR=.

# this should point to the TrackD library
#
# TRACKD_LIBDIR=.

# this should point to the GCC standard library directory
#
GCC_LIBDIR=`gcc -print-libgcc-file-name | sed 's/\/[^\/]*$$//'`

# this is the list of libraries needed for the TrackD JNI shim
#
TRACKD_LIB = -L$(TRACKD_LIBDIR) -ltrackdAPI \
	-L$(GCC_LIBDIR) -lstdc++ -lgcc \
	-lm -lc

# base name for shared library
#
SLIB = TrackdAPI

# object files to include in the shared library
#
OBJS = TrackdAPI.o

# name of the generated JNI header file
#
JNI_HEADERS = visad_java3d_TrackdJNI.h

# rule to build the generated JNI header file from the appropriate class
#
visad_java3d_TrackdJNI.h: TrackdJNI.class
	$(JH) -jni visad.java3d.TrackdJNI

# object file which depends on the generated JNI file
#
TrackdAPI.o: visad_java3d_TrackdJNI.h

# rule to stop Makefile if information is missing
#
required_macros::
	@if [ -z "$(TRACKD_INCDIR)" ]; then \
	  echo ""; \
	  echo "  TRACKD_INCDIR is undefined; JNI portion of build aborting"; \
	  echo ""; \
	  echo "  (Don't worry, this is normal.  It just means the optional"; \
	  echo "   TrackD library won't be supported.)"; \
	  echo ""; \
	  echo "  This 'make' will self-destruct in 1 second"; \
	  echo ""; \
	  exit 1; \
	fi
	@if [ -z "$(TRACKD_LIBDIR)" ]; then \
	  echo ""; \
	  echo "  TRACKD_LIBDIR is undefined; JNI portion of build aborting"; \
	  echo ""; \
	  echo "  (Don't worry, this is normal.  It just means the optional"; \
	  echo "   TrackD library won't be supported.)"; \
	  echo ""; \
	  echo "  This 'make' will self-destruct in 1 second"; \
	  echo ""; \
	  exit 1; \
	fi
	@if [ -z "$(TRACKD_LIBDIR)" ]; then echo "TRACKD_LIBDIR is undefined"; exit 1; fi

# special header/library files needed by this package
#
PKGINCS = -I$(TRACKD_INCDIR)
PKGLIBS = $(TRACKD_LIB)

# set LINK_F77 to 'true' if any Fortran .o files are
# to be included in the shared library
#
LINK_F77 =

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
	SunOS*) arch=solaris_gcc;; \
	Linux*) arch=linux;; \
	AIX*) arch=aix;; \
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

clean:
	rm -f lib$(SLIB).so
	rm -f so_locations
	$(RM) $(OBJS)
	$(RM) $(JNI_HEADERS)
	$(RM) *.class
	$(RM) *~

solaris: required_macros
	@make \
		JAVAARCH=solaris \
		SHCFLAGS="-Xa" \
		SHLD="$(CC)" \
		SHLDFLAGS="-G -z defs" \
		SHFLD="$(FC)" \
		SHFLDFLAGS="-G -Qoption ld -z,defs" \
		SYSLIBS="-lM77 -lF77 -lsunmath -lnsl -lm -lc" \
		JAVADIR="$(JAVADIR)" \
		JC="$(JC)" \
		JFLAGS="$(JFLAGS)" \
		shared_library

solaris_gcc: required_macros
	@make \
		JAVAARCH=solaris \
		CC="gcc" \
		SHCFLAGS="-fPIC -DPIC" \
		SHLD="$(CC)" \
		SHLDFLAGS="-G -z defs" \
		SHFLD="$(FC)" \
		SHFLDFLAGS="-G -Qoption ld -z,defs" \
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

aix: required_macros
	@make \
		JAVAARCH=aix \
		SHLD="$(CC)" \
		SHLDFLAGS="-bE:shrsub.exp -bM:SRE -bnoentry" \
		SHFLD="$(FC)" \
		SHFLDFLAGS="" \
		SYSLIBS="-lm -lc" \
		JAVADIR="$(JAVADIR)" \
		JC="$(JC)" \
		JFLAGS="$(JFLAGS)" \
		shared_library

required_macros::
