//
// AddeURLStreamHandlerFactory.java
//

/*
The code in this file is Copyright(C) 1998-1999 by Tommy Jasmin, 
Don Murray, and Tom Whittaker.  
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

