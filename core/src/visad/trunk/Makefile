# makefile for VisAD version 2.0

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
	@echo "	make tar		to make visad_src-2.0.tar.Z"
	@echo "	make jar		to make visad_src-2.0.jar"
	@echo "	make save		save files to the backup sub-directory"
	@echo "	make classes		to make jar and tar files containing classes"
	@echo "	make clear		to clear source files"
	@echo "	make clean		to clean class files"

.SUFFIXES : .java .class

.java.class:
	javac $<

JC = javac
JFLAGS = -J-mx32m
RM = rm -f
FITS = ../nom/tam

TAR_FILES = \
	visad/README					\
	visad/DEDICATION				\
	visad/NOTICE					\
	visad/LICENSE					\
	visad/DATE					\
	visad/Makefile					\
	visad/Makefile.WinNT				\
	visad/rmic_script				\
	visad/*.java					\
	visad/examples/*.java				\
	visad/java3d/*.java				\
	visad/java2d/*.java				\
	visad/util/*.java				\
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
	visad/data/hdfeos/*.java			\
	visad/data/hdfeos/hdfeosc/*.java		\
	visad/data/hdfeos/hdfeosc/*.c			\
	visad/data/hdfeos/hdfeosc/*.h			\
	visad/data/hdfeos/hdfeosc/Makefile		\
	visad/data/vis5d/*.java				\
	visad/data/vis5d/*.c				\
	visad/data/vis5d/*.h				\
	visad/data/vis5d/Makefile			\
	visad/data/gif/*.java				\
	visad/data/gif/sseclogo.gif			\
	visad/paoloa/README.paoloa			\
	visad/paoloa/Makefile				\
	visad/paoloa/*.java				\
	visad/paoloa/*.f				\
	visad/paoloa/*.c				\
	visad/paoloa/*.h				\
	visad/aune/README.aune				\
	visad/aune/Makefile				\
	visad/aune/shsize.fcm				\
	visad/aune/*.java				\
	visad/aune/*.f					\
	visad/aune/*.c					\
	visad/aune/*.h					\
	ucar.jar					\
	visad/Gridded1D.txt				\
	visad/Gridded2D.txt				\
	visad/Gridded3D.txt				\
	visad/contour.html				\
	visad/delaunay.html				\
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


CLASS_FILES = \
	DATE						\
	visad/*.class					\
	visad/java3d/*.class				\
	visad/java2d/*.class				\
	visad/util/*.class				\
	visad/ss/*.class				\
	visad/ss/*.gif					\
	visad/data/*.class				\
	visad/data/netcdf/*.class			\
	visad/data/netcdf/units/*.class			\
	visad/data/netcdf/in/*.class			\
	visad/data/netcdf/out/*.class			\
	ucar/multiarray/*.class				\
	ucar/netcdf/*.class				\
	visad/data/fits/*.class				\
	nom/tam/fits/*.class				\
	nom/tam/util/*.class				\
	nom/tam/test/*.class				\
	visad/data/hdfeos/*.class			\
	visad/data/hdfeos/hdfeosc/*.class		\
	visad/data/vis5d/*.class			\
	visad/data/gif/*.class


classes:
	cd .. ; \
	date > DATE ; \
	jar xvf ucar.jar ; \
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
	visad/rmic_script				\
	visad/*.java					\
	visad/examples/*.java				\
	visad/java3d/*.java				\
	visad/java2d/*.java				\
	visad/util/*.java				\
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
	visad/data/hdfeos/*.java			\
	visad/data/hdfeos/hdfeosc/*.java		\
	visad/data/hdfeos/hdfeosc/*.c			\
	visad/data/hdfeos/hdfeosc/*.h			\
	visad/data/hdfeos/hdfeosc/Makefile		\
	visad/data/vis5d/*.java				\
	visad/data/vis5d/*.c				\
	visad/data/vis5d/*.h				\
	visad/data/vis5d/Makefile			\
	visad/data/gif/*.java				\
	visad/paoloa/README.paoloa			\
	visad/paoloa/Makefile				\
	visad/paoloa/*.java				\
	visad/paoloa/*.f				\
	visad/paoloa/*.c				\
	visad/paoloa/*.h				\
	visad/aune/README.aune				\
	visad/aune/Makefile				\
	visad/aune/shsize.fcm				\
	visad/aune/*.java				\
	visad/aune/*.f					\
	visad/aune/*.c					\
	visad/aune/*.h					\
	ucar.jar					\
	visad/Gridded1D.txt				\
	visad/Gridded2D.txt				\
	visad/Gridded3D.txt				\
	visad/contour.html				\
	visad/delaunay.html				\
	visad/plotdigits.html

save:
	/bin/rm -f backup/visad/*.java backup/visad/examples/*.java backup/visad/java3d/*.java backup/visad/java2d/*.java backup/visad/util/*.java backup/visad/ss/*.java backup/visad/ss/*.gif backup/visad/data/*.java backup/visad/data/netcdf/*.java backup/visad/data/netcdf/units/*.java backup/visad/data/netcdf/in/*.java backup/visad/data/netcdf/out/*.java backup/visad/data/fits/*.java backup/visad/nom/tam/fits/*.java backup/visad/nom/tam/util/*.java backup/visad/nom/tam/test/*.java backup/visad/data/hdfeos/*.java backup/visad/data/hdfeos/hdfeosc/*.java backup/visad/data/vis5d/*.java backup/visad/data/gif/*.java backup/visad/paoloa/*.java backup/visad/aune/*.java ; \
	cd .. ; \
	tar -cvf save.tar $(SAVE_FILES) ; \
	cd visad/backup ; \
	tar -xvf ../../save.tar ; \
	rm ../../save.tar ; \
	cd .. ; \
	wc backup/visad/*.java backup/visad/examples/*.java backup/visad/java3d/*.java backup/visad/java2d/*.java backup/visad/util/*.java backup/visad/ss/*.java backup/visad/data/*.java backup/visad/data/netcdf/*.java backup/visad/data/netcdf/units/*.java backup/visad/data/netcdf/in/*.java backup/visad/data/netcdf/out/*.java backup/visad/data/fits/*.java backup/nom/tam/fits/*.java backup/nom/tam/util/*.java backup/nom/tam/test/*.java backup/visad/data/hdfeos/*.java backup/visad/data/hdfeos/hdfeosc/*.java backup/visad/data/vis5d/*.java backup/visad/data/gif/*.java backup/visad/paoloa/*.java backup/visad/aune/*.java


clear:
	$(RM) *.java
	$(RM) java3d/*.java java2d/*.java util/*.java ss/*.java ss/*.gif
	$(RM) data/*.java data/netcdf/*.java data/netcdf/units/*.java
	$(RM) data/netcdf/in/*.java data/netcdf/out/*.java
	$(RM) data/fits/*.java
	$(RM) $(FITS)/fits/*.java $(FITS)/util/*.java $(FITS)/test/*.java
	$(RM) data/hdfeos/*.java data/hdfeos/hdfeosc/*.java
	$(RM) data/vis5d/*.java data/gif/*.java
	$(RM) paoloa/*.java aune/*.java examples/*.java


RMIC = \
	visad.RemoteCellImpl \
	visad.RemoteActionImpl \
	visad.RemoteDisplayImpl \
	visad.RemoteThingReferenceImpl \
	visad.RemoteDataReferenceImpl \
	visad.RemoteThingImpl \
	visad.RemoteDataImpl \
	visad.RemoteFunctionImpl \
	visad.RemoteFieldImpl \
	visad.RemoteServerImpl

compile: clean
	$(JC) $(JFLAGS) *.java
	rmic $(RMIC)
	$(JC) $(JFLAGS) java3d/*.java
	$(JC) $(JFLAGS) java2d/*.java
	$(JC) $(JFLAGS) util/*.java
	-(cd .. && jar xvf ucar.jar)
	$(JC) $(JFLAGS) data/*.java
	$(JC) $(JFLAGS) data/netcdf/units/*.java
	$(JC) $(JFLAGS) data/netcdf/in/*.java
	$(JC) $(JFLAGS) data/netcdf/out/*.java
	$(JC) $(JFLAGS) data/netcdf/*.java
	-$(JC) $(JFLAGS) $(FITS)/util/*.java
	-$(JC) $(JFLAGS) $(FITS)/fits/*.java
	-$(JC) $(JFLAGS) $(FITS)/test/*.java
	$(JC) $(JFLAGS) data/fits/*.java
	$(JC) $(JFLAGS) data/vis5d/*.java
	$(JC) $(JFLAGS) data/gif/*.java
	$(JC) $(JFLAGS) data/hdfeos/hdfeosc/*.java
	$(JC) $(JFLAGS) data/hdfeos/*.java
	$(JC) $(JFLAGS) ss/*.java
	-$(JC) $(JFLAGS) paoloa/*.java
	-$(JC) $(JFLAGS) aune/*.java
	-(cd examples && $(JC) $(JFLAGS) *.java)

clean:
	$(RM) *.class
	$(RM) java3d/*.class java2d/*.class util/*.class ss/*.class
	$(RM) data/*.class data/netcdf/*.class data/netcdf/units/*.class
	$(RM) data/netcdf/in/*.class data/netcdf/out/*.class
	$(RM) data/fits/*.class
	$(RM) $(FITS)/fits/*.class $(FITS)/util/*.class $(FITS)/test/*.class
	$(RM) data/hdfeos/*.class data/hdfeos/hdfeosc/*.class
	$(RM) data/vis5d/*.class data/gif/*.class
	$(RM) paoloa/*.class aune/*.class examples/*.class




JAVAONE_FILES = \
	DATE						\
	dot_cshrc					\
	visad_src-2.0.jar				\
	data/vis5d/QLQ.v5d				\
	examples/small.jpg				\
	examples/bill.gif				\
	examples/images.nc				\
	examples/pmsl.nc				\
	examples/ngc1316o.fits				\
	paoloa/data_obs_1.dat				\
	paoloa/data_sou_1.dat				\
	paoloa/goesrtcf					\
	aune/*.so					\
	paoloa/*.so					\
	data/vis5d/*.so					\
	data/hdfeos/hdfeosc/*.so			\
	visad.jar					\
	paoloa/*.class					\
	aune/*.class					\
	examples/*.class				\
	www/*



javaone:
	date > DATE ; \
	cp ../visad.jar . ; \
	cp ../../.cshrc dot_cshrc ; \
	jar cvf ssecj1.jar $(JAVAONE_FILES)
