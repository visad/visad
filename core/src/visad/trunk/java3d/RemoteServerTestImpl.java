
//
// RemoteServerTestImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
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
  private RemoteDataReferenceImpl[] refs;
 
  public RemoteServerTestImpl(RemoteDataReferenceImpl[] r) throws RemoteException {
    super();
    refs = r;
  }
 
  public RemoteDataReference getDataReference(int i) throws RemoteException {
    System.out.println("getDataReference called (" +
                       System.getProperty("os.name") + ")");
    if (0 <= i && i < refs.length) return refs[i];
    else return null;
  }
 
  public static void main(String args[]) {

    DataReferenceImpl[] data_refs;
    RemoteDataReferenceImpl[] rem_data_refs;

    // Create and install a security manager
    System.setSecurityManager(new RMISecurityManager());
 
    try {

      RealType vis_radiance = new RealType("vis_radiance", null, null);
      RealType ir_radiance = new RealType("ir_radiance", null, null);
      RealType count = new RealType("count", null, null);
   
      RealType[] types = {RealType.Latitude, RealType.Longitude};
      RealTupleType earth_location = new RealTupleType(types);
   
      RealType[] types2 = {vis_radiance, ir_radiance};
      RealTupleType radiance = new RealTupleType(types2);
   
      FunctionType image_tuple = new FunctionType(earth_location, radiance);
      FunctionType ir_histogram = new FunctionType(ir_radiance, count);
  
      System.out.println(image_tuple);
      System.out.println(ir_histogram);
  
      int size = 64;
      FlatField histogram = FlatField.makeField(ir_histogram, size, false);
      FlatField imaget = FlatField.makeField(image_tuple, size, false);
      Real real = new Real(ir_radiance, 1.0);

      // create rem_data_ref
      data_refs = new DataReferenceImpl[3];
      rem_data_refs = new RemoteDataReferenceImpl[3];
      data_refs[0] = new DataReferenceImpl("DataReference_0");
      data_refs[1] = new DataReferenceImpl("DataReference_1");
      data_refs[2] = new DataReferenceImpl("DataReference_2");
      rem_data_refs[0] = new RemoteDataReferenceImpl(data_refs[0]);
      rem_data_refs[1] = new RemoteDataReferenceImpl(data_refs[1]);
      rem_data_refs[2] = new RemoteDataReferenceImpl(data_refs[2]);
      data_refs[0].setData(histogram);
      data_refs[1].setData(imaget);
      data_refs[2].setData(real);

      RemoteServerTestImpl obj = new RemoteServerTestImpl(rem_data_refs);
      Naming.rebind("//demedici.ssec.wisc.edu/RemoteServerTest", obj);
      System.out.println("RemoteServerTest bound in registry");
    }
    catch (Exception e) {
      System.out.println("RemoteServerTestImpl err: " + e.getMessage());
      e.printStackTrace();
    }
  }
}


