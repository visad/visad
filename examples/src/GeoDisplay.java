/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;
import visad.VisADException;
import visad.java3d.DisplayImplJ3D;


class GeoDisplay
    extends	DisplayImplJ3D
{
    /**
     * Construct from nothing.
     */
    GeoDisplay()
	throws VisADException, RemoteException
    {
	this("GeoDisplay");
    }


    /**
     * Construct from a name for the display.
     */
    GeoDisplay(String name)
	throws VisADException, RemoteException
    {
	super(name, DisplayImplJ3D.APPLETFRAME);

	/*
	 * Map data dimensions to display dimensions.
	 */
/* WLH 11 Sept 98 - doesn't work anymore
	{
	    QuantityDB	quantityDB = StandardQuantityDB.instance();

	    addMap(new ScalarMap(quantityDB.get("longitude", SI.radian),
		Display.Longitude));
	    addMap(new ScalarMap(quantityDB.get("latitude", SI.radian),
		Display.Latitude));
	}
*/
    }


    /**
     * Test this class.
     */
    public static void main(String[] args)
	throws Exception
    {
	/*
	 * Create and display a GeoDisplay.
	 */
	new GeoDisplay().doAction();
    }
}
