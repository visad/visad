//
// VisADTriangleStripArray.java
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
   VisADTriangleStripArray stands in for
   j3d.TriangleStripArray and is Serializable.<P>
*/
public class VisADTriangleStripArray extends VisADGeometryArray {
  public int[] stripVertexCounts;

  public static VisADTriangleStripArray
                merge(VisADTriangleStripArray[] arrays)
         throws VisADException {
    if (arrays == null || arrays.length == 0) return null;
    VisADTriangleStripArray array = new VisADTriangleStripArray();
    merge(arrays, array);
    int n = arrays.length;
    int nstrips = 0;
    for (int i=0; i<n; i++) {
      if (arrays[i] != null) {
        nstrips += arrays[i].stripVertexCounts.length;
      }
    }
    if (nstrips <= 0) return null;
    int[] stripVertexCounts = new int[nstrips];
    nstrips = 0;
    for (int i=0; i<n; i++) {
      if (arrays[i] != null) {
        int incnstrips = arrays[i].stripVertexCounts.length;
        for (int j=0; j<incnstrips; j++) {
          stripVertexCounts[nstrips + j] = arrays[i].stripVertexCounts[j];
        }
        nstrips += incnstrips;
      }
    }
    array.stripVertexCounts = stripVertexCounts;
    return array;
  }

  private final static float LIMIT = 1.0f; // constant for TEST = 0
  private final static double ALPHA = 0.01; // constant for TEST = 1

  public VisADGeometryArray adjustSeam(DataRenderer renderer)
         throws VisADException {
    CoordinateSystem coord_sys = renderer.getDisplayCoordinateSystem();
    // DRM 19 March 2002
    //if (coord_sys == null || coord_sys instanceof SphericalCoordinateSystem) {
    //  return this;
    //}
    if (coord_sys == null || coord_sys instanceof SphericalCoordinateSystem ||
        coordinates == null) {
      return this;
    }

    int len = coordinates.length / 3;
    double[][] cs = new double[3][len];
    int j = 0;
    for (int i=0; i<len; i++) {
      cs[0][i] = coordinates[j++];
      cs[1][i] = coordinates[j++];
      cs[2][i] = coordinates[j++];
    }
    double[][] rs = coord_sys.fromReference(Set.copyDoubles(cs));
    boolean[] test = new boolean[len];
    int last_i;

    // for TEST 0
    double[] lengths = new double[len];
    for (int i=0; i<len;  i++) lengths[i] = 0.0f;
    double mean_length = 0.0;
    double var_length = 0.0;
    double max_length = 0.0;
    int num_length = 0;

    boolean any_split = false;

    // TEST 1
    if (len < 2) return this;
    double[][] bs = new double[3][len-1];
    double[][] ss = new double[3][len-1];
    double ALPHA1 = 1.0 + ALPHA;
    double ALPHA1m = 1.0 - ALPHA;
    for (int i=0; i<len-1; i++) {
      // BS = point ALPHA * opposite direction
      bs[0][i] = ALPHA1 * rs[0][i] - ALPHA * rs[0][i+1];
      bs[1][i] = ALPHA1 * rs[1][i] - ALPHA * rs[1][i+1];
      bs[2][i] = ALPHA1 * rs[2][i] - ALPHA * rs[2][i+1];
      // SS = point ALPHA * same direction
      ss[0][i] = ALPHA1 * rs[0][i+1] - ALPHA * rs[0][i];
      ss[1][i] = ALPHA1 * rs[1][i+1] - ALPHA * rs[1][i];
      ss[2][i] = ALPHA1 * rs[2][i+1] - ALPHA * rs[2][i];
    }
    double[][] ds = coord_sys.toReference(bs);
    double[][] es = coord_sys.toReference(ss);
    double IALPHA = 1.0 / ALPHA;

    last_i = 0; // start i for each vertex strip
    for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]-1; i++) {
        // A = original line segment
        double a0 = cs[0][i+1] - cs[0][i];
        double a1 = cs[1][i+1] - cs[1][i];
        double a2 = cs[2][i+1] - cs[2][i];
        // B = estimate of vector using ALPHA * opposite direction
        double b0 = IALPHA * (cs[0][i] - ds[0][i]);
        double b1 = IALPHA * (cs[1][i] - ds[1][i]);
        double b2 = IALPHA * (cs[2][i] - ds[2][i]);
        double aa = (a0 * a0 + a1 * a1 + a2 * a2);
        double aminusb =
          (b0 - a0) * (b0 - a0) +
          (b1 - a1) * (b1 - a1) +
          (b2 - a2) * (b2 - a2);
        double abratio = aminusb / aa;

        // C = estimate of vector using ALPHA * opposite direction
        double c0 = IALPHA * (cs[0][i+1] - es[0][i]);
        double c1 = IALPHA * (cs[1][i+1] - es[1][i]);
        double c2 = IALPHA * (cs[2][i+1] - es[2][i]);
        double aminusc =
          (c0 + a0) * (c0 + a0) +
          (c1 + a1) * (c1 + a1) +
          (c2 + a2) * (c2 + a2);
        double acratio = aminusc / aa;

        // true for bad segment
        test[i] = (0.01f < abratio) || (0.01f < acratio);
/*
        double bb = (b0 * b0 + b1 * b1 + b2 * b2);
        double ab = (b0 * a0 + b1 * a1 + b2 * a2);
        // b = A projected onto B, as a signed fraction of B
        double b = ab / bb;
        // c = (norm(A projected onto B) / norm(A)) ^ 2
        double c = (ab * ab) / (aa * bb);
        test[i] = !(0.5f < b && b < 2.0f && 0.5f < c);
*/
        // TEST 0
        double cd = (cs[0][i+1] - cs[0][i]) * (cs[0][i+1] - cs[0][i]) +
                   (cs[1][i+1] - cs[1][i]) * (cs[1][i+1] - cs[1][i]) +
                   (cs[2][i+1] - cs[2][i]) * (cs[2][i+1] - cs[2][i]);
        if (!test[i]) {
          lengths[i] = cd;
          num_length++;
          mean_length += lengths[i];
          var_length += lengths[i] * lengths[i];
          if (lengths[i] > max_length) max_length = lengths[i];
        }

      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3)
      last_i += stripVertexCounts[i_svc];
    } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)

    cs = null;
    rs = null;
    bs = null;
    ss = null;
    ds = null;
    es = null;

    // TEST 0
    if (num_length < 2) return this;
    mean_length = mean_length / num_length;
    var_length = //(float)
      Math.sqrt((var_length - mean_length * mean_length) / num_length);
    double limit_length = mean_length + LIMIT * var_length;

    if (max_length >= limit_length) {
      last_i = 0; // start i for each vertex strip
      for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
        for (int i=last_i; i<last_i+stripVertexCounts[i_svc]-1; i++) {

// WLH 20 June 2001
          // test[i] = test[i] || (lengths[i] > limit_length);

          if (test[i]) any_split = true;
        } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3)
        last_i += stripVertexCounts[i_svc];
      } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)
    }

