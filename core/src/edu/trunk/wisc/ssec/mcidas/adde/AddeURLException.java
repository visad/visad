
//
// AddeURLException.java
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

/**
 * AddeURLException class is to handle exceptions when using ADDE URLs.
 *
 * @author Tommy Jasmin, University of Wisconsin - SSEC
 */

import java.io.IOException;

public class AddeURLException extends IOException {
 
  public AddeURLException() {
    super(); 
  }

  public AddeURLException(String s) {
    super(s); 
  }
 
}

