# $Id: Makefile,v 1.1.1.1 2000-08-28 21:42:24 dglo Exp $
#
#       Makefile for java package ucar (multiarray and netcdf)
#
SHELL = /bin/sh
##
# target directories
ROOT = /tmp/java
CLASSDIR = $(ROOT)/classes
DOCDIR = $(ROOT)/docs

JC = javac
JCFLAGS = -g -d $(CLASSDIR)

JDOC = polardoc
JDOCFLAGS = -version

SUBDIRS = \
	multiarray \
	netcdf \
	tests

PACKING_LIST = \
	MANIFEST \
	VERSION \
	README \
	Makefile \
	multiarray.patch \
	netcdf.patch

all: multiarray/all netcdf/all tests/all

test: tests/test

docs:	FORCE
	$(JDOC) $(JDOCFLAGS) -d $(DOCDIR) multiarray/*.java netcdf/*.java
	-@cp multiarray/overview.html ${DOCDIR}/ucar.multiarray-Overview.html
	-@rm -f ${DOCDIR}/ucar.multiarray-Overview.html.orig
	-@patch -s ${DOCDIR}/Package-ucar.multiarray.html multiarray.patch
	-@cp netcdf/overview.html ${DOCDIR}/ucar.netcdf-Overview.html
	-@patch -s ${DOCDIR}/Package-ucar.netcdf.html netcdf.patch
	-@rm -f ${DOCDIR}/ucar.netcdf-Overview.html.orig

MANIFEST:	FORCE
	$(MAKE) MANIFEST.echo >$@

multiarray/all \
netcdf/all \
tests/all \
:
	@subdir=`echo $@ | sed 's,/.*,,'`; \
	target=`echo $@ | sed 's,.*/,,'`; \
	$(MAKE) CLASSDIR=$(CLASSDIR) SUBDIR=$$subdir TGET=$$target subdir_target
	
tests/test \
:
	@subdir=`echo $@ | sed 's,/.*,,'`; \
	target=`echo $@ | sed 's,.*/,,'`; \
	$(MAKE) $(MFLAGS) SUBDIR=$$subdir TGET=$$target subdir_target
	

subdir_target:
	@echo ""
	@cd $(SUBDIR) && \
	    echo "Making \`$(TGET)' in directory `pwd`" && \
	    echo "" && \
	    $(MAKE) $(MFLAGS) $(TGET) || exit 1
	@echo ""
	@echo "Returning to directory `pwd`"
	@echo ""

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


# Make a compressed, tar(1) file of the source distribution in the current 
# directory.
#
tar.Z:		FORCE
	@version=`cat VERSION`; \
	    $(MAKE) $(MFLAGS) ucar$$version.tar.Z VERSION=$$version

TARFLAGS = -chf

ucar$(VERSION).tar.Z:	MANIFEST
	id=ucar$(VERSION) \
	&& rm -rf $$id \
	&& mkdir $$id \
	&& ln -s .. $$id/src \
	&& tar $(TARFLAGS) - `sed "s|^|$$id/src/|" MANIFEST` | compress > $@ \
	&& rm -r $$id
FORCE:
