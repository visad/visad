package visad.data;

import java.io.IOException;
import java.io.File;

public interface DataWriter
  extends DataProcessor
{
  void close()
    throws IOException;
  void flush()
    throws IOException;
  void setFile(String name)
    throws IOException;
  void setFile(File file)
    throws IOException;
}
