package visad.install;

import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JavaVersion
{
  private static final String[] regexpStr = new String[] {
    "^java version \"(\\d+)\\.(\\d+)\\.",
    "^java version \"HP-UX Java [A-Z]\\.\\d+\\.(\\d)(\\d+)\\.",
  };

  private static RE[] regexp = null;

  private static Runtime runtime = null;

  private static final int getInt(String intStr)
  {
    try {
      return Integer.parseInt(intStr);
    } catch (NumberFormatException nfe) {
    }

    return -1;
  }

  public static final boolean matchMinimum(File java,
                                           int major, int minor)
  {
    if (regexp == null) {
      synchronized (regexpStr) {
        regexp = new RE[regexpStr.length];

        int bad = 0;
        for (int i = 0; i < regexpStr.length; i++) {
          try {
            regexp[i] = new RE(regexpStr[i]);
          } catch (REException ree) {
            System.err.println("For regexp \"" + regexpStr[i] + "\":");
            ree.printStackTrace();
            bad++;
            regexp[i] = null;
          }
        }

        if (bad > 0) {
          RE[] tmpRE = new RE[regexp.length - bad];
          for (int i = 0, j = 0; i < regexp.length; i++) {
            if (regexp[i] != null) {
              tmpRE[j++] = regexp[i];
            }
          }
          regexp = tmpRE;
        }
      }
    }

    if (runtime == null) {
      runtime = Runtime.getRuntime();
    }

    Process proc;
    try {
      proc = runtime.exec(java + " -version");
    } catch (IOException ioe) {
      System.err.println("While running '" + java + " -version':");
      ioe.printStackTrace();
      return false;
    }

    InputStream is = proc.getErrorStream();
    BufferedReader in = new BufferedReader(new InputStreamReader(is));

    while (true) {
      String line;
      try {
        line = in.readLine();
      } catch (IOException ioe) {
        ioe.printStackTrace();
        break;
      }

      if (line == null) {
        break;
      }

      for (int i = 0; i < regexp.length; i++) {
        REMatch match = regexp[i].getMatch(line);
        if (match != null) {
          int tmp = getInt(line.substring(match.getSubStartIndex(1),
                                          match.getSubEndIndex(1)));
          if (tmp < major) {
            continue;
          }

          if (tmp == major) {
            tmp = getInt(line.substring(match.getSubStartIndex(2),
                                        match.getSubEndIndex(2)));
            if (tmp < minor) {
              continue;
            }
          }

          // found a match!
          return true;
        }
      }
    }

    return false;
  }
}
