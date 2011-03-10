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
import visad.util.DataUtility;
import visad.data.netcdf.Plain;
import java.rmi.RemoteException;
import java.io.IOException;
import javax.swing.*;

public class VerySimple {

  // type 'java VerySimple' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    // create a netCDF reader
    Plain plain = new Plain();

    // read an image sequence from a netCDF file into a data object
    DataImpl image_sequence = plain.open("images.nc");

    // create a display for the image sequence
    DisplayImpl display = DataUtility.makeSimpleDisplay(image_sequence);

    // start animation
    AnimationControl control =
      (AnimationControl) display.getControl(AnimationControl.class);
    control.setOn(true);

    // create JFrame (i.e., a window) for the display
    JFrame frame = new JFrame("VerySimple VisAD Application");

    // link the display to the JFrame
    frame.getContentPane().add(display.getComponent());

    // set the size of the JFrame and make it visible
    frame.setSize(400, 400);
    frame.setVisible(true);
  }
}

