
# This script is run whenever files are checked-out.

# Make a link to "rules.make" file in the parent directory.
#
rm -f rules.make
ln -s ../rules.make .
