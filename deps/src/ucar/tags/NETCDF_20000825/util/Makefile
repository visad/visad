# $Id: Makefile,v 1.1.1.2 2000-08-28 21:45:48 dglo Exp $
#
#       Makefile for java package ucar.util
#
SHELL = /bin/sh
##
# target directories
ROOT = ../../..
CLASSDIR = $(ROOT)/classes

JC = javac
JCFLAGS = -g

JSRCS = \
	Logger.java \
	AbstractLogger.java \
	RMILogger.java


PACKING_LIST = \
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
