package visad.install;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JWindow;

import visad.util.CmdlineGenericConsumer;
import visad.util.CmdlineParser;

public class Main
  extends CmdlineGenericConsumer
{
  private static final String CLASSPATH_PROPERTY = "java.class.path";
  private static final String PATH_PROPERTY = "visad.install.path";

  private static final String JAR_NAME = "visad.jar";
  private static final String VISAD_JAR_URL =
    "ftp://ftp.ssec.wisc.edu/pub/visad-2.0/" + JAR_NAME;

  private boolean debug;

  private URL jarURL;
  private ChooserList chooser;
  private Path classpath, path;
  private ArrayList jarList, javaList;
  private File installerJar;
  private JavaFile installerJava;
  private File installerJavaDir;
  private String cPushStr;

  private boolean useSuppliedJava, downloadLatestJar;
  private File jvmToUse, javaInstallDir, jarInstallDir;
  private ClusterInstaller clusterInstaller;

  public Main(String[] args)
  {
    CmdlineParser cmdline = new CmdlineParser(this);
    if (!cmdline.processArgs(args)) {
      System.exit(1);
      return;
    }

    SplashScreen ss = new SplashScreen("visad-splash.jpg");
    ss.setVisible(true);

    boolean initResult = initialize();

    ss.setVisible(false);

    if (!initResult) {
      System.exit(1);
      return;
    }

    if (debug) {
      dumpInitialState();
    }

    useSuppliedJava = downloadLatestJar = false;
    jvmToUse = javaInstallDir = jarInstallDir = null;
    clusterInstaller = null;

    queryUser();

    if (debug) {
      dumpInstallState();
    }

    install();
  }


  /**
   * Check all java executables for minimum version.
   *
   * @param list list of File objects
   * @param major major number of java version
   * @param minor minor number of java version
   */
  private static final void checkJavaVersions(ArrayList list,
                                              int major, int minor)
  {
    int i = 0;
    while (i < list.size()) {
      JavaFile f = new JavaFile((File )list.get(i));

      if (JavaVersion.matchMinimum(f, major, minor)) {
        list.set(i++, f);
      } else {
        list.remove(i);
      }
    }
  }

  public int checkOption(String mainName, char ch, String arg)
  {
    if (ch == 'x') {
      debug = true;
      return 1;
    }

    return 0;
  }

  /**
   * Ugly hack to make JVM binaries executable.
   */
  private final void makeJVMBinariesExecutable()
  {
    // find all instances of 'chmod'
    ArrayList chmodList = path.find("chmod");
    if (chmodList == null || chmodList.size() == 0) {
      // if no 'chmod' found, we're done
      return;
    }

    // only care about one of them, so arbitrarily grab the first one
    File chmod = (File )chmodList.get(0);

    // get a handle for the JVM executable directory
    File jvmBin = new File(javaInstallDir, "bin");
    if (!jvmBin.exists()) {
      // if JVM binary directory doesn't exist, we're done
      return;
    }

    // get the list of JVM executables
    String[] binFiles = jvmBin.list();
    if (binFiles == null || binFiles.length == 0) {
      // if JVM binary directory is empty, we're done
      return;
    }

    // create an array holding the command to be executed
    String[] cmd = new String[2 + binFiles.length];
    cmd[0] = chmod.toString();
    cmd[1] = "555";

    // preload the directory path into the stringbuffer
    StringBuffer buf = new StringBuffer(jvmBin.toString());
    buf.append('/');

    // remember the buffer length
    final int len = buf.length();

    // add all executables to the command list
    for (int i = 0; i < binFiles.length; i++) {
      buf.setLength(len);
      buf.append(binFiles[i]);

      cmd[i+2] = buf.toString();
    }

    Process p;
    try {
      p = Runtime.getRuntime().exec(cmd);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    try {
      p.waitFor();
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  private final static File chooseDirectory(ChooserList chooser,
                                            ArrayList list, String title)
  {
    final int listLen = (list == null ? 0 : list.size());

    boolean allDirs = true;
    for (int i = 0; i < listLen; i++) {
      File f = (File )list.get(i);
      if (!f.isDirectory()) {
        allDirs = false;
        break;
      }
    }

    File[] dList;
    if (!allDirs) {
      // build list containing only directories
      dList = new File[listLen];
      for (int i = 0; i < listLen; i++) {
        File f = (File )list.get(i);
        if (f.isDirectory()) {
          dList[i] = f;
        } else {
          dList[i] = new File(f.getParent());
        }
      }
    } else if (listLen > 0) {
      // build array from ArrayList
      dList = (File[] )list.toArray(new File[list.size()]);
    } else {
      // empty/null list
      dList = null;
    }

    chooser.setList(dList);
    chooser.setFileSelectionMode(ChooserList.DIRECTORIES_ONLY);
    chooser.setDialogTitle(title);

    int option = chooser.showOpenDialog(null);
    if (option == ChooserList.CANCEL_OPTION) {
      return null;
    }

    File choice = chooser.getSelectedFile();
    if (!choice.exists()) {
      return choice;
    }

    if (choice.isDirectory()) {
      return choice;
    }

    return new File(choice.getParent());
  }

  private static final File chooseFile(ChooserList chooser,
                                       ArrayList list, String title)
  {
    if (list == null) {
      chooser.setList(null);
    } else {
      chooser.setList((File[] )list.toArray(new File[list.size()]));
    }
    chooser.setFileSelectionMode(ChooserList.FILES_ONLY);
    chooser.setDialogTitle(title);
    //chooser.setApproveButtonText("Choose...");

    int option = chooser.showOpenDialog(null);
    if (option == ChooserList.CANCEL_OPTION) {
      return null;
    }

    return chooser.getSelectedFile();
  }

  private final void dumpInitialState()
  {
    if (installerJavaDir != null) {
      System.out.println("Supplied java directory: " + installerJavaDir);
      System.out.println("Supplied java: " + installerJava);
    }

    if (jarList == null) {
      System.err.println("No " + JAR_NAME + " found in " + classpath);
    } else {
      System.err.println("== jar file list ==");
      for (int i = 0; i < jarList.size(); i++) {
        System.out.println("#" + i + ": " +
                           getPath((File )jarList.get(i)));
      }
    }

    if (javaList == null) {
      System.err.println("No java executable found in " + path);
    } else {
      System.err.println("== java executable list ==");
      for (int i = 0; i < javaList.size(); i++) {
        System.out.println("#" + i + ": " +
                           getPath((File )javaList.get(i)));
      }
    }

    if (cPushStr == null) {
      System.err.println("No cluster executable found in " + path);
    } else {
      System.err.println("== cluster executable ==");
      System.out.println(cPushStr);
    }
  }

  private final void dumpInstallState()
  {
    if (useSuppliedJava) {
      System.err.println("Install java in " + javaInstallDir);
      if (jvmToUse != null) {
        System.err.println("!! 'jvmToUse' is set !!");
      }
    } else {
      System.err.println("Use jvm in " + jvmToUse);
      if (javaInstallDir != null) {
        System.err.println("!! 'javaInstallDir' is set !!");
      }
    }

    if (downloadLatestJar) {
      System.err.println("Download latest " + JAR_NAME);
    }
    System.err.println("Install " + JAR_NAME + " in " + jarInstallDir);

    if (clusterInstaller != null) {
      System.err.println("Push installed files out to cluster");
    }
  }

  /**
   * Remove and return the installer executable from the
   * list of java executable JavaFile objects.
   *
   * @param list list of JavaFile objects
   *
   * @return <tt>null</tt> if installer-supplied java executable
   *         was not found
   */
  private static final File extractInstallerFile(ArrayList javaList)
  {
    String curDir = getPath(new File("."));

    java.util.Iterator iter = javaList.iterator();
    while (iter.hasNext()) {
      File thisFile = (File )iter.next();
      if (getPath(thisFile).startsWith(curDir)) {
        iter.remove();
        return thisFile;
      }
    }

    return null;
  }

  /**
   * Return either the canonical path or, if that is not possible,
   * the specified path.
   *
   * @param f File object
   *
   * @return either the canonical path or the originally specified path.
   */
  private static final String getPath(File f)
  {
    try {
      return f.getCanonicalPath();
    } catch (IOException ioe) {
      return f.getPath();
    }
  }

  /**
   * Initialize internal state.
   */
  private final boolean initialize()
  {
    // create a ChooserList (for speed purposes)
    chooser = new ChooserList();

    // set everything to null
    classpath = path = null;
    jarURL = null;
    jarList = javaList = null;

    // get class path elements
    try {
      classpath = new Path(System.getProperty(CLASSPATH_PROPERTY));
    } catch (IllegalArgumentException iae) {
      System.err.println(getClass().getName() +
                         ": Couldn't get Java class path");
      return false;
    }

    // get executable path elements
    try {
      path = new Path(System.getProperty(PATH_PROPERTY));
    } catch (IllegalArgumentException iae) {
      System.err.println(getClass().getName() +
                         ": Please pass in the executable path via the '" +
                         PATH_PROPERTY + "' property");
      return false;
    }

    // build the URL for the jar file
    try {
      jarURL = new URL(VISAD_JAR_URL);
    } catch (java.net.MalformedURLException mue) {
      jarURL = null;
    }

    // no installer-supplied jar file found yet
    installerJar = null;

    // find all visad jar files
    jarList = classpath.findMatch(JAR_NAME);
    if (jarList == null) {
      jarList = classpath.find(JAR_NAME);
    }
    if (jarList != null) {
      loseDuplicates(jarList);
      installerJar = extractInstallerFile(jarList);
    }

    // no installer-supplied java found yet
    installerJava = null;
    installerJavaDir = null;

    // find all java executables
    javaList = path.find("java");
    if (javaList != null) {
      loseDuplicates(javaList);
      checkJavaVersions(javaList, 1, 2);
      installerJava = (JavaFile )extractInstallerFile(javaList);

      if (installerJava != null && installerJava.getName().equals("java")) {
        File canonJava = new File(getPath(installerJava));

        installerJavaDir = new File(canonJava.getParent());
        if (installerJavaDir.getName().equals("bin")) {
          installerJavaDir = new File(installerJavaDir.getParent());
        }
      }
    }

    if (jarURL == null && installerJar == null) {
      System.err.println("Couldn't find either distributed jar file" +
                         " or jar file URL!");
      System.exit(1);
    }

    // no cluster installation executable found yet
    cPushStr = null;

    // find all cluster installation executables
    ArrayList c3List = path.find("cpush");
    if (c3List != null) {
      loseDuplicates(c3List);
      if (c3List != null && c3List.size() > 0) {
        cPushStr = getPath((File )c3List.get(0));
      }
    }

    return true;
  }

  public void initializeArgs()
  {
    debug = false;
  }

  /**
   * Install VisAD.
   */
  private void install()
  {
    // install JVM
    if (useSuppliedJava) {
      if (javaInstallDir.exists()) {
        javaInstallDir = new File(javaInstallDir, installerJavaDir.getName());
      }

      Util.copyDirectory(installerJavaDir, javaInstallDir);

      makeJVMBinariesExecutable();

      // if they want a cluster install, push the JVM out to the nodes
      if (clusterInstaller != null) {
        clusterInstaller.push(javaInstallDir.toString(),
                              javaInstallDir.getParent().toString());
      }
    }

    // install jar
    if (downloadLatestJar) {
      new Download(jarURL, jarInstallDir);
    } else {
      Util.copyFile(installerJar, jarInstallDir, ".old");
    }

    // if they want a cluster install, push the jar file out to the nodes
    if (clusterInstaller != null) {
      clusterInstaller.push(getPath(new File(jarInstallDir, JAR_NAME)),
                            getPath(jarInstallDir));
    }
  }

  /**
   * Remove duplicate objects from the list.
   *
   * @param list of Objects
   */
  private static final void loseDuplicates(ArrayList list)
  {
    for (int i = 0; i < list.size(); i++) {
      Object objI = list.get(i);

      int j = i + 1;
      while (j < list.size()) {
        Object objJ = list.get(j);

        if (!objI.equals(objJ)) {
          j++;
        } else {
          list.remove(j);
        }
      }
    }
  }

  public String optionUsage()
  {
    return super.optionUsage() + " [-x(debug)]";
  }

  /**
   * Query user about installation options.
   */
  private final void queryUser()
  {
    final int STEP_USE_SUPPLIED = 0;
    final int STEP_INSTALL_JAVA = 1;
    final int STEP_INSTALL_JAR = 2;
    final int STEP_DOWNLOAD_JAR = 3;
    final int STEP_CLUSTER = 4;
    final int STEP_FINISHED = 5;

    int step = 0;
    while (step < STEP_FINISHED) {
      switch (step) {
      case STEP_USE_SUPPLIED:
        if (installerJavaDir == null) {
          useSuppliedJava = false;
        } else {
          String usMsg = "Would you like to install the supplied " +
            " Java Development Kit " + installerJava.getMajor() + "." +
            installerJava.getMinor() + " (" +
            installerJava.getFullString() + ")?";

          int n = JOptionPane.showConfirmDialog(null, usMsg,
                                                "Install supplied JDK?",
                                                JOptionPane.YES_NO_OPTION);
          useSuppliedJava = (n == JOptionPane.YES_OPTION);
        }
        step++;
        break;
      case STEP_INSTALL_JAVA:
        javaInstallDir = jvmToUse = null;
        if (useSuppliedJava) {
          javaInstallDir = chooseDirectory(chooser, null,
                                           "Select the directory in which the JDK should be installed");
          if (javaInstallDir == null) {
            step--;
          } else if (javaInstallDir.canWrite()) {
            step++;
          } else {
            JOptionPane.showMessageDialog(null,
                                          "Cannot write to that directory!",
                                          "Bad directory?",
                                          JOptionPane.ERROR_MESSAGE);
          }
        } else {
          jvmToUse = chooseFile(chooser, javaList,
                                "Select the java program to use");
          if (jvmToUse == null) {
            step--;
          } else {
            step++;
          }
        }
        break;
      case STEP_INSTALL_JAR:
        jarInstallDir = chooseDirectory(chooser, jarList,
                                        "Select the directory where the VisAD jar file should be installed");
        if (jarInstallDir == null) {
          step--;
        } else if (jarInstallDir.canWrite()) {
          step++;
        } else {
          JOptionPane.showMessageDialog(null,
                                        "Cannot write to that directory!",
                                        "Bad directory?",
                                        JOptionPane.ERROR_MESSAGE);
        }
        break;
      case STEP_DOWNLOAD_JAR:
        if (installerJar == null) {
          downloadLatestJar = true;
        } else {
          String djMsg = "Would you like to download the latest " +
            JAR_NAME + "?";

          int n = JOptionPane.showConfirmDialog(null, djMsg,
                                                "Download latest " +
                                                JAR_NAME + "?",
                                                JOptionPane.YES_NO_OPTION);
          downloadLatestJar = (n == JOptionPane.YES_OPTION);
        }
        step++;
        break;
      case STEP_CLUSTER:
        if (cPushStr == null) {
          clusterInstaller = null;
        } else {
          String cMsg =
            "Would you like to push everything out to the cluster?";

          int n = JOptionPane.showConfirmDialog(null, cMsg,
                                                "Push files to cluster?",
                                                JOptionPane.YES_NO_OPTION);
          if (n == JOptionPane.YES_OPTION) {
            clusterInstaller = new ClusterInstaller(cPushStr);
          }
        }
        step++;
        break;
      }
    }
  }

  public static final void main(String[] args)
  {
    new Main(args);
    System.exit(0);
  }
}
