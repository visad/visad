from visad.python.JPythonMethods import *
data = load("b2rlc.nc")
data2 = data[2][0]
num = data2.getDimension()
spectrum = data2[num-1]

print spectrum.getType()

ft = fft(spectrum)

clearplot()
plot(ft, 1, 0, 0)
