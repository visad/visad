package visad;

import visad.*;

/**
 * Abstract class for coordinate systems that support (lat,lon) <-> (x,y)
 * with a reference coordinate system of (lat, lon) or (lon, lat).
 *
 * @author Don Murray
 */
public abstract class MapProjection extends CoordinateSystem
{
    private final int latIndex;
    private final int lonIndex;

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
     * @exception VisADException  Couldn't create necessary VisAD object or
     *                            reference does not contain RealType.Latitude
     *                            and/or RealType.Longitude or the reference
     *                            dimension is greater than 2.
     */
    public MapProjection(RealTupleType reference, Unit[] units)
        throws VisADException
    {
        super(reference, units);
        if (reference.getDimension() > 2)
            throw new CoordinateSystemException(
                "MapProjection: reference dimension is > 2");
        latIndex = reference.getIndex(RealType.Latitude);
        if (latIndex == -1 )
            throw new CoordinateSystemException(
                "MapProjection: reference does not contain RealType.Latitude");
        lonIndex = reference.getIndex(RealType.Longitude);
        if (lonIndex == -1 )
            throw new CoordinateSystemException(
                "MapProjection: reference does not contain RealType.Longitude");
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

    /**
     * Get the index of RealType.Latitude in the reference RealTupleType.
     *
     * @return  index of RealType.Latitude in the reference
     */
    public int getLatitudeIndex()
    {
        return latIndex;
    }

    /**
     * Get the index of RealType.Longitude in the reference RealTupleType.
     *
     * @return  index of RealType.Longitude in the reference
     */
    public int getLongitudeIndex()
    {
        return lonIndex;
    }
}
