from visad.python.JPythonMethods import *
area = load("../examples/AREA0007")
area2 = load("../examples/AREA0008").extract(0)

area3 = area2 - area

clearplot()
plot(area3)
