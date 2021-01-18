//
// CoordinateSystemException.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

/**
 * {@code CoordinateSystemException} is an exception for a bad VisAD 
 * CoordinateSystem.
 */
public class CoordinateSystemException extends VisADException {

  /**
   * Constructs a {@code CoordinateSystemException} with no specified detail message.
   */
  public CoordinateSystemException() {
    super();
  }

  /**
   * Constructs a {@code CoordinateSystemException} with the specified detail message.
   *
   * @param message Detail message.
   */
  public CoordinateSystemException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code CoordinateSystemException} with the specified detail message
   * and cause.
   *
   * @param message Detail message.
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public CoordinateSystemException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a {@code CoordinateSystemException} with the specified cause.
   *
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public CoordinateSystemException(Throwable cause) {
    super(cause);
  }

  /**
   * Construct a {@code CoordinateSystemException} for unequal 
   * CoordinateSystems.
   * 
   * @param cs1 First coordinate system. {@code null} is allowed.
   * @param cs2 Second coordinate system. {@code null} is allowed.
   */
  public CoordinateSystemException(CoordinateSystem cs1, CoordinateSystem cs2) {
    this("Coordinate system mismatch: " +
      (cs1 == null ? "null" : cs1.getReference().toString()) + " != " +
      (cs2 == null ? "null" : cs2.getReference().toString()));
  }

  /**
   * Construct a {@code CoordinateSystemException} for unequal 
   * CoordinateSystems.
   * 
   * @param cs1 First coordinate system. {@code null} is allowed.
   * @param cs2 Second coordinate system. {@code null} is allowed.
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public CoordinateSystemException(CoordinateSystem cs1, CoordinateSystem cs2, Throwable cause) {
    this("Coordinate system mismatch: " +
      (cs1 == null ? "null" : cs1.getReference().toString()) + " != " +
      (cs2 == null ? "null" : cs2.getReference().toString()), cause);
  }
}

