from visad.python.JPythonMethods import *
# load a netCDF file containg a NAST-I spectrum
data = load("../examples/b2rlc.nc")
# extract the spectrum
data2 = data[2][0]
spectrum = data2[data2.length-1]

# print the VisAD MathType of the spectrum
print spectrum.getType()

# compute the Fourier transform of the spectrum
ft = fft(spectrum)

# plot the Fourier transform in red = 1, green = 0, blue = 0
# i.e., red
clearplot()
plot(ft, 1, 0, 0)

