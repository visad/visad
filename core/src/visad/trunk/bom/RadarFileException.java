
//
// AreaFileException.java
//

/*
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

/**
 * RadarFileException class is to handle exceptions when dealing
 * with Australian Bureau of Meteorology Radar files
 *
 * @author James Kelly
 */

package visad.bom;

public class RadarFileException extends Exception {

  public RadarFileException() {super(); }
  public RadarFileException(String s) {super(s); }

}
