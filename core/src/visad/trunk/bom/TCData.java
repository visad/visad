//
// TCData.java
//

package visad.bom;

import visad.*;
import java.rmi.RemoteException;

public class TCData {

  FieldImpl data = null;
  Object data_lock = new Object();

  // Time
  static RealType rtTime = RealType.Time;

  // Fix
  static RealType rtFixID;
  static RealType rtLat;
  static RealType rtLon;
  static RealType rtError;
  static RealType rtFixStyle;
  static RealTupleType fixTuple;
  static FunctionType fixFunction;

  // Intensity
  static RealType rtIntensityID;
  static RealType rtWindMean;
  static RealType rtWindGust;
  static RealType rtCentralPressure;
  static RealType rtCategory;
  static RealTupleType intensityTuple;
  static FunctionType intensityFunction;

  // Size
  static RealType rtSizeID;
  static RealType rtGaleRadius;
  static RealType rtStormRadius;
  static RealType rtHurricaneRadius;
  static RealType rtRadiusOfMaximumWinds;
  static RealType rtSizeStyle;
  static RealTupleType sizeTuple;
  static FunctionType sizeFunction;

  // Steering
  static RealType rtSteeringID;
  static RealType rtSteeringDirection;
  static RealType rtSteeringStyle;
  static RealTupleType steeringTuple;
  static FunctionType steeringFunction;

  // Track
  static RealType rtTrackID;
  static TextType ttTrackType;
  static TextType ttTrackName;
  static RealType rtBaseDateTime;
  static RealType rtCreateDateTime;
  static TextType ttDisplayType;
  static TupleType ttTrack;
  static FunctionType ftId2Track;

  // Disturbance
  static RealType rtDisturbanceID;
  static TextType ttCountry;
  static TextType ttState;
  static RealType rtYear;
  static RealType rtNumber;
  static TextType ttHistoricalName;
  static RealType rtOpenDate;
  static RealType rtCloseDate;
  static RealType rtArchiveMode;
  static RealType rtRealtimeMode;
  static TupleType ttDisturbance;
  static FunctionType ftId2Disturbance;

  static FunctionType mtTC;


  public TCData() throws VisADException {
    if (mtTC == null) {
      rtTime = RealType.Time;
  
      // Fix
      rtFixID = new RealType("FixID", null, null);
      rtLat = RealType.Latitude;
      rtLon = RealType.Longitude;
      rtError = new RealType("Error", null, null);
      rtFixStyle = new RealType("FixStyle", null, null);
      RealTupleType fixTuple = new RealTupleType(new RealType[]
       {rtFixID, rtLat, rtLon, rtError, rtFixStyle});
      FunctionType fixFunction = new FunctionType(rtTime, fixTuple);
  
      // Intensity
      rtIntensityID = new RealType("IntensityID", null, null);
      rtWindMean = new RealType("WindMean", null, null);
      rtWindGust = new RealType("WindGust", null, null);
      rtCentralPressure = new RealType("CentralPressure", null, null);
      rtCategory = new RealType("Category", null, null);
      RealTupleType intensityTuple = new RealTupleType(new RealType[]
        {rtIntensityID, rtWindMean, rtWindGust, rtCentralPressure, rtCategory});
      FunctionType intensityFunction = new FunctionType(rtTime, intensityTuple);
  
      // Size
      rtSizeID = new RealType("SizeID", null, null);
      rtGaleRadius = new RealType("GaleRadius", null, null);
      rtStormRadius = new RealType("StormRadius", null, null);
      rtHurricaneRadius = new RealType("HurricaneRadius", null, null);
      rtRadiusOfMaximumWinds = new RealType("RadiusOfMaximumWinds", null, null);
      rtSizeStyle = new RealType("SizeStyle", null, null);
      RealTupleType sizeTuple = new RealTupleType(new RealType[]
        {rtSizeID, rtGaleRadius, rtStormRadius, rtHurricaneRadius,
         rtRadiusOfMaximumWinds, rtSizeStyle});
      FunctionType sizeFunction = new FunctionType(rtTime, sizeTuple);
  
      // Steering
      rtSteeringID = new RealType("SteeringID", null, null);
      rtSteeringDirection = new RealType("SteeringDirection", null, null);
      rtSteeringStyle = new RealType("SteeringStyle", null, null);
      RealTupleType steeringTuple = new RealTupleType(new RealType[]
        {rtSteeringID, rtSteeringDirection, rtSteeringStyle});
      FunctionType steeringFunction = new FunctionType(rtTime, steeringTuple);
  
      // Track
      rtTrackID = new RealType("TrackID", null, null);
      TextType ttTrackType = new TextType("TrackType");
      TextType ttTrackName = new TextType("TrackName");
      rtBaseDateTime = new RealType("BaseDateTime", null, null);
      rtCreateDateTime = new RealType("CreateDateTime", null, null);
      TextType ttDisplayType = new TextType("DisplayType");
      TupleType ttTrack = new TupleType(new MathType[]
        {ttTrackType, ttTrackName, rtBaseDateTime, rtCreateDateTime,
         ttDisplayType, fixFunction, intensityFunction, sizeFunction,
         steeringFunction});
      FunctionType  ftId2Track = new FunctionType(rtTrackID, ttTrack);
  
      // Disturbance
      rtDisturbanceID = new RealType("DisturbanceID", null, null);
      TextType ttCountry = new TextType("Country");
      TextType ttState = new TextType("State");
      rtYear = new RealType("Year", null, null);
      rtNumber = new RealType("Number", null, null);
      TextType ttHistoricalName = new TextType("HistoricalName");
      rtOpenDate = new RealType("OpenDate", null, null);
      rtCloseDate = new RealType("CloseDate", null, null);
      rtArchiveMode = new RealType("ArchiveMode", null, null);
      rtRealtimeMode = new RealType("RealtimeMode", null, null);
      TupleType ttDisturbance = new TupleType(new MathType[]
        {ttCountry, ttState, rtYear, rtNumber, ttHistoricalName,
         rtOpenDate, rtCloseDate, rtArchiveMode, rtRealtimeMode, ftId2Track});
      FunctionType ftId2Disturbance =
        new FunctionType(rtDisturbanceID, ttDisturbance);
  
      mtTC = ftId2Disturbance;
    }
  }

