
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

on demedici, in ~/java/visad/java3d:
 
  rmiregistry &

  java visad.java3d.RemoteServerTestImpl
 
on demedici, in ~/java/visad/java3d:
 
  java visad.java3d.RemoteClientTestImpl

*/
public class RemoteClientTestImpl extends UnicastRemoteObject
       implements RemoteClientTest
{

  public RemoteClientTestImpl() throws RemoteException {
    super();
  }
 
  public static void main(String args[]) {

    try {

      System.out.println("RemoteClientTestImpl.main: begin remote activity");

      RemoteServerTest remote_obj = (RemoteServerTest)
        Naming.lookup("//demedici.ssec.wisc.edu/RemoteServerTest");
 
      RemoteDataReference histogram_ref = remote_obj.getDataReference(0);
      RemoteDataReference real_ref = remote_obj.getDataReference(1);
      RemoteDataReference image_ref = remote_obj.getDataReference(2);
      RemoteDataReference temp_ref = remote_obj.getDataReference(3);
      RemoteDataReference image_sequence_ref = remote_obj.getDataReference(4);
 
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

/*
      DisplayImpl display =
        new DisplayImplJ3D("display", DisplayImplJ3D.APPLETFRAME_JAVA3D);
      display.addMap(new ScalarMap(RealType.Latitude, Display.XAxis));
      display.addMap(new ScalarMap(RealType.Longitude, Display.YAxis));
      display.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
      display.addMap(new ScalarMap(vis_radiance, Display.RGB));
      display.addMap(new ConstantMap(0.5, Display.Alpha));
      System.out.println(display);


      RemoteDisplay remote_display = new RemoteDisplayImpl(display);
  
      remote_display.addReference(remote_ref, null);
*/

      delay(1000);
      System.out.println("\ndelay\n");
   
      real_ref.incTick();

      delay(1000);
      System.out.println("\ndelay\n");
   
      real_ref.incTick();
   
      delay(1000);
   
/*
      remote_display.removeReference(remote_ref);
      display.stop();
*/

    }
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

on the server (Irix):

iris 139% rmiregistry &
[1] 21564
iris 140% java visad.RemoteServerTestImpl
RemoteServerTest bound in registry
getDataReference called (Irix)
iris 141% 

on the client (Solaris):

demedici% java visad.RemoteClientTestImpl
FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
FlatField  missing
 
Display
    ScalarMap: Latitude -> DisplayXAxis
    ScalarMap: Longitude -> DisplayYAxis
    ScalarMap: ir_radiance -> DisplayZAxis
    ScalarMap: vis_radiance -> DisplayRGB
    ConstantMap: 0.5 -> DisplayAlpha
 
RemoteClientTestImpl.main: begin remote activity
LevelOfDifficulty = 5 Type = (vis_radiance, ir_radiance)
 LevelOfDifficulty = 4 isDirectManipulation = true
 
delay
 
RemoteClientTestImpl.main: remote_ref.incTick done
LevelOfDifficulty = 5 Type = (vis_radiance, ir_radiance)
 LevelOfDifficulty = 4 isDirectManipulation = true
 
delay
 
LevelOfDifficulty = 5 Type = (vis_radiance, ir_radiance)
 LevelOfDifficulty = 4 isDirectManipulation = true
demedici% 


reconstruction of events:

c RemoteClientTestImpl.main
s   RemoteServerTestImpl.getDataReference
s   DataReferenceImpl.getName
s   RemoteDataReferenceImpl.setData
s     DataReferenceImpl.adaptedSetData
c       RemoteDataImpl.addReference
c         DataImpl.adaptedAddReference
s       DataReferenceImpl.incTick
c         RemoteDataImpl.getType
c           DataImpl.getType
 . . .
c   new RemoteDisplayImpl(display)
c   RemoteDisplayImpl.addReference
 . . .

*/

}

