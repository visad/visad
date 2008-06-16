CLASSDIR=${HOME}/classes

JC = javac

.SUFFIXES: .class .java

.java.class:
	$(JC) $(JFLAGS) $<

RM = rm -f

all::

TestGIF.class: TestGIF.java GIFAdapter.class
	$(JC) $(JFLAGS) TestGIF.java

all:: TestGIF.class

clean::
	-$(RM) TestGIF.class GIFAdapter.class

all:: GIFForm.class

clean::
	-$(RM) GIFForm.class

clean::
	-$(RM) *~
