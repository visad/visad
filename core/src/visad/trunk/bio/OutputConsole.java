//
// OutputConsole.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import visad.util.Util;

/**
 * OutputConsole provides an output stream that pipes to a JTextArea
 * Swing component in its own frame (instead of to a console window).
 */
public class OutputConsole extends OutputStream {

  // -- CONSTANTS --

  /** Monospaced font. */
  private static final Font MONO = new Font("Monospaced", Font.PLAIN, 11);


  // -- FIELDS --

  private JFrame frame;
  private Document doc;
  private JTextArea area;


  // -- CONSTRUCTOR --

  /** Constructs a new instance of OutputConsole. */
  public OutputConsole(String title) {
    super();
    frame = new JFrame(title);
    FontMetrics fm = frame.getFontMetrics(MONO);
    int width = 100 * fm.charWidth(' ');
    frame.setSize(width, 500);
    JPanel pane = new JPanel();
    pane.setLayout(new BorderLayout());
    frame.setContentPane(pane);
    area = new JTextArea();
    area.setEditable(false);
    area.setFont(MONO);
    area.setLineWrap(true);
    JScrollPane scroll = new JScrollPane(area);
    pane.add(scroll, BorderLayout.CENTER);
    doc = area.getDocument();
  }


  // -- API METHODS --

  public void write(int b) throws IOException {
    write(new byte[] {(byte) b}, 0, 1);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    final String s = new String(b, off, len);
    Util.invoke(false, new Runnable() {
      public void run() {
        try { doc.insertString(doc.getLength(), s, null); }
        catch (BadLocationException exc) { }
        if (!frame.isVisible()) show();
      }
    });
  }

  public void show() {
    Util.centerWindow(frame);
    frame.show();
  }

}
