SUBPACKAGE	= in

include ../../../macros.make

include ../../../rules.make

.SUFFIXES:	.nc
.nc.test:	FORCE
	CLASSPATH=$(ROOTDIR):$$CLASSPATH $(JAVA) $(PACKAGE).NetcdfAdapter $<

.nc.debug:
	$(JDB) -classpath $(ROOTDIR):$(NETCDF_JAR) \
	    $(PACKAGE_PREFIX)NetcdfAdapter $<
