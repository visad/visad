import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.java3d.DisplayRendererJ3D;
import visad.java3d.DirectManipulationRendererJ3D;

import visad.util.*;  
import visad.*;

import java.awt.*;
import java.awt.event.*;

// GUI handling
import com.sun.java.swing.*;
import com.sun.java.swing.border.*; 

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import visad.FlatField;
import visad.VisADException;

import visad.data.mcidas.*;
import visad.data.mcidas.AreaAdapter;
import visad.data.mcidas.AreaForm;

public class TestArea {

  public static void main(String args[])
  {
    if (args.length == 0) {
      args = new String[1];

      args[0] = "AREA0001";
      args[1] = "1";
    }
    FlatField imaget = null;

      System.out.println("Testing \"" + args[0] + "\"");

      AreaAdapter aa = null;

      try {
	aa = new AreaAdapter(args[0]);
      } catch (IOException e) {
	System.err.println("Caught IOException for \"" + args[0] + "\": " +
			   e.getMessage());
      } catch (VisADException e) {
	System.err.println("Caught VisADException for \"" + args[0] + "\": " +
			   e.getMessage());
      }

      imaget = aa.getData();
      if (imaget == null) {
	System.out.println("\tNULL FlatField!");
      } else {
	System.out.println("\t" + imaget.getType());
      }

      int bandNumber = Integer.parseInt(args[1].trim());

  try {

    // AreaForm af = new AreaForm();
    // imaget = (FlatField) af.open("AREA0001");
    DisplayImpl display = new DisplayImplJ3D("display1");
    // compute scalar maps
    FunctionType ftype = (FunctionType) imaget.getType();

    RealTupleType dtype = ftype.getDomain();

    RealTupleType rtype = (RealTupleType)ftype.getRange();

    display.addMap(new ScalarMap( (RealType) dtype.getComponent(0),
	    Display.XAxis) );
    display.addMap(new ScalarMap( (RealType) dtype.getComponent(1),
	    Display.YAxis) );

    // select which band to show...
    display.addMap(new ScalarMap( (RealType) rtype.getComponent(bandNumber),
	    Display.RGB) );

    DataReferenceImpl ref_image = new DataReferenceImpl("ref_image");
    ref_image.setData(imaget);
    display.addReference(ref_image,null);

    JFrame jframe = new JFrame("McIDAS AREA in Java 3D");
    jframe.addWindowListener(
      new WindowAdapter() {
	public void windowClosing(WindowEvent e) {System.exit(0);}
      }
    );

    jframe.setContentPane( (JPanel) display.getComponent() );
    jframe.setSize(512,512);
    jframe.setVisible(true);
  } catch (Exception xxx) {System.out.println("Ex: "+xxx);System.exit(1); }

  while (true) {
    try { 
      Thread.sleep(5000);
    } catch (Exception e) {System.exit(0);}
  }

}

}
