CLASSDIR=${HOME}/classes

JC = javac
JAVADOC = javadoc

DOCDIR=html

.SUFFIXES: .class .java

.java.class:
	d=`pwd`; cd ../..; ${JC} ${JFLAGS} $$d/$<

all::

tam/util/ArrayFuncs.class: tam/util/ArrayFuncs.java
	${JC} ${JFLAGS} tam/util/ArrayFuncs.java

tam/util/BufferedDataInputStream.class: \
			tam/util/BufferedDataInputStream.java
	${JC} ${JFLAGS} tam/util/BufferedDataInputStream.java

tam/util/BufferedDataOutputStream.class: \
			tam/util/BufferedDataOutputStream.java
	${JC} ${JFLAGS} tam/util/BufferedDataOutputStream.java

tam/util/TableException.class: tam/util/TableException.java
	${JC} ${JFLAGS} tam/util/TableException.java

tam/util/DataTable.class: tam/util/DataTable.java \
			tam/util/TableException.class
	${JC} ${JFLAGS} tam/util/DataTable.java

tam/util/ColumnTable.class: tam/util/ColumnTable.java \
			tam/util/DataTable.class
	${JC} ${JFLAGS} tam/util/ColumnTable.java

UTILITY_CLASSES = \
	tam/util/ArrayFuncs.class \
	tam/util/BufferedDataInputStream.class \
	tam/util/BufferedDataOutputStream.class \
	tam/util/TableException.class \
	tam/util/DataTable.class \
	tam/util/ColumnTable.class

all:: ${UTILITY_CLASSES}

clean::
	-rm -f ${UTILITY_CLASSES}

tam/fits/FitsException.class: tam/fits/FitsException.java
	${JC} ${JFLAGS} tam/fits/FitsException.java

tam/fits/Column.class: tam/fits/Column.java
	${JC} ${JFLAGS} tam/fits/Column.java

tam/fits/Data.class: tam/fits/Data.java \
			tam/fits/FitsException.class \
			tam/util/ArrayFuncs.class \
			tam/util/BufferedDataInputStream.class \
			tam/util/BufferedDataOutputStream.class
	${JC} ${JFLAGS} tam/fits/Data.java

tam/fits/HeaderCardException.class: tam/fits/HeaderCardException.java \
			tam/fits/FitsException.class
	${JC} ${JFLAGS} tam/fits/HeaderCardException.java

tam/fits/HeaderCard.class: tam/fits/HeaderCard.java \
			tam/fits/HeaderCardException.class
	${JC} ${JFLAGS} tam/fits/HeaderCard.java

tam/fits/TruncatedFileException.class: \
			tam/fits/TruncatedFileException.java
	${JC} ${JFLAGS} tam/fits/TruncatedFileException.java

HEADER_CLASSES = \
	tam/fits/KeyChain.class \
	tam/fits/KeyHash.class \
	tam/fits/CardTable.class \
	tam/fits/Header.class

$(HEADER_CLASSES): tam/fits/Header.java tam/fits/HeaderCard.class \
			tam/fits/Data.class \
			tam/fits/TruncatedFileException.class \
			tam/util/ArrayFuncs.class
	${JC} ${JFLAGS} tam/fits/Header.java

tam/fits/BinaryTableHeaderParser.class: \
			tam/fits/BinaryTableHeaderParser.java \
			tam/fits/Header.class
	${JC} ${JFLAGS} tam/fits/BinaryTableHeaderParser.java

tam/fits/BinaryTable.class: tam/fits/BinaryTable.java \
			tam/fits/BinaryTableHeaderParser.class \
			tam/fits/Column.class tam/fits/Data.class
	${JC} ${JFLAGS} tam/fits/BinaryTable.java

tam/fits/ImageData.class: tam/fits/ImageData.java \
			tam/fits/Data.class
	${JC} ${JFLAGS} tam/fits/ImageData.java

tam/fits/BadHeaderException.class: tam/fits/BadHeaderException.java
	${JC} ${JFLAGS} tam/fits/BadHeaderException.java

tam/fits/FitsDate.class: tam/fits/FitsDate.java
	${JC} ${JFLAGS} tam/fits/FitsDate.java

