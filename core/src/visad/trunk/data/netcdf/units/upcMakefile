SUBPACKAGE	= .units

include ../../../macros.make

JAVASRCS	= \
		  ASCII_CharStream.java	\
		  DefaultUnitsDB.java	\
		  NamedUnit.java	\
		  NoSuchUnitException.java	\
		  ParseException.java	\
		  Parser.java		\
		  PluralUnit.java	\
		  SingleUnit.java	\
		  Token.java	\
		  TokenMgrError.java	\
		  UnitParser.java	\
		  UnitParserConstants.java	\
		  UnitParserTokenManager.java	\
		  UnitPrefix.java	\
		  UnitTable.java	\
		  UnitsDB.java

include ../../../rules.make

test:	classes	\
	UnitParser.test

.SUFFIXES:	.save .run .class
.class.run:	FORCE
	CLASSPATH=$(ROOTDIR):$$CLASSPATH $(JAVA) $(PACKAGE).$* $<

.class.save:
	$(MAKE) -s $*.run >$@ 2>&1

.class.test:
	$(MAKE) -s $*.run 2>&1 | diff -cw $*.save -

UnitParser.java:	UnitParser.jj
classes:		UnitParser.java
ASCII_CharStream.java:	UnitParser.java
ParseException.java:	UnitParser.java
Token.java:		UnitParser.java
TokenMgrError.java:	UnitParser.java
