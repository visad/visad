/*
 * $Id: DataRange.java,v 1.1 2009-10-23 10:28:41 jeffmc Exp $
 *
 * Copyright  1997-2004 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */


package visad.data;


/**
 * Holds a simple min/max range
 *
 *
 */
public class DataRange implements java.io.Serializable {


    /** The range */
    public double min, max;


    /**
     * Default ctor
     *
     */
    public DataRange() {
        min = 0.0;
        max = 1.0;
    }

    /**
     * Create a range with min, max
     *
     * @param min min
     * @param max max
     *
     */
    public DataRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * ctor
     *
     * @param a 2-ary array holding min/max
     *
     */
    public DataRange(double[] a) {
        this(a[0], a[1]);
    }

    /**
     * copy ctor
     *
     * @param r object
     *
     */
    public DataRange(DataRange r) {
        if (r != null) {
            this.min = r.min;
            this.max = r.max;
        }
    }

    /**
     * Equals
     *
     * @param o Object
     * @return equals
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if ( !(o instanceof DataRange)) {
            return false;
        }
        DataRange other = (DataRange) o;
        return ((min == other.min) && (max == other.max));
    }

    /**
     * set the values
     *
     * @param min min
     * @param max max
     */
    public void set(double min, double max) {
        this.min = min;
        this.max = max;
    }


    /**
     * Get the min
     * @return  The min value
     */
    public double getMin() {
        return min;
    }

    /**
     * Get the max
     * @return  The max value
     */
    public double getMax() {
        return max;
    }

    /**
     * Set the min
     *
     * @param v  value
     */
    public void setMin(double v) {
        min = v;
    }

    /**
     * Set the max
     *
     * @param v  value
     */
    public void setMax(double v) {
        max = v;
    }

    /**
     * Set the min
     *
     * @param v value
     */
    public void setMin(int v) {
        min = (double) v;
    }

    /**
     * Set the max
     *
     * @param v value
     */
    public void setMax(int v) {
        max = (double) v;
    }

    /**
     * Get a 2-aray array holding min/max
     * @return array of min and max
     */
    public double[] asArray() {
        return new double[] { min, max };
    }

    /**
     * Get a 2-aray array holding min/max
     * @return array of min and max
     */
    public float[] asFloatArray() {
        return new float[] { (float) min, (float) max };
    }

    /**
     * max-min
     * @return max-min
     */
    public double span() {
        return (max - min);
    }

    /**
     * max-min
     * @return max-min
     */
    public double getSpan() {
        return span();
    }

    /**
     * get abs(max-min)
     * @return abs(max-min)
     */
    public double getAbsSpan() {
        return Math.abs(span());
    }

    /**
     * Get the mid point
     * @return mid point
     */
    public double getMid() {
        return min + span() / 2.0;
    }


    /**
     * Get percent along the way between min and max
     *
     * @param percent percent
     * @return value
     */
    public double getValueOfPercent(double percent) {
        return getMin() + getSpan() * percent;
    }

    /**
     * Ge tthe percent the given value is between min and max
     *
     * @param v value
     * @return percent
     */
    public double getPercent(double v) {
        return (v - min) / span();
    }



}

