//
// AddeURLConnection.java
//

/*
The code in this file is Copyright(C) 1999 by Tommy Jasmin,
Don Murray, Tom Whittaker, James Kelly.  
It is designed to be used with the VisAD system for
interactive analysis and visualization of numerical data.

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

package edu.wisc.ssec.mcidas.adde;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

/**
 * This class extends URLConnection, providing the guts of the
 * work to establish an ADDE network connection, put together
 * a request packet, and initiate data flow.  Connections for
 * image data, image directories and dataset information (McIDAS
 * AGET, ADIR and LWPR requests) are supported.
 * @see <A HREF="http://www.ssec.wisc.edu/mug/prog_man/prog_man.html">
 *      McIDAS Programmer's Manual</A>
 *
 * <pre>
 *
 * URLs must all have the following format:
 *
 *   adde://host/request?keyword_1=value_1&keyword_2=value_2
 *
 * where request can be one of the following:
 *
 *   imagedata - request for data in AreaFile format (AGET)
 *   imagedirectory - request for image directory information (ADIR)
 *   datasetinfo - request for data set information (LWPR)
 *   griddirectory - request for grid directory information (GDIR)
 *   griddata - request for grid data (GGET)
 *
 * There can be any valid combination of the following supported keywords:
 *
 * -------for any request
 *
 *   group=<groupname>         ADDE group name
 *   user=<user_id>            ADDE user identification
 *   proj=<proj #>             a valid ADDE project number
 *   trace=<0/1>               setting to 1 tells server to write debug 
 *                               trace file (imagedata, imagedirectory)
 *   version=1                 ADDE version number, currently 1 
 *
 * -------for images:
 *
 *   descr=<descriptor>        ADDE descriptor name
 *   band=<band>               spectral band or channel number 
 *   mag=<lmag> <emag>         image magnification, postitive for blowup, 
 *                               negative for blowdown (default = 1, emag=lmag)
 *                               (imagedata only)
 *   latlon=<lat> <lon>        lat/lon point to center image on (imagedata only)
 *   linele=<lin> <ele> <type> line/element to center image on (imagedata only)
 *   place=<placement>         placement of lat/lon or linele points (center 
 *                               or upperleft (def=center)) (imagedata only)
 *   pos=<position>            request an absolute or relative ADDE position 
 *                               number
 *   size=<lines> <elements>   size of image to be returned (imagedata only)
 *   unit=<unit>               to specify calibration units other than the 
 *                               default 
 *   spac=<bytes>              number of bytes per data point, 1, 2, or 4 
 *                               (imagedata only)
 *   doc=<yes/no>              specify yes to include line documentation 
 *                               with image (def=no) 
 *   aux=<yes/no>              specify yes to include auxilliary information 
 *                               with image 
 *   time=<time1> <time2>      specify the time range of images to select
 *                               (def=latest image if pos not specified)
 *   day=<day>                 specify the day of the images to select
 *                               (def=latest image if pos not specified)
 *   cal=<cal type>            request a specific calibration on the image 
 *                               (imagedata only)
 *   id=<stn id>               radar station id 
 *
 * ------ for grids:
 *
 *   descr=<descriptor>        ADDE descriptor name
 *   param=<param list>        parameter code list
 *   time=<model run time>     time in hhmmss format
 *   day=<model run day>       day in ccyyddd format
 *   lev=<level list>          list of requested levels (value or SFC, MSL 
 *                               or TRO)
 *   ftime=<forecast time>     valid time (hhmmss format) (use with fday)
 *   fday=<forecast day>       forecast day (ccyyddd)
 *   fhour=<forecast hours>    forecast hours (offset from model run time)
 *                                (hhmmss format)
 *   num=<max>                 maximum number of grids to return (nn)
 *
 * ------ for point data:
 *
 *   descr=<descriptor>        ADDE descriptor name
 *   pos=<position>            request an absolute or relative ADDE position 
 *                               number
 *                               number
 *   
 * The following keywords are required:
 *
 *   group 
 *   descr (except datasetinfo)
 *
 * an example URL for images might look like:
 *
 *   adde://viper/imagedata?group=gvar&band=1&user=tjj&proj=6999&version=1
 *   
 * </pre>
 *
 * @author Tommy Jasmin, University of Wisconsin, SSEC
 * @author Don Murray, UCAR/Unidata
 * @author Tom Whittaker, SSEC/CIMSS
 * @author James Kelly, Australian Bureau of Meteorology
 */

