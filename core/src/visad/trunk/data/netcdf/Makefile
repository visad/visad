SUBPACKAGE	= netcdf

include ../../macros.make

GARBAGE		= plain.nc
SUBDIR_TARGETS	= units/default units/classes units/all units/clean
JAR_CLASSES	= *.class units/*.class
JARFILE		= visad.data.netcdf.jar

default:	units/default
all:		units/all
jar:		units/classes
clean:		units/clean

include ../../rules.make

test:		Plain.test
