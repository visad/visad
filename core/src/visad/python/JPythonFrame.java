//
// JPythonFrame.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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
*/

package visad.python;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.JCheckBoxMenuItem;
import visad.VisADException;
import visad.util.*;

/** A GUI frame for editing JPython code in Java runtime. */
public class JPythonFrame extends CodeFrame {

  /** flag for java shell availability */
  protected boolean canRunSeparate = true;

  /** menu item for running JPython in a separate process */
  protected JCheckBoxMenuItem separateProcess;

  /** constructs a JPythonFrame */
  public JPythonFrame() throws VisADException {
    this((String) null);
  }

  /** constructs a JPythonFrame containing text from the given filename */
  public JPythonFrame(String filename) throws VisADException {
    this(new JPythonEditor(filename));
  }

  /** constructs a JPythonFrame from the given JPythonEditor object */
  public JPythonFrame(JPythonEditor editor) throws VisADException {
    super(editor);
    ((JPythonEditor) textPane).setRunItem(getMenuItem("Command", "Run"));

    // determine external java shell availability
    try {
      JPythonEditor.runCommand("java visad.python.RunJPython");
    }
    catch (IOException exc) { canRunSeparate = false; }
    catch (VisADException exc) { canRunSeparate = false; }
    ((JPythonEditor) textPane).setRunSeparateProcess(false);

    // setup menu bar
    getMenuItem("Command", "Compile").setEnabled(false);
    addMenuSeparator("Command");
    separateProcess = new JCheckBoxMenuItem(
      "Launch JPython in a separate process", false);
    addMenuItem("Command", separateProcess,
      "commandSeparate", 'l', canRunSeparate);
  }

  /** sets up the GUI */
  public void commandSeparate() {
    boolean separate = separateProcess.getState();
    ((JPythonEditor) textPane).setRunSeparateProcess(separate);
  }

  /** sets whether editor should warn user before auto-saving */
  public void setWarnBeforeSave(boolean warn) {
    ((JPythonEditor) textPane).setWarnBeforeSave(warn);
  }

  /** tests the JPythonFrame class */
  public static void main(String[] args) {
    boolean warn = true;
    if (args.length > 0) {
      if ("-nowarn".equalsIgnoreCase(args[0])) warn = false;
    }
    try {
      final JPythonFrame frame = new JPythonFrame();
      frame.setWarnBeforeSave(warn);

      // close program if frame is closed
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          frame.fileExit();
        }
      });

      // display frame onscreen
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int w = screenSize.width > 550 ? 500 : screenSize.width - 50;
      int h = screenSize.height > 850 ? 800 : screenSize.height - 50;
      frame.setSize(w, h);
      Util.centerWindow(frame);
      frame.setVisible(true);
    }
    catch (VisADException exc) {
      exc.printStackTrace();
    }
  }

}
