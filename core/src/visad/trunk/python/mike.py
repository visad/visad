from visad.python.JPythonMethods import *
from visad import *
import math

# n time steps of an n x n x n grid
n=16

# make the type (i.e., schema) for the time sequence of grids
ftype = makeType(" (time -> ( (x, y, z) -> value) )")

# make the (1,...,n) sampling for the time domain
#   this needs the domain type of the sequence
fdom = makeDomain(getDomainType(ftype), 1, n, n)

# get the type of the grid (i.e., the "range" of the sequence)
gtype = getRangeType(ftype)

# make the (1,...,n) x (1,...,n) x (1,...,n) sampling for the grid domain
#   this needs the domain type of the grid
gdom = makeDomain(getDomainType(gtype), 1, n, n,  1, n, n,  1, n, n)

# create the time sequence data object (a VisAD FieldImpl)
seq = FieldImpl(ftype, fdom)

# loop for each time step in the sequence
for i in range(0, n):

  # create an array to hold the grid values for time step i
  v = []

  # nested loops for 3-D grid of values
  for x in range(0, n):
    for y in range(0, n):
      for z in range(0, n):

        # compute the grid value at (x, y, z)
        v.append( math.sin(i*x*y*z*0.0174533/n) )

  # create a grid data object (a VisAD FlatField)
  ff = FlatField(gtype, gdom)

  # put the array of grid values inside another array
  #   if there were multiple values at a grid point, then
  #   we would append a "v" array for each value to "vals"
  vals = []
  vals.append(v)

  # put the float array into the grid data object
  ff.setSamples(vals)

  # put the grid data object as the i-th sample of the time sequence
  seq.setSample(i, ff)

# plot the time sequence of grids
plot(seq)

