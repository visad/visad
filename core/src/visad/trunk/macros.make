# It should only be necessary to customize the following macro.
ROOTDIR		= /home/steve/java/visad

DOCDIR		= $(ROOTDIR)/doc
CLASSDIR	= $(ROOTDIR)/classes
JARDIR		= $(ROOTDIR)/classes
SRCDIR		= $(ROOTDIR)/visad

PACKAGE		= visad
PACKAGE_PREFIX	= $(PACKAGE).

JAVACC		= javacc	# javacc is available from 
#				# <http://www.suntest.com/JavaCC/>
JAVAC		= javac -g
JAVA		= java
JAVADOC		= javadoc
POLARDOC	= polardoc	# the most-excellent polardoc is available from 
				# <http://www.ualberta.ca/~tgee/polardoc>
JDB		= jdb
JAR		= jar

TOP_JAVADOCS	= $(DOCDIR)/AllNames.html \
		  $(DOCDIR)/Package-$(PACKAGE).html \
		  $(DOCDIR)/Packages.html \
		  $(DOCDIR)/tree.html
SUBDIR_TARGETS	= dummy_subdir_targets
