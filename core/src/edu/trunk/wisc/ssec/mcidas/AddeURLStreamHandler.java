
package edu.wisc.ssec.mcidas;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * This class defines the openConnection method, which is
 * used to create an AddeURLConnection.  Note that this 
 * class is automatically loaded when a URL of this type
 * is created, you don't have explicitly create an object.
 *
 * @author Tommy Jasmin, University of Wisconsin, SSEC
 */

public class AddeURLStreamHandler extends URLStreamHandler

{

  /**
   *
   * returns a reference to a special URLConnection object
   * designed to implement the ADDE protocol.
   *
   * @param             url - user specified URL, encodes ADDE request
   * @return            AddeURLConnection reference.
   */

  protected URLConnection openConnection(URL url)
    throws IOException

  {
    return new AddeURLConnection(url);
  }

}

