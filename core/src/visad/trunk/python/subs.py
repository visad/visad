"""
A collection of support methods for connecting VisAD to Jython.  The
emphasis is on display-side methods and classes.

"""

try: # try/except for pydoc
  from visad import ScalarMap, Display, DataReferenceImpl, RealTupleType,\
          Gridded2DSet, Gridded3DSet, DisplayImpl, RealType, RealTuple, \
          VisADLineArray, VisADQuadArray, VisADTriangleArray, \
          VisADGeometryArray, ConstantMap, Integer1DSet, FunctionType, \
          ScalarMap, Display, Integer1DSet, FieldImpl, CellImpl, \
          DisplayListener, DisplayEvent
          

  from types import StringType
  from visad.ss import BasicSSCell
  from visad.java2d import DisplayImplJ2D, DisplayRendererJ2D
  from visad.bom import RubberBandBoxRendererJ3D
  from java.lang import Double

  # define private fields for certains types and scalar mappings
  __py_text_type = RealType.getRealType("py_text_type")
  __py_shape_type = RealType.getRealType("py_shape_type")
  __py_shapes = None 
  __pcMatrix = None
except:
  pass

# try to get 3D
__ok3d = 1
try:
  from visad.java3d import DisplayImplJ3D, TwoDDisplayRendererJ3D, DisplayRendererJ3D,MouseBehaviorJ3D
except:
  __ok3d = 0


def makeDisplay3D(maps):
  """
  Create (and return) a VisAD DisplayImplJ3D and add the ScalarMaps
  <maps>, if any.  The VisAD box is resized to about 95% of the window.
  This returns the Display.
  """
  
  global __py_shapes
  disp = DisplayImplJ3D("Jython3D")
  __py_text_map = ScalarMap(__py_text_type, Display.Text)
  disp.addMap(__py_text_map)
  __py_shape_map = ScalarMap(__py_shape_type, Display.Shape)
  disp.addMap(__py_shape_map)

  if maps != None:  addMaps(disp, maps)
  __py_shapes = Shapes(disp, __py_shape_map)
  return disp

def makeDisplay2D(maps):
  """
  Create (and return) a VisAD DisplayImplJ2D and add the ScalarMaps
  <maps>, if any.  The VisAD box is resized to about 95% of the window.
  This returns the Display.
  """
  
  global __py_shapes
  disp = DisplayImplJ2D("Jython2D")
  __py_text_map = ScalarMap(__py_text_type, Display.Text)
  disp.addMap(__py_text_map)
  __py_shape_map = ScalarMap(__py_shape_type, Display.Shape)
  disp.addMap(__py_shape_map)

  print "Using 2D"
  if maps != None:  addMaps(disp, maps)
  __py_shapes = Shapes(disp, __py_shape_map)
  return disp

# create (and return) a VisAD DisplayImpl and add the ScalarMaps, if any
# the VisAD box is resized to about 95% of the window
def makeDisplay(maps):
  """
  Create (and return) a VisAD DisplayImpl and add the ScalarMaps
  <maps>, if any.  The VisAD box is resized to about 95% of the window.  
  Use 3D if availble, otherwise use 2D.  This returns the Display.
  """

  global __py_shapes
  is3d = 0
  if maps == None:
    is3d = 1
  else:
    for m in maps:
      if m.getDisplayScalar().toString() == "DisplayZAxis": is3d = 1

  if is3d == 1 and __ok3d == 1:
    disp = makeDisplay3D(maps)
  else:
    if __ok3d:
      tdr = TwoDDisplayRendererJ3D() 
      disp = DisplayImplJ3D("Jython3D",tdr)
      addMaps(disp, maps)
      __py_text_map = ScalarMap(__py_text_type, Display.Text)
      disp.addMap(__py_text_map)
      __py_shape_map = ScalarMap(__py_shape_type, Display.Shape)
      disp.addMap(__py_shape_map)
      __py_shapes = Shapes(disp, __py_shape_map)
    else:
      disp =  makeDisplay2D(maps)
  
  return disp


