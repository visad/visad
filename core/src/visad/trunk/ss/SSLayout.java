
//
// SSLayout.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.ss;

// JFC packages
import com.sun.java.swing.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/** SSLayout is the layout manager for the SpreadSheet's cells
    and their labels.  It sets up components in a rectangular
    grid similar to GridLayout, but uses the components'
    getPreferredSize methods to allow for variable-sized cells.<P> */
public class SSLayout implements LayoutManager {

  /** number of columns components should form */
  private int NumCols;

  /** number of rows components should form */
  private int NumRows;

  /** minimum width of a column */
  private int MinColW;

  /** minimum height of a row */
  private int MinRowH;

  /** space between columns */
  private int ColSpace;

  /** space between rows */
  private int RowSpace;

  /** constructor */
  public SSLayout(int ncol, int nrow, int mwidth, int mheight,
                  int wspace, int hspace) {
    NumCols = ncol;
    NumRows = nrow;
    MinColW = mwidth;
    MinRowH = mheight;
    ColSpace = wspace;
    RowSpace = hspace;
  }

  /** heart of the layout manager--does all the work */
  public void layoutContainer(Container parent) {
    // get parent's current width and height
    int curW = parent.getSize().width - (NumCols - 1) * ColSpace;
    int curH = parent.getSize().height - (NumRows - 1) * RowSpace;

    // get parent's components
    Component[] c = parent.getComponents();
    if (c.length != NumCols*NumRows) {
      throw new Error("wrong number of components!");
    }

    // get preferred widths and total width
    double[] pw = new double[NumCols];
    double totalW = 0.0;
    for (int i=0; i<NumCols; i++) {
      pw[i] = (double) c[i].getPreferredSize().width;
      totalW += pw[i];
    }

    // compute real widths
    int[] rw = new int[NumCols];
    for (int i=0; i<NumCols; i++) {
      rw[i] = (int) (pw[i]/totalW*curW);
      if (rw[i] < MinColW) rw[i] = MinColW;
    }

    // get preferred heights and total height
    double[] ph = new double[NumRows];
    double totalH = 0.0;
    for (int i=0; i<NumRows; i++) {
      ph[i] = (double) c[NumCols*i].getPreferredSize().height;
      totalH += ph[i];
    }

    // compute real heights
    int[] rh = new int[NumRows];
    for (int i=0; i<NumRows; i++) {
      rh[i] = (int) (ph[i]/totalH*curH);
      if (rh[i] < MinRowH) rh[i] = MinRowH;
    }

    // set widths and heights of all components
    int sy = 0;
    for (int i=0; i<NumRows; i++) {
      int rhy = rh[i];
      int sx = 0;
      for (int j=0; j<NumCols; j++) {
        int rwx = rw[j];
        c[i*NumCols+j].setBounds(sx, sy, rwx, rhy);
        sx += rwx + ColSpace;
      }
      sy += rhy + RowSpace;
    }
  }

  /** returns minimum layout size */
  public Dimension minimumLayoutSize(Container parent) {
    return new Dimension(NumCols * (MinColW + ColSpace) - ColSpace,
                         NumRows * (MinRowH + RowSpace) - RowSpace);
  }

  /** returns preferred layout size */
  public Dimension preferredLayoutSize(Container parent) {
    Component[] c = parent.getComponents();
    if (c.length != NumCols*NumRows) {
      throw new Error("wrong number of components!");
    }

    // get preferred total width
    int totalW = -ColSpace;
    for (int i=0; i<NumCols; i++) {
      totalW += c[i].getPreferredSize().width; // + ColSpace;
    }

    // get preferred total height
    int totalH = 0; // -RowSpace;
    for (int i=0; i<NumRows; i++) {
      totalH += c[NumCols*i].getPreferredSize().height; // + RowSpace;
    }

    // preferred size should be at least minimum size
    Dimension minSize = minimumLayoutSize(parent);
    if (totalW < minSize.width) totalW = minSize.width;
    if (totalH < minSize.height) totalH = minSize.height;

    return new Dimension(totalW, totalH);
  }

  /** not used by SSLayout */
  public void addLayoutComponent(String name, Component comp) { }

  /** not used by SSLayout */
  public void removeLayoutComponent(Component comp) { }

  /* run 'java visad.ss.SSLayout' to test the SSLayout class */
  public static void main(String[] argv) {
    JFrame f = new JFrame("SSLayout test");
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    JPanel p = new JPanel();
    f.setContentPane(p);
    p.setLayout(new SSLayout(3, 4, 200, 50, 5, 15));
    p.add(new JButton("Button01"));
    p.add(new JButton("Button02"));
    p.add(new JButton("Button03"));
    p.add(new JButton("Button04"));
    p.add(new JButton("Button05"));
    p.add(new JButton("Button06"));
    p.add(new JButton("Button07"));
    p.add(new JButton("Button08"));
    p.add(new JButton("Button09"));
    p.add(new JButton("Button10"));
    p.add(new JButton("Button11"));
    p.add(new JButton("Button12"));
    f.pack();
    f.show();
  }

}

