"""subs.py is a collection of support methods

makeDisplay(maps)
  create (and return) a VisAD DisplayImpl and add the ScalarMaps, if any
  the VisAD box is resized to about 95% of the window.  Use 3D if
  availble.

makeDisplay3D(maps)
  create (and return) a VisAD DisplayImplJ3D and add the ScalarMaps, if any
  the VisAD box is resized to about 95% of the window
  
makeDisplay2D(maps)"
  create (and return) a VisAD DisplayImplJ2D and add the ScalarMaps, if any
  the VisAD box is resized to about 95% of the window

saveDisplay(display, filename)
  save the display as a JPEG

addData(name, data, display, constantMaps=None, renderer=None, ref=None)
  add a Data object to a Display, and return a reference to the Data

setPointSize(display, size)
  set the size of points for point-type plots

setAspectRatio(display, ratio)
  define the aspects of width and height, as a ratio: width/height

setAspects(display, x, y, z)
  define the relative sizes of the axes

maximizeBox(display, clip=1)
  a simple method for making the VisAD "box" 95% of the window size,
  and defining if box-edge clipping should be done in 3D

setBoxSize(display, percent=.70, clip=1)
  a simple method for making the VisAD "box" some % of the window size,
  and defining if box-edge clipping should be done in 3D

x,y,z,disp = getDisplay(display)
  return the x,y,z scalar maps for the display

makeLine(domainType, points)
  make a 2D or 3D line, return a reference so it can be changed

drawLine(display, points[], color=None, mathtype=None)
  draw a line directly into the display; also return reference
  drawLine(display, domainType, points[], color=Color, mathtype=domainType)
  drawLine(name|display, points[], color=Color)
  "Color" is java.awt.Color

drawString(display, string, point, color=None, center=0, font='futural',
start=, base= up=, size=.1)
  draw a string on the display; use 'size=' to set size.

textShape(string, center=0 font='futural', start=, base=, up=, size=.1)
  Creates a VisADGeometryArray (shape) for this string. Used by
  drawString, and may be used directly with the Shapes class.

addMaps(display, maps[])
  add an array of ScalarMaps to a Display

makeMaps(RealType, name, RealType, name, ....)
  define ScalarMap(s) given pairs of (Type, name)
  where "name" is taken from the list, below.

  Alternatively, you may use the "string" names of
  existing RealTypes.

showDisplay(display, width=300, height=300, title=, bottom=, top=)
  quick display of a Display object in a separate JFrame
  you can set the size and title, if you want...  Use the bottom=
  and top= keywords to add these componenets (or panels) to the bottom
  and top of the VisAD display (which always is put in the Center).

Shapes(display)
  a Class that allows you to do some easy displays of Shapes

  addShape(type, scale=.1, color=None, index=None)
    type is a string that names a pre-defined type of shape 
    ("cross", "triangle", "square", "solid_square", "solid_triangle")
    or None if the shape=keyword is used.  This will add a shape for
    the display.  If the shape= is given, it should be a 
    VisADGeometryArray.  If 'index=' is given, it is assumed you
    are simply replacing that shape with a different one.

    Returns the shape index (see next method).

  moveShape(index, coordinates)
    this will reposition the shape numbered 'index'.  The coordinates
    must be in the order of the x,y,z axes mappings, and should be
    the types of values defined by the call to 'getDisplayMaps()'. 

"""

from visad import ScalarMap, Display, DataReferenceImpl, RealTupleType,\
          Gridded2DSet, Gridded3DSet, DisplayImpl, RealType, RealTuple, \
          VisADLineArray, VisADQuadArray, VisADTriangleArray, \
          VisADGeometryArray, ConstantMap, Integer1DSet

from types import StringType
from visad.ss import BasicSSCell
from visad.java2d import DisplayImplJ2D

# try to get 3D
ok3d = 1
try:
  from visad.java3d import DisplayImplJ3D, TwoDDisplayRendererJ3D
except:
  ok3d = 0

py_text_type = RealType.getRealType("py_text_type")
py_text_map = ScalarMap(py_text_type, Display.Text)
py_shape_type = RealType.getRealType("py_shape_type")
py_shape_map = ScalarMap(py_shape_type, Display.Shape)
py_shapes = None 

# create (and return) a VisAD DisplayImplJ3D and add the ScalarMaps, if any
# the VisAD box is resized to about 95% of the window
def makeDisplay3D(maps):
  global py_shapes
  disp = DisplayImplJ3D("Jython3D")
  disp.addMap(py_text_map)
  disp.addMap(py_shape_map)

  if maps != None:  addMaps(disp, maps)
  py_shapes = Shapes(disp)
  return disp

