from visad.python.JPythonMethods import *
from visad import *
from visad.util import Delay
import string
from subs import *
import math

# number of atoms in input file
# determined beforehand
# there should be a better way to derive through data read
n_atoms = 656

# make type for x,y,z positions of atoms of specified id
# independent variable is id_num
atom_type = makeType("(index -> (x, y, z, id))");
atom_range = atom_type.getRange()

# Set range for atoms as total number read in
atom_dom = makeDomain("index", 1, n_atoms, n_atoms)

# open dna molecule location array with identifiers
molecule=open("dna_molecule.txt","r")

# string positions of x, y and z atom locations
# determined by looking at incoming data set
# there should be a better way to read data with separators
xpos=5
ypos=6
zpos=7
idpos = 2

# create a temporary value list to put atom coordinates in
dna_x=[]
dna_y=[]
dna_z=[]
dna_id=[]

print "start"
# loop from 0 to number of atoms - 1
for i in range(n_atoms):

   # create a temporary expandable list to put string data in
   values = []
   # get one line at a time
   values=string.split(molecule.readline())
   # print values

   # convert the x, y and z coodinates for atom "i"
   # append x, y and z values to a triple
   temp=float(values[xpos])
   dna_x.append(temp)

   temp=float(values[ypos])
   dna_y.append(temp)

   temp=float(values[zpos])
   dna_z.append(temp)

   # set id to constant for now
   id_str = values[idpos][0]
   if id_str == "O":
     id_type = 0
   elif id_str == "N":
     id_type = 1
   elif id_str == "C":
     id_type = 2
   elif id_str == "P":
     id_type = 3
   else:
     id_type = -1
   # append id to location triple
   dna_id.append(id_type)

locs = FlatField(atom_type, atom_dom)
locs.setSamples([dna_x, dna_y, dna_z, dna_id])

maps = makeMaps(atom_range[0], "x", atom_range[1], "y", atom_range[2], "z",
                atom_range[3], "shape")
maps[0].setRange(-20, 20)
maps[1].setRange(-20, 20)
maps[2].setRange(-20, 20)
plot (locs, maps)

control = maps[3].getControl()
normals = [0.0,  0.0,  1.0,   1.0,  0.0,  0.0,   0.0,  1.0,  0.0,
           0.0,  0.0,  1.0,   0.0,  1.0,  0.0,  -1.0,  0.0,  0.0,
           0.0,  0.0,  1.0,  -1.0,  0.0,  0.0,   0.0, -1.0,  0.0,
           0.0,  0.0,  1.0,   0.0, -1.0,  0.0,   1.0,  0.0,  0.0,
           0.0,  0.0, -1.0,   1.0,  0.0,  0.0,   0.0,  1.0,  0.0,
           0.0,  0.0, -1.0,   0.0,  1.0,  0.0,  -1.0,  0.0,  0.0,
           0.0,  0.0, -1.0,  -1.0,  0.0,  0.0,   0.0, -1.0,  0.0,
           0.0,  0.0, -1.0,   0.0, -1.0,  0.0,   1.0,  0.0,  0.0]
coords = []
for i in range(72):
  coords.append(0.05 * normals[i])

cols = [1.0, 0.0, 0.0,  0.0, 0.0, 1.0,  0.25, 0.75, 0.75,  0.5, 0.5, 0.2]

atoms = []
for i in range(4):
  atoms.append(VisADTriangleArray())
  atoms[i].vertexCount = 24
  atoms[i].coordinates = coords
  atoms[i].normals = normals
  colors = []
  for j in range(72):
    colors.append(int(255.0 * cols[3*i + j%3]))
  atoms[i].colors = colors

control.setShapeSet(Integer1DSet(4))
control.setShapes(atoms)

# close the file for other uses
molecule.close()

print "done"