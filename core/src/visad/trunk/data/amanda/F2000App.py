#!/usr/bin/env jython

import sys

from java.awt import Dimension
from javax.swing import BoxLayout, JPanel

from visad import *
from visad.data.amanda import AmandaFile, F2000Util
from visad.java3d import DisplayImplJ3D
from visad.util import LabeledColorWidget, VisADSlider

class DisplayFrame:
  def desty(self, event):
    self.display.destroy()
    sys.exit(0)

  def __init__(self, title, display, panel, width, height):
    from java.awt import Toolkit
    from javax.swing import JFrame
    self.display = display

    frame = JFrame(title, windowClosing=self.desty)
    frame.getContentPane().add(panel)

    frame.setSize(width, height)

    screensize = Toolkit.getDefaultToolkit().getScreenSize()
    frame.setLocation((screensize.width - width)/2,
                      (screensize.height - height)/2)
    frame.setVisible(1)

class DisplayMaps:
  def __init__(self, file, display):
    # compute x, y and z ranges with unity aspect ratios
    xrange = file.getXMax() - file.getXMin()
    yrange = file.getYMax() - file.getYMin()
    zrange = file.getZMax() - file.getZMin()
    half_range = -0.5 * max(xrange, max(yrange, zrange))
    xmid = 0.5 * (file.getXMax() + file.getXMin())
    ymid = 0.5 * (file.getYMax() + file.getYMin())
    zmid = 0.5 * (file.getZMax() + file.getZMin())
    xmin = xmid - half_range
    xmax = xmid + half_range
    ymin = ymid - half_range
    ymax = ymid + half_range
    zmin = zmid - half_range
    zmax = zmid + half_range

    xmap = ScalarMap(file.getXType(), Display.XAxis)
    display.addMap(xmap)
    xmap.setRange(xmin, xmax)

    ymap = ScalarMap(file.getYType(), Display.YAxis)
    display.addMap(ymap)
    ymap.setRange(ymin, ymax)

    zmap = ScalarMap(file.getZType(), Display.ZAxis)
    display.addMap(zmap)
    zmap.setRange(zmin, zmax)

    self.trackmap = ScalarMap(AmandaFile.getTrackIndexType(),
                              Display.SelectValue)
    display.addMap(self.trackmap)

    self.shapemap = ScalarMap(AmandaFile.getAmplitudeType(), Display.Shape)
    display.addMap(self.shapemap)

    shape_scalemap = ScalarMap(AmandaFile.getAmplitudeType(),
                               Display.ShapeScale)
    display.addMap(shape_scalemap)
    shape_scalemap.setRange(-20.0, 50.0)

    self.letmap = ScalarMap(AmandaFile.getLeadEdgeTimeType(), Display.RGB)
    display.addMap(self.letmap)

############################################################################

if len(sys.argv) != 2:
  sys.stderr.write("Please specify the F2000 file to be read\n")
  sys.exit(1)

file = AmandaFile(sys.argv[1])
temp = file.makeData()

amanda = temp.getComponent(0)
modules = temp.getComponent(1)

display = DisplayImplJ3D("amanda")

maps = DisplayMaps(file, display)

displayRenderer = display.getDisplayRenderer()
displayRenderer.setBoxOn(0)

scontrol = maps.shapemap.getControl()
scontrol.setShapeSet(Integer1DSet(AmandaFile.getAmplitudeType(), 1))
scontrol.setShapes(F2000Util.getCubeArray())

nevents = amanda.getLength()

amanda_ref = DataReferenceImpl("amanda")
# amanda_ref.setData(amanda)
display.addReference(amanda_ref)

modules_ref = DataReferenceImpl("modules")
modules_ref.setData(modules)
display.addReference(modules_ref)

print "amanda MathType\n",amanda.getType().toString()

event_ref = DataReferenceImpl("event")

class MyCell(CellImpl):
  def doAction(this):
    r = event_ref.getData()
    if r is not None:
      index = r.getValue()
      if index < 0:
        index = 0
      elif index > nevents:
        index = nevents
      amanda_ref.setData(amanda.getSample(int(index)))

cell = MyCell()
cell.addReference(event_ref)

let_widget = LabeledColorWidget(maps.letmap)
let_widget.setMaximumSize(Dimension(400,250))

event_slider = VisADSlider("event", 0, nevents - 1, 0, 1.0, event_ref,
                           AmandaFile.getEventIndexType())

widget_panel = JPanel()
widget_panel.setLayout(BoxLayout(widget_panel, BoxLayout.Y_AXIS))
widget_panel.setMaximumSize(Dimension(400, 600))

widget_panel.add(let_widget)
widget_panel.add(VisADSlider(maps.trackmap, 1))
widget_panel.add(event_slider)

display_panel = display.getComponent()
dim = Dimension(800, 800)
display_panel.setPreferredSize(dim)
display_panel.setMinimumSize(dim)

# create JPanel in frame
panel = JPanel()
panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))

panel.add(widget_panel)
panel.add(display_panel)

DisplayFrame("VisAD AMANDA Viewer", display, panel, 1200, 800)
