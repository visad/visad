SUBPACKAGE	= in

include ../../../macros.make

include ../../../rules.make

.SUFFIXES:	.nc
.nc.test:	FORCE
	CLASSPATH=$(ROOTDIR):$$CLASSPATH $(JAVA) $(PACKAGE).NetcdfAdapter $<
