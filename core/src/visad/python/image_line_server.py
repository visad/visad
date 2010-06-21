from visad.python.JPythonMethods import *
import subs
from visad.java2d import *
from visad import DataReferenceImpl, CellImpl, AxisScale
from visad.util import VisADSlider
from javax.swing import JFrame, JPanel
from java.awt import BorderLayout, GridLayout, Font

image = load("AREA0007")
print "Done reading data..."

dom = getDomain(image)
d = domainType(image)
r = rangeType(image)

# max lines & elements of image
NELE = dom.getX().getLength()
LINES = dom.getY().getLength()

# subs for image in display-1
m = subs.makeMaps(d[0],"x",d[1],"y",r[0],"rgb")
d1 = subs.makeDisplay(m)
subs.setBoxSize(d1,.80)

# add the image to the display
refimg = subs.addData("image", image, d1)

# now the second panel
m2 = subs.makeMaps(d[0],"x",r[0],"y")
m2[1].setRange(0, 255)
d2 = subs.makeDisplay(m2)
subs.setBoxSize(d2,.80)

# get the desired format of the Data (line->(element->value))
# factoring works because image has a 2-D rectangular sampling
byline = domainFactor(image,d[1])
ref2 = subs.addData("imageline", byline[0], d2)

# set up a dummy reference so we can put the line onto the display
usref = subs.addData("line", None, d1)

# define an inner-type CellImpl class to handle changes
class MyCell(CellImpl):
 def doAction(this):
  line = (userline.getData()).getValue()
  iline = (LINES-1) - line
  pts = subs.makeLine( (d[1], d[0]), ((iline,iline),(0,NELE)))
  usref.setData(pts)
  ff = byline[int(line)]
  ref2.setData(ff)

# make a DataReference that we can use to change the value of "line"
userline = DataReferenceImpl("userline")
slide = VisADSlider("imgline",0,LINES,0,1.0,userline,d[1])

cell = MyCell();
cell.addReference(userline)

# change the scale label on x axis
showAxesScales(d2,1)

# display everything...
frame = JFrame("Test T8 Server")
pane = frame.getContentPane()
pane.setLayout(BorderLayout())
# GridLayout with 1 row, 2 columns, 5 pixel horiz and vert gaps
panel = JPanel(GridLayout(1,2,5,5))
panel.add(d1.getComponent())
panel.add(d2.getComponent())
pane.add("Center",panel)
pane.add("North",slide)
frame.setSize(800,500)
frame.setVisible(1)

from visad.util import ClientServer
# add displays and data to the server
server = ClientServer.startServer("Jython")
server.addDisplay(d1)
server.addDisplay(d2)
server.addDataReference(refimg)
server.addDataReference(userline)

