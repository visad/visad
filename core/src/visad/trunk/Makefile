# Makefile for VisAD version 2.0
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
	@echo "Type one of the following:"
	@echo "	make compile		to compile .java files"
	@echo "	make debug		to compile .java files for debugging"
	@echo "	make tar		to make visad_src-2.0.tar.Z"
	@echo "	make jar		to make visad_src-2.0.jar"
	@echo "	make save		save files to the backup sub-directory"
	@echo "	make classes		to make jar and tar files containing classes"
	@echo "	make clear		to clear source files"
	@echo "	make clean		to clean class files"
	@echo " make docs               to create javadoc documentation"

.SUFFIXES : .java .class

.java.class:
	$(JC) $(JFLAGS) $<

JC = javac
JFLAGS = -J-mx32m

# to always use IBM's 'jikes' compiler, uncomment the following two lines
#JC = jikes
#JFLAGS = +P

RMIC=rmic

# to always use the Nexus RMI compiler, uncomment the next line
#RMIC=nexusrmic

JAVADIR = /opt/java

RM = rm -f

DOCS_DIR = ../docs
DOC = javadoc
DOCFLAGS = -d $(DOCS_DIR) -J-Xmx64m -package -doctitle 'VisAD Documentation'

FITS = ../nom/tam
UCAR = ../ucar
SSEC = ../edu/wisc/ssec

