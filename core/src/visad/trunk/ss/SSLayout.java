
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

  /** whether this layout manager should use added "label logic" */
  private boolean Labels;

  /** constructor */
  public SSLayout(int ncol, int nrow, int mwidth, int mheight,
                  int wspace, int hspace, boolean labels) {
    NumCols = ncol;
    NumRows = nrow;
    MinColW = mwidth;
    MinRowH = mheight;
    ColSpace = wspace;
    RowSpace = hspace;
    Labels = labels;
  }

  /** heart of the layout manager--does all the work */
  public void layoutContainer(Container parent) {
    // get parent's current width and height
    int curW = parent.getSize().width - (NumCols - 1) * ColSpace;
    int curH = parent.getSize().height - (NumRows - 1) * RowSpace;

    // work-around for ScrollPane bug
    if (!Labels) {
      curW--;
      curH--;
    }

    // get parent's components
    Component[] c = parent.getComponents();
    if (c.length != NumCols*NumRows) {
      throw new Error("wrong number of components!");
    }

    // get preferred widths and total width
    int[] pw = new int[NumCols];
    double totalW = 0.0;
    int unusedW = 0;
    for (int i=0; i<NumCols; i++) {
      pw[i] = c[i].getPreferredSize().width;
      if (i % 2 == 0 || !Labels) totalW += pw[i];
      else unusedW += pw[i];
    }

    // compute real widths
    int[] rw = new int[NumCols];
    for (int i=0; i<NumCols; i++) {
      if (i % 2 == 0 || !Labels) {
        rw[i] = (int) (pw[i] / totalW * (curW - unusedW));
        if (rw[i] < MinColW) rw[i] = MinColW;
      }
      else rw[i] = pw[i];
    }

    // get preferred heights and total height
    int[] ph = new int[NumRows];
    double totalH = 0.0;
    int unusedH = 0;
    for (int i=0; i<NumRows; i++) {
      ph[i] = c[NumCols*i].getPreferredSize().height;
      if (i % 2 == 0 || !Labels) totalH += ph[i];
      else unusedH += ph[i];
    }

    // compute real heights
    int[] rh = new int[NumRows];
    for (int i=0; i<NumRows; i++) {
      if (i % 2 == 0 || !Labels) {
        rh[i] = (int) (ph[i] / totalH * (curH - unusedH));
        if (rh[i] < MinRowH) rh[i] = MinRowH;
      }
      else rh[i] = ph[i];
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
    if (Labels) {
      // get array of components
      Component[] c = parent.getComponents();
      if (c.length != NumCols*NumRows) {
        throw new Error("wrong number of components!");
      }

      // get total minimum width
      int minW = -ColSpace;
      for (int i=0; i<NumCols; i++) {
        minW += ColSpace;
        int mw = c[i].getMinimumSize().width;
        minW += (mw < MinColW && i % 2 == 0) ? MinColW : mw;
      }

      // get total minimum height
      int minH = -RowSpace;
      for (int i=0; i<NumRows; i++) {
        minH += RowSpace;
        int mh = c[NumCols*i].getMinimumSize().height;
        minH += (mh < MinRowH && i % 2 == 0) ? MinRowH : mh;
      }
      return new Dimension(minW, minH);
    }
    else return new Dimension(NumCols * (MinColW + ColSpace) - ColSpace,
                              NumRows * (MinRowH + RowSpace) - RowSpace);
  }

  /** returns preferred layout size */
  public Dimension preferredLayoutSize(Container parent) {
    Component[] c = parent.getComponents();
    if (c.length != NumCols*NumRows) {
      throw new Error("wrong number of components!");
    }

    // get preferred widths, and smallest preferred width
    int[] pw = new int[NumCols];
    int sw = 0;
    for (int i=0; i<NumCols; i++) {
      pw[i] = c[i].getPreferredSize().width;
      if ((i % 2 == 0 || !Labels) && pw[i] < pw[sw]) sw = i;
    }

    // compute preferred total width
    double scaleW = (double) MinColW / pw[sw];
    int[] rw = new int[NumCols];
    int prefW = -ColSpace;
    for (int i=0; i<NumCols; i++) {
      prefW += ColSpace;
      if (i % 2 == 0 || !Labels) prefW += scaleW * pw[i];
      else prefW += pw[i];
    }

    // get preferred heights, and smallest preferred height
    int[] ph = new int[NumRows];
    int sh = 0;
    for (int i=0; i<NumRows; i++) {
      ph[i] = c[NumCols*i].getPreferredSize().height;
      if ((i % 2 == 0 || !Labels) && ph[i] < ph[sh]) sh = i;
    }

    // compute preferred total height
    double scaleH = (double) MinRowH / ph[sh];
    int[] rh = new int[NumRows];
    int prefH = -RowSpace;
    for (int i=0; i<NumRows; i++) {
      prefH += RowSpace;
      if (i % 2 == 0 || !Labels) prefH += scaleH * ph[i];
      else prefH += ph[i];
    }

    return new Dimension(prefW, prefH);
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
    p.setLayout(new SSLayout(3, 4, 200, 50, 5, 15, false));
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

