
# This script is run whenever files are checked-out.

# Make a link to the "rules.make" file in the parent directory.
#
if cd units; then
    rm -f rules.make
    ln -s ../rules.make .
fi
