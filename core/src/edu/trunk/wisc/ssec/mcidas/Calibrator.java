package edu.wisc.ssec.mcidas;

/**
 * interface for creating Calibrator classes.
 *
 * @version 1.2 16 Nov 1998
 * @author Tommy Jasmin, SSEC
 */

public interface Calibrator {

  public static final int CAL_NONE = -1;
  public static final int CAL_MIN  = 1;
  public static final int CAL_RAW  = 1;
  public static final int CAL_RAD  = 2;
  public static final int CAL_ALB  = 3;
  public static final int CAL_TEMP = 4;
  public static final int CAL_BRIT = 5;
  public static final int CAL_MAX  = 5;

  /** Meteosat Second Generation imager. */
  public static final int SENSOR_MSG_IMGR = 51;
  /** GOES 8 imager. */
  public static final int SENSOR_GOES8_IMGR = 70;
  /** GOES 8 sounder. */
  public static final int SENSOR_GOES8_SNDR = 71;
  /** GOES 9 imager. */
  public static final int SENSOR_GOES9_IMGR = 72;
  /** GOES 9 sounder. */
  public static final int SENSOR_GOES9_SNDR = 73;
  /** GOES 10 imager. */
  public static final int SENSOR_GOES10_IMGR = 74;
  /** GOES 10 sounder. */
  public static final int SENSOR_GOES10_SNDR = 75;
  
  public int setCalType (
    int calType
  );

  public float[] calibrate (
    float[] inputData,
    int band,
    int calTypeOut
  );

  public float calibrate (
    float inputPixel,
    int band,
    int calTypeOut
  );

}
