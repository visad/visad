/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import visad.util.FloatTupleArray;

/**
 * Used by <code>Contour2D</code>'s filled contour algorithm to build a
 * <code>VisADTriangleStripArray</code>. Triangle vertices, normals, and colors
 * are stored until the <code>compile</code> is called, at which point the
 * builder is invalidated. <p> This code is very specific and makes many
 * assumptions about the data used to create the strips. Input validation is
 * kept to a minimum, and when input is validated it is done with assertions so
 * they can be enabled and disabled at runtime.
 * <p>
 * Arrays of vertices and normals grow dynamically when needed to avoid an 
 * initial large array allocation.
 * @see {@link visad.test.FloatTupleArray}
 */
class TriangleStripBuilder {

  static Logger log = Logger.getLogger(TriangleStripBuilder.class.getName());
  /**
   * Flag enabling strip merging with previous grid boxes.
   */
  public static final int MERGE_PREVIOUS = 2 ^ 0;
  /**
   * Flag disabling all merging. All other policy flags are ignored.
   */
  public static final int MERGE_NONE = 0;
  
  /**
   * Default policy for strip merging. NOTE: Currently the default is set to
   * only merge previous. The algorithm for merging in the current grid box does
   * not work properly.
   */
  public static final int DEF_MERGE_POLICY = MERGE_PREVIOUS;
  /** Vertices for the entire grid. */
  private final FloatTupleArray vertices;
  /** Normals corresponding to the vertices for the entire grid. */
  private final FloatTupleArray normals;
  /** Number of grid rows. */
  private int gridRows;
  /** Number of grid columns. */
  private int gridCols;
  /** The current grid box number. */
  private int curBoxNum;
  /** Metadata for the previous grid box. */
  private GridBox prevGridBox;
  /** Metadata for the current grid box. */
  private GridBox curGridBox;
  /**
   * Strip objects that will be compiled into strip counts and color arrays.
   */
  private final List<StripProps> strips;
  /** Dimension of the color arrays. */
  private final int colorDim;
  /** If true merging in the previous grid box will be attempted. */
  private boolean mergePrevious;
  private VisADTriangleStripArray compiledStrip;
  
  private int numFlipped;
  private int numMerged;

  /**
   * New instance with the default coordinate delta and merging policy.
   * 
   * @param rows Number of grid rows
   * @param cols Number of grid cols
   * @param colorDim number of color elements
   */
  TriangleStripBuilder(int rows, int cols, int colorDim) {
    this(rows, cols, colorDim, DEF_MERGE_POLICY);
  }

  /**
   * New instance with the default coordinate delta.
   * 
   * @param rows Number of grid rows
   * @param cols Number of grid cols
   * @param colorDim number of color elements
   * @param mergePolicy A logical OR'ing of <code>MERGE</code> constants that
   *          specifies the options used when merging strips.
   */
  TriangleStripBuilder(int rows, int cols, int colorDim, int mergePolicy) {
    this.gridRows = rows;
    this.gridCols = cols;
    this.curBoxNum = 0;
    this.colorDim = colorDim;
    this.mergePrevious = (mergePolicy & MERGE_PREVIOUS) == MERGE_PREVIOUS;
    log.finer("mergePolicy prev:" + mergePrevious);

    this.vertices = FloatTupleArray.Factory.newInstance(2, rows * cols * 4);
    this.normals = FloatTupleArray.Factory.newInstance(3, rows * cols * 4);

    strips = new ArrayList<StripProps>();
    
    compiledStrip = null;
  }

