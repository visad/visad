# The following macros may be overridden by the top-level makefile.
JAVADIR = /opt/java
JAVAARCH = solaris
JC = javac
JFLAGS = -J-mx32m
JH = javah
RM = rm -f

all:
	@if javap dods.dap.DConnect >/dev/null 2>&1; then \
	    cmd='$(JC) $(JFLAGS) *.java'; \
	else \
	    cmd='$(JC) $(JFLAGS) DODSForm.java'; \
	fi; \
	echo "$$cmd"; \
	$$cmd

clean:
	$(RM) *.class *~
