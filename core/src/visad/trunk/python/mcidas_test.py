from visad.python.JPythonMethods import *
area = read("../examples/AREA2001")
print area.length

for i in range(20000, 21000):
	area[i] = 0

map = read("../examples/OUTLSUPW")
print map.length

print map[3]

j = 100
for i in map:
	print i
	j = j - 1
	if j < 0: break

plot(area)
plot(map)