  /**
   * Compile the computed strips into a scene graph object. The first time compile
   * is called a <code>VisADTriangleStripArray</code> is created and subsequent 
   * calls return the initially compiled object.
   * 
   * @param set Used to convert grid coordinates to display coordinates.
   * @return A valid object ready for adding to the scene graph.
   * @throws visad.VisADException
   */
  public VisADTriangleStripArray compile(Gridded3DSet set) throws VisADException {
    int totalBoxes = gridRows * gridCols;
    if (curBoxNum != totalBoxes) {
      throw new IllegalStateException(String.format("Not finished. On gridbox %s of %s", curBoxNum,
          totalBoxes));
    }
    if (compiledStrip != null) return compiledStrip;

    byte[] colors = new byte[colorDim * vertices.size()];

    // stripVertexCounts are the number of vertices that comprise each
    // individual strip, which were compiled in the StripProps.vertexCount-s
    int[] stripVertexCounts = new int[strips.size()];
    for (int sIdx = 0, clrIdx = 0; sIdx < strips.size(); sIdx++) {
      StripProps strip = strips.get(sIdx);
      // each vertex for a strip gets the same color
      for (int vIdx = 0; vIdx < strip.vertexCount; vIdx++) {
        for (int i = 0; i < colorDim; i++) {
          colors[clrIdx++] = strip.color[i];
        }
      }
      stripVertexCounts[sIdx] = strip.vertexCount;
    }

    // compile normals.
    float[][] curNormals = normals.toArray();
    float[] newNormals = new float[curNormals.length * curNormals[0].length];
    for (int i = 0, j = 0; i < curNormals[0].length; i++) {
      newNormals[j++] = curNormals[0][i];
      newNormals[j++] = curNormals[1][i];
      newNormals[j++] = curNormals[2][i];
    }

    try {
      compiledStrip = new VisADTriangleStripArray();
      // number of vertices for each individual strips
      compiledStrip.stripVertexCounts = stripVertexCounts;
      // number of vertices in this geometry
      compiledStrip.vertexCount = vertices.size();
      compiledStrip.coordinates = set.gridToValue(vertices.toArray(), true)[0];
      compiledStrip.normals = newNormals;
      compiledStrip.colors = colors;
    } catch (VisADException e) {
      // ensure null strip is an exception occurs
      compiledStrip = null;
      throw e;
    }
    
    log.fine("compiled " + toString());
    //System.err.println("compiled " + toString());
    
    return compiledStrip;
  }

  /**
   * Add vertices to the builder. Merging is performed according to either the
   * default policy or the policy provided during instantiation. Unless an error
   * occurs the vertices provided are always either merged with a current strip 
   * or used to create a new strip.
   * <p>
   * Assumptions:
   * <ol>
   * <li>Caller will not give points in an order that will cause triangles to 
   * be folded over one another.
   * <li>Caller will provide strips, not individual triangle vertices. 
   * <li>Grid is traversed left to right.
   * </ol>
   * 
   * @param kase flag indicating the contour case that generated these vertices.
   * @param side flag indicating the side of the grid box the vertices end on.
   * @param orientation flag indicating the orientation of the vertices.
   */
  public void addVerticies(int lvlIdx, float[][] verts, float[][] norms, byte[] color,
      byte sideFirst, byte orientFirst, byte sideLast, byte orientLast) {


    StripProps strip = null;

    //
    // MERGE IN PREVIOUS GRID BOX
    //
    
    if (mergePrevious && curGridBox.strips.size() == 0) {
      strip = mergeToPrevious(lvlIdx, verts, norms, sideFirst, orientFirst, sideLast, 
          orientLast);
    }

    //
    // CREATE NEW STRIP
    //
    if (strip == null) {
      strip = new StripProps(color, lvlIdx, sideFirst, orientFirst, sideLast, orientLast);

      this.vertices.add(verts);
      this.normals.add(norms);
      strip.vertexCount = verts[0].length;

      strips.add(strip);
      curGridBox.add(strip);
    }

    // // strip cannot be null here
    // int start = this.vertices.size() - strip.vertexCount;
    // log.finest(toString(this.vertices.elements(), start,
    // strip.vertexCount));
  }
  
  /**
   * Attempt to merge vertices with a strip in the previous grid box.
   * @param lvlIdx
   * @param verts
   * @param norms
   * @param kase flag indicating the contour case that generated these vertices.
   * @param side flag indicating the side of the grid box the vertices end on.
   * @param orientation flag indicating the orientation of the vertices.
   * 
   * @return the merged strip, or null if no merge took place
   */
  protected StripProps mergeToPrevious(int lvlIdx, float[][] verts, float[][] norms,
      byte sideFirst, byte orientFirst, byte sideLast, byte orientLast) {

    boolean modified = false;
    //currently we can only merge to the last strip added to the prev grid box
    StripProps prevStrip = prevGridBox == null ? null : prevGridBox.lastStrip();

    boolean firstCol = curBoxNum % gridCols == 1; // can't when in first column
    boolean correctLvl = false; // has to have the same color level index
    boolean shareSide = false; // have to share a side
    boolean oppositeOrient = false; // have to have the opposite orientation
    if (prevStrip != null) {
      correctLvl = prevStrip.lvlIdx == lvlIdx;
      shareSide = prevStrip.sideLast == Contour2D.SIDE_RIGHT && sideFirst == Contour2D.SIDE_LEFT;
      if (prevStrip.orientLast == Contour2D.CLOCKWISE) {
        oppositeOrient = (orientFirst == Contour2D.CNTRCLOCKWISE);
      } else {
        oppositeOrient = (orientFirst == Contour2D.CLOCKWISE);
      }
    }
    
    if (!firstCol && correctLvl && shareSide && oppositeOrient) {
      
      if (!oppositeOrient) {
        // FIXME: (1) no flipping for now, (2) if flipped then somehow new orientation has to be recorded.
        boolean mustFlip = false;
        if (mustFlip) {
          flipStrip(verts, norms);
          oppositeOrient = true;
          numFlipped++;
        }
      }

      // add the new
      int num = verts[0].length - 2;
      this.vertices.add(verts, 2, num);
      this.normals.add(norms, 2, num);
      prevStrip.vertexCount += num;
      prevStrip.sideLast = sideLast;
      prevStrip.orientLast = orientLast;
      modified = true;

      // since we merged a current box strip with one in the
      // previous box, we swap it's ownership to the current box
      prevGridBox.strips.remove(prevStrip);
      curGridBox.strips.add(prevStrip);
      
      numMerged++;
    }
    return modified ? prevStrip : null;
  }
  
