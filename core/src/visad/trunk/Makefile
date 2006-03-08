## Makefile for VisAD version 2.0
# For use with Unix-based systems
# To compile VisAD in a Windows-based system, use the Makefile.WinNT file.

# VisAD system for interactive analysis and visualization of numerical
# data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
# Rink and Dave Glowacki.
#  
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 1, or (at your option)
# any later version.
#  
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License in file NOTICE for more details.
#  
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

default:
	@clear
	@echo ""
	@echo "Type one of the following:"
	@echo "	make compile		to compile .java files"
	@echo "	make debug		to compile .java files for debugging"
	@echo "	make tar		to make visad_src-2.0.tar.Z"
	@echo "	make jar		to make visad_src-2.0.jar"
	@echo "	make examplesjar	to make visad_examples.jar"
	@echo "	make save		save files to the backup sub-directory"
	@echo "	make classes		to make jar and tar files containing classes"
	@echo "	make clear		to clear source files"
	@echo "	make clean		to clean class files"
	@echo "	make docs		to create javadoc documentation"
	@echo "	make tardocs		to create compressed javadoc tar file"
	@echo "	make jardocs		to create javadoc jar file"
	@echo ""
	@echo "You may want to uncomment one or more lines in the"
	@echo "'Customizations' section of the top-level Makefile"
	@echo "if you're running on a non-Solaris platform or want"
	@echo "to use something other than the standard JDK tools"
	@echo ""

############################################################################
# Standard definitions -- don't change these
############################################################################

# JC is the Java compiler
# JFLAGS specifies special flags for the Java compiler
# JH is the Java header file generator (javah)
#
JC = javac
JFLAGS = -J-mx64m -target 1.2
JH = javah

# RMIC is the Remote Method Invokation compiler
#
RMIC=rmic

# JAVADIR should be the top-level JDK1.2 directory
# JAVAARCH should be the local machine architecture
# JAVAINCS should list all the necessary include files under JAVADIR
#
JAVADIR = /opt/java
JAVAARCH = solaris
JAVAINCS = -I${JAVADIR}/include -I${JAVADIR}/include/${JAVAARCH}

# JAR is the Java archive executable
# JARVERBOSE is normally 'v'
#
JAR = jar
JARVERBOSE = v

# these hold the flags needed to tell the C and Fortran compilers
# to build shared object files
#
SHCFLAGS =
SHFFLAGS =

# this is the list of Fortran libraries commonly referenced
# by objects in shared library
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

# program used to clean up the results of previous builds
#
RM = rm -f

# DOC is the program used to generate the documentation
# DOC_DIR is the top-level directory where generated documentation is stored
# DOC_MEM is the amount of heap used to build the documentation
# DOC_LINKS points to any external class documentation sites
# DOC_EXTRAFLAGS holds any non-standard flags and should be empty by default
# DOCFLAGS are any flags needed to run the documentation generator
#
DOC = javadoc
DOC_DIR = ../docs
DOC_MEM = 512m
DOC_LINKS = -link http://java.sun.com/j2se/1.4/docs/api
DOCFLAGS = $(DOC_LINKS) -d $(DOC_DIR) -J-Xmx$(DOC_MEM) -package \
	-doctitle 'VisAD Documentation' -windowtitle 'VisAD' $(DOC_EXTRAFLAGS) 

############################################################################
# Customizations
############################################################################

# set this if your JDK/JRE is installed somewhere other than /opt/java
#JAVADIR = /usr/java

# to always use IBM's 'jikes' compiler, uncomment the following two lines
#JC = jikes
#JFLAGS = +P -deprecation

# to always use the Nexus RMI compiler, uncomment the next line
#RMIC=nexusrmic

# to build for Linux, uncomment the next few lines (UNTESTED!)
#JAVAARCH = linux
#SHCFLAGS = -fPIC -DPIC
#SHLDFLAGS = -shared
#SHFLDFLAGS = -shared

# to build for Irix, uncomment the next few lines (UNTESTED!)
#JAVAARCH = irix
#SHLDFLAGS = -shared
#SHFLDFLAGS = -shared

# to build for AIX, uncomment the next few lines (UNTESTED!)
#JAVAARCH = aix
#SHLDFLAGS = -bE:shrsub.exp -bM:SRE -bnoentry
#SHFLDFLAGS = 

############################################################################
# You shouldn't need to change anything beyond this point
############################################################################