tam/fits/BasicHDU.class: tam/fits/BasicHDU.java tam/fits/FitsDate.class \
			tam/fits/Header.class tam/fits/Data.class
	${JC} ${JFLAGS} tam/fits/BasicHDU.java

tam/fits/PrimaryHDU.class: tam/fits/PrimaryHDU.java tam/fits/BasicHDU.class \
			tam/fits/BadHeaderException.class
	${JC} ${JFLAGS} tam/fits/PrimaryHDU.java

tam/fits/RandomGroupsHDU.class: tam/fits/RandomGroupsHDU.java \
			tam/fits/PrimaryHDU.class
	${JC} ${JFLAGS} tam/fits/RandomGroupsHDU.java

tam/fits/ExtensionHDU.class: tam/fits/ExtensionHDU.java tam/fits/BasicHDU.class
	${JC} ${JFLAGS} tam/fits/ExtensionHDU.java

tam/fits/ImageHDU.class: tam/fits/ImageHDU.java tam/fits/ExtensionHDU.class \
			tam/fits/ImageData.class
	${JC} ${JFLAGS} tam/fits/ImageHDU.java

tam/fits/TableHDU.class: tam/fits/TableHDU.java tam/fits/ExtensionHDU.class
	${JC} ${JFLAGS} tam/fits/TableHDU.java

tam/fits/AsciiTableHDU.class: tam/fits/AsciiTableHDU.java \
			tam/fits/TableHDU.class
	${JC} ${JFLAGS} tam/fits/AsciiTableHDU.java

tam/fits/BinaryTableHDU.class: tam/fits/BinaryTableHDU.java \
			tam/fits/TableHDU.class \
			tam/fits/BinaryTable.class
	${JC} ${JFLAGS} tam/fits/BinaryTableHDU.java

tam/fits/A3DTableHDU.class: tam/fits/A3DTableHDU.java \
			tam/fits/BinaryTableHDU.class
	${JC} ${JFLAGS} tam/fits/A3DTableHDU.java

tam/fits/HDU.class: tam/fits/HDU.java tam/fits/ImageHDU.class \
			tam/fits/PrimaryHDU.class \
			tam/fits/RandomGroupsHDU.class \
			tam/fits/AsciiTableHDU.class \
			tam/fits/BinaryTableHDU.class \
			tam/fits/A3DTableHDU.class
	${JC} ${JFLAGS} tam/fits/HDU.java

tam/fits/Fits.class: tam/fits/Fits.java tam/fits/HDU.class
	${JC} ${JFLAGS} tam/fits/Fits.java

FITS_CLASSES = \
	tam/fits/FitsException.class \
	tam/fits/Column.class \
	tam/fits/Data.class \
	tam/fits/HeaderCard.class \
	tam/fits/HeaderCardException.class \
	tam/fits/TruncatedFileException.class \
	$(HEADER_CLASSES) \
	tam/fits/BinaryTableHeaderParser.class \
	tam/fits/BinaryTable.class \
	tam/fits/BinaryTableHDU.class \
	tam/fits/ImageData.class \
	tam/fits/BadHeaderException.class \
	tam/fits/BasicHDU.class \
	tam/fits/PrimaryHDU.class \
	tam/fits/RandomGroupsHDU.class \
	tam/fits/ExtensionHDU.class \
	tam/fits/ImageHDU.class \
	tam/fits/TableHDU.class \
	tam/fits/AsciiTableHDU.class \
	tam/fits/BinaryTableHDU.class \
	tam/fits/A3DTableHDU.class \
	tam/fits/HDU.class \
	tam/fits/Fits.class

all:: ${FITS_CLASSES}

clean::
	-rm -f ${FITS_CLASSES}

docs:
	@if [ ! -d $(DOCDIR) ]; then mkdir $(DOCDIR); fi
	THISDIR=`pwd`;							\
	cd .. && $(JAVADOC) -d $$THISDIR/$(DOCDIR)			\
			nom/tam/fits nom/tam/functor nom/tam/test nom/tam/util

tam/test/FitsTester.class: tam/test/FitsTester.java \
			tam/fits/Fits.class
	${JC} ${JFLAGS} tam/test/FitsTester.java

all:: tam/test/FitsTester.class

clean::
	-rm -f tam/test/FitsTester.class
