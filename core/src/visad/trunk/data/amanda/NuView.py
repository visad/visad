#!/usr/bin/env jython

import sys

from java.awt import Component, Dimension
from java.lang import Boolean
from javax.swing import Box, BoxLayout, JPanel

from visad import *
from visad.data.amanda import AmandaFile, BaseTrack, EventWidget, F2000Util, Hit, TrackWidget
from visad.java3d import DisplayImplJ3D
from visad.util import LabeledColorWidget

class DisplayFrame:
  def desty(self, event):
    self.display.destroy()
    sys.exit(0)

  def __init__(self, title, display, panel):
    from java.awt import Toolkit
    from javax.swing import JFrame
    self.display = display

    frame = JFrame(title, windowClosing=self.desty)
    frame.getContentPane().add(panel)
    frame.pack()
    frame.invalidate()

    fSize = frame.getSize()
    screensize = Toolkit.getDefaultToolkit().getScreenSize()
    frame.setLocation((screensize.width - fSize.width)/2,
                      (screensize.height - fSize.height)/2)
    frame.setVisible(1)

class DisplayMaps:
  def __init__(self, file, display):
    # compute x, y and z ranges with unity aspect ratios
    xrange = file.getXMax() - file.getXMin()
    yrange = file.getYMax() - file.getYMin()
    zrange = file.getZMax() - file.getZMin()
    halfrange = -0.5 * max(xrange, max(yrange, zrange))
    xmid = 0.5 * (file.getXMax() + file.getXMin())
    ymid = 0.5 * (file.getYMax() + file.getYMin())
    zmid = 0.5 * (file.getZMax() + file.getZMin())
    xmin = xmid - halfrange
    xmax = xmid + halfrange
    ymin = ymid - halfrange
    ymax = ymid + halfrange
    zmin = zmid - halfrange
    zmax = zmid + halfrange

    xmap = ScalarMap(RealType.XAxis, Display.XAxis)
    display.addMap(xmap)
    xmap.setRange(xmin, xmax)

    ymap = ScalarMap(RealType.YAxis, Display.YAxis)
    display.addMap(ymap)
    ymap.setRange(ymin, ymax)

    zmap = ScalarMap(RealType.ZAxis, Display.ZAxis)
    display.addMap(zmap)
    zmap.setRange(zmin, zmax)

    self.trackmap = ScalarMap(BaseTrack.indexType, Display.SelectValue)
    display.addMap(self.trackmap)

    self.shapemap = ScalarMap(Hit.amplitudeType, Display.Shape)
    display.addMap(self.shapemap)

    shapeScalemap = ScalarMap(Hit.amplitudeType, Display.ShapeScale)
    display.addMap(shapeScalemap)
    shapeScalemap.setRange(-20.0, 50.0)

    self.letmap = ScalarMap(Hit.leadingEdgeTimeType, Display.RGB)
    display.addMap(self.letmap)

############################################################################

if len(sys.argv) != 2:
  sys.stderr.write("Please specify the F2000 file to be read\n")
  sys.exit(1)

file = AmandaFile(sys.argv[1])

amanda = file.makeEventData()
modules = file.makeModuleData()

display = DisplayImplJ3D("amanda")

maps = DisplayMaps(file, display)

displayRenderer = display.getDisplayRenderer()
displayRenderer.setBoxOn(0)

scontrol = maps.shapemap.getControl()
scontrol.setShapeSet(Integer1DSet(Hit.amplitudeType, 1))
scontrol.setShapes(F2000Util.getCubeArray())

nevents = amanda.getLength()

amandaRef = DataReferenceImpl("amanda")
# data set by eventWidget below
display.addReference(amandaRef)

modulesRef = DataReferenceImpl("modules")
modulesRef.setData(modules)
display.addReference(modulesRef)

letWidget = LabeledColorWidget(maps.letmap)
# align along left side, to match VisADSlider alignment
#   (if we don't left-align, BoxLayout hoses everything
letWidget.setAlignmentX(Component.LEFT_ALIGNMENT)

eventWidget = EventWidget(file, amanda, amandaRef, maps.trackmap)

widgetPanel = JPanel()
widgetPanel.setLayout(BoxLayout(widgetPanel, BoxLayout.Y_AXIS))
widgetPanel.setMaximumSize(Dimension(400, 600))

widgetPanel.add(letWidget)
widgetPanel.add(eventWidget)
widgetPanel.add(Box.createHorizontalGlue())

displayPanel = display.getComponent()
dim = Dimension(800, 800)
displayPanel.setPreferredSize(dim)
displayPanel.setMinimumSize(dim)

# if widgetPanel alignment doesn't match
#  displayPanel alignment, BoxLayout will freak out
widgetPanel.setAlignmentX(displayPanel.getAlignmentX())
widgetPanel.setAlignmentY(displayPanel.getAlignmentY())

# create JPanel in frame
panel = JPanel()
panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))

panel.add(widgetPanel)
panel.add(displayPanel)

DisplayFrame("VisAD AMANDA Viewer", display, panel)
