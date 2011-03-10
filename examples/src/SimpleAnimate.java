/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

// import needed classes
import visad.*;
import visad.java2d.DisplayImplJ2D;
import visad.data.netcdf.Plain;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SimpleAnimate {

  // type 'java SimpleAnimate' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    int step = 1000;
    if (args.length > 0) {
      try {
        step = Integer.parseInt(args[0]);
      }
      catch(NumberFormatException e) {
        step = 1000;
      }
    }
    if (step < 1) step = 1;

    // create a netCDF reader
    Plain plain = new Plain();

    // open a netCDF file containing an image sequence and adapt
    // it to a Field Data object
    Field image_sequence = null;
    try {
      image_sequence = (Field) plain.open("images.nc");
    }
    catch (IOException exc) {
      String s = "To run this example, the images.nc file must be "
        +"present in\nyour visad/examples directory."
        +"You can obtain this file from:\n"
        +"  ftp://ftp.ssec.wisc.edu/pub/visad-2.0/images.nc.Z";
      System.out.println(s);
      System.exit(0);
    }

    // create a DataReference for image sequence
    final DataReference image_ref = new DataReferenceImpl("image");
    image_ref.setData(image_sequence);

    // create a Display using Java3D
    // DisplayImpl display = new DisplayImplJ3D("image display");
    // create a Display using Java2D
    DisplayImpl display = new DisplayImplJ2D("image display");

    // extract the type of image and use
    // it to determine how images are displayed
    FunctionType image_sequence_type =
      (FunctionType) image_sequence.getType();
    FunctionType image_type =
      (FunctionType) image_sequence_type.getRange();
    RealTupleType domain_type = image_type.getDomain();
    // map image coordinates to display coordinates
    display.addMap(new ScalarMap((RealType) domain_type.getComponent(0),
                                 Display.XAxis));
    display.addMap(new ScalarMap((RealType) domain_type.getComponent(1),
                                 Display.YAxis));
    // map image brightness values to RGB (default is grey scale)
    display.addMap(new ScalarMap((RealType) image_type.getRange(),
                                 Display.RGB));
    RealType hour_type =
      (RealType) image_sequence_type.getDomain().getComponent(0);
    ScalarMap animation_map = new ScalarMap(hour_type, Display.Animation);
    display.addMap(animation_map);
    AnimationControl animation_control =
      (AnimationControl) animation_map.getControl();
    animation_control.setStep(step);
    animation_control.setOn(true);

    // link the Display to image_ref
    display.addReference(image_ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("SimpleAnimate VisAD Application");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    // add display to JPanel
    panel.add(display.getComponent());

    // set size of JFrame and make it visible
    frame.setSize(500, 500);
    frame.setVisible(true);
  }
}

