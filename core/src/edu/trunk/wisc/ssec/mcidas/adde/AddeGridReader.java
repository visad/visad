
//
// GridDirList.java
//

/*
The code in this file is Copyright(C) 1999 by Don
Murray.  It is designed to be used with the VisAD system for 
interactive analysis and visualization of numerical data.  
 
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

package edu.wisc.ssec.mcidas.adde;

import java.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import visad.jmet.*;
import visad.*;
import visad.data.mcidas.*;
import visad.data.netcdf.*;
import visad.data.netcdf.units.*;
import edu.wisc.ssec.mcidas.*;
import edu.wisc.ssec.mcidas.adde.*;

/** 
 * GridDirList interface for McIDAS ADDE grid data sets.   Simulates a
 * McIDAS GRDLIST request using an ADDE URL.
 *
 * <pre>
 * URLs must all have the following format   
 *   adde://host/griddirectory?keyword_1=value_1&keyword_2=value_2
 *
 * there can be any valid combination of the following supported keywords:
 *
 *   group - ADDE group name   
 *   type  - ADDE data type.  Must be one of the following:
 *               image, point, grid, text, nav
 *           the default is the image type.
 *
 * the following keywords are required:
 *
 *   group
 *
 * an example URL might look like:
 *   adde://viper/griddirectory?group=gvar&type=image
 * </pre>
 *
 * @author Tom Whittaker
 * 
 */
public class AddeGridReader {

    // load protocol for ADDE URLs
    // See java.net.URL for explanation of URL handling
    static {
        try 
        {
            String handlers = System.getProperty("java.protocol.handler.pkgs");
            String newProperty = null;
            if (handlers == null)
                newProperty = "edu.wisc.ssec.mcidas";
            else if (handlers.indexOf("edu.wisc.ssec.mcidas") < 0)
                newProperty = "edu.wisc.ssec.mcidas | " + handlers;
            if (newProperty != null)  // was set above
                System.setProperty("java.protocol.handler.pkgs", newProperty);
        }
        catch (Exception e)
        {
            System.out.println(
                "Unable to set System Property: java.protocol.handler.pkgs"); 
        }
    }

    private DataInputStream dis;   // input stream
    private int status=0;                 // read status
    private URLConnection urlc;           // URL connection
    private char[] data;                  // data returned from server

    private final int HEARTBEAT = 11223344;
    ArrayList fileHeaders, gridHeaders, gridData;

    /**
      * allows reading of McIDAS grid headers and data
      *
      */
    public AddeGridReader() {
    }
    
    /**
     * creates an ArrayList of McIDASGridDirectories
     *
     * @param request ADDE URL to read from.  See class javadoc.
     *
     * <pre>
     * an example URL might look like:
     *   adde://viper/griddirectory?group=gvar&type=image
     * </pre>
     *
     * @exception AddeURLException if there are no datasets of the particular
     *            type or there is an error reading data
     *
     */
    public ArrayList getGridDirectory (String request) throws AddeURLException {
        URL url;
        gridHeaders = new ArrayList();
        fileHeaders = new ArrayList();

        try {
            url = new URL(request);
            urlc = url.openConnection();
            InputStream is = urlc.getInputStream();
            dis = new DataInputStream(new BufferedInputStream(is));
        } catch (AddeURLException ae) {
            throw new AddeURLException("Dataset not found: "+ae);
        }
        catch (Exception e) {
            throw new AddeURLException("Error opening connection: " + e);
        }

        int numBytes = ((AddeURLConnection) urlc).getInitialRecordSize();
        if (numBytes == 0) {
            status = -1;
            throw new AddeURLException("No datasets found");
        }

        try {

          int check;
          byte[] header = new byte[256];
          while (numBytes == 4) {
            check = dis.readInt();
            if (check != HEARTBEAT) {
              System.out.println("problem...not heartbeat = "+check);
            }
            numBytes = dis.readInt();
          }
          //System.out.println("numBytes = "+numBytes);

          while ( (check = dis.readInt()) == 0) {
            dis.readFully(header,0,256);
            String head = new String(header,0,32);
            fileHeaders.add(head);
            //System.out.println("File Header="+head);

            int check2;

            while( (check2 = dis.readInt()) == 0) {
              dis.readFully(header,0,256);
              String name = new String(header,24,4);
              McIDASGridDirectory mg = new McIDASGridDirectory(header);
              //System.out.println(mg.toString());
              gridHeaders.add(mg);

            }
            //System.out.println("check2 = "+check2);
          }
          //System.out.println("check = "+check);

        } catch (Exception re) {System.out.println(re);}

        return gridHeaders;

    }

    public ArrayList getGridHeaders() {
      return gridHeaders;
    }

