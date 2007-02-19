package edu.wisc.ssec.mcidas;

/**
 * interface for creating Calibrator classes.
 *
 * @version 1.2 16 Nov 1998
 * @author Tommy Jasmin, SSEC
 */

interface Calibrator {

  public static final int CAL_NONE = -1;
  public static final int CAL_MIN  = 1;
  public static final int CAL_RAW  = 1;
  public static final int CAL_RAD  = 2;
  public static final int CAL_ALB  = 3;
  public static final int CAL_TEMP = 4;
  public static final int CAL_BRIT = 5;
  public static final int CAL_MAX  = 5;

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
