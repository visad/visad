# It should only be necessary to customize the following 2 macros.
ROOTDIR		= /home/steve/java/visad
# for testing only
FTPDIR		= /home/steve/java/visad/ftp

DOCDIR		= $(ROOTDIR)/doc
CLASSDIR	= $(ROOTDIR)/classes
JARDIR		= $(ROOTDIR)/classes
SRCDIR		= $(ROOTDIR)/visad

PACKAGE_PREFIX	= $(PACKAGE).

JAVACC		= javacc	# javacc is available from 
#				# <http://www.suntest.com/JavaCC/>
JAVAC		= javac -g -J-Xmx32m
JAVA		= java -Xmx128m
JAVADOC		= javadoc
POLARDOC	= polardoc	# the most-excellent polardoc is available from 
#				# <http://www.ualberta.ca/~tgee/polardoc>
JDB		= jdb
JAR		= jar

TOP_JAVADOCS	= $(DOCDIR)/AllNames.html \
		  $(DOCDIR)/Package-$(PACKAGE).html \
		  $(DOCDIR)/Packages.html \
		  $(DOCDIR)/tree.html
SUBDIR_TARGETS	= dummy_subdir_targets

PACKAGE		= `pwd | sed 's|$(ROOTDIR)||;s|^/||;s|/|.|g'`
