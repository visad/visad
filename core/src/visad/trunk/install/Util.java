package visad.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

class CopyProgress
  implements Runnable
{
  private JProgressBar bar;
  private int value;

  public CopyProgress(JProgressBar bar, int value)
  {
    this.bar = bar;
    this.value = value;
  }

  public void run()
  {
    bar.setValue(value);
  }
}

class InitProgress
  implements Runnable
{
  private JProgressBar bar;
  private int min, max;

  public InitProgress(JProgressBar bar, int min, int max)
  {
    this.bar = bar;
    this.min = min;
    this.max = max;
  }

  public void run()
  {
    bar.setMinimum(min);
    bar.setMaximum(max);
    bar.setValue(min);
  }
}

public class Util
{
  private static final long PROGRESS_SCALE = 1000;

  public static final String getPath(File f)
  {
    try {
      return f.getCanonicalPath();
    } catch (IOException ioe) {
      return f.getAbsolutePath();
    }
  }

  public static final boolean copyFile(File source, File target)
  {
    return copyFile(null, source, target, null);
  }

  public static final boolean copyFile(JProgressBar progress,
                                       File source, File target)
  {
    return copyFile(progress, source, target, null);
  }

  public static final boolean copyFile(File source, File target,
                                       String saveSuffix)
  {
    return copyFile(null, source, target, saveSuffix);
  }

  public static final boolean copyFile(JProgressBar progress,
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
        // out with the old...
        target.delete();
      } else {
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
      SwingUtilities.invokeLater(new InitProgress(progress, 0,
                                                  (int )PROGRESS_SCALE));
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

        if (progress != null) {
          totalBytes += n;

          int pos = (int )((totalBytes * PROGRESS_SCALE) / fileLength);
          SwingUtilities.invokeLater(new CopyProgress(progress, pos));
        }
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return false;
    } finally {
      try { in.close(); } catch (Exception e) { ; }
      try { out.close(); } catch (Exception e) { ; }
    }

    if (progress != null) {
      // set progress bar to 100%
      SwingUtilities.invokeLater(new CopyProgress(progress,
                                                  (int )PROGRESS_SCALE));
    }

    // if source was read-only, the target should be as well
    if (!source.canWrite()) {
      target.setReadOnly();
    }

    // sync up last-modified time
    target.setLastModified(source.lastModified());

    return true;
  }

  public static final boolean copyDirectory(File source, File target)
  {
    return copyDirectory(null, source, target, null);
  }

  public static final boolean copyDirectory(JProgressBar progress,
                                            File source, File target)
  {
    return copyDirectory(progress, source, target, null);
  }

  public static final boolean copyDirectory(File source, File target,
                                            String saveSuffix)
  {
    return copyDirectory(null, source, target, saveSuffix);
  }

  public static final boolean copyDirectory(JProgressBar progress,
                                            File source, File target,
                                            String saveSuffix)
  {
    // source must be a directory
    if (!source.isDirectory() || (target.exists() && !target.isDirectory())) {
      return false;
    }

    // if source and target are the same, we're done
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
}