    public ArrayList getFileHeaders() {
      return fileHeaders;
    }

    /**
     * creates an ArrayList of arrays of data, plus an ArrayList
     * of grid headers (McIDASGridDirectories) which are then
     * available using the getGridHeaders() method.
     *
     * @param request ADDE URL to read from.  See class javadoc.
     *
     * <pre>
     * an example URL might look like:
     *   adde://viper/grid?group=abom&type=grid&parm=T&lev=500&
     * </pre>
     *
     * @exception AddeURLException if there are no datasets of the particular
     *            type or there is an error reading data
     *
     */
    public ArrayList getGridData (String request) throws AddeURLException {
        URL url;
        gridHeaders = new ArrayList();
        gridData = new ArrayList();

        try {
            url = new URL(request);
            urlc = url.openConnection();
            InputStream is = urlc.getInputStream();
            dis = new DataInputStream(new BufferedInputStream(is));
        } catch (AddeURLException ae) {
            throw new AddeURLException("Dataset not found: "+ae);
        }
        catch (Exception e) {
            throw new AddeURLException("Error opening connection: " + e);
        }

        int numBytes = ((AddeURLConnection) urlc).getInitialRecordSize();
        if (numBytes == 0) {
            status = -1;
            throw new AddeURLException("No datasets found");
        }

        try {

          int check;
          byte[] header = new byte[256];
          while (numBytes == 4) {
            check = dis.readInt();
            if (check != HEARTBEAT) {
              System.out.println("problem...not heartbeat = "+check);
            }
            numBytes = dis.readInt();
          }
          //System.out.println("numBytes = "+numBytes);

          int checkBytes = dis.readInt();
          if (checkBytes != numBytes) {throw new
            AddeURLException("Invalid number of bytes returned for grid.");}

          int numGrids = dis.readInt();
          //System.out.println("numGrids = "+numGrids);

          for (int i=0; i<numGrids; i++) {

            dis.readFully(header,0,256);
            String name = new String(header,24,4);
            McIDASGridDirectory mg = new McIDASGridDirectory(header);
            System.out.println(mg.toString());
            CoordinateSystem c = mg.getCoordinateSystem();
            gridHeaders.add(mg);
            int rows = mg.getRows();
            int cols = mg.getColumns();
            //System.out.println("# rows & cols = "+rows+" "+cols);

            double scale = mg.getParamScale();
            //System.out.println("param scale = "+scale+" gridType="+mg.getGridType());

            double[] data = new double[rows*cols];
            int n = 0;
            // store such that 0,0 is in lower left corner...
            for (int nc=0; nc<cols; nc++) {
              for (int nr=0; nr<rows; nr++) {
                int temp = dis.readInt();
                data[(rows-nr-1)*cols + nc] =        // check for missing value
                  (temp == McIDASUtil.MCMISSING)
                    ? Double.NaN
                    : ( (double) temp) / scale ;
              }
            }
            gridData.add(data);

            check = dis.readInt();
            //System.out.println("check after grid = "+check+"  point value = "+data[100]);
            if (check != 0) break;

          }
          //System.out.println("check = "+check);

        } catch (Exception re) {System.out.println(re);}

        return gridData;

    }



    /** test by running 'java edu.wisc.ssec.mcidas.adde.AddeGridReader' */
    public static void main (String[] args)
        throws Exception
    {
        String request = 
            (args.length == 0)
                ? "adde://sweetpea.ssec.wisc.edu/grid?group=ABOM&descr=GRIDS&pos=302&num=40&lev=500&proj=6999&user=tomw&"
//                ? "adde://sweetpea.ssec.wisc.edu/grid?group=ABOM&descr=GRIDS&pos=302&num=2&proj=6999&user=tomw&"
//                ? "adde://noaaport.ssec.wisc.edu/grid?group=NGM&descr=12&num=12&proj=6999&user=tomw&"
//                  ? "adde://noaaport.ssec.wisc.edu/griddirectory?group=NGM&descr=12&num=15&proj=6999&user=tomw&"
//                  ? "adde://adde.unidata.ucar.edu/griddirectory?group=rtgrids&descr=ngm&num=all&proj=6999&user=tomw&"
                  : args[0];
        AddeGridReader d = new AddeGridReader();

        /*
        ArrayList v = d.getGridDirectory(request);
        ArrayList vh = d.getFileHeaders();
        String s = (String) vh.elementAt(0);
        System.out.println("File header = "+s);

        for (int i=0; i<v.size(); i++) {
          McIDASGridDirectory mg = (McIDASGridDirectory) v.elementAt(i);
          System.out.println(mg.toString());
        }
        */

        ArrayList v = d.getGridData(request);
        ArrayList vd = d.getGridHeaders();


    }
}