# create (and return) a VisAD DisplayImplJ2D and add the ScalarMaps, if any
# the VisAD box is resized to about 95% of the window
def makeDisplay2D(maps):
  global py_shapes
  disp = DisplayImplJ2D("Jython2D")
  disp.addMap(py_text_map)
  disp.addMap(py_shape_map)

  print "Using 2D"
  if maps != None:  addMaps(disp, maps)
  py_shapes = Shapes(disp)
  return disp

# create (and return) a VisAD DisplayImpl and add the ScalarMaps, if any
# the VisAD box is resized to about 95% of the window
def makeDisplay(maps):
  global py_shapes
  is3d = 0
  if maps == None:
    is3d = 1
  else:
    for m in maps:
      if m.getDisplayScalar().toString() == "DisplayZAxis": is3d = 1

  if is3d == 1 and ok3d == 1:
    disp = makeDisplay3D(maps)
  else:
    if ok3d:
      tdr = TwoDDisplayRendererJ3D() 
      disp = DisplayImplJ3D("Jython3D",tdr)
      addMaps(disp, maps)
      disp.addMap(py_text_map)
      disp.addMap(py_shape_map)
      py_shapes = Shapes(disp)
    else:
      disp =  makeDisplay2D(maps)
  
  return disp

# save the display as a JPEG
def saveDisplay(disp, filename):
  from visad.util import Util
  Util.captureDisplay(disp, filename)

# add a Data object to a Display, and return a reference to the Data
def addData(name, data, disp, constantMaps=None, renderer=None, ref=None):

  if ref is None: 
    ref = DataReferenceImpl(name)
  if renderer is None:
    disp.addReference(ref, constantMaps)
  else:
    disp.addReferences(renderer, ref, constantMaps)

  if data is not None: 
    ref.setData(data)
  else:
    print "added Data is None"

  return ref
  

# set the size of points for point-type plots
def setPointSize(display, size):
  display.getGraphicsModeControl().setPointSize(size)
  
  
# define the aspects of width and height, as a ratio: width/height
def setAspectRatio(display, ratio):
  x = 1.
  y = 1.
  if ratio > 1:
    x = 1.
    y = 1. / ratio
  else:
    y = 1.
    x = 1. * ratio
  setAspects(display, x, y, 1.)

# define the relative sizes of the axes
def setAspects(display, x, y, z):
  display.getProjectionControl().setAspectCartesian( (x, y, z))

# a simple method for making the VisAD "box" 95% of the window size
def maximizeBox(display, clip=1):
  setBoxSize(display, .95,clip)

# a simple method for making the VisAD "box" some % of the window size
def setBoxSize(display, percent=.70, clip=1):
  pc=display.getProjectionControl()
  pcMatrix=pc.getMatrix()
  if len(pcMatrix) > 10:
    pcMatrix[0]=percent
    pcMatrix[5]=percent
    pcMatrix[10]=percent
  else:
    pcMatrix[0]=percent/.64
    pcMatrix[3]=-percent/.64
    
  pc.setMatrix(pcMatrix)
  if clip & ok3d:
    try:
       dr = display.getDisplayRenderer();
       if isinstance(dr, DisplayRendererJ3D):
         dr.setClip(0, 1,  1.001,  0.0,  0.0, -1.001);
         dr.setClip(1, 1, -1.001,  0.0,  0.0, -1.001);
         dr.setClip(2, 1,  0.0,  1.001,  0.0, -1.001);
         dr.setClip(3, 1,  0.0, -1.001,  0.0, -1.001);
         dr.setClip(4, 1,  0.0,  0.0,  1.001, -1.001);
         dr.setClip(5, 1,  0.0,  0.0, -1.001, -1.001);
    except:
       pass
       

def makeCube(display):
  display.getGraphicsModeControl().setProjectionPolicy(0)

# return the x,y,z scalar maps for the display
def getDisplayMaps(display, includeShapes=0):

  if type(display) == StringType:
    d = BasicSSCell.getSSCellByName(display)
    disp = d.getDisplay()
    maps = d.getMaps()

  elif isinstance(display, DisplayImpl):
    maps = display.getMapVector()
    disp = display

  else:
    maps = None
    disp = None

  x = y = z = shape = None
  if maps == None:
    x = RealType.getRealTypeByName("x")
    y = RealType.getRealTypeByName("y")
    z = RealType.getRealTypeByName("z")
    shape = RealType.getRealTypeByName("py_shape_type")
  # if no maps, make them...
  else:
    for m in maps:
      if m.getDisplayScalar().toString() == "DisplayXAxis":
        x = m.getScalar()
      if m.getDisplayScalar().toString() == "DisplayYAxis":
        y = m.getScalar()
      if m.getDisplayScalar().toString() == "DisplayZAxis":
        z = m.getScalar()
      if m.getDisplayScalar().toString() == "DisplayShape":
        shape = m.getScalar()
  
  if includeShapes:
    return [x,y,z,disp,shape]
  else:
    return [x,y,z,disp]


