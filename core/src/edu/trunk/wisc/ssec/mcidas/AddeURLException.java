
package edu.wisc.ssec.mcidas;

/**
 * AddeURLException class is to handle exceptions when using ADDE URLs.
 *
 * @author Tommy Jasmin
 */

import java.io.IOException;

class AddeURLException extends IOException {
 
  public AddeURLException() {
    super(); 
  }

  public AddeURLException(String s) {
    super(s); 
  }
 
}

