
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
  
      RealType[] time = {RealType.Time};
      RealTupleType time_type = new RealTupleType(time);
      FunctionType time_images = new FunctionType(time_type, image_tuple);

      System.out.println(image_tuple);
      System.out.println(ir_histogram);
  
      // create local DataImpls
      int size = 4;
      FlatField histogram = FlatField.makeField(ir_histogram, size, false);
      Real real = new Real(ir_radiance, 1.0);
      FlatField imaget = FlatField.makeField(image_tuple, size, false);

      // create RemoteData
      Real[] reals = {new Real(vis_radiance, (float) size / 4.0f),
                      new Real(ir_radiance, (float) size / 8.0f)};
      RealTuple val = new RealTuple(reals);
      FlatField temp = (FlatField) imaget.add(val);
      RemoteFieldImpl rem_temp = new RemoteFieldImpl(temp);
      Set time_set = new Linear1DSet(time_type, 0.0, 1.0, 2);
      FieldImpl image_sequence = new FieldImpl(time_images, time_set);
      RemoteFieldImpl rem_image_sequence = new RemoteFieldImpl(image_sequence);


      // create local DataReferenceImpls
      data_refs = new DataReferenceImpl[5];
      data_refs[0] = new DataReferenceImpl("DataReference_0");
      data_refs[1] = new DataReferenceImpl("DataReference_1");
      data_refs[2] = new DataReferenceImpl("DataReference_2");
      data_refs[3] = new DataReferenceImpl("DataReference_3");
      data_refs[4] = new DataReferenceImpl("DataReference_4");

      // link local DataReferenceImpls to local DataImpls
      data_refs[0].setData(histogram);
      data_refs[1].setData(real);
      data_refs[2].setData(imaget);

      // create RemoteDataReferences
      rem_data_refs = new RemoteDataReferenceImpl[5];
      rem_data_refs[0] = new RemoteDataReferenceImpl(data_refs[0]);
      rem_data_refs[1] = new RemoteDataReferenceImpl(data_refs[1]);
      rem_data_refs[2] = new RemoteDataReferenceImpl(data_refs[2]);
      rem_data_refs[3] = new RemoteDataReferenceImpl(data_refs[3]);
      rem_data_refs[4] = new RemoteDataReferenceImpl(data_refs[4]);

      // link RemoteDataReferences to RemoteData
      rem_data_refs[3].setData(rem_temp);
      rem_data_refs[4].setData(rem_image_sequence);

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


