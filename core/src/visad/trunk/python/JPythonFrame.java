//
// JPythonFrame.java
//

/*
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
*/

package visad.python;

import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import visad.VisADException;
import visad.ss.SpreadSheet;
import visad.util.TextFrame;

/** A GUI frame for editing JPython code in Java runtime. */
public class JPythonFrame extends TextFrame {

  /** constructs a JPythonFrame */
  public JPythonFrame() throws VisADException {
    this((String) null);
  }

  /** constructs a JPythonFrame containing text from the given filename */
  public JPythonFrame(String filename) throws VisADException {
    this(new JPythonEditor(filename));
  }

  /** constructs a JPythonFrame from the given JPythonEditor object */
  public JPythonFrame(JPythonEditor jpEdit) throws VisADException {
    super(jpEdit);

    // setup menu bar
    addMenuItem("Command", "Run", "commandRun", 'r');
    addMenuItem("Command", "Compile", "commandCompile", 'c');

    // add immediate text field
    JPanel immediate = new JPanel();
    immediate.setLayout(new BoxLayout(immediate, BoxLayout.X_AXIS));
    final JTextField textLine = new JTextField();
    SpreadSheet.adjustTextField(textLine);
    textLine.setToolTipText(
      "Enter a JPython command and press enter to execute it immediately");
    textLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          ((JPythonEditor) getTextPane()).exec(textLine.getText());
        }
        catch (VisADException exc) {
          showError(exc.getMessage());
        }
      }
    });
    immediate.add(new JLabel("Immediate: "));
    immediate.add(textLine);
    getContentPane().add(immediate);

    // change the title bar text
    setTitle("VisAD JPython Editor");
  }

  public void commandRun() {
    if (textPane.hasChanged()) {
      int ans = JOptionPane.showConfirmDialog(this,
        "A save is required before execution. Okay to save?",
        getTitle(), JOptionPane.YES_NO_OPTION);
      if (ans != JOptionPane.YES_OPTION) return;
      boolean success = fileSave();
      if (!success) return;
    }
    try {
      ((JPythonEditor) textPane).run();
    }
    catch (VisADException exc) {
      showError(exc.getMessage());
    }
  }

  public void commandCompile() {
    try {
      ((JPythonEditor) textPane).compile();
    }
    catch (VisADException exc) {
      showError(exc.getMessage());
    }
  }

  /** tests the JPythonFrame class */
  public static void main(String[] args) throws VisADException {
    final JPythonFrame frame = new JPythonFrame();

    // close program if frame is closed
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        frame.fileExit();
      }
    });

    // display frame onscreen
    frame.setBounds(100, 100, 500, 500);
    frame.setVisible(true);
  }

}
