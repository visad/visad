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
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import HTTPClient.UncompressInputStream;

/**
 * This class extends URLConnection, providing the guts of the
 * work to establish an ADDE network connection, put together
 * a request packet, and initiate data flow.  Connections for
 * image data, image directories, grid data, grid directory, 
 * point source data, text, weather text, observational text
 * and dataset information (McIDAS AGET, ADIR, GDIR, GGET, MDKS, 
 * TXTG, OBTG, WXTG and LWPR requests) are supported.
 *
 * @see <A HREF="http://www.ssec.wisc.edu/mug/prog_man/prog_man.html">
 *      McIDAS Programmer's Manual</A>
 *
 * <pre>
 *
 * URLs must all have the following format:
 *
 *   adde://host/request?keyword_1=value_1&amp;keyword_2=value_2
 *
 * where request can be one of the following:
 *
 *   datasetinfo - request for data set information (LWPR)
 *   griddirectory - request for grid directory information (GDIR)
 *   griddata - request for grid data (GGET)
 *   imagedata - request for data in AreaFile format (AGET)
 *   imagedirectory - request for image directory information (ADIR)
 *   pointdata - request for point data (MDKS)
 *   textdata - request to read a text file (TXTG) 
 *   wxtext   - request to read a weather text file (WTXG) 
 *   obtext   - request to read a observation text file (OBTG) 
 *
 * There can be any valid combination of the following supported keywords:
 *
 * -------for any request
 *
 *   group=&lt;groupname&gt;         ADDE group name
 *   user=&lt;user_id&gt;            ADDE user identification
 *   proj=&lt;proj #&gt;             a valid ADDE project number
 *   trace=&lt;0/1&gt;               setting to 1 tells server to write debug 
 *                               trace file 
 *   version=                  ADDE version number, currently 1 except for
 *                             griddata requests
 *   debug=                    set to true to watch the printlns stream by
 *   compress=                 set to "gzip" if you want to use the GZIP
 *                             compression or "compress" if you want to use 
 *                             transfers in Unix compress format (You need to 
 *                             have the VisAD package if you want to support 
 *                             this.)  default = none.
 *   port=                     Socket port to connect on.  Overridden by
 *                             a port specified in the host 
 *                             (e.g., adde.ucar.edu:500)
 *
 * -------for images:
 *
 *   descr=&lt;descriptor&gt;        ADDE descriptor name
 *   band=&lt;band&gt;               spectral band or channel number 
 *   mag=&lt;lmag&gt; &lt;emag&gt;         image magnification, postitive for blowup, 
 *                               negative for blowdown (default = 1, emag=lmag)
 *                               (imagedata only)
 *   latlon=&lt;lat&gt; &lt;lon&gt;        lat/lon point to center image on (imagedata only)
 *   linele=&lt;lin&gt; &lt;ele&gt; &lt;type&gt; line/element to center image on (imagedata only)
 *   place=&lt;placement&gt;         placement of lat/lon or linele points (center 
 *                               or upperleft (def=center)) (imagedata only)
 *   pos=&lt;position&gt;            request an absolute or relative ADDE position 
 *                               number.  May use &lt;start&gt; &lt;end&gt;  Default
 *                               for &lt;end&gt; is 0 if start&lt;0, or =start otherwise.
 *   size=&lt;lines&gt; &lt;elements&gt;   size of image to be returned (imagedata only)
 *   unit=&lt;unit&gt;               to specify calibration units other than the 
 *                               default 
 *   spac=&lt;bytes&gt;              number of bytes per data point, 1, 2, or 4 
 *                               (imagedata only)
 *   doc=&lt;yes/no&gt;              specify yes to include line documentation 
 *                               with image (def=no) 
 *   aux=&lt;yes/no&gt;              specify yes to include auxilliary information 
 *                               with image 
 *   time=&lt;time1&gt; &lt;time2&gt;      specify the time range of images to select
 *                               (def=latest image if pos not specified)
 *   day=&lt;day&gt;                 specify the day of the images to select
 *                               (def=latest image if pos not specified)
 *   cal=&lt;cal type&gt;            request a specific calibration on the image 
 *                               (imagedata only)
 *   id=&lt;stn id&gt;               radar station id 
 *
 * ------ for grids:
 *
 *   descr=&lt;descriptor&gt;        ADDE descriptor name
 *   param=&lt;param list&gt;        parameter code list
 *   time=&lt;model run time&gt;     time in hhmmss format
 *   day=&lt;model run day&gt;       day in ccyyddd format
 *   lev=&lt;level list&gt;          list of requested levels (value or SFC, MSL 
 *                               or TRO)
 *   ftime=&lt;forecast time&gt;     valid time (hhmmss format) (use with fday)
 *   fday=&lt;forecast day&gt;       forecast day (ccyyddd)
 *   fhour=&lt;forecast hours&gt;    forecast hours (offset from model run time)
 *                                (hhmmss format)
 *   num=&lt;max&gt;                 maximum number of grids to return (nn)
 *
 * ------ for point data:
 *
 *   descr=&lt;descriptor&gt;        ADDE descriptor name
 *   pos=&lt;position&gt;            request an absolute or relative ADDE 
 *                               position number
 *   select=&lt;select clause&gt;    to specify which data is required
 *   param=&lt;param list&gt;        what parameters to return
 *   num=&lt;max&gt;                 maximum number of obs to return
 *   
 * ------ for text data:
 *
 *   descr=&lt;descriptor&gt;        ADDE descriptor name 
 *                             (may also be "descr=FILE=filename")
 *   file=&lt;filename&gt;           name of text file to read
 *
 * ------ for weather text data:
 *
 *   group=&lt;group&gt;         weather text group (default= RTWXTEXT)
 *   prod=&lt;product&gt;        predefind product name
 *   apro=&lt;val1 .. valn&gt;   AFOS/AWIPS product headers to match (don't 
 *                         use with wmo keyword
 *   astn=&lt;val1 .. valn&gt;   AFOS/AWIPS stations to match
 *   wmo= &lt;val1 .. valn&gt;   WMO product headers to match (don't 
 *                         use with apro keyword
 *   wstn=&lt;val1 .. valn&gt;   WMO stations to match
 *   day=&lt;start end&gt;       range of days to search
 *   dtime=&lt;numhours&gt;      maximum number of hours to search back (def=96)
 *   match=&lt;match strings&gt; list of character match strings to find from text
 *   num=&lt;num&gt;             number of matches to find (def=1)
 *
 * ------ for observational text data:
 *
 *   group=&lt;group&gt;         weather text group (default= RTWXTEXT)
 *   descr=&lt;descriptor&gt;    weather text subgroup (default=SFCHOURLY)
 *   id=&lt;id1 id2 ... idn&gt;  list of station ids
 *   co=&lt;co1 co2 ... con&gt;  list of countries
 *   reg=&lt;reg1 reg2..regn&gt; list of regions
 *   newest=&lt;day hour&gt;     most recent time to allow in request 
 *                         (def=current time)
 *   oldest=&lt;day hour&gt;     oldest observation time to allow in request
 *   type=&lt;type&gt;           numeric value for the type of ob
 *   nhours=&lt;numhours&gt;     maximum number of hours to search
 *   num=&lt;num&gt;             number of matches to find (def=1)
 *
 * -------------------------------------------------------------------------
 * The following keywords are required:
 *
 *   group 
 *   descr (except datasetinfo)
 *
 * an example URL for images might look like:
 *
 *   adde://viper/imagedata?group=gvar&amp;band=1&amp;user=tjj&amp;proj=6999&amp;version=1
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

  /** The default number of lines for an image request */
  private final int DEFAULT_LINES = 480;

  /** The default number of elements for an image request */
  private final int DEFAULT_ELEMS = 640;

  /** Size of an ADDE trailer */
  private final static int TRAILER_SIZE = 92;

  /** Size of an ADDE request */
  private final static int REQUEST_SIZE = 120;

  /** Size of an ADDE error message */
  private final static int ERRMSG_SIZE = 72;

  /** Size of an ADDE error message offset */
  private final static int ERRMSG_OFFS = 8;

  /** Default port. TODO: Update this to be 112 */
  private final static int PORT = 500;

  /** Flag for "compress" compression.  Used to be synonymous 
      with the port used for compress transfer */
  private final static int COMPRESS = 503;

  /** Flag for GZip compression.  Used to be synonymous with the 
      port used for compressed transfers */
  private final static int GZIP = 112;

  /**
   * ADDE Version 1 indicator
   */
  private final static int VERSION_1 = 1;

  // ADDE server requests
  /** AGET request type */
  public final static int AGET = 0;
  /** ADIR request type */
  public final static int ADIR = 1;
  /** LWPR request type */
  public final static int LWPR = 2;
  /** GDIR request type */
  public final static int GDIR = 3;
  /** GGET request type */
  public final static int GGET = 4;
  /** MDKS request type */
  public final static int MDKS = 5;
  /** TXTG request type */
  public final static int TXTG = 6;
  /** WTXG request type */
  public final static int WTXG = 7;
  /** OBTG request type */
  public final static int OBTG = 8;


  // ADDE data types (not used?)
  private final static int IMAGE = 100;
  private final static int GRID  = 101;
  private final static int POINT = 102;
  private final static int TEXT  = 103;
  private final static int WXTEXT  = 104;
  private final static int OBTEXT  = 105;

  private int numBytes = 0;
  private int dataType = IMAGE;

  private byte[] binaryData = null;   // byte array to hold extra binary data

  /** request type - default to AGET */
  private int reqType = AGET;

  /** debug flag  - value can be overrided with debug=true */
  private boolean debug = false;

  /** port to use for compression */
  private int portToUse = PORT;   // DRM 03-Mar-2001

  /** compression type */
  private int compressionType = PORT; 

  /**
   *
   * Constructor: just sets URL and calls superclass constructor.
   * Actual network connection is established in connect().
   *
   * @param url   url to use
   */
  AddeURLConnection(URL url)
    throws IOException
  {
    super(url);
    this.url = normalizeURL(url);
  } 

  /**
   *
   * Establishes an ADDE connection using the URL passed to the
   * constructor.  Opens a socket on the ADDE port, and formulates
   * an ADDE image data request based on the file portion of URL.
   * <p>  
   * An example URL might look like:
   * <p>
   *   adde://viper.ssec.wisc.edu/image?group=gvar&amp;band=1&amp;mag=-8&amp;version=1
   *   
   */
  synchronized public void connect ()
    throws IOException, AddeURLException
  {

    // First, see if we can use the URL passed in
    // verify the service is one we can handle 
    // get rid of leading /
    // keep original to preserve case for user= clause
    String requestOriginal = url.getFile().substring(1);
    String request = requestOriginal.toLowerCase();
    debug = debug || request.indexOf("debug=true") >= 0;

    if (debug) System.out.println("host from URL: " + url.getHost());
    if (debug) System.out.println("file from URL: " + url.getFile());

    if (!request.startsWith("image") && 
        (!request.startsWith("datasetinfo")) &&
        (!request.startsWith("text")) &&
        (!request.startsWith("wxtext")) &&
        (!request.startsWith("obtext")) &&
        (!request.startsWith("grid"))   && 
        (!request.startsWith("point"))  )
    {
        throw new AddeURLException("Request for unknown data");
    }

    // service - for area files, it's either AGET (Area GET) or 
    // ADIR (AREA directory)
    byte [] svc = null;
    if (request.startsWith("imagedir"))
    {
        svc = "adir".getBytes();
        reqType = ADIR;
    }
    else if (request.startsWith("datasetinfo"))
    {
        svc = "lwpr".getBytes();
        reqType = LWPR;
    }
    else if (request.startsWith("text"))
    {
        svc = "txtg".getBytes();
        reqType = TXTG;
    }
    else if (request.startsWith("wxtext"))
    {
        svc = "wtxg".getBytes();
        reqType = WTXG;
    }
    else if (request.startsWith("obtext"))
    {
        svc = "obtg".getBytes();
        reqType = OBTG;
    }
    else if (request.startsWith("image"))
    {
        svc = "aget".getBytes();
        reqType = AGET;
    }
    else if (request.startsWith("griddir"))
    {
        svc = "gdir".getBytes();
        reqType = GDIR;
    }
    else if (request.startsWith("grid"))
    {
        svc = "gget".getBytes();
        reqType = GGET;
    }
    else if (request.startsWith("point"))
    {
        svc = "mdks".getBytes();
        reqType = MDKS;
    }
    else
    {
      throw new AddeURLException(
          "Invalid or unsupported ADDE service= "+svc.toString() );
    }
    if (debug) System.out.println("Service = " + new String(svc));

    // prep for real thing - get cmd from file part of URL
    int test = request.indexOf("?");
    String uCmd = (test >=0) ? request.substring(test+1) : request;
    if (debug) System.out.println("uCmd="+uCmd);

    // build the command string
    StringBuffer sb = new StringBuffer();
    switch (reqType)
    {
        case AGET:
            sb.append(decodeAGETString(uCmd));
            break;
        case ADIR:
            sb.append(decodeADIRString(uCmd));
            break;
        case LWPR:
            sb.append(decodeLWPRString(uCmd));
            break;
        case GDIR:
            sb.append(decodeGDIRString(uCmd));
            break;
        case GGET:
            sb.append(decodeGDIRString(uCmd));
            break;
        case MDKS:
            sb.append(decodeMDKSString(uCmd));
            break;
        case TXTG:
            sb.append(decodeTXTGString(uCmd));
            break;
        case WTXG:
            sb.append(decodeWTXGString(uCmd));
            break;
        case OBTG:
            sb.append(decodeOBTGString(uCmd));
            break;
    }

    // indefinitely use ADDE version 1 unless it's a GGET request
    sb.append(" version=");
    sb.append((reqType == GGET || reqType == WTXG) ? "A" : "1");

    // now convert to array of bytes for output since chars are two byte
    String cmd = sb.toString().toUpperCase();
    if (debug) System.out.println(cmd);
    byte [] ob = cmd.getBytes();

    // get some other stuff

    // user initials - pass on what client supplied in user= keyword
    byte [] usr; 
    String testStr = getValue(requestOriginal, "user=", "XXXX");
    if (debug) System.out.println("user = " + testStr);
    usr = testStr.getBytes();

    // project number - we won't validate, but make sure it's there
    int proj = 0;
    testStr = getValue(uCmd, "proj=", "0");
    if (debug) System.out.println("proj = " + testStr);
    try {
      proj = Integer.parseInt(testStr);
    } catch (NumberFormatException e) {
      // TODO: Should we really throw an exception or just let it default to 0?
      throw new AddeURLException("Invalid project number: " + testStr);
    }

    // compression 
    testStr = getValue(uCmd, "compress=", "none");
    if (debug) System.out.println("compression = " + testStr);
    if (testStr.equalsIgnoreCase("gzip")) {

      compressionType = GZIP;

    } else if (testStr.equals("compress") ||
               testStr.equals("true")) {

      // check to see if we can do uncompression
      try {
          Class c = Class.forName("HTTPClient.UncompressInputStream");
          compressionType = COMPRESS;
      } catch (ClassNotFoundException cnfe) {
          System.err.println(
            "Uncompression code not found, turning compression off");
      }

    } 

// end of request decoding
//-------------------------------------------------------------------------
// now write this all to the port

    // first figure out which port to connect on.  This can either be
    // one specified by the user.  
    //
    // The priority is URL port, port=keyword, compression port (default)

    if (url.getPort() == -1) { // not specified as part of URL

      // see if there is a port=keyword

      // default to the compression type
      portToUse = compressionType;
      testStr = getValue(uCmd, "port=", null);
      try {
        portToUse = Integer.parseInt(testStr);
      } catch (NumberFormatException e) {
        // just use default
        if (testStr != null) {
          System.out.println(
            "Warning: Invalid port number \"" + testStr + 
            "\" specified;  using default port " + portToUse + " instead");
        }
      }
    } else {  // specified 
      portToUse = url.getPort();
    }

    if (debug)  {
      System.out.println("connecting on port " + portToUse + " using " +
                         (compressionType == GZIP
                             ? "gzip"
                             : compressionType == COMPRESS
                                 ? "compress"
                                 : "no") + " compression.");
    }

    Socket t;
    try {
      t = new Socket(url.getHost(), portToUse);   // DRM 03-Mar-2001
    } catch (UnknownHostException e) {
      throw new AddeURLException(e.toString());
    }

    dos = new DataOutputStream ( t.getOutputStream() );

    /*
     Now start pumping data to the server.  The sequence is:

        -  ADDE version (1 for now)
        -  Server IP and Port number  (latter used to determine compression)
        -  Service Type (AGET, ADIR, etc)
        -  Server IP and Port number (again)
        -  Client address
        -  User, project, password
        -  Service Type (again)
        -  Actual request

     */

    // send version number - ADDE seems to be stuck at 1
    dos.writeInt(VERSION_1);

    // send IP address of server
    // we know the server IP address is good cause we used it above
    byte [] ipa = new byte[4];
    InetAddress ia = InetAddress.getByName(url.getHost());
    ipa = ia.getAddress();
    dos.write(ipa, 0, ipa.length);

    // send ADDE port number which server uses to determine compression
    dos.writeInt(compressionType);

    // send the service type
    dos.write(svc, 0, svc.length);

    // now build and send request block, repeat some stuff
    // server IP address, port
    dos.write(ipa, 0, ipa.length);
    dos.writeInt(compressionType);   // DRM 03-Mar-2001

    // client IP address
    InetAddress lh = InetAddress.getLocalHost();
    ipa = lh.getAddress();
    dos.write(ipa, 0, ipa.length);

    // gotta send 4 user bytes, ADDE protocol expects it
    if (usr.length <= 4) {
      dos.write(usr, 0, usr.length);
      for (int i = 0; i < 4 - usr.length; i++) {
        dos.writeByte(' ');
      }
    } else {
      // if id entered was > 4 chars, complain
      throw new AddeURLException("Invalid user id: " + new String(usr));
    }

    dos.writeInt(proj);

    // password chars - not used either
    byte [] pwd = new byte[12];
    dos.write(pwd, 0, pwd.length);

    // service - resend svc array 
    dos.write(svc, 0, svc.length);

    // Write out the data.  There are 2 cases:
    //
    //  1) ob.length <= 120 
    //  2) ob.length > 120 
    //
    // In either case, there may or may not be additional binary data
    // 

    int numBinaryBytes = 0;
    if (binaryData != null) numBinaryBytes = binaryData.length;

    if (ob.length > REQUEST_SIZE)
    {
      dos.writeInt(ob.length + numBinaryBytes); // number of additional bytes
      dos.writeInt(ob.length);                  // number of bytes in request
      for (int i=0; i < REQUEST_SIZE - 4; i++) {  // - 4 accounts for prev line
        dos.writeByte(0);
      }
      dos.write(ob,0,ob.length);
    } else {
      if (debug)  System.out.println("numBinaryBytes= " + numBinaryBytes);
      dos.writeInt(numBinaryBytes);
      dos.write(ob, 0, ob.length);
      for (int i=0; i < REQUEST_SIZE - ob.length; i++) {
        dos.writeByte(' ');
      }
    }

    if (numBinaryBytes > 0) dos.write(binaryData, 0, numBinaryBytes);

    is = (compressionType == GZIP) 
        ? new GZIPInputStream(t.getInputStream())
        : (compressionType == COMPRESS)
            ? new UncompressInputStream(t.getInputStream())
            : t.getInputStream();
    dis = new DataInputStream(is);
    if (debug && (compressionType != portToUse) ) {
        System.out.println("Compression is turned ON using " +
                            ((compressionType == GZIP)?"GZIP":"compress"));
    }

    // get response from server, byte count coming back
    numBytes = dis.readInt();
    if (debug) System.out.println("server is sending: " + numBytes + " bytes");

    // if server returns zero, there was an error so read trailer and exit
    if (numBytes == 0) {
      byte [] trailer = new byte[TRAILER_SIZE];
      dis.readFully(trailer, 0, trailer.length);
      String errMsg = new String(trailer, ERRMSG_OFFS, ERRMSG_SIZE);
      throw new AddeURLException(errMsg);
    }

    // if we made it to here, we're getting data
    connected = true;

  }

  private static char AMPERSAND = '&';

  /**
   * Search for a value in the string and return the value or the default
   * @param stringToSearch    String to search for the value
   * @param key               key to search for (includes '=' to disambiguate);
   * @param default           default value
   * @return value in string
   */
  private String getValue(String stringToSearch, String key, String deflt) {
    int startIdx = stringToSearch.toLowerCase().lastIndexOf(key);
    String retVal = deflt;
    if (startIdx >= 0) {
      int endIdx = stringToSearch.indexOf(AMPERSAND, startIdx);
      if (endIdx == -1) { // last on line
        endIdx = stringToSearch.length(); 
      }
      retVal = stringToSearch.substring((startIdx + key.length()), endIdx);
    } 
    return retVal;
  }

  /**
   * Get the request type 
   * @return  type of request (ADIR, AGET, etc)
   */
  public int getRequestType()
  {
      return reqType;
  }

  /**
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
     * <pre>
     *
     *   group=&lt;groupname&gt;         ADDE group name
     *   descr=&lt;descriptor&gt;        ADDE descriptor name
     *   band=&lt;band&gt;               spectral band or channel number 
     *   mag=&lt;lmag&gt; &lt;emag&gt;         image magnification, postitive for blowup, 
     *                               negative for blowdown (default = 1, 
     *                                  emag=lmag) 
     *   latlon=&lt;lat&gt; &lt;lon&gt;        lat/lon point to center image on 
     *   linele=&lt;lin&gt; &lt;ele&gt; &lt;type&gt; line/element to center image on 
     *   place=&lt;placement&gt;         placement of lat/lon or linele points 
     *                               (center or upperleft (def=center)) 
     *   pos=&lt;position&gt;            request an absolute or relative ADDE position
     *                               number
     *   size=&lt;lines&gt; &lt;elements&gt;   size of image to be returned
     *   unit=&lt;unit&gt;               to specify calibration units other than the 
     *                               default 
     *   spac=&lt;bytes&gt;              number of bytes per data point, 1, 2, or 4 
     *   doc=&lt;yes/no&gt;              specify yes to include line documentation 
     *                               with image (def=no) 
     *   aux=&lt;yes/no&gt;              specify yes to include auxilliary information
     *                               with image 
     *   time=&lt;time1&gt; &lt;time2&gt;      specify the time range of images to select
     *                               (def=latest image if pos not specified)
     *   day=&lt;day&gt;                 specify the day of the images to select
     *                               (def=latest image if pos not specified)
     *   cal=&lt;cal type&gt;            request a specific calibration on the image 
     *   id=&lt;stn id&gt;               radar station id
     *   trace=&lt;0/1&gt;               setting to 1 tells server to write debug 
     *                               trace file (imagedata, imagedirectory)
     *
     * the following keywords are required:
     *
     *   group
     *
     * an example URL might look like:
     *   adde://viper/imagedata?group=gvar&amp;band=1
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
        String spaceString = "spac=x";
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
			    lonString = negateLongitude(tempString);
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
                if (tempString.trim().equalsIgnoreCase("all")) {
                   numlinString = "99999";
                   numeleString = "99999";
                } 
                else if (tempString.indexOf(" ") > 0) //is linele, not just lin
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
            if (testString.startsWith("spa"))       // aux keyword
            {
                spaceString = testString;
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
        buf.append(spaceString);
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
     * <pre>
     *   group=&lt;groupname&gt;       ADDE group name
     *   descr=&lt;descriptor&gt;      ADDE descriptor name
     *   param=&lt;param list&gt;      parameter code list
     *   time=&lt;model run time&gt;   time in hhmmss format
     *   day=&lt;model run day&gt;     day in ccyyddd format
     *   lev=&lt;level list&gt;        list of requested levels (value or SFC, MSL 
     *                             or TRO)
     *   ftime=&lt;forecast time&gt;   valid time (hhmmss format) (use with fday)
     *   fday=&lt;forecast day&gt;     forecast day (ccyyddd)
     *   fhour=&lt;forecast hours&gt;  forecast hours (offset from model run time)
     *                                (hhmmss format)
     *   lat=&lt;min lat&gt; &lt;max lat&gt; latitude bounding box (needs lon specified)
     *   lon=&lt;min lon&gt; &lt;max lon&gt; longitude bounding box (needs lat specified)
     *   row=&lt;min row&gt; &lt;max row&gt; row bounding box (needs col specified)
     *   col=&lt;min col&gt; &lt;max col&gt; column bounding box (needs row specified)
     *   skip=&lt;row&gt; &lt;col&gt;        skip factors for rows and columns (def = 1 1)
     *   num=&lt;max&gt;               maximum number of grids (nn) to return (def=1)
     *   trace=&lt;0/1&gt;             setting to 1 tells server to write debug 
     *                             trace file (imagedata, imagedirectory)
     *
     * the following keywords are required:
     *
     *   group
     *
     * an example URL might look like:
     *   adde://noaaport/griddirectory?group=ngm&amp;num=10
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
      String subsetString = null;
      String latString = null;
      String lonString = null;
      String rowString = null;
      String colString = null;
      String srcString = null;
      String skip = null;

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
          String iHMS = 
             testString.substring(testString.indexOf("=") + 1).trim();
          buf.append(iHMS);
          if (iHMS.length() < 5) buf.append("0000");

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

        } else if (testString.startsWith("lat")) {
          latString = 
              ensureTwoValues(
                  testString.substring(testString.indexOf("=") + 1));

        } else if (testString.startsWith("lon")) {
          lonString = 
            adjustLongitudes(
              ensureTwoValues(
                  testString.substring(testString.indexOf("=") + 1)));
      

        } else if (testString.startsWith("row")) {
          rowString = 
              ensureTwoValues(
                  testString.substring(testString.indexOf("=") + 1));

        } else if (testString.startsWith("col")) {
          colString = 
              ensureTwoValues(
                  testString.substring(testString.indexOf("=") + 1));

        } else if (testString.startsWith("skip")) {
          skip = 
              ensureTwoValues(
                  testString.substring(testString.indexOf("=") + 1));
        
        // added with great pains for James DRM 2001-07-05 ;-)
        } else if (testString.startsWith("src")) {
          buf.append(" ");
          buf.append(testString);

        } else if (testString.startsWith("gpro")) {
          buf.append(" ");
          buf.append(testString);

        } else if (testString.startsWith("trang")) {
          buf.append(" ");
          buf.append(testString);

        } else if (testString.startsWith("frang")) {
          buf.append(" ");
          buf.append(testString);

        } else if (testString.startsWith("drang")) {
          buf.append(" ");
          buf.append(testString);

        /*
        } else {
          System.out.println("Unknown token = "+testString);
        */
        } 
      }
      buf.append(" ");
      buf.append(numString);
      buf.append(" ");
      buf.append(traceString);
      buf.append(" ");
      //buf.append(" version=A ");

      // Create a subset string
      if (latString != null && lonString != null)
      {
          StringBuffer subBuf = new StringBuffer();
          subBuf.append("subset=");
          subBuf.append(latString);
          subBuf.append(" ");
          subBuf.append(lonString);
          subBuf.append(" ");
          subBuf.append((skip == null) ? "1 1" : skip);
          subBuf.append(" LATLON");
          subsetString = subBuf.toString();
          if (debug) System.out.println(subsetString);
      }
      else if (rowString != null && colString != null)
      {
          StringBuffer subBuf = new StringBuffer();
          subBuf.append("subset=");
          subBuf.append(rowString);
          subBuf.append(" ");
          subBuf.append(colString);
          subBuf.append(" ");
          subBuf.append((skip == null) ? "1 1" : skip);
          subBuf.append(" ROWCOL");
          subsetString = subBuf.toString();
          if (debug) System.out.println(subsetString);
      }
      else if (skip != null) {  // only skip specified
          StringBuffer subBuf = new StringBuffer();
          subBuf.append("subset=1 99999 1 99999");
          subBuf.append(" ");
          subBuf.append(skip);
          subBuf.append(" ROWCOL");
          subsetString = subBuf.toString();
          if (debug) System.out.println(subsetString);
      }

      if (subsetString != null) buf.append(subsetString);

      // create command string
      String posParams = //new String (
         groupString + " " + descrString + " " + sizeString + " " ;//);

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
     *   group=&lt;groupname&gt;         ADDE group name
     *   descr=&lt;descriptor&gt;        ADDE descriptor name
     *   band=&lt;band&gt;               spectral band or channel number 
     *   pos=&lt;position&gt;            request an absolute or relative ADDE position
     *                               number
     *   doc=&lt;yes/no&gt;              specify yes to include line documentation 
     *                               with image (def=no) 
     *   aux=&lt;yes/no&gt;              specify yes to include auxilliary information
     *                               with image 
     *   time=&lt;time1&gt; &lt;time2&gt;      specify the time range of images to select
     *                               (def=latest image if pos not specified)
     *   day=&lt;day&gt;                 specify the day of the images to select
     *                               (def=latest image if pos not specified)
     *   cal=&lt;cal type&gt;            request a specific calibration on the image 
     *   id=&lt;stn id&gt;               radar station id
     *   trace=&lt;0/1&gt;               setting to 1 tells server to write debug 
     *                               trace file (imagedata, imagedirectory)
     *
     * the following keywords are required:
     *
     *   group
     *
     * an example URL might look like:
     *   adde://viper/imagedirectory?group=gvar&amp;descr=east1km&amp;band=1
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
        String posString = "0";
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
                if (tempString.equals("")) {  // null string
                  posString = "0";
                } else {

                  // see if a single argument
                  StringTokenizer stp = new StringTokenizer(tempString," ");
                  if (stp.countTokens() == 1) {
                    if (tempString.equals("all")) {  // put in numeric
                      posString =  "1095519264";

                    } else if (tempString.equals("x")) {
                      posString = tempString;

                    } else {
                      int posval = Integer.parseInt(stp.nextToken().trim());
                      if (posval < 0) {  // if value < 0 insert 0 as ending
                        posString = tempString + " 0";
                      } else {
                        posString = tempString;  // else default
                      }
                    }

                  } else {  // more than one value...just copy it
                    posString = tempString;
                  }

                }

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
            //new String(
                groupString + " " + descrString + " " + posString + " ";//);
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
     * Decode the ADDE request for a text file.
     *
     * <pre>
     * there can be any valid combination of the following supported keywords:
     *
     *   file=&lt;filename&gt;    the text file name on the server
     *   descr=&lt;dataset&gt;    the dataset name on the server
     *   group=&lt;group&gt;      the ADDE group name for this TEXT
     *
     * the following keywords are required:
     *
     *   file or descr
     *
     * an example URL might look like:
     *   adde://viper/text?group=textdata&amp;file=myfile.txt
     *   
     * </pre>
     */
    public StringBuffer decodeTXTGString(String uCmd)
    {
        StringBuffer buf = new StringBuffer();
        String testString;
        String groupString = null;
        String filenameString = null;
        String descrString = null;

        StringTokenizer cmdTokens = new StringTokenizer(uCmd, "&");
        while (cmdTokens.hasMoreTokens())
        {
            testString = cmdTokens.nextToken();
            if (testString.startsWith("desc"))
            {
                descrString =
                    testString.substring(testString.indexOf("=") + 1);
            }

            if (testString.startsWith("file"))
            {
                filenameString = "FILE="+
                    testString.substring(testString.indexOf("=") + 1);
            }

            if (testString.startsWith("grou"))
            {
                groupString = 
                    testString.substring(testString.indexOf("=") + 1);
            }

        }

        buf.append(groupString);
        buf.append(" ");
        buf.append(descrString);
        buf.append(" ");
        buf.append(filenameString);
        return buf;
    }


    /**
     * Decode the ADDE request for a weather text.
     *
     * <pre>
     * there can be any valid combination of the following supported keywords:
     *
     *   group=&lt;group&gt;         weather text group (default= RTWXTEXT)
     *   prod=&lt;product&gt;        predefind product name
     *   apro=&lt;val1 .. valn&gt;   AFOS/AWIPS product headers to match (don't 
     *                         use with wmo keyword
     *   astn=&lt;val1 .. valn&gt;   AFOS/AWIPS stations to match
     *   wmo= &lt;val1 .. valn&gt;   WMO product headers to match (don't 
     *                         use with apro keyword
     *   wstn=&lt;val1 .. valn&gt;   WMO stations to match
     *   day=&lt;start end&gt;       range of days to search
     *   dtime=&lt;numhours&gt;      maximum number of hours to search back (def=96)
     *   match=&lt;match strings&gt; list of character match strings to find from text
     *   num=&lt;num&gt;             number of matches to find (def=1)
     *
     * the following keywords are required:
     *
     *   day  (should default to current, but there's a bug)
     *   apro, astn or wstn
     *
     * an example URL might look like:
     *   adde://viper/text?group=textdata&amp;file=myfile.txt
     *   
     * </pre>
     */
    public StringBuffer decodeWTXGString(String uCmd)
    {
        StringBuffer buf = new StringBuffer();
        String testString;
        String tempString;
        String numString = "NUM=1";
        String dTimeString = "DTIME=96.0000";
        String traceString = "TRACE=0";
        // Mandatory strings
        String groupString = "RTWXTEXT";

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
            if (testString.startsWith("apro"))       // apro keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("astn"))       // astn keyword
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
            if (testString.startsWith("mat"))        // match keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("prod"))       // prod keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("sour"))       // source keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("wmo"))       // wmo keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("wstn"))       // wstn keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("tra"))       // trace keyword
            {
                traceString = testString;
            }
            else
            if (testString.startsWith("num"))       // num keyword
            {
                numString = testString;
            }
            else
            if (testString.startsWith("dtim"))       // dtime keyword
            {
                dTimeString = testString;
            }
        } 

        buf.append(" ");
        buf.append(dTimeString);
        buf.append(" ");
        buf.append(numString);
        buf.append(" ");
        buf.append(traceString);
        // now create command string
        String posParams = 
            //new String(groupString + " ");
                         groupString + " ";
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
     * Decode the ADDE request for a weather observation text.
     *
     * <pre>
     * there can be any valid combination of the following supported keywords:
     *
     *
     *   group=&lt;group&gt;         weather text group (default= RTWXTEXT)
     *   descr=&lt;descriptor&gt;    weather text subgroup (default=SFCHOURLY)
     *   id=&lt;id1 id2 ... idn&gt;  list of station ids
     *   co=&lt;co1 co2 ... con&gt;  list of countries
     *   reg=&lt;reg1 reg2..regn&gt; list of regions
     *   newest=&lt;day hour&gt;     most recent time to allow in request 
     *                         (def=current time)
     *   oldest=&lt;day hour&gt;     oldest observation time to allow in request
     *   type=&lt;type&gt;           numeric value for the type of ob
     *   nhours=&lt;numhours&gt;     maximum number of hours to search
     *   num=&lt;num&gt;             number of matches to find (def=1)
     *
     * the following keywords are required:
     *
     *   group
     *   descr
     *   id, co, or reg
     *
     * an example URL might look like:
     *  adde://adde.ucar.edu/obtext?group=rtwxtext&amp;descr=sfchourly&amp;id=kden&amp;num=2
     *   
     * </pre>
     */
    public StringBuffer decodeOBTGString(String uCmd)
    {
        StringBuffer buf = new StringBuffer();
        String testString;
        String tempString;
        String numString = "NUM=1";
        String traceString = "TRACE=0";
        // Mandatory strings
        String groupString = "RTWXTEXT";
        String descrString = "SFCHOURLY";
        String idreqString = "IDREQ=LIST";

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
            if (testString.startsWith("desc"))
            {
                descrString = 
                    testString.substring(testString.indexOf("=") + 1);
            }
            else
            if (testString.startsWith("id"))       // id keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("co"))       // co keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("reg"))       // reg keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("nhou"))       // nhour keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("new"))       // newest keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("old"))       // oldest keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("type"))       // type keyword
            {
                buf.append(" ");
                buf.append(testString);
            }
            else
            if (testString.startsWith("tra"))       // trace keyword
            {
                traceString = testString;
            }
            else
            if (testString.startsWith("num"))       // num keyword
            {
                numString = testString;
            }
        } 

        buf.append(" ");
        buf.append(numString);
        buf.append(" ");
        buf.append(traceString);
        // now create command string
        String posParams = 
            //new String(groupString + " " + descrString + " " + idreqString);
                       groupString + " " + descrString + " " + idreqString;
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
     *
     *   group=&lt;groupname&gt;    ADDE group name
     *   type=&lt;datatype&gt;      ADDE data type.  Must be one of the following:
     *                             IMAGE, POINT, GRID, TEXT, NAV
     *                        the default is the IMAGE type.
     *
     * the following keywords are required:
     *
     *   group
     *
     * an example URL might look like:
     *   adde://viper/datasetinfo?group=gvar&amp;type=image
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
     * If the request contains specific parameters (eg param=t),
     * then the class variable binaryData is set to this param string
     *
     * <pre>
     *   group=&lt;groupname&gt;         ADDE group name
     *   descr=&lt;descriptor&gt;        ADDE descriptor name
     *   pos=&lt;position&gt;            request an absolute or relative ADDE 
     *                               position number
     *   select=&lt;select clause&gt;    to specify which data is required
     *   param=&lt;param list&gt;        what parameters to return
     *                             eg param=t[c]
     *                             note that the units [c] are ignored by server
     *                             it is the clients task to convert units
     *                             Note that if "param=" is used, 
     *                             binaryData is set to the
     *                             (processed) parameter list
     *   max=&lt;max&gt;                 maximum number of obs to return
     *   trace=&lt;0/1&gt;               setting to 1 tells server to write debug 
     *                               trace file (imagedata, imagedirectory)
     *   binaryData=&lt;param list&gt;   because an unlimited number of parameters may
     *                             be requested, these must be packaged up at the end
     *                             of the adde request, and this is known as the
     *                             "binary data" part of the request
     *
     * the following keywords are required:
     *
     *   group
     *
     * an example URL might look like:
     *   adde://rtds/point?group=neons&amp;descr=metar
     *   
     * </pre>
     */
    private StringBuffer decodeMDKSString(String uCmd)
    {
        StringBuffer buf = new StringBuffer();
        String testString = null;
        // Mandatory strings
        String groupString = null;
        String descrString = null;
        String maxString = "max=1";
        String numString = "";
        // Options strings
        String posString = "pos=0";
        String traceString = "trace=0";
        String selectString = "";
        String parmString = "";
        String justTheParametersString = "";
        String justTheSelectString = "";
        String sBinaryData = "";
        boolean posInDescriptor = false;
        // in hard coded notation, the binaryData for "param=t" would look like:
        // binaryData = new byte[4];
        // binaryData[0] = (byte) 'T';
        // binaryData[1] = (byte) ' ';
        // binaryData[2] = (byte) ' ';
        // binaryData[3] = (byte) ' ';

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
                int pos = descrString.indexOf(".");
                if (pos >=0) {
                    posString = "pos=" + descrString.substring(pos+1);
                    descrString = descrString.substring(0,pos);
                    posInDescriptor = true;
                }
            }
            else
            // in McIDAS Clients the parameter request string contains param=
            // but the adde server looks for parm=
            // this bit of code forces this change so that Java Clients behave
            // the same as McIDAS Clients
            if (testString.startsWith("par")) 
            {
                justTheParametersString = 
                    testString.substring(testString.indexOf("=") + 1) ;
                parmString = 
                   "parm=" + justTheParametersString;
                if (debug)  System.out.println("paramString = " + parmString);
                sBinaryData =   
                    new String(decodePARAMString(justTheParametersString));
                sBinaryData = sBinaryData.toUpperCase();
                binaryData = sBinaryData.getBytes();
            }
            else
            if (testString.startsWith("select"))
            {
                justTheSelectString = 
                    testString.substring(testString.indexOf("=") + 1) ;
                selectString = 
                   "select=" + new String(
                       decodeSELECTString(justTheSelectString));
                if (debug) 
                    System.out.println("Server selectString = " + selectString);
            }
            else
            // similarly, McIDAS Clients use num= but the server wants max=
            if (testString.startsWith("num"))
            {
                maxString = 
                   "max=" + testString.substring(testString.indexOf("=") + 1) ;
            }
            else
            // allow for clever people who really know that the server uses
            // max =  :-)
            if (testString.startsWith("max"))
            {
                maxString = testString;
            }
            // now get the rest of the keywords (but filter out non-needed)
            else
            if (testString.startsWith("tra"))       // trace keyword
            {
                traceString = testString;
            }
            else
            if (testString.startsWith("pos") && !posInDescriptor)       
            {
                posString = testString;
            }
        } 
        // fudge the max string in case ALL is specified.  Some servers
        // don't handle all
        if (maxString.trim().equalsIgnoreCase("max=all")) {
            maxString="max=99999";
        }

        // now create command string
        /*
        StringBuffer posParams = 
            new StringBuffer(
                 groupString + " " + descrString + " " + parmString + " " + selectString + " " + posString + " " + traceString + " " + maxString);
        */
        StringBuffer posParams = new StringBuffer();
        posParams.append(groupString);
        posParams.append(" ");
        posParams.append(descrString);
        posParams.append(" ");
        posParams.append(parmString);
        posParams.append(" ");
        posParams.append(selectString);
        posParams.append(" ");
        posParams.append(posString);
        posParams.append(" ");
        posParams.append(traceString);
        posParams.append(" ");
        posParams.append(maxString);

        if (debug) System.out.println("String passed to server = " + posParams);

        // stuff it in at the beginning
        try
        {
            buf.insert(0, posParams.toString());
            if (debug) System.out.println("buf = " + buf);
        }
        catch (StringIndexOutOfBoundsException e)
        {
            System.out.println(e.toString());
            buf = new StringBuffer("");
        }
        return buf;
    }

    /**
     * Helper function for decodeMDKSString to decode
     * the "param=" part of a point data request.
     *
     * @param justTheParametersString   The parameter list which follows 
     *                                  "param=" eg: "id dir spd t[c] td[c]"
     * @return parameter list (padded to length 4 for server)
     *                         without any units (units are ignored by server) 
     *                         eg: "id  dir spd t   td  "
     */
     private String decodePARAMString(String justTheParametersString) {

        String testString = null;
        String thisParam = null;
        String thisUnit  = null;
        StringBuffer buf = new StringBuffer();
        StringTokenizer paramTokens = 
            new StringTokenizer(justTheParametersString, " ");
        while (paramTokens.hasMoreTokens())
        {
            testString = (paramTokens.nextToken()).trim();
            StringTokenizer thisParamToken = 
                new StringTokenizer(testString, "[]");
            thisParam = new String((thisParamToken.nextToken()).trim());
                                                buf.append(thisParam);
            for (int i=thisParam.length(); i < 4; i++) {
                buf.append(" ");
            }

            if (thisParamToken.hasMoreTokens()) {
            // note that the units are ignored by the server
            // it is the client's responsibility to do unit conversion
                thisUnit = (thisParamToken.nextToken()).trim();
                if (debug) System.out.println("This Unit = " + thisUnit);
            }
        }


        return (buf.toString());
    }

    /**
     * Helper function for decodeMDKSString to decode
     * the "select=" part of a point data request.
     *
     * @param justTheSelectString   The select list which follows "select=" eg:
     *                'id ymml; time 12 18; day 1999316; t[c] 20 30; td 270 276'
     *
     * @return The select list formatted for the server eg:
     *         'id ymml' 'time 12 to 18' 'day 1999316' 't 20 to 30 c' 'td 270 to 276'
     *
     *   Reference
     *   McIDAS 7.6 source code: m0psort.for
     */
    private String decodeSELECTString(String justTheSelectString) {

        String testString = null;
        String entireSelectString = null;
        // String trimmedSelectString = null;
        String thisSelect = null;
        String thisUnit  = null;
        StringBuffer buf = new StringBuffer();
        StringTokenizer entireSelectToken = 
            new StringTokenizer(justTheSelectString, "'");
        entireSelectString = (entireSelectToken.nextToken()).trim();
        //
        // Break SELECT string up into parts
        //
        StringTokenizer selectTokens = 
            new StringTokenizer(entireSelectString, ";");
        while (selectTokens.hasMoreTokens())
        {
            thisSelect = (selectTokens.nextToken()).trim();
            if (debug) System.out.println(" this Select = " + thisSelect);
            //
            // Break into individual clauses eg:
            // t[c] 20 30
            //
            StringTokenizer thisSelectToken = 
                new StringTokenizer(thisSelect, " ");
            int tokenCount = thisSelectToken.countTokens();
            thisSelect = new String(thisSelectToken.nextToken());
            if (debug) System.out.println("this Select = " + thisSelect);

            //
            // Check to see if any units are involved eg:
                                                // t[c]
            if (thisSelect.indexOf("[") > 0) {
                StringTokenizer thisUnitToken = 
                    new StringTokenizer(thisSelect, "[]");
                if (thisUnitToken.hasMoreTokens()) {
                    thisSelect = new String((thisUnitToken.nextToken()).trim());
                    buf.append("'" + thisSelect);
                    if (thisUnitToken.hasMoreTokens()) {
                        thisUnit = 
                            new String((thisUnitToken.nextToken()).trim());
                    }
                }
            } else {
                // no units involved eg:
                // t
                buf.append("'" + thisSelect);
           }

           //
           // Check for first numeric value eg if select='t[c] 20 30':
           // 20
           //
           if (thisSelectToken.hasMoreTokens()) {
                thisSelect = thisSelectToken.nextToken();
                if (debug) System.out.println("this Select = " + thisSelect);
                buf.append(" " + thisSelect);
           }

           //
           // Check for second numeric value eg if select='t[c] 20 30':
           // 30
           //
           if (thisSelectToken.hasMoreTokens()) {
                thisSelect = thisSelectToken.nextToken();
                // server requires TO for a range of values eg:
                // 20 to 30
                buf.append(" TO " + thisSelect);
                if (debug) System.out.println("this Select = " + thisSelect);
           }

           //
           // add unit if specified
           //
           if (thisUnit != null) {
               buf.append(" " + thisUnit);
               thisUnit = null;
           }

           buf.append("' ");
        }


        return (buf.toString());
    }

    /**
     * Ensures that a string is two values.  If only one, then it
     * is returned as s + " " + s 
     * @param s  String to check
     */
    private String ensureTwoValues(String s)
    {
       String retVal = null;
       if (s.trim().indexOf(" ") > 0)  // has multiple values
       {
           StringTokenizer tok = new StringTokenizer(s);
           // return null if more than 2
           if (tok.countTokens() == 2) retVal = s;
       }
       else
       {
           //retVal = new String(s + " " + s);
           retVal = s + " " + s;
       }
       return retVal;
    }

    /**
     * Adjust the longitude from East Postitive to west positive 
     * @param input  String to check
     */
    private String adjustLongitudes(String input)
    {
       input = input.trim();
       String lon1 = negateLongitude(input.substring(0, input.trim().indexOf(" ")).trim());
       String lon2 = negateLongitude(input.substring(input.trim().indexOf(" ")).trim());
       return (lon2 + " " + lon1);
    }

    /**
     * Negate a longitude.  McIDAS convention is positive west.
     * @param eastLongitude  eastLongitude to negate
     */
    private String negateLongitude(String eastLong)
    {
      if (eastLong.indexOf("-") >= 0) // (comes in as -)
        return eastLong.substring(eastLong.indexOf("-") + 1);
      else
        return "-" + eastLong;
    }

    private static String[] replaceWith = {"&", "<", ">", "\'", "\"", "\r", "\n", " "};
    private static String[] replaceString = {"&amp;", "&lt;", "&gt;", "&apos;", "&quot;", "&#13;", "&#10;", "%20" };

    private URL normalizeURL(URL url) {

      String x;
      try {
        x = URLDecoder.decode(url.toString());
      }
      catch (java.lang.Exception e) {
        throw new RuntimeException(e.toString());
      }
      // common case no replacement
      boolean ok = true;
      for (int i=0; i<replaceString.length; i++) {
        int pos = x.indexOf(replaceString[i]);
        ok &= (pos < 0);
      }

      if (!ok) { // gotta do it

        for (int i=0; i<replaceString.length; i++) {
          int pos = -1;
          while ((pos = x.indexOf(replaceString[i])) >=0) {
            if (debug) System.out.println("found " + replaceString[i] + " at " + pos);
            StringBuffer buf = new StringBuffer(x);
            buf.replace(pos, pos+(replaceString[i].length()), replaceWith[i]);
            x = buf.toString();
          }
        }
      }
      if (debug) System.out.println("normalized url = " + x);
      try {
          return new URL(x);
      } catch (Exception e) {}
      return url;
    } 

}
