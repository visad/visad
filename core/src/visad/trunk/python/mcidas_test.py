from visad.python.JPythonMethods import *
area = read("../examples/AREA2001")

map = read("../examples/OUTLSUPW")

plot(area)
plot(map)
