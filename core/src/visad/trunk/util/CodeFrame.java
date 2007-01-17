//
// CodeFrame.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.awt.event.*;
import javax.swing.*;
import visad.VisADException;
import java.util.Vector;

/** A GUI frame for editing source code in Java runtime. */
public class CodeFrame extends TextFrame {

  /** constructs a CodeFrame from the given CodeEditor object */
  public CodeFrame(CodeEditor editor) {
    super(editor);

    // setup menu bar
    addMenuItem("Command", "Run", "commandRun", 'r');
    addMenuItem("Command", "Compile", "commandCompile", 'c');
  }

  /** sets up the GUI */
  protected void layoutGUI() {
    // set up content pane
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    setContentPane(pane);

    // set up immediate text field
    JPanel immediate = new JPanel();
    immediate.setLayout(new BoxLayout(immediate, BoxLayout.X_AXIS));
    final JTextField textLine = new JTextField();
    Util.adjustTextField(textLine);
    textLine.setToolTipText(
      "Enter a command and press enter to execute it immediately; up/down arrows recall commands");
    final CodeEditor fTextPane = (CodeEditor) textPane;
    final Vector stack = new Vector();
    stack.addElement(new Integer(-1)); // first element is pointer
    textLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          String cmd = textLine.getText();
          fTextPane.exec(cmd);
          int sp = ((Integer)(stack.elementAt(0))).intValue();
          if (sp < 0 || sp >= stack.size() ||
            !cmd.equals( (String)stack.elementAt(sp) ) ) {
            stack.addElement(textLine.getText());
            stack.setElementAt(new Integer(stack.size()),0);
          }
          textLine.setText("");
        }
        catch (VisADException exc) {
          showError(exc.getMessage());
        }
      }
    });

    textLine.addKeyListener(new KeyListener() {
      public void keyReleased(KeyEvent e) {;}
      public void keyTyped(KeyEvent e) {;}
      public void keyPressed(KeyEvent e) {
        int spoint = ((Integer)(stack.elementAt(0))).intValue();
        if (spoint < 0 ) return;

        int kc = e.getKeyCode();
        if (kc == KeyEvent.VK_UP) {
          spoint = spoint - 1;
          if (spoint < 1) spoint = stack.size() - 1;
          textLine.setText( (String)stack.elementAt(spoint));
          stack.setElementAt(new Integer(spoint), 0);

        } else if (kc == KeyEvent.VK_DOWN) {
          spoint = spoint + 1;
          if (spoint >= stack.size()) spoint = 1;
          textLine.setText( (String)stack.elementAt(spoint));
          stack.setElementAt(new Integer(spoint), 0);
        }
        
      }
    });

    immediate.add(new JLabel("Immediate: "));
    immediate.add(textLine);

    // finish GUI setup
    pane.add(textPane);
    pane.add(immediate);
    setTitle("VisAD Source Code Editor");
  }

  final

  public void commandRun() {
    try {
      ((CodeEditor) textPane).run();
    }
    catch (VisADException exc) {
      showError(exc.getMessage());
    }
  }

  public void commandCompile() {
    try {
      ((CodeEditor) textPane).compile();
    }
    catch (VisADException exc) {
      showError(exc.getMessage());
    }
  }

}