# make a 2D or 3D line, return a reference so it can be changed
def makeLine(domainType, points):
  return Gridded2DSet(RealTupleType(domainType), points, len(points[0]))


def makeColorMap(color):

  # see if color should be handled
  if color is not None:
    from java.awt import Color
    if isinstance(color,Color):
      awtColor = color
    else:
      exec 'awtColor=Color.'+color

    red = float(awtColor.getRed())/255.
    green = float(awtColor.getGreen())/255.
    blue = float(awtColor.getBlue())/255.

  # or just make it white
  else:
    red=1.0
    green=1.0
    blue=1.0

  constmap = ( ConstantMap(red,Display.Red), ConstantMap(green,Display.Green),
         ConstantMap(blue,Display.Blue) )

  return constmap

# draw a line directly into the display; also return reference
# drawLine(display, domainType, points[], color=Color, mathtype=domainType)
# drawLine(name|display, points[], color=Color)
# "Color" is java.awt.Color
def drawLine(display, points, color=None, mathtype=None):

  constmap = makeColorMap(color)

  # drawLine(display, domainType, points[])
  maps = None
  if mathtype is not None:
    if len(points) == 2:
      lineseg = Gridded2DSet(RealTupleType(mathtype), points, len(points[0]))
    else:
      lineseg = Gridded3DSet(RealTupleType(mathtype), points, len(points[0]))

    linref = addData("linesegment", lineseg, display, constmap)
    return linref

  # drawLine(name|display, points[])
  else:
    x , y , z , disp = getDisplayMaps(display)

    if len(points) == 2:
      dom = RealTupleType(x,y)
      lineseg = Gridded2DSet(dom, points, len(points[0]))
    else:
      dom = RealTupleType(x,y,z)
      lineseg = Gridded3DSet(dom, points, len(points[0]))

    linref = addData("linesegment", lineseg, disp, constmap)
    return linref 

# draw a string on the display
def drawString(display, string, point, color=None, center=0, font="futural",
                 start=[0.,0.,0.], base=[.1,0.,0.], up=[0.,.1,0.],size=None ):

  textfs = textShape(string, center, font, start, base, up, size)
  i = py_shapes.addShape(None, color=color, shape=textfs)
  py_shapes.moveShape(i, point)

  return py_shapes 
  
# add an array of ScalarMaps to a Display
def addMaps(display, maps):

  for map in maps:
    display.addMap(map)

# define ScalarMap(s) given pairs of (Type, name)
# where "name" is taken from the list, below.
def makeMaps(*a):
  dis = ("x","y","z","lat","lon","rad","list","red","green",
  "blue","rgb","rgba","hue","saturation","value","hsv","cyan",
  "magenta","yellow","cmy","alpha","animation","selectvalue",
  "selectrange","contour","flow1x","flow1y","flow1z",
  "flow2x","flow2y","flow2z","xoffset","yoffset","zoffset",
  "shape","text","shapescale","linewidth","pointsize",
  "cylradius","cylazimuth","cylzaxis",
  "flow1elev","flow1azimuth","flow1radial",
  "flow2elev","flow2azimuth","flow2radial")
# note this list is in the same order as Display.DisplayRealArray! 

  maps=[]
  for i in xrange(0,len(a),2):
    got = -1 

    for k in xrange(len(dis)):
      if dis[k] == a[i+1] : got=k

    if got != -1:
      if type(a[i]) == StringType:
        rt = RealType.getRealType(a[i])
        maps.append(ScalarMap(RealType.getRealType(a[i]),
                                    Display.DisplayRealArray[got]))
      else:
        maps.append(ScalarMap(a[i], Display.DisplayRealArray[got]))
    else:
      print "While making mappings, cannot match: ",a[i+1]

  return maps


# quick display of a Display object in a separate JFrame
# you can set the size and title, if you want...
def showDisplay(display, width=300, height=300, 
                title="VisAD Display", bottom=None, top=None,
                panel=None):
  myf = myFrame(display, width, height, title, bottom, top, panel)

