/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import visad.DisplayImpl;
import visad.LocalDisplay;
import visad.RemoteDisplay;
import visad.RemoteDisplayImpl;
import visad.RemoteServer;
import visad.RemoteServerImpl;
import visad.VisADException;

import visad.java2d.DisplayImplJ2D;

import visad.java3d.DisplayImplJ3D;

import visad.util.ClientServer;

public abstract class TestSkeleton
  extends Thread
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

  int checkExtraOption(char ch, int argc, String[] args)
  {
    return 0;
  }

  String extraOptionUsage() { return ""; }

  int checkExtraKeyword(int argc, String[] args)
  {
    return 0;
  }

  String extraKeywordUsage() { return ""; }

  boolean hasClientServerMode() { return true; }

  public boolean processArgs(String[] args)
  {
    boolean usage = false;

    for (int argc = 0; argc < args.length; argc++) {
      if (args[argc].startsWith("-") && args[argc].length() == 2) {
        if (argc >= args.length) {
          System.err.println("Missing argument for \"" + args[argc] + "\"\n");
          usage = true;
        } else {
          char ch = args[argc].charAt(1);

          String str, result;

          switch (ch) {
          case 'c':
            if (startServer) {
              System.err.println("Cannot specify both '-c' and '-s'!");
              usage = true;
            } else {
              ++argc;
              if (argc >= args.length) {
                System.err.println("Missing hostname for '-c'");
                usage = true;
              } else if (!hasClientServerMode()) {
                System.err.println("Client/server mode not supported" +
                                   " for this test");
                usage = true;
              } else {
                hostName = args[argc];
              }
            }
            break;
          case 's':
            if (hostName != null) {
              System.err.println("Cannot specify both '-c' and '-s'!");
              usage = true;
            } else {
              if (!hasClientServerMode()) {
                System.err.println("Client/server mode not supported" +
                                   " for this test");
                usage = true;
              } else {
                startServer = true;
              }
            }
            break;
          default:
            int handled = checkExtraOption(ch, argc+1, args);
            if (handled > 0) {
              argc += (handled - 1);
            } else {
              System.err.println(getClass().getName() +
                                 ": Unknown option \"-" + ch + "\"");
              usage = true;
            }
            break;
          }
        }
      } else {
        int handled = checkExtraKeyword(argc, args);
        if (handled > 0) {
          argc += (handled - 1);
        } else {
          System.err.println(getClass().getName() + ": Unknown keyword \"" +
                             args[argc] + "\"");
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

  void getClientDataReferences(RemoteServer client)
    throws RemoteException, VisADException
  {
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

    // fetch any data references from server
    getClientDataReferences(client);

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
