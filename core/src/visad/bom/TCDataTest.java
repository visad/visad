/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

//
// TCDataTest.java
//
// Version 3 April 2001

package visad.bom;

import visad.*;
import java.rmi.RemoteException;
// import visad.java3d.DisplayImplJ3D;
import visad.java2d.DisplayImplJ2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Test program to demonstrate TCData, a class for representing
 * Tropical Cyclones in VisAD
 * 
 * Creates some static TC Data, and displays a simple plot
 *
 */

public class TCDataTest {


  public static void main(String[] args)
         throws VisADException, RemoteException {
    MathType mtTC;
    TCData data = new TCData();

    mtTC = data.getType();

    System.out.println("MathType:\n" + mtTC);

    // Step 1
    // Aim: set up the Data Model
    //
    // make a FIX and store in the FlatField ffFixes
    //
    double[] daTimes = {0.0d, 1000.0d, 2000.0d};
    // int[] iaFixIds = {0, 1, 2};
    float[] faLats = {10.0f, 11.0f, 12.0f};
    float[] faLons = {160.0f, 165.0f, 170.0f};
    float[] faErrors = {0.0f, 0.0f, 0.0f};
    int[] iaConfidence = {2, 3, 4};
    int[] iaLocationStyles = {2, 3, 4};
    float[] faWind_means = {50.0f, 60.0f, 70.0f};
    float[] faWind_gusts = {60.0f, 70.0f, 80.0f};
    float[] faCentral_pressures = {990.0f, 985.0f, 980.0f};
    int[] iaCategories = {2, 3, 4};
    int[] iaIntensityStyles = {2, 3, 4};
    float[] faGaleRadii = {200.0f, 210.0f, 220.0f};
    float[] faStormRadii = {100.0f, 110.0f, 120.0f};
    float[] faHurricaneRadii = {60.0f, 70.0f, 80.0f};
    float[] faRadiiOfMaximumWinds = {50.0f, 60.0f, 70.0f};
    int[] iaSizeStyles = {2, 3, 4};
    float[] faDepth = {200.0f, 200.0f, 200.0f};
    float[] faEyeDiameter = {50.0f, 60.0f, 70.0f};
    float[] faPressureOfLastClosedIsobar = {200.0f, 210.0f, 220.0f};
    int[] iaStructureStyles = {2, 3, 4};

    FlatField ffFixes =
      TCData.makeLocations(daTimes, faLats, faLons, 
                           faErrors, iaConfidence, iaLocationStyles, faWind_means,
                           faWind_gusts, faCentral_pressures, iaCategories,
                           iaIntensityStyles, faGaleRadii, faStormRadii, faHurricaneRadii,
                           faRadiiOfMaximumWinds, iaSizeStyles, faDepth, faEyeDiameter,
                           faPressureOfLastClosedIsobar, iaStructureStyles);

 // System.out.println("ffIntensities:\n" + ffFixes);


    //
    // make an INTENSITY and store in the FlatField ffIntensities
    //

    // FlatField ffIntensities = TCData.makeIntensities( daTimes, iaIntensityIds, faWind_means,
    //                                 faWind_gusts, faCentral_pressures, iaCategories);
    // System.out.println("ffIntensities:\n" + ffIntensities);

    //
    // make a SIZE and store in the FlatField ffSizes
    //

    // FlatField ffSizes = TCData.makeSizes( daTimes, iaSizeIds, faGale_radii,
    //                                 faStorm_radii, faHurricane_radii, faRadii_of_maximum_winds,
    //                                 iaSizeStyles);
    // System.out.println("ffSizes:\n" + ffSizes);

    //
    // make a STEERING and store in the FlatField ffSteering
    //

    // FlatField ffSteerings = TCData.makeSteerings( daTimes, iaSteeringIds, faSteering_directions,
    //                                 iaSteeringStyles);
    // System.out.println("ffSteerings:\n" + ffSteerings);

    //
    // now make a TRACK
    //
    String sTrackType = new String("Observed");
    String sTrackName = new String("RealTime");
    int iBaseDateTime = 4000;
    int iCreateDateTime = 5000;
    String sDisplayType = new String("IDunno");

    Tuple tTrack = TCData.makeTrack(sTrackType, sTrackName, iBaseDateTime, iCreateDateTime, sDisplayType, ffFixes);
    //                                ffFixes, ffIntensities, ffSizes, ffSteerings);

    // System.out.println("tTrack:\n" + tTrack);

    //
    // now make a field of TRACKs
    //
    int iTrackID = 0;
    FieldImpl fiTrack = TCData.makeTrackField(iTrackID, tTrack);

    // System.out.println("fiTrack:\n" + fiTrack);

    //
    // now make a disturbance
    //
    String sCountry = new String("Australia");
    String sState = new String("WA");
    int iYear = 2000;
    int iNumber = 0;
    String sHistoricalName = new String("Olga");
    int iOpenDate = 5000;
    int iCloseDate = 15000;
    int iArchiveMode = 0;
    int iRealTimeMode = 0;

    Tuple tDisturbance = TCData.makeDisturbance(sCountry, sState, iYear, iNumber,
                                 sHistoricalName, iOpenDate, iCloseDate, iArchiveMode,
                                 iRealTimeMode, fiTrack);

    // System.out.println("tDisturbance:\n" + tDisturbance);

    //
    // now make a field of disturbances
    //
    TCData tcd = new TCData();
    tcd.addDisturbance(0, tDisturbance);
    FieldImpl fiTCD = tcd.getData();
    // System.out.println("TCData:\n" + fiTCD);

    // Step 2
    // Aim: create the View Model
    // Result: display which is an implementation of the visad.Display interface
    DisplayImpl display = new DisplayImplJ2D("display");
    display.addMap(new ScalarMap(TCData.rtTime, Display.XAxis));
    display.addMap(new ScalarMap(TCData.rtCentralPressure, Display.YAxis));

    // Step 3
    // Aim: create the Communication Model
    // Result: dri which is an implementation of the visad.DataReference interface
    DataReferenceImpl driTC = new DataReferenceImpl("TC");
    // driTC.setData(ffFixes);
    driTC.setData(tTrack);

    // link the View Model (display) to the Data Model (via the DataReference)
      display.addReference(driTC, null);

    // Step 4
    // Do the swing magic
    JFrame frame = new JFrame("TCData Display Test");
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });


     JPanel panel = new JPanel();
     panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
     panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
     panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
     frame.getContentPane().add(panel);
     panel.add(display.getComponent());
     frame.setSize(300, 300);
     frame.setVisible(true);