.SUFFIXES : .java .class

.java.class:
	$(JC) $(JFLAGS) $<

SAVE_FILES = \
	visad/README					\
	visad/DEDICATION				\
	visad/NOTICE					\
	visad/LICENSE					\
	visad/DATE					\
	visad/Makefile					\
	visad/Makefile.WinNT				\
	visad/README.nexusrmi				\
	visad/rmic_script				\
	visad/VisAD.Manifest				\
	visad/*.java					\
	visad/examples/*.java				\
	visad/examples/display_test.c			\
	visad/java3d/*.java				\
	visad/java2d/*.java				\
	visad/python/*.java				\
	visad/python/*.py				\
	visad/python/README.python			\
	visad/browser/*.java				\
	visad/browser/*.html				\
	visad/browser/README.browser			\
	visad/util/*.java				\
	visad/util/*.jhf				\
	visad/matrix/*.java				\
	visad/math/*.java				\
	visad/formula/*.java				\
	visad/ss/*.java					\
	visad/ss/*.gif					\
	visad/ss/README.ss				\
	visad/cluster/*.java				\
	visad/collab/*.java				\
	visad/data/*.java				\
	visad/data/units/*.java				\
	visad/data/units/*.jj				\
	visad/data/in/*.java				\
	visad/data/in/package.html			\
	visad/data/dods/*.java				\
	visad/data/dods/package.html			\
	visad/data/netcdf/*.java			\
	visad/data/netcdf/units/*.java			\
	visad/data/netcdf/in/*.java			\
	visad/data/netcdf/out/*.java			\
	visad/data/fits/*.java				\
	loci/formats/*.java				\
	nom/tam/fits/*.java				\
	nom/tam/util/*.java				\
	nom/tam/test/*.java				\
	HTTPClient/0*					\
	HTTPClient/*.html				\
	HTTPClient/*.java				\
	ucar/COPYRIGHT					\
	ucar/VERSION					\
	ucar/multiarray/*.java				\
	ucar/util/*.java				\
	ucar/netcdf/*.java				\
	ucar/tests/*.java				\
	ucar/tests/test.nc				\
	ucar/tests/test.out				\
	dods/dap/*.java					\
	dods/dap/parser/*.java				\
	dods/dap/Server/*.java				\
	dods/util/*.java				\
	gnu/regexp/*.java				\
	visad/data/hdfeos/*.java			\
	visad/data/hdfeos/hdfeosc/*.java		\
	visad/data/hdfeos/hdfeosc/*.c			\
	visad/data/hdfeos/hdfeosc/*.h			\
	visad/data/hdfeos/hdfeosc/Makefile		\
	edu/wisc/ssec/mcidas/*.java			\
	edu/wisc/ssec/mcidas/adde/*.java		\
	visad/data/mcidas/*.java			\
	visad/data/mcidas/README.mcidas			\
	visad/data/avi/*.java				\
	visad/data/bio/*.java				\
	visad/data/biorad/*.java			\
	visad/data/ij/*.java				\
	visad/data/jai/*.java				\
	visad/data/qt/*.java				\
	visad/data/vis5d/*.java				\
	visad/data/gif/*.java				\
	visad/data/gis/*.java				\
	visad/data/text/*.java				\
	visad/data/text/example1.txt			\
	visad/data/text/example2.csv			\
	visad/data/text/example3.txt			\
	visad/data/text/README.text			\
	visad/data/tiff/*.java				\
	visad/data/visad/*.java				\
	visad/data/visad/object/*.java			\
	visad/data/hdf5/*.java				\
	visad/data/hdf5/hdf5objects/*.java		\
	ncsa/hdf/hdf5lib/*.java				\
	ncsa/hdf/hdf5lib/exceptions/*.java		\
	visad/jmet/*.java				\
	visad/paoloa/README.paoloa			\
	visad/paoloa/Makefile				\
	visad/paoloa/*.java				\
	visad/paoloa/*.f				\
	visad/paoloa/*.c				\
	visad/paoloa/*.h				\
	visad/paoloa/spline/Makefile			\
	visad/paoloa/spline/*.java			\
	visad/paoloa/spline/*.f				\
	visad/paoloa/spline/*.c				\
	visad/paoloa/spline/*.h				\
	visad/aune/README.aune				\
	visad/aune/Makefile				\
	visad/aune/shsize.fcm				\
	visad/aune/*.java				\
	visad/aune/*.f					\
	visad/aune/*.c					\
	visad/aune/*.h					\
	visad/benjamin/Makefile				\
	visad/benjamin/*.java				\
	visad/benjamin/*.f				\
	visad/benjamin/*.c				\
	visad/benjamin/*.h				\
	visad/benjamin/switch.inp			\
	visad/benjamin/*.table				\
	visad/rabin/*.java				\
	visad/bom/*.java				\
	visad/aeri/*.java				\
	visad/data/amanda/*.java			\
	visad/georef/*.java				\
	visad/meteorology/*.java			\
	visad/install/*.java				\
	visad/install/install_visad			\
	visad/install/README				\
	visad/install/README.html			\
	visad/Gridded1D.txt				\
	visad/Gridded2D.txt				\
	visad/Gridded3D.txt

TAR_FILES = \
	$(SAVE_FILES)					\
	visad/data/gif/sseclogo.gif			\
	visad/data/hdf5/COPYING				\
	visad/data/hdfeos/README.hdfeos			\
	visad/java3d/*.c				\
	visad/java3d/Makefile

DOC_PACKAGES = \
		visad \
		visad.cluster \
		visad.collab \
		visad.java3d \
		visad.java2d \
		visad.python \
		visad.browser \
		visad.util \
		HTTPClient \
		ucar.multiarray \
		ucar.util \
		ucar.netcdf \
		ucar.tests \
		dods.dap \
		dods.dap.parser \
		dods.dap.Server \
		dods.util \
		gnu.regexp \
		visad.data \
		visad.data.in \
		visad.data.dods \
		visad.data.units \
		visad.data.netcdf.units \
		visad.data.netcdf.in \
		visad.data.netcdf.out \
		visad.data.netcdf \
		loci.formats \
		nom.tam.fits \
		nom.tam.util \
		nom.tam.test \
		visad.data.fits \
		visad.data.vis5d \
		edu.wisc.ssec.mcidas \
		edu.wisc.ssec.mcidas.adde \
		visad.data.mcidas \
		visad.data.avi \
		visad.data.bio \
		visad.data.biorad \
		visad.data.ij \
		visad.data.jai \
		visad.data.qt \
		visad.data.gif \
		visad.data.gis \
		visad.data.text \
		visad.data.tiff \
		visad.data.visad \
		visad.data.visad.object \
		visad.data.hdfeos.hdfeosc \
		visad.data.hdfeos \
		ncsa.hdf.hdf5lib.exceptions \
		ncsa.hdf.hdf5lib \
		visad.data.hdf5.hdf5objects \
		visad.data.hdf5 \
		visad.matrix \
		visad.math \
		visad.formula \
		visad.ss \
		visad.jmet \
		visad.paoloa \
		visad.paoloa.spline \
		visad.aune \
		visad.benjamin \
		visad.rabin \
		visad.bom \
		visad.aeri \
		visad.data.amanda \
		visad.georef \
		visad.meteorology \
		visad.install

ALL_PACKAGES = \
		$(DOC_PACKAGES) \
		visad.examples

CLASS_FILES=	\
	DATE \
	`echo $(DOC_PACKAGES)' ' | sed -e 's/\./\//g' -e 's/ /\/\*\.class /g'` \
	visad/ss/*.gif \
	visad/util/*.jhf

JAVA_FILES=	\
	`echo $(ALL_PACKAGES)' ' | sed -e 's/\./\//g' -e 's/ /\/\*\.java /g'` \
	visad/ss/*.gif \
	visad/util/*.jhf

tar:
	date > DATE ; \
	cd .. ; \
	tar -cvf visad_src-2.0.tar $(TAR_FILES) ; \
	compress visad_src-2.0.tar ; \
	mv visad_src-2.0.tar.Z visad ; \
	cd visad ; \
	rm -f visadsrc.taz; \
	ln visad_src-2.0.tar.Z visadsrc.taz ; \
	ls -l visad_src-2.0.tar.Z visadsrc.taz

jar:
	@date > DATE ; \
	cd .. ; \
	${JAR} ${JARVERBOSE}cf visad_src-2.0.jar $(TAR_FILES) ; \
	mv visad_src-2.0.jar visad ; \
	cd visad ; \
	rm -f visadsrc.jar ; \
	ln visad_src-2.0.jar visadsrc.jar ; \
	ls -l visad_src-2.0.jar visadsrc.jar

jar_split:
	@date > DATE ; \
	cd .. ; \
	${JAR} ${JARVERBOSE}cf visad_src-2.0.jar $(TAR_FILES) ; \
	mv visad_src-2.0.jar visad ; \
	cd visad ; \
	split -b 1400000 visad_src-2.0.jar visad_ ; \
	ls -l visad_a*

unjar_split:
	@cd .. ; \
	cat visad_a* > visad_src-2.0.jar ;
	${JAR} ${JARVERBOSE}xf visad_src-2.0.jar


classes:
	@cd .. ; \
	date > DATE ; \
	${JAR} ${JARVERBOSE}cfm visad.jar visad/VisAD.Manifest $(CLASS_FILES) ; \
	tar -cvf visad.tar visad.jar


EXAMPLES_TAR_FILES = \
	visad/examples/*.java				\
	visad/examples/display_test.c			\
	visad/examples/*.class

examplesjar:
	@cd .. ; \
	${JAR} ${JARVERBOSE}cf visad_examples.jar $(EXAMPLES_TAR_FILES) ; \
	mv visad_examples.jar visad ; \
	cd visad ; \
	ls -l visad_examples.jar


save:
	/bin/rm -rf backup
	mkdir backup
	cd .. && tar -cvf save.tar $(SAVE_FILES)
	cd backup && tar -xvf ../../save.tar
	rm -f ../save.tar
	wc `find backup -name '*.java' -print`


clear:
	cd .. && $(RM) $(JAVA_FILES)


debug:
	make compile \
	"JFLAGS = ${JFLAGS} -g"


clean:
	$(RM) *.class *~
	for i in $(ALL_PACKAGES); do					\
	  case $$i in							\
	  visad)							\
	    dir=							\
	    ;;								\
	  visad.*)							\
	    dir=`echo $$i | sed -e 's;\.;/;g' -e 's;^visad/;;'`;	\
	    ;;								\
	  *)								\
	    dir=`echo $$i | sed -e 's;\.;/;g' -e 's;^;\.\./;'`;		\
	    ;;								\
	  esac;								\
	  if [ ! -z "$$dir" ]; then					\
	    if [ ! -d $$dir ]; then					\
	      echo "$$dir does not exist";				\
	    else							\
	      echo "*** Cleaning in $$dir";				\
	      if [ -f $$dir/Makefile ]; then				\
		(cd $$dir && make -k clean);				\
	      else							\
		(cd $$dir && $(RM) *.class *.o *.so *~);		\
	      fi;							\
	    fi;								\
	  fi;								\
	done

recompile:
	@root=`cd .. && pwd`;						\
	for i in $(ALL_PACKAGES); do					\
	  case $$i in							\
	  visad)							\
	    dir=.							\
	    ;;								\
	  visad.*)							\
	    dir=`echo $$i | sed -e 's;\.;/;g' -e 's;^visad/;;'`;	\
	    ;;								\
	  *)								\
	    dir=`echo $$i | sed -e 's;\.;/;g' -e 's;^;\.\./;'`;		\
	    ;;								\
	  esac;								\
	  if [ ! -z "$$dir" ]; then					\
	    if [ ! -d $$dir ]; then					\
	      echo "$$dir does not exist";				\
	    else							\
	      echo "*** Building in $$dir";				\
	      if [ -f $$dir/Makefile -a "$$dir" != "." ]; then		\
		(cd $$dir && make					\
				JAVADIR="${JAVADIR}"			\
				JAVAARCH="${JAVAARCH}"			\
				JC="${JC}"				\
				JFLAGS="${JFLAGS}"			\
				JH="${JH}"				\
				SHCFLAGS="${SHCFLAGS}"			\
				SHFFLAGS="${SHFFLAGS}"			\
				SHLD="${SHLD}"				\
				SHLDFLAGS="${SHLDFLAGS}"		\
				SHFLD="${SHFLD}"			\
				SHFLDFLAGS="${SHFLDFLAGS}"		\
				SHFLIBS="${SHFLIBS}"			\
				RM="${RM}"				\
							) || true;	\
	      else							\
		(cd $$dir;						\
		 if [ ! -z "$(COMPILE_ALL)" ]; then			\
		   if ls *.java >/dev/null 2>&1; then			\
		     echo $(JC) $(JFLAGS) '*.java';			\
		     $(JC) $(JFLAGS) *.java;				\
		   fi;							\
		 fi;							\
		 for j in *.java; do					\
		   if [ -f $$j ]; then					\
		     b=`basename $$j .java`;				\
		     if [ -z "$(COMPILE_ALL)" ]; then			\
		       c=$$b.class;					\
		       compile=false;					\
		       if [ ! -f $$c ]; then				\
			 compile=true;					\
		       else						\
			 set `ls -td $$j $$c` X;			\
			 if [ $$c != $$1 ]; then			\
			   compile=true;				\
			 fi;						\
		       fi;						\
		       if $$compile; then				\
			 (echo "$(JC) $(JFLAGS) $$j" &&			\
			  $(JC) $(JFLAGS) $$j || true);			\
		       fi;						\
		     fi;						\
		     rmicomp=;						\
		     case "$$b" in					\
		     *Remote*Impl) rmicomp=1;;				\
		     NetcdfServer) rmicomp=1;;				\
		     esac;						\
		     if [ ! -z "$$rmicomp" ]; then			\
		       set `ls -t $$b.class $${b}_Stub.class 2>/dev/null || true` X; \
		       if [ $${b}_Stub.class != $$1 ]; then		\
			 (echo "${RMIC} -d $$root $$i.$$b" &&		\
			  ${RMIC} -d $$root $$i.$$b || true);		\
		       fi;						\
		     fi;						\
		   fi;							\
	         done);							\
	      fi;							\
	    fi;								\
	  fi;								\
	done

compile: clean
	@make								\
			JAVADIR="${JAVADIR}"				\
			JAVAARCH="${JAVAARCH}"				\
			JC="${JC}"					\
			JFLAGS="${JFLAGS}"				\
			JH="${JH}"					\
			SHCFLAGS="${SHCFLAGS}"				\
			SHFFLAGS="${SHFFLAGS}"				\
			SHLD="${SHLD}"					\
			SHLDFLAGS="${SHLDFLAGS}"			\
			SHFLD="${SHFLD}"				\
			SHFLDFLAGS="${SHFLDFLAGS}"			\
			SHFLIBS="${SHFLIBS}"				\
			RM="${RM}"					\
			COMPILE_ALL=true				\
		recompile

docs:
	@if [ ! -d $(DOC_DIR) ]; then mkdir $(DOC_DIR); fi   
	$(DOC) $(DOCFLAGS) $(DOC_PACKAGES)

tardocs: docs
	(cd $(DOC_DIR)/.. &&						\
	 DNAME=`basename $(DOC_DIR)` &&					\
	 date > $$DNAME/DATE &&						\
	 tar -cvf - $$DNAME) > visad_doc-2.0.tar;			\
	compress visad_doc-2.0.tar;					\
	rm -f visaddoc.taz;						\
	ln visad_doc-2.0.tar.Z visaddoc.taz;				\
	ls -l visad_doc-2.0.tar.Z visaddoc.taz

jardocs: docs
	@(cd $(DOC_DIR)/.. &&						\
	 DNAME=`basename $(DOC_DIR)` &&					\
	 date > $$DNAME/DATE &&						\
	 ${JAR} ${JARVERBOSE}c $$DNAME) > visad_doc-2.0.jar;		\
	rm -f visaddoc.jar;						\
	ln visad_doc-2.0.jar visaddoc.jar;				\
	ls -l visad_doc-2.0.jar visaddoc.jar

nexusrmi_compile:
	make RMIC=nexusrmic compile

tonexus:
	java2nexus `find . -name "*.java"`
	java2nexus `find ../ucar -name "*.java"`
	java2nexus `find ../dods -name "*.java"`
	java2nexus `find ../gnu -name "*.java"`
	java2nexus `find ../loci -name "*.java"`
	java2nexus `find ../nom -name "*.java"`
	java2nexus `find ../edu -name "*.java"`

tojava:
	nexus2java `find . -name "*.java"`
	nexus2java `find ../ucar -name "*.java"`
	nexus2java `find ../dods -name "*.java"`
	nexus2java `find ../gnu -name "*.java"`
	nexus2java `find ../loci -name "*.java"`
	nexus2java `find ../nom -name "*.java"`
	nexus2java `find ../edu -name "*.java"`

jikes_compile:
	make JC=jikes JFLAGS= compile

java_version:
	java -fullversion
