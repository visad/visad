from visad.python.JPythonMethods import *
import subs, graph
from visad import Real, RealTuple, RealType, DataReferenceImpl
from visad import Display, ScalarMap
from visad.java3d import DefaultRendererJ3D
from visad.bom import DiscoverableZoom

a=load("../data/mcidas/AREA0007")
ds = a.getDomainSet()
xdim = ds.getX().getLength()
ydim = ds.getY().getLength()
print "Image size = ",ydim," by",xdim

cs = ds.getCoordinateSystem()

rangetype = a.getType().getRange()
maps=subs.makeMaps(RealType.Latitude, "y", RealType.Longitude, "x")
tm = ScalarMap(rangetype[0], Display.Text)
maps.append(tm)
disp=subs.makeDisplay3D(maps)

pcontrol = disp.getProjectionControl()
dzoom = DiscoverableZoom()
pcontrol.addControlListener(dzoom)

tcontrol = tm.getControl()
tcontrol.setAutoSize(1)

rends = []
for i in range (400, 420):
  for j in range (420, 440):

    ref = DataReferenceImpl("data")
    latlon = cs.toReference( ( (j,), (i,) ))
    tuple = RealTuple( ( 
           a[i * xdim + j][0], 
           Real( RealType.Latitude, latlon[0][0]), 
           Real( RealType.Longitude, latlon[1][0]) ) )

    ref.setData(tuple)
    rend = DefaultRendererJ3D()
    rends.append(rend)
    disp.addReferences(rend, ref, None)

dzoom.setRenderers(rends, .1)
subs.showDisplay(disp)

