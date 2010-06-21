from visad.python.JPythonMethods import *
from visad import *
from visad.util import Delay
from subs import *
import math

# make the type (i.e., schema) for the shape
type = makeType("(x, y, z)")
ftype = makeType("(index->Set(x, y, z))")
itype = makeType("index")
index_set = Integer1DSet(itype, 100)
function = FieldImpl(ftype, index_set)
index = 0

# initialize the location of the shape
loc = [0, 2, 0]

# initialize the "up" direction for the shape
up = [0, 1, 0]

# initialize the direction for the shape is facing
face = [0, 0, 1]

# initialize the direction to the shape's right
right = [1, 0, 0]

# initialize the arm angle for the shape
arm = 0

# define a function for normalizing a vector
def normalize(a):
  norm = math.sqrt(a[0]*a[0]+a[1]*a[1]+a[2]*a[2])
  a[0] = a[0] / norm
  a[1] = a[1] / norm
  a[2] = a[2] / norm

# define a function for a cross product of vectors
def cross(a, b):
  return [a[1]*b[2]-a[2]*b[1],
          a[2]*b[0]-a[0]*b[2],
          a[0]*b[1]-a[1]*b[0]]

# define a function for rotating vector "a" around
#   vector "b" by amount "s"
def rotate(a, b, s):
  r = cross(a, b)
  normalize(r)
  a[0] = a[0] + s * r[0]
  a[1] = a[1] + s * r[1]
  a[2] = a[2] + s * r[2]
  normalize(a)

# define a function for creating a simple human shape
#   as a VisAD UnionSet data object
def makeBody():
  c = cross(up, face)
  right[0] = c[0]
  right[1] = c[1]
  right[2] = c[2]
  sets = []

  # make torso and head
  samples = []
  for i in range(3):
    samples.append(
      [loc[i]-2*up[i],
       loc[i],
       loc[i]+up[i],
       loc[i]+2*up[i]+right[i],
       loc[i]+2*up[i]-right[i],
       loc[i]+up[i]])
  sets.append(Gridded3DSet(type, samples, len(samples[0])))

  # make arms
  samples = []
  for i in range(3):
    samples.append(
      [loc[i]+up[i]-2*right[i]+2*arm*face[i],
       loc[i]-right[i]+arm*face[i],
       loc[i],
       loc[i]+right[i]+arm*face[i],
       loc[i]+up[i]+2*right[i]+2*arm*face[i]])
  sets.append(Gridded3DSet(type, samples, len(samples[0])))

  # make legs
  samples = []
  for i in range(3):
    samples.append(
      [loc[i]-4*up[i]-right[i],
       loc[i]-3*up[i]-right[i],
       loc[i]-2*up[i],
       loc[i]-3*up[i]+right[i],
       loc[i]-4*up[i]+right[i]])
  sets.append(Gridded3DSet(type, samples, len(samples[0])))

  set = UnionSet(type, sets)
  return set

# create the initial shape
set = makeBody()

# connect the shape to a VisAD DataReference
ref = DataReferenceImpl("set")
function.setSample(index, set)
index = index + 1
ref.setData(function)

# make the display mapings for the shape
maps = makeMaps(type[0], "x", type[1], "y", type[2], "z")
maps[0].setRange(-8, 8)
maps[1].setRange(-8, 8)
maps[2].setRange(-8, 8)

# make a VisAD display with those mappings
display = makeDisplay3D(maps)

# connect the DataReference to the display
display.addReference(ref)

# show the display on the screen
showDisplay(display)

# initialize some vectors use for rotating the shape
rot = [0.2, -0.6, 0.7]
normalize(rot)
rot2 = [-0.5, 0.3, 0.4]
normalize(rot2)
funny = [1, 0, 0]
arminc = 0

for i in range(99):
  # wait 1000 milleseconds
  Delay(1000)

  rotate(rot2, rot, 0.043)

  # rotate the shape "up" direction
  rotate(up, rot2, 0.1)
  rotate(funny, rot, 0.08)

  # compute new "face" direction
  face = cross(up, funny)
  normalize(face)

  # compute new location of shape
  rotate(loc, face, 0.9)
  loc[0] = 4*loc[0]
  loc[1] = 4*loc[1]
  loc[2] = 4*loc[2]

  # compute new arm angle
  arm = 1.0 * math.sin(arminc)
  arminc = arminc + 0.4

  # make the new shape
  set = makeBody()

  # set the new shape in the DataReference connected
  #   to the display
  function.setSample(index, set)
  index = index + 1