def saveDisplay(disp, filename):
  """
  Save the display <disp> as a JPEG, given the filename to use.
  """
  from visad.util import Util
  Util.captureDisplay(disp, filename)


def addData(name, data, disp, constantMaps=None, renderer=None, ref=None):
  """
  Add <data> to the display <disp>.  Use a reference <name>.
  If there are ConstantMaps, also add them.  If there is a non-default
  Renderer, use it as well.  Finally, you can supply a pre-defined
  DataReference as <ref>.

  Returns the DataReference
  """

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
  

def setPointSize(display, size):
  """
  Set the size of points (1,2,3...) to use in the <display>.
  """
  display.getGraphicsModeControl().setPointSize(size)
  
  
# define the aspects of width and height, as a ratio: width/height
def setAspectRatio(display, ratio):
  """
  Set the aspect <ratio> for the <display>.  The ratio is
  expressed as the fractional value for: width/height.
  """
  x = 1.
  y = 1.
  if ratio > 1:
    x = 1.
    y = 1. / ratio
  else:
    y = 1.
    x = 1. * ratio
  setAspects(display, x, y, 1.)


def setAspects(display, x, y, z):
  """
  Set the relative sizes of each axis in the <display>.
  """
  display.getProjectionControl().setAspectCartesian( (x, y, z))

def rotateBox(display, azimuth, declination):
  """
  Rotate the 3D display box to the azimuth angle (0-360) and
  declination angle (all in degrees).  Code from Unidata.
  """
  zangle = 180 - azimuth
  aziMat = display.make_matrix(0, 0, zangle, 1, 0, 0, 0)
  pc = display.getProjectionControl()
  comb = display.multiply_matrix(aziMat, pc.getMatrix())
  decMat = display.make_matrix(declination, 0, 0, 1, 0, 0, 0)
  comb2 = display.multiply_matrix(decMat,comb)
  pc.setMatrix(comb2)

def maximizeBox(display, clip=1):
  """
  Set the size of the VisAD 'box' for the <display> to 95%.  If
  <clip> is true, the display will be clipped at the border of the
  box; otherwise, data displays may spill over.
  """
  setBoxSize(display, .95, clip)

def setBoxSize(display, percent=.70, clip=1, showBox=1, snap=0):
  """
  Set the size of the VisAD 'box' for the <display> as a percentage
  (fraction). The default is .70 (70%).   If <clip> is true, the
  display will be clipped at the border of the box; otherwise, data
  displays may spill over.  If <showBox> is true, the wire-frame will
  be shown; otherwise, it will be turned off.  If <snap> is true, the
  box will be reoriented to an upright position.
  """

  global __pcMatrix
  pc=display.getProjectionControl()
  if (not snap) or (__pcMatrix == None): 
    __pcMatrix=pc.getMatrix()
    
  if len(__pcMatrix) > 10:
    __pcMatrix[0]=percent
    __pcMatrix[5]=percent
    __pcMatrix[10]=percent
  else:
    __pcMatrix[0]=percent/.64
    __pcMatrix[3]=-percent/.64
    
  pc.setMatrix(__pcMatrix)
  dr = display.getDisplayRenderer();

  if clip & __ok3d:
    try:
       if isinstance(dr, DisplayRendererJ3D):
         dr.setClip(0, 1,  1.,  0.0,  0.0, -1.);
         dr.setClip(1, 1, -1.,  0.0,  0.0, -1.);
         dr.setClip(2, 1,  0.0,  1.,  0.0, -1.);
         dr.setClip(3, 1,  0.0, -1.,  0.0, -1.);
         #dr.setClip(4, 1,  0.0,  0.0,  1., -1.);
         #dr.setClip(5, 1,  0.0,  0.0, -1., -1.);
       elif isinstance(dr, DisplayRendererJ2D):
         dr.setClip(-1., 1., -1., 1.)
    except:
       pass

  dr.setBoxOn(showBox)

