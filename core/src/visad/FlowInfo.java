package visad;

import visad.*;

public class FlowInfo {

  public float[][] flow_values;
  public float flowScale;
  public float[][] spatial_values;
  public Set spatial_set;
  public int spatialManifoldDimension;
  public byte[][] color_values;
  public boolean[][] range_select;
  public GraphicsModeControl mode;
  public float constant_alpha;
  public float[] constant_color;
  public VisADGeometryArray[] arrays;

  public FlowInfo() {
  }
}

