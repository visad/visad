
//
// RadarFileException.java
//

/*
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

package visad.bom;

/**
 * {@code RadarFileException} class is to handle exceptions when dealing with
 * Australian Bureau of Meteorology Radar files
 *
 * @author James Kelly
 */
public class RadarFileException extends Exception {

  /** 
   * Constructs a {@code RadarFileException} with no specified detail message.
   */
  public RadarFileException() {
    super();
  }

  /**
   * Constructs a {@code RadarFileException} with the specified detail message.
   *
   * @param message Detail message.
   */
  public RadarFileException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code RadarFileException} with the specified detail message
   * and cause.
   *
   * @param message Detail message.
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public RadarFileException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a {@code RadarFileException} with the specified cause.
   *
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public RadarFileException(Throwable cause) {
    super(cause);
  }
}
