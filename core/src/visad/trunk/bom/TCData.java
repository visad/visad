//
// TCData.java
//

package visad.bom;

import visad.*;

public class TCData {

  RealType rtTime = RealType.Time;

  // Fix
  RealType rtFixID;
  RealType rtLat;
  RealType rtLon;
  RealType rtError;
  RealType rtFixStyle;
  RealTupleType fixTuple;
  FunctionType fixFunction;

  // Intensity
  RealType rtIntensityID;
  RealType rtWindMean;
  RealType rtWindGust;
  RealType rtCentralPressure;
  RealType rtCategory;
  RealTupleType intensityTuple;
  FunctionType intensityFunction;

  // Size
  RealType rtSizeID;
  RealType rtGaleRadius;
  RealType rtStormRadius;
  RealType rtHurricaneRadius;
  RealType rtRadiusOfMaximumWinds;
  RealType rtSizeStyle;
  RealTupleType sizeTuple;
  FunctionType sizeFunction;

  // Steering
  RealType rtSteeringID;
  RealType rtSteeringDirection;
  RealType rtSteeringStyle;
  RealTupleType steeringTuple;
  FunctionType steeringFunction;

  // Track
  RealType rtTrackID;
  TextType texttTrackType;
  TextType texttTrackName;
  RealType rtBaseDateTime;
  RealType rtCreateDateTime;
  TextType texttDisplayType;
  TupleType ttTrack;
  FunctionType ftId2Track;

  // Disturbance
  RealType rtDisturbanceID;
  TextType texttCountry;
  TextType texttState;
  RealType rtYear;
  RealType rtNumber;
  TextType texttHistoricalName;
  RealType rtOpenDate;
  RealType rtCloseDate;
  RealType rtArchiveMode;
  RealType rtRealtimeMode;
  TupleType ttDisturbance;
  FunctionType ftId2Disturbance;

  FunctionType mtTC;


  public TCData() throws VisADException {
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
    TextType texttTrackType = new TextType("TrackType");
    TextType texttTrackName = new TextType("TrackName");
    rtBaseDateTime = new RealType("BaseDateTime", null, null);
    rtCreateDateTime = new RealType("CreateDateTime", null, null);
    TextType texttDisplayType = new TextType("DisplayType");
    TupleType ttTrack = new TupleType(new MathType[]
      {texttTrackType, texttTrackName, rtBaseDateTime, rtCreateDateTime,
       texttDisplayType, fixFunction, intensityFunction, sizeFunction,
       steeringFunction});
    FunctionType  ftId2Track = new FunctionType(rtTrackID, ttTrack);

    // Disturbance
    rtDisturbanceID = new RealType("DisturbanceID", null, null);
    TextType texttCountry = new TextType("Country");
    TextType texttState = new TextType("State");
    rtYear = new RealType("Year", null, null);
    rtNumber = new RealType("Number", null, null);
    TextType texttHistoricalName = new TextType("HistoricalName");
    rtOpenDate = new RealType("OpenDate", null, null);
    rtCloseDate = new RealType("CloseDate", null, null);
    rtArchiveMode = new RealType("ArchiveMode", null, null);
    rtRealtimeMode = new RealType("RealtimeMode", null, null);
    TupleType ttDisturbance = new TupleType(new MathType[]
      {texttCountry, texttState, rtYear, rtNumber, texttHistoricalName,
       rtOpenDate, rtCloseDate, rtArchiveMode, rtRealtimeMode, ftId2Track});
    FunctionType ftId2Disturbance =
      new FunctionType(rtDisturbanceID, ttDisturbance);

    mtTC = ftId2Disturbance;
  }

  public MathType getType() {
    return mtTC;
  }

  public static void main(String[] args)
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

