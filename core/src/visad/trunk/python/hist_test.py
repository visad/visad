from visad.python.JPythonMethods import *
area = load("../examples/AREA0008")

histogram = hist(area, [0, 1])

clearplot()
plot(histogram)