  public String toString() {
    float avgLen = strips.size() == 0 ? 0 : vertices.size() / strips.size();
    int maxLen = 0;
    for (int i=0; i<strips.size(); i++) {
      StripProps strip = strips.get(i);
      if (strip.vertexCount > maxLen) maxLen = strip.vertexCount;
    }
    return String.format("<%s numStrips=%s numFlipped=%s avgLen=%s maxLen=%s numMerged=%s>",
        TriangleStripBuilder.class.getName(), strips.size(), numFlipped, avgLen, maxLen, numMerged);
  }

  /**
   * Set the current grid box. The previous grid box will be cleared and will
   * become the former current grid box.
   * 
   * @param row
   * @param col
   */
  public void setGridBox(int row, int col) {
    if (prevGridBox != null) {
      prevGridBox.strips.clear();
    }
    prevGridBox = curGridBox;
    curBoxNum++;
    curGridBox = new GridBox(row, col);
  }

  /**
   * Container for grid box metadata.
   */
  class GridBox {

    int row;
    int col;
    final List<StripProps> strips;

    GridBox(int row, int col) {
      this.row = row;
      this.col = col;
      strips = new ArrayList<StripProps>(3);
    }

    void add(StripProps strip) {
      strips.add(strip);
      assert strip.vertexCount >= 3 : "Stripcount < 3";
    }

    StripProps lastStrip() {
      return strips.size() > 0 ? strips.get(strips.size() - 1) : null;
    }

    @Override
    public String toString() {
      return String.format("<GridBox row=%s col=%s>", row, col);
    }
  }

  /**
   * Container for strip metadata.
   */
  class StripProps {

    /** Color for all vertices. */
    final byte[] color;
    /** Number of vertices. Must be at least 2. */
    int vertexCount;
    /** Index into the contour level array for this strip. */
    final int lvlIdx;
    
    final byte sideFirst;
    final byte orientFirst;
    byte sideLast;
    byte orientLast;

    boolean flipped = false;
    
    StripProps(byte[] color, int lvlIdx, byte sideFirst, byte orientFirst,
        byte sideLast, byte orientLast) {
      this.color = color;
      this.lvlIdx = lvlIdx;
      this.sideFirst = sideFirst;
      this.orientFirst = orientFirst;
      this.sideLast = sideLast;
      this.orientLast = orientLast;
    }

    @Override
    public String toString() {
      return String.format("<StripProps count=%s lvlIdx=%s color=%s>", vertexCount, lvlIdx, 
          Arrays.toString(color));
    }
  }
  
  /**
   * Used in coordinate comparisons.
   */
  static final double DEF_COORD_DELTA = 2.1e-5;

  static boolean coordEquals(float c1, float c2, double delta) {
    return Math.abs(c1 - c2) <= delta;
  }

  /** Alias for checking coordinate equality. */
  static boolean coordEquals(float c1, float c2) {
    return coordEquals(c1, c2, DEF_COORD_DELTA);
  }

  static boolean coordsEqual(FloatTupleArray v1, int idx1, float[][] v2, int idx2, double delta) {
    boolean match = true;
    for (int dim = 0; dim < v1.dim(); dim++) {
      match &= coordEquals(v1.get(dim, idx1), v2[dim][idx2], delta);
    }
    return match;
  }

  /**
   * Check if coordinates are equal.
   * 
   * @param v1 Vertex array 1
   * @param idx1 Index of the coordinates to check in v1
   * @param v2 Vertex array 2
   * @param idx2 Index of coordinates to check in v2
   * @return True if all components at the given indices are equal.
   */
  static boolean coordsEqual(FloatTupleArray v1, int idx1, float[][] v2, int idx2) {
    return coordsEqual(v1, idx1, v2, idx2, DEF_COORD_DELTA);
  }

