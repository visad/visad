//
// TCData.java
//

package visad.bom;

import visad.*;
import visad.util.Util;
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
      rtFixID = RealType.getRealType("FixID", null, null);
      rtLat = RealType.Latitude;
      rtLon = RealType.Longitude;
      rtError = RealType.getRealType("Error", null, null);
      rtFixStyle = RealType.getRealType("FixStyle", null, null);
      RealTupleType fixTuple = new RealTupleType(new RealType[]
       {rtFixID, rtLat, rtLon, rtError, rtFixStyle});
      fixFunction = new FunctionType(rtTime, fixTuple);
  
      // Intensity
      rtIntensityID = RealType.getRealType("IntensityID", null, null);
      rtWindMean = RealType.getRealType("WindMean", null, null);
      rtWindGust = RealType.getRealType("WindGust", null, null);
      rtCentralPressure = RealType.getRealType("CentralPressure", null, null);
      rtCategory = RealType.getRealType("Category", null, null);
      RealTupleType intensityTuple = new RealTupleType(new RealType[]
        {rtIntensityID, rtWindMean, rtWindGust, rtCentralPressure, rtCategory});
      intensityFunction = new FunctionType(rtTime, intensityTuple);
  
      // Size
      rtSizeID = RealType.getRealType("SizeID", null, null);
      rtGaleRadius = RealType.getRealType("GaleRadius", null, null);
      rtStormRadius = RealType.getRealType("StormRadius", null, null);
      rtHurricaneRadius = RealType.getRealType("HurricaneRadius", null, null);
      rtRadiusOfMaximumWinds = RealType.getRealType("RadiusOfMaximumWinds", null, null);
      rtSizeStyle = RealType.getRealType("SizeStyle", null, null);
      RealTupleType sizeTuple = new RealTupleType(new RealType[]
        {rtSizeID, rtGaleRadius, rtStormRadius, rtHurricaneRadius,
         rtRadiusOfMaximumWinds, rtSizeStyle});
      sizeFunction = new FunctionType(rtTime, sizeTuple);
  
      // Steering
      rtSteeringID = RealType.getRealType("SteeringID", null, null);
      rtSteeringDirection = RealType.getRealType("SteeringDirection", null, null);
      rtSteeringStyle = RealType.getRealType("SteeringStyle", null, null);
      RealTupleType steeringTuple = new RealTupleType(new RealType[]
        {rtSteeringID, rtSteeringDirection, rtSteeringStyle});
      steeringFunction = new FunctionType(rtTime, steeringTuple);
  
      // Track
      rtTrackID = RealType.getRealType("TrackID", null, null);
      ttTrackType = TextType.getTextType("TrackType");
      ttTrackName = TextType.getTextType("TrackName");
      rtBaseDateTime = RealType.getRealType("BaseDateTime", null, null);
      rtCreateDateTime = RealType.getRealType("CreateDateTime", null, null);
      ttDisplayType = TextType.getTextType("DisplayType");
      ttTrack = new TupleType(new MathType[]
        {ttTrackType, ttTrackName, rtBaseDateTime, rtCreateDateTime,
         ttDisplayType, fixFunction, intensityFunction, sizeFunction,
         steeringFunction});
      ftId2Track = new FunctionType(rtTrackID, ttTrack);
  
      // Disturbance
      rtDisturbanceID = RealType.getRealType("DisturbanceID", null, null);
      ttCountry = TextType.getTextType("Country");
      ttState = TextType.getTextType("State");
      ttHistoricalName = TextType.getTextType("HistoricalName");
      rtYear = RealType.getRealType("Year", null, null);
      rtNumber = RealType.getRealType("Number", null, null);
      rtOpenDate = RealType.getRealType("OpenDate", null, null);
      rtCloseDate = RealType.getRealType("CloseDate", null, null);
      rtArchiveMode = RealType.getRealType("ArchiveMode", null, null);
      rtRealtimeMode = RealType.getRealType("RealtimeMode", null, null);
      TupleType ttDisturbance = new TupleType(new MathType[]
        {ttCountry, ttState, rtYear, rtNumber, ttHistoricalName,
         rtOpenDate, rtCloseDate, rtArchiveMode, rtRealtimeMode, ftId2Track});
      FunctionType ftId2Disturbance =
        new FunctionType(rtDisturbanceID, ttDisturbance);
  
      mtTC = ftId2Disturbance;
    }
  }

  public FieldImpl getData() {
    return data;
  }

  public MathType getType() {
    return mtTC;
  }

  public synchronized void addFix(int disturbanceID, int trackID, double time,
                                  RealTuple fix)
         throws VisADException, RemoteException {
    addToTrack(disturbanceID, trackID, time, 5, fixFunction, fix);
  }

  public synchronized void addIntensity(int disturbanceID, int trackID, double time,
                                        RealTuple intensity)
         throws VisADException, RemoteException {
    addToTrack(disturbanceID, trackID, time, 6, intensityFunction, intensity);
  }

  public synchronized void addSize(int disturbanceID, int trackID, double time,
                                   RealTuple size)
         throws VisADException, RemoteException {
    addToTrack(disturbanceID, trackID, time, 7, sizeFunction, size);
  }

  public synchronized void addSteering(int disturbanceID, int trackID, double time,
                                       RealTuple steering)
         throws VisADException, RemoteException {
    addToTrack(disturbanceID, trackID, time, 8, steeringFunction, steering);
  }

  private void addToTrack(int disturbanceID, int trackID, double time,
                          int tuple_index, FunctionType function_type,
                          RealTuple rt)
         throws VisADException, RemoteException {

    Tuple disturbance = getDisturbance(disturbanceID);
    if (disturbance == null) {
      throw new VisADException("invalid disturbanceID");
    }
    Tuple track = getTrack(trackID, disturbance);
    if (track == null) {
      throw new VisADException("invalid trackID");
    }
    FlatField field = (FlatField) track.getComponent(tuple_index);
    Gridded1DDoubleSet set = (Gridded1DDoubleSet) field.getDomainSet();
    double[][] times = set.getDoubles(false);
    int length = set.getLength();
    double[][] new_times = new double[1][length + 1];
    float[][] values = field.getFloats(false);
    int dim = values.length;
    float[][] new_values = new float[dim][length + 1];
    int k = 0;
    int m = -1;
    for (int i=0; i<length+1; i++) {
      if (Util.isApproximatelyEqual(time, times[0][k])) {
        throw new VisADException("time " + time + " already used");
      }
      else if (m < 0 && time < times[0][k]) {
        new_times[0][i] = time;
        // mark as missing until new_field.setSample(m, rt) call
        for (int j=0; j<dim; j++) new_values[j][i] = Float.NaN;
        m = i;
      }
      else {
        new_times[0][i] = times[0][k];
        for (int j=0; j<dim; j++) new_values[j][i] = values[j][k];
        k++;
      }
    }
    Gridded1DDoubleSet new_set = 
      new Gridded1DDoubleSet(rtTime, new_times, length + 1);
    FlatField new_field = new FlatField(function_type, new_set);
    new_field.setSamples(new_values, false);
    new_field.setSample(m, rt);

    Data[] comps = new Data[]
      {track.getComponent(0),
       track.getComponent(1),
       track.getComponent(2),
       track.getComponent(3),
       track.getComponent(4),
       track.getComponent(5),
       track.getComponent(6),
       track.getComponent(7),
       track.getComponent(8)};
    comps[tuple_index] = new_field;
    Tuple new_track = new Tuple(new Data[]
      {comps[0], comps[1], comps[2], comps[3], comps[4], comps[5],
       comps[6], comps[7], comps[8]});
    setTrack(trackID, new_track, disturbance);
    setDisturbance(disturbanceID, disturbance);
  }

  public static FieldImpl makeTrackField(int trackID, Tuple track)
         throws VisADException, RemoteException {

    float fid = (float) trackID;

    Gridded1DSet set =
      new Gridded1DSet(rtTrackID, new float[][] {{fid}}, 1);
    FieldImpl field = new FieldImpl(ftId2Track, set);
    field.setSample(0, track);
    return field;
  }


  public synchronized void addTrack(int disturbanceID, int trackID, Tuple track)
         throws VisADException, RemoteException {
    Tuple disturbance = getDisturbance(disturbanceID);
    if (disturbance == null) {
      throw new VisADException("invalid disturbanceID");
    }

    FieldImpl field = (FieldImpl) disturbance.getComponent(9);

    // desired field has MathType ftId2Track;
    // now we want to add a particular track to this field
    // in an analagous manner to adding a disturbance (ie the addDisturbance method)

    /* wlh comments:
    // not necessary since field is mutable
    // so merge find* methods into get* methods and eliminate set* methods ****
    // setDisturbance(disturbanceID, disturbance);
    */

    float fid = (float) trackID;
    FieldImpl new_field = null;
    if (field == null) {
      Gridded1DSet set =
        new Gridded1DSet(rtTrackID, new float[][] {{fid}}, 1);
      new_field = new FieldImpl(ftId2Track, set);
      new_field.setSample(0, track);
    }
    else {
      Gridded1DSet set = (Gridded1DSet) field.getDomainSet();
      float[][] ids = set.getSamples(false);
      int length = set.getLength();
      float[][] new_ids = new float[1][length + 1];
      int k = 0;
      int m = -1;
      for (int i=0; i<length+1; i++) {
        if (fid == ids[0][k]) {
          throw new VisADException("trackID " + trackID +
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
        new Gridded1DSet(rtTrackID, new_ids, length + 1);
      new_field = new FieldImpl(ftId2Track, new_set);
      k = 0;
      for (int i=0; i<length+1; i++) {
        if (i == m) {
          new_field.setSample(i, track, false);
        }
        else {
          new_field.setSample(i, field.getSample(k), false);
          k++;
        }
      }
    }
    Tuple new_disturbance = new Tuple(new Data[]
      {disturbance.getComponent(0),
       disturbance.getComponent(1),
       disturbance.getComponent(2),
       disturbance.getComponent(3),
       disturbance.getComponent(4),
       disturbance.getComponent(5),
       disturbance.getComponent(6),
       disturbance.getComponent(7),
       disturbance.getComponent(8),
       new_field});
    setDisturbance(disturbanceID, new_disturbance);
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
    int index = findDisturbance(disturbanceID);
    if (index < 0) return null;
    else return (Tuple) data.getSample(index);
  }

  private void setDisturbance(int disturbanceID, Tuple disturbance)
          throws VisADException, RemoteException {
    int index = findDisturbance(disturbanceID);
    if (index >= 0) data.setSample(index, disturbance);
  }

  private int findDisturbance(int disturbanceID) 
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

    //jk : allow for null sizes
    if (sizes == null) sizes = TCData.makeMissingSizes();
    if (steerings == null) steerings = TCData.makeMissingSteerings();
    // should also have same test for fixes & intensities & steerings
    // + methods makeMissingFixes and makeMissingIntensities ?

    return new Tuple(new DataImpl[]
      {new Text(ttTrackType, track_type), new Text(ttTrackName, track_name),
       new Real(rtBaseDateTime, base_date_time),
       new Real(rtCreateDateTime, create_date_time),
       new Text(ttDisplayType, display_type),
       fixes, intensities, sizes, steerings});
  }

  
  /**
   * jk:
   * create a flatfield of Disturbance (Tropical Cyclone) Sizes
   * with values set to "missing"
   * This allows for the case when the database has no entries yet,
   * but means we can still create some TCData
   *
   */

  public static FlatField makeMissingSizes() 
         throws VisADException, RemoteException {

    //
    // make a SIZE and store in the FlatField ffSizes
    //
    double[] daTimes = {Double.NaN};
    int[] iaSizeIds = {-1};
    float[] faGale_radii = {Float.NaN};
    float[] faStorm_radii = {Float.NaN};
    float[] faHurricane_radii = {Float.NaN};
    float[] faRadii_of_maximum_winds = {Float.NaN};
    int[] iaSizeStyles = {-1};

    FlatField ffSizes = TCData.makeSizes( daTimes, iaSizeIds, faGale_radii,
                                     faStorm_radii, faHurricane_radii, faRadii_of_maximum_winds,
                                     iaSizeStyles);
    return ffSizes;
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

  /**
   * create a bunch of "intensities" which are measurements of 
   * the intensity of a Tropical Cyclone at particular times
   *
   * input: arrays of times, ids, wind_means...
   * output: a field of mathType intensityFunction, which is represented by:
   *         (time -> intensityTuple)
   */

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

  /** any ids < 0 or size_styles < 0 are treated as missing */
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
      pids[i] = (ids[permute[i]] < 0) ? Float.NaN : ids[permute[i]];
      pgale_radii[i] = gale_radii[permute[i]];
      pstorm_radii[i] = storm_radii[permute[i]];
      phurricane_radii[i] = hurricane_radii[permute[i]];
      pradii_of_maximum_winds[i] = radii_of_maximum_winds[permute[i]];
      psize_styles[i] =
        (size_styles[permute[i]] < 0) ? Float.NaN : size_styles[permute[i]];
    }
    float[][] values = {pids, pgale_radii, pstorm_radii, phurricane_radii,
                        pradii_of_maximum_winds, psize_styles};
    field.setSamples(values, false);
    return field;
  }

  public static FlatField makeMissingSteerings() 
         throws VisADException, RemoteException {

    double[] daTimes = {Double.NaN};
    int[] iaSteeringIds = {-1};
    float[] faSteering_directions = {Float.NaN};
    int[] iaSteeringStyles = {-1};

    FlatField ffSteerings = TCData.makeSteerings( daTimes, iaSteeringIds, faSteering_directions,
                                     iaSteeringStyles);

    return ffSteerings;
  }


  /** any ids < 0 or steering_styles < 0 are treated as missing */
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
      pids[i] = (ids[permute[i]] < 0) ? Float.NaN : ids[permute[i]];
      psteering_directions[i] = steering_directions[permute[i]];
      psteering_styles[i] =
        (steering_styles[permute[i]] < 0) ? Float.NaN : steering_styles[permute[i]];
    }
    float[][] values = {pids, psteering_directions, psteering_styles};
    field.setSamples(values, false);
    return field;
  }

  public static void main(String[] args)
         throws VisADException, RemoteException {
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

