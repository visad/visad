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
import visad.RemoteDisplay;
import visad.RemoteDisplayImpl;
import visad.RemoteServer;
import visad.RemoteServerImpl;
import visad.VisADException;

import visad.java2d.DisplayImplJ2D;

import visad.java3d.DisplayImplJ3D;

public abstract class TestSkeleton
  extends Thread
{
  boolean startServer = false;
  String hostName = null;
  RemoteServer client = null;

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
                         " [-d(ump display)]" +
                         (hasClientServerMode() ?
                          " [-s(erver)]" : "") +
                         extraOptionUsage() + extraKeywordUsage());
    }

    return !usage;
  }

  abstract DisplayImpl[] setupData()
    throws RemoteException, VisADException;

  String getClientServerTitle()
  {
    if (startServer) {
      if (hostName == null) {
        return " server";
      } else {
        return " server+client";
      }
    } else {
      if (hostName == null) {
        return " standalone";
      } else {
        return " client";
      }
    }
  }

  void getClientDataReferences()
    throws RemoteException
  {
  }

  RemoteDisplay[] getClientDisplays()
    throws RemoteException, VisADException
  {
    int loops = 0;
    RemoteDisplay[] rmtDpy = null;
    while (rmtDpy == null && loops < maximumWaitTime) {

      // try to reconnect to the server after the first loop
      if (loops > 0) {
        try {
          String domain = "//" + hostName + "/" + getClass().getName();
          client = (RemoteServer )Naming.lookup(domain);
        } catch (NotBoundException nbe) {
          client = null;
        } catch (Exception e) {
          throw new VisADException ("Cannot connect to server on \"" +
                                    hostName + "\" (" +
                                    e.getClass().getName() + ": " +
                                    e.getMessage() + ")");
        }
      }

      // try to get displays from remote server
      try {
        if (client != null) {
          rmtDpy = client.getDisplays();
        }
      } catch (java.rmi.ConnectException ce) {
        rmtDpy = null;
      }

      // if we didn't get any displays, print a message and wait a bit
      if (rmtDpy == null) {
        if (loops == 0) {
          System.err.print("Client waiting for server ");
        } else {
          System.err.print(".");
        }

        try { sleep(1000); } catch (InterruptedException ie) { }

        loops++;
      }
    }

    if (loops == maximumWaitTime) {
      System.err.println(" giving up!");
      System.exit(1);
    } else if (loops > 0) {
      System.err.println(" connected");
    }

    return rmtDpy;
  }

  DisplayImpl[] setupClientData()
    throws RemoteException, VisADException
  {
    RemoteDisplay[] rmtDpy = getClientDisplays();
    if (rmtDpy == null) {
      throw new VisADException("No RemoteDisplays found!");
    }

    DisplayImpl[] dpys = new DisplayImpl[rmtDpy.length];
    for (int i = 0; i < dpys.length; i++) {
      String className = rmtDpy[i].getDisplayClassName();
      Class dpyClass;
      try {
        dpyClass = Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new VisADException("Couldn't create " + className);
      }

      Class[] params = new Class[1];
      try {
        params[0] = Class.forName("visad.RemoteDisplay");
      } catch (ClassNotFoundException e) {
        throw new VisADException("Yikes! Couldn't find visad.RemoteDisplay!");
      }

      java.lang.reflect.Constructor cons;
      try {
        cons = dpyClass.getConstructor(params);
      } catch (NoSuchMethodException e) {
        throw new VisADException(className + " has no RemoteDisplay" +
                                 " constructor");
      }

      Object[] cargs = new Object[1];
      cargs[0] = rmtDpy[i];
      try {
        dpys[i] = (DisplayImpl )cons.newInstance(cargs);
      } catch (Exception e) {
        throw new VisADException("Couldn't create local shadow for " +
                                 rmtDpy[i]);
      }
    }

    // add any data references to server
    getClientDataReferences();

    return dpys;
  }

  void setServerDataReferences(RemoteServerImpl server)
    throws RemoteException
  {
  }

  RemoteServerImpl setupServer(DisplayImpl[] dpys)
    throws RemoteException, VisADException
  {
    // create new server
    RemoteServerImpl server;
    boolean registryStarted = false;
    while (true) {
      boolean success = true;
      try {
        server = new RemoteServerImpl();
        String domain = "//:/" + getClass().getName();
        Naming.rebind(domain, server);
        break;
      } catch (java.rmi.ConnectException ce) {
        if (!registryStarted) {
          LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
          registryStarted = true;
        } else {
          success = false;
        }
      } catch (Exception e) {
        success = false;
      }
      if (!success) {
        throw new VisADException("Cannot set up server" +
                                 " (rmiregistry may not be running)");
      }
    }

    // add all displays to server
    if (dpys != null) {
      for (int i = 0; i < dpys.length; i++) {
        server.addDisplay(new RemoteDisplayImpl(dpys[i]));
      }
    }

    // add any data references to server
    setServerDataReferences(server);

    return server;
  }

  void setupUI(DisplayImpl[] dpys)
    throws RemoteException, VisADException
  {
  }

  public void startThreads()
    throws RemoteException, VisADException
  {
    DisplayImpl[] displays;
    if (hostName != null) {
      displays = setupClientData();
    } else {
      displays = setupData();
    }

    if (startServer) {
      setupServer(displays);
    }

    setupUI(displays);
  }

  public String toString()
  {
    return null;
  }
}
