
//
// RemoteServerTestImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.java3d;
 
import visad.*;
 
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
 
public class RemoteServerTestImpl extends UnicastRemoteObject
       implements RemoteServerTest
{
  private RemoteDataReferenceImpl ref;
 
  public RemoteServerTestImpl(RemoteDataReferenceImpl r) throws RemoteException {
    super();
    ref = r;
  }
 
  public RemoteDataReference getDataReference() throws RemoteException {
    System.out.println("getDataReference called (" +
                       System.getProperty("os.name") + ")");
    return ref;
  }
 
  public static void main(String args[]) {

    DataReferenceImpl data_ref;
    RemoteDataReferenceImpl rem_data_ref;

    // Create and install a security manager
    System.setSecurityManager(new RMISecurityManager());
 
    try {
      // create rem_data_ref
      data_ref = new DataReferenceImpl("DataReference_A_(" +
                                       System.getProperty("os.name") + ")");
      rem_data_ref = new RemoteDataReferenceImpl(data_ref);

      RemoteServerTestImpl obj = new RemoteServerTestImpl(rem_data_ref);
      Naming.rebind("//demedici.ssec.wisc.edu/RemoteServerTest", obj);
      System.out.println("RemoteServerTest bound in registry");
    }
    catch (Exception e) {
      System.out.println("RemoteServerTestImpl err: " + e.getMessage());
      e.printStackTrace();
    }
  }
}


