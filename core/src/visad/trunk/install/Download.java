package visad.install;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

public class Download
{
  public Download() { }

  public Download(URL url, String dirName)
  {
    this(url, new File(dirName));
  }

  public Download(URL url, File saveDir)
  {
    if (!saveDir.isDirectory()) {
      System.err.println("Bad directory \"" + saveDir + "\"");
      System.exit(1);
    }

    getFile(url, saveDir);
  }

  public static void getFile(URL url, File saveDir)
  {
    File baseFile = new File(url.getFile());
    String baseName = baseFile.getName();

    // check for specified file
    File target;
    if (baseName.length() == 0) {
      baseName = "file";
    }
    target = new File(saveDir, baseName);

    // if a file by that name already exists,
    //  build a usable name
    if (target.exists()) {

      int idx = 0;
      while (true) {
        File tmpFile = new File(saveDir, baseName + "." + idx);
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

    BufferedInputStream in;
    try {
      InputStream uIn = url.openStream();
      in = new BufferedInputStream(uIn);
    } catch (IOException ioe) {
      System.err.println("Couldn't read \"" + url + "\"");
      return;
    }

    BufferedOutputStream out;
    try {
      FileOutputStream fOut = new FileOutputStream(target);
      out = new BufferedOutputStream(fOut);
    } catch (FileNotFoundException fnfe) {
      System.err.println("Couldn't write \"" + target + "\"");
      return;
    }

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

    try { out.close(); } catch (IOException ioe) { }
    try { in.close(); } catch (IOException ioe) { }
  }
}
