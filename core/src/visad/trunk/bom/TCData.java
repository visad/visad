//
// TCData.java
//

package visad.bom;

import visad.*;
import java.rmi.RemoteException;

public class TCData {

  // this is the actual TC data object
  FieldImpl data = null;

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
    Tuple disturbance = getDisturbance(disturbanceID);
    if (disturbance == null) {
      throw new VisADException("invalid disturbanceID");
    }
    FieldImpl field = (FieldImpl) disturbance.getComponent(9);


    // not necessary since field is mutable
    // so merge find* methods into get* methods and eliminate set* methods ****
    // setDisturbance(disturbanceID, disturbance);
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

  private Tuple getDisturbance(int disturbanceID)
          throws VisADException, RemoteException {
    int index = findDistrubance(disturbanceID);
    if (index < 0) return null;
    else return (Tuple) data.getSample(index);
  }

  private void setDisturbance(int disturbanceID, Tuple disturbance)
          throws VisADException, RemoteException {
    int index = findDistrubance(disturbanceID);
    if (index >= 0) data.setSample(index, disturbance);
  }

  private int findDistrubance(int disturbanceID) 
          throws VisADException, RemoteException {
    if (data == null) {
      return -1;
    }
    Gridded1DSet set = (Gridded1DSet) data.getDomainSet();
    float[][] ids = set.getSamples(false);
    int length = set.getLength();
    float fid = disturbanceID;
    for (int i=0; i<length; i++) {
      if (ids[0][i] == fid) return i;
    }
    return -1;
  }

  private Tuple getTrack(int trackID, Tuple disturbance)
          throws VisADException, RemoteException {
    int index = findTrack(trackID, disturbance);
    if (index < 0) return null;
    else {
      FieldImpl field = (FieldImpl) disturbance.getComponent(9);
      return (Tuple) field.getSample(index);
    }
  }

  private void setTrack(int trackID, Tuple track, Tuple disturbance)
          throws VisADException, RemoteException {
    int index = findTrack(trackID, disturbance);
    if (index >= 0) {
      FieldImpl field = (FieldImpl) disturbance.getComponent(9);
      field.setSample(index, track);
    }
  }

  private int findTrack(int trackID, Tuple disturbance)
          throws VisADException, RemoteException {
    if (disturbance == null) {
      return -1;
    }
    FieldImpl field = (FieldImpl) disturbance.getComponent(9);
    if (field == null) {
      return -1;
    }
    Gridded1DSet set = (Gridded1DSet) field.getDomainSet();
    float[][] ids = set.getSamples(false);
    int length = set.getLength();
    float fid = trackID;
    for (int i=0; i<length; i++) {
      if (ids[0][i] == fid) return i;
    }
    return -1;
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

  public static FlatField makeFixes(double[] times, int[] ids, float[] lats,
              float[] lons, float[] errors, int[] fix_styles)
         throws VisADException, RemoteException {
    if (times == null || ids == null || lats == null || lons == null ||
        errors == null || fix_styles == null) {
      throw new VisADException("arguments may not be null");
    }
    int length = times.length;
    if (ids.length != length || lats.length != length || lons.length != length ||
        errors.length != length || fix_styles.length != length) {
      throw new VisADException("argument lengths must match");
    }
    int[] permute = QuickSort.sort(times);
    Gridded1DDoubleSet set =
      new Gridded1DDoubleSet(rtTime, new double[][] {times}, length);
    FlatField field = new FlatField(fixFunction, set);
    float[] pids = new float[length];
    float[] plats = new float[length];
    float[] plons = new float[length];
    float[] perrors = new float[length];
    float[] pfix_styles = new float[length];
    for (int i=0; i<length; i++) {
      pids[i] = ids[permute[i]];
      plats[i] = lats[permute[i]];
      plons[i] = lons[permute[i]];
      perrors[i] = errors[permute[i]];
      pfix_styles[i] = fix_styles[permute[i]];
    }
    float[][] values = {pids, plats, plons, perrors, pfix_styles};
    field.setSamples(values, false);
    return field;
  }

  public static FlatField makeIntensities(double[] times, int[] ids,
              float[] wind_means, float[] wind_gusts, float[] central_pressures,
              int[] categories)
         throws VisADException, RemoteException {
    if (times == null || ids == null || wind_means == null || wind_gusts == null ||
        central_pressures == null || categories == null) {
      throw new VisADException("arguments may not be null");
    }
    int length = times.length;
    if (ids.length != length || wind_means.length != length ||
        wind_gusts.length != length || central_pressures.length != length ||
        categories.length != length) {
      throw new VisADException("argument lengths must match");
    }
    int[] permute = QuickSort.sort(times);
    Gridded1DDoubleSet set =
      new Gridded1DDoubleSet(rtTime, new double[][] {times}, length);
    FlatField field = new FlatField(intensityFunction, set);
    float[] pids = new float[length];
    float[] pwind_means = new float[length];
    float[] pwind_gusts = new float[length];
    float[] pcentral_pressures = new float[length];
    float[] pcategories = new float[length];
    for (int i=0; i<length; i++) {
      pids[i] = ids[permute[i]];
      pwind_means[i] = wind_means[permute[i]];
      pwind_gusts[i] = wind_gusts[permute[i]];
      pcentral_pressures[i] = central_pressures[permute[i]];
      pcategories[i] = categories[permute[i]];
    }
    float[][] values = {pids, pwind_means, pwind_gusts, pcentral_pressures,
                        pcategories};
    field.setSamples(values, false);
    return field;
  }

  public static FlatField makeSizes(double[] times, int[] ids, 
              float[] gale_radii, float[] storm_radii, float[] hurricane_radii,
              float[] radii_of_maximum_winds, int[] size_styles)
         throws VisADException, RemoteException {
    if (times == null || ids == null || gale_radii == null ||
        storm_radii == null || hurricane_radii == null ||
        radii_of_maximum_winds == null || size_styles == null) {
      throw new VisADException("arguments may not be null");
    }
    int length = times.length;
    if (ids.length != length || gale_radii.length != length || 
        storm_radii.length != length || hurricane_radii.length != length || 
        radii_of_maximum_winds.length != length || size_styles.length != length) {
      throw new VisADException("argument lengths must match");
    }
    int[] permute = QuickSort.sort(times);
    Gridded1DDoubleSet set =
      new Gridded1DDoubleSet(rtTime, new double[][] {times}, length);
    FlatField field = new FlatField(sizeFunction, set);
    float[] pids = new float[length];
    float[] pgale_radii = new float[length];
    float[] pstorm_radii = new float[length];
    float[] phurricane_radii = new float[length];
    float[] pradii_of_maximum_winds = new float[length];
    float[] psize_styles = new float[length];
    for (int i=0; i<length; i++) {
      pids[i] = ids[permute[i]];
      pgale_radii[i] = gale_radii[permute[i]];
      pstorm_radii[i] = storm_radii[permute[i]];
      phurricane_radii[i] = hurricane_radii[permute[i]];
      pradii_of_maximum_winds[i] = radii_of_maximum_winds[permute[i]];
      psize_styles[i] = size_styles[permute[i]];
    }
    float[][] values = {pids, pgale_radii, pstorm_radii, phurricane_radii,
                        pradii_of_maximum_winds, psize_styles};
    field.setSamples(values, false);
    return field;
  }

  public static FlatField makeSteerings(double[] times, int[] ids,
              float[] steering_directions, int[] steering_styles)
         throws VisADException, RemoteException {
    if (times == null || ids == null || steering_directions == null ||
        steering_styles == null) {
      throw new VisADException("arguments may not be null");
    }
    int length = times.length;
    if (ids.length != length || steering_directions.length != length ||
        steering_styles.length != length) {
      throw new VisADException("argument lengths must match");
    }
    int[] permute = QuickSort.sort(times);
    Gridded1DDoubleSet set =
      new Gridded1DDoubleSet(rtTime, new double[][] {times}, length);
    FlatField field = new FlatField(steeringFunction, set);
    float[] pids = new float[length];
    float[] psteering_directions = new float[length];
    float[] psteering_styles = new float[length];
    for (int i=0; i<length; i++) {
      pids[i] = ids[permute[i]];
      psteering_directions[i] = steering_directions[permute[i]];
      psteering_styles[i] = steering_styles[permute[i]];
    }
    float[][] values = {pids, psteering_directions, psteering_styles};
    field.setSamples(values, false);
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

