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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import visad.VisADException;
import visad.ss.SpreadSheet;
import visad.util.*;

/** A GUI frame for editing JPython code in Java runtime. */
public class JPythonFrame extends TextFrame implements UndoableEditListener {

  /** text pane containing line numbers */
  protected JTextArea lineNumbers;

  /** number of lines of code in the document */
  protected int numLines;

  /** number of digits in lines of code in the document */
  protected int numDigits;

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
    textPane.addUndoableEditListener(this);

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

    // set up main text editor pane
    JPanel main = new JPanel();
    main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS));
    pane.add(main);

    // set up line numbers
    final int fontWidth = getFontMetrics(TextEditor.MONO).stringWidth(" ");
    lineNumbers = new JTextArea("1") {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = numDigits * fontWidth;
        return d;
      }
    };
    lineNumbers.setEditable(false);
    numLines = 1;
    numDigits = 1;
    JPanel p = new JPanel() {
      public void paint(Graphics g) {
        // paint a thin gray line down right-hand side of line numbering
        super.paint(g);
        Dimension d = getSize();
        int w = d.width - 5;
        int h = d.height - 1;
        g.setColor(Color.gray);
        g.drawLine(w, 0, w, h);
      }

      public Dimension getPreferredSize() {
        Dimension d = lineNumbers.getPreferredSize();
        d.width += 10;
        return d;
      }
    };
    p.setBackground(Color.white);
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.add(lineNumbers);
    textPane.setRowHeaderView(p);

    // set up immediate text field
    JPanel immediate = new JPanel();
    immediate.setLayout(new BoxLayout(immediate, BoxLayout.X_AXIS));
    final JTextField textLine = new JTextField();
    SpreadSheet.adjustTextField(textLine);
    textLine.setToolTipText(
      "Enter a JPython command and press enter to execute it immediately");
    final JPythonEditor fTextPane = (JPythonEditor) textPane;
    textLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          fTextPane.exec(textLine.getText());
        }
        catch (VisADException exc) {
          showError(exc.getMessage());
        }
      }
    });
    immediate.add(new JLabel("Immediate: "));
    immediate.add(textLine);
    pane.add(immediate);

    // finish GUI setup
    main.add(textPane);
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

  /** updates line numbers when undoable action occurs */
  public void undoableEditHappened(UndoableEditEvent e) {
    if (!e.getEdit().isSignificant()) return;

    // update line numbers
    String s = textPane.getText();
    //CTR: TEMP: if (!s.endsWith("\n")) s = s + "\n";
    int len = s.length();
    int count = 0;

    // count number of lines in document
    int index = s.indexOf("\n");
    while (index >= 0) {
      count++;
      index = s.indexOf("\n", index + 1);
    }
    if (count == numLines) return;
    
    // compute index into line numbers text string
    String l = lineNumbers.getText() + "\n";
    int digits = ("" + count).length();
    int spot = 0;
    int nine = 9;
    for (int i=2; i<=digits; i++) {
      spot += i * nine;
      nine *= 10;
    }
    int ten = nine / 9;
    spot += (digits + 1) * (count - ten + 1);

    // update line numbers text string
    int maxSpot = l.length();
    String newL;
    if (spot < maxSpot) {
      // eliminate extra line numbers
      newL = l.substring(0, spot - 1);
    }
    else {
      // append additional line numbers
      StringBuffer sb = new StringBuffer(spot);
      sb.append(l);
      for (int i=numLines+1; i<count; i++) {
        sb.append(i);
        sb.append("\n");
      }
      sb.append(count);
      newL = sb.toString();
    }
    numLines = count;
    numDigits = digits;
    lineNumbers.setText(newL);
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
