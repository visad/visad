from visad.python.JPythonMethods import *
from visad import *
from visad.util import Delay
import subs

type = makeType("(x, y, z)");


samples = [[-1, -0.5,  0, 0.5, 1],
           [ 1,  0,   -1, 0,   1],
           [ 1,  0,    1, 0,   1]]

set = Gridded3DSet(type, samples, len(samples[0]))

ref = DataReferenceImpl("set")
ref.setData(set)
maps=subs.makeMaps(type[0], "x", type[1], "y", type[2], "z")
display = subs.makeDisplay3D(maps)
display.addReference(ref)
subs.showDisplay(display)

while 1:
  samples[1][2] = -samples[1][2]
  set = Gridded3DSet(type, samples, len(samples[0]))
  ref.setData(set)
  Delay(500)

