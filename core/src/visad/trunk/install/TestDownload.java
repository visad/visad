package visad.install;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;

import visad.util.CmdlineConsumer;
import visad.util.CmdlineParser;

public class TestDownload
  extends Download
  implements CmdlineConsumer
{
  private File saveDir;
  private ArrayList urlList;

  public TestDownload(String[] args)
  {
    CmdlineParser cmdline = new CmdlineParser(this);
    if (!cmdline.processArgs(args)) {
      System.err.println("Exiting...");
      System.exit(1);
    }

    if (urlList != null) {
      for (int i = 0; i < urlList.size(); i++) {
        getFile((URL )urlList.get(i), saveDir, true);
      }
    }
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

  public void initializeArgs()
  {
    saveDir = null;
    urlList = null;
  }

  public static final void main(String[] args)
  {
    new TestDownload(args);
  }
}
