/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
   * Copy files under the <i>source</i> directory to the <i>target</i>
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
   * Copy the <i>source</i> file to <i>target</i>.  If <i>target</i>
   * does not exist, it is assumed to be the name of the copied file.
   * If <i>target</i> is a directory, the file will be copied
   * into that directory.
   *
   * @param progess if non-null, this progress monitor is updated
   *                with the name of each file as it is copied.
   * @param source source directory
   * @param target target file/directory
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
    if (source.isDirectory()) {
      return false;
    }

    // if source and target are the same, we're done
    if (getPath(source).equals(getPath(target))) {
      return false;
    }

    if (target.isDirectory()) {
      target = new File(target, source.getName());
    }

    FileInputStream  in;
    try {
      in = new FileInputStream(source);
    } catch (IOException ioe) {
      System.err.println("Couldn't open source file " + source);
      return false;
    }

    copyStreamToFile(progress, in, target, saveSuffix);

    try { in.close(); } catch (Exception e) { ; }

    // if source was read-only, the target should be as well
    if (!source.canWrite()) {
      target.setReadOnly();
    }

    // sync up last-modified time
    target.setLastModified(source.lastModified());

    return true;
  }

  /**
   * @see Util#copyJar(ProgressMonitor, File, File, String)
   */
  public static final boolean copyJar(File source, File target)
  {
    return copyJar(null, source, target, null);
  }

  /**
   * @see Util#copyJar(ProgressMonitor, File, File, String)
   */
  public static final boolean copyJar(ProgressMonitor progress,
                                       File source, File target)
  {
    return copyJar(progress, source, target, null);
  }

  /**
   * @see Util#copyJar(ProgressMonitor, File, File, String)
   */
  public static final boolean copyJar(File source, File target,
                                       String saveSuffix)
  {
    return copyJar(null, source, target, saveSuffix);
  }

  /**
   * Extract files from the <i>source</i> jar file to the <i>target</i>
   * directory.  If necessary, the <i>target</i> directory is created.<br>
   * <br>
   * For example, if this method is called with a <i>source</i> of
   * <tt>foo.jar</tt> (which contains <tt>a</tt> and <tt>b</tt>)
   * and a <i>target</i> of <tt>/bar</tt>, when this method exits
   * <tt>/bar</tt> will contain <tt>/bar/a</tt> and <tt>/bar/b</tt>.
   *
   * @param progess if non-null, this progress monitor is updated
   *                with the name of each file as it is copied.
   * @param source source jar file
   * @param target directory
   * @param saveSuffix if non-null, pre-existing files in <i>target</i>
   *                   whose paths match files to be copied from
   *                   <i>source</i> will be renamed to
   *                   <tt>name + saveSuffix</tt>.
   *
   * @return false if any problems were encountered.
   */
  public static final boolean copyJar(ProgressMonitor progress,
                                      File source, File target,
                                      String saveSuffix)
  {
    // if target exists, it must be a directory
    if (target.exists() && !target.isDirectory()) {
      return false;
    }

    // if the target doesn't exist yet, create it
    if (!target.exists()) {
      target.mkdirs();
    }

    // try to open the jar file
    JarFile jar;
    try {
      jar = new JarFile(source);
    } catch (IOException ioe) {
      return false;
    }

    boolean result = true;

    Enumeration en = jar.entries();
    while (en.hasMoreElements()) {
      JarEntry entry = (JarEntry )en.nextElement();

      final String entryName = entry.getName();

      // skip manifest files
      if (JarFile.MANIFEST_NAME.startsWith(entryName)) {
        continue;
      }

      File newFile = new File(target, entryName);
      newFile.mkdirs();

      if (!entry.isDirectory()) {

        InputStream in;
        try {
          in = jar.getInputStream(entry);
        } catch (IOException ioe) {
          System.err.println("Couldn't copy entry " + entryName);
          continue;
        }

        copyStreamToFile(progress, in, newFile, saveSuffix);

        try { in.close(); } catch (Exception e) { ; }
      }

      newFile.setLastModified(entry.getTime());
    }

    return result;
  }

  private static final boolean copyStreamToFile(ProgressMonitor progress,
                                                InputStream in, File target,
                                                String saveSuffix)
  {
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

    FileOutputStream out;
    try {
      out = new FileOutputStream(target);
    } catch (IOException ioe) {
      System.err.println("Couldn't open output file " + target);
      return false;
    }

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
      try { out.close(); } catch (Exception e) { ; }
    }

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
