SUBPACKAGE	= in

include ../../../macros.make

include ../../../rules.make

test:	\
	scalar.test \
	sounding.test \
	time.test
	small.test \
	pmsl.test \
	big.test \
	lwc3rms.test \
	b2rlc.test \
	rueden.test \
	images.test \
	2332_2365.test

.SUFFIXES:	.save .run .nc
.nc.run:	FORCE
	CLASSPATH=$(ROOTDIR):$$CLASSPATH $(JAVA) $(PACKAGE).NetcdfAdapter $<

.nc.debug:
	$(JDB) -classpath $(ROOTDIR):$(NETCDF_JAR) \
	    $(PACKAGE_PREFIX)NetcdfAdapter $<

.nc.save:
	$(MAKE) -s $*.run >$@

.nc.test:
	$(MAKE) -s $*.run | diff -cw $*.save -


NcFlatRange.class:	NcRange.java

NcNestedRange.class:	NcRange.java

NcRange.class:		NcFlatRange.java

NcFlatField.class:	NcFlatRange.java

NcNestedData.class:	NcData.java
NcNestedData.class:	NcNestedField.java

NcScalar.class:		NcData.java
