from visad.python.JPythonMethods import *
matrix = field([[1, 2], [1, 3]])
vector = field([2, 1])
solution = solve(matrix, vector)
print solution[0], solution[1]

# prints 4.0 -1.0
#
# note
# 1  2       4       2
#        *       =
# 1  3      -1       1
