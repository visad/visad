package visad.install;

import java.io.File;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Path
{
  private String[] path;

  public Path(String pathStr)
    throws IllegalArgumentException
  {
    if (pathStr == null) {
      throw new IllegalArgumentException("Null path string");
    }
    if (pathStr.length() == 0) {
      throw new IllegalArgumentException("Empty path string");
    }

    StringTokenizer tok = new StringTokenizer(pathStr, File.pathSeparator);

    final int numTokens = tok.countTokens();
    if (numTokens == 0) {
      throw new IllegalArgumentException("Empty path string");
    }

    path = new String[numTokens];

    int i = 0;
    while (tok.hasMoreTokens()) {
      path[i++] = tok.nextToken();
    }
  }

  public File[] find(String file)
  {
    if (file == null || file.length() == 0) {
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

  public File[] findMatch(String file)
  {
    if (file == null || file.length() == 0) {
      return null;
    }

    ArrayList list = null;
    for (int i = 0; i < path.length; i++) {
      if (path[i].endsWith(File.separator + file)) {
        File f = new File(path[i]);
        if (f.exists()) {
          if (list == null) {
            list = new ArrayList();
          }

          list.add(f);
        }
      }
    }

    if (list == null) {
      return null;
    }

    return (File[] )list.toArray(new File[list.size()]);
  }

  public String toString()
  {
    if (path == null || path.length == 0) {
      return null;
    }

    StringBuffer buf = new StringBuffer(path[0]);
    for (int i = 1; i < path.length; i++) {
      buf.append(File.pathSeparator);
      buf.append(path[i]);
    }

    return buf.toString();
  }

  public static final void main(String[] args)
    throws IllegalArgumentException
  {
    if (args.length < 2) {
      System.err.println("Usage: java Path pathString file [file ...]");
      System.exit(1);
      return;
    }

    Path p = new Path(args[0]);
    for (int i = 1; i < args.length; i++) {
      File[] f = p.findMatch(args[i]);
      if (f == null) {
        f = p.find(args[i]);
        if (f == null) {
          System.err.println("Couldn't find \"" + args[i] + "\"");
          continue;
        }
      }

      System.out.println(args[i] + ":");
      for (int j = 0; j < f.length; j++) {
        System.out.println("  " + f[j]);
      }
    }

    System.exit(0);
  }
}
