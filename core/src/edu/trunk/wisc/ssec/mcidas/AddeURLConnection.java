
package edu.wisc.ssec.mcidas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class extends URLConnection, providing the guts of the
 * work to establish an ADDE network connection, put together
 * a request packet, and initiate data flow.
 *
 * <pre>
 *
 * URLs must all have the following format   
 *   adde://host/image?keyword_1=value_1&keyword_2=value_2
 *
 * there can be any valid combination of the following supported keywords:
 *   group - ADDE group name   
 *   descr - ADDE descriptor name   
 *   band - spectral band or channel number
 *   mag - image magnification, postitive for blowup, negative for blowdown
 *   lmag - like mag keyword, but line direction only
 *   emag - like mag keyword, but element direction only
 *   user - ADDE user identification
 *   proj - a valid ADDE project number
 *   lat - latitude to center image on
 *   lon - longitude to center image on
 *   pos - when requesting an absolute or relative ADDE position number
 *   lines - number of lines to include in image
 *   elems - number of elements to include in image
 *   unit - to specify calibration units other than the default
 *   spac - number of bytes per data point, 1, 2, or 4
 *   doc - specify yes to include line documentation with image
 *   aux - specify yes to include auxilliary information with image
 *   time - to specify an image start time
 *   cal - to request a specific calibration on the image
 *   trace - setting non zero tells server to write debug trace file
 *   version - ADDE version number, currently 1
 *
 * the following keywords are required, and version MUST be the last 
 * in the URL:
 *
 *   group, band, user, proj, version
 *
 * an example URL might look like:
 *   adde://viper/image?group=gvar&band=1&user=tjj&proj=6999&version=1
 *   
 * </pre>
 *
 * @author Tommy Jasmin, University of Wisconsin, SSEC
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

    boolean latFlag = false;
    boolean lonFlag = false;
    String latStr = null;
    String lonStr = null;

    Socket t;
    try {
      t = new Socket(url.getHost(), 500);
    } catch (UnknownHostException e) {
      throw new AddeURLException(e.toString());
    }
    is = t.getInputStream();
    dis = new DataInputStream(is);
    dos = new DataOutputStream ( t.getOutputStream() );

    // now formulate and send an ADDE request

    // see if we can use the URL passed in
    System.out.println("host from URL: " + url.getHost());
    System.out.println("file from URL: " + url.getFile());

    // verify the service requested is for image, not grid or md data
    String request = url.getFile();
    if (request.indexOf("image") < 0) {
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
    int port = 500;
    dos.writeInt(port);

    // service - for area files, it's AGET (Area GET)
    byte [] svc = {(byte) 'a', (byte) 'g', (byte) 'e', (byte) 't'};
    dos.write(svc, 0, svc.length);

    // now build and send request block, repeat some stuff
    // server IP address, port
    dos.write(ipa, 0, ipa.length);
    dos.writeInt(port);

    // client IP address
    InetAddress lh = InetAddress.getLocalHost();
    ipa = lh.getAddress();
    dos.write(ipa, 0, ipa.length);

    int startIdx;
    int endIdx;

    // prep for real thing - get cmd from file part of URL
    String uCmd = url.getFile();
    uCmd.toLowerCase();

    // user initials - pass on what client supplied in user= keyword
    byte [] usr; 
    String userStr;
    startIdx = uCmd.indexOf("user=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      userStr = uCmd.substring(startIdx + 5, endIdx);
      usr = userStr.getBytes();
    } else {
      throw new AddeURLException("Must provide user id");
    }
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
      throw new AddeURLException("Must provide a project number");
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

    // group keyword
    startIdx = uCmd.indexOf("group=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(uCmd.substring(startIdx + 6, endIdx));
    }

    // descr keyword
    startIdx = uCmd.indexOf("descr=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx + 6, endIdx));
    } else {
      sb.append(" all");
    }

    // check for optional lat/lon keywords
    startIdx = uCmd.indexOf("lat=");
    if (startIdx > 0) {
      latFlag = true;
      endIdx = uCmd.indexOf('&', startIdx);
      latStr = uCmd.substring(startIdx + 4, endIdx);
    }

    startIdx = uCmd.indexOf("lon=");
    if (startIdx > 0) {
      lonFlag = true;
      endIdx = uCmd.indexOf('&', startIdx);
      lonStr = uCmd.substring(startIdx + 4, endIdx);
    }

    // pos keyword
    startIdx = uCmd.indexOf("pos=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx + 4, endIdx));
    } else {
      sb.append(" 0");
    }

    // earth coordinate positional parameters, or placeholders
    if (latFlag) {
      sb.append(" ec " + latStr + " " + lonStr);
    } else {
      sb.append(" x x x");
    }

    // magnification keyword
    startIdx = uCmd.indexOf("mag=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx + 4, endIdx));
    } else {
      sb.append(" x");
    }

    // lines keyword
    startIdx = uCmd.indexOf("lines=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx + 6, endIdx));
    } else {
      sb.append(" " + Integer.toString(DEFAULT_LINES));
    }

    // elems keyword
    startIdx = uCmd.indexOf("elems=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx + 6, endIdx));
    } else {
      sb.append(" " + Integer.toString(DEFAULT_ELEMS));
    }

    // band keyword
    startIdx = uCmd.indexOf("band=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx, endIdx));
    }

    // line magnification keyword
    startIdx = uCmd.indexOf("lmag=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx, endIdx));
    }

    // element magnification keyword
    startIdx = uCmd.indexOf("emag=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx, endIdx));
    }

    // trace keyword
    startIdx = uCmd.indexOf("trace=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx, endIdx));
    } else {
      sb.append(" trace=0");
    }

    // cal keyword
    startIdx = uCmd.indexOf("cal=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx, endIdx));
    } else {
      sb.append(" cal=x");
    }

    // time keyword
    startIdx = uCmd.indexOf("time=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx, endIdx));
    } else {
      sb.append(" time=x x i");
    }

    // aux keyword
    startIdx = uCmd.indexOf("aux=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx, endIdx));
    } else {
      sb.append(" aux=yes");
    }

    // doc keyword
    startIdx = uCmd.indexOf("doc=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx, endIdx));
    } else {
      sb.append(" doc=no");
    }

    // unit keyword
    startIdx = uCmd.indexOf("unit=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx, endIdx));
    } else {
      sb.append(" unit=x");
    }

    // spac keyword
    startIdx = uCmd.indexOf("spac=");
    if (startIdx > 0) {
      endIdx = uCmd.indexOf('&', startIdx);
      sb.append(" ");
      sb.append(uCmd.substring(startIdx, endIdx));
    } else {
      sb.append(" spac=0");
    }

    // indefinitely use ADDE version 1
    sb.append(" version=1");

    // now convert to array of bytes for output since chars are two byte
    String cmd = new String(sb);
    cmd = cmd.toUpperCase();
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
    int ans = dis.readInt();

    // if server returns zero, there was an error so read trailer and exit
    if (ans == 0) {
      byte [] trailer = new byte[TRAILER_SIZE];
      dis.read(trailer, 0, trailer.length);
      String errMsg = new String(trailer, ERRMSG_OFFS, ERRMSG_SIZE);
      throw new AddeURLException(errMsg);
    } else {
      System.out.println("server is sending: " + ans + " bytes");
    }

    // if we made it to here, we're getting an image
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

}

