package visad.install;

import java.io.File;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Main
{
  private static final String PATH_PROPERTY = "visad.install.path";

  public Main(String[] args)
  {
    SplashScreen ss = new SplashScreen("visad-splash.jpg");
    ss.setVisible(true);

    String[] path = getPath();
    if (path == null) {
      System.err.println(getClass().getName() +
                         ": Please pass in the executable path via the '" +
                         PATH_PROPERTY + "' property");
      System.exit(1);
      return;
    }

    File[] javaList = findFile("java", path);
    if (javaList == null) {
      System.err.println("No java executable found?");
    } else {
      for (int i = 0; i < javaList.length; i++) {
        System.out.println("#"+i+": "+javaList[i]);
      }
    }

    ss.setVisible(false);
  }

  private static File[] findFile(String file, String[] path)
  {
    if (file == null || file.length() == 0 ||
        path == null || path.length == 0)
    {
      return null;
    }

    ArrayList list = null;
    for (int i = 0; i < path.length; i++) {
      File f = new File(path[i], file);
      if (f.isFile()) {
        if (list == null) {
          list = new ArrayList();
        }

        list.add(f);
      }
    }

    if (list == null) {
      return null;
    }

    return (File[] )list.toArray(new File[list.size()]);
  }

  private static String[] getPath()
  {
    String path = System.getProperty(PATH_PROPERTY);
    if (path == null || path.length() == 0) {
      return null;
    }

    String delim = System.getProperty("path.separator");

    StringTokenizer tok = new StringTokenizer(path, delim);

    final int numTokens = tok.countTokens();
    if (numTokens == 0) {
      return null;
    }

    String[] list = new String[numTokens];

    int i = 0;
    while (tok.hasMoreTokens()) {
      list[i++] = tok.nextToken();
    }

    return list;
  }

  public static final void main(String[] args)
  {
    new Main(args);
  }
}
