from visad.python.JPythonMethods import plot
from visad.python.JPythonMethods import read
from visad.python.JPythonMethods import equals
from visad.python.JPythonMethods import getClass
area = read("../examples/AREA2001")

map = read("../examples/OUTLSUPW")

plot(area)
plot(map)