def makeCube(display):
  """
  Turn the VisAD box for this <display> into a cube with no
  perspective (no 'vanishing point'.  Useful for aligning
  vertically-stacked data.  
  """

  display.getGraphicsModeControl().setProjectionPolicy(0)

def zoomBox(display, factor):
  """
  Zoom the display by 'factor' (1.0 does nothing...). Related:
  setBoxSize().
  """
  mouseBehavior = display.getMouseBehavior()
  pc = display.getProjectionControl()
  currentMatrix = pc.getMatrix()
  scaleMatrix = mouseBehavior.make_matrix( 0, 0, 0, factor, 0, 0, 0)
  scaleMatrix = mouseBehavior.multiply_matrix( scaleMatrix, currentMatrix);
  try:
    pc.setMatrix(scaleMatrix)
  except:
    pass

def enableRubberBandBoxZoomer(display,useKey):
  """
  Method to attach a Rubber Band Box zoomer to this
  display.  Once attached, it is there foreever!  The useKey
  parameter can be 0 (no key), 1(CTRL), 2(SHIFT)
  """
  RubberBandZoomer(display,useKey)

def getDisplayScalarMaps(display, includeShapes=0):
  """
  Return a list of the scalarmaps mappings for the <display>. The list
  elements are ordered: x,y,z,display.  If <includeShapes> is
  true, then mappings for Shape will be appended.  The <display>
  may be a Display, or the name of a 'plot()' window.
  """

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
  if maps != None:
    for m in maps:
      if m.getDisplayScalar().toString() == "DisplayXAxis":
        x = m
      if m.getDisplayScalar().toString() == "DisplayYAxis":
        y = m
      if m.getDisplayScalar().toString() == "DisplayZAxis":
        z = m
      if m.getDisplayScalar().toString() == "DisplayShape":
        shape = m
  
  if includeShapes:
    return [x,y,z,disp,shape]
  else:
    return [x,y,z,disp]


def getDisplayMaps(display, includeShapes=0):
  """
  Return a list of the type mappings for the <display>. The list
  elements are ordered: x,y,z,display.  If <includeShapes> is
  true, then mappings for Shape will be appended.  The <display>
  may be a Display, or the name of a 'plot()' window.
  """

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
  """
  returns a set of <points>, as defined in the <domainType>. For
  example, if <domaintType> defines a (Latitude,Longitude), then
  the <points> are in Latitude,Longitude.
  """

  return Gridded2DSet(RealTupleType(domainType), points, len(points[0]))


# make ConstantMaps for line style and width
def makeLineStyleMap(style, width):
  """
  Make a ConstantMap for the indicated line <style>, which
  may be: "dash", "dot", "dashdot", or "solid" (default). The
  <width> is the line width in pixels.  Used by drawLine.
  """

  constmap = None
  constyle = None
  if style is not None:
    from visad import GraphicsModeControl

    if style == "dash":
      constyle = ConstantMap(GraphicsModeControl.DASH_STYLE,Display.LineStyle)
    elif style == "dot":
      constyle = ConstantMap(GraphicsModeControl.DOT_STYLE,Display.LineStyle)
    elif style == "dashdot":
      constyle = ConstantMap(GraphicsModeControl.DASH_DOT_STYLE,Display.LineStyle)
    else:
      constyle = ConstantMap(GraphicsModeControl.SOLID_STYLE, Display.LineStyle)

    constmap = constyle

  if width is not None:
    constwid = ConstantMap(width, Display.LineWidth)
    if constyle is not None:
      constmap = [constyle, constwid]
    else:
      constmap = constwid
    
  return constmap

# make ConstantMap list for color

def makeColorMap(color):
  """
  Return a ConstantMap of <color>, given by name (e.g., "red")
  or a java.awt.Color object.  Default is white. Used by numerous methods.
  """

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

  constmap = [ ConstantMap(red,Display.Red), ConstantMap(green,Display.Green),
         ConstantMap(blue,Display.Blue) ] 

  return constmap

