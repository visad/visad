from visad.python.JPythonMethods import *
from visad import *
from visad.data.netcdf import Plain

# construct file interface for writing data
file = Plain()

# create generic real file
a = Real(1.0)
file.save("generic_real.nc", a, 1)

# create typed real file
t = RealType("temp")
rt = Real(t, 1.0)
file.save("real.nc", rt, 1)

# create typed real vector file
p = RealType("pres")
h = RealType("hum")
rp = Real(p, 2.0)
rh = Real(h, -1.0)
reals = [rt, rp, rh]
v = RealTuple(reals)
file.save("vector.nc", v, 1)

values = [-2.0, 0.0, 1.0, 0.0, -2.0]
f = field("temp", values)
file.save("field.nc", f, 1)
