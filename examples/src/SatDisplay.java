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

//
// SatDisplay.java by Don Murray of Unidata
//

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import javax.swing.JFrame;
import visad.ColorControl;
import visad.CoordinateSystem;
import visad.ConstantMap;
import visad.Data;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Linear2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.data.mcidas.AreaAdapter;
import visad.data.mcidas.BaseMapAdapter;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.java3d.DefaultRendererJ3D;
import visad.DisplayListener;
import visad.DisplayEvent;
import visad.VisADException;
import java.rmi.RemoteException;

/**
 * Example class to display a satellite image in VisAD.
 *
 * @author  Don Murray - Unidata
 */
public class SatDisplay implements DisplayListener
{

    private DisplayImpl display;

    private DefaultRendererJ3D drmap;
    private DefaultRendererJ3D drimage;

    /**
     * Construct a satellite display using the specified McIDAS map file,
     * image source.  The image can be displayed on a 3D globe or on a
     * flat rectillinear projection.
     *
     * @param  mapFile      location of the McIDAS map file (path or URL)
     * @param  imageSource  location of the image source (path or URL)
     * @param  display3D    if true, use 3D display, otherwise flat rectillinear
     * @param  remap        remap the image into a domain over North America
     */
    public SatDisplay(String mapFile, String imageSource,
                      boolean display3D, boolean remap)
    {
        try
        {
            //  Read in the map file
            BaseMapAdapter baseMapAdapter;
            if (mapFile.indexOf("://") > 0)   // URL specified
            {
               baseMapAdapter = new BaseMapAdapter(new URL(mapFile) );
            }
            else   // local disk file
            {
               baseMapAdapter = new BaseMapAdapter(mapFile);
            }

            // Create the display and set up the scalar maps to map
            // data to the display
            ScalarMap latMap;     // latitude  -> YAxis
            ScalarMap lonMap;     // longitude -> XAxis
            if (display3D)
            {
                display = new DisplayImplJ3D("display");
                latMap = new ScalarMap(RealType.Latitude, Display.Latitude);
                lonMap = new ScalarMap(RealType.Longitude, Display.Longitude);
            }
            else
            {
                display = new DisplayImplJ3D("display",
                                               new TwoDDisplayRendererJ3D());
                latMap = new ScalarMap(RealType.Latitude, Display.YAxis);
                lonMap = new ScalarMap(RealType.Longitude, Display.XAxis);
            }
            display.addMap(latMap);
            display.addMap(lonMap);

            // set the display to a global scale
            latMap.setRange(-90.0, 90.0);
            lonMap.setRange(-180.0, 180.0);

            // create a reference for the map line
            DataReference maplinesRef = new DataReferenceImpl("MapLines");
            maplinesRef.setData(baseMapAdapter.getData());

            // set the attributes of the map lines (color, location)
            ConstantMap[] maplinesConstantMap = new ConstantMap[4];
            maplinesConstantMap[0] = new ConstantMap(0., Display.Blue);
            maplinesConstantMap[1] = new ConstantMap(1., Display.Red);
            maplinesConstantMap[2] = new ConstantMap(0., Display.Green);
            maplinesConstantMap[3] =
                new ConstantMap(1.001, Display.Radius); // just above the image

            // read in the image
            AreaAdapter areaAdapter = new AreaAdapter(imageSource);
            FlatField image = areaAdapter.getData();

            // Extract the metadata from the image
            FunctionType imageFunctionType =
                (FunctionType) image.getType();
            RealTupleType imageDomainType = imageFunctionType.getDomain();
            RealTupleType imageRangeType =
                (RealTupleType) imageFunctionType.getRange();

            // remap and resample the image
            if (remap)
            {
                int SIZE = 256;
                RealTupleType latlonType =
                  ((CoordinateSystem)
                      imageDomainType.getCoordinateSystem()).getReference();
                Linear2DSet remapDomainSet =
                    new Linear2DSet(
                        latlonType, -4.0, 70.0, SIZE, -150.0, 5.0, SIZE);
                image =
                    (FlatField) image.resample(
                        remapDomainSet, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            }

            // select which band to show...
            ScalarMap rgbMap =
                new ScalarMap(
                    (RealType) imageRangeType.getComponent(0), Display.RGB);
            display.addMap(rgbMap);

            // set the enhancement to a grey scale
            ColorControl colorControl = (ColorControl) rgbMap.getControl();
            colorControl.initGreyWedge();

            // create a data reference for the image
            DataReferenceImpl imageRef = new DataReferenceImpl("ImageRef");
            imageRef.setData(image);

            // add the data references to the display
            display.disableAction();
            drmap = new DefaultRendererJ3D();
            drimage = new DefaultRendererJ3D();
            drmap.toggle(false);
            drimage.toggle(false);
            display.addDisplayListener(this);
    
            display.addReferences(drmap, maplinesRef, maplinesConstantMap);
            display.addReferences(drimage, imageRef,null);
            display.enableAction();
        }
        catch (Exception ne)
        {
            ne.printStackTrace(); System.exit(1);
        }

    }

    public void displayChanged(DisplayEvent e)
         throws VisADException, RemoteException {
      if (e.getId() == DisplayEvent.TRANSFORM_DONE) {
        drmap.toggle(true);
        drimage.toggle(true);
      }
    }

    /**
     * <UL>
     * <LI>run 'java -mx64m SatDisplay' for globe display
     * <LI>run 'java -mx64m SatDisplay X remap' for remapped globe display
     * <LI>run 'java -mx64m SatDisplay X 2D' for flat display
     * </UL>
     */
    public static void main (String[] args) {

        String mapFile = "ftp://ftp.ssec.wisc.edu/pub/visad-2.0/OUTLSUPW";
        String imageSource = "ftp://ftp.ssec.wisc.edu/pub/visad-2.0/AREA2001";
        boolean use3D = true;
        boolean remap = false;

        JFrame frame = new JFrame("Satellite Display");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        if (args.length > 0 && !args[0].equals("X")) {
           imageSource = args[0];
           // mapFile = args[0];
        }
        if (args.length == 2) {
           use3D = (args[1].indexOf("2") >= 0) ? false : true;
           remap = (args[1].indexOf("2") >= 0) ? false : true;
        }

        SatDisplay satDisplay =
            new SatDisplay(mapFile, imageSource, use3D, remap);
        frame.getContentPane().add(satDisplay.display.getComponent());
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}
