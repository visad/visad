from visad.python.JPythonMethods import *
import math

# make an n x n grid of values
n = 16

# make the type (i.e. schema) for the 2 x 2 grid domain
type = makeType("(x, y)");

# make the 2 x 2 grid domain
set = makeDomain(type, 1, n, n, 1, n, n)

# create an array to hold the grid values
values = []

# nested loops for the 2-D grid
for y in range(n):
  for x in range(n):

    # compute the grid value at (x, y)
    values.append( math.sin(4 * x * y * 0.017) )

# create a VisAD field with the grid values
data = field(set, "value", values)

# plot the field
plot(data)
