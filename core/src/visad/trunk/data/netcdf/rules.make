default:	classes

all:		classes javadocs

classes:	FORCE
	@case "$(JAVASRCS)" in \
	    '') $(MAKE) `ls *.java | sed 's/\.java/.class/'` ;; \
	    *) $(MAKE) $(JAVASRCS:.java=.class) ;; \
	esac

test:		all

javadocs:	FORCE
	@case "$(JAVASRCS)" in \
	    '') $(MAKE) $(TOP_JAVADOCS) JAVASRCS=`ls *.java` ;; \
	    *) $(MAKE) $(TOP_JAVADOCS) ;; \
	esac

$(TOP_JAVADOCS):	$(JAVASRCS)
	$(JAVADOC) $(PACKAGE)

clean:
	rm -f $(GARBAGE)	\
	    AllNames.html	\
	    Package-$(PACKAGE).html	\
	    packages.html	\
	    tree.html	\
	    $(PACKAGE).*.html	\
	    *.class

.SUFFIXES:
.SUFFIXES:	.debug .test .class .html .java .jj

.jj.java:
	$(JAVACC) $<

.java.class:
	$(JAVAC) $(JAVACOPTS) $<

.class.test:
	$(JAVA_G) $(PACKAGE_PREFIX)$*

.class.debug:
	$(JDB) $(PACKAGE_PREFIX)$*

.java.html:
	$(JAVADOC) $<

FORCE:
