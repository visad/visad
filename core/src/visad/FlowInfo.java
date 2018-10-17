package visad;

import visad.data.DataCacheManager;

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
  public byte[][] trajColors;
  Object flowObjId;
  Object colorObjId;
  boolean useCache;

  public FlowInfo() {
  }
  
  float[][] getFlowValues() {
     if (useCache) {
       return DataCacheManager.getCacheManager().getFloatArray2D(flowObjId);
     }
     else {
       return flow_values;
     }
  }
  
  byte[][] getColorValues() {
     if (useCache) {
       return DataCacheManager.getCacheManager().getByteArray2D(colorObjId);
     }
     else {
       return color_values;
     }
  }
}

