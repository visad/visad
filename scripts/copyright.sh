#!/bin/sh
cd $(dirname $0)/..
find core examples -name '*.java' -print0 | xargs -0 \
  sed -i'' -e 's/\(Copyright \{0,1\}(C) \([0-9]\{4\} - \)\{0,1\}\)\([0-9]\{4\}\)/\12021/'
