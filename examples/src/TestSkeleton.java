/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.logging.Level;
import visad.DataReference;
import visad.DisplayImpl;
import visad.LocalDisplay;
import visad.RemoteDisplayImpl;
import visad.RemoteServer;
import visad.RemoteServerImpl;
import visad.RemoteSourceListener;
import visad.VisADException;

import visad.util.ClientServer;
import visad.util.CmdlineConsumer;
import visad.util.CmdlineParser;
import visad.util.Util;

public abstract class TestSkeleton
  extends Thread
  implements CmdlineConsumer, RemoteSourceListener
{
  boolean startServer;
  String hostName;

  private static final int maximumWaitTime = 60;
  private int verbosity = 0;

  private CmdlineParser cmdline;

  public TestSkeleton()
  {
    cmdline = new CmdlineParser(this);
  }

  public TestSkeleton(String[] args)
    throws RemoteException, VisADException
  {
    this();

    if (!processArgs(args)) {
      System.err.println("Exiting...");
      System.exit(1);
    }
    startThreads();
  }

  boolean hasClientServerMode() { return true; }

  public void initializeArgs() { startServer = false; hostName = null; }

  public int checkOption(String mainName, char ch, String arg)
  {
    if (ch == 'c') {
      if (arg == null) {
        System.err.println(mainName + ": Missing hostname for \"-c\"");
        return -1;
      }

      if (!hasClientServerMode()) {
        System.err.println("Client/server mode not supported" +
                           " for " + mainName);
        return -1;
      }

      if (startServer) {
        System.err.println(mainName +
                           ": Cannot specify both '-c' and '-s'!");
        return -1;
      }

      hostName = arg;
      return 2;
    }

    if (ch == 's') {
      if (hostName != null) {
        System.err.println(mainName +
                           ": Cannot specify both '-c' and '-s'!");
        return -1;
      }

      if (!hasClientServerMode()) {
        System.err.println("Client/server mode not supported" +
                           " for " + mainName);
        return -1;
      }

      startServer = true;
      return 1;
    }

    if (ch == 'v') {
      verbosity++;
      return 1;
    }

    return 0;
  }      

  public String optionUsage()
  {
    if (hasClientServerMode()) {
      return " [-c(lient) hostname] [-s(erver)]";
    }

    return "";
  }

  public int checkKeyword(String mainName, int argc, String[] args)
  {
    return 0;
  }

  public String keywordUsage() { return ""; }

  public boolean finalizeArgs(String mainName) {
    Util.configureLogging(verbosity);
    return true;
  }

  boolean processArgs(String[] args) { return cmdline.processArgs(args); }

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

  DataReference[] getClientDataReferences()
    throws RemoteException, VisADException
  {
    return null;
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
    // build local data references
    DataReference[] refs = getClientDataReferences();

    RemoteServer client;
    try {
      client = ClientServer.connectToServer(hostName, getClass().getName(),
                                            true);
    } catch (VisADException ve) {
      System.err.println(ve.getMessage());
      System.exit(1);
      client = null;
    }

    LocalDisplay[] dpys = ClientServer.getClientDisplays(client, refs);
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
