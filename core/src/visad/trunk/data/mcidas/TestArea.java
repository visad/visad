//
// TestArea.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.java3d.DefaultRendererJ3D;
import visad.java3d.DisplayRendererJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.Set;

import visad.util.*;  
import visad.DataReferenceImpl;
import visad.DataReference;
import visad.DisplayImpl;
import visad.FunctionType;
import visad.RealTupleType;
import visad.RealType;
import visad.Display;
import visad.ScalarMap;
import visad.ConstantMap;
import visad.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

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
import visad.data.mcidas.BaseMapAdapter;

/** This will test the Area File Adapter and Base Map (McIDAS
 *  formats) Adapter for VisAD.  You need a sample AREA file,
 *  along with a McIDAS "OUTL" format map file.  You may
 *  get these from ftp://allegro.ssec.wisc.edu/visad/
 *  
 *  At this poing, only GVAR navigation is supported, and no
 *  work has been done on calibration yet.
 *
 *  There is quite a bit of extra printout that is usually
 *  commented out, but may provide some insight into the
 *  Math and Data structures employed.
 *
 *  @author Tom Whittaker (SSEC)
 */
public class TestArea {

  public static void main(String args[]) {

    boolean use2D = false;
    String filename, band, mapfile;

    if (args.length == 0) {
      filename = "AREA0007";
      band = "1";
      mapfile = "OUTLUSAM";

      System.out.println("Usage: java TestArea <AREAfilename> <band#> <mapfilename>");
      System.out.println("          (filenames may also be URLs)");
      System.out.println("       -- using default: AREA0007 1 OUTLUSAM");

    } else if (args.length == 1) {
      filename = args[0];
      band = "1";
      mapfile = "OUTLUSAM";
    } else if (args.length == 2) {
      filename = args[0];
      band = args[1];
      mapfile = "OUTLUSAM";
    } else if (args.length == 3) {
      filename = args[0];
      band = args[1];
      mapfile = args[2];
    } else {
      System.out.println("Usage: java TestArea <AREAnnnn> <band#>");
      return;
    }

    FlatField imaget = null;

      System.out.println("Reading AREA file \"" + filename+ "\"");

      AreaAdapter aa = null;

      try {
	if (filename.indexOf("://") > 0) {
	  URL u = new URL(filename);
	  aa = new AreaAdapter(u);
        } else {
	  aa = new AreaAdapter(filename);
	}

      } catch (Exception e) {
	System.err.println("Caught IOException for \"" + filename + "\": " +
			   e.getMessage());
      }

      imaget = aa.getData();
      if (imaget == null) {
	System.out.println("\tNULL FlatField!");
      } else {
	System.out.println("\t" + imaget.getType());
      }

      int bandNumber = Integer.parseInt(band.trim());

    try {

 /*  This is diagnostic output only...
    System.out.println("aa="+aa);
    System.out.println("imaget.getDomainSet()="+imaget.getDomainSet() );
    System.out.println("imaget.getDomain.getType="+imaget.getDomainSet().getType() );
    System.out.println("imaget.getDomainSet.getDimension="+
	 imaget.getDomainSet().getDimension() );
    System.out.println("imaget.getDomainSet.getCoordinateSystem="+
	 imaget.getDomainSet().getCoordinateSystem() );
    System.out.println("imaget.getDomainSet.getLength="+
	 imaget.getDomainSet().getLength() );
    System.out.println("imaget.getDomainSet.getX.getFirst="+
	( (Linear2DSet)imaget.getDomainSet()).getX().getFirst() );
    System.out.println("imaget.getDomainSet.getX.getLast="+
	( (Linear2DSet)imaget.getDomainSet()).getX().getLast() );

    System.out.println("imaget.getDomainSet.getY.getFirst="+
	( (Linear2DSet)imaget.getDomainSet()).getY().getFirst() );
    System.out.println("imaget.getDomainSet.getY.getLast="+
	( (Linear2DSet)imaget.getDomainSet()).getY().getLast() );
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

 */

      // In order to test AreaForm, use...but without a basemap!:
      // AreaForm af = new AreaForm();
      // imaget = (FlatField) af.open("AREA0007");

    int[] dim = aa.getDimensions();
    int numEles = dim[1];
    int numLines = dim[2];

      System.out.println("Creating basemap overlay from \"" + mapfile+ "\"");

      BaseMapAdapter bma;

      if (mapfile.indexOf("://") > 0) {
        bma = new BaseMapAdapter(new URL(mapfile) );
      } else {
        bma = new BaseMapAdapter(mapfile);
      }

      bma.setDomainSet( imaget.getDomainSet());

      // other possible form of getting info into BaseMapAdapter:
      // bma.setCoordinateSystem(dics, numEles, numLines, idom);

      DataReference maplines_ref =  new DataReferenceImpl("MapLines");
      maplines_ref.setData(bma.getData() );

      DisplayImpl display;

      if (use2D) {
        display = new DisplayImplJ3D("display1", 
		new TwoDDisplayRendererJ3D() );
      } else {
	display = new DisplayImplJ3D("display1"); 
      }


      System.out.println("Starting to render display");
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

      ConstantMap[] redMap;
      if (use2D) {

	redMap = new ConstantMap[3];
	redMap[0] = new ConstantMap(0., Display.Blue);
        redMap[1] = new ConstantMap(1., Display.Red);
        redMap[2] = new ConstantMap(0., Display.Green);

      } else {
	redMap = new ConstantMap[4];
	redMap[0] = new ConstantMap(0., Display.Blue);
        redMap[1] = new ConstantMap(1., Display.Red);
        redMap[2] = new ConstantMap(0., Display.Green);
        redMap[3] = new ConstantMap(.01, Display.ZAxis);
      }

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
