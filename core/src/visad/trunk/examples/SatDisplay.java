
//
// SatDisplay.java by Don Murray of Unidata
//

import visad.*;
import visad.data.mcidas.BaseMapAdapter;
import visad.data.mcidas.AreaAdapter;
import visad.java3d.*;
import visad.java2d.*;

import java.net.MalformedURLException;
import java.net.URL;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SatDisplay {

    private DisplayImpl display;
    private BaseMapAdapter baseMap;
    private ScalarMap lat_map;
    private ScalarMap lon_map;
    private ScalarMap xaxis;
    private ScalarMap yaxis;

    public SatDisplay(String mapfile, boolean display3D, boolean remap) {

        try {

           if (mapfile.indexOf("://") > 0) {
               baseMap = new BaseMapAdapter(new URL(mapfile) );
           } else {
               baseMap = new BaseMapAdapter(mapfile);
           }

           //--- map data to display ---//
           if (display3D) 
           {
               display = new DisplayImplJ3D("display");
               lat_map = new ScalarMap(RealType.Latitude, Display.Latitude);
               lon_map = new ScalarMap(RealType.Longitude, Display.Longitude);
           }
           else
           {
               display = new DisplayImplJ3D("display",
                                              new TwoDDisplayRendererJ3D());
               lat_map = new ScalarMap(RealType.Latitude, Display.YAxis);
               lon_map = new ScalarMap(RealType.Longitude, Display.XAxis);
            }

            display.addMap(lat_map);
            display.addMap(lon_map);

            lat_map.setRange(-90.0, 90.0);
            lon_map.setRange(-180.0, 180.0);
  
            DataReference maplines_ref = new DataReferenceImpl("MapLines");
            maplines_ref.setData(baseMap.getData());
        
            ConstantMap[] colMap;
            colMap = new ConstantMap[4];
            colMap[0] = new ConstantMap(0., Display.Blue);
            colMap[1] = new ConstantMap(1., Display.Red);
            colMap[2] = new ConstantMap(0., Display.Green);
            colMap[3] = new ConstantMap(1.001, Display.Radius);

            AreaAdapter aa = new AreaAdapter(
                            "ftp://www.ssec.wisc.edu/pub/visad-2.0/AREA2001");

            FlatField imaget = aa.getData();

            FunctionType ftype = (FunctionType) imaget.getType();
            RealTupleType dtype = ftype.getDomain();
            RealTupleType rtype = (RealTupleType)ftype.getRange();

            if (remap) {
              int SIZE = 256;
              RealTupleType lat_lon =
                ((CoordinateSystem) dtype.getCoordinateSystem()).getReference();
              Linear2DSet dset = new Linear2DSet(lat_lon, -4.0, 70.0, SIZE,
                                                 -150.0, 5.0, SIZE);
              imaget = (FlatField)
                imaget.resample(dset, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
            }

            // select which band to show...
            display.addMap(new ScalarMap( (RealType) rtype.getComponent(0),
                                               Display.RGB) );

            DataReferenceImpl ref_image = new DataReferenceImpl("ref_image");

            ref_image.setData(imaget);

            display.disableAction();
            display.addReference(ref_image,null);
            display.addReference(maplines_ref, colMap);
            display.enableAction();
        } catch (Exception ne) {ne.printStackTrace(); System.exit(1); }

    }

    // run 'java -mx64m SatDisplay' for globe display
    // run 'java -mx64m SatDisplay X remap' for remapped globe display
    // run 'java -mx64m SatDisplay X 2D' for flat display
    public static void main (String[] args) {

        String mapFile = "ftp://www.ssec.wisc.edu/pub/visad-2.0/OUTLSUPW";
        boolean threeD = true;
        boolean remap = false;

        JFrame frame = new JFrame("Map Display");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        if (args.length > 0 && !args[0].equals("X")) {
           mapFile = args[0];
        }
        if (args.length == 2) {
           threeD = (args[1].indexOf("2") >= 0) ? false : true;
           remap = (args[1].indexOf("2") >= 0) ? false : true;
        }

        SatDisplay map = new SatDisplay(mapFile, threeD, remap);
        frame.getContentPane().add(map.display.getComponent());
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}