public class AddeURLConnection extends URLConnection 
{

  private InputStream is = null;
  private DataInputStream dis = null;
  private DataOutputStream dos = null;
  private URL url;

  private final static int DEFAULT_LINES = 480;
  private final static int DEFAULT_ELEMS = 640;
  private final static int TRAILER_SIZE = 92;
  private final static int REQUEST_SIZE = 120;
  private final static int ERRMSG_SIZE = 72;
  private final static int ERRMSG_OFFS = 8;
  private final static int PORT = 500;

  // ADDE server requests
  private final static int AGET = 0;
  private final static int ADIR = 1;
  private final static int LWPR = 2;
  private final static int GDIR = 3;
  private final static int GGET = 4;
  private final static int MDKS = 5;



  // ADDE data types
  private final static int IMAGE = 100;
  private final static int GRID  = 101;
  private final static int POINT = 102;
  private final static int TEXT  = 103;

  private int numBytes = 0;
  private int dataType = IMAGE;

  /**
   *
   * Constructor: just sets URL and calls superclass constructor.
   * Actual network connection is established in connect().
   *
   */

  AddeURLConnection(URL url)
    throws IOException
  {
    super(url);
    this.url = url;
  } 

  /**
   *
   * Establishes an ADDE connection using the URL passed to the
   * constructor.  Opens a socket on the ADDE port, and formulates
   * an ADDE image data request based on the file portion of URL.
   *   
   * an example URL might look like:
   *   adde://viper.ssec.wisc.edu/image?group=gvar&band=1&mag=-8&version=1
   *   
   */

