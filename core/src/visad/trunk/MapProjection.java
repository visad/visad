package visad;

import visad.*;

/**
 * Abstract class for coordinate systems that support (lat,lon) to (x,y)
 * with a reference coordinate system of (lat, lon).
 *
 * @author Don Murray
 */
public abstract class MapProjection extends CoordinateSystem
{

    /**
     * Default Constructor with reference of RealType.Latitude, 
     * RealType.Longitude
     *
     * @exception  VisADException   couldn't create the necessary VisAD object
    public MapProjection()
        throws VisADException
    {
        this(
            new RealTupleType(
                RealType.Latitude, 
                RealType.Longitude),
            new Unit[] {null, null});
    }
     */


    /**
     * Constructs from the type of the reference coordinate system and 
     * units for values in this coordinate system. The reference coordinate
     * system must contain RealType.Latitude and RealType.Longitude.
     *
     * @param reference  The type of the reference coordinate system. The
     *                   reference must contain RealType.Latitude and
     *                   RealType.Longitude.  Values in the reference 
     *                   coordinate system shall be in units of 
     *                   reference.getDefaultUnits() unless specified 
     *                   otherwise.
     * @param units      The default units for this coordinate system. 
     *                   Numeric values in this coordinate system shall be 
     *                   in units of units unless specified otherwise. 
     *                   May be null or an array of null-s.
     * @exception VisADException  Couldn't create necessary VisAD object.
     */
    public MapProjection(RealTupleType reference, Unit[] units)
        throws VisADException
    {
        super(reference, units);

    }

    /**
     * Get a reasonable bounding box in this coordinate system. MapProjections 
     * are typically specific to an area of the world; there's no bounding 
     * box that works for all projections so each subclass must implement
     * this method. For example, the bounding box for a satellite image 
     * MapProjection might have an upper left corner of (0,0) and the width 
     * and height of the Rectangle2D would be the number of elements and 
     * lines, respectively.
     *
     * @return the bounding box of the MapProjection
     *
     */
    public abstract java.awt.geom.Rectangle2D getDefaultMapArea();
}