  /**
   * Check if 2 vertices of a line match 2 vertices of a triangle.
   * 
   * @param v1 Vertex array of the line
   * @param idx1 Index of the first point of the line, where idx1 + 1 is the
   *          second.
   * @param v2 Vertex array of the triangle
   * @param idx2 Index of the first vertex of the triangle, where idx+1 and
   *          idx1+2 will be the next two.
   * @param delta delta to use for comparisons
   * @return An array with the matching triangle vertices in index 1 and 2 and
   *         the unmatched triangle vertex index at index 3. If no match is
   *         found return null.
   */
  static synchronized int[] stripCoordEquals(FloatTupleArray stripVerticies, int sIdx,
      float[][] triVerticies, int tIdx) {
    return stripCoordEquals(stripVerticies, sIdx, triVerticies, tIdx, DEF_COORD_DELTA);
  }

  static synchronized int[] stripCoordEquals(FloatTupleArray stripVerticies, int sIdx,
      float[][] triVerticies, int tIdx, double delta) {
    assert sIdx + 1 < stripVerticies.size();
    assert tIdx + 2 < triVerticies[0].length;

    int s1 = sIdx, s2 = sIdx + 1;
    int t1 = tIdx, t2 = tIdx + 1, t3 = tIdx + 2;

    if (coordsEqual(stripVerticies, s1, triVerticies, t1, delta)) {
      if (coordsEqual(stripVerticies, s2, triVerticies, t2, delta)) {
        return new int[] { t1, t2, t3 };

      } else if (coordsEqual(stripVerticies, s2, triVerticies, t3, delta)) {
        return new int[] { t1, t3, t2 };

      }
    } else if (coordsEqual(stripVerticies, s1, triVerticies, t2, delta)) {
      if (coordsEqual(stripVerticies, s2, triVerticies, t1, delta)) {
        return new int[] { t2, t1, t3 };

      } else if (coordsEqual(stripVerticies, s2, triVerticies, t3, delta)) {
        return new int[] { t2, t3, t1 };

      }

    } else if (coordsEqual(stripVerticies, s1, triVerticies, t3, delta)) {
      if (coordsEqual(stripVerticies, s2, triVerticies, t1, delta)) {
        return new int[] { t3, t1, t2 };

      } else if (coordsEqual(stripVerticies, s2, triVerticies, t2, delta)) {
        return new int[] { t3, t2, t1 };

      }
    }
    return null;
  }

  static float[] extractCoords(float[][] vertices, int idx) {
    float[] coords = new float[vertices.length];
    for (int i = 0; i < vertices.length; i++) {
      coords[i] = vertices[i][idx];
    }
    return coords;
  }

  static String toString(float[][] coords) {
    int len = coords.length > 0 ? coords[0].length : 0;
    return toString(coords, 0, len);
  }

  static String toString(float[][] coords, int start, int num) {
    StringBuilder buf = new StringBuilder();
    for (int i = start, cnt = 0; cnt < num; i++, cnt++) {
      buf.append("(");
      for (int dim = 0; dim < coords.length - 1; dim++) {
        buf.append("" + coords[dim][i] + ",");
      }
      buf.append("" + coords[coords.length - 1][i] + ") ");
    }
    return buf.toString();
  }

  static void flipStrip(float[][] verts, float[][] norms) {
    int num = verts[0].length % 2 == 0 ? verts[0].length - 1 : verts[0].length - 2;
    for (int i = 0; i < num; i += 2) {
      float tv = verts[0][i];
      verts[0][i] = verts[0][i + 1];
      verts[0][i + 1] = tv;
      tv = verts[1][i];
      verts[1][i] = verts[1][i + 1];
      verts[1][i + 1] = tv;

      float tn = norms[0][i];
      norms[0][i] = norms[0][i + 1];
      norms[0][i + 1] = tn;
      tn = norms[1][i];
      norms[1][i] = norms[1][i + 1];
      norms[1][i + 1] = tn;
      tn = norms[2][i];
      norms[2][i] = norms[2][i + 1];
      norms[2][i + 1] = tn;
    }
  }

  static String toString(FloatTupleArray coords) {
    return toString(coords, 0, coords.size());
  }

  static String toString(FloatTupleArray coords, int start, int num) {
    StringBuilder buf = new StringBuilder();
    for (int i = start, cnt = 0; cnt < num; i++, cnt++) {
      buf.append("(");
      for (int dim = 0; dim < coords.dim() - 1; dim++) {
        buf.append("" + coords.get(dim, i) + ",");
      }
      buf.append("" + coords.get(coords.dim() - 1, i) + ") ");
    }
    return buf.toString();
  }
}
