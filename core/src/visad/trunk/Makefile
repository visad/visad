# makefile for VisAD version 2.0

# VisAD system for interactive analysis and visualization of numerical
# data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
# Rink.
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
	@echo "	make save		save files to the backup sub-directory"

# Make the VisAD source tar file

TAR_FILES = \
	README			\
	NOTICE			\
	LICENSE			\
	Makefile		\
	rmic_script		\
	*.java			\
	*.txt			\
	*.html

tar:
	tar -cvf visad-2.0.tar $(TAR_FILES) ; \
	compress visad-2.0.tar ; \
	cp visad-2.0.tar.Z visad.taz ; \
	ls -l visad-2.0.tar.Z visad.taz

save:
	cp *.java backup
	cp *.txt backup
	cp *.html backup
	cp README backup
	cp NOTICE backup
	cp LICENSE backup
	cp Makefile backup
	cp rmic_script backup
	cp docs/DESIGN5 backup
	wc backup/*.java

.SUFFIXES : .java .class

.java.class:
	javac $<

compile:
	javac *.java

