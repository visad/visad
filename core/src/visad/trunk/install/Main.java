package visad.install;

import java.io.File;

public class Main
{
  private static final String CLASSPATH_PROPERTY = "java.class.path";
  private static final String PATH_PROPERTY = "visad.install.path";

  Path classpath, path;
  File[] jarList, javaList;

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
      jarList = loseDuplicates(jarList);
      for (int i = 0; i < jarList.length; i++) {
        System.out.println("#"+i+": "+jarList[i]);
      }
    }

    if (javaList == null) {
      System.err.println("No java executable found in " + path);
    } else {
      for (int i = 0; i < javaList.length; i++) {
        System.out.println("#"+i+": "+javaList[i]);
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

    File[] tmpList = path.find("java");
    if (tmpList != null) {
      javaList = checkJavaVersions(loseDuplicates(tmpList), 1, 2);
    }

    return true;
  }

  private static final File[] checkJavaVersions(File[] list,
                                                int major, int minor)
  {
    // check all java executables for minimum version
    int deleted = 0;
    for (int i = 0; i < list.length; i++) {
      if (!JavaVersion.matchMinimum(list[i], major, minor)) {
        list[i] = null;
        deleted++;
      }
    }

    return rebuildList(list, deleted);
  }

  private static final File[] loseDuplicates(File[] list)
  {
    final int listLen = list.length;

    int deleted = 0;
    for (int i = 0; i < listLen; i++) {
      if (list[i] == null) {
        continue;
      }

      for (int j = i+1; j < listLen; j++) {
        if (list[j] == null || !list[i].equals(list[j])) {
          continue;
        }

        // mark this entry for deletion
        list[j] = null;
        deleted++;
      }
    }

    return rebuildList(list, deleted);
  }

  private static final File[] rebuildList(File[] list, int deleted)
  {
    // if everything matched, we're done
    if (deleted == 0) {
      return list;
    }

    // if everything was deleted, set list to null
    if (deleted == list.length) {
      return null;
    }

    // build a new list containing the valid entries
    File[] newList = new File[list.length - deleted];
    for (int i = 0, j = 0; i < list.length; i++) {
      if (list[i] != null) {
        newList[j++] = list[i];
      }
    }

    return newList;
  }

  public static final void main(String[] args)
  {
    new Main(args);
    System.exit(0);
  }
}
