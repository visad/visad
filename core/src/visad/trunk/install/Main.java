package visad.install;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;

public class Main
{
  private static final String CLASSPATH_PROPERTY = "java.class.path";
  private static final String PATH_PROPERTY = "visad.install.path";

  Path classpath, path;
  ArrayList jarList, javaList;

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

    if (jarList == null) {
      System.err.println("No visad.jar found in " + classpath);
    } else {
      for (int i = 0; i < jarList.size(); i++) {
        System.out.println("#"+i+": "+jarList.get(i));
      }
    }

    if (javaList == null) {
      System.err.println("No java executable found in " + path);
    } else {
      for (int i = 0; i < javaList.size(); i++) {
        System.out.println("#"+i+": "+javaList.get(i));
      }
    }
  }

  private boolean initialize()
  {
    try {
      classpath = new Path(System.getProperty(CLASSPATH_PROPERTY));
    } catch (IllegalArgumentException iae) {
      System.err.println(getClass().getName() +
                         ": Couldn't get Java class path");
      return false;
    }

    try {
      path = new Path(System.getProperty(PATH_PROPERTY));
    } catch (IllegalArgumentException iae) {
      System.err.println(getClass().getName() +
                         ": Please pass in the executable path via the '" +
                         PATH_PROPERTY + "' property");
      return false;
    }

    jarList = classpath.findMatch("visad.jar");
    if (jarList == null) {
      jarList = classpath.find("visad.jar");
    }
    if (jarList != null) {
      loseDuplicates(jarList);
    }

    javaList = path.find("java");
    if (javaList != null) {
      loseDuplicates(javaList);
      checkJavaVersions(javaList, 1, 2);
    }

    return true;
  }

  private static final void checkJavaVersions(ArrayList list,
                                              int major, int minor)
  {
    // check all java executables for minimum version
    Iterator iter = list.iterator();
    while (iter.hasNext()) {
      if (!JavaVersion.matchMinimum((File )iter.next(), major, minor)) {
        iter.remove();
      }
    }
  }

  private static final void loseDuplicates(ArrayList list)
  {
    Iterator iter = list.iterator();
    int iterIndex = 0;

    while (iter.hasNext()) {
      Object obj = iter.next();

      int j = iterIndex + 1;
      while (j < list.size()) {
        if (obj.equals(list.get(j))) {
          // delete this entry
          iter.remove();
          iterIndex--;
          break;
        }

        j++;
      }

      iterIndex++;
    }
  }

  public static final void main(String[] args)
  {
    new Main(args);
    System.exit(0);
  }
}
