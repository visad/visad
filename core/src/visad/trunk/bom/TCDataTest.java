//
// TCDataTest.java
//

package visad.bom;

import visad.*;
import java.rmi.RemoteException;
// import visad.java3d.DisplayImplJ3D;
import visad.java2d.DisplayImplJ2D;
import visad.util.VisADSlider;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;

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
    int[] iaFixIds = {0, 1, 2};
    float[] faLats = {10.0f, 11.0f, 12.0f};
    float[] faLons = {160.0f, 165.0f, 170.0f};
    float[] faErrors = {0.0f, 0.0f, 0.0f};
    int[] iaFix_styles = {2, 3, 4};

    FlatField ffFixes = TCData.makeFixes( daTimes, iaFixIds, faLats, faLons, 
                                     faErrors, iaFix_styles);
    // System.out.println("ffIntensities:\n" + ffFixes);


    //
    // make an INTENSITY and store in the FlatField ffIntensities
    //
    int[] iaIntensityIds = {0, 1, 2};
    float[] faWind_means = {50.0f, 60.0f, 70.0f};
    float[] faWind_gusts = {60.0f, 70.0f, 80.0f};
    float[] faCentral_pressures = {990.0f, 985.0f, 980.0f};
    int[] iaCategories = {2, 3, 4};

    FlatField ffIntensities = TCData.makeIntensities( daTimes, iaIntensityIds, faWind_means,
                                     faWind_gusts, faCentral_pressures, iaCategories);
    // System.out.println("ffIntensities:\n" + ffIntensities);

    //
    // make a SIZE and store in the FlatField ffSizes
    //
    int[] iaSizeIds = {0, 1, 2};
    float[] faGale_radii = {150.0f, 160.0f, 170.0f};
    float[] faStorm_radii = {100.0f, 110.0f, 120.0f};
    float[] faHurricane_radii = {50.0f, 60.0f, 70.0f};
    float[] faRadii_of_maximum_winds = {50.0f, 60.0f, 70.0f};
    int[] iaSizeStyles = {2, 3, 4};

    FlatField ffSizes = TCData.makeSizes( daTimes, iaSizeIds, faGale_radii,
                                     faStorm_radii, faHurricane_radii, faRadii_of_maximum_winds,
                                     iaSizeStyles);
    // System.out.println("ffSizes:\n" + ffSizes);

    //
    // make a STEERING and store in the FlatField ffSteering
    //
    int[] iaSteeringIds = {0, 1, 2};
    float[] faSteering_directions = {150.0f, 160.0f, 170.0f};
    int[] iaSteeringStyles = {2, 3, 4};

    FlatField ffSteerings = TCData.makeSteerings( daTimes, iaSteeringIds, faSteering_directions,
                                     iaSteeringStyles);
    // System.out.println("ffSteerings:\n" + ffSteerings);

    //
    // now make a TRACK
    //
    String sTrackType = new String("Observed");
    String sTrackName = new String("RealTime");
    int iBaseDateTime = 4000;
    int iCreateDateTime = 5000;
    String sDisplayType = new String("IDunno");

    Tuple tTrack = TCData.makeTrack(sTrackType, sTrackName, iBaseDateTime, iCreateDateTime, sDisplayType,
                                    ffFixes, ffIntensities, ffSizes, ffSteerings);

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
    driTC.setData(ffIntensities);

    // link the View Model (display) to the Data Model (via the DataReference)
      display.addReference(driTC, null);

    // Step 4
    // Do the swing magic
    JFrame frame = new JFrame("Point Display Test");
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

