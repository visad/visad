from visad import RealType, RealTupleType, FunctionType, FieldImpl, ScalarMap, Display
from visad.util import AnimationWidget
from visad.python.JPythonMethods import *
from javax.swing import JFrame, JPanel
from java.awt import BorderLayout, FlowLayout, Font
import subs

__mapname='outlsupu'

def image(data, panel=None, colortable=None, width=400, height=400, title="VisAD Image"):

  dom_1 = RealType.getRealType(domainType(data,0) )
  dom_2 = RealType.getRealType(domainType(data,1)) 
  rng = RealType.getRealType(rangeType(data,0))
  rngMap = ScalarMap(rng, Display.RGB)
  xMap = ScalarMap(dom_1, Display.XAxis)
  yMap = ScalarMap(dom_2, Display.YAxis)
  maps = (xMap, yMap, rngMap)
  disp = subs.makeDisplay(maps)

  if colortable is None:
    # make a gray-scale table
    gray = []
    for i in range(0,255):
      gray.append( float(i)/255.)
    colortable = (gray, gray, gray)

  rngMap.getControl().setTable(colortable)

  dr=subs.addData("brightness", data, disp)
  subs.setBoxSize(disp, .80)
  subs.setAspectRatio(disp, float(width)/float(height))
  subs.showDisplay(disp,width,height,title,None,None,panel)
  return disp

#----------------------------------------------------------------------
# mapimage displays a navigatedimage with a basemap on top
def mapimage(imagedata, mapfile="outlsupw", panel=None, colortable=None, width=400, height=400, lat=None, lon=None, title="VisAD Image and Map"):

  rng = RealType.getRealType(rangeType(imagedata,0))
  rngMap = ScalarMap(rng, Display.RGB)
  xMap = ScalarMap(RealType.Longitude, Display.XAxis)
  yMap = ScalarMap(RealType.Latitude, Display.YAxis)
  maps = (xMap, yMap, rngMap)
  dom = getDomain(imagedata)
  xc = dom.getX()
  yc = dom.getY()
  xl = len(xc)
  yl = len(yc)
  if xl > 1024 or yl > 1024:
    print "Resampling image from",yl,"x",xl,"to",min(yl,1024),"x",min(xl,1024)
    imagedata = resample(imagedata, makeDomain(dom.getType(),
                         xc.getFirst(), xc.getLast(), min(xl, 1024),
                         yc.getFirst(), yc.getLast(), min(yl, 1024) ) )

  if lat is None or lon is None:
    c=dom.getCoordinateSystem()
    ll = c.toReference( ( (0,0,xl,xl),(0,yl,0,yl) ) )
    import java.lang.Double.NaN as missing

    if (min(ll[0]) == missing) or (min(ll[1]) == missing) or (min(ll[1]) == max(ll[1])) or (min(ll[0]) == max(ll[0])):
      # compute delta from mid-point...as an estimate
      xl2 = xl/2.0
      yl2 = yl/2.0
      ll2 = c.toReference( ( 
                (xl2,xl2,xl2,xl2-10, xl2+10),(yl2,yl2-10,yl2+10,yl2,yl2)))
      dlon = abs((ll2[1][4] - ll2[1][3])*xl/40.) + abs((ll2[0][4] - ll2[0][3])*yl/40.)

      dlat = abs((ll2[0][2] - ll2[0][1])*yl/40.) + abs((ll2[1][2] - ll2[1][1])*xl/40.)

      lonmin = max( -180., min(ll2[1][0] - dlon, min(ll[1])))
      lonmax = min( 360., max(ll2[1][0] + dlon, max(ll[1])))

      latmin = max(-90., min(ll2[0][0] - dlat, min(ll[0])))
      latmax = min(90., max(ll2[0][0] + dlat, min(ll[0])))

      xMap.setRange(lonmin, lonmax)
      yMap.setRange(latmin, latmax)
      print "computed lat/lon bounds=",latmin,latmax,lonmin,lonmax

    else:
      xMap.setRange(min(ll[1]), max(ll[1]))
      yMap.setRange(min(ll[0]), max(ll[0]))

  else:
    yMap.setRange(lat[0], lat[1])
    xMap.setRange(lon[0], lon[1])

  disp = subs.makeDisplay(maps)

  if colortable is None:
    # make a gray-scale table
    gray = []
    for i in range(0,255):
      gray.append( float(i)/255.)
    colortable = (gray, gray, gray)

  rngMap.getControl().setTable(colortable)
  mapdata = load(mapfile)
  drm = subs.addData("basemap", mapdata, disp)
  dr=subs.addData("addeimage", imagedata, disp)
  subs.setBoxSize(disp, .80, clip=1)
  subs.setAspectRatio(disp, float(width)/float(height))
  subs.showDisplay(disp,width,height,title,None,None,panel)
  return disp

  
