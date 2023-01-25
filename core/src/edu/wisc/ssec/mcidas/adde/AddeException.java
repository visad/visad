//
// AddeException.java
//

/*
This source file is part of the edu.wisc.ssec.mcidas package and is
Copyright (C) 1998 - 2023 by Tom Whittaker, Tommy Jasmin, Tom Rink,
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

import edu.wisc.ssec.mcidas.McIDASException;

/**
 * {@code AddeException} class is to handle exceptions when dealing
 * with ADDE access to data. More general than {@link AddeURLException}.
 *
 * @author Don Murray - Unidata
 */
public class AddeException extends McIDASException 
{
  /** ADDE error code associated with this exception, if any. */
  private final int addeErrorCode;

  /** Whether or not an error code has been set for this exception. */
  private final boolean hasAddeErrorCode;

  /**
   * Constructs an AddeException with no specified detail message.
   */
  public AddeException() {
    super();
    hasAddeErrorCode = false;
    addeErrorCode = 0;
  }

  /**
   * Constructs an AddeException with the specified detail message.
   *
   * @param message The detail message.
   */
  public AddeException(String message) {
    super(message);
    hasAddeErrorCode = false;
    addeErrorCode = 0;
  }

  /**
   * Constructs an {@code AddeException} with an ADDE error code in place of a
   * detail message.
   *
   * @param errorCode ADDE error code.
   */
  public AddeException(int errorCode) {
    super();
    hasAddeErrorCode = true;
    addeErrorCode = errorCode;
  }

  /**
   * Constructs an {@code AddeException} with an ADDE error code and a detail
   * message.
   *
   * @param errorCode ADDE error code.
   * @param message Detail message.
   */
  public AddeException(int errorCode, String message) {
    super(message);
    hasAddeErrorCode = true;
    addeErrorCode = errorCode;
  }

    /**
     * Constructs an {@code AddeException} with an ADDE error code and the
     * cause of the exception.
     *
     * @param errorCode ADDE error code.
     * @param cause Cause of the exception. {@code null} indicates that the
     * cause is nonexistent or unknown.
     */
  public AddeException(int errorCode, Throwable cause) {
    super(cause);
    hasAddeErrorCode = true;
    addeErrorCode = errorCode;
  }

  /**
   * Constructs a {@code AddeException} with the specified detail message
   * and cause.
   *
   * @param message Detail message.
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public AddeException(String message, Throwable cause) {
    super(message, cause);
    hasAddeErrorCode = false;
    addeErrorCode = 0;
  }

    /**
     * Constructs an {@code AddeException} with an ADDE error code, detail
     * message, and cause.
     *
     * @param errorCode ADDE error code.
     * @param message Detail message.
     * @param cause Cause of the exception. {@code null} indicates that the
     * cause is nonexistent or unknown.
     */
  public AddeException(int errorCode, String message, Throwable cause) {
    super(message, cause);
    hasAddeErrorCode = true;
    addeErrorCode = errorCode;
  }

  /**
   * Constructs an {@code AddeException} with the specified cause.
   *
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public AddeException(Throwable cause) {
    super(cause);
    hasAddeErrorCode = false;
    addeErrorCode = 0;
  }

  /**
   * Returns the ADDE error code associated with this exception. <b>Note:</b>
   * you should first check for the presence of an error code via
   * {@link #hasAddeErrorCode()}.
   *
   * @return The ADDE error code associated with this exception.
   */
  public int getAddeErrorCode() {
    return addeErrorCode;
  }

  /**
   * Determine whether or not an error code has been provided for this
   * exception.
   *
   * @return Whether or not an ADDE error code has been provided.
   */
  public boolean hasAddeErrorCode() {
    return hasAddeErrorCode;
  }
}
