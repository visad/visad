package visad.data;

import java.io.IOException;
import java.io.File;

/**
 * Standard routines used to write a {@link visad.Data Data} object.
 */
public interface DataWriter
  extends DataProcessor
{
  /**
   * Close the file
   *
   * @exception IOException If there is a problem.
   */
  void close()
    throws IOException;

  /**
   * Flush all data to disk.
   *
   * @exception IOException If there is a problem.
   */
  void flush()
    throws IOException;

  /**
   * Open the named file.  If a file is already being written to,
   * all data will be flushed and the file will be closed.
   *
   * @param name The path used to open the file.
   *
   * @exception IOException If there is a problem.
   */
  void setFile(String name)
    throws IOException;

  /**
   * Open the specified file.  If a file is already being written to,
   * all data will be flushed and the file will be closed.
   *
   * @param file The file.
   *
   * @exception IOException If there is a problem.
   */
  void setFile(File file)
    throws IOException;
}