#----------------------------------------------------------------------------
# use GetAreaGUI to repeatedly show an image

def __showaddeimage(event):
  mapimage(load(event.getActionCommand()),__mapname)

def addeimage(mapname='outlsupu'):
  global __mapname
  __mapname = mapname
  from edu.wisc.ssec.mcidas.adde import GetAreaGUI
  __gag = GetAreaGUI("Show",0,0,actionPerformed=__showaddeimage)
  __gag.show()

#----------------------------------------------------------------------------
# basic scatter plot between two fields.
def scatter(data_1, data_2, panel=None, pointsize=None, width=400, height=400, xlabel=None, ylabel=None, title="VisAD Scatter"):

  rng_1 = data_1.getType().getRange().toString()
  rng_2 = data_2.getType().getRange().toString()
  data = FieldImpl.combine((data_1,data_2))
  maps = subs.makeMaps(getRealType(rng_1),"x", getRealType(rng_2),"y")
  disp = subs.makeDisplay(maps)
  subs.addData("data", data, disp)
  subs.setBoxSize(disp, .70)
  showAxesScales(disp,1)
  #setAxesScalesFont(maps, Font("Monospaced", Font.PLAIN, 18))
  if pointsize is not None: subs.setPointSize(disp, size)

  subs.setAspectRatio(disp, float(width)/float(height))
  subs.showDisplay(disp,width,height,title,None,None,panel)
  return disp

#----------------------------------------------------------------------------
# quick look histogram - only first range component is used.
def histogram(data, bins=20, width=400, height=400, title="VisAD Histogram", color=None, panel=None):

  from java.lang.Math import abs

  x=[]
  y=[]

  h = hist(data, [0], [bins])
  dom = getDomain(h)
  d = dom.getSamples()
  step2 = dom.getStep()/2

  hmin = h[0].getValue()
  hmax = hmin

  for i in range(0,len(h)):
    hval = h[i].getValue()
    if hval < hmin: hmin = hval
    if hval > hmax: hmax = hval

  for i in range(0,len(h)):
    xm = d[0][i]-step2
    xp = d[0][i]+step2
    x.append(xm)
    y.append(hmin)
    x.append(xm)
    hval = h[i].getValue()
    y.append(hval)
    x.append(xp)
    y.append(hval)
    x.append(xp)
    y.append(hmin)
  
  domt = domainType(h)
  rngt = rangeType(h)

  xaxis = ScalarMap(domt[0], Display.XAxis)
  yaxis = ScalarMap(rngt, Display.YAxis)

  yaxis.setRange(hmin, hmax + abs(hmax * .05))

  disp = subs.makeDisplay( (xaxis, yaxis) )
  subs.drawLine(disp, (x,y), mathtype=(domt[0],rngt), color=color)
  showAxesScales(disp,1)
  subs.setBoxSize(disp,.65)
  subs.setAspectRatio(disp, float(width)/float(height))
  subs.showDisplay(disp,width,height,title,None,None,panel)

  return disp


#----------------------------------------------------------------------------
# a simple line plot for one parameter
def lineplot(data, panel=None, color=None, width=400, height=400, title="Line Plot"):
  domt = domainType(data)
  rngt = rangeType(data)
  xaxis = ScalarMap(domt[0], Display.XAxis)
  yaxis = ScalarMap(rngt, Display.YAxis)
  axes = (xaxis, yaxis)

  disp = subs.makeDisplay( axes )
  constmap = subs.makeColorMap(color)

  dr=subs.addData("Lineplot", data, disp, constmap)
  subs.setBoxSize(disp, .70)
  showAxesScales(disp, 1)
  setAxesScalesFont(axes, Font("Monospaced", Font.PLAIN, 18))

  subs.setAspectRatio(disp, float(width)/float(height))
  subs.showDisplay(disp,width,height,title,None,None,panel)
  
  return disp

#----------------------------------------------------------------------------
# a contour plot of a 2D field
# interval = [ interval, lowvalue, highvalue, basevalue ]