# draw a line directly into the display; also return reference
# drawLine(display, domainType, points[], color=Color, mathtype=domainType)
# drawLine(name|display, points[], color=Color)
# "Color" is java.awt.Color
def drawLine(display, points, color=None, mathtype=None, style=None, width=None):
  """
  Draw lines on the <display>.  <points> is a 2 or 3 dimensional,
  list/tuple of points to connect as a line, ordered as given in the
  <mathtype>.  Default <mathtype> is whatever is mapped to the x,y
  (and maybe z) axis.  <color> is the line color ("red" or
  java.awt.Color; default=white), <style> is the line style (e.g.,
  "dash"), and <width> is the line width in pixels.

  Return a reference to this line.
  """

  constmap = makeColorMap(color)
  constyle = makeLineStyleMap(style, width)
  if constyle is not None:
    constmap.append(constyle)
    
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

  """
  Draw a string of characters on the <display>.  <string> is the
  string of characters.  <point> is the starting location for the
  string drawing, expressed as a list/tuple of 2 or 3 values (x,y,z).
  <color> is the color (e.g., "red" or java.awt.Color; default =
  white).  <center> if true will center the string.  <font> defiles
  the HersheyFont name to use.  <start> defines the relative location
  of the starting point (x,y,z; default = 0,0,0).  <base> defines the
  direction that the base of the string should point (x,y,z; default=
  along x-axis).  <up> defines the direction of the vertical stroke
  for each character (default = along y-axis).  <size> is the relative
  size.
  """

  textfs = textShape(string, center, font, start, base, up, size)
  i = __py_shapes.addShape(textfs, color=color)
  __py_shapes.moveShape(i, point)

  return __py_shapes 
  
def addMaps(display, maps):
  """
  Add list/tuple <maps> mappings to the <display>.  These determine what
  scalars will appear along what axes. See makeMaps, below.
  """

  for map in maps:
    display.addMap(map)

# define ScalarMap(s) given pairs of (Type, name)
# where "name" is taken from the list, below.
def makeMaps(*a):
  """
  Define a list of scalar mappings for each axis and any
  other one needed.  The parameter list is in pairs:
  Type, Name.  For example: makeMaps("lat","y", "lon","x")
  returns a list that maps variable "lat" to the y-axis, and
  variable "lon" to the x-axis.

  Here is a complete list of available names:

  "x","y","z","lat","lon","rad","list","red","green",
  "blue","rgb","rgba","hue","saturation","value","hsv","cyan",
  "magenta","yellow","cmy","alpha","animation","selectvalue",
  "selectrange","contour","flow1x","flow1y","flow1z",
  "flow2x","flow2y","flow2z","xoffset","yoffset","zoffset",
  "shape","text","shapescale","linewidth","pointsize",
  "cylradius","cylazimuth","cylzaxis",
  "flow1elev","flow1azimuth","flow1radial",
  "flow2elev","flow2azimuth","flow2radial","linestyle",
  "textureenable"

  """

  dis = ("x","y","z","lat","lon","rad","list","red","green",
  "blue","rgb","rgba","hue","saturation","value","hsv","cyan",
  "magenta","yellow","cmy","alpha","animation","selectvalue",
  "selectrange","contour","flow1x","flow1y","flow1z",
  "flow2x","flow2y","flow2z","xoffset","yoffset","zoffset",
  "shape","text","shapescale","linewidth","pointsize",
  "cylradius","cylazimuth","cylzaxis",
  "flow1elev","flow1azimuth","flow1radial",
  "flow2elev","flow2azimuth","flow2radial","linestyle",
  "textureenable")

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
                panel=None, right=None, left=None):
  """
  Quick display of <display> in a separate frame. <width> and
  <height> give the dimensions of the window; <title> is the
  text string for the titlebar, <bottom> is a panel to put
  below the <display>, <top> is a panel to put above the
  <display>, and <panel> is the panel to put everything into (default
  is to make a new one, and display it).  Additionally, you may put
  panels on the <right> and <left>.  
  """

  myf = myFrame(display, width, height, title, bottom, top, right, left, panel)
  return myf


