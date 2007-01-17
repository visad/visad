//
// HSVCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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
   HSVCoordinateSystem is the VisAD CoordinateSystem class for
   (Hue, Saturation, Value) with Reference (Red, Green, Blue).
   Algorithm from Foley and van Dam.<P>
*/
public class HSVCoordinateSystem extends CoordinateSystem {

  private static Unit[] coordinate_system_units =
    {CommonUnit.degree, null, null};

  public HSVCoordinateSystem(RealTupleType reference)
         throws VisADException {
    super(reference, coordinate_system_units);
  }

  /** trusted constructor for initializers */
  HSVCoordinateSystem(RealTupleType reference, boolean b) {
    super(reference, coordinate_system_units, b);
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("HSVCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[3][len];
    int j = 0;
    double f = 0.0, p = 0.0, q = 0.0, t = 0.0;
    for (int i=0; i<len ;i++) {
      double h = tuples[0][i] % 360.0; // belt
      if (h < 0.0) h += 360.0;         // suspenders
      double s = Math.max(0.0, Math.min(1.0, tuples[1][i]));
      double v = Math.max(0.0, Math.min(1.0, tuples[2][i]));
      if (s == 0.0) {
        value[0][i] = v;
        value[1][i] = v;
        value[2][i] = v;
      }
      else { // if (s != 0.0)
        h = h / 60.0f;
        j = (int) Math.floor(h);
        f = h - j;
        p = v * (1.0 - s);
        q = v * (1.0 - s * f);
        t = v * (1.0 - s * (1.0 - f));
        switch (j) {
          case 0:
            value[0][i] = v;
            value[1][i] = t;
            value[2][i] = p;
            break;
          case 1:
            value[0][i] = q;
            value[1][i] = v;
            value[2][i] = p;
            break;
          case 2:
            value[0][i] = p;
            value[1][i] = v;
            value[2][i] = t;
            break;
          case 3:
            value[0][i] = p;
            value[1][i] = q;
            value[2][i] = v;
            break;
          case 4:
            value[0][i] = t;
            value[1][i] = p;
            value[2][i] = v;
            break;
          case 5:
          default:
            value[0][i] = v;
            value[1][i] = p;
            value[2][i] = q;
            break;
        } // end switch (i)
      } // end if (s != 0.0)
    } // end for (int i=0; i<len ;i++)
    return value;
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("HSVCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[3][len];
    for (int i=0; i<len ;i++) {
      double r = Math.max(0.0, Math.min(1.0, tuples[0][i]));
      double g = Math.max(0.0, Math.min(1.0, tuples[1][i]));
      double b = Math.max(0.0, Math.min(1.0, tuples[2][i]));
      double max = Math.max(r, Math.max(g, b));
      double min = Math.min(r, Math.min(g, b));
      value[2][i] = max; // V
      value[1][i] = (max != 0.0) ? (max - min) / max : 0.0; // S
      if (value[1][i] == 0.0) {
        value[2][i] = 0.0; // H (use 0.0 rather than undetermined)
      }
      else {
        double rc = (max - r) / (max - min);
        double gc = (max - g) / (max - min);
        double bc = (max - b) / (max - min);
        double h;
        if (r == max) h = bc - gc;
        else if (g == max) h = 2.0 + rc - bc;
        else if (b == max) h = 4.0 + gc - rc;
        else {
          throw new CoordinateSystemException("HSVCoordinateSystem: bad h");
        }
        h = 60.0 * h;
        if (h < 0.0) h += 360.0;
        value[2][i] = h; // H
      }
    }
    return value;
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("HSVCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[3][len];
    int j = 0;
    float f = 0.0f, p = 0.0f, q = 0.0f, t = 0.0f;
    for (int i=0; i<len ;i++) {
      float h = tuples[0][i] % 360.0f; // belt
      if (h < 0.0f) h += 360.0f;       // suspenders
      float s = Math.max(0.0f, Math.min(1.0f, tuples[1][i]));
      float v = Math.max(0.0f, Math.min(1.0f, tuples[2][i]));
      if (s == 0.0f) {
        value[0][i] = v;
        value[1][i] = v;
        value[2][i] = v;
      }
      else { // if (s != 0.0f)
        h = h / 60.0f;
        j = (int) Math.floor(h);
        f = h - j;
        p = v * (1.0f - s);
        q = v * (1.0f - s * f);
        t = v * (1.0f - s * (1.0f - f));
        switch (j) {
          case 0:
            value[0][i] = v;
            value[1][i] = t;
            value[2][i] = p;
            break;
          case 1:
            value[0][i] = q;
            value[1][i] = v;
            value[2][i] = p;
            break;
          case 2:
            value[0][i] = p;
            value[1][i] = v;
            value[2][i] = t;
            break;
          case 3:
            value[0][i] = p;
            value[1][i] = q;
            value[2][i] = v;
            break;
          case 4:
            value[0][i] = t;
            value[1][i] = p;
            value[2][i] = v;
            break;
          case 5:
          default:
            value[0][i] = v;
            value[1][i] = p;
            value[2][i] = q;
            break;
        } // end switch (i)
      } // end if (s != 0.0f)
    } // end for (int i=0; i<len ;i++)
    return value;
  }

  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("HSVCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[3][len];
    for (int i=0; i<len ;i++) {
      float r = Math.max(0.0f, Math.min(1.0f, tuples[0][i]));
      float g = Math.max(0.0f, Math.min(1.0f, tuples[1][i]));
      float b = Math.max(0.0f, Math.min(1.0f, tuples[2][i]));
      float max = Math.max(r, Math.max(g, b));
      float min = Math.min(r, Math.min(g, b));
      value[2][i] = max; // V
      value[1][i] = (max != 0.0f) ? (max - min) / max : 0.0f; // S
      if (value[1][i] == 0.0f) {
        value[2][i] = 0.0f; // H (use 0.0f rather than undetermined)
      }
      else {
        float rc = (max - r) / (max - min);
        float gc = (max - g) / (max - min);
        float bc = (max - b) / (max - min);
        float h;
        if (r == max) h = bc - gc;
        else if (g == max) h = 2.0f + rc - bc;
        else if (b == max) h = 4.0f + gc - rc;
        else {
          throw new CoordinateSystemException("HSVCoordinateSystem: bad h");
        }
        h = 60.0f * h;
        if (h < 0.0f) h += 360.0f;
        value[2][i] = h; // H
      }
    }
    return value;
  }

  public boolean equals(Object cs) {
    return (cs instanceof HSVCoordinateSystem);
  }

}

