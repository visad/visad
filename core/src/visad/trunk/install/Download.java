package visad.install;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;

import visad.util.CmdlineConsumer;
import visad.util.CmdlineParser;

public class Download
  implements CmdlineConsumer
{
  private File saveDir;
  private ArrayList urlList;

  public Download(String[] args)
  {
    CmdlineParser cmdline = new CmdlineParser(this);
    if (!cmdline.processArgs(args)) {
      System.err.println("Exiting...");
      System.exit(1);
    }

    if (urlList != null) {
      for (int i = 0; i < urlList.size(); i++) {
        getFile((URL )urlList.get(i));
      }
    }
  }

  public Download(URL url, String dirName)
  {
    this(url, new File(dirName));
  }

  public Download(URL url, File saveDir)
  {
    this.saveDir = saveDir;
    if (!this.saveDir.isDirectory()) {
      System.err.println("Bad directory \"" + saveDir + "\"");
      System.exit(1);
    }

    getFile(url);
  }

  public int checkKeyword(String mainName, int thisArg, String[] args)
  {
    URL url;
    try {
      url = new URL(args[thisArg]);
    } catch (MalformedURLException me) {
      System.err.println(mainName + ": Bad URL \"" + args[thisArg] +
                         "\": " + me.getMessage());
      return -1;
    }

    if (urlList == null) {
      urlList = new ArrayList();
    }

    urlList.add(url);
    return 1;
  }

  public int checkOption(String mainName, char ch, String arg)
  {
    if (ch == 'd') {
      saveDir = new File(arg);
      if (!saveDir.isDirectory()) {
        System.err.println(mainName + ": \"" + arg + "\" is not a directory");
        return -1;
      }

      return 2;
    }

    return 0;
  }

  public String keywordUsage() { return " url [url ...]"; }

  public String optionUsage() { return " -d saveDir"; }

  public boolean finalizeArgs(String mainName)
  {
    if (saveDir == null) {
      System.err.println(mainName + ": Please specify a save directory");
      return false;
    }

    return true;
  }

  public void getFile(URL url)
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

  public void initializeArgs()
  {
    saveDir = null;
    urlList = null;
  }

  public static final void main(String[] args)
  {
    new Download(args);
  }
}