class myFrame:
  """
  Creates a frame out of the display, with possible optional panels
  (JPanels) to add to the "top", "bottom", "left", "right". The
  'display' will be added to the 'center' of this panel.  If 'panel'
  is None, a new JFrame is created, and will be shown; otherwise,
  the contents are just put into the JPanel 'panel'.
  """

  def destroy(self, event):
    self.desty(event)

  def desty(self, event):
    self.display.destroy()
    self.frame.dispose()

  def __init__(self, display, width, height, title, 
                         bottom, top, right, left, panel):

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
    if right != None: 
      self.pt = JPanel(BorderLayout())
      self.pt.add(right)
      self.pane.add(self.pt, BorderLayout.EAST)
    if left != None: 
      self.pt = JPanel(BorderLayout())
      self.pt.add(left)
      self.pane.add(self.pt, BorderLayout.WEST)

    if autoShow:
      self.frame.pack()
      self.frame.show()


def textShape(string, center=0, font="futural",
                 start=[0.,0.,0.], base=[.1,0.,0.], up=[0.,.1,0.],
                 size=None ):
    
    """
    Creates a Shape for the text string given.  For use with the Shape
    class.  See comments on drawString, above.
    """

    from visad import PlotText
    from visad.util import HersheyFont
    if size != None:
      start = [0., 0., size]
      base = [size, 0., 0.]
      up = [0., size, 0.]

    return (PlotText.render_font(string, HersheyFont(font), 
                                          start, base, up, center))

# local shadow methods for addShape and moveShape
def addShape(type, scale=.1, color=None, index=None, autoScale=1):
  """
  Simply a shadow method for addShape in case the user has not
  made their own.
  """
  return (__py_shapes.addShape(type, scale, color, index, autoScale))


def moveShape(index, coord):
  """
  Simply a shadow method for moveShape in case the user has not
  made their own.
  """
  __py_shapes.moveShape(index, coord)


class Shapes:
  """
  Helper class for handling Shapes within a display.
  """

  def __init__(self, display, shapemap):
    """
    <display> is the display this is working with; <shapemap>
    is the mapping parameter for shapes.
    """

    self.x, self.y, self.z, self.disp = getDisplayMaps(display)
    self.doing3D = 1
    if self.z == None:
      self.doing3D = 0

    self.count = -1 
    self.shapeList = []
    self.shapeRef = []
    self.shapeMap = shapemap
    self.shapeType = shapemap.getScalar()

  def addShape(self, type, scale=.1, color=None, index=None, autoScale=1):
    """
    Add a shape.  Parameters: <type> of shape ('cross','triangle','square',
    'solid_square','solid_triangle', or a VisADGeometryArray.  <scale> = 
    relative size for the shape.  <color> = color name (e.g., "green") 
    <index> = the index of the shape to replace with this one.  If
    autoScale is true, the shape will be scaled.

    Returns an index to this shape. (For use with moveShape.)

    """

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
      elif type == "cube":
        self.shape = VisADQuadArray()
        self.shape.coordinates = (scale, scale, -scale,  scale, -scale, -scale,
                   scale, -scale, -scale,    -scale, -scale, -scale,
                  -scale, -scale, -scale,    -scale,  scale, -scale,
                  -scale,  scale, -scale,     scale,  scale, -scale,

                   scale,  scale,  scale,     scale, -scale,  scale,
                   scale, -scale,  scale,    -scale, -scale,  scale,
                  -scale, -scale,  scale,    -scale,  scale,  scale,
                  -scale,  scale,  scale,     scale,  scale,  scale,

                   scale,  scale,  scale,     scale,  scale, -scale,
                   scale,  scale, -scale,     scale, -scale, -scale,
                   scale, -scale, -scale,     scale, -scale,  scale,
                   scale, -scale,  scale,     scale,  scale,  scale,

                  -scale,  scale,  scale,    -scale,  scale, -scale,
                  -scale,  scale, -scale,    -scale, -scale, -scale,
                  -scale, -scale, -scale,    -scale, -scale,  scale,
                  -scale, -scale,  scale,    -scale,  scale,  scale,

                   scale,  scale,  scale,     scale,  scale, -scale,
                   scale,  scale, -scale,    -scale,  scale, -scale,
                  -scale,  scale, -scale,    -scale,  scale,  scale,
                  -scale,  scale,  scale,     scale,  scale,  scale,

                   scale, -scale,  scale,     scale, -scale, -scale,
                   scale, -scale, -scale,    -scale, -scale, -scale,
                  -scale, -scale, -scale,    -scale, -scale,  scale,
                  -scale, -scale,  scale,     scale, -scale,  scale)

      elif type == "solid_triangle":
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


    shape_control = self.shapeMap.getControl()

    if index != None:
      shape_control.setShape(index, self.shape)
      return (index)

    self.count = self.count + 1
    self.shapeList.append(self.shape)

    shape_control.setShapeSet(Integer1DSet(self.count+1))
    shape_control.setShapes( self.shapeList )
    if autoScale:  
      shape_control.setAutoScale(1)

    if self.doing3D:
      self.shape_coord = RealTupleType(self.x,self.y,self.z,self.shapeType)
      shapeLoc = RealTuple(self.shape_coord, (0., 0., 0., self.count))
    else:
      self.shape_coord = RealTupleType(self.x,self.y,self.shapeType)
      shapeLoc = RealTuple(self.shape_coord, (0., 0., self.count))

    constmap = makeColorMap(color)
    self.shapeRef.append(addData("shape", shapeLoc, self.disp, constmap))

    return (self.count)

  def moveShape(self, inx, coordinates):
    """
    Move the shape pointed to by <inx>, to the <coordinates> which
    is a list of values in the same order as returned by
    getDisplayMaps.
    """
    coord = list(coordinates)
    coord.append(inx)
    shapeLoc = RealTuple(self.shape_coord, coord)
    self.shapeRef[inx].setData(shapeLoc)

