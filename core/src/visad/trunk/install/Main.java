package visad.install;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JWindow;

public class Main
{
  private static final String CLASSPATH_PROPERTY = "java.class.path";
  private static final String PATH_PROPERTY = "visad.install.path";

  private Path classpath, path;
  private ArrayList jarList, javaList;
  private JavaFile installerJava;

  public Main(String[] args)
  {
    classpath = path = null;
    jarList = javaList = null;

    SplashScreen ss = new SplashScreen("visad-splash.jpg");
    ss.setVisible(true);

    boolean initResult = initialize();

    ss.setVisible(false);

    if (!initResult) {
      System.exit(1);
      return;
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

  private final static File chooseDirectory(ArrayList list, String title)
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

    ChooserList chooser;
    if (allDirs) {
      chooser = new ChooserList(list);
    } else {
      // build list containing only files
      File[] fList = new File[listLen];
      for (int i = 0; i < listLen; i++) {
        File f = (File )list.get(i);
        if (f.isDirectory()) {
          fList[i] = f;
        } else {
          fList[i] = new File(f.getParent());
        }
      }

      chooser = new ChooserList(fList);
    }

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

  private static final File chooseFile(ArrayList list, String title)
  {
    ChooserList chooser = new ChooserList(list);
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
    if (installerJava != null) {
      System.out.println("Supplied java: " + installerJava);
    }

    if (jarList == null) {
      System.err.println("No visad.jar found in " + classpath);
    } else {
      for (int i = 0; i < jarList.size(); i++) {
        System.out.println("#" + i + ": " +
                           getPath((File )jarList.get(i)));
      }
    }

    if (javaList == null) {
      System.err.println("No java executable found in " + path);
    } else {
      for (int i = 0; i < javaList.size(); i++) {
        System.out.println("#" + i + ": " +
                           getPath((File )javaList.get(i)));
      }
    }
  }

  private static final void dumpInstallState(boolean useSuppliedJava,
                                             File javaInstallDir,
                                             File jvmToUse,
                                             File jarInstallDir,
                                             boolean downloadLatestJar)
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
      System.err.println("Download latest visad.jar");
    }
    System.err.println("Install visad.jar in " + jarInstallDir);
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
  private static final JavaFile extractInstallerJava(ArrayList javaList)
  {
    String curDir = getPath(new File("."));

    java.util.Iterator iter = javaList.iterator();
    while (iter.hasNext()) {
      JavaFile thisFile = (JavaFile )iter.next();
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

    // find all visad jar files
    jarList = classpath.findMatch("visad.jar");
    if (jarList == null) {
      jarList = classpath.find("visad.jar");
    }
    if (jarList != null) {
      loseDuplicates(jarList);
    }

    // find all java executables
    javaList = path.find("java");
    if (javaList != null) {
      loseDuplicates(javaList);
      checkJavaVersions(javaList, 1, 2);
      installerJava = extractInstallerJava(javaList);
    }

    return true;
  }

  /**
   * Install VisAD.
   */
  private final void install()
  {
    final int STEP_USE_SUPPLIED = 0;
    final int STEP_INSTALL_JAVA = 1;
    final int STEP_INSTALL_JAR = 2;
    final int STEP_DOWNLOAD_JAR = 3;
    final int STEP_FINISHED = 4;

    dumpInitialState();

    boolean useSuppliedJava, downloadLatestJar;
    useSuppliedJava = downloadLatestJar = false;

    File jvmToUse, javaInstallDir, jarInstallDir;
    jvmToUse = javaInstallDir = jarInstallDir = null;

    int step = 0;
    while (step < STEP_FINISHED) {
      switch (step) {
      case STEP_USE_SUPPLIED:
        if (installerJava == null) {
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
          javaInstallDir = chooseDirectory(null, "Select the directory in which the JDK should be installed");
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
          jvmToUse = chooseFile(javaList, "Select the java program to use");
          if (jvmToUse == null) {
            step--;
          } else {
            step++;
          }
        }
        break;
      case STEP_INSTALL_JAR:
        jarInstallDir = chooseDirectory(jarList, "Select the directory where the VisAD jar file should be installed");
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
        String djMsg = "Would you like to download the latest visad.jar?";

        int n = JOptionPane.showConfirmDialog(null, djMsg,
                                              "Download latest visad.jar?",
                                              JOptionPane.YES_NO_OPTION);
        downloadLatestJar = (n == JOptionPane.YES_OPTION);
        step++;
        break;
      }
    }

    dumpInstallState(useSuppliedJava, javaInstallDir, jvmToUse, jarInstallDir,
                     downloadLatestJar);
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

  public static final void main(String[] args)
  {
    new Main(args);
    System.exit(0);
  }
}
