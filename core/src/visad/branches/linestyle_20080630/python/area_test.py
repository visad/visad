from visad.python.JPythonMethods import *
# load two McIDAS area files
area7 = load("../examples/AREA0007")
area8 = load("../examples/AREA0008")

# extract one band from area8
area8 = area8.extract(0)

# subtract one area from the other, georeferenced
difference = area8 - area7

# plot area difference
clearplot()
plot(difference)

