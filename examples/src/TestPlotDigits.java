//
// TestPlotDigits.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import visad.*;

/**
   TestPlotDigits calculates an array of points to be plotted to
   the screen as vector pairs, given a number and a bounding
   rectangle, for use as a label on a contour R^2.<P>

   It is implemented as an applet so that it can be
   tested graphically with the appletviewer utility.<P>
*/
public class TestPlotDigits extends JPanel implements MouseListener {

  // Variables
  protected PlotDigits plot;
  protected int reverseLetters = 0;
  protected int width, height;

  /* run 'java TestPlotDigits' to test the PlotDigits class. */
  public static void main(String[] args) {
    if (args.length < 3) args = new String[] {"-73.81", "600", "200"};
    float number = (float) Double.parseDouble(args[0]);
    int width = Integer.parseInt(args[1]);
    int height = Integer.parseInt(args[2]);

    JFrame frame = new JFrame("PlotDigits test window");
    frame.setContentPane(new TestPlotDigits(number, width, height));
    frame.setBounds(100, 100, width, height);
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    frame.show();
  }

  public TestPlotDigits(float number, int width, int height) {
    addMouseListener(this);
    plot = new PlotDigits();
    plot.Number = number;
    this.width = width;
    this.height = height;
    try {
      boolean[] swap = {false, false, false};
      plot.plotdigits(plot.Number, 0, 0, height, 7*width/8, 150, swap);
    }
    catch (VisADException VE) {
      System.out.println("TestPlotDigits: "+VE);
      System.exit(1);
    }
  }

  public void mouseClicked(MouseEvent e) {
    reverseLetters = (reverseLetters+1)%4;
    Graphics g = getGraphics();
    if (g != null) {
      paint(g);
      g.dispose();
    }
  }

  public void mousePressed(MouseEvent e) {;}
  public void mouseReleased(MouseEvent e) {;}
  public void mouseEntered(MouseEvent e) {;}
  public void mouseExited(MouseEvent e) {;}

  public void paint(Graphics g) {
    g.setColor(Color.white);
    g.fillRect(0, 0, width, height);
    g.setColor(Color.black);
    for (int i=0; i<plot.NumVerts; i+=2) {
      int v1, v2, v3, v4;
      if (reverseLetters%2 == 1) { // y is backwards
        v1 = (int) plot.VyB[i];
        v3 = (int) plot.VyB[(i+1)%plot.NumVerts];
      }
      else {
        v1 = (int) plot.Vy[i];
        v3 = (int) plot.Vy[(i+1)%plot.NumVerts];
      }
      if (reverseLetters > 1) { // x is backwards
        v2 = (int) plot.VxB[i];
        v4 = (int) plot.VxB[(i+1)%plot.NumVerts];
      }
      else {
        v2 = (int) plot.Vx[i];
        v4 = (int) plot.Vx[(i+1)%plot.NumVerts];
      }
      g.drawLine(v1, v2, v3, v4);
    }
  }

}

