//
// SSLayout.java
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

package visad.ss;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** SSLayout is the layout manager for the SpreadSheet's cells
    and their labels.  It sets up components in a rectangular
    grid similar to GridLayout, but uses the components'
    getPreferredSize methods to allow for variable-sized cells.<P> */
public class SSLayout implements LayoutManager {

  /** number of columns components should form */
  private int NumCols;

  /** number of rows components should form */
  private int NumRows;

  /** space between columns */
  private int ColSpace;

  /** space between rows */
  private int RowSpace;

  /** constructor */
  public SSLayout(int ncol, int nrow, int wspace, int hspace) {
    NumCols = ncol;
    NumRows = nrow;
    ColSpace = wspace;
    RowSpace = hspace;
  }

  /** add the necessary number of elements to the Component array */
  private Component[] fillOut(Component[] c) {
    // warn the user
    System.err.println("Warning: spreadsheet cell layout is corrupted");

    // add blank components to the layout
    Component[] nc = new Component[NumCols * NumRows];
    System.arraycopy(c, 0, nc, 0, c.length);
    for (int i=c.length; i<nc.length; i++) {
      nc[i] = new JComponent() {
        public void paint(Graphics g) { }
      };
    }
    return nc;
  }

  /** lay out the components */
  public void layoutContainer(Container parent) {
    // get parent's components
    Component[] c = parent.getComponents();
    if (c.length < NumCols * NumRows) c = fillOut(c);

    // get preferred widths
    int[] pw = new int[NumCols];
    for (int i=0; i<NumCols; i++) {
      pw[i] = c[i].getPreferredSize().width;
    }

    // get preferred heights
    int[] ph = new int[NumRows];
    for (int j=0; j<NumRows; j++) {
      ph[j] = c[NumCols * j].getPreferredSize().height;
    }

    // lay out all components
    int sy = 0;
    for (int j=0; j<NumRows; j++) {
      int sx = 0;
      for (int i=0; i<NumCols; i++) {
        c[NumCols * j + i].setBounds(sx, sy, pw[i], ph[j]);
        sx += pw[i] + ColSpace;
      }
      sy += ph[j] + RowSpace;
    }
  }

  /** return minimum layout size */
  public Dimension minimumLayoutSize(Container parent) {
    return preferredLayoutSize(parent);
  }

  /** return preferred layout size */
  public Dimension preferredLayoutSize(Container parent) {
    // get parent's components
    Component[] c = parent.getComponents();
    if (c.length < NumCols * NumRows) c = fillOut(c);

    // get preferred widths total
    int pwt = -ColSpace;
    for (int i=0; i<NumCols; i++) {
      pwt += c[i].getPreferredSize().width + ColSpace;
    }

    // get preferred heights total
    int pht = -RowSpace;
    for (int j=0; j<NumRows; j++) {
      pht += c[NumCols * j].getPreferredSize().height + RowSpace;
    }

    // return final layout size
    return new Dimension(pwt, pht);
  }

  /** not used by SSLayout */
  public void addLayoutComponent(String name, Component comp) { }

  /** not used by SSLayout */
  public void removeLayoutComponent(Component comp) { }

  /** test the SSLayout layout manager class */
  public static void main(String[] argv) {
    JFrame f = new JFrame("SSLayout test");
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    JPanel p = new JPanel();
    f.setContentPane(p);
    p.setLayout(new SSLayout(3, 4, 5, 15));
    for (int i=1; i<=12; i++) {
      String s = "" + i;
      if (i < 10) s = "0" + s;
      p.add(new JButton("Button" + s));
    }
    f.pack();
    f.show();
  }

}

