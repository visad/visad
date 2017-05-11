/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.lang.reflect.InvocationTargetException;

import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import visad.DataReference;
import visad.DisplayImpl;
import visad.LocalDisplay;
import visad.RemoteDisplay;
import visad.RemoteServer;
import visad.RemoteServerImpl;
import visad.VisADException;

public class ClientServer
{
  private static final int maximumWaitTime = 60;

  public static RemoteServer connectToServer(String hostName,
                                             String serviceName)
    throws RemoteException, VisADException
  {
    return connectToServer(hostName, serviceName, false);
  }

  public static RemoteServer connectToServer(String hostName,
                                             String serviceName,
                                             boolean verbose)
    throws RemoteException, VisADException
  {
    RemoteServer client = null;
    String domain = "//" + hostName + "/" + serviceName;

    int loops = 0;
    while (client == null && loops < maximumWaitTime) {

      // try to reconnect to the server after the first loop
      if (loops > 0) {
        try {
          client = (RemoteServer )Naming.lookup(domain);
        } catch (NotBoundException nbe) {
          client = null;
        } catch (ConnectException ce) {
          client = null;
        } catch (Exception e) {
          throw new VisADException ("Cannot connect to server on \"" +
                                    hostName + "\" (" +
                                    e.getClass().getName() + ": " +
                                    e.getMessage() + ")");
        }
      }

      // try to get first display from remote server
      RemoteDisplay rmtDpy;
      try {
        if (client != null) {
          rmtDpy = client.getDisplay(0);
        }
      } catch (java.rmi.ConnectException ce) {
        client = null;
      }

      // if we didn't get the display, print a message and wait a bit
      if (client == null) {
        if (verbose) {
          if (loops == 0) {
            System.err.print("Client waiting for server ");
          } else {
            System.err.print(".");
          }
        }

        try { Thread.sleep(1000); } catch (InterruptedException ie) { }

        loops++;
      }
    }

    if (loops == maximumWaitTime) {
      if (verbose) {
        System.err.println(" giving up!");
      }
      throw new VisADException("Cannot connect to " + hostName +
                               ":" + serviceName);
    } else if (loops > 0) {
      if (verbose) {
        System.err.println(" connected");
      }
    }

    return client;
  }

  private static LocalDisplay wrapRemoteDisplay(RemoteDisplay rmtDpy)
    throws RemoteException, VisADException
  {
    String className = rmtDpy.getDisplayClassName();
    Class dpyClass;
    try {
      dpyClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new VisADException("Couldn't create " + className);
    }

    java.lang.reflect.Constructor cons;
    try {
      cons = dpyClass.getConstructor(new Class[] { RemoteDisplay.class });
    } catch (NoSuchMethodException e) {
      throw new VisADException(className + " has no RemoteDisplay" +
                               " constructor");
    }

    DisplayImpl dpy;

    Object[] cargs = new Object[1];
    cargs[0] = rmtDpy;
    try {
      dpy = (DisplayImpl )cons.newInstance(cargs);
    } catch (InvocationTargetException ite) {
      Throwable t = ite.getTargetException();
      if (t instanceof VisADException) {
        throw (VisADException )t;
      } else if (t instanceof ConnectException) {
        throw new VisADException("Couldn't create local shadow for " +
                                 rmtDpy + ": Connection refused");
      } else {
        throw new VisADException("Couldn't create local shadow for " +
                                 rmtDpy + ": " + t.getClass().getName() +
                                 ": " + t.getMessage());
      }
    } catch (Exception e) {
      throw new VisADException("Couldn't create local shadow for " +
                               rmtDpy + ": " + e.getClass().getName() +
                               ": " + e.getMessage());
    }

    return dpy;
  }

  public static LocalDisplay getClientDisplay(RemoteServer client, int index)
    throws RemoteException, VisADException
  {
    return getClientDisplay(client, index, null);
  }

  public static LocalDisplay getClientDisplay(RemoteServer client, int index,
                                              DataReference[] refs)
    throws RemoteException, VisADException
  {
    // fail if there's no remote server
    if (client == null) {
      return null;
    }

    RemoteDisplay rmtDpy = null;

    int loops = 0;
    while (rmtDpy == null && loops < maximumWaitTime) {

      try {
        rmtDpy = client.getDisplay(index);
      } catch (java.rmi.ConnectException ce) {
        ce.printStackTrace();
      }

      // if we didn't get the display, print a message and wait a bit
      if (rmtDpy == null) {
        if (loops == 0) {
          System.err.print("Client waiting for server display #" + index +
                           " ");
        } else {
          System.err.print(".");
        }

        try { Thread.sleep(1000); } catch (InterruptedException ie) { }

        loops++;
      }
    }

    if (rmtDpy == null && loops == maximumWaitTime) {
      System.err.println(" giving up!");
      System.exit(1);
    } else if (loops > 0) {
      System.err.println(". ready");
    }

    if (rmtDpy == null) {
      return null;
    }

    LocalDisplay dpy = wrapRemoteDisplay(rmtDpy);
    if (dpy != null && refs != null) {
      dpy.replaceReferences(rmtDpy, null, refs, null);
    }

    return dpy;
  }

  public static LocalDisplay[] getClientDisplays(RemoteServer client)
    throws RemoteException, VisADException
  {
    return getClientDisplays(client, null);
  }

  public static LocalDisplay[] getClientDisplays(RemoteServer client,
                                                 DataReference[] refs)
    throws RemoteException, VisADException
  {
    // fail if there's no remote server
    if (client == null) {
      return null;
    }

    RemoteDisplay[] rmtDpys = null;

    int loops = 0;
    while (rmtDpys == null && loops < maximumWaitTime) {

      try {
        rmtDpys = client.getDisplays();
      } catch (java.rmi.ConnectException ce) {
      }

      // if we didn't get the display, print a message and wait a bit
      if (rmtDpys == null) {
        if (loops == 0) {
          System.err.print("Client waiting for server displays ");
        } else {
          System.err.print(".");
        }

        try { Thread.sleep(1000); } catch (InterruptedException ie) { }

        loops++;
      }
    }

    if (rmtDpys == null && loops == maximumWaitTime) {
      System.err.println(" giving up!");
      System.exit(1);
    } else if (loops > 0) {
      System.err.println(". ready");
    }

    if (rmtDpys == null) {
      return null;
    }

    LocalDisplay[] dpys = new LocalDisplay[rmtDpys.length];
    for (int i = 0; i < dpys.length; i++) {
      dpys[i] = wrapRemoteDisplay(rmtDpys[i]);
      if (dpys[i] != null && refs != null) {
        dpys[i].replaceReferences(rmtDpys[i], null, refs, null);
      }
    }

    return dpys;
  }

  public static RemoteServerImpl startServer(String serviceName)
    throws RemoteException, VisADException
  {
    // create new server
    RemoteServerImpl server;
    boolean registryStarted = false;
    while (true) {
      boolean success = true;
      try {
        server = new RemoteServerImpl();
        String domain = "///" + serviceName;
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

    return server;
  }
}