# SelectField aids in showing a series of data objects
# using the Display.SelectValue.  'data' should be
# an array of data with same MathTypes
class SelectField:
  """
  Aids in showing a series of data objects using Display.SelectValue.
  """

  def __init__(this, selectMapName, data):
    """
    <selectMapName> is the name of the type to be used for this
    select map.  <data> is an array/list of data with identical
    types (MathTypes).
    """
    selectMap = RealType.getRealType(selectMapName)
    this.selectScalarMap = ScalarMap(selectMap, Display.SelectValue)
    selectType = FunctionType(selectMap, data[0].getType())
    selectSet = Integer1DSet(selectMap, len(data))
    this.selectField = FieldImpl(selectType, selectSet)
    for i in xrange(len(data)):
      this.selectField.setSample(i, data[i])
    this.selectedIndex = -1

  def showIt(this, index):
    """
    Show only field <index> from this group.
    """
    if this.selectedIndex == -1:
      this.control = this.selectScalarMap.getControl()
    if index != this.selectedIndex:
      this.control.setValue(index)
      this.selectedIndex = index

  def getScalarMap(this):
    """
    Return the ScalarMap to add to the display for this Select
    """
    return this.selectScalarMap

  def getSelectField(this):
    """
    Return the Field to add() to a display.
    """
    return this.selectField