// System.out.println("any_split = " + any_split);

    if (!any_split) {
      return this;
    }

    // most recent point
    float[] lastcoord = null;
    float[] lastno = null;
    byte[] lastcol = null;
    float[] lasttex = null;

    // point before most recent
    float[] earlycoord = null;
    float[] earlyno = null;
    byte[] earlycol = null;
    float[] earlytex = null;

    // this point
    float[] coord = null;
    float[] no = null;
    byte[] col = null;
    float[] tex = null;

    VisADTriangleStripArray array = new VisADTriangleStripArray();
    // worst case makes 3 times as many triangles
    int worst = 6; // WLH 30 Dec 99 - try new worst
    float[] coords = new float[worst * coordinates.length];
    float[] nos = null;
    if (normals != null) {
      nos = new float[worst * normals.length];
    }
    int color_length = 0;
    byte[] cols = null;
    if (colors != null) {
      color_length = 3;
      cols = new byte[worst * colors.length];
      if (colors.length != coordinates.length) color_length = 4;
    }
    float[] texs = null;
    if (texCoords != null) {
      texs = new float[worst * texCoords.length];
    }
    // worst case makes as many strips as there were points
    int[] svcs = new int[coordinates.length];
    int svc_index = 0;
    last_i = 0; // start i for each vertex strip

    int[] kmr = {0, 0, 0};
    int t = 0;
    j = 0;
    any_split = false;
    for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
      boolean this_split = false;
      int accum = 0; // strip counter
      j = (color_length * last_i / 3) - color_length;
      t = (2 * last_i / 3) - 2;
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3-3; i+=3) {
        j += color_length;
        t += 2;
        boolean last_split = this_split; // true if last edge was split
        if (test[i/3]) {
          this_split = true;
          any_split = true;

          // treat split as a break
          if (accum >= 3) {
            svcs[svc_index] = accum;
            svc_index++;
          }
          lastcoord = new float[]
            {coordinates[i+3], coordinates[i+4], coordinates[i+5]};
          if (normals != null) {
            lastno = new float[]
              {normals[i+3], normals[i+4], normals[i+5]};
          }
          if (color_length == 3) {
            lastcol = new byte[]
              {colors[j+3], colors[j+4], colors[j+5]};
          }
          else if (color_length == 4) {
            lastcol = new byte[]
              {colors[j+4], colors[j+5], colors[j+6], colors[j+7]};
          }
          if (texCoords != null) {
            lasttex = new float[]
              {texCoords[t+2], texCoords[t+3]};
          }
          accum = 1; // reset strip counter;
          continue;

        }
        else { // no split
          this_split = false;
        }

        if (!this_split) {
          if (accum == 0) {
            earlycoord = new float[]
              {coordinates[i], coordinates[i+1], coordinates[i+2]};
            lastcoord = new float[]
              {coordinates[i+3], coordinates[i+4], coordinates[i+5]};
            if (normals != null) {
              earlyno = new float[]
                {normals[i], normals[i+1], normals[i+2]};
              lastno = new float[]
                {normals[i+3], normals[i+4], normals[i+5]};
            }
            if (color_length == 3) {
              earlycol = new byte[]
                {colors[j], colors[j+1], colors[j+2]};
              lastcol = new byte[]
                {colors[j+3], colors[j+4], colors[j+5]};
            }
            else if (color_length == 4) {
              earlycol = new byte[]
                {colors[j], colors[j+1], colors[j+2], colors[j+3]};
              lastcol = new byte[]
                {colors[j+4], colors[j+5], colors[j+6], colors[j+7]};
            }
            if (texCoords != null) {
              earlytex = new float[]
                {texCoords[t], texCoords[t+1]};
              lasttex = new float[]
                {texCoords[t+2], texCoords[t+3]};
            }
            accum = 2;
            continue; // don't make any triangles yet, for accum = 0
          } // end if (accum == 0)
          else { // (!last_split || lon_axis < 0) && accum > 0 && !this_split
            // just add the next point (i+3)
            coord = new float[]
              {coordinates[i+3], coordinates[i+4], coordinates[i+5]};
            if (normals != null) {
              no = new float[]
                {normals[i+3], normals[i+4], normals[i+5]};
            }
            if (color_length == 3) {
              col = new byte[]
                {colors[j+3], colors[j+4], colors[j+5]};
            }
            else if (color_length == 4) {
              col = new byte[]
                {colors[j+4], colors[j+5], colors[j+6], colors[j+7]};
            }
            if (texCoords != null) {
              tex = new float[]
                {texCoords[t+2], texCoords[t+3]};
            }
            accum++;

            // WLH
            if (earlycoord == null) {
              earlycoord = new float[3];
              if (normals != null) {
                earlyno = new float[3];
              }
              if (color_length > 0) {
                earlycol = new byte[color_length];
              }
              if (texCoords != null) {
                earlytex = new float[2];
              }
            }

            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
          }
        } // end if no split
      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3-3; i+=3)
      if (accum >= 3) {
        svcs[svc_index] = accum;
        svc_index++;
      }
      last_i += stripVertexCounts[i_svc] * 3;
    } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)

    if (!any_split) {
      return this;
    }
    else {
      array.coordinates = new float[kmr[0]];
      System.arraycopy(coords, 0, array.coordinates, 0, kmr[0]);
      if (normals != null) {
        array.normals = new float[kmr[0]];
        System.arraycopy(nos, 0, array.normals, 0, kmr[0]);
      }
      if (colors != null) {
        array.colors = new byte[kmr[1]];
        System.arraycopy(cols, 0, array.colors, 0, kmr[1]);
      }
      if (texCoords != null) {
        array.texCoords = new float[kmr[2]];
        System.arraycopy(texs, 0, array.texCoords, 0, kmr[2]);
      }
      array.vertexCount = kmr[0] / 3;
      array.stripVertexCounts = new int[svc_index];
      System.arraycopy(svcs, 0, array.stripVertexCounts, 0, svc_index);
      return array;
    }
  }
