from visad.python.JPythonMethods import *
# load a McIDAS area file
area = load("../examples/AREA0008")

# compute a 2-D histogram of the first two bands of the area
histogram = hist(area, [0, 1])

# plot the histogram
clearplot()
plot(histogram)

