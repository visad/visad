default:	classes

all:		classes

classes:	FORCE
	@case "$(JAVASRCS)" in \
	    '') $(MAKE) `ls *.java | sed 's/\.java/.class/'` ;; \
	    *) $(MAKE) $(JAVASRCS:.java=.class) ;; \
	esac

test:		classes

jar:		$(JARDIR)/$(JARFILE)

$(JARDIR)/$(JARFILE):	classes $(JARDIR)
	case "$(JARFILE)" in \
	    '') ;; \
	    *)  $(JAR) cf $@ $(JAR_CLASSES);; \
	esac

$(DOCDIR) \
$(JARDIR):
	mkdir $@

clean:
	rm -f $(GARBAGE) *.class;

#	subdir=`pwd | sed 's:$(SRCDIR)::'`; \
#	    echo rm $(CLASSDIR)$$subdir/*.class

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
.SUFFIXES:	.debug .test .class .java .jj

.jj.java:
	$(JAVACC) $<

# The "CLASSPATH=" settings in the following ensure that the package
# sources are used before any installed .class files.
#
.java.class:
	CLASSPATH=$(ROOTDIR):$$CLASSPATH $(JAVAC) $<
#	CLASSPATH=$(CLASSDIR):$$CLASSPATH $(JAVAC) -d $(CLASSDIR) $<
.class.test:
	CLASSPATH=$(ROOTDIR):$$CLASSPATH $(JAVA) $(PACKAGE_PREFIX)$*
#	CLASSPATH=$(CLASSDIR):$$CLASSPATH $(JAVA) $(PACKAGE_PREFIX)$*
.class.debug:
	CLASSPATH=$(ROOTDIR):$$CLASSPATH $(JDB) $(PACKAGE_PREFIX)$*
#	CLASSPATH=$(CLASSDIR):$$CLASSPATH $(JDB) $(PACKAGE_PREFIX)$*

# The following entry may be used as a dependency in order to force
# execution of the associated rule.
FORCE:
