from visad.python.JPythonMethods import *
# construct a 2 x 2 matrix in a VisAD Field
matrix = field([[1, 2], [1, 3]])
# construct a 2 vector in a VisAD Field
vector = field([2, 1])

# solve the linear system
solution = solve(matrix, vector)

# print the solution
print solution[0], solution[1]

# prints 4.0 -1.0
#
# note
# 1  2       4       2
#        *       =
# 1  3      -1       1

