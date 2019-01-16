//
// ClusterException.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

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

package visad.cluster;

import visad.VisADException;

/**
 * {@code ClusterException} is an exception for VisAD cluster errors.
 */
public class ClusterException extends VisADException {

  /**
   * Constructs a {@code ClusterException} with no specified detail message.
   */
  public ClusterException() {
    super();
  }

  /**
   * Constructs a {@code ClusterException} with the specified detail message.
   *
   * @param message Detail message.
   */
  public ClusterException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code ClusterException} with the specified detail message
   * and cause.
   *
   * @param message Detail message.
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public ClusterException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a {@code ClusterException} with the specified cause.
   *
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public ClusterException(Throwable cause) {
    super(cause);
  }
}

