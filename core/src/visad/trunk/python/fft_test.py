from visad.python.JPythonMethods import *
data = load("b2rlc.nc")
data2 = data[2][0]
spectrum = data2[data2.length-1]

print spectrum.getType()

ft = fft(spectrum)

clearplot()
plot(ft, 1, 0, 0)
