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

  private final File chooseInstallDirectory()
  {
    System.out.println("Ask where supplied JVM should be installed");
    return null;
  }

  private final File chooseJarDirectory()
  {
    System.out.println("Ask where visad.jar should be installed");
    return null;
  }

  private final File chooseJVM()
  {
    System.out.println("Ask user which JVM should be used");
    return null;
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
    final int STEP_FINISHED = 3;

    dumpInitialState();

    boolean useSuppliedJava = false;
    File jvmToUse, javaInstallDir, jarInstallDir;
    jvmToUse = javaInstallDir = jarInstallDir = null;

    int step = 0;
    while (step < STEP_FINISHED) {
      switch (step) {
      case STEP_USE_SUPPLIED:
        useSuppliedJava = installSuppliedJava(installerJava);
        step++;
        break;
      case STEP_INSTALL_JAVA:
        javaInstallDir = jvmToUse = null;
        if (useSuppliedJava) {
          javaInstallDir = chooseInstallDirectory();
          if (javaInstallDir == null) {
            step--;
          } else {
            step++;
          }
        } else {
          jvmToUse = chooseJVM();
          if (jvmToUse == null) {
            step--;
          } else {
            step++;
          }
        }
        break;
      case STEP_INSTALL_JAR:
        jarInstallDir = chooseJarDirectory();
        if (jarInstallDir == null) {
          step--;
        } else {
          step++;
        }
        break;
      }
    }
  }

  /**
   * Ask user if they'd like to install the supplied JDK.
   *
   * @param java the JavaFile object for the supplied JDK
   *
   * @return <tt>true</tt> if the supplied JDK should be installed.
   */
  private static final boolean installSuppliedJava(JavaFile java)
  {
    if (java == null) {
      return false;
    }

    String msg = "Would you like to install the supplied " +
      " Java Development Kit " + java.getMajor() + "." + java.getMinor() +
      " (" + java.getFullString() + ")?";

    int n = JOptionPane.showConfirmDialog(null, msg, "Install supplied JDK?",
                                          JOptionPane.YES_NO_OPTION);
    return (n == JOptionPane.YES_OPTION);
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
