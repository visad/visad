//
// CodeEditor.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import visad.VisADException;

/** An editor for writing and executing source code in Java runtime. */
public abstract class CodeEditor extends TextEditor {

  /** text pane containing line numbers */
  protected JTextArea lineNumbers;

  /** number of lines of code in the document */
  protected int numLines;

  /** number of digits in lines of code in the document */
  protected int numDigits;

  /** constructs a CodeEditor */
  public CodeEditor() {
    this(null);
  }

  /** constructs a CodeEditor containing text from the given filename */
  public CodeEditor(String filename) {
    super(filename);

    // set up line numbers
    final int fontWidth = getFontMetrics(MONO).stringWidth(" ");
    lineNumbers = new JTextArea("1") {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = numDigits * fontWidth;
        return d;
      }
    };
    lineNumbers.setEditable(false);
    lineNumbers.setFont(MONO);
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
    setRowHeaderView(p);
  }

  /** highlights the given line of code in the document */
  public void highlightLine(int line) {
    int start = 0;
    String s = text.getText() + "\n";
    for (int i=0; i<line-1; i++) start = s.indexOf("\n", start) + 1;
    int end = s.indexOf("\n", start);
    text.requestFocus();
    text.setCaretPosition(start);
    text.moveCaretPosition(end);
  }

  /** executes the source code in Java runtime */
  public abstract void run() throws VisADException;

  /** compiles the source code to a Java class */
  public abstract void compile() throws VisADException;

  /** executes the given line of code immediately in Java runtime */
  public abstract void exec(String line) throws VisADException;

  /** updates line numbers to match document text */
  protected void updateLineNumbers() {
    String s = text.getText();
    int len = s.length();
    int count = 1;

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

  /** undoes the last edit */
  public void undo() throws CannotUndoException {
    super.undo();
    updateLineNumbers();
  }

  /** redoes the last undone edit */
  public void redo() throws CannotRedoException {
    super.redo();
    updateLineNumbers();
  }

  /** updates line numbers when undoable action occurs */
  public void undoableEditHappened(UndoableEditEvent e) {
    super.undoableEditHappened(e);
    if (!e.getEdit().isSignificant()) return;
    updateLineNumbers();
  }

}
