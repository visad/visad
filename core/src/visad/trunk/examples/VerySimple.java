// import needed classes
import visad.*;
import visad.util.DataUtility;
import visad.java3d.DisplayImplJ3D;
import visad.data.netcdf.Plain;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class VerySimple {

  // type 'java VerySimple' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    // create a netCDF reader
    Plain plain = new Plain();

    // open a netCDF file containing an image sequence and adapt
    // it to a Field Data object
    FieldImpl image_sequence = null;
    try {
      image_sequence = (FieldImpl) plain.open("images.nc");
    }
    catch (IOException exc) {
      String s = "To run this example, the images.nc file must be "
        +"present in\nyour visad/examples directory."
        +"You can obtain this file from:\n"
        +"  ftp://demedici.ssec.wisc.edu/pub/visad-2.0/images.nc.Z";
      System.out.println(s);
      System.exit(0);
    }

    DisplayImpl display = DataUtility.makeSimpleDisplay(image_sequence);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("VerySimple VisAD Application");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
 
    // add display to JFrame
    frame.getContentPane().add(display.getComponent());
 
    // set size of JFrame and make it visible
    frame.setSize(400, 400);
    frame.setVisible(true);
  }
}

