package visad.meteorology;

import visad.*;

/**
 * An interface for defining properties associated with satellite data.
 */
public interface SatelliteData extends Data
{
    /**
     * Get the name of the sensor.
     * @return  sensor description
     */
    public String getSensorName();

    /**
     * Get a description of this satellite data.
     * @return  description
     */
    public String getDescription();
}