/* */

  public VisADGeometryArray adjustLongitude(DataRenderer renderer)
         throws VisADException {
    float[] lons = getLongitudes(renderer);
    if (lons == null) return this;
    int[] axis = new int[1];
    float[] lon_coords = new float[2];
    float[] lon_range = getLongitudeRange(lons, axis, lon_coords);
    if (lon_range[0] != lon_range[0] ||
        lon_range[1] != lon_range[1]) return this;
    float bottom = lon_range[0];
    float top = lon_range[1];
    float low = bottom + 30.0f;
    float hi = top - 30.0f;
    int lon_axis = axis[0];
    float coord_bottom = lon_coords[0];
    float coord_top = lon_coords[1];

    // midpoint from this (if this_split)
    float[] midcoord_first = null;
    float[] midcoord_second = null;
    float[] midno = null;
    byte[] midcol = null;
    float[] midtex = null;

    // save midpoint from last (if last_split)
    float[] last_midcoord_first = null;
    float[] last_midcoord_second = null;
    float[] last_midno = null;
    byte[] last_midcol = null;
    float[] last_midtex = null;

    // midpoint between -3 and +3 (if this_split^last_split)
    float[] sillymidcoord_first = null;
    float[] sillymidcoord_second = null;
    float[] sillymidno = null;
    byte[] sillymidcol = null;
    float[] sillymidtex = null;

    // most recent point
    float[] lastcoord = null;
    float[] lastno = null;
    byte[] lastcol = null;
    float[] lasttex = null;

    // point before most recent
    float[] earlycoord = null;
    float[] earlyno = null;
    byte[] earlycol = null;
    float[] earlytex = null;

    // this point
    float[] coord = null;
    float[] no = null;
    byte[] col = null;
    float[] tex = null;


    VisADTriangleStripArray array = new VisADTriangleStripArray();
    // worst case makes 3 times as many triangles
    int worst = 6; // WLH 30 Dec 99 - try new worst
    float[] coords = new float[worst * coordinates.length];
    float[] nos = null;
    if (normals != null) {
      nos = new float[worst * normals.length];
    }
    int color_length = 0;
    byte[] cols = null;
    if (colors != null) {
      color_length = 3;
      cols = new byte[worst * colors.length];
      if (colors.length != coordinates.length) color_length = 4;
    }
    float[] texs = null;
    if (texCoords != null) {
      texs = new float[worst * texCoords.length];
    }
    // worst case makes as many strips as there were points
    int[] svcs = new int[coordinates.length];
    int svc_index = 0;
    int last_i = 0; // start i for each vertex strip

    int[] kmr = {0, 0, 0};
    int t = 0;
    int j = 0;
    boolean any_split = false;
    for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
      boolean this_split = false;
      int accum = 0; // strip counter
      j = (color_length * last_i / 3) - color_length;
      t = (2 * last_i / 3) - 2;
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3-3; i+=3) {
        j += color_length;
        t += 2;
        boolean last_split = this_split; // true if last edge was split
        if (last_split) {
          last_midcoord_first = midcoord_first;
          last_midcoord_second = midcoord_second;
          last_midno = midno;
          last_midcol = midcol;
          last_midtex = midtex;
        }
        int i3 = i / 3;
        if ((lons[i3] < low && hi < lons[i3 + 1]) ||
            (lons[i3 + 1] < low && hi < lons[i3])) {
/*
System.out.println("any_split " + lons[i3] + " " + lons[i3 + 1] + " " +
                   low + " " + hi + " " + bottom + " " + top);
*/
          this_split = true;
          any_split = true;
          if (lon_axis < 0) {
            // not enough info to interpolate, so treat split as a break
            if (accum >= 3) {
              svcs[svc_index] = accum;
              svc_index++;
            }
            lastcoord = new float[]
              {coordinates[i+3], coordinates[i+4], coordinates[i+5]};
            if (normals != null) {
              lastno = new float[]
                {normals[i+3], normals[i+4], normals[i+5]};
            }
            if (color_length == 3) {
              lastcol = new byte[]
                {colors[j+3], colors[j+4], colors[j+5]};
            }
            else if (color_length == 4) {
              lastcol = new byte[]
                {colors[j+4], colors[j+5], colors[j+6], colors[j+7]};
            }
            if (texCoords != null) {
              lasttex = new float[]
                {texCoords[t+2], texCoords[t+3]};
            }
            accum = 1; // reset strip counter;
            continue;
          }
          // lon_axis >= 0
          // split line by interpolation
          float a, b;
          float coord_first, coord_second;
          if (lons[i3] < low) {
            a = lons[i3] - bottom;
            b = top - lons[i3 + 1];
            coord_first = coord_bottom;
            coord_second = coord_top;
          }
          else {
            a = top - lons[i3];
            b = lons[i3 + 1] - bottom;
            coord_first = coord_top;
            coord_second = coord_bottom;
          }
          float alpha = b / (a + b);
          alpha = (alpha != alpha || alpha < 0.0f) ? 0.0f :
                    ((1.0f < alpha) ? 1.0f : alpha);
          float beta = 1.0f - alpha;

          midcoord_first = new float[]
            {alpha * coordinates[i] + beta * coordinates[i+3],
             alpha * coordinates[i+1] + beta * coordinates[i+4],
             alpha * coordinates[i+2] + beta * coordinates[i+5]};
          midcoord_second = new float[]
            {midcoord_first[0], midcoord_first[1], midcoord_first[2]};
          midcoord_first[lon_axis] = coord_first;
          midcoord_second[lon_axis] = coord_second;
          if (normals != null) {
            midno = new float[]
              {alpha * normals[i] + beta * normals[i+3],
               alpha * normals[i+1] + beta * normals[i+4],
               alpha * normals[i+2] + beta * normals[i+5]};
          }
          if (color_length == 3) {
            midcol = new byte[]
              {ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j]) +
                beta * ShadowType.byteToFloat(colors[j+3])),
               ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j+1]) +
                beta * ShadowType.byteToFloat(colors[j+4])),
               ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j+2]) +
                beta * ShadowType.byteToFloat(colors[j+5]))};
          }
          else if (color_length == 4) {
            midcol = new byte[]
              {ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j]) +
                beta * ShadowType.byteToFloat(colors[j+4])),
               ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j+1]) +
                beta * ShadowType.byteToFloat(colors[j+5])),
               ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j+2]) +
                beta * ShadowType.byteToFloat(colors[j+6])),
               ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j+3]) +
                beta * ShadowType.byteToFloat(colors[j+7]))};
          }
          if (texCoords != null) {
            midtex = new float[]
              {alpha * texCoords[t] + beta * texCoords[t+2],
               alpha * texCoords[t+1] + beta * texCoords[t+3]};
          }
        }
        else { // no split
          this_split = false;
        }

        if (accum > 0 && this_split != last_split && lon_axis >= 0) {
          // need to compute mid edge from -3 to +3

          // split line by interpolation
          float a, b;
          float coord_first, coord_second;
          if (lons[i3 - 1] < low) {
            a = lons[i3 - 1] - bottom;
            b = top - lons[i3 + 1];
            coord_first = coord_bottom;
            coord_second = coord_top;
          }
          else {
            a = top - lons[i3 - 1];
            b = lons[i3 + 1] - bottom;
            coord_first = coord_top;
            coord_second = coord_bottom;
          }
          float alpha = b / (a + b);
          alpha = (alpha != alpha || alpha < 0.0f) ? 0.0f :
                    ((1.0f < alpha) ? 1.0f : alpha);
          float beta = 1.0f - alpha;

          sillymidcoord_first = new float[]
            {alpha * coordinates[i-3] + beta * coordinates[i+3],
             alpha * coordinates[i-2] + beta * coordinates[i+4],
             alpha * coordinates[i-1] + beta * coordinates[i+5]};
          sillymidcoord_second = new float[]
            {sillymidcoord_first[0], sillymidcoord_first[1], sillymidcoord_first[2]};
          sillymidcoord_first[lon_axis] = coord_first;
          sillymidcoord_second[lon_axis] = coord_second;
          if (normals != null) {
            sillymidno = new float[]
              {alpha * normals[i-3] + beta * normals[i+3],
               alpha * normals[i-2] + beta * normals[i+4],
               alpha * normals[i-1] + beta * normals[i+5]};
          }
          if (color_length == 3) {
            sillymidcol = new byte[]
              {ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j-3]) +
                beta * ShadowType.byteToFloat(colors[j+3])),
               ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j-2]) +
                beta * ShadowType.byteToFloat(colors[j+4])),
               ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j-1]) +
                beta * ShadowType.byteToFloat(colors[j+5]))};
          }
          else if (color_length == 4) {
            sillymidcol = new byte[]
              {ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j-4]) +
                beta * ShadowType.byteToFloat(colors[j+4])),
               ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j-3]) +
                beta * ShadowType.byteToFloat(colors[j+5])),
               ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j-2]) +
                beta * ShadowType.byteToFloat(colors[j+6])),
               ShadowType.floatToByte(
                alpha * ShadowType.byteToFloat(colors[j-1]) +
                beta * ShadowType.byteToFloat(colors[j+7]))};
          }
          if (texCoords != null) {
            sillymidtex = new float[]
              {alpha * texCoords[t-2] + beta * texCoords[t+2],
               alpha * texCoords[t-1] + beta * texCoords[t+3]};
          }
        }

        if (this_split) {
          if (accum == 0) {
            earlycoord = new float[]
              {midcoord_second[0], midcoord_second[1], midcoord_second[2]};
            lastcoord = new float[]
              {coordinates[i+3], coordinates[i+4], coordinates[i+5]};
            if (normals != null) {
              earlyno = new float[]
                {midno[0], midno[1], midno[2]};
              lastno = new float[]
                {normals[i+3], normals[i+4], normals[i+5]};
            }
            if (color_length == 3) {
              earlycol = new byte[]
                {midcol[0], midcol[1], midcol[2]};
              lastcol = new byte[]
                {colors[j+3], colors[j+4], colors[j+5]};
            }
            else if (color_length == 4) {
              earlycol = new byte[]
                {midcol[0], midcol[1], midcol[2], midcol[3]};
              lastcol = new byte[]
                {colors[j+4], colors[j+5], colors[j+6], colors[j+7]};
            }
            if (texCoords != null) {
              earlytex = new float[]
                {midtex[0], midtex[1]};
              lasttex = new float[]
                {texCoords[t+2], texCoords[t+3]};
            }
            accum = 2;
            continue; // don't make any triangles yet, for accum = 0
          } // end if (accum == 0)
          else if (last_split) { // && this_split
            coord = midcoord_first;
            no = midno;
            col = midcol;
            tex = midtex;
            accum++;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
            // bind off
            svcs[svc_index] = accum;
            svc_index++;
            // accum = 0, but more to come

            // create a 2-triangle strip
            //   (last_mid_first, i-3, mid_second, i+3)
            earlycoord = last_midcoord_first;
            earlyno = last_midno;
            earlycol = last_midcol;
            earlytex = last_midtex;
            lastcoord = new float[]
              {coordinates[i-3], coordinates[i-2], coordinates[i-1]};
            if (normals != null) {
              lastno = new float[]
                {normals[i-3], normals[i-2], normals[i-1]};
            }
            if (color_length == 3) {
              lastcol = new byte[]
                {colors[j-3], colors[j-2], colors[j-1]};
            }
            else if (color_length == 4) {
              lastcol = new byte[]
                {colors[j-4], colors[j-3], colors[j-2], colors[j-1]};
            }
            if (texCoords != null) {
              lasttex = new float[]
                {texCoords[t-2], texCoords[t-1]};
            }
            coord = midcoord_second;
            no = midno;
            col = midcol;
            tex = midtex;
            accum = 3;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
            coord = new float[]
              {coordinates[i+3], coordinates[i+4], coordinates[i+5]};
            if (normals != null) {
              no = new float[]
                {normals[i+3], normals[i+4], normals[i+5]};
            }
            if (color_length == 3) {
              col = new byte[]
                {colors[j+3], colors[j+4], colors[j+5]};
            }
            else if (color_length == 4) {
              col = new byte[]
                {colors[j+4], colors[j+5], colors[j+6], colors[j+7]};
            }
            if (texCoords != null) {
              tex = new float[]
                {texCoords[t+2], texCoords[t+3]};
            }
            accum++;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
          }
          else { // !last_split && accum > 0 && this_split
            // add (mid_first, i-3, sillymid_first)
            coord = midcoord_first;
            no = midno;
            col = midcol;
            tex = midtex;
            accum++;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
            coord = new float[]
              {coordinates[i-3], coordinates[i-2], coordinates[i-1]};
            if (normals != null) {
              no = new float[]
                {normals[i-3], normals[i-2], normals[i-1]};
            }
            if (color_length == 3) {
              col = new byte[]
                {colors[j-3], colors[j-2], colors[j-1]};
            }
            else if (color_length == 4) {
              col = new byte[]
                {colors[j-4], colors[j-3], colors[j-2], colors[j-1]};
            }
            if (texCoords != null) {
              tex = new float[]
                {texCoords[t-2], texCoords[t-1]};
            }
            accum++;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
            coord = sillymidcoord_first;
            no = sillymidno;
            col = sillymidcol;
            tex = sillymidtex;
            accum++;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
            // (accum >= 3)
            svcs[svc_index] = accum;
            svc_index++;
            // accum = 0, but more to come

            // start a triangle strip
            //   (sillymid_second, mid_second, i+3)
            earlycoord = sillymidcoord_second;
            earlyno = sillymidno;
            earlycol = sillymidcol;
            earlytex = sillymidtex;
            lastcoord = midcoord_second;
            lastno = midno;
            lastcol = midcol;
            lasttex = midtex;
            coord = new float[]
              {coordinates[i+3], coordinates[i+4], coordinates[i+5]};
            if (normals != null) {
              no = new float[]
                {normals[i+3], normals[i+4], normals[i+5]};
            }
            if (color_length == 3) {
              col = new byte[]
                {colors[j+3], colors[j+4], colors[j+5]};
            }
            else if (color_length == 4) {
              col = new byte[]
                {colors[j+4], colors[j+5], colors[j+6], colors[j+7]};
            }
            if (texCoords != null) {
              tex = new float[]
                {texCoords[t+2], texCoords[t+3]};
            }
            accum = 3;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
          }
        }
        else { // !this_split
          if (accum == 0) {
            earlycoord = new float[]
              {coordinates[i], coordinates[i+1], coordinates[i+2]};
            lastcoord = new float[]
              {coordinates[i+3], coordinates[i+4], coordinates[i+5]};
            if (normals != null) {
              earlyno = new float[]
                {normals[i], normals[i+1], normals[i+2]};
              lastno = new float[]
                {normals[i+3], normals[i+4], normals[i+5]};
            }
            if (color_length == 3) {
              earlycol = new byte[]
                {colors[j], colors[j+1], colors[j+2]};
              lastcol = new byte[]
                {colors[j+3], colors[j+4], colors[j+5]};
            }
            else if (color_length == 4) {
              earlycol = new byte[]
                {colors[j], colors[j+1], colors[j+2], colors[j+3]};
              lastcol = new byte[]
                {colors[j+4], colors[j+5], colors[j+6], colors[j+7]};
            }
            if (texCoords != null) {
              earlytex = new float[]
                {texCoords[t], texCoords[t+1]};
              lasttex = new float[]
                {texCoords[t+2], texCoords[t+3]};
            }
            accum = 2;
            continue; // don't make any triangles yet, for accum = 0
          } // end if (accum == 0)
          else if (last_split && lon_axis >= 0) { // && !this_split
            // first, bind off
            if (accum >= 3) {
              svcs[svc_index] = accum;
              svc_index++;
            }
            // accum = 0, but more to come

            // create a lone triangle (i-3, last_mid_first, sillymid_first)
            earlycoord = new float[]
              {coordinates[i-3], coordinates[i-2], coordinates[i-1]};
            if (normals != null) {
              earlyno = new float[]
                {normals[i-3], normals[i-2], normals[i-1]};
            }
            if (color_length == 3) {
              earlycol = new byte[]
                {colors[j-3], colors[j-2], colors[j-1]};
            }
            else if (color_length == 4) {
              earlycol = new byte[]
                {colors[j-4], colors[j-3], colors[j-2], colors[j-1]};
            }
            if (texCoords != null) {
              earlytex = new float[]
                {texCoords[t-2], texCoords[t-1]};
            }
            lastcoord = last_midcoord_first;
            lastno = last_midno;
            lastcol = last_midcol;
            lasttex = last_midtex;
            coord = sillymidcoord_first;
            no = sillymidno;
            col = sillymidcol;
            tex = sillymidtex;
            accum = 3;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
            // bind off again
            svcs[svc_index] = accum;
            svc_index++;
            // accum = 0, but more to come

            // create a 2-triangle strip
            //   (last_mid_second, sillymid_second, i, i+3)
            earlycoord = last_midcoord_second;
            earlyno = last_midno;
            earlycol = last_midcol;
            earlytex = last_midtex;
            lastcoord = sillymidcoord_second;
            lastno = sillymidno;
            lastcol = sillymidcol;
            lasttex = sillymidtex;
            coord = new float[]
              {coordinates[i], coordinates[i+1], coordinates[i+2]};
            if (normals != null) {
              no = new float[]
                {normals[i], normals[i+1], normals[i+2]};
            }
            if (color_length == 3) {
              col = new byte[]
                {colors[j], colors[j+1], colors[j+2]};
            }
            else if (color_length == 4) {
              col = new byte[]
                {colors[j], colors[j+1], colors[j+2], colors[j+3]};
            }
            if (texCoords != null) {
              tex = new float[]
                {texCoords[t], texCoords[t+1]};
            }
            accum = 3;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
            coord = new float[]
              {coordinates[i+3], coordinates[i+4], coordinates[i+5]};
            if (normals != null) {
              no = new float[]
                {normals[i+3], normals[i+4], normals[i+5]};
            }
            if (color_length == 3) {
              col = new byte[]
                {colors[j+3], colors[j+4], colors[j+5]};
            }
            else if (color_length == 4) {
              col = new byte[]
                {colors[j+4], colors[j+5], colors[j+6], colors[j+7]};
            }
            if (texCoords != null) {
              tex = new float[]
                {texCoords[t+2], texCoords[t+3]};
            }
            accum++;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
          }
          else { // (!last_split || lon_axis < 0) && accum > 0 && !this_split
            // just add the next point (i+3)

            if (earlycoord == null) {
              earlycoord = new float[3];
              if (normals != null) {
                earlyno = new float[3];
              }
              if (color_length > 0) {
                earlycol = new byte[color_length];
              }
              if (texCoords != null) {
                earlytex = new float[2];
              }
            }

            coord = new float[]
              {coordinates[i+3], coordinates[i+4], coordinates[i+5]};
            if (normals != null) {
              no = new float[]
                {normals[i+3], normals[i+4], normals[i+5]};
            }
            if (color_length == 3) {
              col = new byte[]
                {colors[j+3], colors[j+4], colors[j+5]};
            }
            else if (color_length == 4) {
              col = new byte[]
                {colors[j+4], colors[j+5], colors[j+6], colors[j+7]};
            }
            if (texCoords != null) {
              tex = new float[]
                {texCoords[t+2], texCoords[t+3]};
            }
            accum++;
            nextPoint(accum, color_length, coords, nos, cols, texs,
                 coord, no, col, tex, lastcoord, lastno, lastcol,
                 lasttex, earlycoord, earlyno, earlycol, earlytex, kmr);
          }
        } // end if no split
      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3-3; i+=3)
      if (accum >= 3) {
        svcs[svc_index] = accum;
        svc_index++;
      }
      last_i += stripVertexCounts[i_svc] * 3;
    } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)

    if (!any_split) {
      return this;
    }
    else {
      array.coordinates = new float[kmr[0]];
      System.arraycopy(coords, 0, array.coordinates, 0, kmr[0]);
      if (normals != null) {
        array.normals = new float[kmr[0]];
        System.arraycopy(nos, 0, array.normals, 0, kmr[0]);
      }
      if (colors != null) {
        array.colors = new byte[kmr[1]];
        System.arraycopy(cols, 0, array.colors, 0, kmr[1]);
      }
      if (texCoords != null) {
        array.texCoords = new float[kmr[2]];
        System.arraycopy(texs, 0, array.texCoords, 0, kmr[2]);
      }
      array.vertexCount = kmr[0] / 3;
      array.stripVertexCounts = new int[svc_index];
      System.arraycopy(svcs, 0, array.stripVertexCounts, 0, svc_index);
      return array;
    }
  }

  private void nextPoint(int accum, int color_length, float[] coords,
                float[] nos, byte[] cols, float[] texs,
                float[] coord, float[] no, byte[] col, float[] tex,
                float[] lastcoord, float[] lastno, byte[] lastcol,
                float[] lasttex, float[] earlycoord, float[] earlyno,
                byte[] earlycol, float[] earlytex, int[] kmr) {
    if (accum == 2) {
      System.arraycopy(lastcoord, 0, earlycoord, 0, 3);
      System.arraycopy(coord, 0, lastcoord, 0, 3);
      if (normals != null) {
        System.arraycopy(lastno, 0, earlyno, 0, 3);
        System.arraycopy(no, 0, lastno, 0, 3);
      }
      if (colors != null) {
        System.arraycopy(lastcol, 0, earlycol, 0, color_length);
        System.arraycopy(col, 0, lastcol, 0, color_length);
      }
      if (texCoords != null) {
/* WLH 9 March 2000
        System.arraycopy(lasttex, 0, earlytex, 0, 3);
        System.arraycopy(tex, 0, lasttex, 0, 3);
*/
        System.arraycopy(lasttex, 0, earlytex, 0, 2);
        System.arraycopy(tex, 0, lasttex, 0, 2);
      }
      return;
    }
    else if (accum == 3) {
      // put early point on new strip
      coords[kmr[0]] = earlycoord[0];
      coords[kmr[0]+1] = earlycoord[1];
      coords[kmr[0]+2] = earlycoord[2];
      if (normals != null) {
        nos[kmr[0]] = earlyno[0];
        nos[kmr[0]+1] = earlyno[1];
        nos[kmr[0]+2] = earlyno[2];
      }
      kmr[0] += 3;
      if (colors != null) {
        cols[kmr[1]] = earlycol[0];
        cols[kmr[1]+1] = earlycol[1];
        cols[kmr[1]+2] = earlycol[2];
        kmr[1] += 3;
        if (color_length == 4) {
          cols[kmr[1]++] = earlycol[3];
        }
      }
      if (texCoords != null) {
        texs[kmr[2]] = earlytex[0];
        texs[kmr[2]+1] = earlytex[1];
        kmr[2] += 2;
      }

      // put last point on new strip
      coords[kmr[0]] = lastcoord[0];
      coords[kmr[0]+1] = lastcoord[1];
      coords[kmr[0]+2] = lastcoord[2];
      if (normals != null) {
        nos[kmr[0]] = lastno[0];
        nos[kmr[0]+1] = lastno[1];
        nos[kmr[0]+2] = lastno[2];
      }
      kmr[0] += 3;
      if (colors != null) {
        cols[kmr[1]] = lastcol[0];
        cols[kmr[1]+1] = lastcol[1];
        cols[kmr[1]+2] = lastcol[2];
        kmr[1] += 3;
        if (color_length == 4) {
          cols[kmr[1]++] = lastcol[3];
        }
      }
      if (texCoords != null) {
        texs[kmr[2]] = lasttex[0];
        texs[kmr[2]+1] = lasttex[1];
        kmr[2] += 2;
      }
    } // end if (accum == 3)

    // put this point on new strip
    coords[kmr[0]] = coord[0];
    coords[kmr[0]+1] = coord[1];
    coords[kmr[0]+2] = coord[2];
    if (normals != null) {
      nos[kmr[0]] = no[0];
      nos[kmr[0]+1] = no[1];
      nos[kmr[0]+2] = no[2];
    }
    kmr[0] += 3;
    if (colors != null) {
      cols[kmr[1]] = col[0];
      cols[kmr[1]+1] = col[1];
      cols[kmr[1]+2] = col[2];
      kmr[1] += 3;
      if (color_length == 4) {
        cols[kmr[1]++] = col[3];
      }
    }
    if (texCoords != null) {
      texs[kmr[2]] = tex[0];
      texs[kmr[2]+1] = tex[1];
      kmr[2] += 2;
    }
  }

  public VisADGeometryArray removeMissing() {
    VisADTriangleStripArray array = new VisADTriangleStripArray();
    float[] coords = new float[coordinates.length];
    float[] nos = null;
    if (normals != null) {
      nos = new float[normals.length];
    }
    int color_length = 3;
    byte[] cols = null;
    if (colors != null) {
      cols = new byte[colors.length];
      if (colors.length != coordinates.length) color_length = 4;
    }
    float[] texs = null;
    if (texCoords != null) {
      texs = new float[texCoords.length];
    }
    int[] svcs = new int[coordinates.length / 4];
    int svc_index = 0;
    int last_i = 0; // start i for each vertex strip

    int k = 0;
    int m = 0;
    int t = 0;
    int r = 0;
    int j = 0;
    boolean any_missing = false;
    for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
      int accum = 0; // strip counter
      j = color_length * last_i / 3;
      t = 2 * last_i / 3;
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3) {

        if (coordinates[i] == coordinates[i] &&
            coordinates[i+1] == coordinates[i+1] &&
            coordinates[i+2] == coordinates[i+2]) {
          accum++;
          if (accum >= 3) {
            int iml = i;
            int jml = j;
            int tml = t;
            if (accum == 3) {
              iml = i - 6;
              jml = j - 2 * color_length;
              tml = t - 4;
            }
            int jm = jml;
            int tm = tml;
            for (int im=iml; im<=i; im+=3) {
              coords[k] = coordinates[im];
              coords[k+1] = coordinates[im+1];
              coords[k+2] = coordinates[im+2];
              if (normals != null) {
                nos[k] = normals[im];
                nos[k+1] = normals[im+1];
                nos[k+2] = normals[im+2];
              }
              if (colors != null) {
                cols[m] = colors[jm];
                cols[m+1] = colors[jm+1];
                cols[m+2] = colors[jm+2];
                m += 3;
                if (color_length == 4) {
                  cols[m++] = colors[jm+3];
                }
              }
              if (texCoords != null) {
                texs[r] = texCoords[tm];
                texs[r+1] = texCoords[tm+1];
                r += 2;
              }
              k += 3;
              jm += color_length;
              tm += 2;
            } // end for (im=iml; im<=i; im+=3)
          } // end if (accum >= 3)
        }
        else { // missing coordinates values
          any_missing = true;
          if (accum >= 3) {
            svcs[svc_index] = accum;
            svc_index++;
          }
          accum = 0; // reset strip counter;
        }
        j += color_length;
        t += 2;
      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3)
      if (accum >= 3) {
        svcs[svc_index] = accum;
        svc_index++;
      }
      last_i += stripVertexCounts[i_svc] * 3;
    } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)

    if (!any_missing) {
      return this;
    }
    else {
      array.coordinates = new float[k];
      System.arraycopy(coords, 0, array.coordinates, 0, k);
      if (normals != null) {
        array.normals = new float[k];
        System.arraycopy(nos, 0, array.normals, 0, k);
      }
      if (colors != null) {
        array.colors = new byte[m];
        System.arraycopy(cols, 0, array.colors, 0, m);
      }
      if (texCoords != null) {
        array.texCoords = new float[r];
        System.arraycopy(texs, 0, array.texCoords, 0, r);
      }
      array.vertexCount = k / 3;
      array.stripVertexCounts = new int[svc_index];
      System.arraycopy(svcs, 0, array.stripVertexCounts, 0, svc_index);
      return array;
    }
  }

  public String toString() {
/*
    String string = "VisADTriangleStripArray\n" + super.toString() +
                    "\n indexCount = " + indexCount;
*/
    String string = "VisADTriangleStripArray ";
    string = string + "\n stripVertexCounts = ";
    for (int i=0; i<stripVertexCounts.length; i++) {
      string = string + stripVertexCounts[i] + " ";
    }
    return string;
  }

  public Object clone() {
    VisADTriangleStripArray array =
      new VisADTriangleStripArray();
    copy(array);
    if (stripVertexCounts != null) {
      array.stripVertexCounts = new int[stripVertexCounts.length];
      System.arraycopy(stripVertexCounts, 0, array.stripVertexCounts, 0,
                       stripVertexCounts.length);
    }
    return array;
  }

}

