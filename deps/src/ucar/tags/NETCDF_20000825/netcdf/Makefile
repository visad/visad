# $Id: Makefile,v 1.1.1.6 2000-08-28 21:45:46 dglo Exp $
#
#       Makefile for java package ucar.netcdf
#       requires ucar.multiarray
#
SHELL = /bin/sh
##
# target directories
ROOT = ../../..
CLASSDIR = $(ROOT)/classes

JC = javac
JCFLAGS = -g
RMIC = rmic  # -keepgenerated

JSRCS = \
	AbstractNetcdf.java \
	Attribute.java \
	AttributeDictionary.java \
	AttributeIterator.java \
	AttributeSet.java \
	Dimension.java \
	DimensionDictionary.java \
	DimensionIterator.java \
	DimensionSet.java \
	Named.java \
	NamedDictionary.java \
	Netcdf.java \
	NetcdfFile.java \
	NetcdfRemoteProxy.java \
	NetcdfRemoteProxyImpl.java \
	NetcdfServer.java \
	NetcdfService.java \
	ProtoVariable.java \
	ProtoVariableIterator.java \
	RandomAccessFile.java \
	RemoteAccessorImpl.java \
	RemoteNetcdf.java \
	Schema.java \
	UnlimitedDimension.java \
	Variable.java \
	VariableIterator.java


PACKING_LIST = \
	package.html \
	Makefile \
	$(JSRCS)

RMICLASSES = \
	ucar.netcdf.RemoteAccessorImpl \
	ucar.netcdf.NetcdfRemoteProxyImpl \
	ucar.netcdf.NetcdfServer

all:
	$(JC) $(JCFLAGS) -d $(CLASSDIR) $(JSRCS)
	$(RMIC) $(JCFLAGS) -d $(CLASSDIR) $(RMICLASSES)

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
