# $Id: Makefile,v 1.1.1.3 2000-08-28 21:43:43 dglo Exp $
#
#       Makefile for java ucar/tests
#		requires ucar.multiarray
#		requires ucar.netcdf
#
SHELL = /bin/sh
##
# target directories
ROOT = /tmp/java
CLASSDIR = $(ROOT)/classes
PKGDIR = $(CLASSDIR)/ucar/test

JC = javac
JCFLAGS = -g -d $(CLASSDIR)

JAVA = java

JSRCS = \
	TestNetcdf.java

PACKING_LIST = \
	Makefile \
	test.nc \
	test.out \
	$(JSRCS)

all: $(PKGDIR)/TestNetcdf.class

test: all
	$(JAVA) ucar.test.TestNetcdf | diff - test.out

$(PKGDIR)/TestNetcdf.class: TestNetcdf.java
	$(JC) $(JCFLAGS) TestNetcdf.java

MANIFEST:	FORCE
	$(MAKE) MANIFEST.echo >$@

# The following rule echoes the contents of $(PACKING_LIST) in the
# current directory and in all subdirectories.  All pathnames are made
# relative to the current directory.
#
MANIFEST.echo:	FORCE
	@echo $(PACKING_LIST) | fmt -1
	@if [ -n "$(SUBDIRS)" ]; then \
	    subdirs="$(SUBDIRS)"; \
	    for subdir in $$subdirs; do \
		(cd $$subdir && \
		echo 1>&2 Creating $@ in `pwd` && \
		$(MAKE) MANIFEST.echo | sed "s|^|$$subdir/|") || exit 1; \
	    done; \
	else \
	   :; \
	fi

FORCE:
