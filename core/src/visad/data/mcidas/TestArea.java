//
// TestArea.java
//

/*

The software in this file is Copyright(C) 2019 by Tom Whittaker.
It is designed to be used with the VisAD system for interactive
analysis and visualization of numerical data.

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

package visad.data.mcidas;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JPanel;
import visad.ColorControl;
import visad.ConstantMap;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.DataRenderer;
import visad.Display;
import visad.DisplayImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Linear2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.java2d.DefaultRendererJ2D;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.DefaultRendererJ3D;
import visad.java3d.DisplayImplJ3D;

/** This will test the Area File Adapter and Base Map (McIDAS
 *  formats) Adapter for VisAD.  You need a sample AREA file,
 *  along with a McIDAS "OUTL" format map file.  You may
 *  get these from ftp://allegro.ssec.wisc.edu/visad/
 *
 *  At this point, only GVAR, MSAT and MOLL navigation is supported, and no
 *  work has been done on calibration yet.
 *
 *  There is quite a bit of extra printout that is usually
 *  commented out, but may provide some insight into the
 *  Math and Data structures employed.
 *
 *  @author Tom Whittaker (SSEC)
 */
public class TestArea {

  static boolean use2D = false;
  static String imageSource = "AREA0007";
  static String band = "1";
  static String mapfile = "OUTLUSAM";

  private static boolean getOptions(String[] args)
  {
    boolean defaultFile = true;
    boolean defaultBand = true;
    boolean defaultMap = true;

    int keyNum = 0;
    boolean gotValidOptions = true;
    for (int i = 0; i < args.length; i++) {
      if (args[i].charAt(0) == '-') {

    // handle options
    switch (args[i].charAt(1)) {
    case '2':
      use2D = true;
      break;
    case '3':
      use2D = false;
      break;
    case 'f':
      i++;
      imageSource = args[i];
      defaultFile = false;
      break;
    case 'b':
      i++;
      band = args[i];
      defaultBand = false;
      break;
    case 'm':
      i++;
      mapfile = args[i];
      defaultMap = false;
      break;
    default:
      System.err.println("Unknown option \"" + args[i] + "\"");
      gotValidOptions = true;
      break;
    }
      } else {

    // handle keywords (AKA positional parameters)
    switch (keyNum) {
    case 0:
      imageSource = args[i];
      defaultFile = false;
      break;
    case 1:
      band = args[i];
      defaultBand = false;
      break;
    case 2:
      mapfile = args[i];
      defaultMap = false;
          break;
        default:
          System.err.println("Unknown keyword \"" + args[i] + "\"");
          gotValidOptions = true;
          break;
        }
        keyNum++;
      }
    }

    if (!gotValidOptions) {
      System.err.print("Usage: java TestArea ");
      System.err.print(" <AREAfilename>");
      System.err.print(" <band#>");
      System.err.print(" <mapfilename>");
      System.err.println("");

      System.err.print("  or : java TestArea");
      System.err.print(" [-2(D)|-3(D)]");
      System.err.print(" [-f AREAfilename]");
      System.err.print(" [-b band#]");
      System.err.print(" [-m mapfilename]");
      System.err.println("");

      System.err.println("\t(filenames may also be URLs)");
    }

    if (defaultFile || defaultBand || defaultMap) {
      boolean needComma = false;

      System.out.print("Using default");
      if (defaultFile) {
        System.out.print((needComma ? "," : "") + " file " + imageSource);
      }
      if (defaultBand) {
        System.out.print((needComma ? "," : "") + " band " + band);
      }
      if (defaultMap) {
        System.out.print((needComma ? "," : "") + " map " + mapfile);
      }

      System.out.println("");
    }

    return gotValidOptions;
  }

  public static void main(String args[]) {

    if (!getOptions(args)) {
      System.exit(1);
      return;
    }

    FlatField imaget = null;

      System.out.println("Reading AREA file \"" + imageSource+ "\"");

      AreaAdapter aa = null;

      try {
        aa = new AreaAdapter(imageSource);
      } catch (Exception e) {
        System.err.println("Caught IOException for \"" + imageSource + "\": " +
                           e.getMessage());
        System.exit(1);
      }

      imaget = aa.getData();
      if (imaget == null) {
        System.out.println("\tNULL FlatField!");
      } else {
        System.out.println("\t" + imaget.getType());
      }

      int bandNumber = Integer.parseInt(band.trim());

    try {

    //System.out.println("aa="+aa);
    System.out.println("DateTime= "+aa.getNominalTime());
    System.out.println("imaget.getDomainSet()="+imaget.getDomainSet() );
    System.out.println("imaget.getDomain.getType="+imaget.getDomainSet().getType() );
 /*  This is diagnostic output only...
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

      bma.setDomainSet( (Linear2DSet) imaget.getDomainSet());

      // other possible form of getting info into BaseMapAdapter:
      // bma.setCoordinateSystem(dics, numEles, numLines, idom);

      DataReference maplines_ref =  new DataReferenceImpl("MapLines");
      maplines_ref.setData(bma.getData() );

      DisplayImpl display;

      if (use2D) {
        display = new DisplayImplJ2D("display1");
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
      ScalarMap rgbMap =
          new ScalarMap( (RealType) rtype.getComponent(bandNumber-1),
              Display.RGB);
      display.addMap(rgbMap);
      ColorControl cc = (ColorControl) rgbMap.getControl();
      cc.initGreyWedge();

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

      DataRenderer drend;
      if (use2D) {
        drend = new DefaultRendererJ2D();
      } else {
        drend = new DefaultRendererJ3D();
      }

      display.addReferences( drend, maplines_ref, redMap);

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
