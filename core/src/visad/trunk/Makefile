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


TAR_FILES = \
	visad/README					\
	visad/DEDICATION				\
	visad/NOTICE					\
	visad/LICENSE					\
	visad/DATE					\
	visad/Makefile					\
	visad/rmic_script				\
	visad/*.java					\
	visad/examples/*.java				\
	visad/java3d/*.java				\
	visad/util/*.java				\
	visad/data/*.java				\
	visad/data/netcdf/*.java			\
	visad/data/netcdf/units/*.java			\
	visad/data/netcdf/units/*.jj			\
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
	visad/util/*.class				\
	visad/data/*.class				\
	visad/data/netcdf/*.class			\
	visad/data/netcdf/units/*.class			\
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

CORE_CLASS_FILES = \
	visad/DATE					\
	visad/*.class

JAVA3D_CLASS_FILES = \
	visad/java3d/DATE				\
	visad/java3d/*.class

UTIL_CLASS_FILES = \
	visad/util/DATE					\
	visad/util/*.class

NETCDF_CLASS_FILES = \
	visad/data/netcdf/DATE				\
	visad/data/*.class				\
	visad/data/netcdf/*.class			\
	visad/data/netcdf/units/*.class			\
	ucar/multiarray/*.class				\
	ucar/netcdf/*.class

FITS_CLASS_FILES = \
	visad/data/fits/DATE				\
	visad/data/*.class				\
	visad/data/fits/*.class				\
	nom/tam/fits/*.class				\
	nom/tam/util/*.class				\
	nom/tam/test/*.class

HDFEOS_CLASS_FILES = \
	visad/data/hdfeos/DATE				\
	visad/data/*.class				\
	visad/data/hdfeos/*.class			\
	visad/data/hdfeos/hdfeosc/*.class

VIS5D_CLASS_FILES = \
	visad/data/vis5d/DATE				\
	visad/data/*.class				\
	visad/data/vis5d/*.class			\
 
GIF_CLASS_FILES = \
	visad/data/gif/DATE				\
	visad/data/*.class				\
	visad/data/gif/*.class


classes:
	cd .. ; \
	date > DATE ; \
	jar xvf ucar.jar ; \
	jar cvf visad.jar $(CLASS_FILES) ; \
	tar -cvf visad.tar visad.jar ; \
	date > visad/DATE ; \
	jar cvf visad_core.jar $(CORE_CLASS_FILES) ; \
	tar -cvf visad_core.tar visad_core.jar ; \
	date > visad/java3d/DATE ; \
	jar cvf visad_java3d.jar $(JAVA3D_CLASS_FILES) ; \
	tar -cvf visad_java3d.tar visad_java3d.jar ; \
	date > visad/util/DATE ; \
	jar cvf visad_util.jar $(UTIL_CLASS_FILES) ; \
	tar -cvf visad_util.tar visad_util.jar ; \
	date > visad/data/netcdf/DATE ; \
	jar cvf visad_netcdf.jar $(NETCDF_CLASS_FILES) ; \
	tar -cvf visad_netcdf.tar visad_netcdf.jar ; \
	date > visad/data/fits/DATE ; \
	jar cvf visad_fits.jar $(FITS_CLASS_FILES) ; \
	tar -cvf visad_fits.tar visad_fits.jar ; \
	date > visad/data/hdfeos/DATE ; \
	jar cvf visad_hdfeos.jar $(HDFEOS_CLASS_FILES) ; \
	tar -cvf visad_hdfeos.tar visad_hdfeos.jar ; \
	date > visad/data/vis5d/DATE ; \
	jar cvf visad_vis5d.jar $(VIS5D_CLASS_FILES) ; \
	tar -cvf visad_vis5d.tar visad_vis5d.jar ; \
	date > visad/data/gif/DATE ; \
	jar cvf visad_gif.jar $(GIF_CLASS_FILES) ; \
	tar -cvf visad_gif.tar visad_gif.jar


SAVE_FILES = \
	visad/README					\
	visad/DEDICATION				\
	visad/NOTICE					\
	visad/LICENSE					\
	visad/DATE					\
	visad/Makefile					\
	visad/rmic_script				\
	visad/*.java					\
	visad/examples/*.java				\
	visad/java3d/*.java				\
	visad/util/*.java				\
	visad/data/*.java				\
	visad/data/netcdf/*.java			\
	visad/data/netcdf/units/*.java			\
	visad/data/netcdf/units/*.jj			\
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
	/bin/rm -f backup/visad/*.java backup/visad/examples/*.java backup/visad/java3d/*.java backup/visad/util/*.java backup/visad/data/*.java backup/visad/data/netcdf/*.java backup/visad/data/netcdf/units/*.java backup/visad/data/fits/*.java backup/visad/nom/tam/fits/*.java backup/visad/nom/tam/util/*.java backup/visad/nom/tam/test/*.java backup/visad/data/hdfeos/*.java backup/visad/data/hdfeos/hdfeosc/*.java backup/visad/data/vis5d/*.java backup/visad/data/gif/*.java backup/visad/paoloa/*.java backup/visad/aune/*.java ; \
	cd .. ; \
	tar -cvf save.tar $(SAVE_FILES) ; \
	cd visad/backup ; \
	tar -xvf ../../save.tar ; \
	rm ../../save.tar ; \
	cd .. ; \
	wc backup/visad/*.java backup/visad/examples/*.java backup/visad/java3d/*.java backup/visad/util/*.java backup/visad/data/*.java backup/visad/data/netcdf/*.java backup/visad/data/netcdf/units/*.java backup/visad/data/fits/*.java backup/nom/tam/fits/*.java backup/nom/tam/util/*.java backup/nom/tam/test/*.java backup/visad/data/hdfeos/*.java backup/visad/data/hdfeos/hdfeosc/*.java backup/visad/data/vis5d/*.java backup/visad/data/gif/*.java backup/visad/paoloa/*.java backup/visad/aune/*.java


clear:
	/bin/rm -f *.java java3d/*.java util/*.java data/*.java data/netcdf/*.java data/netcdf/units/*.java data/fits/*.java ../nom/tam/fits/*.java ../nom/tam/util/*.java ../nom/tam/test/*.java data/hdfeos/*.java data/hdfeos/hdfeosc/*.java data/vis5d/*.java data/gif/*.java paoloa/*.java aune/*.java examples/*.java ; \


.SUFFIXES : .java .class

.java.class:
	javac $<

compile:
	/bin/rm -f *.class java3d/*.class util/*.class data/*.class data/netcdf/*.class data/netcdf/units/*.class data/fits/*.class ../nom/tam/fits/*.class ../nom/tam/util/*.class ../nom/tam/test/*.class data/hdfeos/*.class data/hdfeos/hdfeosc/*.class data/vis5d/*.class data/gif/*.class paoloa/*.class aune/*.class examples/*.class ; \
	javac -J-mx32m *.java ; \
	rmic visad.RemoteCellImpl visad.RemoteActionImpl visad.RemoteDisplayImpl visad.RemoteThingReferenceImpl visad.RemoteDataReferenceImpl visad.RemoteThingImpl visad.RemoteDataImpl visad.RemoteFunctionImpl visad.RemoteFieldImpl visad.RemoteServerImpl ; \
	javac java3d/*.java ; \
	javac util/*.java ; \
	javac data/*.java ; \
	cd .. ; \
	jar xvf ucar.jar ; \
	cd visad ; \
	javac data/netcdf/units/*.java ; \
	javac data/netcdf/*.java ; \
	javac ../nom/tam/util/*.java ; \
	javac ../nom/tam/fits/*.java ; \
	javac ../nom/tam/test/*.java ; \
	javac data/fits/*.java ; \
	javac data/vis5d/*.java ; \
	javac data/gif/*.java ; \
	javac data/hdfeos/hdfeosc/*.java ; \
	javac data/hdfeos/*.java ; \
	javac paoloa/*.java ; \
	javac aune/*.java ; \
	cd examples ; \
	javac *.java



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


