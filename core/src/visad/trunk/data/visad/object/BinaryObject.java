package visad.data.visad.object;

import visad.data.visad.BinaryFile;

public interface BinaryObject
  extends BinaryFile
{
  Object SAVE_DEPEND = new Integer(1);
  Object SAVE_DEPEND_BIG = new Integer(5);
  Object SAVE_DATA = new Integer(100);
}
