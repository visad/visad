SUBPACKAGE	= netcdf

include ../../macros.make

GARBAGE		= plain.nc
SUBDIR_TARGETS	= in/default in/classes in/all in/clean \
		  out/default out/classes out/all out/clean \
		  units/default units/classes units/all units/clean
JAR_CLASSES	= *.class in/*.class out/*.class units/*.class
JARFILE		= visad.data.netcdf.jar

default:	in/default out/default units/default
all:		in/all out/all units/all
jar:		in/classes out/classes units/classes
clean:		in/clean out/clean units/clean

include ../../rules.make

test:		small.test big.test pmsl.test #time.test

.SUFFIXES:	.nc
.nc.test:	FORCE
	CLASSPATH=$(ROOTDIR):$$CLASSPATH $(JAVA) $(PACKAGE).Plain $<

.nc.debug:
	$(JDB) -classpath $(ROOTDIR):$(NETCDF_JAR) $(PACKAGE_PREFIX)Plain $<
