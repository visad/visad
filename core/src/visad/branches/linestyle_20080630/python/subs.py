"""
A collection of support methods for connecting VisAD to Jython.  The
emphasis is on display-side methods and classes.  All display-dependent
functions (methods) are available as either static functions 
(where one of the parameter is the 'display') or as an instance
function (for a previously-created 'display').  See the '_vdisp' class.

"""

try:  #try/except done to remove these lines from documentation only

  from visad import ScalarMap, Display, DataReferenceImpl, RealTupleType,\
          Gridded2DSet, Gridded3DSet, DisplayImpl, RealType, RealTuple, \
          VisADLineArray, VisADQuadArray, VisADTriangleArray, SetType, \
          VisADGeometryArray, ConstantMap, Integer1DSet, FunctionType, \
          ScalarMap, Display, Integer1DSet, FieldImpl, CellImpl, \
          DisplayListener, DisplayEvent, GraphicsModeControl, \
          MouseHelper, UnionSet
          

  from types import StringType
  from visad.ss import BasicSSCell
  from visad.java2d import DisplayImplJ2D, DisplayRendererJ2D
  from java.lang import Double

  # define private fields for certains types and scalar mappings
  __py_shapes = None 
except:
  pass

# Super class for displays - this contains all the
# useful instance methods for the displays.  (Note
# the 'static' methods usually use these.)  This
# class should _only_ be instantiated indirectly using
# the makeDisplay(), makeDisplay2D(), or makeDisplay3D()
# methods!!
class _vdisp:
  instance = 0 # make sure we use unique names for each instance

  def __init__(self):
      instance =+ 1
      self.text_type = RealType.getRealType("py_text_type_"+str(instance))
      self.shape_type = RealType.getRealType("py_shape_type_"+str(instance))
      self.pcMatrix = None
      self.text_map = ScalarMap(self.text_type, Display.Text)
      self.addMap(self.text_map)
      self.shape_map = ScalarMap(self.shape_type, Display.Shape)
      self.addMap(self.shape_map)
      self.my_frame = None

  def addMaps(self, maps):
    """
    Add list/tuple <maps> mappings to the <display>.  These determine what
    scalars will appear along what axes. See makeMaps, below.
    """

    for map in maps:
      self.addMap(map)


  def addShape(self, type, scale=.1, color=None, index=None, autoScale=1):
    """
    Add a shape for this display.  
    Parameters: <type> of shape ('cross','triangle','square',
    'solid_square','solid_triangle', or a VisADGeometryArray.  <scale> = 
    relative size for the shape.  <color> = color name (e.g., "green") 
    <index> = the index of the shape to replace with this one.  If
    autoScale is true, the shape will be scaled.

    Returns an index to this shape. (For use with moveShape.)

    """
    return (self.shapes.addShape(type, scale, color, index, autoScale))


  def moveShape(self, index, coord):
    """
    Move the shape pointed to by <inx>, to the <coordinates> which
    is a list of values in the same order as returned by
    getDisplayMaps.
    """
    self.shapes.moveShape(index, coord)


  def drawString(self, string, point, color=None, center=0, font="futural",
                   start=[0.,0.,0.], base=[.1,0.,0.], up=[0.,.1,0.],size=None ):

    """
    Draw a string of characters on this display.  <string> is the
    string of characters.  <point> is the starting location for the
    string drawing, expressed as a list/tuple of 2 or 3 values (x,y,z).
    <color> is the color (e.g., "red" or java.awt.Color; default =
    white).  <center> if true will center the string.  <font> defiles
    the HersheyFont name to use.  <start> defines the relative location
    of the starting point (x,y,z; default = 0,0,0).  <base> defines the
    direction that the base of the string should point (x,y,z; default=
    along x-axis).  <up> defines the direction of the vertical stroke
    for each character (default = along y-axis).  <size> is the relative
    size.  Returns the index to the shape of the text (useful for
    moving).
    """

    textfs = textShape(string, center, font, start, base, up, size)
    i = self.shapes.addShape(textfs, color=color, index=None)
    self.shapes.moveShape(i, point)
    return i


  def addTitle(self, string, center=1, font="timesrb", color=None, size=None, offset=.1, index=None):
    """
    Put a title onto this display.  The title is centered just 
    above the VisAD 'box'.  Use 'offset' to move it up/down.
    Set the text color with 'color', and if this is a replacement
    for a previous title, then 'index' is the value previously
    returned.

    Return the shape index for later adjustments...
    """
    ts = textShape(string,center,font,start=[1.,2.+offset,0], size=size)
    return self.shapes.addShape(ts, color=color, index=index, autoScale=0)


  def saveDisplay(self,filename, display_wait=1, file_write_wait=1):
    """
    Save the display <disp> as a JPEG, given the filename to use. This
    will wait for the display to be 'done'.
    """
    from visad.util import Util
    Util.captureDisplay(self, filename, display_wait, file_write_wait)


  def addData(self, name, data, constantMaps=None, renderer=None, ref=None, zlayer=None):
    """
    Add VisAD <data> to the display <disp>.  Use a reference <name>.
    If there are ConstantMaps, also add them.  If there is a non-default
    Renderer, use it as well.  You can supply a pre-defined
    DataReference as <ref>.  Finally, if nothing is mapped to Display.ZAxis,
    data objects with the highest <zlayer: (0,1)> will appear on top in the display.


    Returns the DataReference
    """

    if zlayer is not None:
      zmap = [ConstantMap(zlayer, Display.ZAxis)]
      if constantMaps is not None:
        constantMaps = constantMaps + zmap
      else:
        constantMaps = zmap

    if ref is None: 
      ref = DataReferenceImpl(name)
    if renderer is None:
      self.addReference(ref, constantMaps)
    else:
      self.addReferences(renderer, ref, constantMaps)

    if data is not None: 
      ref.setData(data)
    else:
      print "added Data is None"

    return ref

  def toggle(self, ref, on):
    rendVector = self.getRendererVector()
    for i in range(rendVector.size()):
      ren = rendVector.elementAt(i)
      links = ren.getLinks()
      for j in range(len(links)):
        if (links[j].getThingReference() == ref):
          links[j].getRenderer().toggle(on)
    
  def setPointSize(self, size):
    """
    Set the size of points (1,2,3...) to use in this display.
    """
    self.getGraphicsModeControl().setPointSize(size)
    
  def setAspectRatio(self, ratio):
    """
    Set the aspect <ratio> for this display.  The ratio is
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
    self.setAspects(x, y, 1.)


  def setAspects(self, x, y, z):
    """
    Set the relative sizes of each axis in this display.
    """
    self.getProjectionControl().setAspectCartesian( (x, y, z))

  def rotateBox(self, azimuth, declination):
    """
    Rotate the 3D display box to the azimuth angle (0-360) and
    declination angle (all in degrees).  Code from Unidata.
    """
    zangle = 180 - azimuth
    aziMat = self.make_matrix(0, 0, zangle, 1, 0, 0, 0)
    pc = self.getProjectionControl()
    comb = self.multiply_matrix(aziMat, pc.getMatrix())
    decMat = self.make_matrix(declination, 0, 0, 1, 0, 0, 0)
    comb2 = self.multiply_matrix(decMat,comb)
    pc.setMatrix(comb2)

  def maximizeBox(self, clip=1):
    """
    Set the size of the VisAD 'box' for the <display> to 95%.  If
    <clip> is true, the display will be clipped at the border of the
    box; otherwise, data displays may spill over.
    """
    self.setBoxSize(.95, clip)

  def setBoxSize(self, percent=.70, clip=1, showBox=1, snap=0):
    """
    Set the size of the VisAD 'box' for the <display> as a percentage
    (fraction). The default is .70 (70%).   If <clip> is true, the
    display will be clipped at the border of the box; otherwise, data
    displays may spill over.  If <showBox> is true, the wire-frame will
    be shown; otherwise, it will be turned off.  If <snap> is true, the
    box will be reoriented to an upright position.
    """
    pc=self.getProjectionControl()
    if (not snap) or (self.pcMatrix == None): 
      self.pcMatrix=pc.getMatrix()
      
    if len(self.pcMatrix) > 10:
      self.pcMatrix[0]=percent
      self.pcMatrix[5]=percent
      self.pcMatrix[10]=percent
    else:
      self.pcMatrix[0]=percent/.64
      self.pcMatrix[3]=-percent/.64
      
    pc.setMatrix(self.pcMatrix)
    dr = self.getDisplayRenderer();

    if __ok3d:
      try:
         if isinstance(dr, DisplayRendererJ3D):
           dr.setClip(0, clip,  1.,  0.0,  0.0, -1.);
           dr.setClip(1, clip, -1.,  0.0,  0.0, -1.);
           dr.setClip(2, clip,  0.0,  1.,  0.0, -1.);
           dr.setClip(3, clip,  0.0, -1.,  0.0, -1.);
           #dr.setClip(4, 1,  0.0,  0.0,  1., -1.);
           #dr.setClip(5, 1,  0.0,  0.0, -1., -1.);
         elif isinstance(dr, DisplayRendererJ2D):
           if clip: 
             dr.setClip(-1., 1., -1., 1.)
           else:
             dr.unsetClip()
      except:
         pass

    dr.setBoxOn(showBox)

  def makeCube(self):
    """
    Turn the VisAD box for this display into a cube with no
    perspective (no 'vanishing point'.  Useful for aligning
    vertically-stacked data.  
    """
    self.getGraphicsModeControl().setProjectionPolicy(0)

  def setBackgroundColor(self,c):
    """
    Set the background color to 'color' (which may be a
    java.awt.Color, or a string with the name in it (like 'green')
    """
    r,g,b = _color2rgb(c)
    self.getDisplayRenderer().getRendererControl().setBackgroundColor(r,g,b)

  def setForegroundColor(self,c):
    """
    Set the foreground color to 'color' (which may be a
    java.awt.Color, or a string with the name in it (like 'green')
    """
    r,g,b = _color2rgb(c)
    self.getDisplayRenderer().getRendererControl().setForegroundColor(r,g,b)

  def setBoxOn(self,on):
    """
    Turn the wire frame box on or off.
    """
    self.getDisplayRenderer().getRendererControl().setBoxOn(on)
     
  def setCursorColor(self,c):
    """
    Set the cursor color to 'color' (which may be a
    java.awt.Color, or a string with the name in it (like 'green')
    """
    r,g,b = _color2rgb(c)
    self.getDisplayRenderer().getRendererControl().setCursorColor(r,g,b)

  def setBoxColor(self,c):
    """
    Set the box color to 'color' (which may be a
    java.awt.Color, or a string with the name in it (like 'green')
    """
    r,g,b = _color2rgb(c)
    self.getDisplayRenderer().getRendererControl().setBoxColor(r,g,b)

  def zoomBox(self, factor):
    """
    Zoom the display by 'factor' (1.0 does nothing...). Related:
    setBoxSize().
    """
    mouseBehavior = self.getMouseBehavior()
    pc = self.getProjectionControl()
    currentMatrix = pc.getMatrix()
    scaleMatrix = mouseBehavior.make_matrix( 0, 0, 0, factor, 0, 0, 0)
    scaleMatrix = mouseBehavior.multiply_matrix( scaleMatrix, currentMatrix);
    try:
      pc.setMatrix(scaleMatrix)
    except:
      pass

  def setPointMode(size, on):
    """
    Turn on (on=true) point mode for some renderings; otherwise
    these objects may be shown as lines or texture maps.
    """
    self.getGraphicsModControl().setPointSize(size)
    self.getGraphicsModControl().setPointMode(on)

  def showAxesScales(self, on):
    """
    Turn on the axes labels for this display if 'on' is true
    """
    self.getGraphicsModeControl().setScaleEnable(on)

  def enableRubberBandBoxZoomer(self,useKey,callback=None,color=None,x=None,y=None):
    """
    Method to attach a Rubber Band Box zoomer to this
    display.  Once attached, it is there foreever!  The useKey
    parameter can be 0 (no key), 1(CTRL), 2(SHIFT)
    """
    rrbz = RubberBandZoomer(self,useKey,1,callback,color,x,y)
    return rrbz.ref

  def enableRubberBandBox(self,useKey,callback=None,color=None,x=None,y=None):
    """
    Method to attach a Rubber Band Box to this display.  Once
    attached, it is there forever!  The useKey parameter can 
    be 0 (no key), 1(CTRL), 2(SHIFT)
    """
    rrbz = RubberBandZoomer(self,useKey,0,callback,color,x,y)
    return rrbz.ref

  def getDisplayScalarMaps(self, includeShapes=0):
    """
    Return a list of the scalarmaps mappings for this display. The list
    elements are ordered: x,y,z,display.  If <includeShapes> is
    true, then mappings for Shape will be appended.  The <display>
    may be a Display, or the name of a 'plot()' window.
    """
    return (getDisplayScalarMaps(self,includeShapes))

  def getDisplayScalarMapLists(self, includeShapes=0):
    """
    Return a list of the scalarmaps mappings for this display. The list
    elements are ordered: x,y,z,display.  If <includeShapes> is
    true, then mappings for Shape will be appended.  The <display>
    may be a Display, or the name of a 'plot()' window.
    Note: this is identical to getDisplayScalarMaps except that
    the return type is a list of lists since there may more than
    one ScalarMap to x, y or z.
    """
    return (getDisplayScalarMapLists(self,includeShapes))

  def getDisplayMaps(self, includeShapes=0):
    """
    Return a list of the type mappings for the <display>. The list
    elements are ordered: x,y,z,display.  If <includeShapes> is
    true, then mappings for Shape will be appended.  The <display>
    may be a Display, or the name of a 'plot()' window.
    """
    return (getDisplayMaps(self,includeShapes))


  def moveLine(self, lref, points):
    """ 
    move the referenced line to the new points
    """
    type = lref.getType()
    lref.setData(makeLine(type,points))


  def drawLine(self, points, color=None, mathtype=None, style=None, width=None):
    """
    Draw lines on this display.  <points> is a 2 or 3 dimensional,
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

      linref = self.addData("linesegment", lineseg, constmap)
      return linref

    # drawLine(name|display, points[])
    else:
      x , y , z , disp = self.getDisplayMaps()

      if len(points) == 2:
        dom = RealTupleType(x,y)
        lineseg = Gridded2DSet(dom, points, len(points[0]))
      else:
        dom = RealTupleType(x,y,z)
        lineseg = Gridded3DSet(dom, points, len(points[0]))

      linref = self.addData("linesegment", lineseg, constmap)
      return linref


  def drawBox(self, points, color=None, mathtype=None, style=None, width=None, boxref=None):
    """
    Draw a box on this display.  <points> is a 2 dimensional, list/tuple
    of points to connect a box diagonal, ordered as given in the
    <mathtype>.  Default <mathtype> is whatever is mapped to the x,y
    (and maybe z) axis.  <color> is the line color ("red" or
    java.awt.Color; default=white), <style> is the line style (e.g.,
    "dash"), and <width> is the line width in pixels.

    Return a reference to this box.
    """

    constmap = makeColorMap(color)
    constyle = makeLineStyleMap(style, width)
    if constyle is not None:
      constmap.append(constyle)

    maps = None
    lineseg_s = []
    if mathtype is not None:
      dom = RealTupleType(mathtype)
      if len(points) == 2:
        aa = [[points[0][0], points[0][1]], [points[1][0], points[1][0]]]
        lineseg_s.append(Gridded2DSet(dom, aa, len(aa[0])))
        bb = [[aa[0][1], points[0][1]], [aa[1][1], points[1][1]]]
        lineseg_s.append(Gridded2DSet(dom, bb, len(bb[0])))
        cc = [[bb[0][1], aa[0][0]], [bb[1][1], bb[1][1]]]
        lineseg_s.append(Gridded2DSet(dom, cc, len(cc[0])))
        dd = [[cc[0][1], aa[0][0]], [cc[1][1], aa[1][0]]]
        lineseg_s.append(Gridded2DSet(dom, dd, len(dd[0])))
      else:
        lineseg = Gridded3DSet(RealTupleType(mathtype), points, len(points[0]))

      uset = UnionSet(lineseg_s)
      if boxref is not None:
        boxref.setData(uset)
      else:
        boxref = self.addData("box", uset, constmap)
      return boxref

    else:
      x , y , z , disp = self.getDisplayMaps()

      if len(points) == 2:
        dom = RealTupleType(x,y)
        aa = [[points[0][0], points[0][1]], [points[1][0], points[1][0]]]
        lineseg_s.append(Gridded2DSet(dom, aa, len(aa[0])))
        bb = [[aa[0][1], points[0][1]], [aa[1][1], points[1][1]]]
        lineseg_s.append(Gridded2DSet(dom, bb, len(bb[0])))
        cc = [[bb[0][1], aa[0][0]], [bb[1][1], bb[1][1]]]
        lineseg_s.append(Gridded2DSet(dom, cc, len(cc[0])))
        dd = [[cc[0][1], aa[0][0]], [cc[1][1], aa[1][0]]]
        lineseg_s.append(Gridded2DSet(dom, dd, len(dd[0])))
      else:
        dom = RealTupleType(x,y,z)
        lineseg = Gridded3DSet(dom, points, len(points[0]))

      uset = UnionSet(lineseg_s)
      if boxref is not None:
        boxref.setData(uset)
      else:
        boxref = self.addData("box", uset, constmap)
      return boxref


  def showDisplay(self, width=300, height=300, 
                  title="VisAD Display", bottom=None, top=None,
                  panel=None, right=None, left=None):
    """
    Quick display of this display in a separate frame. <width> and
    <height> give the dimensions of the window; <title> is the
    text string for the titlebar, <bottom> is a panel to put
    below the <display>, <top> is a panel to put above the
    <display>, and <panel> is the panel to put everything into (default
    is to make a new one, and display it).  Additionally, you may put
    panels on the <right> and <left>.  
    """

    return myFrame(self, width, height, title, bottom, top, right, left, panel)



#-----------------------------------------------------------------
# try to get 3D and make display class for 3D
__ok3d = 1
try:
  from visad.java3d import DisplayImplJ3D, TwoDDisplayRendererJ3D, DisplayRendererJ3D,MouseBehaviorJ3D
  from visad.bom import RubberBandBoxRendererJ3D

  # helper class for 3D displays -- need to subclass DisplayImplJ3D
  # and the _vdisp helper
  class _vdisp3D(_vdisp, DisplayImplJ3D):
      
    def __init__(self,maps,renderer):
      global __py_shapes
      DisplayImplJ3D.__init__(self,"Jython3D",renderer)
      _vdisp.__init__(self)
      if maps is not None:  addMaps(self, maps)
      self.shapes = Shapes(self, self.shape_map)
      __py_shapes = self.shapes

except:
  __ok3d = 0

try: # this keeps the doc from being generated 

  # helper class for 2D displays -- need to subclass DisplayImplJ2D
  # and the _vdisp helper
  class _vdisp2D(_vdisp, DisplayImplJ2D):

    def __init__(self,maps):
      global __py_shapes
      DisplayImplJ2D.__init__(self,"Jython2D")
      _vdisp.__init__(self)
      if maps is not None:  addMaps(self, maps)
      self.shapes = Shapes(self, self.shape_map)
      __py_shapes = self.shapes
except:
  pass


#----------------------------------------------------------------
# static methods start here
def makeDisplay3D(maps):
  """
  Create (and return) a VisAD DisplayImplJ3D and add the ScalarMaps
  <maps>, if any.  The VisAD box is resized to about 95% of the window.
  This returns the Display.
  """
  return _vdisp3D(maps,None)

def makeDisplay2D(maps):
  """
  Create (and return) a VisAD DisplayImplJ2D and add the ScalarMaps
  <maps>, if any.  The VisAD box is resized to about 95% of the window.
  This returns the Display.
  """
  return _vdisp2D(maps)

# create (and return) a VisAD DisplayImpl and add the ScalarMaps, if any
# the VisAD box is resized to about 95% of the window
def makeDisplay(maps):
  """
  Create (and return) a VisAD DisplayImpl and add the ScalarMaps
  <maps>, if any.  The VisAD box is resized to about 95% of the window.  
  Use 3D if availble, otherwise use 2D.  This returns the Display.
  """

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
      #tdr = TwoDDisplayRendererJ3D()
      #disp = _vdisp3D(maps, tdr)
      disp = makeDisplay3D(maps)
      mode = disp.getGraphicsModeControl()
      mode.setProjectionPolicy(DisplayImplJ3D.PARALLEL_PROJECTION)
      mousehelper = disp.getDisplayRenderer().getMouseBehavior().getMouseHelper()
      mousehelper.setFunctionMap([[[MouseHelper.NONE, MouseHelper.ZOOM],
                                   [MouseHelper.TRANSLATE, MouseHelper.NONE]],
                                  [[MouseHelper.CURSOR_TRANSLATE, MouseHelper.CURSOR_ZOOM],
                                   [MouseHelper.NONE, MouseHelper.NONE]],
                                  [[MouseHelper.DIRECT, MouseHelper.DIRECT],
                                   [MouseHelper.DIRECT, MouseHelper.DIRECT]]])
    else:
      disp =  _vdisp2D(maps)
  
  return disp


def saveDisplay(disp, filename):
  """
  Save the display <disp> as a JPEG, given the filename to use.
  """
  disp.saveDisplay(filename)


def addData(name, data, disp, constantMaps=None, renderer=None, ref=None, zlayer=None):
  """
  Add <data> to the display <disp>.  Use a reference <name>.
  If there are ConstantMaps, also add them.  If there is a non-default
  Renderer, use it as well.  You can supply a pre-defined
  DataReference as <ref>.  Finally, if nothing is mapped to Display.ZAxis,
  data objects with the highest <zlayer: (0,1)> will appear on top in the display.  

  Returns the DataReference
  """
  return disp.addData(name, data, constantMaps, renderer, ref, zlayer)


def setPointSize(display, size):
  """
  Set the size of points (1,2,3...) to use in the <display>.
  """
  display.setPointSize(size)

  
# define the aspects of width and height, as a ratio: width/height
def setAspectRatio(display, ratio):
  """
  Set the aspect <ratio> for the <display>.  The ratio is
  expressed as the fractional value for: width/height.
  """
  display.setAspectRatio(ratio)


def setAspects(display, x, y, z):
  """
  Set the relative sizes of each axis in the <display>.
  """
  display.setAspects(x, y, z)

def rotateBox(display, azimuth, declination):
  """
  Rotate the 3D display box to the azimuth angle (0-360) and
  declination angle (all in degrees).  Code from Unidata.
  """
  display.rotateBox(azimuth, declination)

def maximizeBox(display, clip=1):
  """
  Set the size of the VisAD 'box' for the <display> to 95%.  If
  <clip> is true, the display will be clipped at the border of the
  box; otherwise, data displays may spill over.
  """
  display.maximizeBox(clip)

def setBoxSize(display, percent=.70, clip=1, showBox=1, snap=0):
  """
  Set the size of the VisAD 'box' for the <display> as a percentage
  (fraction). The default is .70 (70%).   If <clip> is true, the
  display will be clipped at the border of the box; otherwise, data
  displays may spill over.  If <showBox> is true, the wire-frame will
  be shown; otherwise, it will be turned off.  If <snap> is true, the
  box will be reoriented to an upright position.
  """
  display.setBoxSize(percent, clip, showBox, snap)


def makeCube(display):
  """
  Turn the VisAD box for this <display> into a cube with no
  perspective (no 'vanishing point'.  Useful for aligning
  vertically-stacked data.  
  """
  display.makeCube()

def setBackgroundColor(display,color):
  """
  Set the background color to 'color' (which may be a
  java.awt.Color, or a string with the name in it (like 'green')
  """
  display.setBackgroundColor(color)

def setForegroundColor(display,color):
  """
  Set the foreground color to 'color' (which may be a
  java.awt.Color, or a string with the name in it (like 'green')
  """
  display.setForegroundColor(color)

def setBoxOn(display,on):
  """
  Turn the wire frame box on or off.
  """
  display.setBoxOn(on)
   
def setCursorColor(display,color):
  """
  Set the cursor color to 'color' (which may be a
  java.awt.Color, or a string with the name in it (like 'green')
  """
  display.setCursorColor(color)

def setBoxColor(display,color):
  """
  Set the box color to 'color' (which may be a
  java.awt.Color, or a string with the name in it (like 'green')
  """
  display.setBoxColor(color)

def zoomBox(display, factor):
  """
  Zoom the display by 'factor' (1.0 does nothing...). Related:
  setBoxSize().
  """
  display.zoomBox(factor)

def enableRubberBandBoxZoomer(display,useKey,callback=None,color=None,x=None,y=None):
  """
  Method to attach a Rubber Band Box zoomer to this
  display.  Once attached, it is there foreever!  The useKey
  parameter can be 0 (no key), 1(CTRL), 2(SHIFT)
  """
  display.enableRubberBandBoxZoomer(useKey,callback,color,x,y)

def enableRubberBandBox(display,useKey,callback=None,color=None,x=None,y=None):
  """
  Method to attach a Rubber Band Box to this display.
  Once attached, it is there foreever!  The useKey
  parameter can be 0 (no key), 1(CTRL), 2(SHIFT)
  """
  display.enableRubberBandBox(useKey,callback,color,x,y)

def getDisplayScalarMaps(display, includeShapes=0):
  """
  Return a list of the scalarmaps mappings for this display. The list
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

def getDisplayScalarMapLists(display, includeShapes=0):
  """
  Return a list of the scalarmaps mappings for this display. The list
  elements are ordered: x,y,z,display.  If <includeShapes> is
  true, then mappings for Shape will be appended.  The <display>
  may be a Display, or the name of a 'plot()' window.
  Note: this is identical to getDisplayScalarMaps except that
  the return type is a list of lists since there may more than
  one ScalarMap to x, y or z.
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
                                                                                                                                   
  x = []
  y = []
  z = []
  shape = []
                                                                                                                                   
  if maps != None:
    for m in maps:
      if m.getDisplayScalar().toString() == "DisplayXAxis":
        x.append(m)
      if m.getDisplayScalar().toString() == "DisplayYAxis":
        y.append(m)
      if m.getDisplayScalar().toString() == "DisplayZAxis":
        z.append(m)
      if m.getDisplayScalar().toString() == "DisplayShape":
        shape.append(m)
                                                                                                                                   
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

def drawLine(display, points, color=None, mathtype=None, style=None, width=None):
  """
  Deprecated.

  Draw lines on the <display>.  <points> is a 2 or 3 dimensional,
  list/tuple of points to connect as a line, ordered as given in the
  <mathtype>.  Default <mathtype> is whatever is mapped to the x,y
  (and maybe z) axis.  <color> is the line color ("red" or
  java.awt.Color; default=white), <style> is the line style (e.g.,
  "dash"), and <width> is the line width in pixels.

  Return a reference to this line.
  """
  return display.drawLine(points, color, mathtype, style, width)

# draw a string on the display
def drawString(display, string, point, color=None, center=0, font="futural",
                 start=[0.,0.,0.], base=[.1,0.,0.], up=[0.,.1,0.],size=None ):

  """
  Deprecated.

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
  size.  This returns the Shapes object.  Note that the drawString
  method in the display returns the shape index for this, so is
  the preferred method.
  """

  display.drawString(string, point, color, center,font, start, base, up, size)
  return display.shapes

def addMaps(display, maps):
  """
  Add list/tuple <maps> mappings to the <display>.  These determine what
  scalars will appear along what axes. See makeMaps, below.
  """
  display.addMaps(maps)



#-----------------------------------------------------------------
# non-display type of methods to do useful things
# make a 2D or 3D line, return a reference so it can be changed
def makeLine(domainType, points):
  """
  returns a set of <points>, as defined in the <domainType>. For
  example, if <domaintType> defines a (Latitude,Longitude), then
  the <points> are in Latitude,Longitude.
  """
  if isinstance(domainType, SetType):
    dt = domainType
  elif isinstance(domainType, RealTupleType):
    dt = domainType
  else:
    dt = RealTupleType(domainType)

  if len(points) == 2:
    return Gridded2DSet(dt, points, len(points[0]))
  else:
    return Gridded3DSet(dt, points, len(points[0]))


def makeLineStyleMap(style, width):
  """
  Make a ConstantMap for the indicated line <style>, which
  may be: "dash", "dot", "dashdot", or "solid" (default). The
  <width> is the line width in pixels.  Used by drawLine.
  """
  constmap = None
  constyle = None
  if style is not None:

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


def _color2rgb(color):
  """
  Return a triplet of Red,Green,Blue values (0-1)
  for the given color or color name
  """
  # if it's a real color or a name
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

  return red,green,blue

  
def makeColorMap(color):
  """
  Return a ConstantMap of <color>, given by name (e.g., "red")
  or a java.awt.Color object.  Default is white. Used by numerous methods.
  """

  red,green,blue = _color2rgb(color)
  constmap = [ ConstantMap(red,Display.Red), ConstantMap(green,Display.Green),
         ConstantMap(blue,Display.Blue) ] 

  return constmap


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
  Deprecated.  (Please use the instance method from the 'display'
  you intend to use this Shape within.)
  
  Simply a shadow method for addShape in case the user has not
  made their own.  You cannot use this method with more than
  one display (see Shapes class, or use the addShapes() method
  for the 'display')
  """
  return (__py_shapes.addShape(type, scale, color, index, autoScale))


def moveShape(index, coord):
  """
  Deprecated.  (Please use the instance method from the 'display'
  you intend to use this Shape within.)

  Simply a shadow method for moveShape in case the user has not
  made their own.
  """
  __py_shapes.moveShape(index, coord)


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


def changeRangeName(data, new_name):
  """
  Change the name of the (single) range component of the Data 
  object...which really needs to be a Field of some kind.  <data>
  is the Data object, <new_name> is the new name -- it will
  inherit the Units of the original.
  """
  _at = data.getType()
  _au = _at.getRange()[0].getDefaultUnit()
  _nft = FunctionType(_at.getDomain(), RealType.getRealType(new_name,_au))
  return data.changeMathType(_nft)

#--------------------------------------------------------------------
# other classes
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
    try:
      self.display.destroy()
    except:
      pass
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

    self.display.my_frame = self.frame



class Shapes:
  """
  Helper class for handling Shapes within a display.
  """

  def __init__(self, display, shapemap):
    """
    <display> is the display this is working with; <shapemap>
    is the mapping parameter for shapes. Prior to creating
    this instance, the <display> should have all its
    ScalarMaps set up.
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
    ad=addData("shape",shapeLoc, self.disp, constmap)
    self.shapeRef.append(ad)

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

  def __init__(self, display, requireKey, zoom, callback, color, x, y):
    """
    display is the display object.  requireKey = 0 (no key),
    = 1 (CTRL), = 2 (SHIFT)
    """
    if x != None and y != None:
      self.x = x
      self.y = y
      self.display = display
    else:
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
    constMap = None
    if color != None:
      constMap = makeColorMap(color)
    self.ref = addData('rbb',self.dummy_set,self.display, constMap, renderer=self.rbb)
    self.callback = callback
    self.zoom = zoom
    
    class MyCell(CellImpl):
      def __init__(self, ref, disp, zoom, callback):
        self.ref = ref
        self.display = disp
        self.xm, self.ym, self.zm, self.d = getDisplayScalarMaps(disp)
        self.dr = self.display.getDisplayRenderer()
        self.can = self.dr.getCanvas()
        self.mb = MouseBehaviorJ3D(self.dr)
        self.pc = self.display.getProjectionControl()
        self.callback = callback
        self.zoom     = zoom

      def doAction(self):

        # get the 'box' coordinates of the RB box
        set = self.ref.getData()
        samples = set.getSamples()
        if samples is None: return  # no sense in going further
        if self.callback is not None:  self.callback(samples)
        if self.zoom == 1: self.zoomIt(samples)

      def zoomIt(self, samples):
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


    self.cell = MyCell(self.ref,self.display,self.zoom, self.callback)
    self.cell.addReference(self.ref)

  def zoomit(self, samples):
    """
    To force a zoom with an array of samples, call this...
    samples[2][] contains values for the corner points, in
    the proper scalar type.  samples[0][] is x,y of upperleft.
    samples[1][] is x,y is lower right.
    
    """
    self.cell.zoomIt(samples)


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

from visad import ControlListener
class LinkBoxControl(ControlListener):
  def __init__(self, mydisplay, otherdisplay):
    self.me = mydisplay
    self.other = otherdisplay
    self.control = self.me.getProjectionControl()
    self.control.addControlListener(self)
    self.mat = self.control.getMatrix()
  
  def controlChanged(self,e):
    self.otherpc = self.other.getProjectionControl()
    if not self.control.equals(self.otherpc):
      self.mat = self.control.getMatrix()
      self.otherpc.setMatrix(self.mat)
