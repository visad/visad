
package edu.wisc.ssec.mcidas;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * This class creates a URLStreamHandler for the ADDE protocol.
 * An instance is passed to URL.setURLStreamHandlerFactory when
 * an application will be using ADDE URLs.
 *
 * @author Tommy Jasmin, University of Wisconsin, SSEC
 */

public class AddeURLStreamHandlerFactory 
  implements URLStreamHandlerFactory

{

  /**
   *
   * Creates URLStreamHandler object - not called directly.
   *
   * @param             protocol - should be "adde"
   * @return            AddeURLStreamHandler reference.
   */

  public URLStreamHandler createURLStreamHandler(String protocol) {
    if (protocol.equalsIgnoreCase("adde")) {
      return new AddeURLStreamHandler();
    } else {
      return null;
    }
  }

}