class RubberBandZoomer:
  """
  Class to define a Rubber Band Box zoom capability
  for a display.  Once invoked, a drag with right mouse
  button creates a RubberBandBox.  When released, the
  image is moved and zoomed to fill the window.
  """

  def __init__(self,display, requireKey):
    """
    display is the display object.  requireKey = 0 (no key),
    = 1 (CTRL), = 2 (SHIFT)
    """
    self.x, self.y, self.z, self.display = getDisplayMaps(display)
    self.xy = RealTupleType(self.x, self.y)
    self.dummy_set = Gridded2DSet(self.xy,None,1)
    mask = 0
    from java.awt.event import InputEvent
    if requireKey == 1: 
      mask = InputEvent.CTRL_MASK
    elif requireKey == 2:
      mask = InputEvent.SHIFT_MASK

    self.rbb = RubberBandBoxRendererJ3D(self.x, self.y, mask, mask)
    self.ref = addData('rbb',self.dummy_set,self.display, renderer=self.rbb)
    
    class MyCell(CellImpl):
      def __init__(self, ref, disp):
        self.ref = ref
        self.display = disp
        self.xm, self.ym, self.zm, self.d = getDisplayScalarMaps(disp)
        self.dr = self.display.getDisplayRenderer()
        self.can = self.dr.getCanvas()
        self.mb = MouseBehaviorJ3D(self.dr)
        self.pc = self.display.getProjectionControl()

      def doAction(self):

        # get the 'box' coordinates of the RB box
        set = self.ref.getData()
        samples = set.getSamples()
        if samples is None: return  # no sense in going further
        xsv = self.xm.scaleValues((samples[0][0],samples[0][1]))
        ysv = self.ym.scaleValues((samples[1][0],samples[1][1]))
        xsc = (xsv[0] + xsv[1]) / 2.0
        ysc = (ysv[0] + ysv[1]) / 2.0

        # compute the 'box' coordinates of the corners of Canvas
        canvasDim = self.can.getSize()
        sc = self.mb.findRay( 0, 0 )
        a = sc.position[2]/sc.vector[2]
        x0 = sc.position[0] - a*sc.vector[0]
        y0 = sc.position[1] - a*sc.vector[1]

        sc = self.mb.findRay( canvasDim.width, canvasDim.height )
        a = sc.position[2]/sc.vector[2]
        x1 = sc.position[0] - a*sc.vector[0]
        y1 = sc.position[1] - a*sc.vector[1]

        # center point of the canvas
        xc = (x0 + x1)/2
        yc = (y0 + y1)/2

        # do the matrix changes.....
        mat = self.pc.getMatrix()

        # compute matrix to translate 
        tm = self.mb.make_matrix(0,0,0,1,(xc - xsc),(yc - ysc),0)
        tsm = self.mb.multiply_matrix( mat, tm)

        try:
          # compute the ratio if possible
          ratio = min(abs((x1 - x0)/(xsv[0]-xsv[1])) , 
                      abs((y1 - y0)/(ysv[0]-ysv[1])))

          ts = self.mb.make_matrix(0,0,0,ratio,0,0,0)
          newm = self.mb.multiply_matrix( ts, tsm)

          # apply the stuff
          self.pc.setMatrix(newm)

        except:
          pass


    cell = MyCell(self.ref,self.display)
    cell.addReference(self.ref)

class HandlePickEvent(DisplayListener):
  """
  Helper class for interfacing to the VisAD Display when
  the user drags the mouse around with both buttons
  pressed (which causes a cursor to appear and the domain
  readout values to be shown).  When the mouse buttons
  are released, the applications 'handler' will be called.
  """

  def __init__(self, display, handler):
    """
    The 'display' is the display.  The 'handler' is the
    method in the caller's program that will be called
    when the user releases the buttons.  It must have
    two parameters: x,y that will get the values of
    the domain values for the x- and y-axis, respectively.
    """
    self.x, self.y, self.z, self.display=getDisplayMaps(display)
    self.dr = self.display.getDisplayRenderer()
    self.display.addDisplayListener(self)
    self.handler = handler

  def displayChanged(self, event):
    """
    Handle event
    """
    # first, confirm this is only a drag, with no other key down
    try:
      ie = event.getInputEvent()
      if ie.isControlDown(): return
      if ie.isShiftDown(): return
    except:
      pass

    # multiple MOUSE_RELEASE type events will happen,
    # but only one has values...

    if event.getId() == DisplayEvent.MOUSE_RELEASED:
      self.xx = self.dr.getDirectAxisValue(self.x)
      self.yy = self.dr.getDirectAxisValue(self.y)
      if not Double(self.xx).isNaN():  # there will be NaN values
        self.display.disableAction()
        self.handler(self.xx, self.yy)  # to get a new image
        self.display.enableAction()

