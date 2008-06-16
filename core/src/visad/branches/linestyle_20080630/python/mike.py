from visad.python.JPythonMethods import *
from visad import *
import math

# n time steps of an n x n x n grid
n=16

# make the type (i.e., schema) for the time sequence of grids
ftype = makeType(" (time -> ( (x, y, z) -> value) )")

# make the (1,...,n) sampling for the time domain
fdom = makeDomain("time", 1, n, n)

# make the (1,...,n) x (1,...,n) x (1,...,n) sampling for the
#   grid domain
gdom = makeDomain("(x, y, z)", 1, n, n,  1, n, n,  1, n, n)

# create the time sequence data object (a VisAD FieldImpl)
seq = FieldImpl(ftype, fdom)

# loop for each time step in the sequence
for i in range(0, n):

  # create an array to hold the grid values for time step i
  v = []

  # nested loops for 3-D grid of values
  for z in range(0, n):
    for y in range(0, n):
      for x in range(0, n):

        # compute the grid value at (x, y, z)
        v.append( math.sin(i*x*y*z*0.0174533/n) )

  # create a grid field as the i-th sample of the time sequence
  seq.setSample(i, field(gdom, "value", v) )

# plot the time sequence of grids
plot(seq)
