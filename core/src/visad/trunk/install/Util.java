package visad.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util
{
  /**
   * @see Util#copyDirectory(ProgressMonitor, File, File, String)
   */
  public static final boolean copyDirectory(File source, File target)
  {
    return copyDirectory(null, source, target, null);
  }

  /**
   * @see Util#copyDirectory(ProgressMonitor, File, File, String)
   */
  public static final boolean copyDirectory(ProgressMonitor progress,
                                            File source, File target)
  {
    return copyDirectory(progress, source, target, null);
  }

  /**
   * @see Util#copyDirectory(ProgressMonitor, File, File, String)
   */
  public static final boolean copyDirectory(File source, File target,
                                            String saveSuffix)
  {
    return copyDirectory(null, source, target, saveSuffix);
  }

  /**
   * Copy files under <i>source</i> directory to <i>target</i>
   * directory.  If necessary, <i>target</i> directory is created.<br>
   * <br>
   * For example, if this method is called with a <i>source</i> of
   * <tt>/foo</tt> (which contains <tt>/foo/a</tt> and <tt>/foo/b</tt>)
   * and a <i>target</i> of <tt>/bar</tt>, when this method exits
   * <tt>/bar</tt> will contain <tt>/bar/a</tt> and <tt>/bar/b</tt>.
   * Note that <tt>foo</tt> itself is not copied.
   *
   * @param progess if non-null, this progress monitor is updated
   *                with the name of each file as it is copied.
   * @param source source directory
   * @param target directory
   * @param saveSuffix if non-null, pre-existing files under <i>target</i>
   *                   whose paths match files to be copied from
   *                   <i>source</i> will be renamed to
   *                   <tt>name + saveSuffix</tt>.
   *
   * @return false if any problems were encountered.
   */
  public static final boolean copyDirectory(ProgressMonitor progress,
                                            File source, File target,
                                            String saveSuffix)
  {
    // source must be a directory
    if (!source.isDirectory() || (target.exists() && !target.isDirectory())) {
      return false;
    }

    // if source and target are the same, we're done
    if (getPath(source).equals(getPath(target))) {
      return false;
    }

    // if the target doesn't exist yet, create it
    if (!target.exists()) {
      target.mkdirs();
    }

    boolean result = true;

    String[] list = source.list();
    for (int i = 0; i < list.length; i++) {
      File srcFile = new File(source, list[i]);
      File tgtFile = new File(target, list[i]);

      if (srcFile.isDirectory()) {
        result |= copyDirectory(progress, srcFile, tgtFile, saveSuffix);
      } else {
        result |= copyFile(progress, srcFile, tgtFile, saveSuffix);
      }
    }

    // if source was read-only, the target should be as well
    if (!source.canWrite()) {
      target.setReadOnly();
    }

    // sync up last-modified time
    target.setLastModified(source.lastModified());

    return result;
  }

  /**
   * @see Util#copyFile(ProgressMonitor, File, File, String)
   */
  public static final boolean copyFile(File source, File target)
  {
    return copyFile(null, source, target, null);
  }

  /**
   * @see Util#copyFile(ProgressMonitor, File, File, String)
   */
  public static final boolean copyFile(ProgressMonitor progress,
                                       File source, File target)
  {
    return copyFile(progress, source, target, null);
  }

  /**
   * @see Util#copyFile(ProgressMonitor, File, File, String)
   */
  public static final boolean copyFile(File source, File target,
                                       String saveSuffix)
  {
    return copyFile(null, source, target, saveSuffix);
  }

  /**
   * Copy <i>source</i> file to <i>target</i> file.
   * <i>target</i> cannot be a directory.
   * <br>
   * @param progess if non-null, this progress monitor is updated
   *                with the name of each file as it is copied.
   * @param source source directory
   * @param target directory
   * @param saveSuffix if non-null and <i>target</i> exists,
   *                   <i>target</i> will be renamed to
   *                   <tt>name + saveSuffix</tt>.
   *
   * @return false if any problems were encountered.
   */
  public static final boolean copyFile(ProgressMonitor progress,
                                       File source, File target,
                                       String saveSuffix)
  {
    // don't copy directories
    if (source.isDirectory() || target.isDirectory()) {
      return false;
    }

    // if source and target are the same, we're done
    if (getPath(source).equals(getPath(target))) {
      return false;
    }

    // if the target already exists and we need to save the existing file...
    if (target.exists()) {
      if (saveSuffix == null) {
        if (progress != null) {
          progress.setDetail("Deleting existing " + target);
        }

        // out with the old...
        target.delete();
      } else {
        if (progress != null) {
          progress.setDetail("Backing up existing " + target);
        }

        File saveFile = new File(target.getPath() + saveSuffix);

        // delete the old savefile
        if (saveFile.exists()) {
          saveFile.delete();
        }

        // save the existing target file
        target.renameTo(saveFile);
      }
    }

    if (progress != null) {
      progress.setDetail("Installing " + target);
    }

    FileInputStream  in;
    try {
      in = new FileInputStream(source);
    } catch (IOException ioe) {
      System.err.println("Couldn't open source file " + source);
      return false;
    }

    FileOutputStream out;
    try {
      out = new FileOutputStream(target);
    } catch (IOException ioe) {
      System.err.println("Couldn't open output file " + target);
      return false;
    }

    final long fileLength = source.length();

    byte buffer[]  = new byte[1024];

    try {
      long totalBytes = 0;
      while (true) {
        int n = in.read(buffer);

        if (n < 0) {
          break;
        }

        out.write(buffer, 0, n);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return false;
    } finally {
      try { in.close(); } catch (Exception e) { ; }
      try { out.close(); } catch (Exception e) { ; }
    }

    // if source was read-only, the target should be as well
    if (!source.canWrite()) {
      target.setReadOnly();
    }

    // sync up last-modified time
    target.setLastModified(source.lastModified());

    return true;
  }

  /**
   * @return either the canonical path or, if that is not
   *         available, the absolute path.
   */
  public static final String getPath(File f)
  {
    try {
      return f.getCanonicalPath();
    } catch (IOException ioe) {
      return f.getAbsolutePath();
    }
  }
}
