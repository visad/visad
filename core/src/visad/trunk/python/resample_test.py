from visad.python.JPythonMethods import *
# load two McIDAS area files
area7 = load("../examples/AREA0007")
area8 = load("../examples/AREA0008")

# extract one band from area8
area8 = area8.extract(0)

# get set of area8 pixel locations
set = area8.getDomainSet()

# resample area7 to area8 locations
area9 = area7.resample(set)

# resample area7 to area8 locations one pixel at a time
# and compute difference with all at once resample
#   NOTE - this is slow
for i in range(set.length):
	area9[i] = area9[i] - area7[set[i]]

clearplot()
plot(area9)

