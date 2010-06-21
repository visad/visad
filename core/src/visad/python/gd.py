from visad import *
from visad import ScalarMap, FlowControl, Display, ConstantMap
from visad.bom import BarbRendererJ3D,ImageRendererJ3D
from visad.data.mcidas import PointDataAdapter, BaseMapAdapter
from visad.python.JPythonMethods import *
from subs import *
from visad.util import SelectRangeWidget
from visad.util import AnimationWidget

#select conditions
position=2
imgDate = "2001-01-26"
imgTime = ["04:52:00","05:52:00","06:52:00","07:52:00","08:52:00",
           "09:52:00","10:52:00","11:52:00","12:52:00","13:52:00"]

ptdata=[]
date=[]
img=[]

for t in range(10):
# get winds
  request= "adde://suomi.ssec.wisc.edu/pointdata?group=cimssp&descr=wndpointhur&parm=lat lon dir spd pw &select='LAT 15 65;LON 80 180'&num=all&pos="+str(t+1)
  print request
  ptdata.append( PointDataAdapter(request).getData() )
  date.append( DateTime.createDateTime(imgDate+" "+imgTime[t]+"Z") )

# get images
  request = "adde://suomi.ssec.wisc.edu/imagedata?group=cimssp&descr=wndimagehur&latlon=34 -144&size=425 1100&day="+imgDate+"&time="+imgTime[t]+"&mag=-2 -2&band=4&unit=BRIT&version=1"
  print request
  img.append( load(request) )

# get the types of the data for everything
lat = getRealType('LAT')
lon = getRealType('LON')
dir = getRealType('DIR')
spd = getRealType('SPD')
pw = getRealType('PW')

maps = makeMaps(lat,"y",
                lon, "x",
                pw, "z",
                dir, "flow1azimuth",
                spd, "flow1radial",
                spd, "rgb",
                pw, "selectrange",
                RealType.Time, "animation")

# set the ranges along each axis
maps[0].setRange(15, 65)
maps[1].setRange(-180, -80)
maps[2].setRange(1000, 100)
maps[3].setRange(0,360)
maps[4].setRange(0,1)

disp = makeDisplay3D(maps)
setBoxSize(disp,.6)
makeCube(disp)
showAxesScales(disp, 1)

# set up the size of the barbs and stuff
control = maps[4].getControl()
control.setFlowScale(.03)
control.setBarbOrientation(FlowControl.NH_ORIENTATION)


# define the underlying basemap and put it at z=1000 in gray
bma=BaseMapAdapter("OUTLUSAM")
bma.setLatLonLimits(15.,65.,-180.,-80.)
a=bma.getData()

constBaseMap = (ConstantMap(.0, Display.Red), 
            ConstantMap(.0, Display.Green), 
            ConstantMap(.0, Display.Blue), 
            ConstantMap(-.98, Display.ZAxis) )

#domain = makeDomain(RealType.Time, date[0].getValue(), date[2].getValue(),3)
domain = makeDomain(RealType.Time, date[0].getValue(), date[9].getValue(),10)
ftype = FunctionType(RealType.Time, ptdata[0].getType())
field = FieldImpl(ftype, domain)
ftype2 = FunctionType(RealType.Time, img[0].getType())
field2 = FieldImpl(ftype2, domain)

for t in range(10):
  field.setSample(t,ptdata[t])
  field2.setSample(t,img[t])

constImageMap = (ConstantMap(-1.0, Display.ZAxis), )
imgRange = rangeType(img[0])
rngMap = ScalarMap(imgRange[0], Display.RGB)
disp.addMap(rngMap)

gray=[]
for i in xrange(255):
  gray.append(float(i)/255.)
colorTable = (gray, gray, gray)
rngMap.getControl().setTable(colorTable)

addData("winds", field, disp, renderer=BarbRendererJ3D() )
addData("basemap", a, disp, constBaseMap)
addData("image", field2, disp, constImageMap, renderer=ImageRendererJ3D() )

rw = SelectRangeWidget(maps[6])
aw = AnimationWidget(maps[7], 200)

showDisplay(disp, 400, 400, "GWINDEX Winds", bottom=rw, top=aw)

