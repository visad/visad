
//
// RemoteClientTestImpl.java
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

package visad;
 
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

/**
   How to run this test:

BE SURE TO FIRST RUN:  rmic_script

on demedici, in ~/java/visad:
 
  rmiregistry &

  java visad.RemoteServerTestImpl
 
on demedici, in ~/java/visad:
 
  java visad.RemoteClientTestImpl


OR(?):

on demedici, in ~/java/visad:

  rmiregistry &

  java_g -debug -l1 visad.RemoteServerTestImpl
or
  java_g -debug visad.RemoteServerTestImpl
  jdb -password ######
 
on demedici, in ~/java/visad:
 
  java_g -debug visad.RemoteClientTestImpl

*/
public class RemoteClientTestImpl extends UnicastRemoteObject
       implements RemoteClientTest
{

  public RemoteClientTestImpl() throws RemoteException {
    super();
  }
 
  public static void main(String args[]) {

    try {

      // create local objects

      RealType[] types = {RealType.Latitude, RealType.Longitude};
      RealTupleType earth_location = new RealTupleType(types);
   
      RealType vis_radiance = new RealType("vis_radiance", null, null);
      RealType ir_radiance = new RealType("ir_radiance", null, null);
      RealType[] types2 = {vis_radiance, ir_radiance};
      RealTupleType radiance = new RealTupleType(types2);
   
      FunctionType image_tuple = new FunctionType(earth_location, radiance);
   
      System.out.println(image_tuple);
   
      Integer2DSet Domain2dSet = new Integer2DSet(earth_location, 4, 4);
   
      FlatField image = new FlatField(image_tuple, Domain2dSet);
   
      System.out.println(image);
   
      DisplayImpl display =
        new DisplayImpl("display", DisplayImpl.APPLETFRAME_JAVA3D);
      display.addMap(new ScalarMap(RealType.Latitude, Display.XAxis));
      display.addMap(new ScalarMap(RealType.Longitude, Display.YAxis));
      display.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
      display.addMap(new ScalarMap(vis_radiance, Display.RGB));
      display.addMap(new ConstantMap(0.5, Display.Alpha));
      System.out.println(display);

      System.out.println("RemoteClientTestImpl.main: begin remote activity");

      RemoteField remote_image = new RemoteFieldImpl(image);

      RemoteServerTest remote_obj =
        (RemoteServerTest) Naming.lookup("//demedici.ssec.wisc.edu/RemoteServerTest");

      RemoteDataReference remote_ref = remote_obj.getDataReference();

      remote_ref.setData(remote_image);

      RemoteDisplay remote_display = new RemoteDisplayImpl(display);
  
      remote_display.addReference(remote_ref, null);

      delay(1000);
      System.out.println("\ndelay\n");
   
      remote_ref.incTick();

      delay(1000);
      System.out.println("\ndelay\n");
   
      remote_ref.incTick();
   
      delay(1000);
   
      remote_display.removeReference(remote_ref);
  
      display.stop();

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

