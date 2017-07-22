//
// AddeURLException.java
//

/*
This source file is part of the edu.wisc.ssec.mcidas package and is
Copyright (C) 1998 - 2017 by Tom Whittaker, Tommy Jasmin, Tom Rink,
Don Murray, James Kelly, Bill Hibbard, Dave Glowacki, Curtis Rueden
and others.
 
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

  /**
   * Constructs a {@code AddeURLException} with no specified detail message.
   */
  public AddeURLException() {
    super(); 
  }

  /**
   * Constructs a {@code AddeURLException} with the specified detail message.
   *
   * @param message Detail message.
   */
  public AddeURLException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code AddeURLException} with the specified detail message
   * and cause.
   *
   * @param message Detail message.
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public AddeURLException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a {@code AddeURLException} with the specified cause.
   *
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public AddeURLException(Throwable cause) {
    super(cause);
  }
}

