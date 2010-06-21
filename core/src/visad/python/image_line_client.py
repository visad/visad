from visad.python.JPythonMethods import *
from visad.util import VisADSlider, ClientServer
from javax.swing import JFrame, JPanel
from java.awt import BorderLayout, GridLayout, Font

# connect to server - modify "localhost" to actual server name
client = ClientServer.connectToServer("localhost", "Jython")

# fetch displays and data from the server
d1 = ClientServer.getClientDisplay(client, 0)
d2 = ClientServer.getClientDisplay(client, 1)
refimg = client.getDataReference(0)
userline = client.getDataReference(1)

# get image in order to get its size and type schema
image = refimg.getData()
dom = getDomain(image)
LINES = dom.getY().getLength()
d = domainType(image)

# create line slider for client user
slide = VisADSlider("imgline",0,LINES,0,1.0,userline,d[1])

showAxesScales(d2,1)

# display everything...
frame = JFrame("Test T8 Client")
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

