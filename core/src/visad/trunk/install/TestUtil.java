import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import visad.install.ProgressMonitor;
import visad.install.Util;

import visad.util.CmdlineConsumer;
import visad.util.CmdlineGenericConsumer;
import visad.util.CmdlineParser;

public class TestUtil
  extends CmdlineGenericConsumer
{
  private File fromFile, toFile;
  private String suffix;
  private boolean trackProgress;

  public class MakeCopy
    extends Thread
  {
    private ProgressMonitor mon;
    private File fromFile, toFile;
    private String suffix;
    private boolean result;

    MakeCopy(ProgressMonitor mon, File fromFile, File toFile, String suffix)
    {
      this.mon = mon;
      this.fromFile = fromFile;
      this.toFile = toFile;
      this.suffix = suffix;
      this.result = false;
    }

    public void run()
    {
      if (fromFile.isDirectory()) {
        result = Util.copyDirectory(mon, fromFile, toFile, suffix);
      } else {
        result = Util.copyJar(mon, fromFile, toFile, suffix);
        if (!result) {
          result = Util.copyFile(mon, fromFile, toFile, suffix);
        }
      }
      System.err.println("Result was " + result);
    }

    boolean getResult() { return result; }
  }

  public TestUtil(String[] args)
  {
    CmdlineParser cmdline = new CmdlineParser(this);
    if (!cmdline.processArgs(args)) {
      System.err.println("Exiting...");
      System.exit(1);
    }

    ProgressMonitor mon = null;
    if (trackProgress) {
      JFrame win = new JFrame("Frame-o-licious");

      mon = new ProgressMonitor();
      win.getContentPane().add(buildProgress("Copying " + fromFile + " to " +
                                             toFile, mon));
      win.pack();
      win.setVisible(true);
    }

    MakeCopy cp = new MakeCopy(mon, fromFile, toFile, suffix);
    cp.start();
    while (cp.isAlive()) {
      try {
        cp.join();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }

    if (!cp.getResult()) {
      System.err.println("Copy failed!");
      System.exit(1);
    }

    System.out.println("Copied \"" + fromFile + "\" to \"" + toFile + "\"");
    System.exit(0);
  }

  private JPanel buildProgress(String label, ProgressMonitor mon)
  {
    JPanel panel = new JPanel();

    panel.setLayout(new java.awt.BorderLayout());

    panel.add("North", new JLabel(label));
    panel.add("South", mon);

    return panel;
  }

  public int checkKeyword(String mainName, int thisArg, String[] args)
  {
    if (fromFile == null) {
      fromFile = new File(args[thisArg]);
      if (!fromFile.exists()) {
        System.err.println(mainName + ": File \"" + fromFile +
                           "\" does not exist");
        return -1;
      }

      return 1;
    }

    if (toFile == null) {
      toFile = new File(args[thisArg]);
      return 1;
    }

    return super.checkKeyword(mainName, thisArg, args);
  }

  public int checkOption(String mainName, char ch, String arg)
  {
    if (ch == 'p') {
      trackProgress = true;
      return 1;
    }

    if (ch == 's') {
      suffix = arg;
      return 2;
    }

    return super.checkOption(mainName, ch, arg);
  }

  public boolean finalizeArgs(String mainName)
  {
    if (fromFile == null || toFile == null) {
      System.err.println(mainName + ": Please specify two file names!");
      return false;
    }

    return true;
  }

  public void initializeArgs()
  {
    fromFile = toFile = null;
    suffix = null;
    trackProgress = false;
  }

  public String keywordUsage() { return " fromFile toFile"; }

  public String optionUsage() { return " [-p(rogressBar)]"; }

  public static final void main(String[] args)
  {
    new TestUtil(args);
  }
}
