from visad.python.JPythonMethods import read
from visad.python.JPythonMethods import plot
"""\
vis_test.py
"""

"""\
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
"""

"""\
An example of a JPython script that utilizes VisAD functionality.

To execute at the command prompt, type:
  jpython vis_test.py

To execute within the JPython editor, launch the editor with:
  java visad.python.JPythonFrame
Then open this file and choose "Command", "Run"
"""

data = read("C:/java/visad/ss/cut.gif")
plot(data)
