# $Id: Makefile,v 1.1.1.4 2000-08-28 21:44:19 dglo Exp $
#
#       Makefile for java package ucar.multiarray
#
SHELL = /bin/sh
##
# target directories
ROOT = /tmp/java
CLASSDIR = $(ROOT)/classes

JC = javac
JCFLAGS = -g

JSRCS = \
	AbstractAccessor.java \
	Accessor.java \
	ArrayMultiArray.java \
	ClipMap.java \
	ConcreteIndexMap.java \
	DecimateMap.java \
	FlattenMap.java \
	FlipMap.java \
	IndexIterator.java \
	IndexMap.java \
	MultiArray.java \
	MultiArrayImpl.java \
	MultiArrayInfo.java \
	MultiArrayProxy.java \
	OffsetIndexIterator.java \
	RemoteAccessor.java \
	ScalarMultiArray.java \
	SliceMap.java \
	StringCharAdapter.java \
	TransposeMap.java

PACKING_LIST = \
	package.html \
	Makefile \
	$(JSRCS)
all:
	$(JC) $(JCFLAGS) -d $(CLASSDIR) $(JSRCS)

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
