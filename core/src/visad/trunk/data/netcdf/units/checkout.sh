
# This script is run whenever files are checked-out.

# Make a link to the included make(1) files in the parent directory.
#
set -x

for file in macros.make rules.make; do
    if test -r ../$file; then
	rm -f $file
	ln -s ../$file .
    fi
done
