
//
// RemoteClientTestImpl.java
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

/**
   How to run this test:

BE SURE TO FIRST RUN:  rmic_script
IN BOTH visad and visad/java3d

on the server:
 
  rmiregistry &

  java visad.java3d.RemoteServerTestImpl
 
on the client:
 
  java visad.java3d.RemoteClientTestImpl server.domain.url


for demedici:

  java visad.java3d.RemoteClientTestImpl demedici.ssec.wisc.edu

*/
public class RemoteClientTestImpl extends UnicastRemoteObject
       implements RemoteClientTest
{

  public RemoteClientTestImpl() throws RemoteException {
    super();
  }
 
  public static void main(String args[]) {

    if (args.length < 2) {
    }
    String domain = null;
    int test_case = -1;
    if (args.length > 1) {
      domain = args[0];
      try {
        test_case = Integer.parseInt(args[1]);
      }
      catch(NumberFormatException e) {
        test_case = -1;
      }
    }

    if (test_case < 0) {
      System.out.println("to run RMI tests run\n");
      System.out.println("  java visad.java3d.RemoteClientTestImpl " +
                         "server.domain.url N, where N =\n");
      System.out.println("  0: RemoteField.setSamples with local and remote " +
                         "Data in Data[] argument");
      System.out.println("  1: RemoteDisplay");
      System.out.println("  2: collaborative direct manipulation");
      System.exit(0);
    }

    try {

      System.out.println("RemoteClientTestImpl.main: begin remote activity");
      System.out.println("  to " + domain);

      RemoteServerTest remote_obj = (RemoteServerTest)
        Naming.lookup("//" + domain + "/RemoteServerTest");

      System.out.println("connected");

      RemoteDataReference histogram_ref = remote_obj.getDataReference(0);
      RemoteDataReference real_ref = remote_obj.getDataReference(1);
      RemoteDataReference image_ref = remote_obj.getDataReference(2);
      RemoteDataReference temp_ref = remote_obj.getDataReference(3);
      RemoteDataReference image_sequence_ref = remote_obj.getDataReference(4);

      System.out.println("call setTestCase");

      remote_obj.setTestCase(test_case);

      switch(test_case) {
        default:

          System.out.println("bad test_case value");
          break;

        case 0:

          System.out.println(test_case + ": test RemoteField.setSamples");

          Data real = real_ref.getData();
          Data histogram = histogram_ref.getData();
          Data image = image_ref.getData();
          Data temp = temp_ref.getData();
          Field image_sequence = (Field) image_sequence_ref.getData();

          System.out.println("real type = " + real.getType());
          System.out.println("histogram type = " + histogram.getType());
          System.out.println("image type = " + image.getType());
          System.out.println("temp type = " + temp.getType());
          System.out.println("image_sequence type = " + image_sequence.getType());
    
          Data[] data = new Data[2];
          data[0] = image;
          data[1] = temp;
          image_sequence.setSamples(data, false);
    
          FieldImpl local_image_sequence = (FieldImpl) image_sequence.local();
          System.out.println(local_image_sequence);

          break;
   
        case 1:

          System.out.println(test_case + ": test RemoteDisplay");

          FunctionType ftype = (FunctionType) image_ref.getData().getType();
          RealTupleType dtype = ftype.getDomain();
          RealTupleType rtype = (RealTupleType) ftype.getRange();

          DisplayImpl display =
            new DisplayImplJ3D("display", DisplayImplJ3D.APPLETFRAME);
          display.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                       Display.XAxis));
          display.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                       Display.YAxis));
          display.addMap(new ScalarMap((RealType) rtype.getComponent(0),
                                       Display.ZAxis));
          display.addMap(new ScalarMap((RealType) rtype.getComponent(1),
                                       Display.RGB));
          System.out.println(display);
    
          RemoteDisplay remote_display = new RemoteDisplayImpl(display);
      
          remote_display.addReference(image_ref, null);

          while (true) {
            delay(5000);
            System.out.println("\ndelay\n");
          }

        case 2:
 
          System.out.println(test_case + ": test collaborative direct manipulation");
 
          ftype = (FunctionType) histogram_ref.getData().getType();
          dtype = ftype.getDomain();
          RealType r2type = (RealType) ftype.getRange();
 
          display = new DisplayImplJ3D("display", DisplayImplJ3D.APPLETFRAME);
          display.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                       Display.XAxis));
          display.addMap(new ScalarMap(r2type, Display.YAxis));
          System.out.println(display);
 
          DataReference[] refs1 = {histogram_ref};
          RemoteDisplayImpl remote_display2 = new RemoteDisplayImpl(display);
          remote_display2.addReferences(new DirectManipulationRendererJ3D(),
                                        refs1, null);
 
          while (true) {
            delay(5000);
            System.out.println("\ndelay\n");
          }

      } // end switch(test_case)

    } // end try
    catch (Exception e) {
      System.out.println("RemoteClientTestImpl exception: " + e.getMessage());
      e.printStackTrace(System.out);
    }

    // Applications that export remote objects may not exit (according
    // to the JDK 1.1 release notes).  Here's the work around:
    System.exit(0);

  }

  public static void delay(int millis) throws VisADException {
    try {
      Real r = new Real(0.0);
      synchronized(r) {
        r.wait(millis);
      }
    }
    catch(InterruptedException e) {
    }
  }

/* here's the output:

*/

}

