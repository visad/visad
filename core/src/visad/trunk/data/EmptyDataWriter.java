package visad.data;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class EmptyDataWriter
  extends EmptyDataProcessor
  implements DataWriter
{
  public EmptyDataWriter() { }
  public void close() throws IOException { }
  public void flush() throws IOException { }
  public void setFile(String name) { }
  public void setFile(File file) { }
}
