from visad.python.JPythonMethods import *

# create an expandable list to put data in
values = []

# number of values to create
n = 16

# loop from 0 to n-1
for i in range(n):

  # compute the value at "i"
  values.append((n - 1) * i - i * i)

# create a VisAD field with the values
data = field(values)

# plot the field
plot(data)
