from visad.python.JPythonMethods import *
from visad import *
import math

n=16
ftype = makeType(" (time -> ( (x, y, z) -> value) )")
fdom = makeDomain(getDomainType(ftype), 1, n, n)
gtype = getRangeType(ftype)
gdom = makeDomain(getDomainType(gtype), 1, n, n,  1, n, n,  1, n, n)
seq = FieldImpl(ftype, fdom)
for i in range(0, n):
  v = []
  for x in range(0,n):
    for y in range(0,n):
      for z in range(0,n):
        v.append( math.sin(i*x*y*z*0.0174533/n) )
  ff = FlatField(gtype, gdom)
  vals = []
  vals.append(v)
  ff.setSamples(vals)
  seq.setSample(i, ff)
plot(seq)