class myFrame:

  def desty(self, event):
    self.display.destroy()
    self.frame.dispose()

  def __init__(self, display, width, height, title, bottom, top, panel):
    from javax.swing import JFrame, JPanel
    from java.awt import BorderLayout, Dimension
    self.display = display

    autoShow = 0
    if panel==None:
      self.frame = JFrame(title, windowClosing=self.desty)
      self.pane = self.frame.getContentPane()
      autoShow = 1
    elif isinstance(panel, JFrame):
      self.pane = panel.getContentPane()
    else:
      self.pane = panel
      self.pane.setLayout(BorderLayout())

    self.display.getComponent().setPreferredSize(Dimension(width,height))
    self.pane.add(self.display.getComponent(), BorderLayout.CENTER)
    if bottom != None: 
      self.pb = JPanel(BorderLayout())
      self.pb.add(bottom)
      self.pane.add(self.pb, BorderLayout.SOUTH)
    if top != None: 
      self.pt = JPanel(BorderLayout())
      self.pt.add(top)
      self.pane.add(self.pt, BorderLayout.NORTH)

    if autoShow:
      self.frame.pack()
      self.frame.show()

# create a (VisADGeometryArray) shape for use with the Shape class
def textShape(string, center=0, font="futural",
                 start=[0.,0.,0.], base=[.1,0.,0.], up=[0.,.1,0.],
                 size=None ):
    
    from visad import PlotText
    from visad.util import HersheyFont
    if size != None:
      start = [0., 0., size]
      base = [size, 0., 0.]
      up = [0., size, 0.]

    return (PlotText.render_font(string, HersheyFont(font), 
                                          start, base, up, center))

# defines Shapes for a display
# note that when a Display is created (makeDisplay) a new Shapes object
# is also created, in case it is needed by drawString()

class Shapes:

  def __init__(self, display):

    self.x, self.y, self.z, self.disp = getDisplayMaps(display)
    self.doing3D = 1
    if self.z == None:
      self.doing3D = 0

    self.count = -1 
    self.shapeList = []
    self.shapeRef = []

    py_shapes = self

  # type = type of shape ('cross','triangle','square',
  #  'solid_square','solid_triangle'
  # ...or a VisADGeometryArray
  # scale = relative size for the shape
  # color = color name (e.g., "green")
  # index = the index of the shape to replace with this one
  def addShape(self, type, scale=.1, color=None, index=None):

    if isinstance(type,VisADGeometryArray): 

      self.shape = type

    else:

      if type == "cross":
        self.shape = VisADLineArray()
        self.shape.coordinates = ( scale,scale,0,  -scale, -scale, 0,
                                   scale, -scale, 0,  -scale, scale, 0 )
      elif type == "triangle":
        self.shape = VisADLineArray()
        self.shape.coordinates = ( -scale, -scale/2, 0,  scale, -scale/2, 0,  
                                    scale, -scale/2, 0,  0, scale,0,
                                    0, scale, 0, -scale, -scale/2, 0 )
      elif type == "solid_square":
        self.shape = VisADQuadArray()
        self.shape.coordinates = (scale, scale, 0, scale, -scale, 0,
                                  -scale, -scale, 0, -scale, scale, 0)
      elif type == "solid_triagle":
        self.shape = VisADTriangleArray()
        self.shape.coordinates = ( -scale, -scale/2, 0,  
                                   scale, -scale/2, 0,  0, scale, 0 )
      else:
        self.shape = VisADLineArray()
        self.shape.coordinates = ( scale, scale, 0,  scale, -scale, 0,
                                   scale, -scale, 0,  -scale, -scale, 0,
                                    -scale, -scale,0,  -scale, scale, 0,
                                    -scale, scale, 0,  scale, scale, 0)

      self.shape.vertexCount = len(self.shape.coordinates)/3


    shape_control = py_shape_map.getControl()

    if index != None:
      shape_control.setShape(index, self.shape)
      return (index)

    self.count = self.count + 1
    self.shapeList.append(self.shape)

    shape_control.setShapeSet(Integer1DSet(self.count+1))
    shape_control.setShapes( self.shapeList )

    if self.doing3D:
      self.shape_coord = RealTupleType(self.x,self.y,self.z,py_shape_type)
      shapeLoc = RealTuple(self.shape_coord, (0., 0., 0., self.count))
    else:
      self.shape_coord = RealTupleType(self.x,self.y,py_shape_type)
      shapeLoc = RealTuple(self.shape_coord, (0., 0., self.count))

    constmap = makeColorMap(color)
    self.shapeRef.append(addData("shape", shapeLoc, self.disp, constmap))

    return (self.count)

  # move the shape to a new location
  # inx = the shape index to move
  # coordinates = a list or tuple of the coordinates (in the
  #   same order as returned by 'getDisplayMaps()'
  def moveShape(self, inx, coordinates):
    coord = list(coordinates)
    coord.append(inx)
    shapeLoc = RealTuple(self.shape_coord, coord)
    self.shapeRef[inx].setData(shapeLoc)