  synchronized public void connect ()
    throws IOException, AddeURLException
  {

    Socket t;
    try {
      t = new Socket(url.getHost(), PORT);
    } catch (UnknownHostException e) {
      throw new AddeURLException(e.toString());
    }
    is = t.getInputStream();
    dis = new DataInputStream(is);
    dos = new DataOutputStream ( t.getOutputStream() );

    // now formulate and send an ADDE request

    // see if we can use the URL passed in
    
    //System.out.println("host from URL: " + url.getHost());
    //System.out.println("file from URL: " + url.getFile());
    

    // verify the service requested is for image, not grid or md data
    // get rid of leading /
    String request = url.getFile().toLowerCase().substring(1);
    if (!request.startsWith("image") && 
        (!request.startsWith("datasetinfo")) &&
        (!request.startsWith("grid"))   && 
        (!request.startsWith("point"))  )
    {
        throw new AddeURLException("Request for non-image data");
    }

    // send version number - ADDE seems to be stuck at 1
    int version = 1;
    dos.writeInt(version);

    // send IP address of server
    // we know the server IP address is good cause we used it above
    byte [] ipa = new byte[4];
    InetAddress ia = InetAddress.getByName(url.getHost());
    ipa = ia.getAddress();
    dos.write(ipa, 0, ipa.length);

    // send ADDE port number
    dos.writeInt(PORT);

    // service - for area files, it's either AGET (Area GET) or 
    // ADIR (AREA directory)
    byte [] svc = null;
    int reqType;
    if (request.startsWith("imagedir"))
    {
        svc = (new String("adir")).getBytes();
        reqType = ADIR;
    }
    else if (request.startsWith("datasetinfo"))
    {
        svc = (new String("lwpr")).getBytes();
        reqType = LWPR;
    }
    else if (request.startsWith("image"))
    {
        svc = (new String("aget")).getBytes();
        reqType = AGET;
    }
    else if (request.startsWith("griddirectory"))
    {
        svc = (new String("gdir")).getBytes();
        reqType = GDIR;
    }
    else if (request.startsWith("grid"))
    {
        svc = (new String("gget")).getBytes();
        reqType = GGET;
    }
    else if (request.startsWith("point"))
    {
        svc = (new String("mdks")).getBytes();
        reqType = MDKS;
    }
    else
    {
      throw new AddeURLException("Invalid ADDE service="+svc.toString() );
    }

    dos.write(svc, 0, svc.length);

    // now build and send request block, repeat some stuff
    // server IP address, port
    dos.write(ipa, 0, ipa.length);
    dos.writeInt(PORT);

    // client IP address
    InetAddress lh = InetAddress.getLocalHost();
    ipa = lh.getAddress();
    dos.write(ipa, 0, ipa.length);


    // prep for real thing - get cmd from file part of URL
    int test = request.indexOf("?");
    String uCmd = (test >=0) ? request.substring(test+1) : request;
    //System.out.println(uCmd);

    int startIdx;
    int endIdx;

    // user initials - pass on what client supplied in user= keyword
    byte [] usr; 
    String userStr;
    startIdx = uCmd.indexOf("user=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      userStr = uCmd.substring(startIdx + 5, endIdx);
    } else {
      userStr = "XXXX";
    }
    usr = userStr.getBytes();

    // gotta send 4 user bytes, ADDE protocol expects it
    if (usr.length <= 4) {
      dos.write(usr, 0, usr.length);
      for (int i = 0; i < 4 - usr.length; i++) {
        dos.writeByte(' ');
      }
    } else {
      // if id entered was > 4 chars, complain
      throw new AddeURLException("Invalid user id: " + userStr);
    }

    // project number - we won't validate, but make sure it's there
    startIdx = uCmd.indexOf("proj=");
    String projStr;
    int proj;
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      projStr = uCmd.substring(startIdx + 5, endIdx);
    } else {
      projStr = "0";
    }
    try {
      proj = Integer.parseInt(projStr);
    } catch (NumberFormatException e) {
      throw new AddeURLException("Invalid project number: " + projStr);
    }
    dos.writeInt(proj);

    // password chars - not used either
    byte [] pwd = new byte[12];
    dos.write(pwd, 0, pwd.length);

    // service - resend svc array 
    dos.write(svc, 0, svc.length);

    // build and output the command string
    StringBuffer sb = new StringBuffer();
    switch (reqType)
    {
        case AGET:
            sb = decodeAGETString(uCmd);
            break;
        case ADIR:
            sb = decodeADIRString(uCmd);
            break;
        case LWPR:
            sb = decodeLWPRString(uCmd);
            break;
        case GDIR:
            sb = decodeGDIRString(uCmd);
            break;
        case GGET:
            sb = decodeGDIRString(uCmd);
            break;
        case MDKS:
            sb = decodeMDKSString(uCmd);
            break;
    }

    // indefinitely use ADDE version 1
    sb.append(" version=1");

    // now convert to array of bytes for output since chars are two byte
    String cmd = new String(sb);
    cmd = cmd.toUpperCase();
    System.out.println(cmd);
    byte [] ob = cmd.getBytes();

    // data length - num bytes beyond 120 length for command string
    int dlen = 0;
    if (ob.length > REQUEST_SIZE) {
      dlen = ob.length - REQUEST_SIZE;
    }
    dos.writeInt(dlen);
    
    // dos.writeBytes(cmd);
    dos.write(ob, 0, ob.length);
    if (ob.length < REQUEST_SIZE) {
      for (int i = 0; i < REQUEST_SIZE - ob.length; i++) {
        dos.writeByte(0);
      }
    }

    // get response from server, byte count coming back
    numBytes = dis.readInt();

    // if server returns zero, there was an error so read trailer and exit
    if (numBytes == 0) {
      byte [] trailer = new byte[TRAILER_SIZE];
      dis.read(trailer, 0, trailer.length);
      String errMsg = new String(trailer, ERRMSG_OFFS, ERRMSG_SIZE);
      throw new AddeURLException(errMsg);
    }
    //System.out.println("server is sending: " + numBytes + " bytes");


