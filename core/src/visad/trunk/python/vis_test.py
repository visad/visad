from visad.python.JPythonMethods import *
"""\
vis_test.py

An example of a JPython script that utilizes
VisAD functionality.

To execute at the command prompt, type:
  jpython vis_test.py

To execute within the JPython editor, launch
the editor with:
  java visad.python.JPythonFrame
Then open this file and choose "Command", "Run"
"""

# load a GIF image file
data = load("../ss/cut.gif")

# plot the GIF image
clearplot()
plot(data)

