default:	classes

all:		classes

classes:	FORCE
	@case "$(JAVASRCS)" in \
	    '') set -x; $(JAVAC) *.java ;; \
	    *)  set -x; $(JAVAC) $(JAVASRCS) ;; \
	esac

test:		classes

#jar:		$(JARDIR)/$(JARFILE)

$(JARDIR)/$(JARFILE):	classes $(JARDIR)
	case "$(JARFILE)" in \
	    '') ;; \
	    *)  $(JAR) cf $@ $(JAR_CLASSES);; \
	esac

$(DOCDIR) \
$(JARDIR) \
$(FTPDIR):
	mkdir -p $@

clean:
	rm -f $(GARBAGE) *.class;
distclean:
	rm -f $(GARBAGE) *.class *.log;

$(SUBDIR_TARGETS):	FORCE
	@subdir=`echo $@ | sed 's,/.*,,'`; \
	target=`echo $@ | sed 's,.*/,,'`; \
	case $$target in default) target=;; esac; \
	$(MAKE) subdir=$$subdir target=$$target subdir_target

subdir_target:
	@echo ""
	@cd $(subdir) && \
	    echo "Making \"$(target)\" in directory `pwd`" && \
	    echo "" && \
	    $(MAKE) $(target) || exit 1
	@echo ""
	@echo "Returning to directory `pwd`"
	@echo ""

.SUFFIXES:
.SUFFIXES:	.debug .test .class .html .java .jj

.jj.java:
	$(JAVACC) $<
# The following overwrites the high-level .html files -- so it's commented-
# out.
#.java.html:
#	$(JAVADOC) -J-mx64m -notree -noindex -d $(DOCDIR) $(PACKAGE_PREFIX)$*
.java.class:
	$(JAVAC) $<
.class.test:
	@cmd="$(JAVA) $(PACKAGE_PREFIX)$*"; echo $$cmd; $$cmd
.class.debug:
	@cmd="$(JDB) -classpath $$CLASSPATH $(PACKAGE_PREFIX)$*"; echo $$cmd; \
	$$cmd

deps:		FORCE
	javaFiles=`ls *.java`; \
	for javaFile in $$javaFiles; do \
	    className=`basename $$javaFile .java`; \
	    egrep -l -e \
		'new[ 	]+'$$className'\(|[^A-Za-z0-9_]'$$className'\.' \
		*.java | grep -v $$javaFile | \
		sed "s/\.java/.class:	$$className.class/"; \
	done | sort -u >depend

# The following entry may be used to force execution of a rule by placing
# it in the rule's dependency list:
FORCE:
