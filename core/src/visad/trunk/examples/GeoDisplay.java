import java.rmi.RemoteException;
import visad.Display;
import visad.data.netcdf.QuantityDB;
import visad.RealType;
import visad.SI;
import visad.ScalarMap;
import visad.data.netcdf.StandardQuantityDB;
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
	{
	    QuantityDB	quantityDB = StandardQuantityDB.instance();

	    addMap(new ScalarMap(quantityDB.get("longitude", SI.radian),
		Display.Longitude));
	    addMap(new ScalarMap(quantityDB.get("latitude", SI.radian),
		Display.Latitude));
	}
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
