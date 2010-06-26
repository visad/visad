default:	classes

all:		classes

classes:	FORCE
	$(JAVAC) $(JAVAC_FLAGS) *.java

# test:		classes

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
.SUFFIXES:	.debug .test .save .run .class .html .java .jj

.jj.java:
	$(JAVACC) $<
# The following overwrites the high-level .html files -- so it's commented-
# out.
#.java.html:
#	$(JAVADOC) -J-mx64m -notree -noindex -d $(DOCDIR) $(PACKAGE_PREFIX)$*
.java.class:
	$(JAVAC) $<
.class.run:
#	@$(JAVAC) $*.java
	$(JAVA) $(PACKAGE_PREFIX)$*
#	@cmd="$(JAVA) $(PACKAGE_PREFIX)$*"; echo $$cmd; $$cmd
.class.save:
	$(MAKE) -s $*.run >$@ 2>&1
.class.test:
	$(MAKE) -s $*.run 2>&1 | diff -cw $*.save -
.class.debug:
	@cmd="$(JDB) -classpath $$CLASSPATH $(PACKAGE_PREFIX)$*"; echo $$cmd; \
	$$cmd

deps:		FORCE
	javaFiles=`ls *.java`; \
	for javaFile in $$javaFiles; do \
	    className=`basename $$javaFile .java`; \
	    grep -E -l \
		-e '(^|[^[:alnum:]])'$$className'([^[:alnum:]]|$$)' \
		*.java | \
	    grep -v "^$$javaFile$$" | \
	    sed "s/\.java/.class:	$$className.class/"; \
	done | \
	sort -u >depend
#		-e 'extends[ 	]+'$$className'([^[:alnum:]]|$$)' \
#		-e 'implements[ 	]+'$$className'([^[:alnum:]]|$$)' \
#		-e 'throws[ 	]+'$$className'([^[:alnum:]]|$$)' \
#		-e 'new[ 	]+'$$className'([^[:alnum:]]|$$)' \
#		-e '[^[:alnum:]]'$$className'\.[[:alnum:]]+' \
#

# The following entry may be used to force execution of a rule by placing
# it in the rule's dependency list:
FORCE:
