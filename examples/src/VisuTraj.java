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

/* by Dr. Christian C. Mullon, University of Cape Town */

import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class VisuTraj{

    int nbTrajectories ;

    float [][][] vectTrajectories ;
    // -------------------------------------------
    DataReferenceImpl referenceTrajectories ;
    // -------------------------------------------
    RealType latitude ;
    RealType longitude ;
    RealType profondeur ;
    RealType trajectories ;
    // -------------------------------------------
    RealTupleType domain3D ;

// -------------------------------------------
public VisuTraj() throws RemoteException, VisADException {
    nbTrajectories = 60;
    }


// -------------------------------------------
DataImpl makeTrajectories() throws VisADException, RemoteException {
  SampledSet[] setTrajectories = new SampledSet[nbTrajectories];
  for(int t=0;t < nbTrajectories; t ++){
    float [][] traject = new float [3][5];
    for(int i=0;i<3;i++) for(int p=0;p<5;p++)
      traject[i][p] = vectTrajectories[i][p][t];
    setTrajectories[t] = new Gridded3DSet( domain3D, traject,5,null,null,null);
    }
  return new UnionSet(domain3D, setTrajectories);
  }
// -------------------------------------------
public void initVisad(){
 try{
    // -------------------------------------------
    latitude = RealType.getRealType("latitude");
    longitude = RealType.getRealType("longitude");
    profondeur = RealType.getRealType("profondeur");
    trajectories = RealType.getRealType("trajectories");
    // -------------------------------------------
    domain3D = new RealTupleType(latitude, longitude, profondeur);
    // -------------------------------------------
    referenceTrajectories = new DataReferenceImpl("trajectories");
    DataImpl theTrajectories = makeTrajectories();
    referenceTrajectories.setData(theTrajectories);
    // -------------------------------------------
    ScalarMap latMap = new ScalarMap(latitude, Display.YAxis);
    ScalarMap lonMap = new ScalarMap(longitude, Display.XAxis);
    ScalarMap altMap = new ScalarMap(profondeur, Display.ZAxis);
    ScalarMap colMap = new ScalarMap(profondeur, Display.RGBA );
    // -------------------------------------------
    DisplayImpl display = new DisplayImplJ3D("display");
    // -------------------------------------------
    display.addMap( latMap );
    display.addMap( lonMap );
    display.addMap( altMap );
    // -------------------------------------------
    display.addReference(referenceTrajectories);
    // -------------------------------------------
    JFrame jframe = new JFrame("VisAD Tutorial");
    // -------------------------------------------
    jframe.getContentPane().add(display.getComponent());
    jframe.setSize(600, 600);
    jframe.setLocation(300,300);
    // -------------------------------------------
    jframe.setVisible(true);
    }
    catch(Exception e){
      e.printStackTrace();
      System.exit(0);
      }
  }

// -------------------------------------------
public void step(int t){
    int n = 0;
    try{ Thread.sleep(100);
        setData();
        DataImpl theTrajectories = makeTrajectories();
        referenceTrajectories.setData(theTrajectories);
      System.out.println(" iteration "+t);
       }
    catch(Exception e){
      e.printStackTrace();
      System.exit(0);
      }
   }
// -------------------------------------------
public void initData(){
    vectTrajectories = new float[3][5][nbTrajectories];
    for(int p = 0; p<nbTrajectories;p++){
     for(int i=0;i<3;i++) {
        vectTrajectories[i][0][p] =   (float)Math.random()*50.1f;
        for(int t =1; t<5;t++){
          vectTrajectories[i][t][p] =   vectTrajectories[i][t-1][p]+
                                 ((float)Math.random()- 0.5f)* 4.0f;
          }
        }
      }
     }
// -------------------------------------------
public void setData(){
    for(int p = 0; p<nbTrajectories;p++){
     for(int i=0;i<3;i++) {
        for(int t =0; t<4;t++) {
          vectTrajectories[i][t][p] =   vectTrajectories[i][t+1][p];
          }
        vectTrajectories[i][4][p] +=   ((float)Math.random()- 0.5f)* 4.0f;
        }
      }
     }
// -------------------------------------------
public static void main(String[] args) throws RemoteException, VisADException{
   VisuTraj vb = new VisuTraj();
    vb.initData();
    vb.initVisad();
    for(int it = 0;it<500;it++)vb.step(it);
    }

}

