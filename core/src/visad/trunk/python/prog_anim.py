from visad.python.JPythonMethods import *
from visad import *
from visad.util import Delay
from subs import *
import math

type = makeType("(x, y, z)");

loc = [0, 2, 0]
up = [0, 1, 0]
face = [0, 0, 1]
right = [1, 0, 0]
arml = 0
armr = 0
legl = 0
legr = 0

def normalize(a):
  norm = math.sqrt(a[0]*a[0]+a[1]*a[1]+a[2]*a[2])
  a[0] = a[0] / norm
  a[1] = a[1] / norm
  a[2] = a[2] / norm

def cross(a, b):
  return [a[1]*b[2]-a[2]*b[1],
          a[2]*b[0]-a[0]*b[2],
          a[0]*b[1]-a[1]*b[0]]

def scaled_add(a, b, s):
  return [a[0]+s*b[0], a[1]+s*b[1], a[2]+s*b[2]]

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

rot = [0.2, -0.6, 0.7]
normalize(rot)
rot2 = [-0.5, 0.3, 0.4]
normalize(rot2)
funny = [1, 0, 0]

while 1:
  r = cross(rot2, rot)
  normalize(r)
  rot2 = scaled_add(rot2, r, 0.043)
  normalize(rot2)
  r2 = cross(up, rot2)
  normalize(r2)
  up = scaled_add(up, r2, 0.1)
  normalize(up)
  f = cross(funny, rot)
  normalize(f)
  funny = scaled_add(funny, f, 0.08)
  normalize(funny)
  face = cross(up, funny)
  normalize(face)
  l = cross(loc, face)
  normalize(l)
  loc = scaled_add(loc, l, 0.9)
  normalize(loc)
  loc[0] = 4*loc[0]
  loc[1] = 4*loc[1]
  loc[2] = 4*loc[2]
  set = makeBody()
  ref.setData(set)
  Delay(100)

