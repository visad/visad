SUBPACKAGE	= .units

include macros.make

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

GARBAGE	= \
	  ASCII_CharStream.java \
	  ParseException.java Token.java \
	  TokenMgrError.java UnitParser.java

include rules.make

test:	classes	\
	UnitParser.test

UnitParser.java:	UnitParser.jj
ASCII_CharStream.java:	UnitParser.class
