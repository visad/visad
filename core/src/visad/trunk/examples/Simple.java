// import needed classes
import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.util.VisADSlider;
import visad.data.netcdf.Plain;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.awt.swing.*;

public class Simple {

  // type 'java Simple' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    // create a DataReference for an 'hour' value
    final DataReference hour_ref = new DataReferenceImpl("hour");
    // and link it to a slider
    VisADSlider slider = new VisADSlider("hour", 0, 3, 0, 1.0, 
                                         hour_ref, new RealType("hour"));

    // create a DataReference for an image
    final DataReference image_ref = new DataReferenceImpl("image");

    // create a netCDF reader
    Plain plain = new Plain();

    // open a netCDF file containing an image sequence and adapt
    // it to a Field Data object
    final Field image_sequence = (Field) plain.open("images.nc");

    // create a Cell to extract an image at 'hour'
    // (this is an anonymous inner class extending CellImpl)
    Cell cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        // extract image from sequence by evaluating image_sequence
        // Field at 'hour' value
        image_ref.setData(image_sequence.evaluate(
                                       (Real) hour_ref.getData()));
      }
    };
    // link cell to hour_ref to trigger doAction whenever
    // 'hour' value changes
    cell.addReference(hour_ref);
 
    // create a Display and add it to panel
    DisplayImpl display = new DisplayImplJ3D("image display");

    // extract the type of image and use
    // it to determine how images are displayed
    FunctionType image_type = (FunctionType)
      ((FunctionType) image_sequence.getType()).getRange();
    RealTupleType domain_type = image_type.getDomain();
    // map image coordinates to display coordinates
    display.addMap(new ScalarMap((RealType) domain_type.getComponent(0),
                                 Display.XAxis));
    display.addMap(new ScalarMap((RealType) domain_type.getComponent(1),
                                 Display.YAxis));
    // map image brightness values to RGB (default is grey scale)
    display.addMap(new ScalarMap((RealType) image_type.getRange(),
                                 Display.RGB));

    // link the Display to image_ref
    // display will update whenever image changes
    display.addReference(image_ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("Simple VisAD Application");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
 
    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);
 
    // add slider and display to JPanel
    panel.add(slider);
    panel.add(display.getComponent());
 
    // set size of JFrame and make it visible
    frame.setSize(500, 600);
    frame.setVisible(true);
  }
}

