CLASSDIR=${HOME}/classes

JC = javac

.SUFFIXES: .class .java

.java.class:
	${JC} ${JFLAGS} $<

all::

TestGIF.class: TestGIF.java GIFAdapter.class
	$(JC) $(JFLAGS) TestGIF.java

all:: TestGIF.class

clean::
	-rm -f TestGIF.class GIFAdapter.class

all:: GIFForm.class

clean::
	-rm -f GIFForm.class