/*
documentation for VisADTriangleStripArray.adjustLongitude()
and VisADIndexedTriangleStripArray.adjustLongitude()

given a sequence of points (i-1, i, i+1) along a triangle strip:
and the three possible split points (silly = last .xor. this)

                    i
                    /\
                   /  \
                  /    \
                 /      \
          2nd   /        \   1st
    last mid   /          \   mid (this)
          1st /            \ 2nd
             /              \
            /                \
           /                  \
          i-1    1st  2nd     i+1
                silly mid

longitude
split?
l   t
a   h
s   i
t   s

                    1
No  No              /\
                   /  \
                  /    \
                 /      \
                /        \
               /          \
              /            \
             /              \
            /                \
           /                  \
          0                    2


                    1
No  Yes             /\
                   /  \
                  /    \
                 /      \
                /        \ 2
               /          \
              /            \ 6
             /              \
            /                \
           /                  \
          0, 3     4, 5*       7


                    1, 7
Yes No              /\
                   /  \
                  /    \
                 /      \
          0, 5* /        \
               /          \
            3 /            \
             /              \
            /                \
           /                  \
          2*       4, 6        8


                    1
Yes Yes             /\
                   /  \
                  /    \
                 /      \
             0  /        \ 2
               /          \
           3* /            \ 5
             /              \
            /                \
           /                  \
          4                    6


the numbers are the order of visiting points in the new strip
and n* indicates start a new strip

*/

