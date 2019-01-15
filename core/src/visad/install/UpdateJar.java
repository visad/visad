/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.install;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;

import visad.util.CmdlineGenericConsumer;
import visad.util.CmdlineParser;

public class UpdateJar
  extends CmdlineGenericConsumer
{
  private static final String CLASSPATH_PROPERTY = "java.class.path";

  // XXX TJJ where does the distribution live as of 2012?
  // I doubt this app is still used, but if so, it should at
  // least get the correct, current, production .jar!
  private static final String VISAD_JAR_URL =
    "ftp://ftp.ssec.wisc.edu/pub/visad-2.0/visad.jar";

  private File installJar;
  private URL jarURL;
  private boolean verbose;

  public UpdateJar(String[] args)
  {
    CmdlineParser cmdline = new CmdlineParser(this);
    if (!cmdline.processArgs(args)) {
      System.exit(1);
      return;
    }

    Download.getFile(jarURL, installJar, verbose);
  }

  public int checkKeyword(String mainName, int thisArg, String[] args)
  {
    // try to convert argument to a URL
    URL tmpURL;
    try {
      tmpURL = new URL(args[thisArg]);
    } catch (MalformedURLException mfe) {
      // must not be a URL
      tmpURL = null;
    }

    // if it's a URL...
    if (tmpURL != null) {
      if (jarURL != null) {
        System.err.println(mainName + ": Too many URLs specified!");
        return -1;
      }

      // save the URL
      jarURL = tmpURL;
      return 1;
    }

    // whine if we've already got a jar directory/file
    if (installJar != null) {
      System.err.println(mainName +
                         ": Too many jar install directories specified!");
      return -1;
    }

    final int thisLen = args[thisArg].length();
    final int suffixLen = 4;

    installJar = new File(args[thisArg]);

    File dir = installJar;

    if (thisLen > suffixLen) {
      String suffix = args[thisArg].substring(thisLen - suffixLen, thisLen);
      if (suffix.toLowerCase().equals(".jar")) {
        dir = new File(installJar.getParent());
      }
    }

    if (!dir.exists()) {
      System.err.println(mainName + ": Directory \"" + dir +
                         "\" does not exist!");
      installJar = null;
      return -1;
    }

    if (!dir.isDirectory()) {
      System.err.println(mainName + ": \"" + dir + "\" is not a directory!");
      installJar = null;
      return -1;
    }

    if (!dir.canWrite()) {
      System.err.println(mainName + ": Cannot write to directory \"" + dir +
                         "\"!");
      installJar = null;
      return -1;
    }

    if (installJar != dir && installJar.exists() && !installJar.canWrite()) {
      System.err.println(mainName + ": Cannot write to jar file \"" +
                         installJar + "\"!");
      installJar = null;
      return -1;
    }

    return 1;
  }

  public int checkOption(String mainName, char ch, String arg)
  {
    if (ch == 'v') {
      verbose = true;
      return 1;
    }

    return 0;
  }

  public boolean finalizeArgs(String mainName)
  {
    // build the URL for the jar file
    if (jarURL == null) {
      try {
        jarURL = new URL(VISAD_JAR_URL);
      } catch (java.net.MalformedURLException mue) {
        System.err.println(mainName + ": Couldn't build URL from \"" +
                           VISAD_JAR_URL + "\"");
        return false;
      }
    }

    // if user didn't specify a jar file...
    if (installJar == null) {

      File urlFile = new File(jarURL.getFile());

      // ...search for one in their classpath
      installJar = findJar(urlFile.getName());
    }

    // if no jar file was found, whine and quit
    if (installJar == null) {
      System.err.println(mainName + ": Couldn't determine" +
                         " where jar file should be installed");
      return false;
    }

    return true;
  }

  private File findJar(String jarName)
  {
    Path classpath;

    // get class path elements
    try {
      classpath = new Path(System.getProperty(CLASSPATH_PROPERTY));
    } catch (IllegalArgumentException iae) {
      System.err.println(getClass().getName() +
                         ": Couldn't get Java class path");
      return null;
    }

    // find all visad jar files
    ArrayList jarList = classpath.findMatch(jarName);
    if (jarList == null || jarList.size() == 0) {
      return null;
    }

    // use first valid jar file
    return (File )jarList.get(0);
  }

  public void initializeArgs()
  {
    installJar = null;
    jarURL = null;
  }

  public String keywordUsage() { return " [url] [jarLocation]"; }
  public String optionUsage() { return " [-v(erbose)]"; }

  public static final void main(String[] args)
  {
    new UpdateJar(args);
    System.exit(0);
  }
}
