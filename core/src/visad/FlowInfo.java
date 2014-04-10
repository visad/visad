package visad;

import visad.*;

public class FlowInfo {

  public float[][] flow_values;
  public Unit[] flow_units;
  public float flowScale;
  public float[][] spatial_values;
  public Set spatial_set;
  public int spatialManifoldDimension;
  public byte[][] color_values;
  public boolean[][] range_select;
  public GraphicsModeControl mode;
  public float constant_alpha;
  public float[] constant_color;
  public DataRenderer renderer;
  public int which;
  public VisADGeometryArray[] arrays;

  public FlowInfo() {
  }
}

