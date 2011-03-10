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

import java.util.Random;

import visad.FlatField;
import visad.LocalDisplay;
import visad.MessageEvent;
import visad.MessageListener;
import visad.RemoteDisplayImpl;
import visad.VisADException;

import visad.java2d.DisplayImplJ2D;

////////////////////////////////////////
import visad.FunctionType;
import visad.RemoteFieldImpl;
import visad.Set;
////////////////////////////////////////

public class TestMsg
{
  private static java.util.Random rand = null;

  private int numClients = 6;
  private int numMessages = 10;

  private long randSeed = -1;
  private boolean verbose = false;

  public TestMsg(String[] args)
  {
    if (!processArgs(args)) {
      System.err.println("Exiting...");
      System.exit(1);
    }

    if (rand == null) {
      if (randSeed == -1) {
        rand = new Random();
      } else {
        rand = new Random(randSeed);
      }
    }

    LocalDisplay[] dpys = createAllDisplays();
    if (dpys == null) {
      System.err.println("Couldn't create Displays!");
      System.exit(1);
      return;
    }

    float[][] samples = new float[1][1];
    samples[0][0] = 0.0f;

    StupidData data;
    try {
      Set set = new visad.Gridded1DSet(visad.RealType.Generic, samples,
                                       samples.length);

      data = new StupidData(set);
      data.setSamples(samples);
    } catch (RemoteException re) {
      re.printStackTrace();
      data = null;
    } catch (VisADException ve) {
      ve.printStackTrace();
      data = null;
    }

    for (int i = 0; i < numMessages; i++) {
      int n = rand.nextInt() % dpys.length;
      if (n < 0) {
        n = -n;
      }

      try {
        samples[0][0] += 1.0f;
        data.setSamples(samples);
      } catch (RemoteException re) {
        re.printStackTrace();
        data = null;
      } catch (VisADException ve) {
        ve.printStackTrace();
        data = null;
      }

      String msgStr = "Msg#" + i + " from dpy#" + n;
      try {
        dpys[n].sendMessage(new MessageEvent(msgStr,
                                             new RemoteFieldImpl(data)));
      } catch (RemoteException re) {
        System.err.println("Couldn't send message \"" + msgStr + "\":");
        re.printStackTrace();
      }

      try { Thread.sleep(1000); } catch (InterruptedException ie) { }
    }

    try { Thread.sleep(5000); } catch (InterruptedException ie) { }

    System.exit(0);
  }

  private LocalDisplay[] createAllDisplays()
  {
    DisplayImplJ2D srvr;
    try {
      srvr = new DisplayImplJ2D("root");
    } catch (RemoteException re) {
      return null;
    } catch (VisADException ve) {
      return null;
    }
    srvr.addMessageListener(new MsgListener("server"));

    LocalDisplay[] dpys = new LocalDisplay[numClients+1];
    dpys[0] = srvr;

    RemoteDisplayImpl rmtSrvr;
    try {
      rmtSrvr = new RemoteDisplayImpl(srvr);
    } catch (RemoteException re) {
      return null;
    }

    for (int i = 0; i < numClients; i++) {
      try {
        dpys[i+1] = new DisplayImplJ2D(rmtSrvr);
      } catch (RemoteException re) {
        return null;
      } catch (VisADException ve) {
        return null;
      }

      dpys[i+1].addMessageListener(new MsgListener("cli#" + i));
    }

    return dpys;
  }

  public boolean processArgs(String[] args)
  {
    boolean usage = false;

    String className = getClass().getName();
    int pt = className.lastIndexOf('.');
    final int ds = className.lastIndexOf('$');
    if (ds > pt) {
      pt = ds;
    }
    String progName = className.substring(pt == -1 ? 0 : pt + 1);

    for (int i = 0; args != null && i < args.length; i++) {
      if (args[i].length() > 0 && args[i].charAt(0) == '-') {
        char ch = args[i].charAt(1);

        String str, result;

        switch (ch) {
        case 'c':
          str = (args[i].length() > 2 ? args[i].substring(2) :
                 ((i + 1) < args.length ? args[++i] : null));
          if (str == null) {
            System.err.println(progName +
                               ": Missing number of clients for \"-c\"");
            usage = true;
          } else {
            try {
              numClients = Integer.parseInt(str);
            } catch (NumberFormatException nfe) {
              System.err.println(progName +
                                 ": Bad number of clients \"" + str + "\"");
              numClients = 2;
              usage = true;
            }

            if (numClients < 1) {
              System.err.println(progName +
                                 ": Need at least one client!");
              usage = true;
            }
          }
          break;
        case 'm':
          str = (args[i].length() > 2 ? args[i].substring(2) :
                 ((i + 1) < args.length ? args[++i] : null));
          if (str == null) {
            System.err.println(progName +
                               ": Missing number of messages for \"-m\"");
            usage = true;
          } else {
            try {
              numMessages = Integer.parseInt(str);
            } catch (NumberFormatException nfe) {
              System.err.println(progName +
                                 ": Bad number of messages \"" + str + "\"");
              numMessages = 2;
              usage = true;
            }

            if (numMessages < 1) {
              System.err.println(progName +
                                 ": Need at least one client!");
              usage = true;
            }
          }
          break;
        case 's':
          str = (args[i].length() > 2 ? args[i].substring(2) :
                 ((i + 1) < args.length ? args[++i] : null));
          if (str == null) {
            System.err.println(progName +
                               ": Missing random seed value for \"-s\"");
            usage = true;
          } else {
            try {
              randSeed = Long.parseLong(str);
            } catch (NumberFormatException nfe) {
              System.err.println(progName +
                                 ": Bad random seed value \"" + str + "\"");
              usage = true;
            }
          }
          break;
        case 'v':
          verbose = true;
          break;
        default:
          System.err.println(progName +
                             ": Unknown option \"-" + ch + "\"");
          usage = true;
          break;
        }
      } else {
        System.err.println(progName + ": Unknown keyword \"" + args[i] + "\"");
        usage = true;
      }
    }

    if (usage) {
      System.err.println("Usage: " + getClass().getName() +
                         " [-c numClients]" +
                         " [-m numMessages]" +
                         " [-s randomSeed]" +
                         " [-v(erbose)]" +
                         "");
    }

    return !usage;
  }

  class MsgListener
    implements MessageListener
  {
    private String name;

    public MsgListener(String name)
    {
      this.name = name;
    }

    public void receiveMessage(MessageEvent msg)
      throws RemoteException
    {
      System.out.println(name + ": " + msg);
    }
  }

  public static void main(String[] args)
  {
    new TestMsg(args);
  }

  class StupidData
    extends FlatField
  {
    public StupidData(Set set)
      throws VisADException
    {
      super(FunctionType.REAL_1TO1_FUNCTION, set);
    }
  }
}
