package visad.data.visad.object;

import visad.data.visad.BinaryFile;

public interface BinaryObject
  extends BinaryFile
{
  boolean DEBUG_RD_CSYS = false;
  boolean DEBUG_RD_DATA = false;
  boolean DEBUG_RD_DATA_DETAIL = false;
  boolean DEBUG_RD_ERRE = false;
  boolean DEBUG_RD_MATH = false;
  boolean DEBUG_RD_STR = false;
  boolean DEBUG_RD_TIME = false;
  boolean DEBUG_RD_UNIT = false;

  boolean DEBUG_WR_CSYS = false;
  boolean DEBUG_WR_DATA = false;
  boolean DEBUG_WR_DATA_DETAIL = false;
  boolean DEBUG_WR_ERRE = false;
  boolean DEBUG_WR_MATH = false;
  boolean DEBUG_WR_STR = false;
  boolean DEBUG_WR_TIME = false;
  boolean DEBUG_WR_UNIT = false;

  Object SAVE_DEPEND = new Integer(1);
  Object SAVE_DATA = new Integer(2);
}
