from visad.python.JPythonMethods import *
import subs
from visad import *
from visad.util import VisADSlider
from visad.util import LabeledColorWidget

# load our neep602_mystery data set, available from
# ftp://www.ssec.wisc.edu/pub/visad-2.0/course/neep602_mystery
a = load("neep602_mystery")

# get one 3-D grid from the data set, in this case the 8-th
grid = a[7]

# get the type (i.e., schema) information for the 3-D grid
d = domainType(grid)
r = rangeType(grid)

# get the spatial sampling of the 3-D grid
set = getDomain(grid)

# assuming the spatial sampling is rectangular, get its factors
xset = set.getX()
yset = set.getY()
zset = set.getZ()

# get units, coordinate system and errors for the sampling
units = set.getSetUnits()
cs = set.getCoordinateSystem()
errors = set.getSetErrors()

# get the actual x, y and z values of the spatial sampling
xv = xset.getSamples()[0]
yv = yset.getSamples()[0]
zv = zset.getSamples()[0]

# create a display with mappings for our grid
maps = subs.makeMaps(d[0], "y", d[1], "x", d[2], "z", r, "rgb")
maps[2].setRange(zv[0], zv[-1])
display = subs.makeDisplay(maps)
rgbwidget = LabeledColorWidget(maps[3])

# create an interactive slider for choosing a grid level
level = DataReferenceImpl("height")
slider = VisADSlider("level", int(1000.0 * zv[0]),
                     int(1000.0 * zv[-1]),
                     int(1000.0 * zv[0]), 0.001,
                     level, d[2])

# define a function for extracting a grid level at height 'z'
def makeSlice(z):
  # initialize arrays for a 2-D grid at height 'z'
  xs = []
  ys = []
  zs = []
  # loops for the x and y values from the original 3-D grid
  for y in yv:
    for x in xv:
      # set x and y locations for the 2-D grid
      xs.append(x)
      ys.append(y)
      # set constant z heights in the 2-D grid
      zs.append(z)
      # or, for a curved slice, use this instead:
      # zs.append(z + 0.04 * ((x-xv[9])*(x-xv[9])+(y-yv[9])*(y-yv[9])))
  # create a 2-D grid embedded at height 'z' in 3-D space
  slice_set = Gridded3DSet(d, [xs, ys, zs], len(xv), len(yv),
                           cs, units, errors)
  # resample our original 3-D grid to the 2-D grid
  return grid.resample(slice_set)

# add an initial slice to the display
slice = subs.addData("slice", makeSlice(zv[0]), display)

# a little program to run whenever the user moves the slider
#   it displays a 2-D grid at the height defined by the slider
class MyCell(CellImpl):
  def doAction(this):
    z = level.getData().getValue()
    slice.setData(makeSlice(z))

# connect the slider to the little program
cell = MyCell();
cell.addReference(level)

# turn on axis scales in the display
showAxesScales(display, 1)

# show the display on the screen, along with the slider
subs.showDisplay(display, top=slider, bottom=rgbwidget)

# ordinary plot of the 3-D grid for comparison
plot(grid)

