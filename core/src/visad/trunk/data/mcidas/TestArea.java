import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.java3d.DefaultRendererJ3D;
import visad.java3d.DisplayRendererJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.Set;

import visad.util.*;  
import visad.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

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
import visad.data.mcidas.MapFile;
import visad.data.mcidas.BaseMapAdapter;

public class TestArea {

  public static void main(String args[])
  {
    if (args.length == 0) {
      args = new String[2];

      args[0] = "AREA0001";
      args[1] = "1";
    }
    if (args.length == 1) {
      String a = args[0];
      args = new String[2];
      args[0] = a;
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

  System.out.println("aa="+aa);

  FunctionType ft = (FunctionType) imaget.getType();
  System.out.println("ft="+ft);
  RealTupleType idom = ft.getDomain();
  System.out.println("idom="+idom);
  CoordinateSystem cs = idom.getCoordinateSystem();
  System.out.println("cs="+cs);

  Set ffds= imaget.getDomainSet();

  System.out.println("image.getDomainSet()="+ffds );

  CoordinateSystem dics = aa.getCoordinateSystem();
  System.out.println("dics="+dics);

  int[] dim = aa.getDimensions();
  int numEles = dim[1];
  int numLines = dim[2];

  try {

    BaseMapAdapter bma = new BaseMapAdapter("OUTLUSAM");
    bma.setCoordinateSystem(dics, numEles, numLines, idom);

    DataReference maplines_ref =  new DataReferenceImpl("MapLines");
    maplines_ref.setData(bma.getData() );



    // AreaForm af = new AreaForm();
    // imaget = (FlatField) af.open("AREA0001");

    DisplayImpl display = new DisplayImplJ3D("display1", 
       new TwoDDisplayRendererJ3D() );

    FunctionType ftype = (FunctionType) imaget.getType();
    RealTupleType dtype = ftype.getDomain();
    RealTupleType rtype = (RealTupleType)ftype.getRange();

    ScalarMap xaxis = new ScalarMap( (RealType) dtype.getComponent(0), 
	    Display.XAxis);
    xaxis.setRange( 0.d, (double) numEles);
    ScalarMap yaxis = new ScalarMap( (RealType) dtype.getComponent(1), 
	    Display.YAxis);
    yaxis.setRange( 0.d, (double) numLines);

    display.addMap(xaxis);
    display.addMap(yaxis);

    // select which band to show...
    display.addMap(new ScalarMap( (RealType) rtype.getComponent(bandNumber-1),
	    Display.RGB) );

    DataReferenceImpl ref_image = new DataReferenceImpl("ref_image");
    ref_image.setData(imaget);
    display.addReference(ref_image,null);

    // display.addReference(maplines_ref);

    ConstantMap[] redMap = { new ConstantMap(0., Display.Blue),
                        new ConstantMap(1., Display.Red),
                        new ConstantMap(0., Display.Green) };
			/*
                        new ConstantMap(0., Display.Green),
			new ConstantMap(.01, Display.ZAxis) };
			*/

    display.addReferences( new DefaultRendererJ3D(), maplines_ref, redMap);

    JFrame jframe = new JFrame("McIDAS AREA in Java 3D");
    jframe.addWindowListener(
      new WindowAdapter() {
	public void windowClosing(WindowEvent e) {System.exit(0);}
      }
    );

    jframe.setContentPane( (JPanel) display.getComponent() );
    jframe.setSize(numEles,numLines);
    jframe.setVisible(true);
  } catch (Exception xxx) {System.out.println("Ex: "+xxx);System.exit(1); }

  while (true) {
    try { 
      Thread.sleep(5000);
    } catch (Exception e) {System.exit(0);}
  }

}

}
