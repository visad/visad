
//
// HSVCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
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

package visad;

/**
   HSVCoordinateSystem is the VisAD CoordinateSystem class for
   (Hue, Saturation, Value).  Algorithm from Foley and van Dam.<P>
*/
class HSVCoordinateSystem extends CoordinateSystem {

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
      throw new CoordinateSystemException("CMYCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[3][len];
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
        int j = (int) Math.floor(h / 60.0);
        double f = h - j;
        double p = v * (1.0 - s);
        double q = v * (1.0 - s * f);
        double t = v * (1.0 - s * (1.0 - f));
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
      throw new CoordinateSystemException("CMYCoordinateSystem." +
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
      throw new CoordinateSystemException("CMYCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[3][len];
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
        int j = (int) Math.floor(h / 60.0f);
        float f = h - j;
        float p = v * (1.0f - s);
        float q = v * (1.0f - s * f);
        float t = v * (1.0f - s * (1.0f - f));
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
      throw new CoordinateSystemException("CMYCoordinateSystem." +
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

