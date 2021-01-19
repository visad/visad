/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;

/**
 * Download a file from a URL to a local directory.
 */
public abstract class Download
{
  /**
   * Save the file found at the URL to the specified directory.<br>
   * <br>
   * If <tt>saveFile</tt> exists and is a file, it is overwritten.
   *
   * @param url the file to download
   * @param saveFile the directory or file to which the downloaded
   *                 file is written
   * @param verbose <tt>true</tt> if a running commentary of the
   *                download's progress is desired.
   */
  public static void getFile(URL url, File saveFile, boolean verbose)
  {
    getFile(url, saveFile, false, verbose);
  }

  /**
   * Save the file found at the URL to the specified directory.
   *
   * @param url the file to download
   * @param saveFile the directory or file to which the downloaded
   *                 file is written
   * @param backUpExisting <tt>true</tt> if any existing <tt>saveFile</tt>
   *                       should be backed up
   * @param verbose <tt>true</tt> if a running commentary of the
   *                download's progress is desired.
   */
  public static void getFile(URL url, File saveFile, boolean backUpExisting,
                             boolean verbose)
  {
    if (verbose) {
      System.err.println("Downloading " + url + " to " + saveFile);
    }

    File target;
    String baseName;

    // get the target file and base name
    if (!saveFile.isDirectory()) {
      target = saveFile;
      baseName = saveFile.getName();
    } else {
      File baseFile = new File(url.getFile());
      baseName = baseFile.getName();

      // check for specified file
      if (baseName.length() == 0) {
        baseName = "file";
      }
      target = new File(saveFile, baseName);
    }

    // open the URL connection
    URLConnection conn;
    try {
      conn = url.openConnection();
    } catch (IOException ioe) {
      System.err.println("Couldn't open \"" + url + "\"");
      return;
    }

    // if file exists, only get it if there's a newer version
    if (target.exists()) {
      conn.setIfModifiedSince(target.lastModified());
    }

    // if content length is less than 0, we didn't fetch the file
    if (conn.getContentLength() < 0) {
      if (verbose) {
        System.err.println(url + " is not newer than " + target);
      }
      return;
    }

    // if a file by that name already exists,
    //  build a usable name
    if (backUpExisting && target.exists()) {

      int idx = 0;
      while (true) {
        File tmpFile = new File(saveFile, baseName + "." + idx);
        if (!tmpFile.exists()) {
          if (!target.renameTo(tmpFile)) {
            System.err.println("Couldn't rename \"" + target + "\" to \"" +
                               tmpFile + "\"");
            target.delete();
          }
          break;
        }
        idx++;
      }
    }

    // open URL for reading
    BufferedInputStream in;
    try {
      InputStream uIn = conn.getInputStream();
      in = new BufferedInputStream(uIn);
    } catch (IOException ioe) {
      System.err.println("Couldn't read \"" + url + "\"");
      return;
    }

    // open file for writing
    BufferedOutputStream out;
    try {
      FileOutputStream fOut = new FileOutputStream(target);
      out = new BufferedOutputStream(fOut);
    } catch (IOException ioe) {
      System.err.println("Couldn't write \"" + target + "\"");
      return;
    }

    // copy URL to file
    byte[] block = new byte[1024];
    while (true) {
      int len;
      try {
        len = in.read(block);
      } catch (IOException ioe) {
        ioe.printStackTrace();
        break;
      }

      if (len < 0) {
        break;
      }

      try {
        out.write(block, 0, len);
      } catch (IOException ioe) {
        ioe.printStackTrace();
        break;
      }
    }

    // close up shop
    try { out.close(); } catch (IOException ioe) { }
    try { in.close(); } catch (IOException ioe) { }

    // try to set the last-modified time appropriately
    long connMod = conn.getLastModified();
    if (connMod != 0) {
      target.setLastModified(connMod);
    }

    if (verbose) {
      System.out.println("Successfully updated " + target);
    }
  }
}