    // if we made it to here, we're getting data
    connected = true;

  }

  /**
   *
   * returns a reference to InputStream established in connect().
   * calls connect() if client has not done so yet.
   *
   * @return            InputStream reference
   */

  synchronized public InputStream getInputStream ()
    throws IOException
  {
    if (!connected) connect();
    return is;
  }

  /**
   *
   * returns a reference to DataInputStream established in connect().
   * calls connect() if client has not done so yet.
   *
   * @return            DataInputStream reference
   */

  synchronized public DataInputStream getDataInputStream ()
    throws IOException
  {
    if (!connected) connect();
    return dis;
  }


    /**
     * Return the number of bytes being sent by the server for the 
     * first record.
     *
     * @return  number of bytes send in the first record
     */
    public int getInitialRecordSize()
    {
        return numBytes;
    }

    /**
     * Decode the ADDE request for image data.
     *
     * there can be any valid combination of the following supported keywords:
     *
     *   group=<groupname>         ADDE group name
     *   descr=<descriptor>        ADDE descriptor name
     *   band=<band>               spectral band or channel number 
     *   mag=<lmag> <emag>         image magnification, postitive for blowup, 
     *                               negative for blowdown (default = 1, 
     *                                  emag=lmag) 
     *   latlon=<lat> <lon>        lat/lon point to center image on 
     *   linele=<lin> <ele> <type> line/element to center image on 
     *   place=<placement>         placement of lat/lon or linele points 
     *                               (center or upperleft (def=center)) 
     *   pos=<position>            request an absolute or relative ADDE position
     *                               number
     *   size=<lines> <elements>   size of image to be returned
     *   unit=<unit>               to specify calibration units other than the 
     *                               default 
     *   spac=<bytes>              number of bytes per data point, 1, 2, or 4 
     *   doc=<yes/no>              specify yes to include line documentation 
     *                               with image (def=no) 
     *   aux=<yes/no>              specify yes to include auxilliary information
     *                               with image 
     *   time=<time1> <time2>      specify the time range of images to select
     *                               (def=latest image if pos not specified)
     *   day=<day>                 specify the day of the images to select
     *                               (def=latest image if pos not specified)
     *   cal=<cal type>            request a specific calibration on the image 
     *   id=<stn id>               radar station id
     *   user=<user_id>            ADDE user identification
     *   proj=<proj #>             a valid ADDE project number
     *   trace=<0/1>               setting to 1 tells server to write debug 
     *                               trace file (imagedata, imagedirectory)
     *
     * the following keywords are required:
     *
     *   group
     *
     * an example URL might look like:
     *   adde://viper/imagedata?group=gvar&band=1&user=tjj&proj=6999
     *   
     * </pre>
     */
    private StringBuffer decodeAGETString(String uCmd)
    {
        StringBuffer buf = new StringBuffer();
        boolean latFlag = false;
        boolean lonFlag = false;
        boolean linFlag = false;
        boolean eleFlag = false;
        String latString = null;
        String lonString = null;
        String linString = null;
        String eleString = null;
        String tempString = null;
        String testString = null;
        // Mandatory strings
        String groupString = null;
        String descrString = "all";
        String posString = "0";
        String numlinString = Integer.toString(DEFAULT_LINES);
        String numeleString = Integer.toString(DEFAULT_ELEMS);
        String magString = "x";
        String traceString = "trace=0";
        String spaceString = "spac=1";
        String unitString = "unit=brit";
        String auxString = "aux=yes";
        String calString = "cal=x";
        String docString = "doc=no";
        String timeString = "time=x x i";
        String lineleType = "a";
        String placement = "c";

        StringTokenizer cmdTokens = new StringTokenizer(uCmd, "&");
        while (cmdTokens.hasMoreTokens())
        {
            testString = cmdTokens.nextToken();
            // group, descr and pos are mandatory
            if (testString.startsWith("grou"))
            {
                groupString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("des"))
            {
                descrString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("pos"))
            {
                posString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("lat"))        // lat or latlon
            {
                latString = 
                    testString.substring(testString.indexOf("=") + 1).trim();
                latFlag = true;
                if (latString.indexOf(" ") > 0)  // is latlon, not just lat
                {
                    StringTokenizer tok = new StringTokenizer(latString);
                    if (tok.countTokens() < 2) break;
                    for (int i = 0; i < 2; i++)
                    {
                        tempString = tok.nextToken();
                        if (i == 0)
                            latString = tempString;
                        else
                        {
                            if (tempString.indexOf("-") >= 0) // (comes in as -)
                                lonString = tempString.substring(
                                    tempString.indexOf("-") + 1);
                            else
                                lonString = "-" + tempString;
                            lonFlag = true;
                        }
                    }
                }
            }
            else
            if (testString.startsWith("lon"))
            {
                lonFlag = true;
                lonString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("lin"))     // line keyword or linele
            {
                tempString = 
                    testString.substring(testString.indexOf("=") + 1);
                if (tempString.indexOf(" ") > 0)  // is linele, not just lin
                {
                    StringTokenizer tok = new StringTokenizer(tempString);
                    if (tok.countTokens() < 2) break;
                    for (int i = 0; i < 2; i++)
                    {
                        tempString = tok.nextToken();
                        if (i == 0)
                        {
                           linString = tempString;
                           linFlag = true;
                        }
                        else
                        {
                           eleString = tempString;
                           eleFlag = true;
                        }
                    }
                    if (tok.hasMoreTokens())  // specified file or image coords
                    {
                        tempString = tok.nextToken().toLowerCase();
                        if (tempString.startsWith("i")) lineleType = "i";
                    }
                }
                else  // is just lines string
                {
                    numlinString = tempString;
                }
            }
            else
            if (testString.startsWith("ele"))    // elements keyword
            {
                numeleString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("pla"))    // placement keyword
            {
                if (testString.substring(
                    testString.indexOf("=") + 1).toLowerCase().startsWith("u"))
                        placement = "u";
            }
            else
            if (testString.startsWith("mag"))
            {
                tempString = 
                    testString.substring(testString.indexOf("=") + 1);
                if (tempString.indexOf(" ") > 0)  // is more than one mag
                {
                    StringTokenizer tok = new StringTokenizer(tempString);
                    if (tok.countTokens() < 2) break;
                    for (int i = 0; i < 2; i++)
                    {
                        buf.append(" ");
                        tempString = tok.nextToken();
                        if (i == 0)
                           buf.append("lmag=" + tempString);
                        else
                           buf.append("emag=" + tempString);
                    }
                }
                else
                    magString = tempString;
            }
            // now get the rest of the keywords (but filter out non-needed)
            else
            if (testString.startsWith("size"))       // size keyword
            {
                tempString = 
                    testString.substring(testString.indexOf("=") + 1);
                if (tempString.indexOf(" ") > 0)  // is linele, not just lin
                {
                    StringTokenizer tok = new StringTokenizer(tempString);
                    if (tok.countTokens() < 2) break;
                    for (int i = 0; i < 2; i++)
                    {
                        tempString = tok.nextToken();
                        if (i == 0)
                           numlinString = tempString;
                        else
                           numeleString = tempString;
                    }
                }
            }
            else
            if (testString.startsWith("tra"))       // trace keyword
            {
                traceString = testString;
            }
            else
            if (testString.startsWith("aux"))       // aux keyword
            {
                auxString = testString;
            }
            else
            if (testString.startsWith("uni"))      // unit keyword
            {
                unitString = testString;
            }
            else
            if (testString.startsWith("cal"))      // cal keyword
            {
                calString = testString;
            }
            else
            if (testString.startsWith("doc"))      // doc keyword
            {
                docString = testString;
            }
            else
            if (testString.startsWith("tim"))      // time keyword
            {
                timeString = testString;
            }
            else
            if (testString.startsWith("ban"))      // band keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("day"))       // day keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("id"))        // id keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("lmag"))      // lmag keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("emag"))      // emag keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
        } 
        buf.append(" ");
        buf.append(traceString);
        buf.append(" ");
        buf.append(unitString);
        buf.append(" ");
        buf.append(auxString);
        buf.append(" ");
        buf.append(docString);
        buf.append(" ");
        buf.append(timeString);
        buf.append(" ");
        buf.append(calString);

        // now create command string
        StringBuffer posParams = 
            new StringBuffer(
                groupString + " " + descrString + " " + posString + " ");

        // Set up location information
        if (latFlag && lonFlag)
            posParams.append("ec " + latString + " " + lonString + " ");
        else if (linFlag && eleFlag)
            posParams.append(lineleType + placement +"  " + 
                             linString + " " + eleString + " ");
        else
            posParams.append("x x x ");

        // add on the mag, lin and ele pos params
        posParams.append(magString + " " + numlinString + 
                         " " + numeleString + " ");

        // stuff it in at the beginning
        try
        {
            buf.insert(0, posParams);
        }
        catch (StringIndexOutOfBoundsException e)
        {
            System.out.println(e.toString());
            buf = new StringBuffer("");
        }
        return buf;
    }

    /**
     * Decode the ADDE request for grid directory information.
     *
     *
     * there can be any valid combination of the following supported keywords:
     *
     *   group=<groupname>       ADDE group name
     *   descr=<descriptor>      ADDE descriptor name
     *   param=<param list>      parameter code list
     *   time=<model run time>   time in hhmmss format
     *   day=<model run day>     day in ccyyddd format
     *   lev=<level list>        list of requested levels (value or SFC, MSL 
     *                             or TRO)
     *   ftime=<forecast time>   valid time (hhmmss format) (use with fday)
     *   fday=<forecast day>     forecast day (ccyyddd)
     *   fhour=<forecast hours>  forecast hours (offset from model run time)
     *                                (hhmmss format)
     *   num=<max>               maximum number of grids (nn) to return (def=1)
     *   user=<user_id>          ADDE user identification
     *   proj=<proj #>           a valid ADDE project number
     *   trace=<0/1>             setting to 1 tells server to write debug 
     *                             trace file (imagedata, imagedirectory)
     *
     * the following keywords are required:
     *
     *   group
     *
     * an example URL might look like:
     *   adde://noaaport/griddirectory?group=ngm&num=10
     *   
     * </pre>
     */
    private StringBuffer decodeGDIRString(String uCmd) {
      StringBuffer buf = new StringBuffer();
      String testString, tempString;
      String groupString = null;
      String descrString = "all";
      String sizeString = " 999999 ";
      String traceString = "trace=0";
      String numString = "num=1";

      StringTokenizer cmdTokens = new StringTokenizer(uCmd, "&");
      while (cmdTokens.hasMoreTokens()) {
        testString = cmdTokens.nextToken();

        // group, descr and pos are mandatory
        if (testString.startsWith("grou")) {
            groupString = 
                testString.substring(testString.indexOf("=") + 1);

        } else if (testString.startsWith("des")) {
            descrString = 
                testString.substring(testString.indexOf("=") + 1);

        // now get the rest of the keywords (but filter out non-needed)
        } else if (testString.startsWith("num")) {
            numString = testString;

        } else if (testString.startsWith("tra")) {      // trace keyword
          traceString = testString;

        } else if (testString.startsWith("pos")) {
          buf.append(" ");
          buf.append(testString);

        } else if (testString.startsWith("par")) {
          buf.append(" ");
          buf.append("parm=");
          buf.append(testString.substring(testString.indexOf("=") + 1));

        } else if (testString.startsWith("fho")) {
          buf.append(" ");
          buf.append("vt=");
          buf.append(testString.substring(testString.indexOf("=") + 1));

        } else if (testString.startsWith("day")) {
          buf.append(" ");
          buf.append(testString);

        } else if (testString.startsWith("time")) {
          buf.append(" ");
          buf.append(testString);

        } else if (testString.startsWith("lev")) {
          buf.append(" ");
          buf.append(testString);

        } else if (testString.startsWith("fday")) {
          buf.append(" ");
          buf.append(testString);

        } else if (testString.startsWith("ftime")) {
          buf.append(" ");
          buf.append(testString);

        } else if (testString.startsWith("vt")) {    // deprecated
          buf.append(" ");
          buf.append(testString);

        } else {
          System.out.println("Unknown token = "+testString);
        } 
      }
      buf.append(" ");
      buf.append(numString);
      buf.append(" ");
      buf.append(traceString);
      buf.append(" version=A ");

      // create command string
      String posParams = new String (
         groupString + " " + descrString + " " + sizeString + " " );

      try {
        buf.insert(0,posParams);
      } catch (StringIndexOutOfBoundsException e) {
        System.out.println(e);
        buf = new StringBuffer("");
      }

      return buf;

    }


    /**
     * Decode the ADDE request for image directory information.
     *
     * <pre>
     * there can be any valid combination of the following supported keywords:
     *
     *   group=<groupname>         ADDE group name
     *   descr=<descriptor>        ADDE descriptor name
     *   band=<band>               spectral band or channel number 
     *   pos=<position>            request an absolute or relative ADDE position
     *                               number
     *   doc=<yes/no>              specify yes to include line documentation 
     *                               with image (def=no) 
     *   aux=<yes/no>              specify yes to include auxilliary information
     *                               with image 
     *   time=<time1> <time2>      specify the time range of images to select
     *                               (def=latest image if pos not specified)
     *   day=<day>                 specify the day of the images to select
     *                               (def=latest image if pos not specified)
     *   cal=<cal type>            request a specific calibration on the image 
     *   id=<stn id>               radar station id
     *   user=<user_id>            ADDE user identification
     *   proj=<proj #>             a valid ADDE project number
     *   trace=<0/1>               setting to 1 tells server to write debug 
     *                               trace file (imagedata, imagedirectory)
     *
     * the following keywords are required:
     *
     *   group
     *
     * an example URL might look like:
     *   adde://viper/imagedirectory?group=gvar&descr=east1km&band=1
     *   
     * </pre>
     */
    private StringBuffer decodeADIRString(String uCmd)
    {
        StringBuffer buf = new StringBuffer();
        String testString;
        String tempString;
        // Mandatory strings
        String groupString = null;
        String descrString = "all";
        String posString = "0 0";
        String traceString = "trace=0";
        String bandString = "band=all x";
        String auxString = "aux=yes";

        StringTokenizer cmdTokens = new StringTokenizer(uCmd, "&");
        while (cmdTokens.hasMoreTokens())
        {
            testString = cmdTokens.nextToken();
            // group, descr and pos are mandatory
            if (testString.startsWith("grou"))
            {
                groupString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("des"))
            {
                descrString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("pos"))
            {
                tempString = 
                    testString.substring(testString.indexOf("=") + 1);
                posString = 
                    tempString.equals("")    // check for no input ("pos=")
                        ? "0 0"
                        : tempString.equals("all")  // check for all request
                                ? "1095519264" : tempString + " 0";
            }
            // now get the rest of the keywords (but filter out non-needed)
            else
            if (testString.startsWith("tra"))       // trace keyword
            {
                traceString = testString;
            }
            else
            if (testString.startsWith("aux"))       // aux keyword
            {
                auxString = testString;
            }
            else
            if (testString.startsWith("ban"))      // band keyword
            {
                bandString = testString;
            }
            else
            if (testString.startsWith("tim"))       // time keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("day"))       // time keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("id"))        // id keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
        } 
        buf.append(" ");
        buf.append(traceString);
        buf.append(" ");
        buf.append(bandString);
        buf.append(" ");
        buf.append(auxString);

        // now create command string
        String posParams = 
            new String(
                groupString + " " + descrString + " " + posString + " ");
        try
        {
            buf.insert(0, posParams);
        }
        catch (StringIndexOutOfBoundsException e)
        {
            System.out.println(e.toString());
            buf = new StringBuffer("");
        }
        return buf;
    }

    /**
     * Decode the ADDE request for data set information.
     *
     * <pre>
     * there can be any valid combination of the following supported keywords:
     *   group - ADDE group name   
     *   type  - ADDE data type.  Must be one of the following:
     *               IMAGE, POINT, GRID, TEXT, NAV
     *           the default is the IMAGE type.
     *
     * the following keywords are required:
     *
     *   group
     *
     * an example URL might look like:
     *   adde://viper/datasetinfo?group=gvar&type=image
     *   
     * </pre>
     */
    public StringBuffer decodeLWPRString(String uCmd)
    {
        StringBuffer buf = new StringBuffer();
        String testString;
        String tempString;
        String groupString = null;
        String typeString = "ala.";

        StringTokenizer cmdTokens = new StringTokenizer(uCmd, "&");
        while (cmdTokens.hasMoreTokens())
        {
            testString = cmdTokens.nextToken();
            // group, descr and pos are mandatory
            if (testString.startsWith("grou"))
            {
                groupString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            if (testString.startsWith("type"))
            {
                tempString = 
                    testString.substring(testString.indexOf("=") + 1);
                if (tempString.startsWith("i")) 
                    typeString = "ala.";
                if (tempString.startsWith("g")) 
                    typeString = "alg.";
                else if (tempString.startsWith("p"))
                    typeString = "alm.";
                else if (tempString.startsWith("t")) 
                    typeString = "alt.";
                else if (tempString.startsWith("n")) 
                    typeString = "aln.";
                else if (tempString.startsWith("s")) 
                    typeString = "aln.";
            }

        }
        buf.append(typeString);
        buf.append(groupString);
        return buf;
    }

    /**
     * Decode the ADDE request for point data.
     *
     *   group=<groupname>         ADDE group name
     *   descr=<descriptor>        ADDE descriptor name
     *   pos=<position>            request an absolute or relative ADDE 
     *                               position number
     *   user=<user_id>            ADDE user identification
     *   proj=<proj #>             a valid ADDE project number
     *   trace=<0/1>               setting to 1 tells server to write debug 
     *                               trace file (imagedata, imagedirectory)
//   *   select - to specify which data is required
//   *   param  - what parameters to return
     *
     * the following keywords are required:
     *
     *   group
     *
     * an example URL might look like:
     *   adde://rtds/point?group=neons&descr=metar&user=jmk&proj=6999
     *   
     * </pre>
     */
    private StringBuffer decodeMDKSString(String uCmd)
    {
        StringBuffer buf = new StringBuffer();
        boolean latFlag = false;
        boolean lonFlag = false;
        String latString = null;
        String lonString = null;
        String testString = null;
        // Mandatory strings
        String groupString = null;
        String descrString = null;
        String maxString = "max=1000";
                                // Options strings
        String posString = "pos=0";
        String traceString = "trace=0";
        String spaceString = "spac=1";
        String selectString = null;
        String paramString = null;

        StringTokenizer cmdTokens = new StringTokenizer(uCmd, "&");
        while (cmdTokens.hasMoreTokens())
        {
            testString = cmdTokens.nextToken();
            // group and descr
            if (testString.startsWith("grou"))
            {
                groupString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("des"))
            {
                descrString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("select"))
            {
                selectString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("max"))
            {
                maxString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("param"))
            {
                paramString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            // now get the rest of the keywords (but filter out non-needed)
            else
            if (testString.startsWith("tra"))       // trace keyword
            {
                traceString = testString;
            }
        } 
        // buf.append(" ");
        // buf.append(traceString);

        // now create command string
        StringBuffer posParams = 
            new StringBuffer(
                 groupString + " " + descrString + " " + maxString + " " + posString);

        // do something here with paramString & selectString
                                // not yet done!

        // stuff it in at the beginning
        try
        {
            buf.insert(0, posParams);
        }
        catch (StringIndexOutOfBoundsException e)
        {
            System.out.println(e.toString());
            buf = new StringBuffer("");
        }
        return buf;
    }
}
