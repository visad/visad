JAVAC		= javac -g
JAVA		= java
JAVA_G		= java_g
JAVACC		= javacc
JAVADOC		= javadoc
PACKAGE		= visad.data.netcdf.units
PACKAGE_PREFIX	= $(PACKAGE).

all:	UnitParser.class \
	DefaultUnitsDB.class \
	NamedUnit.class \
	PluralUnit.class \
	Prefix.class \
	SingleUnit.class \
	UnitTable.class \
	UnitsDB.class

test:	all \
	UnitParser.test

documentation:
	$(JAVADOC) $(PACKAGE)

clean:
	rm -f *.class *.html .cvsignore .exrc ASCII_CharStream.java \
	    ParseError.java ParseException.java Token.java \
	    TokenMgrError.java UnitParser.java

.SUFFIXES:
.SUFFIXES:	.test .class .html .java .jj

.jj.java:
	$(JAVACC) $<

.java.class:
	$(JAVAC) $<

.class.test:
	@echo ''
	$(JAVA_G) $(PACKAGE_PREFIX)$*
	@echo ''

.java.html:
	$(JAVADOC) $<

UnitParser.java:	UnitParser.jj
UnitParser.class:	UnitParser.java
