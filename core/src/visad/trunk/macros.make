# It should only be necessary to customize the following 2 macros.
ROOTDIR		= /home/steve/java/visad
# for testing only
FTPDIR		= /home/steve/java/visad/ftp

MAKEFILE	= upcMakefile
MAKE		= make -f $(MAKEFILE)

DOCDIR		= $(ROOTDIR)/docs
CLASSDIR	= $(ROOTDIR)/classes
JARDIR		= $(ROOTDIR)/classes
SRCDIR		= $(ROOTDIR)/visad
NETCDF_JAR	= /upc/share/classes/ucar19980123.jar

JAVACC		= javacc	# javacc is available from 
#				# <http://www.suntest.com/JavaCC/>
JAVAC		= javac -g -J-Xmx32m
#JAVAC		= javac -g -J-Xmx32m -d $(CLASSDIR)
JAVA		= java -Xmx128m
JAVADOC		= javadoc
POLARDOC	= polardoc	# the most-excellent polardoc is available from 
#				# <http://www.ualberta.ca/~tgee/polardoc>
JDB		= jdb
JAR		= jar

TOP_JAVADOCS	= $(DOCDIR)/index.html \
		  $(DOCDIR)/overview-summary.html \
		  $(DOCDIR)/overview-tree.html \
		  $(DOCDIR)/deprecated-list.html \
		  $(DOCDIR)/serialized-form.html \
		  $(DOCDIR)/overview-frame.html \
		  $(DOCDIR)/allclasses-frame.html \
		  $(DOCDIR)/help-doc.html \
		  $(DOCDIR)/index-all.html
SUBDIR_TARGETS	= dummy_subdir_targets

PACKAGE		= `pwd | sed 's|'$(SRCDIR)'|visad|;s|^/||;s|/|.|g'`
PACKAGE_PREFIX	= $(PACKAGE).
