import java.rmi.Naming;
import java.rmi.RemoteException;

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
  boolean dumpDpy = false;
  boolean startServer = false;
  String hostName = null;

  public TestSkeleton() { }

  public TestSkeleton(String args[])
  throws VisADException, RemoteException
  {
    if (!processArgs(args)) {
      System.err.println("Exiting...");
      System.exit(1);
    }
    startThreads();
  }

  int checkExtraOption(char ch, int argc, String args[])
  {
    return 0;
  }

  String extraOptionUsage() { return ""; }

  int checkExtraKeyword(int argc, String args[])
  {
    return 0;
  }

  String extraKeywordUsage() { return ""; }

  boolean hasClientServerMode() { return true; }

  public boolean processArgs(String args[])
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
	  case 'd':
	    dumpDpy = true;
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

  public void startThreads()
	throws VisADException, RemoteException
  {
    start(dumpDpy);
  }

  abstract DisplayImpl[] setupData()
	throws VisADException, RemoteException;

  void dumpDisplayImpl(DisplayImpl dpy, String name)
  {
    boolean FULL_DUMP = false;

    System.out.println("#### " + name + " ####");

    java.awt.Component comp = dpy.getComponent();
    System.out.println("Component=<" + comp + ">");

    java.util.Vector cmv = dpy.getConstantMapVector();
    System.out.println("ConstantMapVector=<" + cmv + ">");

    if (FULL_DUMP) {
      java.util.Vector cv = dpy.getControlVector();
      System.out.println("ControlVector=<" + cv + ">");
    }

    if (FULL_DUMP) {
      visad.DisplayRenderer dr = dpy.getDisplayRenderer();
      System.out.println("DisplayRenderer=<" + dr + ">");
    }

    int dsnum = dpy.getDisplayScalarCount();
    System.out.println("" + dsnum + " DisplayScalars:");
    for (int i = 0; i < dsnum; i++) {
      visad.DisplayRealType ds = dpy.getDisplayScalar(i);
      System.out.println("\t#" + i + "=<" + ds + ">");
    }

    if (FULL_DUMP) {
      visad.GraphicsModeControl gmc = dpy.getGraphicsModeControl();
      System.out.println("GraphicsModeControl=<" + gmc + ">");
    }

    java.util.Vector mv = dpy.getMapVector();
    System.out.println("MapVector=<" + mv + ">");

    if (FULL_DUMP) {
      visad.ProjectionControl pc = dpy.getProjectionControl();
      System.out.println("ProjectionControl=<" + pc + ">");
    }

    if (FULL_DUMP) {
      java.util.Vector rv = dpy.getRendererVector();
      System.out.println("RendererVector=<" + rv + ">");
    }

    int snum = dpy.getScalarCount();
    System.out.println("" + snum + " Scalars:");
    for (int i = 0; i < snum; i++) {
      visad.ScalarType s = dpy.getScalar(i);
      System.out.println("\t#" + i + "=<" + s + ">");
    }

    int vnum = dpy.getValueArrayLength();
    System.out.println("" + vnum + " ValueArray entries:");
    int[] vm = dpy.getValueToMap();
    int[] vs = dpy.getValueToScalar();
    for (int i = 0; i < vnum; i++) {
      System.out.println("\t#" + i + ": map=" + vm[i] + ", scalar=" + vs[i]);
    }
  }

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

  void getClientDataReferences(RemoteServer client)
	throws RemoteException
  {
  }

  DisplayImpl[] setupClientData()
	throws VisADException, RemoteException
  {
    RemoteServer client;
    try {
      String domain = "//" + hostName + "/" + getClass().getName();
      client = (RemoteServer )Naming.lookup(domain);
    } catch (Exception e) {
      throw new VisADException ("Cannot connect to server on \"" +
				hostName + "\"");
    }

    RemoteDisplay[] rmtDpy = client.getDisplays();
    if (rmtDpy == null) {
      throw new VisADException("No RemoteDisplays found!");
    }

    DisplayImpl[] dpys = new DisplayImpl[rmtDpy.length];
    for (int i = 0; i < dpys.length; i++) {
      String dpyClass = rmtDpy[i].getDisplayClassName();
      if (dpyClass.endsWith(".DisplayImplJ3D")) {
	dpys[i] = new DisplayImplJ3D(rmtDpy[i]);
      } else {
	dpys[i] = new DisplayImplJ2D(rmtDpy[i]);
      }
    }

    // add any data references to server
    getClientDataReferences(client);

    return dpys;
  }

  void setServerDataReferences(RemoteServerImpl server)
	throws RemoteException
  {
  }

  RemoteServerImpl setupServer(DisplayImpl[] dpys)
	throws VisADException, RemoteException
  {
    // create new server
    RemoteServerImpl server;
    try {
      server = new RemoteServerImpl();
      String domain = "//:/" + getClass().getName();
      Naming.rebind(domain, server);
    } catch (Exception e) {
      throw new VisADException("Cannot set up server" + 
			       " (rmiregistry may not be running)");
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
	throws VisADException, RemoteException
  {
  }

  void start(boolean dumpDpy)
	throws VisADException, RemoteException
  {
    DisplayImpl[] displays;
    if (hostName != null) {
      displays = setupClientData();
    } else {
      displays = setupData();
    }

    if (dumpDpy) {
      for (int i = 0; i < displays.length; i++) {
	dumpDisplayImpl(displays[i], "Display#" + i);
      }
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