TAR_FILES = \
	visad/README					\
	visad/DEDICATION				\
	visad/NOTICE					\
	visad/LICENSE					\
	visad/DATE					\
	visad/Makefile					\
	visad/Makefile.WinNT				\
	visad/README.nexusrmi				\
	visad/rmic_script				\
	visad/*.java					\
	visad/examples/*.java				\
	visad/java3d/*.java				\
	visad/java2d/*.java				\
	visad/util/*.java				\
	visad/formula/*.java				\
	visad/ss/*.java					\
	visad/ss/*.gif					\
	visad/ss/README.ss				\
	visad/data/*.java				\
	visad/data/netcdf/*.java			\
	visad/data/netcdf/units/*.java			\
	visad/data/netcdf/units/*.jj			\
	visad/data/netcdf/in/*.java			\
	visad/data/netcdf/out/*.java			\
	visad/data/fits/*.java				\
	nom/tam/fits/*.java				\
	nom/tam/util/*.java				\
	nom/tam/test/*.java				\
	ucar/COPYRIGHT					\
	ucar/VERSION					\
	ucar/multiarray/*.java				\
	ucar/util/*.java				\
	ucar/netcdf/*.java				\
	ucar/netcdf/Makefile				\
	ucar/tests/*.java				\
	ucar/tests/test.nc				\
	ucar/tests/test.out				\
	visad/data/hdfeos/*.java			\
	visad/data/hdfeos/README.hdfeos			\
	visad/data/hdfeos/hdfeosc/*.java		\
	visad/data/hdfeos/hdfeosc/*.c			\
	visad/data/hdfeos/hdfeosc/*.h			\
	visad/data/hdfeos/hdfeosc/Makefile		\
	visad/data/vis5d/*.java				\
	visad/data/vis5d/*.c				\
	visad/data/vis5d/*.h				\
	visad/data/vis5d/Makefile			\
	edu/wisc/ssec/mcidas/*.java			\
	visad/data/mcidas/*.java                        \
	visad/data/mcidas/README.mcidas                 \
	visad/data/gif/*.java				\
	visad/data/gif/sseclogo.gif			\
	visad/data/visad/*.java				\
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
	visad/benjamin/README.benjamin			\
	visad/benjamin/Makefile				\
	visad/benjamin/*.java				\
	visad/benjamin/*.f				\
	visad/benjamin/*.c				\
	visad/benjamin/*.h				\
	visad/benjamin/switch.inp			\
	visad/benjamin/*.table				\
	visad/rabin/*.java				\
	visad/bom/*.java				\
	visad/Gridded1D.txt				\
	visad/Gridded2D.txt				\
	visad/Gridded3D.txt				\
	visad/contour.html				\
	visad/plotdigits.html

tar:
	date > DATE ; \
	cd .. ; \
	tar -cvf visad_src-2.0.tar $(TAR_FILES) ; \
	compress visad_src-2.0.tar ; \
	mv visad_src-2.0.tar.Z visad ; \
	cd visad ; \
	cp visad_src-2.0.tar.Z visadsrc.taz ; \
	ls -l visad_src-2.0.tar.Z visadsrc.taz

jar:
	date > DATE ; \
	cd .. ; \
	jar cvf visad_src-2.0.jar $(TAR_FILES) ; \
	mv visad_src-2.0.jar visad ; \
	cd visad ; \
	cp visad_src-2.0.jar visadsrc.jar ; \
	ls -l visad_src-2.0.jar visadsrc.jar

jar_split:
	date > DATE ; \
	cd .. ; \
	jar cvf visad_src-2.0.jar $(TAR_FILES) ; \
	mv visad_src-2.0.jar visad ; \
	cd visad ; \
	split -b 1400000 visad_src-2.0.jar visad_ ; \
	ls -l visad_a*

unjar_split:
	cd .. ; \
	cat visad_a* > visad_src-2.0.jar ;
	jar xvf visad_src-2.0.jar

CLASS_FILES = \
	DATE						\
	visad/*.class					\
	visad/java3d/*.class				\
	visad/java2d/*.class				\
	visad/util/*.class				\
	visad/formula/*.class				\
	visad/ss/*.class				\
	visad/ss/*.gif					\
	visad/data/*.class				\
	visad/data/netcdf/*.class			\
	visad/data/netcdf/units/*.class			\
	visad/data/netcdf/in/*.class			\
	visad/data/netcdf/out/*.class			\
	ucar/multiarray/*.class				\
	ucar/util/*.class				\
	ucar/netcdf/*.class				\
	ucar/tests/*.class				\
	visad/data/fits/*.class				\
	nom/tam/fits/*.class				\
	nom/tam/util/*.class				\
	nom/tam/test/*.class				\
	visad/data/hdfeos/*.class			\
	visad/data/hdfeos/hdfeosc/*.class		\
	visad/data/vis5d/*.class			\
	edu/wisc/ssec/mcidas/*.class			\
	visad/data/mcidas/*.class                       \
	visad/data/gif/*.class				\
	visad/data/visad/*.class			\
	visad/jmet/*.class				\
	visad/paoloa/*.class				\
	visad/paoloa/spline/*.class			\
	visad/aune/*.class				\
	visad/benjamin/*.class				\
	visad/rabin/*.class				\
	visad/bom/*.class				\
	visad/examples/*.class


classes:
	cd .. ; \
	date > DATE ; \
	jar cvf visad.jar $(CLASS_FILES) ; \
	tar -cvf visad.tar visad.jar

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
	visad/*.java					\
	visad/examples/*.java				\
	visad/java3d/*.java				\
	visad/java2d/*.java				\
	visad/util/*.java				\
	visad/formula/*.java				\
	visad/ss/*.java					\
	visad/ss/*.gif					\
	visad/ss/README.ss				\
	visad/data/*.java				\
	visad/data/netcdf/*.java			\
	visad/data/netcdf/units/*.java			\
	visad/data/netcdf/units/*.jj			\
	visad/data/netcdf/in/*.java			\
	visad/data/netcdf/out/*.java			\
	visad/data/fits/*.java				\
	nom/tam/fits/*.java				\
	nom/tam/util/*.java				\
	nom/tam/test/*.java				\
	ucar/COPYRIGHT					\
	ucar/VERSION					\
	ucar/multiarray/*.java				\
	ucar/util/*.java				\
	ucar/netcdf/*.java				\
	ucar/netcdf/Makefile				\
	ucar/tests/*.java				\
	ucar/tests/test.nc				\
	ucar/tests/test.out				\
	visad/data/hdfeos/*.java			\
	visad/data/hdfeos/hdfeosc/*.java		\
	visad/data/hdfeos/hdfeosc/*.c			\
	visad/data/hdfeos/hdfeosc/*.h			\
	visad/data/hdfeos/hdfeosc/Makefile		\
	edu/wisc/ssec/mcidas/*.java			\
	visad/data/mcidas/*.java                        \
	visad/data/mcidas/README.mcidas                 \
	visad/data/vis5d/*.java				\
	visad/data/vis5d/*.c				\
	visad/data/vis5d/*.h				\
	visad/data/vis5d/Makefile			\
	visad/data/gif/*.java				\
	visad/data/visad/*.java				\
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
	visad/benjamin/README.benjamin                  \
	visad/benjamin/Makefile                         \
	visad/benjamin/*.java                           \
	visad/benjamin/*.f                              \
	visad/benjamin/*.c                              \
	visad/benjamin/*.h                              \
	visad/benjamin/switch.inp                       \
	visad/benjamin/*.table                          \
	visad/rabin/*.java                              \
	visad/bom/*.java                                \
	visad/Gridded1D.txt				\
	visad/Gridded2D.txt				\
	visad/Gridded3D.txt				\
	visad/contour.html				\
	visad/plotdigits.html

save:
	/bin/rm -f backup/visad/*.java backup/visad/examples/*.java backup/visad/java3d/*.java backup/visad/java2d/*.java backup/visad/util/*.java backup/visad/formula/*.java backup/visad/ss/*.java backup/visad/ss/*.gif backup/visad/data/*.java backup/visad/data/netcdf/*.java backup/visad/data/netcdf/units/*.java backup/visad/data/netcdf/in/*.java backup/visad/data/netcdf/out/*.java backup/edu/wisc/ssec/mcidas/*.java backup/visad/data/mcidas/*.java backup/visad/data/fits/*.java backup/nom/tam/fits/*.java backup/nom/tam/util/*.java backup/nom/tam/test/*.java backup/ucar/multiarray/*.java backup/ucar/util/*.java backup/ucar/netcdf/*.java backup/ucar/tests/*.java backup/visad/data/hdfeos/*.java backup/visad/data/hdfeos/hdfeosc/*.java backup/visad/data/vis5d/*.java backup/visad/data/gif/*.java backup/visad/data/visad/*.java backup/visad/paoloa/*.java backup/visad/aune/*.java backup/visad/benjamin/*.java backup/visad/rabin/*.java backup/visad/bom/*.java backup/visad/jmet/*.java backup/visad/paoloa/spline/*.java ; \
	cd .. ; \
	tar -cvf save.tar $(SAVE_FILES) ; \
	cd visad/backup ; \
	tar -xvf ../../save.tar ; \
	rm ../../save.tar ; \
	cd .. ; \
	wc backup/visad/*.java backup/visad/examples/*.java backup/visad/java3d/*.java backup/visad/java2d/*.java backup/visad/util/*.java backup/visad/formula/*.java backup/visad/ss/*.java backup/visad/data/*.java backup/visad/data/netcdf/*.java backup/visad/data/netcdf/units/*.java backup/visad/data/netcdf/in/*.java backup/visad/data/netcdf/out/*.java backup/edu/wisc/ssec/mcidas/*.java backup/visad/data/mcidas/*.java backup/visad/data/fits/*.java backup/nom/tam/fits/*.java backup/nom/tam/util/*.java backup/nom/tam/test/*.java backup/ucar/multiarray/*.java backup/ucar/util/*.java backup/ucar/netcdf/*.java backup/ucar/tests/*.java backup/visad/data/hdfeos/*.java backup/visad/data/hdfeos/hdfeosc/*.java backup/visad/data/vis5d/*.java backup/visad/data/gif/*.java backup/visad/data/visad/*.java backup/visad/paoloa/*.java backup/visad/aune/*.java backup/visad/benjamin/*.java backup/visad/rabin/*.java backup/visad/bom/*.java backup/visad/jmet/*.java backup/visad/paoloa/spline/*.java


clear:
	$(RM) *.java
	$(RM) java3d/*.java java2d/*.java util/*.java formula/*.java ss/*.java ss/*.gif
	$(RM) data/*.java data/netcdf/*.java data/netcdf/units/*.java
	$(RM) data/netcdf/in/*.java data/netcdf/out/*.java
	$(RM) data/fits/*.java
	$(RM) $(FITS)/fits/*.java $(FITS)/util/*.java $(FITS)/test/*.java
	$(RM) $(UCAR)/multiarray/*.java $(UCAR)/netcdf/*.java $(UCAR)/tests/*.java
	$(RM) data/hdfeos/*.java data/hdfeos/hdfeosc/*.java
	$(RM) $(SSEC)/mcidas/*.java data/mcidas/*.java jmet/*.java
	$(RM) data/vis5d/*.java data/gif/*.java data/visad/*.java
	$(RM) paoloa/*.java aune/*.java benjamin/*.java examples/*.java
	$(RM) paoloa/spline/*.java rabin/*.java bom/*.java


debug:
	make compile \
	"JFLAGS = -J-mx32m -g"

VISAD_PACKAGES = \
		visad \
		visad.java3d \
		visad.java2d \
		visad.util \
		ucar.multiarray \
		ucar.util \
		ucar.netcdf \
		ucar.tests \
		visad.data \
		visad.data.netcdf.units \
		visad.data.netcdf.in \
		visad.data.netcdf.out \
		visad.data.netcdf \
		nom.tam.fits \
		nom.tam.util \
		nom.tam.test \
		visad.data.fits \
		visad.data.vis5d \
		edu.wisc.ssec.mcidas \
		visad.data.mcidas \
		visad.data.gif \
		visad.data.visad \
		visad.data.hdfeos.hdfeosc \
		visad.data.hdfeos \
		visad.formula \
		visad.ss \
		visad.jmet \
		visad.paoloa \
		visad.paoloa.spline \
		visad.aune \
		visad.benjamin \
		visad.rabin \
		visad.bom

clean:
	$(RM) *.class
	for i in $(VISAD_PACKAGES); do					\
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
		(cd $$dir && $(RM) *.class);				\
	      fi;							\
	    fi;								\
	  fi;								\
	done
	-(cd examples && $(RM) *.class)

recompile:
	@root=`cd .. && pwd`;						\
	for i in $(VISAD_PACKAGES) visad.examples; do			\
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
		(cd $$dir && make JAVADIR=${JAVADIR} JC=${JC}		\
				JFLAGS="${JFLAGS}") || true;		\
	      else							\
		(cd $$dir &&						\
		 for j in *.java; do					\
		   b=`basename $$j .java`;				\
		   c=$$b.class;						\
		   compile=false;					\
		   if [ ! -f $$c ]; then				\
		     compile=true;					\
		   else							\
		     set `ls -td $$j $$c` X;				\
		     if [ $$c != $$1 ]; then				\
		       compile=true;					\
		     fi;						\
		   fi;							\
		   if $$compile; then					\
		     (echo "$(JC) $(JFLAGS) $$j" &&			\
		      $(JC) $(JFLAGS) $$j || true);			\
		   fi;							\
		   case "$$b" in					\
		   *Remote*Impl)					\
		     set `ls -t $$b.class $${b}_Stub.class 2>/dev/null || true` X; \
		     if [ $${b}_Stub.class != $$1 ]; then		\
		       compile=true;					\
		     fi;						\
		     if $$compile; then					\
		       (echo "${RMIC} -d $$root $$i.$$b" &&		\
			${RMIC} -d $$root $$i.$$b || true);		\
		     fi;						\
		     ;;							\
		   esac;						\
		 done);							\
	      fi;							\
	    fi;								\
	  fi;								\
	done

compile: clean recompile

docs:
	@if [ ! -d $(DOCS_DIR) ]; then mkdir $(DOCS_DIR); fi   
	$(DOC) $(DOCFLAGS) $(VISAD_PACKAGES)

nexusrmi_compile:
	make RMIC=nexusrmic compile

tonexus:
	java2nexus `find . -name "*.java"`
	java2nexus `find ../ucar -name "*.java"`
	java2nexus `find ../nom -name "*.java"`
	java2nexus `find ../edu -name "*.java"`

tojava:
	nexus2java `find . -name "*.java"`
	nexus2java `find ../ucar -name "*.java"`
	nexus2java `find ../nom -name "*.java"`
	nexus2java `find ../edu -name "*.java"`

jikes_compile:
	make JC=jikes JFLAGS= compile

java_version:
	java -fullversion