  public MathType getType() {
    return mtTC;
  }

  public synchronized void addFix(int disturbanceID, int trackID, double time,
                                  RealTuple fix)
         throws VisADException, RemoteException {
  }

  public synchronized void addIntensity(int disturbanceID, int trackID, double time,
                                        RealTuple intensity)
         throws VisADException, RemoteException {
  }

  public synchronized void addSize(int disturbanceID, int trackID, double time,
                                   RealTuple size)
         throws VisADException, RemoteException {
  }

  public synchronized void addSteering(int disturbanceID, int trackID, double time,
                                       RealTuple steering)
         throws VisADException, RemoteException {
  }

  public synchronized void addTrack(int disturbanceID, int trackID, Tuple track)
         throws VisADException, RemoteException {
  }

  public synchronized void addDisturbance(int disturbanceID, Tuple disturbance)
         throws VisADException, RemoteException {
    float fid = (float) disturbanceID;
    if (data == null) {
      Gridded1DSet set =
        new Gridded1DSet(rtDisturbanceID, new float[][] {{fid}}, 1);
      data = new FieldImpl(mtTC, set);
      data.setSample(0, disturbance);
    }
    else {
      Gridded1DSet set = (Gridded1DSet) data.getDomainSet();
      float[][] ids = set.getSamples(false);
      int length = set.getLength();
      float[][] new_ids = new float[1][length + 1];
      int k = 0;
      int m = -1;
      for (int i=0; i<length+1; i++) {
        if (fid == ids[0][k]) {
          throw new VisADException("disturbanceID " + disturbanceID +
                                   " already used");
        }
        else if (m < 0 && fid < ids[0][k]) {
          new_ids[0][i] = fid;
          m = i;
        }
        else {
          new_ids[0][i] = ids[0][k];
          k++;
        }
      }
      Gridded1DSet new_set =
        new Gridded1DSet(rtDisturbanceID, new_ids, length + 1);
      FieldImpl new_data = new FieldImpl(mtTC, new_set);
      k = 0;
      for (int i=0; i<length+1; i++) {
        if (i == m) {
          new_data.setSample(i, disturbance, false);
        }
        else {
          new_data.setSample(i, data.getSample(k), false);
          k++;
        }
      }
      data = new_data;
    }
  }

  public static Tuple makeDisturbance(String country, String state, int year,
             int number, String historical_name, int open_date, int close_date,
             int archive_mode, int realtime_mode, FieldImpl tracks)
         throws VisADException, RemoteException {
    return new Tuple(new DataImpl[]
      {new Text(ttCountry, country), new Text(ttState, state),
       new Real(rtYear, year), new Real(rtNumber, number),
       new Text(ttHistoricalName, historical_name),
       new Real(rtOpenDate, open_date), new Real(rtCloseDate, close_date),
       new Real(rtArchiveMode, archive_mode),
       new Real(rtRealtimeMode, realtime_mode), tracks});
  }

  public static Tuple makeTrack(String track_type, String track_name,
             int base_date_time, int create_date_time, String display_type,
             FlatField fixes, FlatField intensities, FlatField sizes,
             FlatField steerings)
         throws VisADException, RemoteException {
    return new Tuple(new DataImpl[]
      {new Text(ttTrackType, track_type), new Text(ttTrackName, track_name),
       new Real(rtBaseDateTime, base_date_time),
       new Real(rtCreateDateTime, create_date_time),
       new Text(ttDisplayType, display_type),
       fixes, intensities, sizes, steerings});
  }

  public static FlatField makeFixes(double[] times, RealTuple[] fixes)
         throws VisADException, RemoteException {
    if (times == null || fixes == null || times.length != fixes.length) {
      throw new VisADException("times and fixes must match and be non-null");
    }
    int length = times.length;
    int[] permute = QuickSort.sort(times);
    Gridded1DDoubleSet set =
      new Gridded1DDoubleSet(rtTime, new double[][] {times}, length);
    FlatField field = new FlatField(fixFunction, set);
    for (int i=0; i<length; i++) {
      field.setSample(i, fixes[permute[i]]);
    }
    return field;
  }

  public void main(String[] args)
         throws VisADException {
    MathType mtTC;
    TCData data = new TCData();

    mtTC = data.getType();

    System.out.println("MathType:\n" + mtTC);
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

