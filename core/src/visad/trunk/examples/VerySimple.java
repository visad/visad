// import needed classes
import visad.*;
import visad.util.DataUtility;
import visad.java3d.DisplayImplJ3D;
import visad.data.netcdf.Plain;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
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

    // create JFrame (i.e., a window) for the display
    JFrame frame = new JFrame("VerySimple VisAD Application");
 
    // link the display to the JFrame
    frame.getContentPane().add(display.getComponent());
 
    // set the size of the JFrame and make it visible
    frame.setSize(400, 400);
    frame.setVisible(true);
  }
}

