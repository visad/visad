//
// Gridded3DDoubleSet.java
//

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

package visad;

/**
   Gridded3DDoubleSet is a Gridded3DSet with double-precision samples.<P>
*/
public class Gridded3DDoubleSet extends Gridded3DSet
       implements GriddedDoubleSet {

  double[] Low = new double[3];
  double[] Hi = new double[3];
  double LowX, HiX, LowY, HiY, LowZ, HiZ;
  double[][] Samples;


  // Overridden Gridded3DSet constructors (float[][])

  /** a 3-D set whose topology is a lengthX x lengthY x lengthZ
      grid, with null errors, CoordinateSystem and Units are
      defaults from type */
  public Gridded3DDoubleSet(MathType type, float[][] samples, int lengthX,
                      int lengthY, int lengthZ) throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, lengthY, lengthZ,
      null, null, null, true);
  }

  /** a 3-D set whose topology is a lengthX x lengthY x lengthZ
      grid; samples array is organized float[3][number_of_samples]
      where lengthX * lengthY * lengthZ = number_of_samples;
      samples must form a non-degenerate 3-D grid (no bow-tie-shaped
      grid cubes);  the X component increases fastest and the Z
      component slowest in the second index of samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded3DDoubleSet(MathType type, float[][] samples,
                      int lengthX, int lengthY, int lengthZ,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, lengthY, lengthZ,
      coord_sys, units, errors, true);
  }

  public Gridded3DDoubleSet(MathType type, float[][] samples,
               int lengthX, int lengthY, int lengthZ,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, lengthY, lengthZ,
      coord_sys, units, errors, copy);
  }

  /** a 3-D set with manifold dimension = 2, with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded3DDoubleSet(MathType type, float[][] samples, int lengthX,
                      int lengthY) throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, lengthY,
      null, null, null, true);
  }

  /** a 3-D set with manifold dimension = 2; samples array is
      organized float[3][number_of_samples] where lengthX * lengthY
      = number_of_samples; no geometric constraint on samples; the
      X component increases fastest in the second index of samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded3DDoubleSet(MathType type, float[][] samples,
                      int lengthX, int lengthY,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, lengthY,
      coord_sys, units, errors, true);
  }

  public Gridded3DDoubleSet(MathType type, float[][] samples,
               int lengthX, int lengthY,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, lengthY,
      coord_sys, units, errors, copy);
  }

  /** a 3-D set with manifold dimension = 1, with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded3DDoubleSet(MathType type, float[][] samples, int lengthX)
         throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, null, null, null, true);
  }

  /** a 3-D set with manifold dimension = 1; samples array is
      organized float[3][number_of_samples] where lengthX =
      number_of_samples; no geometric constraint on samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded3DDoubleSet(MathType type, float[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX,
      coord_sys, units, errors, true);
  }

  public Gridded3DDoubleSet(MathType type, float[][] samples, int lengthX,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX,
      coord_sys, units, errors, copy);
  }


  // Corresponding Gridded3DDoubleSet constructors (double[][])

  /** a 3-D set whose topology is a lengthX x lengthY x lengthZ
      grid, with null errors, CoordinateSystem and Units are
      defaults from type */
  public Gridded3DDoubleSet(MathType type, double[][] samples, int lengthX,
                      int lengthY, int lengthZ) throws VisADException {
    this(type, samples, lengthX, lengthY, lengthZ, null, null, null, true);
  }

  /** a 3-D set whose topology is a lengthX x lengthY x lengthZ
      grid; samples array is organized double[3][number_of_samples]
      where lengthX * lengthY * lengthZ = number_of_samples;
      samples must form a non-degenerate 3-D grid (no bow-tie-shaped
      grid cubes);  the X component increases fastest and the Z
      component slowest in the second index of samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded3DDoubleSet(MathType type, double[][] samples,
                      int lengthX, int lengthY, int lengthZ,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, lengthY, lengthZ,
      coord_sys, units, errors, true);
  }

  public Gridded3DDoubleSet(MathType type, double[][] samples,
               int lengthX, int lengthY, int lengthZ,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    this(type, samples, lengthX, lengthY, lengthZ, coord_sys, units,
         errors, copy, true);
  }

  public Gridded3DDoubleSet(MathType type, double[][] samples,
               int lengthX, int lengthY, int lengthZ,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy, boolean test)
               throws VisADException {
    super(type, null, lengthX, lengthY, lengthZ,
      coord_sys, units, errors, copy);
    if (samples == null) {
      throw new SetException("Gridded3DDoubleSet: samples are null");
    }
    init_doubles(samples, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];
    LengthY = Lengths[1];
    LowZ = Low[2];
    HiZ = Hi[2];
    LengthZ = Lengths[2];

    if (Samples != null &&
        Lengths[0] > 1 && Lengths[1] > 1 && Lengths[2] > 1) {
      for (int i=0; i<Length; i++) {
        if (Samples[0][i] != Samples[0][i]) {
          throw new SetException(
           "Gridded3DDoubleSet: samples values may not be missing");
        }
      }
      // Samples consistency test
      double[] t000 = new double[3];
      double[] t100 = new double[3];
      double[] t010 = new double[3];
      double[] t001 = new double[3];
      double[] t110 = new double[3];
      double[] t101 = new double[3];
      double[] t011 = new double[3];
      double[] t111 = new double[3];
      for (int v=0; v<3; v++) {
        t000[v] = Samples[v][0];
        t100[v] = Samples[v][1];
        t010[v] = Samples[v][LengthX];
        t001[v] = Samples[v][LengthY*LengthX];
        t110[v] = Samples[v][LengthX+1];
        t101[v] = Samples[v][LengthY*LengthX+1];
        t011[v] = Samples[v][(LengthY+1)*LengthX];
        t111[v] = Samples[v][(LengthY+1)*LengthX+1];
      }
/* CICERO
      Pos = (  ( (t100[1]-t000[1])*(t101[2]-t100[2])
               - (t100[2]-t000[2])*(t101[1]-t100[1]) )
                *(t110[0]-t100[0])  )
          + (  ( (t100[2]-t000[2])*(t101[0]-t100[0])
               - (t100[0]-t000[0])*(t101[2]-t100[2]) )
                *(t110[1]-t100[1])  )
          + (  ( (t100[0]-t000[0])*(t101[1]-t100[1])
               - (t100[1]-t000[1])*(t101[0]-t100[0]) )
                *(t110[2]-t100[2])  ) > 0;
*/
      double xpos = (  ( (t100[1]-t000[1])*(t101[2]-t100[2])
                       - (t100[2]-t000[2])*(t101[1]-t100[1]) )
                        *(t110[0]-t100[0])  )
                  + (  ( (t100[2]-t000[2])*(t101[0]-t100[0])
                       - (t100[0]-t000[0])*(t101[2]-t100[2]) )
                        *(t110[1]-t100[1])  )
                  + (  ( (t100[0]-t000[0])*(t101[1]-t100[1])
                       - (t100[1]-t000[1])*(t101[0]-t100[0]) )
                        *(t110[2]-t100[2])  );
      Pos = (xpos > 0);

      if (test) {
        double[] v000 = new double[3];
        double[] v100 = new double[3];
        double[] v010 = new double[3];
        double[] v001 = new double[3];
        double[] v110 = new double[3];
        double[] v101 = new double[3];
        double[] v011 = new double[3];
        double[] v111 = new double[3];

        for (int k=0; k<LengthZ-1; k++) {
          for (int j=0; j<LengthY-1; j++) {
            for (int i=0; i<LengthX-1; i++) {
              for (int v=0; v<3; v++) {
                int zadd = LengthY*LengthX;
                int base = k*zadd + j*LengthX + i;
                v000[v] = Samples[v][base];
                v100[v] = Samples[v][base+1];
                v010[v] = Samples[v][base+LengthX];
                v001[v] = Samples[v][base+zadd];
                v110[v] = Samples[v][base+LengthX+1];
                v101[v] = Samples[v][base+zadd+1];
                v011[v] = Samples[v][base+zadd+LengthX];
                v111[v] = Samples[v][base+zadd+LengthX+1];
              }
/* CICERO
              if (((  ( (v100[1]-v000[1])*(v101[2]-v100[2])    // test 1
                      - (v100[2]-v000[2])*(v101[1]-v100[1]) )
                       *(v110[0]-v100[0])  )
                 + (  ( (v100[2]-v000[2])*(v101[0]-v100[0])
                      - (v100[0]-v000[0])*(v101[2]-v100[2]) )
                       *(v110[1]-v100[1])  )
                 + (  ( (v100[0]-v000[0])*(v101[1]-v100[1])
                      - (v100[1]-v000[1])*(v101[0]-v100[0]) )
                       *(v110[2]-v100[2])  ) > 0 != Pos)
               || ((  ( (v101[1]-v100[1])*(v001[2]-v101[2])    // test 2
                      - (v101[2]-v100[2])*(v001[1]-v101[1]) )
                       *(v111[0]-v101[0])  )
                 + (  ( (v101[2]-v100[2])*(v001[0]-v101[0])
                      - (v101[0]-v100[0])*(v001[2]-v101[2]) )
                       *(v111[1]-v101[1])  )
                 + (  ( (v101[0]-v100[0])*(v001[1]-v101[1])
                      - (v101[1]-v100[1])*(v001[0]-v101[0]) )
                       *(v111[2]-v101[2])  ) > 0 != Pos)
               || ((  ( (v001[1]-v101[1])*(v000[2]-v001[2])    // test 3
                      - (v001[2]-v101[2])*(v000[1]-v001[1]) )
                       *(v011[0]-v001[0])  )
                 + (  ( (v001[2]-v101[2])*(v000[0]-v001[0])
                      - (v001[0]-v101[0])*(v000[2]-v001[2]) )
                       *(v011[1]-v001[1])  )
                 + (  ( (v001[0]-v101[0])*(v000[1]-v001[1])
                      - (v001[1]-v101[1])*(v000[0]-v001[0]) )
                       *(v011[2]-v001[2])  ) > 0 != Pos)
               || ((  ( (v000[1]-v001[1])*(v100[2]-v000[2])    // test 4
                      - (v000[2]-v001[2])*(v100[1]-v000[1]) )
                       *(v010[0]-v000[0])  )
                 + (  ( (v000[2]-v001[2])*(v100[0]-v000[0])
                      - (v000[0]-v001[0])*(v100[2]-v000[2]) )
                       *(v010[1]-v000[1])  )
                 + (  ( (v000[0]-v001[0])*(v100[1]-v000[1])
                      - (v000[1]-v001[1])*(v100[0]-v000[0]) )
                       *(v010[2]-v000[2])  ) > 0 != Pos)
               || ((  ( (v110[1]-v111[1])*(v010[2]-v110[2])    // test 5
                      - (v110[2]-v111[2])*(v010[1]-v110[1]) )
                       *(v100[0]-v110[0])  )
                 + (  ( (v110[2]-v111[2])*(v010[0]-v110[0])
                      - (v110[0]-v111[0])*(v010[2]-v110[2]) )
                       *(v100[1]-v110[1])  )
                 + (  ( (v110[0]-v111[0])*(v010[1]-v110[1])
                      - (v110[1]-v111[1])*(v010[0]-v110[0]) )
                       *(v100[2]-v110[2])  ) > 0 != Pos)
               || ((  ( (v111[1]-v011[1])*(v110[2]-v111[2])    // test 6
                      - (v111[2]-v011[2])*(v110[1]-v111[1]) )
                       *(v101[0]-v111[0])  )
                 + (  ( (v111[2]-v011[2])*(v110[0]-v111[0])
                      - (v111[0]-v011[0])*(v110[2]-v111[2]) )
                       *(v101[1]-v111[1])  )
                 + (  ( (v111[0]-v011[0])*(v110[1]-v111[1])
                      - (v111[1]-v011[1])*(v110[0]-v111[0]) )
                       *(v101[2]-v111[2])  ) > 0 != Pos)
               || ((  ( (v011[1]-v010[1])*(v111[2]-v011[2])    // test 7
                      - (v011[2]-v010[2])*(v111[1]-v011[1]) )
                       *(v001[0]-v011[0])  )
                 + (  ( (v011[2]-v010[2])*(v111[0]-v011[0])
                      - (v011[0]-v010[0])*(v111[2]-v011[2]) )
                       *(v001[1]-v011[1])  )
                 + (  ( (v011[0]-v010[0])*(v111[1]-v011[1])
                      - (v011[1]-v010[1])*(v111[0]-v011[0]) )
                       *(v001[2]-v011[2])  ) > 0 != Pos)
               || ((  ( (v010[1]-v110[1])*(v011[2]-v010[2])    // test 8
                      - (v010[2]-v110[2])*(v011[1]-v010[1]) )
                       *(v000[0]-v010[0])  )
                 + (  ( (v010[2]-v110[2])*(v011[0]-v010[0])
                      - (v010[0]-v110[0])*(v011[2]-v010[2]) )
                       *(v000[1]-v010[1])  )
                 + (  ( (v010[0]-v110[0])*(v011[1]-v010[1])
                      - (v010[1]-v110[1])*(v011[0]-v010[0]) )
                       *(v000[2]-v010[2])  ) > 0 != Pos))
*/
// CICERO
              double w1 = ((  ( (v100[1]-v000[1])*(v101[2]-v100[2])
                              - (v100[2]-v000[2])*(v101[1]-v100[1]) )
                               *(v110[0]-v100[0])  )
                         + (  ( (v100[2]-v000[2])*(v101[0]-v100[0])
                              - (v100[0]-v000[0])*(v101[2]-v100[2]) )
                               *(v110[1]-v100[1])  )
                         + (  ( (v100[0]-v000[0])*(v101[1]-v100[1])
                              - (v100[1]-v000[1])*(v101[0]-v100[0]) )
                               *(v110[2]-v100[2])  ));
              double w2 = ((  ( (v101[1]-v100[1])*(v001[2]-v101[2])
                              - (v101[2]-v100[2])*(v001[1]-v101[1]) )
                               *(v111[0]-v101[0])  )
                         + (  ( (v101[2]-v100[2])*(v001[0]-v101[0])
                              - (v101[0]-v100[0])*(v001[2]-v101[2]) )
                               *(v111[1]-v101[1])  )
                         + (  ( (v101[0]-v100[0])*(v001[1]-v101[1])
                              - (v101[1]-v100[1])*(v001[0]-v101[0]) )
                               *(v111[2]-v101[2])  ));
              double w3 = ((  ( (v001[1]-v101[1])*(v000[2]-v001[2])
                              - (v001[2]-v101[2])*(v000[1]-v001[1]) )
                               *(v011[0]-v001[0])  )
                         + (  ( (v001[2]-v101[2])*(v000[0]-v001[0])
                              - (v001[0]-v101[0])*(v000[2]-v001[2]) )
                               *(v011[1]-v001[1])  )
                         + (  ( (v001[0]-v101[0])*(v000[1]-v001[1])
                              - (v001[1]-v101[1])*(v000[0]-v001[0]) )
                               *(v011[2]-v001[2])  ));
              double w4 = ((  ( (v000[1]-v001[1])*(v100[2]-v000[2])
                              - (v000[2]-v001[2])*(v100[1]-v000[1]) )
                               *(v010[0]-v000[0])  )
                         + (  ( (v000[2]-v001[2])*(v100[0]-v000[0])
                              - (v000[0]-v001[0])*(v100[2]-v000[2]) )
                               *(v010[1]-v000[1])  )
                         + (  ( (v000[0]-v001[0])*(v100[1]-v000[1])
                              - (v000[1]-v001[1])*(v100[0]-v000[0]) )
                               *(v010[2]-v000[2])  ));
              double w5 = ((  ( (v110[1]-v111[1])*(v010[2]-v110[2])
                              - (v110[2]-v111[2])*(v010[1]-v110[1]) )
                               *(v100[0]-v110[0])  )
                         + (  ( (v110[2]-v111[2])*(v010[0]-v110[0])
                              - (v110[0]-v111[0])*(v010[2]-v110[2]) )
                               *(v100[1]-v110[1])  )
                         + (  ( (v110[0]-v111[0])*(v010[1]-v110[1])
                              - (v110[1]-v111[1])*(v010[0]-v110[0]) )
                               *(v100[2]-v110[2])  ));
              double w6 = ((  ( (v111[1]-v011[1])*(v110[2]-v111[2])
                              - (v111[2]-v011[2])*(v110[1]-v111[1]) )
                               *(v101[0]-v111[0])  )
                         + (  ( (v111[2]-v011[2])*(v110[0]-v111[0])
                              - (v111[0]-v011[0])*(v110[2]-v111[2]) )
                               *(v101[1]-v111[1])  )
                         + (  ( (v111[0]-v011[0])*(v110[1]-v111[1])
                              - (v111[1]-v011[1])*(v110[0]-v111[0]) )
                               *(v101[2]-v111[2])  ));
              double w7 = ((  ( (v011[1]-v010[1])*(v111[2]-v011[2])
                              - (v011[2]-v010[2])*(v111[1]-v011[1]) )
                               *(v001[0]-v011[0])  )
                         + (  ( (v011[2]-v010[2])*(v111[0]-v011[0])
                              - (v011[0]-v010[0])*(v111[2]-v011[2]) )
                               *(v001[1]-v011[1])  )
                         + (  ( (v011[0]-v010[0])*(v111[1]-v011[1])
                              - (v011[1]-v010[1])*(v111[0]-v011[0]) )
                               *(v001[2]-v011[2])  ));
              double w8 = ((  ( (v010[1]-v110[1])*(v011[2]-v010[2])
                              - (v010[2]-v110[2])*(v011[1]-v010[1]) )
                               *(v000[0]-v010[0])  )
                         + (  ( (v010[2]-v110[2])*(v011[0]-v010[0])
                              - (v010[0]-v110[0])*(v011[2]-v010[2]) )
                               *(v000[1]-v010[1])  )
                         + (  ( (v010[0]-v110[0])*(v011[1]-v010[1])
                              - (v010[1]-v110[1])*(v011[0]-v010[0]) )
                               *(v000[2]-v010[2])  ));
              if ((w1 > 0 != Pos) || w1 == 0 ||
                  (w2 > 0 != Pos) || w2 == 0 ||
                  (w3 > 0 != Pos) || w3 == 0 ||
                  (w4 > 0 != Pos) || w4 == 0 ||
                  (w5 > 0 != Pos) || w5 == 0 ||
                  (w6 > 0 != Pos) || w6 == 0 ||
                  (w7 > 0 != Pos) || w7 == 0 ||
                  (w8 > 0 != Pos) || w8 == 0) {
                throw new SetException("Gridded3DDoubleSet: samples do not "
                                       +"form a valid grid ("
                                       +i+","+j+","+k+")");
              }
            }
          }
        }
      } // end if (test)
    }
  }

  /** a 3-D set with manifold dimension = 2, with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded3DDoubleSet(MathType type, double[][] samples, int lengthX,
                      int lengthY) throws VisADException {
    this(type, samples, lengthX, lengthY, null, null, null, true);
  }

  /** a 3-D set with manifold dimension = 2; samples array is
      organized double[3][number_of_samples] where lengthX * lengthY
      = number_of_samples; no geometric constraint on samples; the
      X component increases fastest in the second index of samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded3DDoubleSet(MathType type, double[][] samples,
                      int lengthX, int lengthY,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, lengthY, coord_sys, units, errors, true);
  }

  public Gridded3DDoubleSet(MathType type, double[][] samples,
               int lengthX, int lengthY,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    super(type, null, lengthX, lengthY, coord_sys, units, errors, copy);
    if (samples == null) {
      throw new SetException("Gridded3DDoubleSet: samples are null");
    }
    init_doubles(samples, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];
    LengthY = Lengths[1];
    LowZ = Low[2];
    HiZ = Hi[2];

    // no Samples consistency test
  }

  /** a 3-D set with manifold dimension = 1, with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded3DDoubleSet(MathType type, double[][] samples, int lengthX)
         throws VisADException {
    this(type, samples, lengthX, null, null, null, true);
  }

  /** a 3-D set with manifold dimension = 1; samples array is
      organized double[3][number_of_samples] where lengthX =
      number_of_samples; no geometric constraint on samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded3DDoubleSet(MathType type, double[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, coord_sys, units, errors, true);
  }

  public Gridded3DDoubleSet(MathType type, double[][] samples, int lengthX,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    super(type, null, lengthX, coord_sys, units, errors, copy);
    if (samples == null) {
      throw new SetException("Gridded3DDoubleSet: samples are null");
    }
    init_doubles(samples, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];
    LowZ = Low[2];
    HiZ = Hi[2];

    // no Samples consistency test
  }


  // Overridden Gridded3DSet methods (float[][])

  public float[][] getSamples() throws VisADException {
    return getSamples(true);
  }

  public float[][] getSamples(boolean copy) throws VisADException {
    return Set.doubleToFloat(Samples);
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    return Set.doubleToFloat(indexToDouble(index));
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    return doubleToIndex(Set.floatToDouble(value));
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    return Set.doubleToFloat(gridToDouble(Set.floatToDouble(grid)));
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    return Set.doubleToFloat(doubleToGrid(Set.floatToDouble(value)));
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void valueToInterp(float[][] value, int[][] indices,
    float[][] weights) throws VisADException
  {
    int len = weights.length;
    double[][] w = new double[len][];
    doubleToInterp(Set.floatToDouble(value), indices, w);
    for (int i=0; i<len; i++) {
      if (w[i] != null) {
        weights[i] = new float[w[i].length];
        for (int j=0; j<w[i].length; j++) {
          weights[i][j] = (float) w[i][j];
        }
      }
    }
  }


  // Corresponding Gridded3DDoubleSet methods (double[][])

  public double[][] getDoubles() throws VisADException {
    return getDoubles(true);
  }

  public double[][] getDoubles(boolean copy) throws VisADException {
    return copy ? Set.copyDoubles(Samples) : Samples;
  }

  /** convert an array of 1-D indices to an array of values in
      R^DomainDimension */
  public double[][] indexToDouble(int[] index) throws VisADException {
    int length = index.length;
    if (Samples == null) {
      // not used - over-ridden by Linear3DSet.indexToValue
      int indexX, indexY, indexZ;
      int k;
      double[][] grid = new double[ManifoldDimension][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          indexX = index[i] % LengthX;
          k = index[i] / LengthX;
          indexY = k % LengthY;
          indexZ = k / LengthY;
        }
        else {
          indexX = -1;
          indexY = -1;
          indexZ = -1;
        }
        grid[0][i] = indexX;
        grid[1][i] = indexY;
        grid[2][i] = indexZ;
      }
      return gridToDouble(grid);
    }
    else {
      double[][] values = new double[3][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          values[0][i] = Samples[0][index[i]];
          values[1][i] = Samples[1][index[i]];
          values[2][i] = Samples[2][index[i]];
        }
        else {
          values[0][i] = Double.NaN;
          values[1][i] = Double.NaN;
          values[2][i] = Double.NaN;
        }
      }
      return values;
    }
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] doubleToIndex(double[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded3DDoubleSet.doubleToIndex: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length;
    int[] index = new int[length];

    double[][] grid = doubleToGrid(value);
    double[] grid0 = grid[0];
    double[] grid1 = grid[1];
    double[] grid2 = grid[2];
    double g0, g1, g2;
    for (int i=0; i<length; i++) {
      g0 = grid0[i];
      g1 = grid1[i];
      g2 = grid2[i];
      // test for missing
      index[i] = (g0 != g0 || g1 != g1 || g2 != g2) ? -1 :
                 ((int) (g0 + 0.5)) + LengthX*( ((int) (g1 + 0.5)) +
                  LengthY*((int) (g2 + 0.5)));
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public double[][] gridToDouble(double[][] grid) throws VisADException {
    if (grid.length != ManifoldDimension) {
      throw new SetException("Gridded3DDoubleSet.gridToDouble: grid dimension " +
                             grid.length +
                             " not equal to Manifold dimension " +
                             ManifoldDimension);
    }
    if (ManifoldDimension == 3) {
      return gridToDouble3D(grid);
    }
    else if (ManifoldDimension == 2) {
      return gridToDouble2D(grid);
    }
    else {
      throw new SetException("Gridded3DDoubleSet.gridToDouble: ManifoldDimension " +
                             "must be 2 or 3");
    }
  }

  private double[][] gridToDouble2D(double[][] grid) throws VisADException {
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
      throw new SetException("Gridded3DDoubleSet.gridToDouble: requires all grid " +
                             "dimensions to be > 1");
    }
    // avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(grid[0].length, grid[1].length);
    double[][] value = new double[3][length];
    for (int i=0; i<length; i++) {
      // let gx and gy by the current grid values
      double gx = grid[0][i];
      double gy = grid[1][i];
      if ( (gx < -0.5)        || (gy < -0.5) ||
           (gx > LengthX-0.5) || (gy > LengthY-0.5) ) {
        value[0][i] = value[1][i] = value[2][i] = Double.NaN;
      } else if (Length == 1) {
        value[0][i] = Samples[0][0];
        value[1][i] = Samples[1][0];
        value[2][i] = Samples[2][0];
      } else {
        // calculate closest integer variables
        int igx = (int) gx;
        int igy = (int) gy;
        if (igx < 0) igx = 0;
        if (igx > LengthX-2) igx = LengthX-2;
        if (igy < 0) igy = 0;
        if (igy > LengthY-2) igy = LengthY-2;
  
        // set up conversion to 1D Samples array
        int[][] s = { {LengthX*igy+igx,           // (0, 0)
                       LengthX*(igy+1)+igx},      // (0, 1)
                      {LengthX*igy+igx+1,         // (1, 0)
                       LengthX*(igy+1)+igx+1} };  // (1, 1)
        if (gx+gy-igx-igy-1 <= 0) {
          // point is in LOWER triangle
          for (int j=0; j<3; j++) {
            value[j][i] = Samples[j][s[0][0]]
              + (gx-igx)*(Samples[j][s[1][0]]-Samples[j][s[0][0]])
              + (gy-igy)*(Samples[j][s[0][1]]-Samples[j][s[0][0]]);
          }
        }
        else {
          // point is in UPPER triangle
          for (int j=0; j<3; j++) {
            value[j][i] = Samples[j][s[1][1]]
              + (1+igx-gx)*(Samples[j][s[0][1]]-Samples[j][s[1][1]])
              + (1+igy-gy)*(Samples[j][s[1][0]]-Samples[j][s[1][1]]);
          }
        }
      }
    }
    return value;
  }

  private double[][] gridToDouble3D(double[][] grid) throws VisADException {
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2 || Lengths[2] < 2)) {
      throw new SetException("Gridded3DDoubleSet.gridToDouble: requires all grid " +
                             "dimensions to be > 1");
    }
    // avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(grid[0].length, grid[1].length);
    length = Math.min(length, grid[2].length);
    double[][] value = new double[3][length];
    double[] A = new double[3];
    double[] B = new double[3];
    double[] C = new double[3];
    double[] D = new double[3];
    double[] E = new double[3];
    double[] F = new double[3];
    double[] G = new double[3];
    double[] H = new double[3];




    for (int i=0; i<length; i++) {
      // let gx, gy, and gz be the current grid values
      double gx = grid[0][i];
      double gy = grid[1][i];
      double gz = grid[2][i];
      if ( (gx < -0.5)        || (gy < -0.5)        || (gz < -0.5) ||
           (gx > LengthX-0.5) || (gy > LengthY-0.5) || (gz > LengthZ-0.5) ) {
        value[0][i] = value[1][i] = value[2][i] = Double.NaN;
      } else if (Length == 1) {
        value[0][i] = Samples[0][0];
        value[1][i] = Samples[1][0];
        value[2][i] = Samples[2][0];
      } else {
        // calculate closest integer variables
        int igx, igy, igz;
        if (gx < 0) igx = 0;
        else if (gx > LengthX-2) igx = LengthX - 2;
        else igx = (int) gx;
        if (gy < 0) igy = 0;
        else if (gy > LengthY-2) igy = LengthY - 2;
        else igy = (int) gy;
        if (gz < 0) igz = 0;
        else if (gz > LengthZ-2) igz = LengthZ - 2;
        else igz = (int) gz;
  
        // determine tetrahedralization type
        boolean evencube = ((igx+igy+igz) % 2 == 0);
  
        // calculate distances from integer grid point
        double s, t, u;
        if (evencube) {
          s = gx - igx;
          t = gy - igy;
          u = gz - igz;
        }
        else {
          s = 1 + igx - gx;
          t = 1 + igy - gy;
          u = 1 + igz - gz;
        }
  
        // Define vertices of grid box
        int zadd = LengthY*LengthX;
        int base = igz*zadd + igy*LengthX + igx;
        int ai = base+zadd;            // 0, 0, 1
        int bi = base+zadd+1;          // 1, 0, 1
        int ci = base+zadd+LengthX+1;  // 1, 1, 1
        int di = base+zadd+LengthX;    // 0, 1, 1
        int ei = base;                 // 0, 0, 0
        int fi = base+1;               // 1, 0, 0
        int gi = base+LengthX+1;       // 1, 1, 0
        int hi = base+LengthX;         // 0, 1, 0
        if (evencube) {
          A[0] = Samples[0][ai];
          A[1] = Samples[1][ai];
          A[2] = Samples[2][ai];
          B[0] = Samples[0][bi];
          B[1] = Samples[1][bi];
          B[2] = Samples[2][bi];
          C[0] = Samples[0][ci];
          C[1] = Samples[1][ci];
          C[2] = Samples[2][ci];
          D[0] = Samples[0][di];
          D[1] = Samples[1][di];
          D[2] = Samples[2][di];
          E[0] = Samples[0][ei];
          E[1] = Samples[1][ei];
          E[2] = Samples[2][ei];
          F[0] = Samples[0][fi];
          F[1] = Samples[1][fi];
          F[2] = Samples[2][fi];
          G[0] = Samples[0][gi];
          G[1] = Samples[1][gi];
          G[2] = Samples[2][gi];
          H[0] = Samples[0][hi];
          H[1] = Samples[1][hi];
          H[2] = Samples[2][hi];
        }
        else {
          G[0] = Samples[0][ai];
          G[1] = Samples[1][ai];
          G[2] = Samples[2][ai];
          H[0] = Samples[0][bi];
          H[1] = Samples[1][bi];
          H[2] = Samples[2][bi];
          E[0] = Samples[0][ci];
          E[1] = Samples[1][ci];
          E[2] = Samples[2][ci];
          F[0] = Samples[0][di];
          F[1] = Samples[1][di];
          F[2] = Samples[2][di];
          C[0] = Samples[0][ei];
          C[1] = Samples[1][ei];
          C[2] = Samples[2][ei];
          D[0] = Samples[0][fi];
          D[1] = Samples[1][fi];
          D[2] = Samples[2][fi];
          A[0] = Samples[0][gi];
          A[1] = Samples[1][gi];
          A[2] = Samples[2][gi];
          B[0] = Samples[0][hi];
          B[1] = Samples[1][hi];
          B[2] = Samples[2][hi];
        }
  
        // These tests determine which tetrahedron the point is in
        boolean test1 = (1 - s - t - u >= 0);
        boolean test2 = (s - t + u - 1 >= 0);
        boolean test3 = (t - s + u - 1 >= 0);
        boolean test4 = (s + t - u - 1 >= 0);
  
        // These cases handle grid coordinates off the grid
        // (Different tetrahedrons must be chosen accordingly)
        if ( (gx < 0) || (gx > LengthX-1)
          || (gy < 0) || (gy > LengthY-1)
          || (gz < 0) || (gz > LengthZ-1) ) {
          boolean OX, OY, OZ, MX, MY, MZ, LX, LY, LZ;
          OX = OY = OZ = MX = MY = MZ = LX = LY = LZ = false;
          if (igx == 0) OX = true;
          if (igy == 0) OY = true;
          if (igz == 0) OZ = true;
          if (igx == LengthX-2) LX = true;
          if (igy == LengthY-2) LY = true;
          if (igz == LengthZ-2) LZ = true;
          if (!OX && !LX) MX = true;
          if (!OY && !LY) MY = true;
          if (!OZ && !LZ) MZ = true;
          test1 = test2 = test3 = test4 = false;
          // 26 cases
          if (evencube) {
            if (!LX && !LY && !LZ) test1 = true;
            else if ( (LX && OY && MZ) || (MX && OY && LZ)
                   || (LX && MY && LZ) || (LX && OY && LZ)
                   || (MX && MY && LZ) || (LX && MY && MZ) ) test2 = true;
            else if ( (OX && LY && MZ) || (OX && MY && LZ)
                   || (MX && LY && LZ) || (OX && LY && LZ)
                                       || (MX && LY && MZ) ) test3 = true;
            else if ( (MX && LY && OZ) || (LX && MY && OZ)
                   || (LX && LY && MZ) || (LX && LY && OZ) ) test4 = true;
          }
          else {
            if (!OX && !OY && !OZ) test1 = true;
            else if ( (OX && MY && OZ) || (MX && LY && OZ)
                   || (OX && LY && MZ) || (OX && LY && OZ)
                   || (MX && MY && OZ) || (OX && MY && MZ) ) test2 = true;
            else if ( (LX && MY && OZ) || (MX && OY && OZ)
                   || (LX && OY && MZ) || (LX && OY && OZ)
                                       || (MX && OY && MZ) ) test3 = true;
            else if ( (OX && OY && MZ) || (OX && MY && OZ)
                   || (MX && OY && LZ) || (OX && OY && LZ) ) test4 = true;
          }
        }
        if (test1) {
          for (int j=0; j<3; j++) {
            value[j][i] = E[j] + s*(F[j]-E[j])
                               + t*(H[j]-E[j])
                               + u*(A[j]-E[j]);
          }
        }
        else if (test2) {
          for (int j=0; j<3; j++) {
            value[j][i] = B[j] + (1-s)*(A[j]-B[j])
                                   + t*(C[j]-B[j])
                               + (1-u)*(F[j]-B[j]);
          }
        }
        else if (test3) {
          for (int j=0; j<3; j++) {
            value[j][i] = D[j]     + s*(C[j]-D[j])
                               + (1-t)*(A[j]-D[j])
                               + (1-u)*(H[j]-D[j]);
          }
        }
        else if (test4) {
          for (int j=0; j<3; j++) {
            value[j][i] = G[j] + (1-s)*(H[j]-G[j])
                               + (1-t)*(F[j]-G[j])
                                   + u*(C[j]-G[j]);
          }
        }
        else {
          for (int j=0; j<3; j++) {
            value[j][i] = (H[j]+F[j]+A[j]-C[j])/2 + s*(C[j]+F[j]-H[j]-A[j])/2
                                                  + t*(C[j]-F[j]+H[j]-A[j])/2
                                                  + u*(C[j]-F[j]-H[j]+A[j])/2;
          }
        }
      }
    }
    return value;
  }

  // WLH 6 Dec 2001
  //private int gx = -1;
  //private int gy = -1;
  //private int gz = -1;

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public double[][] doubleToGrid(double[][] value) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Gridded3DDoubleSet.doubleToGrid: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    if (ManifoldDimension < 3) {
      throw new SetException("Gridded3DDoubleSet.doubleToGrid: ManifoldDimension " +
                             "must be 3");
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2 || Lengths[2] < 2)) {
      throw new SetException("Gridded3DDoubleSet.doubleToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    // Avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(value[0].length, value[1].length);
    length = Math.min(length, value[2].length);
    double[][] grid = new double[ManifoldDimension][length];

    // (gx, gy, gz) is the current grid box guess
    int gx = (LengthX-1)/2;
    int gy = (LengthY-1)/2;
    int gz = (LengthZ-1)/2;
/* WLH 6 Dec 2001
    // use value from last call as first guess, if reasonable
    if (gx < 0 || gx >= LengthX || gy < 0 || gy >= LengthY ||
        gz < 0 || gz >= LengthZ) {
      gx = (LengthX-1)/2;
      gy = (LengthY-1)/2;
      gz = (LengthZ-1)/2;
    }
*/

    double[] A = new double[3];
    double[] B = new double[3];
    double[] C = new double[3];
    double[] D = new double[3];
    double[] E = new double[3];
    double[] F = new double[3];
    double[] G = new double[3];
    double[] H = new double[3];

    double[] M = new double[3];
    double[] N = new double[3];
    double[] O = new double[3];
    double[] P = new double[3];
    double[] X = new double[3];
    double[] Y = new double[3];
    double[] Q = new double[3];


    for (int i=0; i<length; i++) {

      if (Length == 1) {
        if (Double.isNaN(value[0][i]) || Double.isNaN(value[1][i]) || Double.isNaN(value[2][i])) {
           grid[0][i] = grid[1][i] = grid[2][i] = Double.NaN;
        } else {
           grid[0][i] = 0;
           grid[1][i] = 0;
           grid[2][i] = 0;
        }
        continue;
      }

      // a flag indicating whether point is off the grid
      boolean offgrid = false;
      // the first guess should be the last box unless there was no solution
      // test for missing
      if ( (i != 0) && grid[0][i-1] != grid[0][i-1] ) {
        gx = (LengthX-1)/2;
        gy = (LengthY-1)/2;
        gz = (LengthZ-1)/2;
      }
      int tetnum = 5;  // Tetrahedron number in which to start search
      // if the iteration loop fails, the result should be NaN
      grid[0][i] = grid[1][i] = grid[2][i] = Double.NaN;
      for (int itnum=0; itnum<2*(LengthX+LengthY+LengthZ); itnum++) {
        // determine tetrahedralization type
        boolean evencube = ((gx+gy+gz) % 2 == 0);

        // Define vertices of grid box
        int zadd = LengthY*LengthX;
        int base = gz*zadd + gy*LengthX + gx;
        int ai = base+zadd;            // 0, 0, 1
        int bi = base+zadd+1;          // 1, 0, 1
        int ci = base+zadd+LengthX+1;  // 1, 1, 1
        int di = base+zadd+LengthX;    // 0, 1, 1
        int ei = base;                 // 0, 0, 0
        int fi = base+1;               // 1, 0, 0
        int gi = base+LengthX+1;       // 1, 1, 0
        int hi = base+LengthX;         // 0, 1, 0
        if (evencube) {
          A[0] = Samples[0][ai];
          A[1] = Samples[1][ai];
          A[2] = Samples[2][ai];
          B[0] = Samples[0][bi];
          B[1] = Samples[1][bi];
          B[2] = Samples[2][bi];
          C[0] = Samples[0][ci];
          C[1] = Samples[1][ci];
          C[2] = Samples[2][ci];
          D[0] = Samples[0][di];
          D[1] = Samples[1][di];
          D[2] = Samples[2][di];
          E[0] = Samples[0][ei];
          E[1] = Samples[1][ei];
          E[2] = Samples[2][ei];
          F[0] = Samples[0][fi];
          F[1] = Samples[1][fi];
          F[2] = Samples[2][fi];
          G[0] = Samples[0][gi];
          G[1] = Samples[1][gi];
          G[2] = Samples[2][gi];
          H[0] = Samples[0][hi];
          H[1] = Samples[1][hi];
          H[2] = Samples[2][hi];
        }
        else {
          G[0] = Samples[0][ai];
          G[1] = Samples[1][ai];
          G[2] = Samples[2][ai];
          H[0] = Samples[0][bi];
          H[1] = Samples[1][bi];
          H[2] = Samples[2][bi];
          E[0] = Samples[0][ci];
          E[1] = Samples[1][ci];
          E[2] = Samples[2][ci];
          F[0] = Samples[0][di];
          F[1] = Samples[1][di];
          F[2] = Samples[2][di];
          C[0] = Samples[0][ei];
          C[1] = Samples[1][ei];
          C[2] = Samples[2][ei];
          D[0] = Samples[0][fi];
          D[1] = Samples[1][fi];
          D[2] = Samples[2][fi];
          A[0] = Samples[0][gi];
          A[1] = Samples[1][gi];
          A[2] = Samples[2][gi];
          B[0] = Samples[0][hi];
          B[1] = Samples[1][hi];
          B[2] = Samples[2][hi];
        }

        // Compute tests and go to a new box depending on results
        boolean test1, test2, test3, test4;
        double tval1, tval2, tval3, tval4;
        int ogx = gx;
        int ogy = gy;
        int ogz = gz;
        if (tetnum==1) {
          tval1 = ( (E[1]-A[1])*(F[2]-E[2]) - (E[2]-A[2])*(F[1]-E[1]) )
                   *(value[0][i]-E[0])
                + ( (E[2]-A[2])*(F[0]-E[0]) - (E[0]-A[0])*(F[2]-E[2]) )
                   *(value[1][i]-E[1])
                + ( (E[0]-A[0])*(F[1]-E[1]) - (E[1]-A[1])*(F[0]-E[0]) )
                   *(value[2][i]-E[2]);
          tval2 = ( (E[1]-H[1])*(A[2]-E[2]) - (E[2]-H[2])*(A[1]-E[1]) )
                   *(value[0][i]-E[0])
                + ( (E[2]-H[2])*(A[0]-E[0]) - (E[0]-H[0])*(A[2]-E[2]) )
                   *(value[1][i]-E[1])
                + ( (E[0]-H[0])*(A[1]-E[1]) - (E[1]-H[1])*(A[0]-E[0]) )
                   *(value[2][i]-E[2]);
          tval3 = ( (E[1]-F[1])*(H[2]-E[2]) - (E[2]-F[2])*(H[1]-E[1]) )
                   *(value[0][i]-E[0])
                + ( (E[2]-F[2])*(H[0]-E[0]) - (E[0]-F[0])*(H[2]-E[2]) )
                   *(value[1][i]-E[1])
                + ( (E[0]-F[0])*(H[1]-E[1]) - (E[1]-F[1])*(H[0]-E[0]) )
                   *(value[2][i]-E[2]);
          test1 = (tval1 == 0) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (tval2 == 0) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (tval3 == 0) || ((tval3 > 0) == (!evencube)^Pos);

          // if a test failed go to a new box
          int updown = (evencube) ? -1 : 1;
          if (!test1) gy += updown; // UP/DOWN
          if (!test2) gx += updown; // LEFT/RIGHT
          if (!test3) gz += updown; // BACK/FORWARD
          tetnum = 5;
          // Snap coordinates back onto grid in case they fell off.
          if (gx < 0) gx = 0;
          if (gy < 0) gy = 0;
          if (gz < 0) gz = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy > LengthY-2) gy = LengthY-2;
          if (gz > LengthZ-2) gz = LengthZ-2;

          // Detect if the point is off the grid entirely
          if ( (gx == ogx) && (gy == ogy) && (gz == ogz)
            && (!test1 || !test2 || !test3) && !offgrid ) {
            offgrid = true;
            continue;
          }

          // If all tests pass then this is the correct tetrahedron
          if (  ( (gx == ogx) && (gy == ogy) && (gz == ogz) )
                || offgrid) {
            // solve point
            for (int j=0; j<3; j++) {
              M[j] = (F[j]-E[j])*(A[(j+1)%3]-E[(j+1)%3])
                   - (F[(j+1)%3]-E[(j+1)%3])*(A[j]-E[j]);
              N[j] = (H[j]-E[j])*(A[(j+1)%3]-E[(j+1)%3])
                   - (H[(j+1)%3]-E[(j+1)%3])*(A[j]-E[j]);
              O[j] = (F[(j+1)%3]-E[(j+1)%3])*(A[(j+2)%3]-E[(j+2)%3])
                   - (F[(j+2)%3]-E[(j+2)%3])*(A[(j+1)%3]-E[(j+1)%3]);
              P[j] = (H[(j+1)%3]-E[(j+1)%3])*(A[(j+2)%3]-E[(j+2)%3])
                   - (H[(j+2)%3]-E[(j+2)%3])*(A[(j+1)%3]-E[(j+1)%3]);
              X[j] = value[(j+2)%3][i]*(A[(j+1)%3]-E[(j+1)%3])
                   - value[(j+1)%3][i]*(A[(j+2)%3]-E[(j+2)%3])
                   + E[(j+1)%3]*A[(j+2)%3] - E[(j+2)%3]*A[(j+1)%3];
              Y[j] = value[j][i]*(A[(j+1)%3]-E[(j+1)%3])
                   - value[(j+1)%3][i]*(A[j]-E[j])
                   + E[(j+1)%3]*A[j] - E[j]*A[(j+1)%3];
            }
            double s, t, u;
            // these if statements handle skewed grids
            double d0 = M[0]*P[0] - N[0]*O[0];
            double d1 = M[1]*P[1] - N[1]*O[1];
            double d2 = M[2]*P[2] - N[2]*O[2];
            double ad0 = Math.abs(d0);
            double ad1 = Math.abs(d1);
            double ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              s = (N[0]*X[0] + P[0]*Y[0])/d0;
              t = -(M[0]*X[0] + O[0]*Y[0])/d0;
            }
            else if (ad1 > ad2) {
              s = (N[1]*X[1] + P[1]*Y[1])/d1;
              t = -(M[1]*X[1] + O[1]*Y[1])/d1;
            }
            else {
              s = (N[2]*X[2] + P[2]*Y[2])/d2;
              t = -(M[2]*X[2] + O[2]*Y[2])/d2;
            }
            d0 = A[0]-E[0];
            d1 = A[1]-E[1];
            d2 = A[2]-E[2];
            ad0 = Math.abs(d0);
            ad1 = Math.abs(d1);
            ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              u = ( value[0][i] - E[0] - s*(F[0]-E[0])
                - t*(H[0]-E[0]) ) / d0;
            }
            else if (ad1 > ad2) {
              u = ( value[1][i] - E[1] - s*(F[1]-E[1])
                - t*(H[1]-E[1]) ) / d1;
            }
            else {
              u = ( value[2][i] - E[2] - s*(F[2]-E[2])
                - t*(H[2]-E[2]) ) / d2;
            }
            if (evencube) {
              grid[0][i] = gx+s;
              grid[1][i] = gy+t;
              grid[2][i] = gz+u;
            }
            else {
              grid[0][i] = gx+1-s;
              grid[1][i] = gy+1-t;
              grid[2][i] = gz+1-u;
            }
            break;
          }
        }
        else if (tetnum==2) {
          tval1 = ( (B[1]-C[1])*(F[2]-B[2]) - (B[2]-C[2])*(F[1]-B[1]) )
                   *(value[0][i]-B[0])
                + ( (B[2]-C[2])*(F[0]-B[0]) - (B[0]-C[0])*(F[2]-B[2]) )
                   *(value[1][i]-B[1])
                + ( (B[0]-C[0])*(F[1]-B[1]) - (B[1]-C[1])*(F[0]-B[0]) )
                   *(value[2][i]-B[2]);
          tval2 = ( (B[1]-A[1])*(C[2]-B[2]) - (B[2]-A[2])*(C[1]-B[1]) )
                   *(value[0][i]-B[0])
                + ( (B[2]-A[2])*(C[0]-B[0]) - (B[0]-A[0])*(C[2]-B[2]) )
                   *(value[1][i]-B[1])
                + ( (B[0]-A[0])*(C[1]-B[1]) - (B[1]-A[1])*(C[0]-B[0]) )
                   *(value[2][i]-B[2]);
          tval3 = ( (B[1]-F[1])*(A[2]-B[2]) - (B[2]-F[2])*(A[1]-B[1]) )
                   *(value[0][i]-B[0])
                + ( (B[2]-F[2])*(A[0]-B[0]) - (B[0]-F[0])*(A[2]-B[2]) )
                   *(value[1][i]-B[1])
                + ( (B[0]-F[0])*(A[1]-B[1]) - (B[1]-F[1])*(A[0]-B[0]) )
                   *(value[2][i]-B[2]);
          test1 = (tval1 == 0) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (tval2 == 0) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (tval3 == 0) || ((tval3 > 0) == (!evencube)^Pos);

          // if a test failed go to a new box
          if (!test1 &&  evencube) gx++; // RIGHT
          if (!test1 && !evencube) gx--; // LEFT
          if (!test2 &&  evencube) gz++; // FORWARD
          if (!test2 && !evencube) gz--; // BACK
          if (!test3 &&  evencube) gy--; // UP
          if (!test3 && !evencube) gy++; // DOWN
          tetnum = 5;
          // Snap coordinates back onto grid in case they fell off
          if (gx < 0) gx = 0;
          if (gy < 0) gy = 0;
          if (gz < 0) gz = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy > LengthY-2) gy = LengthY-2;
          if (gz > LengthZ-2) gz = LengthZ-2;

          // Detect if the point is off the grid entirely
          if ( (gx == ogx) && (gy == ogy) && (gz == ogz)
            && (!test1 || !test2 || !test3) && !offgrid ) {
            offgrid = true;
            continue;
          }

          // If all tests pass then this is the correct tetrahedron
          if (  ( (gx == ogx) && (gy == ogy) && (gz == ogz) )
                || offgrid) {
            // solve point
            for (int j=0; j<3; j++) {
              M[j] = (A[j]-B[j])*(F[(j+1)%3]-B[(j+1)%3])
                   - (A[(j+1)%3]-B[(j+1)%3])*(F[j]-B[j]);
              N[j] = (C[j]-B[j])*(F[(j+1)%3]-B[(j+1)%3])
                   - (C[(j+1)%3]-B[(j+1)%3])*(F[j]-B[j]);
              O[j] = (A[(j+1)%3]-B[(j+1)%3])*(F[(j+2)%3]-B[(j+2)%3])
                   - (A[(j+2)%3]-B[(j+2)%3])*(F[(j+1)%3]-B[(j+1)%3]);
              P[j] = (C[(j+1)%3]-B[(j+1)%3])*(F[(j+2)%3]-B[(j+2)%3])
                   - (C[(j+2)%3]-B[(j+2)%3])*(F[(j+1)%3]-B[(j+1)%3]);
              X[j] = value[(j+2)%3][i]*(F[(j+1)%3]-B[(j+1)%3])
                   - value[(j+1)%3][i]*(F[(j+2)%3]-B[(j+2)%3])
                   + B[(j+1)%3]*F[(j+2)%3] - B[(j+2)%3]*F[(j+1)%3];
              Y[j] = value[j][i]*(F[(j+1)%3]-B[(j+1)%3])
                   - value[1][i]*(F[j]-B[j])
                   + B[(j+1)%3]*F[j] - B[j]*F[(j+1)%3];
            }
            double s, t, u;
            // these if statements handle skewed grids
            double d0 = M[0]*P[0] - N[0]*O[0];
            double d1 = M[1]*P[1] - N[1]*O[1];
            double d2 = M[2]*P[2] - N[2]*O[2];
            double ad0 = Math.abs(d0);
            double ad1 = Math.abs(d1);
            double ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              s = 1 - (N[0]*X[0] + P[0]*Y[0])/d0;
              t = -(M[0]*X[0] + O[0]*Y[0])/d0;
            }
            else if (ad1 > ad2) {
              s = 1 - (N[1]*X[1] + P[1]*Y[1])/d1;
              t = -(M[1]*X[1] + O[1]*Y[1])/d1;
            }
            else {
              s = 1 - (N[2]*X[2] + P[2]*Y[2])/d2;
              t = -(M[2]*X[2] + O[2]*Y[2])/d2;
            }
            d0 = F[0]-B[0];
            d1 = F[1]-B[1];
            d2 = F[2]-B[2];
            ad0 = Math.abs(d0);
            ad1 = Math.abs(d1);
            ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              u = 1 - ( value[0][i] - B[0] - (1-s)*(A[0]-B[0])
                - t*(C[0]-B[0]) ) / d0;
            }
            else if (ad1 > ad2) {
              u = 1 - ( value[1][i] - B[1] - (1-s)*(A[1]-B[1])
                - t*(C[1]-B[1]) ) / d1;
            }
            else {
              u = 1 - ( value[2][i] - B[2] - (1-s)*(A[2]-B[2])
                - t*(C[2]-B[2]) ) / d2;
            }
            if (evencube) {
              grid[0][i] = gx+s;
              grid[1][i] = gy+t;
              grid[2][i] = gz+u;
            }
            else {
              grid[0][i] = gx+1-s;
              grid[1][i] = gy+1-t;
              grid[2][i] = gz+1-u;
            }
            break;
          }
        }
        else if (tetnum==3) {
          tval1 = ( (D[1]-A[1])*(H[2]-D[2]) - (D[2]-A[2])*(H[1]-D[1]) )
                   *(value[0][i]-D[0])
                + ( (D[2]-A[2])*(H[0]-D[0]) - (D[0]-A[0])*(H[2]-D[2]) )
                   *(value[1][i]-D[1])
                + ( (D[0]-A[0])*(H[1]-D[1]) - (D[1]-A[1])*(H[0]-D[0]) )
                   *(value[2][i]-D[2]);
          tval2 = ( (D[1]-C[1])*(A[2]-D[2]) - (D[2]-C[2])*(A[1]-D[1]) )
                   *(value[0][i]-D[0])
                + ( (D[2]-C[2])*(A[0]-D[0]) - (D[0]-C[0])*(A[2]-D[2]) )
                   *(value[1][i]-D[1])
                + ( (D[0]-C[0])*(A[1]-D[1]) - (D[1]-C[1])*(A[0]-D[0]) )
                   *(value[2][i]-D[2]);
          tval3 = ( (D[1]-H[1])*(C[2]-D[2]) - (D[2]-H[2])*(C[1]-D[1]) )
                   *(value[0][i]-D[0])
                + ( (D[2]-H[2])*(C[0]-D[0]) - (D[0]-H[0])*(C[2]-D[2]) )
                   *(value[1][i]-D[1])
                + ( (D[0]-H[0])*(C[1]-D[1]) - (D[1]-H[1])*(C[0]-D[0]) )
                   *(value[2][i]-D[2]);
          test1 = (tval1 == 0) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (tval2 == 0) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (tval3 == 0) || ((tval3 > 0) == (!evencube)^Pos);

          // if a test failed go to a new box
          if (!test1 &&  evencube) gx--; // LEFT
          if (!test1 && !evencube) gx++; // RIGHT
          if (!test2 &&  evencube) gz++; // FORWARD
          if (!test2 && !evencube) gz--; // BACK
          if (!test3 &&  evencube) gy++; // DOWN
          if (!test3 && !evencube) gy--; // UP
          tetnum = 5;
          // Snap coordinates back onto grid in case they fell off
          if (gx < 0) gx = 0;
          if (gy < 0) gy = 0;
          if (gz < 0) gz = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy > LengthY-2) gy = LengthY-2;
          if (gz > LengthZ-2) gz = LengthZ-2;

          // Detect if the point is off the grid entirely
          if ( (gx == ogx) && (gy == ogy) && (gz == ogz)
            && (!test1 || !test2 || !test3) && !offgrid ) {
            offgrid = true;
            continue;
          }

          // If all tests pass then this is the correct tetrahedron
          if (  ( (gx == ogx) && (gy == ogy) && (gz == ogz) )
                || offgrid) {
            // solve point
            for (int j=0; j<3; j++) {
              M[j] = (C[j]-D[j])*(H[(j+1)%3]-D[(j+1)%3])
                   - (C[(j+1)%3]-D[(j+1)%3])*(H[j]-D[j]);
              N[j] = (A[j]-D[j])*(H[(j+1)%3]-D[(j+1)%3])
                   - (A[(j+1)%3]-D[(j+1)%3])*(H[j]-D[j]);
              O[j] = (C[(j+1)%3]-D[(j+1)%3])*(H[(j+2)%3]-D[(j+2)%3])
                   - (C[(j+2)%3]-D[(j+2)%3])*(H[(j+1)%3]-D[(j+1)%3]);
              P[j] = (A[(j+1)%3]-D[(j+1)%3])*(H[(j+2)%3]-D[(j+2)%3])
                   - (A[(j+2)%3]-D[(j+2)%3])*(H[(j+1)%3]-D[(j+1)%3]);
              X[j] = value[(j+2)%3][i]*(H[(j+1)%3]-D[(j+1)%3])
                   - value[(j+1)%3][i]*(H[(j+2)%3]-D[(j+2)%3])
                   + D[(j+1)%3]*H[(j+2)%3] - D[(j+2)%3]*H[(j+1)%3];
              Y[j] = value[j][i]*(H[(j+1)%3]-D[(j+1)%3])
                   - value[(j+1)%3][i]*(H[j]-D[j])
                   + D[(j+1)%3]*H[j] - D[j]*H[(j+1)%3];
            }
            double s, t, u;
            // these if statements handle skewed grids
            double d0 = M[0]*P[0] - N[0]*O[0];
            double d1 = M[1]*P[1] - N[1]*O[1];
            double d2 = M[2]*P[2] - N[2]*O[2];
            double ad0 = Math.abs(d0);
            double ad1 = Math.abs(d1);
            double ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              s = (N[0]*X[0] + P[0]*Y[0])/d0;
              t = 1 + (M[0]*X[0] + O[0]*Y[0])/d0;
            }
            else if (ad1 > ad2) {
              s = (N[1]*X[1] + P[1]*Y[1])/d1;
              t = 1 + (M[1]*X[1] + O[1]*Y[1])/d1;
            }
            else {
              s = (N[2]*X[2] + P[2]*Y[2])/d2;
              t =  1 + (M[2]*X[2] + O[2]*Y[2])/d2;
            }
            d0 = H[0]-D[0];
            d1 = H[1]-D[1];
            d2 = H[2]-D[2];
            ad0 = Math.abs(d0);
            ad1 = Math.abs(d1);
            ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              u = 1 - ( value[0][i] - D[0] - s*(C[0]-D[0])
                - (1-t)*(A[0]-D[0]) ) / d0;
            }
            else if (ad1 > ad2) {
              u = 1 - ( value[1][i] - D[1] - s*(C[1]-D[1])
                - (1-t)*(A[1]-D[1]) ) / d1;
            }
            else {
              u = 1 - ( value[2][i] - D[2] - s*(C[2]-D[2])
                - (1-t)*(A[2]-D[2]) ) / d2;
            }
            if (evencube) {
              grid[0][i] = gx+s;
              grid[1][i] = gy+t;
              grid[2][i] = gz+u;
            }
            else {
              grid[0][i] = gx+1-s;
              grid[1][i] = gy+1-t;
              grid[2][i] = gz+1-u;
            }
            break;
          }
        }
        else if (tetnum==4) {
          tval1 = ( (G[1]-C[1])*(H[2]-G[2]) - (G[2]-C[2])*(H[1]-G[1]) )
                   *(value[0][i]-G[0])
                + ( (G[2]-C[2])*(H[0]-G[0]) - (G[0]-C[0])*(H[2]-G[2]) )
                   *(value[1][i]-G[1])
                + ( (G[0]-C[0])*(H[1]-G[1]) - (G[1]-C[1])*(H[0]-G[0]) )
                   *(value[2][i]-G[2]);
          tval2 = ( (G[1]-F[1])*(C[2]-G[2]) - (G[2]-F[2])*(C[1]-G[1]) )
                   *(value[0][i]-G[0])
                + ( (G[2]-F[2])*(C[0]-G[0]) - (G[0]-F[0])*(C[2]-G[2]) )
                   *(value[1][i]-G[1])
                + ( (G[0]-F[0])*(C[1]-G[1]) - (G[1]-F[1])*(C[0]-G[0]) )
                   *(value[2][i]-G[2]);
          tval3 = ( (G[1]-H[1])*(F[2]-G[2]) - (G[2]-H[2])*(F[1]-G[1]) )
                   *(value[0][i]-G[0])
                + ( (G[2]-H[2])*(F[0]-G[0]) - (G[0]-H[0])*(F[2]-G[2]) )
                   *(value[1][i]-G[1])
                + ( (G[0]-H[0])*(F[1]-G[1]) - (G[1]-H[1])*(F[0]-G[0]) )
                   *(value[2][i]-G[2]);
          test1 = (tval1 == 0) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (tval2 == 0) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (tval3 == 0) || ((tval3 > 0) == (!evencube)^Pos);

          // if a test failed go to a new box
          if (!test1 &&  evencube) gy++; // DOWN
          if (!test1 && !evencube) gy--; // UP
          if (!test2 &&  evencube) gx++; // RIGHT
          if (!test2 && !evencube) gx--; // LEFT
          if (!test3 &&  evencube) gz--; // BACK
          if (!test3 && !evencube) gz++; // FORWARD
          tetnum = 5;
          // Snap coordinates back onto grid in case they fell off
          if (gx < 0) gx = 0;
          if (gy < 0) gy = 0;
          if (gz < 0) gz = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy > LengthY-2) gy = LengthY-2;
          if (gz > LengthZ-2) gz = LengthZ-2;

          // Detect if the point is off the grid entirely
          if ( (gx == ogx) && (gy == ogy) && (gz == ogz)
            && (!test1 || !test2 || !test3) && !offgrid ) {
            offgrid = true;
            continue;
          }

          // If all tests pass then this is the correct tetrahedron
          if (  ( (gx == ogx) && (gy == ogy) && (gz == ogz) )
                || offgrid) {
            // solve point
            for (int j=0; j<3; j++) {
              M[j] = (H[j]-G[j])*(C[(j+1)%3]-G[(j+1)%3])
                   - (H[(j+1)%3]-G[(j+1)%3])*(C[j]-G[j]);
              N[j] = (F[j]-G[j])*(C[(j+1)%3]-G[(j+1)%3])
                   - (F[(j+1)%3]-G[(j+1)%3])*(C[j]-G[j]);
              O[j] = (H[(j+1)%3]-G[(j+1)%3])*(C[(j+2)%3]-G[(j+2)%3])
                   - (H[(j+2)%3]-G[(j+2)%3])*(C[(j+1)%3]-G[(j+1)%3]);
              P[j] = (F[(j+1)%3]-G[(j+1)%3])*(C[(j+2)%3]-G[(j+2)%3])
                   - (F[(j+2)%3]-G[(j+2)%3])*(C[(j+1)%3]-G[(j+1)%3]);
              X[j] = value[(j+2)%3][i]*(C[(j+1)%3]-G[(j+1)%3])
                   - value[(j+1)%3][i]*(C[(j+2)%3]-G[(j+2)%3])
                   + G[(j+1)%3]*C[(j+2)%3] - G[(j+2)%3]*C[(j+1)%3];
              Y[j] = value[j][i]*(C[(j+1)%3]-G[(j+1)%3])
                   - value[(j+1)%3][i]*(C[j]-G[j])
                   + G[(j+1)%3]*C[j] - G[j]*C[(j+1)%3];
            }
            double s, t, u;
            // these if statements handle skewed grids
            double d0 = M[0]*P[0] - N[0]*O[0];
            double d1 = M[1]*P[1] - N[1]*O[1];
            double d2 = M[2]*P[2] - N[2]*O[2];
            double ad0 = Math.abs(d0);
            double ad1 = Math.abs(d1);
            double ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              s = 1 - (N[0]*X[0] + P[0]*Y[0])/d0;
              t = 1 + (M[0]*X[0] + O[0]*Y[0])/d0;
            }
            else if (ad1 > ad2) {
              s = 1 - (N[1]*X[1] + P[1]*Y[1])/d1;
              t = 1 + (M[1]*X[1] + O[1]*Y[1])/d1;
            }
            else {
              s = 1 - (N[2]*X[2] + P[2]*Y[2])/d2;
              t = 1 + (M[2]*X[2] + O[2]*Y[2])/d2;
            }
            d0 = C[0]-G[0];
            d1 = C[1]-G[1];
            d2 = C[2]-G[2];
            ad0 = Math.abs(d0);
            ad1 = Math.abs(d1);
            ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              u = ( value[0][i] - G[0] - (1-s)*(H[0]-G[0])
                - (1-t)*(F[0]-G[0]) ) / d0;
            }
            else if (ad1 > ad2) {
              u = ( value[1][i] - G[1] - (1-s)*(H[1]-G[1])
                - (1-t)*(F[1]-G[1]) ) / d1;
            }
            else {
              u = ( value[2][i] - G[2] - (1-s)*(H[2]-G[2])
                - (1-t)*(F[2]-G[2]) ) / d2;
            }
            if (evencube) {
              grid[0][i] = gx+s;
              grid[1][i] = gy+t;
              grid[2][i] = gz+u;
            }
            else {
              grid[0][i] = gx+1-s;
              grid[1][i] = gy+1-t;
              grid[2][i] = gz+1-u;
            }
            break;
          }
        }
        else {    // tetnum==5
          tval1 = ( (F[1]-H[1])*(A[2]-F[2]) - (F[2]-H[2])*(A[1]-F[1]) )
                   *(value[0][i]-F[0])
                + ( (F[2]-H[2])*(A[0]-F[0]) - (F[0]-H[0])*(A[2]-F[2]) )
                   *(value[1][i]-F[1])
                + ( (F[0]-H[0])*(A[1]-F[1]) - (F[1]-H[1])*(A[0]-F[0]) )
                   *(value[2][i]-F[2]);
          tval2 = ( (C[1]-F[1])*(A[2]-C[2]) - (C[2]-F[2])*(A[1]-C[1]) )
                   *(value[0][i]-C[0])
                + ( (C[2]-F[2])*(A[0]-C[0]) - (C[0]-F[0])*(A[2]-C[2]) )
                   *(value[1][i]-C[1])
                + ( (C[0]-F[0])*(A[1]-C[1]) - (C[1]-F[1])*(A[0]-C[0]) )
                   *(value[2][i]-C[2]);
          tval3 = ( (C[1]-A[1])*(H[2]-C[2]) - (C[2]-A[2])*(H[1]-C[1]) )
                   *(value[0][i]-C[0])
                + ( (C[2]-A[2])*(H[0]-C[0]) - (C[0]-A[0])*(H[2]-C[2]) )
                   *(value[1][i]-C[1])
                + ( (C[0]-A[0])*(H[1]-C[1]) - (C[1]-A[1])*(H[0]-C[0]) )
                   *(value[2][i]-C[2]);
          tval4 = ( (F[1]-C[1])*(H[2]-F[2]) - (F[2]-C[2])*(H[1]-F[1]) )
                   *(value[0][i]-F[0])
                + ( (F[2]-C[2])*(H[0]-F[0]) - (F[0]-C[0])*(H[2]-F[2]) )
                   *(value[1][i]-F[1])
                + ( (F[0]-C[0])*(H[1]-F[1]) - (F[1]-C[1])*(H[0]-F[0]) )
                   *(value[2][i]-F[2]);
          test1 = (tval1 == 0) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (tval2 == 0) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (tval3 == 0) || ((tval3 > 0) == (!evencube)^Pos);
          test4 = (tval4 == 0) || ((tval4 > 0) == (!evencube)^Pos);

          // if a test failed go to a new tetrahedron
          if (!test1 && test2 && test3 && test4) tetnum = 1;
          if (test1 && !test2 && test3 && test4) tetnum = 2;
          if (test1 && test2 && !test3 && test4) tetnum = 3;
          if (test1 && test2 && test3 && !test4) tetnum = 4;
          if ( (!test1 && !test2 && evencube)
            || (!test3 && !test4 && !evencube) ) gy--; // GO UP
          if ( (!test1 && !test3 && evencube)
            || (!test2 && !test4 && !evencube) ) gx--; // GO LEFT
          if ( (!test1 && !test4 && evencube)
            || (!test2 && !test3 && !evencube) ) gz--; // GO BACK
          if ( (!test2 && !test3 && evencube)
            || (!test1 && !test4 && !evencube) ) gz++; // GO FORWARD
          if ( (!test2 && !test4 && evencube)
            || (!test1 && !test3 && !evencube) ) gx++; // GO RIGHT
          if ( (!test3 && !test4 && evencube)
            || (!test1 && !test2 && !evencube) ) gy++; // GO DOWN

          // Snap coordinates back onto grid in case they fell off
          if (gx < 0) gx = 0;
          if (gy < 0) gy = 0;
          if (gz < 0) gz = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy > LengthY-2) gy = LengthY-2;
          if (gz > LengthZ-2) gz = LengthZ-2;

          // Detect if the point is off the grid entirely
          if (  ( (gx == ogx) && (gy == ogy) && (gz == ogz)
               && (!test1 || !test2 || !test3 || !test4)
               && (tetnum == 5)) || offgrid) {
            offgrid = true;
            boolean OX, OY, OZ, MX, MY, MZ, LX, LY, LZ;
            OX = OY = OZ = MX = MY = MZ = LX = LY = LZ = false;
            if (gx == 0) OX = true;
            if (gy == 0) OY = true;
            if (gz == 0) OZ = true;
            if (gx == LengthX-2) LX = true;
            if (gy == LengthY-2) LY = true;
            if (gz == LengthZ-2) LZ = true;
            if (!OX && !LX) MX = true;
            if (!OY && !LY) MY = true;
            if (!OZ && !LZ) MZ = true;
            test1 = test2 = test3 = test4 = false;
            // 26 cases
            if (evencube) {
              if (!LX && !LY && !LZ) tetnum = 1;
              else if ( (LX && OY && MZ) || (MX && OY && LZ)
                     || (LX && MY && LZ) || (LX && OY && LZ)
                     || (MX && MY && LZ) || (LX && MY && MZ) ) tetnum = 2;
              else if ( (OX && LY && MZ) || (OX && MY && LZ)
                     || (MX && LY && LZ) || (OX && LY && LZ)
                                         || (MX && LY && MZ) ) tetnum = 3;
              else if ( (MX && LY && OZ) || (LX && MY && OZ)
                     || (LX && LY && MZ) || (LX && LY && OZ) ) tetnum = 4;
            }
            else {
              if (!OX && !OY && !OZ) tetnum = 1;
              else if ( (OX && MY && OZ) || (MX && LY && OZ)
                     || (OX && LY && MZ) || (OX && LY && OZ)
                     || (MX && MY && OZ) || (OX && MY && MZ) ) tetnum = 2;
              else if ( (LX && MY && OZ) || (MX && OY && OZ)
                     || (LX && OY && MZ) || (LX && OY && OZ)
                                         || (MX && OY && MZ) ) tetnum = 3;
              else if ( (OX && OY && MZ) || (OX && MY && OZ)
                     || (MX && OY && LZ) || (OX && OY && LZ) ) tetnum = 4;
            }
          }

          // If all tests pass then this is the correct tetrahedron
          if ( (gx == ogx) && (gy == ogy) && (gz == ogz) && (tetnum == 5) ) {
            // solve point

            for (int j=0; j<3; j++) {
              Q[j] = (H[j] + F[j] + A[j] - C[j])/2;
            }

            for (int j=0; j<3; j++) {
              M[j] = (F[j]-Q[j])*(A[(j+1)%3]-Q[(j+1)%3])
                   - (F[(j+1)%3]-Q[(j+1)%3])*(A[j]-Q[j]);
              N[j] = (H[j]-Q[j])*(A[(j+1)%3]-Q[(j+1)%3])
                   - (H[(j+1)%3]-Q[(j+1)%3])*(A[j]-Q[j]);
              O[j] = (F[(j+1)%3]-Q[(j+1)%3])*(A[(j+2)%3]-Q[(j+2)%3])
                   - (F[(j+2)%3]-Q[(j+2)%3])*(A[(j+1)%3]-Q[(j+1)%3]);
              P[j] = (H[(j+1)%3]-Q[(j+1)%3])*(A[(j+2)%3]-Q[(j+2)%3])
                   - (H[(j+2)%3]-Q[(j+2)%3])*(A[(j+1)%3]-Q[(j+1)%3]);
              X[j] = value[(j+2)%3][i]*(A[(j+1)%3]-Q[(j+1)%3])
                   - value[(j+1)%3][i]*(A[(j+2)%3]-Q[(j+2)%3])
                   + Q[(j+1)%3]*A[(j+2)%3] - Q[(j+2)%3]*A[(j+1)%3];
              Y[j] = value[j][i]*(A[(j+1)%3]-Q[(j+1)%3])
                   - value[(j+1)%3][i]*(A[j]-Q[j])
                   + Q[(j+1)%3]*A[j] - Q[j]*A[(j+1)%3];
            }
            double s, t, u;
            // these if statements handle skewed grids
            double d0 = M[0]*P[0] - N[0]*O[0];
            double d1 = M[1]*P[1] - N[1]*O[1];
            double d2 = M[2]*P[2] - N[2]*O[2];
            double ad0 = Math.abs(d0);
            double ad1 = Math.abs(d1);
            double ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              s = (N[0]*X[0] + P[0]*Y[0])/d0;
              t = -(M[0]*X[0] + O[0]*Y[0])/d0;
            }
            else if (ad1 > ad2) {
              s = (N[1]*X[1] + P[1]*Y[1])/d1;
              t = -(M[1]*X[1] + O[1]*Y[1])/d1;
            }
            else {
              s = (N[2]*X[2] + P[2]*Y[2])/d2;
              t = -(M[2]*X[2] + O[2]*Y[2])/d2;
            }
            d0 = A[0]-Q[0];
            d1 = A[1]-Q[1];
            d2 = A[2]-Q[2];
            ad0 = Math.abs(d0);
            ad1 = Math.abs(d1);
            ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              u = ( value[0][i] - Q[0] - s*(F[0]-Q[0])
                - t*(H[0]-Q[0]) ) / d0;
            }
            else if (ad1 > ad2) {
              u = ( value[1][i] - Q[1] - s*(F[1]-Q[1])
                - t*(H[1]-Q[1]) ) / d1;
            }
            else {
              u = ( value[2][i] - Q[2] - s*(F[2]-Q[2])
                - t*(H[2]-Q[2]) ) / d2;
            }
            if (evencube) {
              grid[0][i] = gx+s;
              grid[1][i] = gy+t;
              grid[2][i] = gz+u;
            }
            else {
              grid[0][i] = gx+1-s;
              grid[1][i] = gy+1-t;
              grid[2][i] = gz+1-u;
            }
            break;
          }
        }
      }
      // allow estimations up to 0.5 boxes outside of defined samples
      if ( (grid[0][i] <= -0.5) || (grid[0][i] >= LengthX-0.5)
        || (grid[1][i] <= -0.5) || (grid[1][i] >= LengthY-0.5)
        || (grid[2][i] <= -0.5) || (grid[2][i] >= LengthZ-0.5) ) {
        grid[0][i] = grid[1][i] = grid[2][i] = Double.NaN;
      }
    }
    return grid;
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void doubleToInterp(double[][] value, int[][] indices,
    double[][] weights) throws VisADException
  {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded3DDoubleSet.doubleToInterp: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length; // number of values
    if (indices.length != length) {
      throw new SetException("Gridded3DDoubleSet.doubleToInterp: indices length " +
                             indices.length +
                             " doesn't match value[0] length " +
                             value[0].length);
    }
    if (weights.length != length) {
      throw new SetException("Gridded3DDoubleSet.doubleToInterp: weights length " +
                             weights.length +
                             " doesn't match value[0] length " +
                             value[0].length);
    }
    // convert value array to grid coord array
    double[][] grid = doubleToGrid(value);

    int i, j, k; // loop indices
    int lis; // temporary length of is & cs
    int length_is; // final length of is & cs, varies by i
    int isoff; // offset along one grid dimension
    double a, b; // weights along one grid dimension; a + b = 1.0
    int[] is; // array of indices, becomes part of indices
    double[] cs; // array of coefficients, become part of weights

    int base; // base index, as would be returned by valueToIndex
    int[] l = new int[ManifoldDimension]; // integer 'factors' of base
    // fractions with l; -0.5 <= c <= 0.5
    double[] c = new double[ManifoldDimension];

    // array of index offsets by grid dimension
    int[] off = new int[ManifoldDimension];
    off[0] = 1;
    for (j=1; j<ManifoldDimension; j++) off[j] = off[j-1] * Lengths[j-1];

    for (i=0; i<length; i++) {
      // compute length_is, base, l & c
      length_is = 1;
      if (Double.isNaN(grid[ManifoldDimension-1][i])) {
        base = -1;
      }
      else {
        l[ManifoldDimension-1] = (int) (grid[ManifoldDimension-1][i] + 0.5);
        // WLH 23 Dec 99
        if (l[ManifoldDimension-1] == Lengths[ManifoldDimension-1]) {
          l[ManifoldDimension-1]--;
        }
        c[ManifoldDimension-1] = grid[ManifoldDimension-1][i] -
                                 ((double) l[ManifoldDimension-1]);
        if (!((l[ManifoldDimension-1] == 0 && c[ManifoldDimension-1] <= 0.0) ||
              (l[ManifoldDimension-1] == Lengths[ManifoldDimension-1] - 1 &&
               c[ManifoldDimension-1] >= 0.0))) {
          // only interp along ManifoldDimension-1
          // if between two valid grid coords
          length_is *= 2;
        }
        base = l[ManifoldDimension-1];
      }
      for (j=ManifoldDimension-2; j>=0 && base>=0; j--) {
        if (Double.isNaN(grid[j][i])) {
          base = -1;
        }
        else {
          l[j] = (int) (grid[j][i] + 0.5);
          if (l[j] == Lengths[j]) l[j]--; // WLH 23 Dec 99
          c[j] = grid[j][i] - ((double) l[j]);
          if (!((l[j] == 0 && c[j] <= 0.0) ||
                (l[j] == Lengths[j] - 1 && c[j] >= 0.0))) {
            // only interp along dimension j if between two valid grid coords
            length_is *= 2;
          }
          base = l[j] + Lengths[j] * base;
        }
      }

      if (base < 0) {
        // value is out of grid so return null
        is = null;
        cs = null;
      }
      else {
        // create is & cs of proper length, and init first element
        is = new int[length_is];
        cs = new double[length_is];
        is[0] = base;
        cs[0] = 1.0f;
        lis = 1;

        for (j=0; j<ManifoldDimension; j++) {
          if (!((l[j] == 0 && c[j] <= 0.0) ||
                (l[j] == Lengths[j] - 1 && c[j] >= 0.0))) {
            // only interp along dimension j if between two valid grid coords
            if (c[j] >= 0.0) {
              // grid coord above base
              isoff = off[j];
              a = 1.0f - c[j];
              b = c[j];
            }
            else {
              // grid coord below base
              isoff = -off[j];
              a = 1.0f + c[j];
              b = -c[j];
            }
            // double is & cs; adjust new offsets; split weights
            for (k=0; k<lis; k++) {
              is[k+lis] = is[k] + isoff;
              cs[k+lis] = cs[k] * b;
              cs[k] *= a;
            }
            lis *= 2;
          }
        }
      }
      indices[i] = is;
      weights[i] = cs;
    }
  }


  // Miscellaneous Set methods that must be overridden
  // (this code is duplicated throughout all *DoubleSet classes)

  void init_doubles(double[][] samples, boolean copy)
       throws VisADException {
    if (samples.length != DomainDimension) {
      throw new SetException("Gridded3DDoubleSet.init_doubles: samples dimension " +
                             samples.length +
                             " not equal to Domain dimension " +
                             DomainDimension);
    }
    if (Length == 0) {
      // Length set in init_lengths, but not called for IrregularSet
      Length = samples[0].length;
    }
    else {
      if (Length != samples[0].length) {
        throw new SetException("Gridded3DDoubleSet.init_doubles: " +
                               "samples[0] length " + samples[0].length +
                               " doesn't match expected length " + Length);
      }
    }
    // MEM
    if (copy) {
      Samples = new double[DomainDimension][Length];
    }
    else {
      Samples = samples;
    }
    for (int j=0; j<DomainDimension; j++) {
      if (samples[j].length != Length) {
        throw new SetException("Gridded3DDoubleSet.init_doubles: " +
                               "samples[" + j + "] length " +
                               samples[0].length +
                               " doesn't match expected length " + Length);
      }
      double[] samplesJ = samples[j];
      double[] SamplesJ = Samples[j];
      if (copy) {
        System.arraycopy(samplesJ, 0, SamplesJ, 0, Length);
      }
      Low[j] = Double.POSITIVE_INFINITY;
      Hi[j] = Double.NEGATIVE_INFINITY;
      double sum = 0.0f;
      for (int i=0; i<Length; i++) {
        if (SamplesJ[i] == SamplesJ[i] && !Double.isInfinite(SamplesJ[i])) {
          if (SamplesJ[i] < Low[j]) Low[j] = SamplesJ[i];
          if (SamplesJ[i] > Hi[j]) Hi[j] = SamplesJ[i];
        }
        else {
          SamplesJ[i] = Double.NaN;
        }
        sum += SamplesJ[i];
      }
      if (SetErrors[j] != null ) {
        SetErrors[j] =
          new ErrorEstimate(SetErrors[j].getErrorValue(), sum / Length,
                            Length, SetErrors[j].getUnit());
      }
      super.Low[j] = (float) Low[j];
      super.Hi[j] = (float) Hi[j];
    }
  }

  public void cram_missing(boolean[] range_select) {
    int n = Math.min(range_select.length, Samples[0].length);
    for (int i=0; i<n; i++) {
      if (!range_select[i]) Samples[0][i] = Double.NaN;
    }
  }

  public boolean isMissing() {
    return (Samples == null);
  }

  public boolean equals(Object set) {
    if (!(set instanceof Gridded3DDoubleSet) || set == null) return false;
    if (this == set) return true;
    if (testNotEqualsCache((Set) set)) return false;
    if (testEqualsCache((Set) set)) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    try {
      int i, j;
      if (DomainDimension != ((Gridded3DDoubleSet) set).getDimension() ||
          ManifoldDimension !=
            ((Gridded3DDoubleSet) set).getManifoldDimension() ||
          Length != ((Gridded3DDoubleSet) set).getLength()) return false;
      for (j=0; j<ManifoldDimension; j++) {
        if (Lengths[j] != ((Gridded3DDoubleSet) set).getLength(j)) {
          return false;
        }
      }
      // Sets are immutable, so no need for 'synchronized'
      double[][] samples = ((Gridded3DDoubleSet) set).getDoubles(false);
      if (Samples != null && samples != null) {
        for (j=0; j<DomainDimension; j++) {
          for (i=0; i<Length; i++) {
            if (Samples[j][i] != samples[j][i]) {
              addNotEqualsCache((Set) set);
              return false;
            }
          }
        }
      }
      else {
        double[][] this_samples = getDoubles(false);
        if (this_samples == null) {
          if (samples != null) {
            return false;
          }
        } else if (samples == null) {
          return false;
        } else {
          for (j=0; j<DomainDimension; j++) {
            for (i=0; i<Length; i++) {
              if (this_samples[j][i] != samples[j][i]) {
                addNotEqualsCache((Set) set);
                return false;
              }
            }
          }
        }
      }
      addEqualsCache((Set) set);
      return true;
    }
    catch (VisADException e) {
      return false;
    }
  }

  /**
   * Clones this instance.
   *
   * @return                    A clone of this instance.
   */
  public Object clone() {
    Gridded3DDoubleSet clone = (Gridded3DDoubleSet)super.clone();
    
    if (Samples != null) {
      /*
       * The Samples array is cloned because getDoubles(false) allows clients
       * to manipulate the array and the general clone() contract forbids
       * cross-clone contamination.
       */
      clone.Samples = (double[][])Samples.clone();
      for (int i = 0; i < Samples.length; i++)
        clone.Samples[i] = (double[])Samples[i].clone();
    }
    
    return clone;
  }

  public Object cloneButType(MathType type) throws VisADException {
    if (ManifoldDimension == 3) {
      return new Gridded3DDoubleSet(type, Samples, LengthX, LengthY, LengthZ,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
    else if (ManifoldDimension == 2) {
      return new Gridded3DDoubleSet(type, Samples, LengthX, LengthY,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
    else {
      return new Gridded3DDoubleSet(type, Samples, LengthX,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
  }
/* WLH 3 April 2003
  public Object cloneButType(MathType type) throws VisADException {
    return new Gridded3DDoubleSet(type, Samples, Length,
      DomainCoordinateSystem, SetUnits, SetErrors);
  }
*/
}

