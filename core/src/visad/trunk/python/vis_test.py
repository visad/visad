from visad.python.JPythonMethods import plot
from visad.python.JPythonMethods import read
from visad.python.JPythonMethods import equals
from visad.python.JPythonMethods import getClass
"""\
vis_test.py
"""

"""\
An example of a JPython script that utilizes
VisAD functionality.

To execute at the command prompt, type:
  jpython vis_test.py

To execute within the JPython editor, launch
the editor with:
  java visad.python.JPythonFrame
Then open this file and choose "Command", "Run"
"""

data = read("../ss/cut.gif")
plot(data)
