from visad.python.JPythonMethods import *
from visad import *
from visad.util import Delay
from subs import *
import math

type = makeType("(x, y, z)");

loc = [0, 0, 0]
up = [0, 1, 0]
face = [0, 0, 1]
right = [1, 0, 0]
arml = 0
armr = 0
legl = 0
legr = 0

def makeBody():
  upn = math.sqrt(up[0]*up[0]+up[1]*up[1]+up[2]*up[2])
  up[0] = up[0] / upn
  up[1] = up[1] / upn
  up[2] = up[2] / upn
  facen = math.sqrt(face[0]*face[0]+face[1]*face[1]+face[2]*face[2])
  face[0] = face[0] / facen
  face[1] = face[1] / facen
  face[2] = face[2] / facen
  right[0] = up[1]*face[2]-up[2]*face[1]
  right[1] = up[2]*face[0]-up[0]*face[2]
  right[2] = up[0]*face[1]-up[1]*face[0]
  sets = []

  # make torso and head
  samples = []
  for i in range(3):
    samples.append(
      [loc[i]-2*up[i], loc[i], loc[i]+up[i], loc[i]+2*up[i]+right[i],
       loc[i]+2*up[i]-right[i], loc[i]+up[i]])
  sets.append(Gridded3DSet(type, samples, len(samples[0])))

  # make arms
  samples = []
  for i in range(3):
    samples.append(
      [loc[i]+up[i]-2*right[i], loc[i]-right[i], loc[i], loc[i]+right[i],
       loc[i]+up[i]+2*right[i]])
  sets.append(Gridded3DSet(type, samples, len(samples[0])))

  # make legs
  samples = []
  for i in range(3):
    samples.append(
      [loc[i]-4*up[i]-right[i], loc[i]-3*up[i]-right[i], loc[i]-2*up[i],
       loc[i]-3*up[i]+right[i], loc[i]-4*up[i]+right[i]])
  sets.append(Gridded3DSet(type, samples, len(samples[0])))

  set = UnionSet(type, sets)
  return set

loc = [0, 0, 0]
up = [0, 1, 0]
face = [0, 0, 1]
right = [1, 0, 0]
arml = 0
armr = 0
legl = 0
legr = 0

set = makeBody()
ref = DataReferenceImpl("set")
ref.setData(set)

maps = makeMaps(type[0], "x", type[1], "y", type[2], "z")
maps[0].setRange(-8, 8)
maps[1].setRange(-8, 8)
maps[2].setRange(-8, 8)
display = makeDisplay3D(maps)
display.addReference(ref)
showDisplay(display)

while 1:
  tup = []
  tface = []
  for i in range(3):
    tup.append(up[i]+0.1*face[i]-0.1*right[i])
    tface.append(face[i]-0.1*up[i]+0.1*right[i])
  up = tup
  face = tface
  set = makeBody()
  ref.setData(set)
  Delay(100)
