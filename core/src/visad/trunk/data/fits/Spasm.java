package visad.data.fits;

import com.sun.java.swing.BoxLayout;
import com.sun.java.swing.JFrame;
import com.sun.java.swing.JLabel;
import com.sun.java.swing.JPanel;

import com.sun.java.swing.border.Border;
import com.sun.java.swing.border.CompoundBorder;
import com.sun.java.swing.border.EmptyBorder;
import com.sun.java.swing.border.EtchedBorder;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import visad.ConstantMap;
import visad.Data;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.FunctionType;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.Tuple;
import visad.VisADException;

import visad.java3d.DisplayImplJ3D;

public class Spasm
{
  private Data fitsTuple;

  // the width and height of the UI frame
  public static int WIDTH = 600;
  public static int HEIGHT = 600;

  public Spasm(String filename)
	throws VisADException, RemoteException
  {
    FitsAdaptor fits = new FitsAdaptor(filename);

    Data[] fitsData;
    try {
      fitsData = fits.getData();
    } catch (ExceptionStack e) {
      System.err.println(filename + " getData threw " + e.getMessage());
      e.printStackTrace(System.err);

      fits.clearExceptionStack();
      fitsData = fits.getData();
    }

    fits = null;

    if (fitsData.length == 1) {
      fitsTuple = fitsData[0];
    } else {
      fitsTuple = new Tuple(fitsData);
    }
    fitsData = null;
  }

  public String toString()
  {
    try {
      return fitsTuple.getType().toString();
    } catch (Exception e) {
      return e.getClass().toString() + ": " + e.getMessage();
    }
  }

  private void linkData(DisplayImpl display)
	throws VisADException, RemoteException
  {
    // compute ScalarMaps from type components
    FunctionType ftype = (FunctionType )fitsTuple.getType();

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

    System.out.println(fitsTuple.getType());
    System.out.println(display);

    // point display at FITS data
    DataReferenceImpl ref = new DataReferenceImpl("FITS");
    ref.setData(fitsTuple);
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

    frame.setSize(WIDTH, HEIGHT);
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);

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
    p.add(new JLabel("Silly FITS file viewer"));
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
    frame.setVisible(true);
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    String testdir = "/home/dglo/prj/visad/data/fits/testdata/";
    String filename = testdir + "ngc1316o.fits";

    Spasm spaz = new Spasm(filename);

    try {
      System.out.println("Spasm: " + spaz);
    } catch (Exception e) {
      System.err.println(filename + " print threw " + e.getMessage());
      e.printStackTrace(System.err);
      System.exit(1);
      return;
    }

    spaz.showSwing("Spasm");
  }
}
