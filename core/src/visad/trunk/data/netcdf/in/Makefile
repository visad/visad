SUBPACKAGE	= in

include ../../../macros.make

include ../../../rules.make

.SUFFIXES:	.nc
.nc.test:	FORCE
	CLASSPATH=$(ROOTDIR):$$CLASSPATH $(JAVA) $(PACKAGE).NetcdfAdapter $<

.nc.debug:
	$(JDB) -classpath $(ROOTDIR):$(NETCDF_JAR) \
	    $(PACKAGE_PREFIX)NetcdfAdapter $<


NcFlatRange.class:	NcRange.java

NcNestedRange.class:	NcRange.java

NcRange.class:		NcFlatRange.java

NcFlatField.class:	NcFlatRange.java

NcNestedData.class:	NcData.java
NcNestedData.class:	NcNestedField.java

NcScalar.class:		NcData.java