def contour(data, panel=None, enableLabels=1, interval=None, width=400, height=400, title="VisAD Contour Plot"):

  ndom = domainDimension(data)
  if ndom != 2:
    print "domain dimension must be 2!"
    return None

  dom_1 = RealType.getRealType(domainType(data,0) )
  dom_2 = RealType.getRealType(domainType(data,1)) 
  rng = RealType.getRealType(rangeType(data,0))
  rngMap = ScalarMap(rng, Display.IsoContour)
  xMap = ScalarMap(dom_1, Display.XAxis)
  yMap = ScalarMap(dom_2, Display.YAxis)
  maps = (xMap, yMap, rngMap)

  disp = subs.makeDisplay(maps)
  ci = rngMap.getControl()
  ci.enableLabels(enableLabels)
  if interval is not None:
    ci.setContourInterval(interval[0], interval[1], interval[2], interval[3])

  dr=subs.addData("contours", data, disp)
  subs.setBoxSize(disp, .80)
  subs.setAspectRatio(disp, float(width)/float(height))
  subs.showDisplay(disp,width,height,title,None,None,panel)

  return disp


#----------------------------------------------------------------------------
# animation(data) creates a VisAD animation of the items in the data list/tuple
# if panel is not None, then it will return a JPanel with the images
# and AnimationWidget in it
def animation(data, panel=None, width=400, height=500, title="VisAD Animation"):

  num_frames = len(data)

  frames = RealType.getRealType("frames")
  frames_type = RealTupleType( frames )

  image_type = data[0].getType()
  ndom = domainDimension(data[0])

  if ndom != 2:
    print "domain dimension must be 2!"
    return None

  dom_1 = RealType.getRealType(domainType(data[0],0) )
  dom_2 = RealType.getRealType(domainType(data[0],1)) 

  nrng = rangeDimension(data[0])
  if (nrng != 3) and (nrng != 1):
    print "range dimension must be 1 or 3"
    return None

  # now create display scalar maps
  maps = None
  rng_1 = rangeType(data[0],0)
  if nrng == 3:
    rng_2 = rangeType(data[0],1)
    rng_3 = rangeType(data[0],2)
    rng_red = None
    if (rng_1 == "Red"): rng_red = rng_1
    if (rng_2 == "Red"): rng_red = rng_2
    if (rng_3 == "Red"): rng_red = rng_3
    rng_green = None
    if (rng_1 == "Green"): rng_green = rng_1
    if (rng_2 == "Green"): rng_green = rng_2
    if (rng_3 == "Green"): rng_green = rng_3
    rng_blue = None
    if (rng_1 == "Blue"): rng_blue = rng_1
    if (rng_2 == "Blue"): rng_blue = rng_2
    if (rng_3 == "Blue"): rng_blue = rng_3

    if (rng_red is None) or (rng_green is None) or (rng_blue is None):
      print "3 Range components must be Red, Green and Blue"

    else:
      maps = subs.makeMaps(dom_1,"x", dom_2,"y", RealType.getRealType(rng_red), "red", RealType.getRealType(rng_green), "green", RealType.getRealType(rng_blue), "blue")

  else:
    maps = subs.makeMaps(dom_1,"x", dom_2, "y", RealType.getRealType(rng_1), "rgb")

  frame_images = FunctionType(frames_type, image_type)
  frame_set = makeDomain(frames, 0, num_frames-1, num_frames)
  frame_seq = FieldImpl(frame_images, frame_set)

  for i in range(0,num_frames):
    frame_seq.setSample(i, data[i])

  disp = subs.makeDisplay(maps)
  animap = ScalarMap(frames, Display.Animation)
  disp.addMap(animap)
  refimg = subs.addData("VisAD_Animation", frame_seq, disp)
  widget = AnimationWidget(animap, 500) 
  subs.setAspectRatio(disp, float(width)/float(height))
  myAnimFrame(disp, widget, width, height, "Animation")

  return disp

class myAnimFrame:
  def desty(self, event):
    self.display.destroy()
    self.frame.dispose()

  def __init__(self, display, widget, width, height, title):
    from javax.swing import JFrame, JPanel
    from java.awt import BorderLayout, FlowLayout
    self.display = display
    self.panel = JPanel(BorderLayout())
    self.panel2 = JPanel(FlowLayout())
    self.panel2.add(widget)
    self.panel.add("North", self.panel2)
    self.panel.add("Center",self.display.getComponent())

    self.frame = JFrame(title, windowClosing=self.desty)
    self.pane = self.frame.getContentPane()
    self.pane.add(self.panel)

    self.frame.setSize(width,height)
    self.frame.pack()
    self.frame.show()

