from visad.python.JPythonMethods import *
area = read("../examples/AREA2001")
print area.length

for i in range(20000, 21000):
	area[i] = 0

map = read("../examples/OUTLSUPW")
print map.length

plot(area)
plot(map)
