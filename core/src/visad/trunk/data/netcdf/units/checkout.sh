
# This script is run whenever files are checked-out.

# Make a link to the included make(1) files in the parent directory.
#
set -x
case $1 in
    netcdf)
	if cd netcdf/units; then
	    rm -f rules.make
	    ln -s ../rules.make .

	    rm -f macros.make
	    ln -s ../macros.make .
	fi
	;;
    units)
	if cd units; then
	    rm -f rules.make
	    ln -s ../rules.make .

	    rm -f macros.make
	    ln -s ../macros.make .
	fi
	;;
esac
