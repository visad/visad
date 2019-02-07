/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.fits;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import java.awt.Cursor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;

import java.rmi.RemoteException;

import visad.ConstantMap;
import visad.Data;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.FunctionImpl;
import visad.FunctionType;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.Tuple;
import visad.VisADException;

import visad.data.DefaultFamily;

import visad.java3d.DisplayImplJ3D;

public class Spasm
{
  private Data data;

  // the width and height of the UI frame
  public static int WIDTH = 600;
  public static int HEIGHT = 600;

  public Spasm(String filename)
	throws VisADException, RemoteException, IOException
  {
    DefaultFamily dflt = new DefaultFamily("Default");

    Data data;
    try {
      data = dflt.open(filename);
    } catch (VisADException e) {
      e.printStackTrace();
      System.exit(1);
      return;
    }

    // throw away all but the object
    if (data instanceof Tuple) {
      boolean discard = false;

      Tuple t = (Tuple )data;
      try {
	int i = 0;
	Data d;

	do {
	  d = t.getComponent(i);
	  if (discard) {
	    System.err.println("Discarding VisAD Data object #" + i + ": " +
				d.getType());
	  } else if (d instanceof FunctionImpl) {
	    data = d;
	    discard = true;
	  }
	  i++;
	} while (d != null);
      } catch (VisADException e) {
      } catch (ArrayIndexOutOfBoundsException e) {
      }

      // leave some whitespace after any error message(s)
      if (discard) {
	System.err.println("");
      }
    }

    this.data = data;
  }

  public String toString()
  {
    try {
      return data.getType().toString();
    } catch (Exception e) {
      return e.getClass().toString() + ": " + e.getMessage();
    }
  }

  private void linkData(DisplayImpl display)
	throws VisADException, RemoteException
  {
    // compute ScalarMaps from type components
    FunctionType ftype = (FunctionType )data.getType();

    // get domain and domain dimensions
    RealTupleType dtype = ftype.getDomain();
    int dims = dtype.getDimension();

    // map domain to up to 3 dimensions
    display.addMap(new ScalarMap((RealType )dtype.getComponent(0),
				  Display.XAxis));
    if (dims > 1) {
      display.addMap(new ScalarMap((RealType )dtype.getComponent(1),
				    Display.YAxis));
      if (dims > 2) {
	display.addMap(new ScalarMap((RealType )dtype.getComponent(2),
				      Display.ZAxis));
      }
    }

    // set up colors
    display.addMap(new ConstantMap(0.5, Display.Red));
    display.addMap(new ConstantMap(0.0, Display.Blue));

    // get range values
    MathType rtype = ftype.getRange();
    RealType rg, rz;
    if (rtype instanceof RealType) {
      rg = rz = (RealType )rtype;
    } else if (rtype instanceof RealTupleType) {
      rg = (RealType )((RealTupleType )rtype).getComponent(0);
      if (((RealTupleType )rtype).getDimension() > 1) {
	rz = (RealType )((RealTupleType )rtype).getComponent(1);
      } else {
	rz = rg;
      }
    } else {
      rg = rz = null;
    }

    // map range values to green
    if (rg != null) {
      display.addMap(new ScalarMap(rg, Display.Green));
    }

    // if Z axes isn't used yet, use it for range values
    if (dims <= 2 && rz != null) {
      display.addMap(new ScalarMap(rz, Display.ZAxis));
    }

    System.out.println(data.getType());
    System.out.println(display);

    // point display at data
    DataReferenceImpl ref = new DataReferenceImpl("SpazData");
    ref.setData(data);
    display.addReference(ref, null);
  }

  public void showApp()
	throws VisADException, RemoteException
  {
    DisplayImplJ3D display = new DisplayImplJ3D("display",
						DisplayImplJ3D.APPLETFRAME);
    linkData(display);
  }

  private JFrame mainFrame(String frameName)
  {
    // create a JFrame
    JFrame frame = new JFrame(frameName);
    frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
				  System.exit(0);
				}
			    });

    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    return frame;
  }

  private JPanel textPanel()
  {
    // create text JPanel
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setAlignmentY(JPanel.TOP_ALIGNMENT);
    p.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    // construct JLabels
    // (JTextArea does not align in BoxLayout well, so use JLabels)
    p.add(new JLabel("Silly file viewer"));
    p.add(new JLabel("using VisAD  -  see:"));
    p.add(new JLabel("  "));
    p.add(new JLabel("  http://www.ssec.wisc.edu/~billh/visad.html"));
    p.add(new JLabel("  "));
    p.add(new JLabel("for more information about VisAD."));
    p.add(new JLabel("  "));

    return p;
  }

  private void showSwing(String frameName)
	throws VisADException, RemoteException
  {
    // construct Display 1 (using default DisplayRenderer);
    // the text name is used only for debugging
    DisplayImplJ3D display = new DisplayImplJ3D("display");
    linkData(display);

    JFrame frame = mainFrame(frameName);

    // create mainPanel JPanel in frame
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
    mainPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    mainPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(mainPanel);

    mainPanel.add(textPanel());

    // get Display panel
    JPanel displayPanel = (JPanel) display.getComponent();

    // make borders for Display and embed in mainPanel
    Border etchedBorder10 =
      new CompoundBorder(new EtchedBorder(),
                         new EmptyBorder(10, 10, 10, 10));
    displayPanel.setBorder(etchedBorder10);

    mainPanel.add(displayPanel);

    // make the JFrame visible
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String args[])
	throws VisADException, RemoteException, IOException
  {
    if (args.length != 1) {
      System.err.println("Usage: Spasm file");
      System.exit(1);
      return;
    }

    Spasm spaz = new Spasm(args[0]);

    try {
      System.out.println("Spasm: " + spaz);
    } catch (Exception e) {
      System.err.println(args[0] + " print threw " + e.getMessage());
      e.printStackTrace(System.err);
      System.exit(1);
      return;
    }

    spaz.showSwing("Spasm");
  }
}