/*
C:\jamesk\java\tc\visad\bom>java visad.bom.TCDataTest
MathType:
(DISTURBANCEID -> (COUNTRY(Text),
                   STATE(Text),
                   YEAR,
                   NUM,
                   HISTORICALNAME(Text),
                   OPENDATE,
                   CLOSEDATE,
                   ARCHIVEMODE,
                   REALTIMEMODE,
                   (TRACKID -> (TRACKTYPE(Text),
                                TRACKNAME(Text),
                                BASEDATETIME,
                                CREATEDATETIME,
                                TRACKSTYLE(Text),
                                (Time -> (Latitude,
                                          Longitude,
                                          ERROR,
                                          CONFIDENCE,
                                          LOCATIONSTYLE,
                                          WINDMEAN,
                                          WINDGUST,
                                          CENTRALPRESSURE,
                                          CATEGORY,
                                          INTENSITYSTYLE,
                                          GALERADIUS,
                                          STORMRADIUS,
                                          HURRICANERADIUS,
                                          RADIUSOFMAXIMUMWINDS,
                                          SIZESTYLE,
                                          DEPTH,
                                          EYEDIAMETER,
                                          PRESSUREOFLASTCLOSEDISOBAR,
                                          STRUCTURESTYLE))))))

 
 was:
 
doll% java visad.bom.TCData
MathType:
(DisturbanceID -> (Country(Text),
                   State(Text),
                   Year,
                   Number,
                   HistoricalName(Text),
                   OpenDate,
                   CloseDate,
                   ArchiveMode,
                   RealtimeMode,
                   (TrackID -> (TrackType(Text),
                                TrackName(Text),
                                BaseDateTime,
                                CreateDateTime,
                                DisplayType(Text),
                                (Time -> (FixID, Latitude, Longitude, Error, FixStyle)),
                                (Time -> (IntensityID, WindMean, WindGust, CentralPressure, Category)),
                                (Time -> (SizeID, GaleRadius, StormRadius, HurricaneRadius, RadiusOfMaximumWinds, SizeStyle)),
                                (Time -> (SteeringID, SteeringDirection, SteeringStyle))))))

*/
  }
}

