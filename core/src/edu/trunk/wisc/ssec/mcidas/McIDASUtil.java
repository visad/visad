//
// McIDASUtil.java
//

/*
The code in this file is Copyright(C) 1999 by Don
Murray.  It is designed to be used with the VisAD system for 
interactive analysis and visualization of numerical data.  
 
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

package edu.wisc.ssec.mcidas;

/**
 * Class for static McIDAS utility functions
 *
 * @author Don Murray
 */
public final class McIDASUtil 
{

    /** McIDAS missing value for 4-byte integers */
    public static final int MCMISSING = 0x80808080;

    /**
     * Converts a packed integer (SIGN DDD MM SS) latitude/longitude to double.
     *
     * @param value  integer containing the packed data
     * @return  double representation of value
     */
    public static double integerLatLonToDouble(int value)
    {
        int val = value < 0 ? -value : value;
        double dvalue  = ((double) (value/10000) + 
                          ((double) ((value/100)%100))/60.0 +
                          (double) (value%100)/3600.0);
        return (value < 0) ? -dvalue : dvalue;
    }
   
}
