/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;


import visad.DisplayImpl;
import visad.LocalDisplay;
import visad.RemoteDisplayImpl;
import visad.RemoteServer;
import visad.RemoteServerImpl;
import visad.RemoteSourceListener;
import visad.VisADException;

import visad.util.ClientServer;

public abstract class TestSkeleton
  extends Thread
  implements RemoteSourceListener
{
  boolean startServer = false;
  String hostName = null;

  private static final int maximumWaitTime = 60;

  public TestSkeleton() { }

  public TestSkeleton(String[] args)
    throws RemoteException, VisADException
  {
    if (!processArgs(args)) {
      System.err.println("Exiting...");
      System.exit(1);
    }
    startThreads();
  }

  boolean hasClientServerMode() { return true; }

  /**
   * Method used to initialize any instance variables which may be
   * changed by a cmdline option.<br>
   * <br>
   * This is needed because arguments are processed inside the
   * constructor.  This means that the first line in the constructor
   * of classes which extend this class will be <tt>super(args)</tt>,
   * which gets run <em>before</em> any instance variables for that
   * class are initialized.
   */
  void initializeArgs() { }

  /**
   * Handle subclass-specific command line options and their arguments.<br>
   * <br>
   * If <tt>-abc -d efg -h -1 -i</tt> is specified, this
   * method will be called a maximum of 5 times:<ul>
   * <li><tt>checkExtraOption(progName, 'a', "bc");</tt>
   * <li><tt>checkExtraOption(progName, 'd', "efg");</tt>
   * <li><tt>checkExtraOption(progName, 'h', "-1");</tt>
   * <li><tt>checkExtraOption(progName, '1', "-i");</tt>
   * <li><tt>checkExtraOption(progName, 'i', null);</tt>
   * </ul>
   * <br>
   * Note that either of the last two method calls may not
   * happen if the preceeding method call claims to have used
   * the following argument (by returning <tt>2</tt>.<br>
   * <br>
   * For example,
   * if the third call (where <tt>ch</tt> is set to <tt>'h'</tt>)
   * returns <tt>0</tt> or <tt>1</tt>, the next call will contain
   * <tt>'1'</tt> and <tt>"-i"</tt>.  If, however, the third call
   * returns <tt>2</tt>, the next call will contain <tt>'i'</tt>
   * and <tt>null</tt>.
   *
   * @param progName The name of the original program (useful for
   *                 error messages.
   * @param ch Option character.  If <tt>-a</tt> is specified
   *           on the command line, <tt>'a'</tt> would be passed to
   *           this method.)
   * @param arg The argument associated with this option.
   *
   * @return less than <tt>0</tt> to indicate an error<br>
   *         <tt>0</tt> to indicate that this option is not used by this
   *         class<br>
   *         <tt>1</tt> to indicate that only the option was used<br>
   *         <tt>2</tt> or greater to indicate that both the option and the
   *         argument were used
   */
  int checkExtraOption(String progName, char ch, String arg)
  {
    return 0;
  }

  /**
   * A short string included in the usage message to indicate
   * valid options.  An example might be <tt>"[-t type]"</tt>.
   *
   * @return A <em>very</em> terse description string.
   */
  String extraOptionUsage() { return ""; }

  /**
   * Handle subclass-specific command line options and their arguments.
   *
   * @param progName The name of the original program (useful for
   *                 error messages.
   * @param thisArg The index of the current keyword.
   * @param args The full list of arguments.
   *
   * @return less than <tt>0</tt> to indicate an error<br>
   *         <tt>0</tt> to indicate that this argument is not used by this
   *         class<br>
   *         <tt>1 or more</tt> to indicate the number of arguments used<br>
   */
  int checkExtraKeyword(String progName, int thisArg, String[] args)
  {
    return 0;
  }

  /**
   * A short string included in the usage message to indicate
   * valid keywords.  An example might be <tt>"[username] [password]"</tt>.
   *
   * @return A <em>very</em> terse description string.
   */
  String extraKeywordUsage() { return ""; }

  boolean processArgs(String[] args)
  {
    boolean usage = false;

    String className = getClass().getName();
    int pt = className.lastIndexOf('.');
    final int ds = className.lastIndexOf('$');
    if (ds > pt) {
      pt = ds;
    }
    String progName = className.substring(pt == -1 ? 0 : pt + 1);

    initializeArgs();
    for (int i = 0; args != null && i < args.length; i++) {
      if (args[i].length() > 0 && args[i].charAt(0) == '-') {
        char ch = args[i].charAt(1);

        String str, result;

        switch (ch) {
        case 'c':
          str = (args[i].length() > 2 ? args[i].substring(2) :
                 ((i + 1) < args.length ? args[++i] : null));
          if (str == null) {
            System.err.println(progName + ": Missing hostname for \"-c\"");
            usage = true;
          } else if (!hasClientServerMode()) {
            System.err.println("Client/server mode not supported" +
                               " for " + progName);
            usage = true;
          } else if (startServer) {
            System.err.println(progName +
                               ": Cannot specify both '-c' and '-s'!");
            usage = true;
          } else {
            hostName = str;
          }
          break;
        case 's':
          if (hostName != null) {
            System.err.println(progName +
                               ": Cannot specify both '-c' and '-s'!");
            usage = true;
          } else if (!hasClientServerMode()) {
            System.err.println("Client/server mode not supported" +
                               " for " + progName);
            usage = true;
          } else {
            startServer = true;
          }
          break;
        default:
          boolean strInOption = false;
          if (args[i].length() > 2) {
            str = args[i].substring(2);
            strInOption = true;
          } else if ((i + 1) < args.length) {
            str = args[i+1];
          } else {
            str = null;
          }

          int handled = checkExtraOption(progName, ch, str);
          if (handled > 0) {
            if (handled > 1) {
              if (strInOption) {
                handled = 1;
              } else {
                handled = 2;
              }
            }
            i += (handled - 1);
          } else {
            System.err.println(progName + ": Unknown option \"-" + ch + "\"");
            usage = true;
          }
          break;
        }
      } else {
        int handled = checkExtraKeyword(progName, i, args);
        if (handled > 0) {
          i += (handled - 1);
        } else {
          System.err.println(progName + ": Unknown keyword \"" +
                             args[i] + "\"");
          usage = true;
        }
      }
    }

    if (usage) {
      System.err.println("Usage: " + getClass().getName() +
                         (hasClientServerMode() ?
                          " [-c(lient) hostname]" : "") +
                         (hasClientServerMode() ?
                          " [-s(erver)]" : "") +
                         extraOptionUsage() + extraKeywordUsage());
    }

    return !usage;
  }

  boolean isServer() { return (startServer && hostName == null); }
  boolean isClient() { return (!startServer && hostName != null); }
  boolean isStandalone() { return (!startServer && hostName == null); }

  String getClientServerTitle()
  {
    if (isServer()) {
      return " server";
    } else if (isClient()) {
      return " client";
    } else if (isStandalone()) {
      return " standalone";
    }
    return " unknown";
  }

  void finishClientSetup(RemoteServer client)
    throws RemoteException, VisADException
  {
  }

  public void dataSourceLost(String name)
  {
    System.err.println("Lost Data object \"" + name + "\"");
  }

  public void collabSourceLost(int connectionID)
  {
    System.err.println("Lost collaboration source #" + connectionID);
  }

  LocalDisplay[] setupClientData()
    throws RemoteException, VisADException
  {
    RemoteServer client;
    try {
      client = ClientServer.connectToServer(hostName, getClass().getName(),
                                            true);
    } catch (VisADException ve) {
      System.err.println(ve.getMessage());
      System.exit(1);
      client = null;
    }

    LocalDisplay[] dpys = ClientServer.getClientDisplays(client);
    if (dpys == null) {
      throw new VisADException("No remote displays found!");
    }

    for (int i = 0; i < dpys.length; i++) {
      ((DisplayImpl )dpys[i]).addRemoteSourceListener(this);
    }

    // fetch any data references from server
    finishClientSetup(client);

    return dpys;
  }

  abstract DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException;

  abstract void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException;

  void setServerDataReferences(RemoteServerImpl server)
    throws RemoteException, VisADException
  {
  }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
  }

  public void startThreads()
    throws RemoteException, VisADException
  {
    LocalDisplay[] local;
    if (isClient()) {
      local = setupClientData();
    } else {
      DisplayImpl[] displays = setupServerDisplays();

      RemoteServerImpl server;
      if (!startServer) {
        server = null;
      } else {
        server = ClientServer.startServer(getClass().getName());

        // add all displays to server
        if (displays != null) {
          for (int i = 0; i < displays.length; i++) {
            server.addDisplay(new RemoteDisplayImpl(displays[i]));
          }
        }
      }
      setServerDataReferences(server);

      local = displays;
      setupServerData(local);
    }

    setupUI(local);
  }

  public String toString()
  {
    return null;
  }
}
