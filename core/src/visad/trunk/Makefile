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
	@echo "	make tar		to make visad-2.0.tar.Z"
	@echo "	make jar		to make visad-2.0.jar"
	@echo "	make save		save files to the backup sub-directory"

# Make the VisAD source tar file

TAR_FILES = \
	visad/README					\
	visad/DEDICATION				\
	visad/NOTICE					\
	visad/LICENSE					\
	visad/DATE					\
	visad/Makefile					\
	visad/rmic_script				\
	visad/*.java					\
	visad/java3d/*.java				\
	visad/util/*.java				\
	visad/data/*.java				\
	visad/data/netcdf/*.java			\
	visad/data/netcdf/units/*.java			\
	visad/data/netcdf/units/*.jj			\
	visad/data/fits/*.java				\
	visad/data/fits/fits/*.java			\
	visad/data/fits/util/*.java			\
	visad/data/hdfeos/*.java			\
	visad/paoloa/GoesCollaboration.java		\
	visad/ucar.jar					\
	visad/Gridded1D.txt				\
	visad/Gridded2D.txt				\
	visad/Gridded3D.txt				\
	visad/contour.html				\
	visad/delaunay.html				\
	visad/plotdigits.html

tar:
	date > DATE ; \
	cd .. ; \
	tar -cvf visad-2.0.tar $(TAR_FILES) ; \
	compress visad-2.0.tar ; \
	mv visad-2.0.tar.Z visad ; \
	cd visad ; \
	cp visad-2.0.tar.Z visad.taz ; \
	ls -l visad-2.0.tar.Z visad.taz

jar:
	date > DATE ; \
	cd .. ; \
	jar cvf visad-2.0.jar $(TAR_FILES) ; \
	mv visad-2.0.jar visad ; \
	cd visad ; \
	cp visad-2.0.jar visad.jar ; \
	ls -l visad-2.0.jar visad.jar

save:
	cp *.java backup
	cp java3d/*.java backup/java3d
	cp data/*.java backup/data
	cp data/netcdf/*.java backup/data/netcdf
	cp data/netcdf/plain/*.java backup/data/netcdf/plain
	cp paoloa/*.java backup/paoloa
	cp paoloa/*.f backup/paoloa
	cp paoloa/*.c backup/paoloa
	cp paoloa/*.h backup/paoloa
	cp paoloa/Makefile backup/paoloa
	cp *.txt backup
	cp *.html backup
	cp README backup
	cp DEDICATION backup
	cp NOTICE backup
	cp LICENSE backup
	cp Makefile backup
	cp rmic_script backup
	cp docs/DESIGN5 backup
	wc backup/*.java backup/java3d/*.java backup/data/*.java backup/data/netcdf/*.java backup/data/netcdf/plain/*.java backup/paoloa/*.java

.SUFFIXES : .java .class

.java.class:
	javac $<

compile:
	javac -J-mx32m *.java ; \
	rmic_script ; \
	javac java3d/*.java ; \
	javac util/*.java ; \
	javac paoloa/GoesCollaboration.java

