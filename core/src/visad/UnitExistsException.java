//
// UnitExistsException.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
 * Provides support for attempting to define a unit with a previously-used
 * identifier.
 *
 * @author Steven R. Emmerson
 *
 * This is part of Steve Emmerson's Unit package that has been
 * incorporated into VisAD.
 */
public final class UnitExistsException extends UnitException {

  /**
   * Constructs what is essentially an empty {@code UnitExistsException}. 
   *
   * <p>Please consider using {@link #UnitExistsException(String)} or
   * {@link #UnitExistsException(String, Throwable)} instead.</p>
   */
  public UnitExistsException() {
    super();
  }

  /**
   * Creates an exception from a unit identifier.
   * 
   * @param id Unit ID.
   */
  public UnitExistsException(String id) {
    super("Unit \"" + id + "\" already exists");
  }

  /**
   * Creates an exception from a unit identifier, with a given cause.
   *
   * @param id Unit ID.
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public UnitExistsException(String id, Throwable cause) {
    super("Unit \"" + id + "\" already exists", cause);
  }

  /**
   * Constructs a {@code UnitExistsException} with the specified cause.
   *
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public UnitExistsException(Throwable cause) {
    super(cause);
  }
}
